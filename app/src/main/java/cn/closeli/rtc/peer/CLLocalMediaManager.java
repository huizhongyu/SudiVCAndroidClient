package cn.closeli.rtc.peer;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.projection.MediaProjection;
import android.util.Log;
import android.view.SurfaceView;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.EglBase;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.ScreenCapturerAndroid;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.closeli.rtc.sdk.IViewCallback;

/**
 * 本地Media的管理类，暂时不拆分
 */
public class CLLocalMediaManager {
    private static final String AUDIO_ECHO_CANCELLATION_CONSTRAINT = "googEchoCancellation";
    private static final String AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT = "googAutoGainControl";
    private static final String AUDIO_HIGH_PASS_FILTER_CONSTRAINT = "googHighpassFilter";
    private static final String AUDIO_NOISE_SUPPRESSION_CONSTRAINT = "googNoiseSuppression";

    private Context context;
    private EglBase eglBase;
    private SurfaceTextureHelper surfaceTextureHelper;
    private SurfaceViewRenderer surfaceHDMI;

    private MediaStream localMediaStream;       // 本地相机 [编码发送&预览][预览][不编码发送]
    private MediaStream localMediaStreamCamera; // 本地相机                      [预览]
    private MediaStream localMediaStreamHDMI;   // 本地HDMI IN [不编码发送]
    private MediaStream localMediaStreamScreen; // 屏幕共享 [编码发送&预览]
    private IViewCallback viewCallback;

    private Map<Integer, VideoCapturer> videoCapturers;
    private VideoCapturer screenCapturer;
    private VideoCapturer cameraCapturer;
    private VideoCapturer hdmiCapturer;
    private VHDVideoPreview hdmiPreview;
    private int curCameraIndex;
    private AudioManager am;
    private EncodeParam currParam = null;
    private VHDVideoProducer videoProducer;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public class EncodeParam {
        public int width;
        public int height;
        public int framerate;
        public int minBitrate;
        public int currentBitrate;
        public int maxBitrate;

        EncodeParam(int width, int height, int framerate, int minBitrate, int currentBitrate, int maxBitrate) {
            this.width = width;
            this.height = height;
            this.framerate = framerate;
            this.minBitrate = minBitrate;
            this.currentBitrate = currentBitrate;
            this.maxBitrate = maxBitrate;
        }
    }

    //    private final EncodeParam subParam = new EncodeParam(1280, 720, 20, 1024 * 1024, 1536 * 1024, 2048 * 1024);
    private final EncodeParam mainParam = new EncodeParam(1920, 1080, 20, 1024 * 1024, 2048 * 1024, 4096 * 1024);
    //    private final EncodeParam subParam = new EncodeParam(320, 240, 20, 200 * 1024, 256 * 1024, 512 * 1024);
    private final EncodeParam subParam = new EncodeParam(640, 360, 20, 384 * 1024, 512 * 1024, 768 * 1024);
//    private final EncodeParam subParam = new EncodeParam(1920, 1080, 20, 1024 * 1024, 2048 * 1024, 4096 * 1024);
    private final EncodeParam shareParam = new EncodeParam(1280, 720, 8, 1024 * 1024, 1536 * 1024, 2048 * 1024);

    public EncodeParam getMainParam() {
        return mainParam;
    }

    public EncodeParam getSubParam() {
        return subParam;
    }

    public EncodeParam getShareParam() {
        return shareParam;
    }

    public CLLocalMediaManager(Context context, EglBase eglBase) {
        this.context = context;
        this.eglBase = eglBase;
        this.videoCapturers = new HashMap<>();
        am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    /**
     * 创建本地视频
     */
    public void create(PeerConnectionFactory peerConnectionFactory, boolean isHost) {
        this.currParam = isHost ? mainParam : subParam;
        this.localMediaStream = peerConnectionFactory.createLocalMediaStream("102");
        /**
         * 音频初始化
         */
        AudioSource audioSource = peerConnectionFactory.createAudioSource(createAudioConstraints());
        AudioTrack localAudioTrack = peerConnectionFactory.createAudioTrack("101", audioSource);
        this.localMediaStream.addTrack(localAudioTrack);
        /**
         * 视频初始化
         */
        VideoSource videoSource = peerConnectionFactory.createVideoSource(false);
        VideoTrack localVideoTrack = peerConnectionFactory.createVideoTrack("100", videoSource);
        surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBase.getEglBaseContext());
        if (createVideoCapturer()) {
            for (VideoCapturer v : videoCapturers.values()) {
                v.initialize(surfaceTextureHelper, context, videoSource.getCapturerObserver());
            }
        }

        this.localMediaStream.addTrack(localVideoTrack);

        if (this.viewCallback != null) {
            this.viewCallback.onSetLocalStream(this.localMediaStream);
        }
    }

