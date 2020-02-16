package cn.closeli.rtc.sdk;

import org.webrtc.Logging;
import org.webrtc.MediaStream;
import org.webrtc.VideoFrame;
import org.webrtc.VideoSink;

import cn.closeli.rtc.model.ParticipantModel;
import cn.closeli.rtc.model.info.ParticipantInfoModel;

/**
 * 画面渲染
 */
public class ProxyVideoSink implements VideoSink {
    private static final String TAG = "ProxyVideoSink";
    private VideoSink target;
    private ParticipantInfoModel participantModel;
    private MediaStream stream;
    @Override
    synchronized public void onFrame(VideoFrame frame) {
        if (target == null) {
            Logging.d(TAG,  "Dropping frame in proxy because target is null.");
            return;
        }
        target.onFrame(frame);
    }

    synchronized public void setTarget(VideoSink target) {
        this.target = target;
    }

    public VideoSink getTarget() {
        return target;
    }
}