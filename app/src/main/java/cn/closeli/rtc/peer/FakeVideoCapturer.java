package cn.closeli.rtc.peer;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.vhd.base.video.FrameData;

import org.webrtc.CapturerObserver;
import org.webrtc.JavaI420Buffer;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoFrame;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class FakeVideoCapturer implements VideoCapturer {
    private static final String TAG = "FakeVideoCapturer";
    private CapturerObserver capturerObserver;
    private Timer timer = null;
    private final TimerTask tickTask = new TimerTask() {
        public void run() {
            FakeVideoCapturer.this.tick();
        }
    };

    private LinkedBlockingQueue<FrameData> frameQueue = null;

    FakeVideoEncoderFactory fakeVideoEncoderFactory = null;
    FakeVideoEncoder fakeVideoEncoder = null;
    private VHDVideoProducer videoProducer = null;

    private int width;
    private int height;
    private int framerate;

    // encode statistics param
    private final long printDurationMs = 4000;
    private long lastPrintTimeNs = TimeUnit.MILLISECONDS.toNanos(SystemClock.elapsedRealtime());
    private long lastAdjustTimeNs = TimeUnit.MILLISECONDS.toNanos(SystemClock.elapsedRealtime());
    private long lastIFrameTimeNs = TimeUnit.MILLISECONDS.toNanos(SystemClock.elapsedRealtime());
    private long totalBitrate;
    private long totalFramerate;

    FakeVideoCapturer(VHDVideoProducer videoProducer, FakeVideoEncoderFactory fakeVideoEncoderFactory) {
        this.fakeVideoEncoderFactory = fakeVideoEncoderFactory;
        this.videoProducer = videoProducer;
    }

    @Override
    public void initialize(SurfaceTextureHelper surfaceTextureHelper, Context context, CapturerObserver capturerObserver) {
        Log.i(TAG, "initialize: ");
        this.capturerObserver = capturerObserver;
    }

    @Override
    public void startCapture(int i, int i1, int i2) {
        Log.i(TAG, "startCapture: width = " + i + ", height = " + i1 + ", framerate = " + i2);
        this.width = i;
        this.height = i1;
        this.framerate = i2;

        boolean ret = this.videoProducer.startCameraEncoder(this.width, this.height, this.framerate);
        if (!ret) {
            Log.e(TAG, "startCapture: startCameraEncoder failed!");
            return;
        }
        this.frameQueue = this.videoProducer.getFrameQueue(videoProducer.isSinkOneOnly());
        if (this.timer == null) {
            this.timer = new Timer();
            this.timer.schedule(this.tickTask, 0L, (long) (1000 / this.framerate / 2));
        }
    }

    @Override
    public void stopCapture() throws InterruptedException {
        if (this.timer != null) {
            this.timer.cancel();
            this.timer.purge();
            this.timer = null;
        }
        this.videoProducer.stopCameraEncoder(videoProducer.isSinkOneOnly());
    }

    @Override
    public void changeCaptureFormat(int i, int i1, int i2) {
        if (i2 > 1024) {
            this.videoProducer.setBitrate(i, i1, i2);
        } else {
            this.videoProducer.updateParameter(i, i1, i2, -1, -1);
        }
    }

    @Override
    public void dispose() {
        try {
            stopCapture();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isScreencast() {
        return false;
    }

    public void tick() {
        if (this.frameQueue.size() <= 0) {
            return;
        }
        if (fakeVideoEncoder == null) {
            fakeVideoEncoder = fakeVideoEncoderFactory.getFakeVideoEncoder();
            if (fakeVideoEncoder == null) {
                Log.e(TAG, "tick: fakeVideoEncoder is null");
                return;
            }
            this.fakeVideoEncoder.setVideoProducer(this.videoProducer);
        }
        long captureTimeNs = TimeUnit.MILLISECONDS.toNanos(SystemClock.elapsedRealtime());

        JavaI420Buffer buffer = JavaI420Buffer.allocate(this.width, this.height);
        VideoFrame videoFrame = new VideoFrame(buffer, 0, captureTimeNs);
        this.capturerObserver.onFrameCaptured(videoFrame);
        videoFrame.release();

        FrameData frameData = this.frameQueue.poll();
        if (frameData == null || frameData.mData == null) {
            Log.e(TAG, "tick: error frameData = " + frameData);
            return;
        }
        frameData.isKeyFrame = (frameData.mData[4] & 0x1F) == 7;
        frameData.mTimestamp = captureTimeNs;
        boolean sentRet = this.fakeVideoEncoder.sendEncodedFrame(frameData, this.width, this.height);
        if (!sentRet) {
            Log.e(TAG, "send frame failed, drop frame and force I frame. isKeyFrame: " + frameData.isKeyFrame
                    + ", timeNs: " + captureTimeNs + ", queueSize = " + frameQueue.size());
            this.frameQueue.clear();
            this.videoProducer.forceIFrameRequest();
        }

        // encode statistics
        long nowTimeNs = TimeUnit.MILLISECONDS.toNanos(SystemClock.elapsedRealtime());
        if (frameData.isKeyFrame) {
            Log.d(TAG, "encoded statistics: I frame, iFramePeroid = " + (nowTimeNs - lastIFrameTimeNs) / 1000000 + " ms.");
            lastIFrameTimeNs = nowTimeNs;
        }
        this.totalBitrate += frameData.mData.length;
        this.totalFramerate++;
        if (nowTimeNs - this.lastPrintTimeNs >= printDurationMs * 1000000L) {
            long durationMs = (nowTimeNs - this.lastPrintTimeNs) / 1000000L;
            long curAvgBitrateKbps = this.totalBitrate * 1000 / durationMs / 128;
            long curAvgFramerateFps = this.totalFramerate * 1000 / durationMs;
            this.totalBitrate = 0;
            this.totalFramerate = 0;
            this.lastPrintTimeNs = nowTimeNs;
            Log.d(TAG, "encoded statistics: duration: " + durationMs + " ms"
                    + ", bitrate = " + curAvgBitrateKbps + " kbps"
                    + ", framerate = " + curAvgFramerateFps + " fps.");
            // test adjust encode parameter
//            if (nowTimeNs - this.lastAdjustTimeNs >= 5 * printDurationMs * 1000000L) {
//                this.lastAdjustTimeNs = nowTimeNs;
//                long newBitrateKbps = -1; // curAvgBitrateKbps > 1024 ? curAvgBitrateKbps / 2 : curAvgBitrateKbps * 2;
//                long newFramerateFps = -1; // curAvgFramerateFps > 17 ? 15 : 20;
//                int newIFramePeroid = -1; // curAvgFramerateFps > 17 ? 10 : 15;
//                this.width = this.width == 1920 ? 640 :  1920;
//                this.height = this.height == 1080 ? 480 :  1080;
//                videoProducer.updateParameter(this.width, this.height, (int)newFramerateFps,  (int)newBitrateKbps * 1024, newIFramePeroid);
//                videoProducer.forceIFrameRequest();
//                Log.d(TAG, "adjust encode parameter, bitrate: " + String.valueOf(curAvgBitrateKbps) + " kbps -> " + String.valueOf(newBitrateKbps) + " kbps"
//                        + ", framerate: " + String.valueOf(curAvgFramerateFps) + "fps -> " + String.valueOf(newFramerateFps) + "fps.");
//            }
        }
    }
}