    public void createCamEncoded(PeerConnectionFactory peerConnectionFactory, FakeVideoEncoderFactory videoEncoderFactory, boolean isHost) {
        Log.e("CLLocalMediaManager", "createCamEncoded: " + videoEncoderFactory);
        this.currParam = isHost ? mainParam : subParam;

        VHDVideoProducer videoProducer = new VHDVideoProducer(VHDVideoProducer.VideoIndex_Camera, true,
                currParam.minBitrate, currParam.currentBitrate, currParam.maxBitrate);
        videoProducer.initVHDDeviceInfo();
        this.cameraCapturer = new FakeVideoCapturer(videoProducer, videoEncoderFactory);

        this.localMediaStream = peerConnectionFactory.createLocalMediaStream("402");
        /**
         * 音频初始化
         */
        AudioSource audioSource = peerConnectionFactory.createAudioSource(createAudioConstraints());
        AudioTrack localAudioTrack = peerConnectionFactory.createAudioTrack("401", audioSource);
        this.localMediaStream.addTrack(localAudioTrack);
        /**
         * 视频初始化
         */
        VideoSource videoSource = peerConnectionFactory.createVideoSource(false);
        VideoTrack localVideoTrack = peerConnectionFactory.createVideoTrack("400", videoSource);
        SurfaceTextureHelper surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread4", eglBase.getEglBaseContext());
        this.localMediaStream.addTrack(localVideoTrack);

        this.cameraCapturer.initialize(surfaceTextureHelper, context, videoSource.getCapturerObserver());

        this.cameraCapturer.startCapture(currParam.width, currParam.height, currParam.framerate);
    }

    public void cameraPreview(PeerConnectionFactory peerConnectionFactory, boolean isHost) {
        this.currParam = isHost ? mainParam : subParam;
        this.localMediaStreamCamera = peerConnectionFactory.createLocalMediaStream("502");
        /**
         * 音频初始化
         */
        AudioSource audioSource = peerConnectionFactory.createAudioSource(createAudioConstraints());
        AudioTrack localAudioTrack = peerConnectionFactory.createAudioTrack("501", audioSource);
        this.localMediaStreamCamera.addTrack(localAudioTrack);
        /**
         * 视频初始化
         */
        VideoSource videoSource = peerConnectionFactory.createVideoSource(false);
        VideoTrack localVideoTrack = peerConnectionFactory.createVideoTrack("500", videoSource);
        this.surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBase.getEglBaseContext());
        if (createVideoCapturer()) {
            for (VideoCapturer v : videoCapturers.values()) {
                v.initialize(surfaceTextureHelper, context, videoSource.getCapturerObserver());
            }
        }

        this.localMediaStreamCamera.addTrack(localVideoTrack);

