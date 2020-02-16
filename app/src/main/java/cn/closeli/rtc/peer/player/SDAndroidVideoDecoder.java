package cn.closeli.rtc.peer.player;

import android.media.MediaCodecInfo;
import android.media.MediaCrypto;
import android.media.MediaFormat;
import android.media.MediaCodec.BufferInfo;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.view.Surface;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import org.webrtc.EglBase.Context;
import org.webrtc.EncodedImage;
import org.webrtc.EncodedImage.FrameType;
import org.webrtc.JavaI420Buffer;
import org.webrtc.Logging;
import org.webrtc.NV12Buffer;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.ThreadUtils;
import org.webrtc.ThreadUtils.ThreadChecker;
import org.webrtc.VideoCodecInfo;
import org.webrtc.VideoCodecStatus;
import org.webrtc.VideoDecoder;
import org.webrtc.VideoFrame;
import org.webrtc.VideoFrame.Buffer;
import org.webrtc.VideoFrame.I420Buffer;
import org.webrtc.VideoSink;
import org.webrtc.YuvHelper;

public class SDAndroidVideoDecoder implements VideoDecoder, VideoSink {
    private static final String TAG = "SDAndroidVideoDecoder";
    private static final String MEDIA_FORMAT_KEY_STRIDE = "stride";
    private static final String MEDIA_FORMAT_KEY_SLICE_HEIGHT = "slice-height";
    private static final String MEDIA_FORMAT_KEY_CROP_LEFT = "crop-left";
    private static final String MEDIA_FORMAT_KEY_CROP_RIGHT = "crop-right";
    private static final String MEDIA_FORMAT_KEY_CROP_TOP = "crop-top";
    private static final String MEDIA_FORMAT_KEY_CROP_BOTTOM = "crop-bottom";
    private static final int MEDIA_CODEC_RELEASE_TIMEOUT_MS = 5000;
    private static final int DEQUEUE_INPUT_TIMEOUT_US = 500000;
    private static final int DEQUEUE_OUTPUT_BUFFER_TIMEOUT_US = 100000;
    private final SDMediaCodecWrapperFactory mediaCodecWrapperFactory;
    private final String codecName;
    private final SDVideoCodecType codecType;
    private final BlockingDeque<FrameInfo> frameInfos;
    private int colorFormat;
    @Nullable
    private Thread outputThread;
    private ThreadChecker outputThreadChecker;
    private ThreadChecker decoderThreadChecker;
    private volatile boolean running;
    @Nullable
    private volatile Exception shutdownException;
    private final Object dimensionLock = new Object();
    private int width;
    private int height;
    private int stride;
    private int sliceHeight;
    private boolean hasDecodedFirstFrame;
    private boolean keyFrameRequired;
    @Nullable
    private final Context sharedContext;
    @Nullable
    private SurfaceTextureHelper surfaceTextureHelper;
    @Nullable
    private Surface surface;
    private final Object renderedTextureMetadataLock = new Object();
    @Nullable
    private DecodedTextureMetadata renderedTextureMetadata;
    @Nullable
    private Callback callback;
    @Nullable
    private SDMediaCodecWrapper codec;

    public SDAndroidVideoDecoder(SDMediaCodecWrapperFactory mediaCodecWrapperFactory, String codecName, SDVideoCodecType codecType, int colorFormat, @Nullable Context sharedContext) {
        if (!this.isSupportedColorFormat(colorFormat)) {
            throw new IllegalArgumentException("Unsupported color format: " + colorFormat);
        } else {
            Logging.d(TAG, "ctor name: " + codecName + " type: " + codecType + " color format: " + colorFormat + " context: " + sharedContext);
            this.mediaCodecWrapperFactory = mediaCodecWrapperFactory;
            this.codecName = codecName;
            this.codecType = codecType;
            this.colorFormat = colorFormat;
            this.sharedContext = sharedContext;
            this.frameInfos = new LinkedBlockingDeque();
        }
    }

    public VideoCodecStatus initDecode(Settings settings, Callback callback) {
        this.decoderThreadChecker = new ThreadChecker();
        this.callback = callback;
        if (this.sharedContext != null) {
            this.surfaceTextureHelper = this.createSurfaceTextureHelper();
            this.surface = new Surface(this.surfaceTextureHelper.getSurfaceTexture());
            this.surfaceTextureHelper.startListening(this);
        }

        return this.initDecodeInternal(settings.width, settings.height);
    }

