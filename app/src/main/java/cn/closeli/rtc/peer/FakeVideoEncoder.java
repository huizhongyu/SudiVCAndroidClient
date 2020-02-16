package cn.closeli.rtc.peer;

import android.os.SystemClock;
import android.util.Log;

import com.vhd.base.video.FrameData;

import org.webrtc.EncodedImage;
import org.webrtc.VideoCodecStatus;
import org.webrtc.VideoEncoder;
import org.webrtc.VideoFrame;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import cn.closeli.rtc.utils.LooperExecutor;

public class FakeVideoEncoder implements VideoEncoder {
    private static final String TAG = "FakeVideoEncoder";
    private Callback callback;
    private AtomicLong encodedTimeNs = new AtomicLong(0);
    VHDVideoProducer videoProducer;
    private long lastUpdateTimeNs = TimeUnit.MILLISECONDS.toNanos(SystemClock.elapsedRealtime());
    private LooperExecutor executor = new LooperExecutor();

    FakeVideoEncoder() {
        executor.requestStart();
    }

    @Override
    public boolean isHardwareEncoder() {
        return false;
    }

    @Override
    public VideoCodecStatus initEncode(Settings settings, Callback callback) {
        Log.e(TAG, "initEncode: settings.width = " + settings.width + ", settings.height = " + settings.height);
        this.callback = callback;
        return VideoCodecStatus.OK;
    }

    @Override
    public VideoCodecStatus release() {
        return VideoCodecStatus.OK;
    }

    @Override
    public VideoCodecStatus encode(VideoFrame videoFrame, EncodeInfo encodeInfo) {
        this.encodedTimeNs.set(videoFrame.getTimestampNs());
        return VideoCodecStatus.OK;
    }

    @Override
    public VideoCodecStatus setRateAllocation(BitrateAllocation bitrateAllocation, int i) {
//        executor.execute(new Runnable() {
//            @Override
//            public void run() {
//                long nowNs = TimeUnit.MILLISECONDS.toNanos(SystemClock.elapsedRealtime());
//                long intervalMs = (nowNs - lastUpdateTimeNs) / (1000 * 1000);
//                if (intervalMs >= 5 * 1000) {
//                    Log.d(TAG, "setRateAllocation: intervalMs = " + intervalMs + ", bitrate = " + bitrateAllocation.getSum() + ", framerate = " + i);
//                    FakeVideoEncoder.this.videoProducer.updateParameter(-1, -1, i, bitrateAllocation.getSum(), -1);
//                    lastUpdateTimeNs = nowNs;
//                }
//            }
//        });

        return VideoCodecStatus.OK;
    }

    @Override
    public ScalingSettings getScalingSettings() {
        return new ScalingSettings(24, 37);
    }

    @Override
    public String getImplementationName() {
        return "fake_encoder";
    }

    public void setVideoProducer(VHDVideoProducer videoProducer) {
        this.videoProducer = videoProducer;
    }

    public boolean sendEncodedFrame(FrameData frameData, int width, int height) {
        if (this.callback == null) {
            Log.e(TAG, "sendEncodedFrame: this.callback is null");
            return false;
        }
        int count = 3;
        while (this.encodedTimeNs.get() <= 0 && count-- >0 ) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (this.encodedTimeNs.get() <= 0) {
            Log.e(TAG, "sendEncodedFrame: encodedTimeNs is 0, drop frame, timeNs = " + frameData.mTimestamp);
            return false;
        }
        long captureTimeNs = this.encodedTimeNs.getAndSet(0);

//        Log.v(TAG, "sendEncodedFrame: isKeyFrame = " + String.valueOf(isKeyFrame)
//                + ", frameSize = " + String.valueOf(frameData.mData.length)
//                + ", width = " + String.valueOf(width)
//                + ", height = " + String.valueOf(height)
//                + ", captureTimeNs = " + String.valueOf(captureTimeNs));

        ByteBuffer frameBuffer;
        frameBuffer = ByteBuffer.allocateDirect(frameData.mData.length);
        frameBuffer.put(frameData.mData, 0, frameData.mData.length);
        frameBuffer.rewind();

        EncodedImage.Builder builder = EncodedImage.builder()
                .setCaptureTimeNs(captureTimeNs)
                .setCompleteFrame(true)
                .setEncodedWidth(width)
                .setEncodedHeight(height)
                .setRotation(0)
                .setBuffer(frameBuffer)
                .setFrameType(frameData.isKeyFrame ? EncodedImage.FrameType.VideoFrameKey : EncodedImage.FrameType.VideoFrameDelta);
        this.callback.onEncodedFrame(builder.createEncodedImage(), new CodecSpecificInfo());

        return true;
    }
}
