package cn.closeli.rtc.peer;

import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceView;

import com.vhd.base.util.FileWriter;
import com.vhd.base.video.FrameData;

import org.webrtc.EglBase;
import org.webrtc.EncodedImage;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCodecStatus;
import org.webrtc.VideoDecoder;
import org.webrtc.VideoFrame;

import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cn.closeli.rtc.peer.player.SDAndroidVideoDecoder;
import cn.closeli.rtc.peer.player.SDMediaCodecWrapperFactoryImpl;
import cn.closeli.rtc.peer.player.SDVideoCodecType;
import cn.closeli.rtc.sdk.ProxyVideoSink;

public class VHDVideoPreview {
    private static final String TAG = "VHDVideoPreview";
    private static Lock lock = new ReentrantLock();
    private Lock lockLocalSink = new ReentrantLock();

    // encode param
    private int width;
    private int height;
    private int framerate;

    private VHDVideoProducer videoProducer = null;

    private SurfaceView surfaceView;

    private SDAndroidVideoDecoder sdAndroidVideoDecoder;
    private EglBase eglBase;
    ProxyVideoSink localSink = new ProxyVideoSink();
    private LinkedBlockingQueue<FrameData> frameQueue = null;
    private int gotFirstIFrame = 0;
    private boolean needForceIFrame = false;
    private Timer timer = null;
    private final TimerTask tickTask = new TimerTask() {
        public void run() {
            VHDVideoPreview.this.tick();
        }
    };

    public VHDVideoPreview(VHDVideoProducer videoProducer, EglBase eglBase, SurfaceView surfaceView, int width, int height, int framerate) {
        Log.i(TAG, "VHDVideoPreview: ");
        this.videoProducer = videoProducer;
        this.eglBase = eglBase;
        this.width = width;
        this.height = height;
        this.framerate = framerate;
        updateSurfaceView(surfaceView);
    }

    public void start() {
        Log.i(TAG, "start: ");
        try {
            lock.lock();
            boolean ret = this.videoProducer.startCameraEncoder(this.width, this.height, this.framerate);
            if (!ret) {
                Log.e("VHDVideoPreview", "start: startCameraEncoder failed!");
                return;
            }
            this.frameQueue = this.videoProducer.getFrameQueue(true);

            if (this.sdAndroidVideoDecoder == null) {
                this.sdAndroidVideoDecoder = new SDAndroidVideoDecoder(new SDMediaCodecWrapperFactoryImpl(),
                        "OMX.MTK.VIDEO.DECODER.AVC",
                        SDVideoCodecType.H264,
                        19,
                        this.eglBase.getEglBaseContext());
            }

            VideoDecoder.Settings settings = new VideoDecoder.Settings(0, this.width, this.height);
            try {
                sdAndroidVideoDecoder.initDecode(settings, new VideoDecoder.Callback() {
                    @Override
                    public void onDecodedFrame(VideoFrame videoFrame, Integer integer, Integer integer1) {
                        VHDVideoPreview.this.lockLocalSink.lock();
                        if (VHDVideoPreview.this.localSink != null) {
                            VHDVideoPreview.this.localSink.onFrame(videoFrame);
                        }
                        VHDVideoPreview.this.lockLocalSink.unlock();
                    }
                });
            } catch (Exception var5) {
                Log.e(TAG, "start: initDecode failed", var5);
                return;
            }

            if (this.timer == null) {
                this.timer = new Timer();
                this.timer.schedule(this.tickTask, 0L, (long) (1000 / this.framerate / 2));
            }
        } finally {
            lock.unlock();
        }
    }

    public void stop() {
        Log.i(TAG, "stop: ");
        lock.lock();
        if (this.timer != null) {
            this.timer.cancel();
            this.timer.purge();
            this.timer = null;
        }
        this.videoProducer.stopCameraEncoder(true);
        if (this.sdAndroidVideoDecoder != null) {
            this.sdAndroidVideoDecoder.release();
            this.sdAndroidVideoDecoder = null;
        }
        lock.unlock();
    }

    public void updateSurfaceView(SurfaceView surfaceView) {
        this.surfaceView = surfaceView;
        this.lockLocalSink.lock();
        if (this.surfaceView != null) {
            this.localSink = new ProxyVideoSink();
            this.localSink.setTarget((SurfaceViewRenderer) VHDVideoPreview.this.surfaceView);
            this.needForceIFrame = this.frameQueue != null;
        } else {
            this.localSink = null;
        }
        this.lockLocalSink.unlock();
    }