    private VideoCodecStatus initDecodeInternal(int width, int height) {
        this.decoderThreadChecker.checkIsOnValidThread();
        Logging.d(TAG, "initDecodeInternal name: " + this.codecName + " type: " + this.codecType + " width: " + width + " height: " + height);
        if (this.outputThread != null) {
            Logging.e(TAG, "initDecodeInternal called while the codec is already running");
            return VideoCodecStatus.FALLBACK_SOFTWARE;
        } else {
            this.width = width;
            this.height = height;
            this.stride = width;
            this.sliceHeight = height;
            this.hasDecodedFirstFrame = false;
            this.keyFrameRequired = true;

            try {
                this.codec = this.mediaCodecWrapperFactory.createByCodecName(this.codecName);
            } catch (IllegalArgumentException | IOException var4) {
                Logging.e(TAG, "Cannot create media decoder " + this.codecName);
                return VideoCodecStatus.FALLBACK_SOFTWARE;
            }

            try {
                MediaFormat format = MediaFormat.createVideoFormat(this.codecType.mimeType(), width, height);
                if (this.sharedContext == null) {
                    format.setInteger("color-format", this.colorFormat);
                }

                this.codec.configure(format, this.surface, (MediaCrypto)null, 0);
                this.codec.start();
            } catch (IllegalStateException var5) {
                Logging.e(TAG, "initDecode failed", var5);
                this.release();
                return VideoCodecStatus.FALLBACK_SOFTWARE;
            }

            this.running = true;
            this.outputThread = this.createOutputThread();
            this.outputThread.start();
            Logging.d(TAG, "initDecodeInternal done");
            return VideoCodecStatus.OK;
        }
    }

    public VideoCodecStatus decode(EncodedImage frame, DecodeInfo info) {
//        this.decoderThreadChecker.checkIsOnValidThread();
        if (this.codec != null && this.callback != null) {
            if (frame.buffer == null) {
                Logging.e(TAG, "decode() - no input data");
                return VideoCodecStatus.ERR_PARAMETER;
            } else {
                int size = frame.buffer.remaining();
                if (size == 0) {
                    Logging.e(TAG, "decode() - input buffer empty");
                    return VideoCodecStatus.ERR_PARAMETER;
                } else {
                    int width;
                    int height;
                    synchronized(this.dimensionLock) {
                        width = this.width;
                        height = this.height;
                    }

                    if (frame.encodedWidth * frame.encodedHeight > 0 && (frame.encodedWidth != width || frame.encodedHeight != height)) {
                        VideoCodecStatus status = this.reinitDecode(frame.encodedWidth, frame.encodedHeight);
                        if (status != VideoCodecStatus.OK) {
                            return status;
                        }
                    }

                    if (this.keyFrameRequired) {
                        if (frame.frameType != FrameType.VideoFrameKey) {
                            Logging.e(TAG, "decode() - key frame required first");
                            return VideoCodecStatus.NO_OUTPUT;
                        }

                        if (!frame.completeFrame) {
                            Logging.e(TAG, "decode() - complete frame required first");
                            return VideoCodecStatus.NO_OUTPUT;
                        }
                    }

                    int index;
                    try {
                        index = this.codec.dequeueInputBuffer(500000L);
                    } catch (IllegalStateException var11) {
                        Logging.e(TAG, "dequeueInputBuffer failed", var11);
                        return VideoCodecStatus.ERROR;
                    }

                    if (index < 0) {
                        Logging.e(TAG, "decode() - no HW buffers available; decoder falling behind");
                        return VideoCodecStatus.ERROR;
                    } else {
                        ByteBuffer buffer;
                        try {
                            buffer = this.codec.getInputBuffers()[index];
                        } catch (IllegalStateException var10) {
                            Logging.e(TAG, "getInputBuffers failed", var10);
                            return VideoCodecStatus.ERROR;
                        }

                        if (buffer.capacity() < size) {
                            Logging.e(TAG, "decode() - HW buffer too small");
                            return VideoCodecStatus.ERROR;
                        } else {
                            buffer.put(frame.buffer);
                            this.frameInfos.offer(new FrameInfo(SystemClock.elapsedRealtime(), frame.rotation));

                            try {
                                this.codec.queueInputBuffer(index, 0, size, TimeUnit.NANOSECONDS.toMicros(frame.captureTimeNs), 0);
                            } catch (IllegalStateException var9) {
                                Logging.e(TAG, "queueInputBuffer failed", var9);
                                this.frameInfos.pollLast();
                                return VideoCodecStatus.ERROR;
                            }

                            if (this.keyFrameRequired) {
                                this.keyFrameRequired = false;
                            }

                            return VideoCodecStatus.OK;
                        }
                    }
                }
            }
        } else {
            Logging.d(TAG, "decode uninitalized, codec: " + (this.codec != null) + ", callback: " + this.callback);
            return VideoCodecStatus.UNINITIALIZED;
        }
    }

