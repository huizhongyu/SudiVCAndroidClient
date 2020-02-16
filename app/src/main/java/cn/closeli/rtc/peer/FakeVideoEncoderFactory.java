package cn.closeli.rtc.peer;

import android.util.Log;

import org.webrtc.VideoCodecInfo;
import org.webrtc.VideoEncoder;
import org.webrtc.VideoEncoderFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FakeVideoEncoderFactory implements VideoEncoderFactory {
    //    @javax.annotation.Nullable
    private FakeVideoEncoder fakeVideoEncoder;

    @Override
    public VideoEncoder createEncoder(VideoCodecInfo videoCodecInfo) {
        Log.d("FakeVideoEncoderFactory", "createEncoder: ");
        if (fakeVideoEncoder == null) {
            fakeVideoEncoder = new FakeVideoEncoder();
        }
        return fakeVideoEncoder;
    }

    @Override
    public VideoCodecInfo[] getSupportedCodecs() {
        Log.d("FakeVideoEncoderFactory", "getSupportedCodecs: ");
        List<VideoCodecInfo> codecs = new ArrayList();
        codecs.add(new VideoCodecInfo("H264", new HashMap()));
//        codecs.add(new VideoCodecInfo("VP8", new HashMap()));

        return codecs.toArray(new VideoCodecInfo[codecs.size()]);
    }

    public FakeVideoEncoder getFakeVideoEncoder() {
        return fakeVideoEncoder;
    }

    public void setFakeVideoEncoder(FakeVideoEncoder fakeVideoEncoder) {
        this.fakeVideoEncoder = fakeVideoEncoder;
    }
}
