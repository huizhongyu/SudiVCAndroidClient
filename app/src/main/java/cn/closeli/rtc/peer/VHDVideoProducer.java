package cn.closeli.rtc.peer;

import android.media.MediaFormat;
import android.util.Log;

import com.vhd.base.contant.VideoConstant;
import com.vhd.base.video.FrameData;
import com.vhd.camera.CameraEncoderv2;
import com.vhd.camera.Device;
import com.vhd.camera.Parameter;
import com.vhd.camera.VHDDeviceInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

public class VHDVideoProducer {
    private static final String TAG = "VHDVideoProducer";

    public static final int VideoIndex_Camera = 0;
    public static final int VideoIndex_HdmiIn = 1;

    // encode param
    private String mimeType = mListPort1MimeTypeValue[0];
    private int width = VideoConstant.ListResolutionValue[0][0];                //分辨率
    private int height = VideoConstant.ListResolutionValue[0][1];
    private int framerate = VideoConstant.ListFramerateValue[2];                //帧率
    private int profile = mListPort1H264ProfileValue[0];                        //轮廓：默认baseline
    private int rateControlMode = mListPort1H264RateCtrlModeValue[0];           //速率控制模式
    private int currentBitrate = VideoConstant.ListEncoderBitrateValue[0];      //比特率
    private int iFramePeroid = VideoConstant.ListEncoderIFramePeriodValue[0];   //帧周期
    private int dumpDuration = VideoConstant.ListEncoderDumpDurationValue[0];   //转储持续时间
    private boolean dumpFile = false;

    private int limiteMinFps = 8;
    private int limiteMaxFps = 30;
    private int minBitrate;
    private int maxBitrate;

    private boolean hasInited = false;
    private boolean sinkOneOnly = false;
    private boolean firstRunning = false;
    private boolean secondRunning = false;
    private int videoIndex = 0;
    public static List<String> mListVideoDeviceName = new ArrayList<>();
    public static List<VHDDeviceInfo> mListVideoDevice = new ArrayList<>();
    private LinkedBlockingQueue<FrameData> mFrameQueueFromCameraEncoder = new LinkedBlockingQueue<FrameData>(10);
    private LinkedBlockingQueue<FrameData> mFrameQueueFromCameraEncoderClone = new LinkedBlockingQueue<FrameData>(10);
    private CameraEncoderv2 mCameraEncoder = null;

    final static String[] mListP2PH264VideoDeviceName = {
            Device.DEV_VIDEO_0,
            Device.DEV_VIDEO_1,
    };
    final static String[] mListPort1MimeTypeValue = {
            MediaFormat.MIMETYPE_VIDEO_AVC,
            MediaFormat.MIMETYPE_VIDEO_HEVC,
    };
    final static int[] mListPort1H264ProfileValue = {
            Parameter.PROFILE_BASELINE,
            Parameter.PROFILE_MAIN,
            Parameter.PROFILE_HIGH,
    };
    final static int[] mListPort1H264RateCtrlModeValue = {
            Parameter.RATE_CONTROL_MODE_CBR,
            Parameter.RATE_CONTROL_MODE_VBR,
            Parameter.RATE_CONTROL_MODE_CONSTANT_QP,
    };

//    private static class SingletonHolderVideo0 {
//        private static VHDVideoProducer instance = new VHDVideoProducer(0);
//    }
//
//    private static class SingletonHolderVideo1 {
//        private static VHDVideoProducer instance = new VHDVideoProducer(1);
//    }
//
//    public static VHDVideoProducer get(int videoIndex) {
//        if (videoIndex == 1) {
//            return VHDVideoProducer.SingletonHolderVideo1.instance;
//        } else {
//            return VHDVideoProducer.SingletonHolderVideo0.instance;
//        }
//    }

    public VHDVideoProducer(int videoIndex, boolean sinkOneOnly, int minBitrate, int currentBitrate, int maxBitrate) {
        Log.i(TAG, "VHDVideoProducer: videoIndex = " + videoIndex
                + ", minBitrate = " + minBitrate + ", currentBitrate = " + currentBitrate + ", maxBitrate = " + maxBitrate);
        this.videoIndex = videoIndex;
        this.sinkOneOnly = sinkOneOnly;
        this.limiteMinFps = this.videoIndex == 0 ? 15 : 8;
        setBitrate(minBitrate, currentBitrate, maxBitrate);
    }

    public void setBitrate(int min, int current, int max) {
        this.minBitrate = min;
        this.currentBitrate = current;
        this.maxBitrate = max;
    }

    public boolean isSinkOneOnly() {
        return this.sinkOneOnly;
    }