    public boolean getPrefersLateDecoding() {
        return true;
    }

    public String getImplementationName() {
        return this.codecName;
    }

    public VideoCodecStatus release() {
        Logging.d(TAG, "release");
        VideoCodecStatus status = this.releaseInternal();
        if (this.surface != null) {
            this.releaseSurface();
            this.surface = null;
            this.surfaceTextureHelper.stopListening();
            this.surfaceTextureHelper.dispose();
            this.surfaceTextureHelper = null;
        }

        synchronized(this.renderedTextureMetadataLock) {
            this.renderedTextureMetadata = null;
        }

        this.callback = null;
        this.frameInfos.clear();
        return status;
    }

    private VideoCodecStatus releaseInternal() {
        if (!this.running) {
            Logging.d(TAG, "release: Decoder is not running.");
            return VideoCodecStatus.OK;
        } else {
            VideoCodecStatus var1;
            try {
                this.running = false;
                if (ThreadUtils.joinUninterruptibly(this.outputThread, 5000L)) {
                    if (this.shutdownException == null) {
                        return VideoCodecStatus.OK;
                    }

                    Logging.e(TAG, "Media decoder release error", new RuntimeException(this.shutdownException));
                    this.shutdownException = null;
                    var1 = VideoCodecStatus.ERROR;
                    return var1;
                }

                Logging.e(TAG, "Media decoder release timeout", new RuntimeException());
                var1 = VideoCodecStatus.TIMEOUT;
            } finally {
                this.codec = null;
                this.outputThread = null;
            }

            return var1;
        }
    }

    private VideoCodecStatus reinitDecode(int newWidth, int newHeight) {
        this.decoderThreadChecker.checkIsOnValidThread();
        VideoCodecStatus status = this.releaseInternal();
        return status != VideoCodecStatus.OK ? status : this.initDecodeInternal(newWidth, newHeight);
    }

    private Thread createOutputThread() {
        return new Thread("AndroidVideoDecoder.outputThread") {
            public void run() {
                SDAndroidVideoDecoder.this.outputThreadChecker = new ThreadChecker();

                while(SDAndroidVideoDecoder.this.running) {
                    SDAndroidVideoDecoder.this.deliverDecodedFrame();
                }

                SDAndroidVideoDecoder.this.releaseCodecOnOutputThread();
            }
        };
    }

    protected void deliverDecodedFrame() {
        this.outputThreadChecker.checkIsOnValidThread();

        try {
            BufferInfo info = new BufferInfo();
            int result = this.codec.dequeueOutputBuffer(info, 100000L);
            if (result == -2) {
                this.reformat(this.codec.getOutputFormat());
                return;
            }

            if (result < 0) {
                Logging.v(TAG, "dequeueOutputBuffer returned " + result);
                return;
            }

            FrameInfo frameInfo = (FrameInfo)this.frameInfos.poll();
            Integer decodeTimeMs = null;
            int rotation = 0;
            if (frameInfo != null) {
                decodeTimeMs = (int)(SystemClock.elapsedRealtime() - frameInfo.decodeStartTimeMs);
                rotation = frameInfo.rotation;
            }

            this.hasDecodedFirstFrame = true;
            if (this.surfaceTextureHelper != null) {
                this.deliverTextureFrame(result, info, rotation, decodeTimeMs);
            } else {
                this.deliverByteFrame(result, info, rotation, decodeTimeMs);
            }
        } catch (IllegalStateException var6) {
            Logging.e(TAG, "deliverDecodedFrame failed", var6);
        }

    }

    private void deliverTextureFrame(int index, BufferInfo info, int rotation, Integer decodeTimeMs) {
        int width;
        int height;
        synchronized(this.dimensionLock) {
            width = this.width;
            height = this.height;
        }

        synchronized(this.renderedTextureMetadataLock) {
            if (this.renderedTextureMetadata != null) {
                this.codec.releaseOutputBuffer(index, false);
            } else {
                this.surfaceTextureHelper.setTextureSize(width, height);
                this.surfaceTextureHelper.setFrameRotation(rotation);
                this.renderedTextureMetadata = new DecodedTextureMetadata(info.presentationTimeUs, decodeTimeMs);
                this.codec.releaseOutputBuffer(index, true);
            }
        }
    }