        if (this.viewCallback != null) {
            this.viewCallback.onSetLocalStream(this.localMediaStreamCamera);
        }
    }

    public void setViewCallback(IViewCallback viewCallback) {
        this.viewCallback = viewCallback;
    }

    private MediaConstraints createAudioConstraints() {
        MediaConstraints audioConstraints = new MediaConstraints();
        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair(AUDIO_ECHO_CANCELLATION_CONSTRAINT, "true"));
        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair(AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT, "true"));
        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair(AUDIO_HIGH_PASS_FILTER_CONSTRAINT, "true"));
        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair(AUDIO_NOISE_SUPPRESSION_CONSTRAINT, "true"));
        return audioConstraints;
    }

    /**
     * 开启预览
     */
    public void start() {
        VideoCapturer videoCapturerDefault = null;
        if (videoCapturers.get(0) != null) {
            videoCapturerDefault = videoCapturers.get(0);
            curCameraIndex = 0;
        } else {
            videoCapturerDefault = videoCapturers.get(1);
            curCameraIndex = 1;
        }
        if (videoCapturerDefault != null) {
            videoCapturerDefault.startCapture(currParam.width, currParam.height, currParam.framerate);
        }
    }

    public void changeCaptureFormat(boolean isGoUp) {
        this.currParam = isGoUp ? mainParam : subParam;
        if (cameraCapturer != null) {
            Log.i("CLLocalMediaManager", "vhd changeCaptureFormat: " + this.currParam.width + "x" + this.currParam.height + "-" + this.currParam.framerate);
            cameraCapturer.changeCaptureFormat(this.currParam.minBitrate, this.currParam.currentBitrate, this.currParam.maxBitrate);
            cameraCapturer.changeCaptureFormat(this.currParam.width, this.currParam.height, this.currParam.framerate);
            return;
        }
        if (videoCapturers.get(curCameraIndex) != null) {
            Log.i("CLLocalMediaManager", "changeCaptureFormat: " + this.currParam.width + "x" + this.currParam.height + "-" + this.currParam.framerate);
            videoCapturers.get(curCameraIndex).changeCaptureFormat(this.currParam.width, this.currParam.height, this.currParam.framerate);
        }
    }

    public void stopLocalPreview() {
        for (VideoCapturer v : videoCapturers.values()) {
            if (v != null) {
                v.dispose();
            }
        }
        videoCapturers.clear();
        if (this.localMediaStream != null) {
            this.localMediaStream.dispose();
            this.localMediaStream = null;
        }
    }

    public void stopHDMIPreview() {
        if (hdmiPreview != null) {
            hdmiPreview.stop();
            hdmiPreview = null;
        }
    }

    public void close() {
        for (VideoCapturer v : videoCapturers.values()) {
            if (v != null) {
                //这里有可能是文件操作
                executorService.execute(v::dispose);
            }
        }
        videoCapturers.clear();
        if (this.cameraCapturer != null) {
            this.cameraCapturer.dispose();
            this.cameraCapturer = null;
        }
        stopShareScreen();
        if (surfaceTextureHelper != null) {
            surfaceTextureHelper.dispose();
            surfaceTextureHelper = null;
        }
        if (this.localMediaStreamCamera != null) {
            this.localMediaStreamCamera.dispose();
            this.localMediaStreamCamera = null;
        }
        this.localMediaStream = null;
        this.localMediaStreamHDMI = null;
        this.localMediaStreamScreen = null;
    }

    public MediaStream getLocalMediaStream() {
        return localMediaStream;
    }
    public void setMediaNull() {
         localMediaStream = null;
    }
    public MediaStream getLocalScreenMediaStream() {
        return this.localMediaStreamScreen;
    }

    public MediaStream getLocalMediaStreamHDMI() {
        return this.localMediaStreamHDMI;
    }

    public void setLocalMediaStream(MediaStream localMediaStream) {
        this.localMediaStream = localMediaStream;
    }

    public void switchCamera() {
        int nextCameraId = (curCameraIndex + 1) % 2;
        VideoCapturer preVideoCapturer = videoCapturers.get(curCameraIndex);
        if (preVideoCapturer != null) {
            try {
                preVideoCapturer.stopCapture();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        VideoCapturer nextVideoCapturer = videoCapturers.get(nextCameraId);
        if (nextVideoCapturer != null) {
            nextVideoCapturer.startCapture(currParam.width, currParam.height, currParam.framerate);
        }
        curCameraIndex = nextCameraId;
    }

    public void shareScreen(Intent data, SurfaceView localScreenVideoView) {
        this.screenCapturer = new ScreenCapturerAndroid(data, new MediaProjection.Callback() {
            @Override
            public void onStop() {
                Log.e("CLLocalMediaManager", "shareScreen: User revoked permission to capture the screen.");
            }
        });
    }

    public void startShareScreen(PeerConnectionFactory peerConnectionFactory) {
        if (this.screenCapturer != null) {
            VideoSource videoSource = peerConnectionFactory.createVideoSource(this.screenCapturer.isScreencast());
            VideoTrack localScreenVideoTrack = peerConnectionFactory.createVideoTrack("200", videoSource);
            surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBase.getEglBaseContext());
            this.screenCapturer.initialize(surfaceTextureHelper, context, videoSource.getCapturerObserver());

            this.localMediaStreamScreen = peerConnectionFactory.createLocalMediaStream("202");
            this.localMediaStreamScreen.addTrack(localScreenVideoTrack);

            if (viewCallback != null) {
                viewCallback.onSetLocalScreenStream(localMediaStreamScreen);
            }
            this.screenCapturer.startCapture(shareParam.width, shareParam.height, shareParam.framerate);
        }
    }

    public void stopShareScreen() {
        if (localMediaStreamScreen != null) {
            localMediaStreamScreen.dispose();
            localMediaStreamScreen = null;
        }
        if (screenCapturer != null) {
            screenCapturer.dispose();
            screenCapturer = null;
        }
    }

    public void shareHdmi(SurfaceViewRenderer surfaceHDMI) {
        this.surfaceHDMI = surfaceHDMI;
        if (this.hdmiPreview != null) {
            this.hdmiPreview.updateSurfaceView(surfaceHDMI);
        }
    }

    public void startShareHDMI(PeerConnectionFactory peerConnectionFactory, FakeVideoEncoderFactory videoEncoderFactory) {
        Log.e("CLLocalMediaManager", "startShareHDMI: " + videoEncoderFactory);
        videoProducer = new VHDVideoProducer(VHDVideoProducer.VideoIndex_HdmiIn, false,
                shareParam.minBitrate, shareParam.currentBitrate, shareParam.maxBitrate);
        videoProducer.initVHDDeviceInfo();

        hdmiCapturer = new FakeVideoCapturer(videoProducer, videoEncoderFactory);

        VideoSource videoSource = peerConnectionFactory.createVideoSource(false);
        VideoTrack localVideoTrackHDMI = peerConnectionFactory.createVideoTrack("300", videoSource);
        surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBase.getEglBaseContext());
        hdmiCapturer.initialize(surfaceTextureHelper, context, videoSource.getCapturerObserver());

        this.localMediaStreamHDMI = peerConnectionFactory.createLocalMediaStream("302");
        this.localMediaStreamHDMI.addTrack(localVideoTrackHDMI);
        if (viewCallback != null) {
            viewCallback.onSetLocalHDMIStream(localMediaStreamHDMI);
        }
        hdmiCapturer.startCapture(shareParam.width, shareParam.height, shareParam.framerate);
        this.hdmiPreview = new VHDVideoPreview(videoProducer, eglBase, this.surfaceHDMI, shareParam.width, shareParam.height, shareParam.framerate);
        this.hdmiPreview.start();
    }

    public void hdmiPreview(boolean onlyPreview) {
        VHDVideoProducer videoProducer = new VHDVideoProducer(VHDVideoProducer.VideoIndex_HdmiIn, onlyPreview,
                shareParam.minBitrate, shareParam.currentBitrate, shareParam.maxBitrate);
        videoProducer.initVHDDeviceInfo();
        this.hdmiPreview = new VHDVideoPreview(videoProducer, eglBase, this.surfaceHDMI, shareParam.width, shareParam.height, shareParam.framerate);
        this.hdmiPreview.start();
    }

    public void hdmiOnlyPreview(boolean onlyPreview) {
        this.hdmiPreview = new VHDVideoPreview(videoProducer, eglBase, this.surfaceHDMI, shareParam.width, shareParam.height, shareParam.framerate);
        this.hdmiPreview.start();
    }

    public void stopShareHDMI() {
        if (localMediaStreamHDMI != null) {
            localMediaStreamHDMI = null;
        }
        if (hdmiPreview != null) {
            hdmiPreview.stop();
            hdmiPreview = null;
        }
        if (hdmiCapturer != null) {
            hdmiCapturer.dispose();
            hdmiCapturer = null;
        }
    }

    public void enableVideo(boolean enabled) {
        if (localMediaStreamCamera != null && this.localMediaStreamCamera.videoTracks.get(0) != null) {
            this.localMediaStreamCamera.videoTracks.get(0).setEnabled(enabled);
        }
        if (localMediaStream != null && this.localMediaStream.videoTracks.get(0) != null) {
            this.localMediaStream.videoTracks.get(0).setEnabled(enabled);
            startVideoCapture(enabled);
        }
    }

    public void enableSpeaker(boolean enabled) {
        if (am != null) {
            am.setSpeakerphoneOn(enabled);
        }
    }

    public void enableAudio(boolean enabled) {
        if (localMediaStream != null && this.localMediaStream.audioTracks.get(0) != null) {
            this.localMediaStream.audioTracks.get(0).setEnabled(enabled);
        }
        if (localMediaStreamCamera != null && this.localMediaStreamCamera.audioTracks.get(0) != null) {
            this.localMediaStreamCamera.audioTracks.get(0).setEnabled(enabled);
        }
    }

    public void setAudioValue(int value) {
        if (localMediaStream != null && this.localMediaStream.audioTracks.get(0) != null) {
            this.localMediaStream.audioTracks.get(0).setVolume(value);
        }
        if (localMediaStreamCamera != null && this.localMediaStreamCamera.audioTracks.get(0) != null) {
            this.localMediaStreamCamera.audioTracks.get(0).setVolume(value);
        }
    }

    public void muteVideoStream(boolean muted) {
        if (localMediaStream != null && this.localMediaStream.videoTracks.get(0) != null) {
            if (muted) {
                localMediaStream.removeTrack(this.localMediaStream.videoTracks.get(0));
            } else {
                if (localMediaStream.videoTracks.isEmpty()) {
                    localMediaStream.addTrack(this.localMediaStream.videoTracks.get(0));
                }
            }
        }
        if (localMediaStreamCamera != null && this.localMediaStreamCamera.videoTracks.get(0) != null) {
            if (muted) {
                localMediaStreamCamera.removeTrack(this.localMediaStreamCamera.videoTracks.get(0));
            } else {
                if (localMediaStreamCamera.videoTracks.isEmpty()) {
                    localMediaStreamCamera.addTrack(this.localMediaStreamCamera.videoTracks.get(0));
                }
            }
        }
    }

    public void muteAudioStream(boolean muted) {
        if (localMediaStream != null && this.localMediaStream.audioTracks.get(0) != null) {
            if (muted) {
                localMediaStream.removeTrack(this.localMediaStream.audioTracks.get(0));
            } else {
                if (localMediaStream.audioTracks.isEmpty()) {
                    localMediaStream.addTrack(this.localMediaStream.audioTracks.get(0));
                }
            }
        }
        if (localMediaStreamCamera != null && this.localMediaStreamCamera.audioTracks.get(0) != null) {
            if (muted) {
                localMediaStreamCamera.removeTrack(this.localMediaStreamCamera.audioTracks.get(0));
            } else {
                if (localMediaStreamCamera.audioTracks.isEmpty()) {
                    localMediaStreamCamera.addTrack(this.localMediaStreamCamera.audioTracks.get(0));
                }
            }
        }
    }


    private void startVideoCapture(boolean enabled) {
        VideoCapturer curVideoCapturer = videoCapturers.get(curCameraIndex);
        if (curVideoCapturer != null) {
            if (enabled) {
                curVideoCapturer.startCapture(currParam.width, currParam.height, currParam.framerate);
            } else {
                try {
                    curVideoCapturer.stopCapture();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean useCamera2() {
        return Camera2Enumerator.isSupported(context);
    }

    private boolean createVideoCapturer() {
//        return createCameraCapturer(new Camera1Enumerator(false));
        if (useCamera2()) {
            return createCameraCapturer(new Camera2Enumerator(context));
        } else {
            return createCameraCapturer(new Camera1Enumerator(true));
        }
    }

    private boolean createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                videoCapturers.put(0, enumerator.createCapturer(deviceName, null));
            } else {
                videoCapturers.put(1, enumerator.createCapturer(deviceName, null));
            }
        }

        return videoCapturers.size() > 0;
    }
}