    public void initVHDDeviceInfo() {
        Log.i(TAG, "initVHDDeviceInfo: ");
        if (hasInited) {
            return;
        }
        this.hasInited = true;
        mListVideoDevice.clear();
        mListVideoDeviceName.clear();

        VHDDeviceInfo[] deviceInfos = CameraEncoderv2.getDeviceInfoList();
        if (deviceInfos == null || deviceInfos.length <= 0) {
            Log.e(TAG, "initVHDDeviceInfo: cannot find vhd device");
            mListVideoDeviceName.addAll(Arrays.asList(mListP2PH264VideoDeviceName));
            return;
        }

        for (VHDDeviceInfo info : deviceInfos) {
            mListVideoDevice.add(info);
            mListVideoDeviceName.add(info.getDeviceName());
            Log.d("device", "initVHDDevice() called " + info);
        }
    }

    public boolean startCameraEncoder(int width, int height, int framerate) {
        Log.i(TAG, "startCameraEncoder: width = " + width + ", height = " + height + ", framerate = " + framerate);
        if (mCameraEncoder == null) {
            this.width = width;
            this.height = height;
            this.framerate = framerate;
            mCameraEncoder = new CameraEncoderv2(
                    mListVideoDeviceName.get(this.videoIndex),
                    this.mimeType,
                    this.width,
                    this.height,
                    this.framerate,
                    this.profile,
                    this.currentBitrate,
                    this.rateControlMode,
                    this.iFramePeroid,
                    this.mFrameQueueFromCameraEncoder,
                    this.sinkOneOnly ? null : this.mFrameQueueFromCameraEncoderClone,
                    this.dumpFile,
                    this.dumpDuration);
            boolean success = mCameraEncoder.startThread();
            if (!success) {
                mCameraEncoder.stopThread();
                mCameraEncoder = null;
                Log.e(TAG, "startCameraEncoder: open encoder error!");
                return false;
            }
            // updateParameter delay 1000 ms
//            Timer timer = new Timer();
//            timer.schedule(new TimerTask() {
//                @Override
//                public void run() {
//                    updateParameter(-1, -1, -1, -1, -1);
//                }
//            }, 1000L);
            Log.d(TAG, "startCameraEncoder: open encoder success!");
        } else {
            Log.d(TAG, "startCameraEncoder: had started!");
        }
        return true;
    }

    public LinkedBlockingQueue<FrameData> getFrameQueue(boolean first) {
        if (first) {
            this.firstRunning = true;
            return mFrameQueueFromCameraEncoder;
        } else {
            this.secondRunning = true;
            return mFrameQueueFromCameraEncoderClone;
        }
    }

    public boolean updateParameter(int width, int height, int framerate, int bitrate, int iFramePeroid) {
        int dstWidth = width == -1 ? this.width : width;
        int dstHeight = height == -1 ? this.height : height;
        int dstFramerate = framerate == -1 ? this.framerate : framerate > limiteMaxFps ? limiteMaxFps : framerate < limiteMinFps ? limiteMinFps : framerate;
        int dstBitrate = bitrate == -1 ? this.currentBitrate : bitrate > maxBitrate ? maxBitrate : bitrate < minBitrate ? minBitrate : bitrate;
        int dstIFramePeroid = iFramePeroid == -1 ? this.iFramePeroid : iFramePeroid;
        Log.d(TAG, "updateParameter: width = " + dstWidth
                + ", height = " + dstHeight
                + ", dstFramerate = " + dstFramerate
                + ", this.profile = " + this.profile
                + ", this.rateControlMode = " + this.rateControlMode
                + ", dstBitrate = " + dstBitrate
                + ", iFramePeroid = " + dstIFramePeroid);
        if (mCameraEncoder == null) {
            Log.d(TAG, "updateParameter: mCameraEncoder is null");
            return false;
        }
        this.width = dstWidth;
        this.height = dstHeight;
        this.framerate = dstFramerate;
        this.currentBitrate = dstBitrate;
        this.iFramePeroid = dstIFramePeroid;
        return mCameraEncoder.updateParameter(dstWidth, dstHeight, dstFramerate, this.profile, dstBitrate, this.rateControlMode, dstIFramePeroid);
    }

    public void forceIFrameRequest() {
        if (mCameraEncoder != null) {
            mCameraEncoder.forceIFrameRequest();
        }
    }

    public synchronized void stopCameraEncoder(boolean first) {
        Log.i(TAG, "stopCameraEncoder: begin >> first = " + first);

        if (first) {
            this.firstRunning = false;
        } else {
            this.secondRunning = false;
        }
        if (this.firstRunning || this.secondRunning) {
            Log.i(TAG, "stopCameraEncoder: leave >> first = " + this.firstRunning + ", second = " + this.secondRunning);
            return;
        }
        if (mCameraEncoder != null) {
            mCameraEncoder.stopThread();
            mCameraEncoder = null;
        }

        mFrameQueueFromCameraEncoder.clear();
        mFrameQueueFromCameraEncoderClone.clear();
        Log.i(TAG, "stopCameraEncoder: end !! first = " + first);
    }
}