    public void onFrame(VideoFrame frame) {
        Integer decodeTimeMs;
        long timestampNs;
        synchronized(this.renderedTextureMetadataLock) {
            if (this.renderedTextureMetadata == null) {
                throw new IllegalStateException("Rendered texture metadata was null in onTextureFrameAvailable.");
            }

            timestampNs = this.renderedTextureMetadata.presentationTimestampUs * 1000L;
            decodeTimeMs = this.renderedTextureMetadata.decodeTimeMs;
            this.renderedTextureMetadata = null;
        }

        VideoFrame frameWithModifiedTimeStamp = new VideoFrame(frame.getBuffer(), frame.getRotation(), timestampNs);
        this.callback.onDecodedFrame(frameWithModifiedTimeStamp, decodeTimeMs, (Integer)null);
    }

    private void deliverByteFrame(int result, BufferInfo info, int rotation, Integer decodeTimeMs) {
        int width;
        int height;
        int stride;
        int sliceHeight;
        synchronized(this.dimensionLock) {
            width = this.width;
            height = this.height;
            stride = this.stride;
            sliceHeight = this.sliceHeight;
        }

        if (info.size < width * height * 3 / 2) {
            Logging.e(TAG, "Insufficient output buffer size: " + info.size);
        } else {
            if (info.size < stride * height * 3 / 2 && sliceHeight == height && stride > width) {
                stride = info.size * 2 / (height * 3);
            }

            ByteBuffer buffer = this.codec.getOutputBuffers()[result];
            buffer.position(info.offset);
            buffer.limit(info.offset + info.size);
            buffer = buffer.slice();
            Buffer frameBuffer;
            if (this.colorFormat == 19) {
                frameBuffer = this.copyI420Buffer(buffer, stride, sliceHeight, width, height);
            } else {
                frameBuffer = this.copyNV12ToI420Buffer(buffer, stride, sliceHeight, width, height);
            }

            this.codec.releaseOutputBuffer(result, false);
            long presentationTimeNs = info.presentationTimeUs * 1000L;
            VideoFrame frame = new VideoFrame(frameBuffer, rotation, presentationTimeNs);
            this.callback.onDecodedFrame(frame, decodeTimeMs, (Integer)null);
            frame.release();
        }
    }

    private Buffer copyNV12ToI420Buffer(ByteBuffer buffer, int stride, int sliceHeight, int width, int height) {
        return (new NV12Buffer(width, height, stride, sliceHeight, buffer, (Runnable)null)).toI420();
    }

    private Buffer copyI420Buffer(ByteBuffer buffer, int stride, int sliceHeight, int width, int height) {
        if (stride % 2 != 0) {
            throw new AssertionError("Stride is not divisible by two: " + stride);
        } else {
            int chromaWidth = (width + 1) / 2;
            int chromaHeight = sliceHeight % 2 == 0 ? (height + 1) / 2 : height / 2;
            int uvStride = stride / 2;
//            int yPos = false;
            int yEnd = 0 + stride * height;
            int uPos = 0 + stride * sliceHeight;
            int uEnd = uPos + uvStride * chromaHeight;
            int vPos = uPos + uvStride * sliceHeight / 2;
            int vEnd = vPos + uvStride * chromaHeight;
            I420Buffer frameBuffer = this.allocateI420Buffer(width, height);
            buffer.limit(yEnd);
            buffer.position(0);
            this.copyPlane(buffer.slice(), stride, frameBuffer.getDataY(), frameBuffer.getStrideY(), width, height);
            buffer.limit(uEnd);
            buffer.position(uPos);
            this.copyPlane(buffer.slice(), uvStride, frameBuffer.getDataU(), frameBuffer.getStrideU(), chromaWidth, chromaHeight);
            ByteBuffer dataV;
            if (sliceHeight % 2 == 1) {
                buffer.position(uPos + uvStride * (chromaHeight - 1));
                dataV = frameBuffer.getDataU();
                dataV.position(frameBuffer.getStrideU() * chromaHeight);
                dataV.put(buffer);
            }

            buffer.limit(vEnd);
            buffer.position(vPos);
            this.copyPlane(buffer.slice(), uvStride, frameBuffer.getDataV(), frameBuffer.getStrideV(), chromaWidth, chromaHeight);
            if (sliceHeight % 2 == 1) {
                buffer.position(vPos + uvStride * (chromaHeight - 1));
                dataV = frameBuffer.getDataV();
                dataV.position(frameBuffer.getStrideV() * chromaHeight);
                dataV.put(buffer);
            }

            return frameBuffer;
        }
    }