    public void tick() {
        if (this.frameQueue.size() <= 0) {
            return;
        }
        FrameData frameData = this.frameQueue.poll();
        if (this.localSink == null) {
            return;
        }
        if (this.sdAndroidVideoDecoder == null) {
            Log.e(TAG, "tick: sdAndroidVideoDecoder is null");
            return;
        }
        frameData.isKeyFrame = (frameData.mData[4] & 0x1F) == 7;
        if (this.needForceIFrame) {
            this.needForceIFrame = false;
            if (!frameData.isKeyFrame) {
                Log.i(TAG, "tick: force I frame after update surface view." + ", queueSize = " + frameQueue.size());
                this.frameQueue.clear();
                this.videoProducer.forceIFrameRequest();
            }
        }
        long captureTimeNs = TimeUnit.MILLISECONDS.toNanos(SystemClock.elapsedRealtime());
        frameData.mTimestamp = captureTimeNs;

        ByteBuffer frameBuffer;
        frameBuffer = ByteBuffer.allocateDirect(frameData.mData.length);
        frameBuffer.put(frameData.mData, 0, frameData.mData.length);
        frameBuffer.rewind();

        if (gotFirstIFrame < 2) {
            if (!frameData.isKeyFrame) {
                Log.e(TAG, "tick: not got 2 I frame, force I frame, frame size: " + frameData.mData.length + ", queueSize = " + frameQueue.size());
                this.frameQueue.clear();
                this.videoProducer.forceIFrameRequest();
                return;
            } else if (0 == gotFirstIFrame) {
                Log.i(TAG, "tick: got first I frame, frame size: " + frameData.mData.length + ", queueSize = " + frameQueue.size());
                gotFirstIFrame++;
                this.frameQueue.clear();
                this.videoProducer.forceIFrameRequest();
            } else {
                Log.i(TAG, "tick: got second I frame, frame size: " + frameData.mData.length + ", queueSize = " + frameQueue.size());
                gotFirstIFrame++;
            }
        }

        // debug
//        dumpFile(frameData.mData);

        EncodedImage.Builder builder = EncodedImage.builder()
                .setBuffer(frameBuffer)
                .setEncodedWidth(this.width)
                .setEncodedHeight(this.height)
                .setCaptureTimeNs(captureTimeNs)
                .setFrameType(frameData.isKeyFrame ? EncodedImage.FrameType.VideoFrameKey : EncodedImage.FrameType.VideoFrameDelta)
                .setRotation(0)
                .setCompleteFrame(true);
        VideoCodecStatus status = this.sdAndroidVideoDecoder.decode(builder.createEncodedImage(), null);
        if (status != VideoCodecStatus.OK) {
            Log.e(TAG, "decode frame failed, drop frame and force I frame. isKeyFrame: " + frameData.isKeyFrame
                    + ", timeNs: " + captureTimeNs + ", queueSize = " + frameQueue.size());
            this.frameQueue.clear();
            this.videoProducer.forceIFrameRequest();
        }
    }

    // debug
    private boolean mDumpStart = false;
    private long mDumpStartTime = 0;
    private OutputStream mDumpOutputStream = null;
    long mDumpDurationMs = 60000;

    private void dumpFile(byte[] data) {
        if (!this.mDumpStart) {
            this.mDumpStart = true;
            this.mDumpStartTime = System.currentTimeMillis();
            if (this.mDumpOutputStream == null) {
                Date date = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS");
                String dumpFileName = "/sdcard/mediatest/encode/" + Build.MODEL + "_enc_" + sdf.format(date) + "_" + this.width + "x" + this.height;
                dumpFileName = dumpFileName + "-" + this.framerate + "fps";
                dumpFileName = dumpFileName + ".h264";

                this.mDumpOutputStream = FileWriter.openFile(dumpFileName);
            }
        }

        if (this.mDumpStart && this.mDumpDurationMs > 0 && System.currentTimeMillis() - this.mDumpStartTime < (long) this.mDumpDurationMs && this.mDumpOutputStream != null) {
            FileWriter.writeBytes(this.mDumpOutputStream, data);
        }
    }
}