    private void reformat(MediaFormat format) {
        this.outputThreadChecker.checkIsOnValidThread();
        Logging.d(TAG, "Decoder format changed: " + format.toString());
        int newWidth;
        int newHeight;
        if (format.containsKey("crop-left") && format.containsKey("crop-right") && format.containsKey("crop-bottom") && format.containsKey("crop-top")) {
            newWidth = 1 + format.getInteger("crop-right") - format.getInteger("crop-left");
            newHeight = 1 + format.getInteger("crop-bottom") - format.getInteger("crop-top");
        } else {
            newWidth = format.getInteger("width");
            newHeight = format.getInteger("height");
        }

        synchronized(this.dimensionLock) {
            if (this.hasDecodedFirstFrame && (this.width != newWidth || this.height != newHeight)) {
                this.stopOnOutputThread(new RuntimeException("Unexpected size change. Configured " + this.width + "*" + this.height + ". New " + newWidth + "*" + newHeight));
                return;
            }

            this.width = newWidth;
            this.height = newHeight;
        }

        if (this.surfaceTextureHelper == null && format.containsKey("color-format")) {
            this.colorFormat = format.getInteger("color-format");
            Logging.d(TAG, "Color: 0x" + Integer.toHexString(this.colorFormat));
            if (!this.isSupportedColorFormat(this.colorFormat)) {
                this.stopOnOutputThread(new IllegalStateException("Unsupported color format: " + this.colorFormat));
                return;
            }
        }

        synchronized(this.dimensionLock) {
            if (format.containsKey("stride")) {
                this.stride = format.getInteger("stride");
            }

            if (format.containsKey("slice-height")) {
                this.sliceHeight = format.getInteger("slice-height");
            }

            Logging.d(TAG, "Frame stride and slice height: " + this.stride + " x " + this.sliceHeight);
            this.stride = Math.max(this.width, this.stride);
            this.sliceHeight = Math.max(this.height, this.sliceHeight);
        }
    }

    private void releaseCodecOnOutputThread() {
        this.outputThreadChecker.checkIsOnValidThread();
        Logging.d(TAG, "Releasing MediaCodec on output thread");

        try {
            this.codec.stop();
        } catch (Exception var3) {
            Logging.e(TAG, "Media decoder stop failed", var3);
        }

        try {
            this.codec.release();
        } catch (Exception var2) {
            Logging.e(TAG, "Media decoder release failed", var2);
            this.shutdownException = var2;
        }

        Logging.d(TAG, "Release on output thread done");
    }

    private void stopOnOutputThread(Exception e) {
        this.outputThreadChecker.checkIsOnValidThread();
        this.running = false;
        this.shutdownException = e;
    }

    private boolean isSupportedColorFormat(int colorFormat) {
        int[] var2 = SDMediaCodecUtils.DECODER_COLOR_FORMATS;
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            int supported = var2[var4];
            if (supported == colorFormat) {
                return true;
            }
        }

        return false;
    }

    protected SurfaceTextureHelper createSurfaceTextureHelper() {
        return SurfaceTextureHelper.create("decoder-texture-thread", this.sharedContext);
    }

    protected void releaseSurface() {
        this.surface.release();
    }

    protected I420Buffer allocateI420Buffer(int width, int height) {
        return JavaI420Buffer.allocate(width, height);
    }

    protected void copyPlane(ByteBuffer src, int srcStride, ByteBuffer dst, int dstStride, int width, int height) {
        YuvHelper.copyPlane(src, srcStride, dst, dstStride, width, height);
    }

    private static class DecodedTextureMetadata {
        final long presentationTimestampUs;
        final Integer decodeTimeMs;

        DecodedTextureMetadata(long presentationTimestampUs, Integer decodeTimeMs) {
            this.presentationTimestampUs = presentationTimestampUs;
            this.decodeTimeMs = decodeTimeMs;
        }
    }

    private static class FrameInfo {
        final long decodeStartTimeMs;
        final int rotation;

        FrameInfo(long decodeStartTimeMs, int rotation) {
            this.decodeStartTimeMs = decodeStartTimeMs;
            this.rotation = rotation;
        }
    }
}

class SDMediaCodecUtils {
    private static final String TAG = "MediaCodecUtils";
    static final String EXYNOS_PREFIX = "OMX.Exynos.";
    static final String INTEL_PREFIX = "OMX.Intel.";
    static final String NVIDIA_PREFIX = "OMX.Nvidia.";
    static final String QCOM_PREFIX = "OMX.qcom.";
    static final String MTK_PREFIX = "OMX.MTK.";
    static final String[] SOFTWARE_IMPLEMENTATION_PREFIXES = new String[]{"OMX.google.", "OMX.SEC."};
    static final int COLOR_QCOM_FORMATYVU420PackedSemiPlanar32m4ka = 2141391873;
    static final int COLOR_QCOM_FORMATYVU420PackedSemiPlanar16m4ka = 2141391874;
    static final int COLOR_QCOM_FORMATYVU420PackedSemiPlanar64x32Tile2m8ka = 2141391875;
    static final int COLOR_QCOM_FORMATYUV420PackedSemiPlanar32m = 2141391876;
    static final int[] DECODER_COLOR_FORMATS = new int[]{19, 21, 2141391872, 2141391873, 2141391874, 2141391875, 2141391876};
    static final int[] ENCODER_COLOR_FORMATS = new int[]{19, 21, 2141391872, 2141391876};
    static final int[] TEXTURE_COLOR_FORMATS = getTextureColorFormats();

    private static int[] getTextureColorFormats() {
        return Build.VERSION.SDK_INT >= 18 ? new int[]{2130708361} : new int[0];
    }

    @Nullable
    static Integer selectColorFormat(int[] supportedColorFormats, MediaCodecInfo.CodecCapabilities capabilities) {
        int[] var2 = supportedColorFormats;
        int var3 = supportedColorFormats.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            int supportedColorFormat = var2[var4];
            int[] var6 = capabilities.colorFormats;
            int var7 = var6.length;

            for(int var8 = 0; var8 < var7; ++var8) {
                int codecColorFormat = var6[var8];
                if (codecColorFormat == supportedColorFormat) {
                    return codecColorFormat;
                }
            }
        }

        return null;
    }

    static boolean codecSupportsType(MediaCodecInfo info, SDVideoCodecType type) {
        String[] var2 = info.getSupportedTypes();
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            String mimeType = var2[var4];
            if (type.mimeType().equals(mimeType)) {
                return true;
            }
        }

        return false;
    }

    static Map<String, String> getCodecProperties(SDVideoCodecType type, boolean highProfile) {
        switch(type) {
            case VP8:
            case VP9:
                return new HashMap();
            case H264:
                return H264Utils.getDefaultH264Params(highProfile);
            default:
                throw new IllegalArgumentException("Unsupported codec: " + type);
        }
    }

    private SDMediaCodecUtils() {
    }
}

class H264Utils {
    public static final String H264_FMTP_PROFILE_LEVEL_ID = "profile-level-id";
    public static final String H264_FMTP_LEVEL_ASYMMETRY_ALLOWED = "level-asymmetry-allowed";
    public static final String H264_FMTP_PACKETIZATION_MODE = "packetization-mode";
    public static final String H264_PROFILE_CONSTRAINED_BASELINE = "42e0";
    public static final String H264_PROFILE_CONSTRAINED_HIGH = "640c";
    public static final String H264_LEVEL_3_1 = "1f";
    public static final String H264_CONSTRAINED_HIGH_3_1 = "640c1f";
    public static final String H264_CONSTRAINED_BASELINE_3_1 = "42e01f";
    public static VideoCodecInfo DEFAULT_H264_BASELINE_PROFILE_CODEC = new VideoCodecInfo("H264", getDefaultH264Params(false));
    public static VideoCodecInfo DEFAULT_H264_HIGH_PROFILE_CODEC = new VideoCodecInfo("H264", getDefaultH264Params(true));

    H264Utils() {
    }

    public static Map<String, String> getDefaultH264Params(boolean isHighProfile) {
        Map<String, String> params = new HashMap();
        params.put("level-asymmetry-allowed", "1");
        params.put("packetization-mode", "1");
        params.put("profile-level-id", isHighProfile ? "640c1f" : "42e01f");
        return params;
    }

    public static boolean isSameH264Profile(Map<String, String> params1, Map<String, String> params2) {
        return nativeIsSameH264Profile(params1, params2);
    }

    private static native boolean nativeIsSameH264Profile(Map<String, String> var0, Map<String, String> var1);
}
