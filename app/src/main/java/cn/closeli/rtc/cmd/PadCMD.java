package cn.closeli.rtc.cmd;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

import cn.closeli.rtc.model.info.ParticipantInfoModel;
import cn.closeli.rtc.peer.CLPeerConnection;
import cn.closeli.rtc.room.RoomClient;
import cn.closeli.rtc.utils.RoomManager;
import cn.closeli.rtc.utils.SPEditor;
import cn.closeli.rtc.utils.SystemUtil;

public class PadCMD {
    /**
     * 硬终端回应会控权限连接
     *
     * @return
     */
    public static Map<String, Object> accessPadIn(String method) {
        Map<String, Object> params = new HashMap<>();
        params.put("status", "on");
        params.put("padMethod", method);
        params.put("sessionId", method);
        return params;
    }

    /**
     * ping
     */
    public static Map<String, Object> pingpong() {
        Map<String, Object> params = new HashMap<>();
        params.put("value", "pong");
        return params;
    }

    /**
     * 得到方法
     */
    public static Map<String, Object> receivedMethod(int receivedId, String method) {
        Map<String, Object> params = new HashMap<>();
        params.put("receivedId", String.valueOf(receivedId));
        params.put("idToMethod", method);
        return params;
    }

    /**
     * 信息同步
     */
    public static Map<String, Object> userInfoToPad() {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", SPEditor.instance().getUserId());
        params.put("isNativeAudio", RoomManager.get().isNativeAudio());
        params.put("isNativeVideo", RoomManager.get().isNativeVideo());
        return params;
    }

    /**
     * 信息同步
     */
    public static Map<String, Object> participantJoin(ParticipantInfoModel participantInfoModel) {
        Map<String, Object> params = new HashMap<>();
        params.put("avatar", participantInfoModel.getAvatar());
        params.put("post", participantInfoModel.getPost());
        params.put("isChecked", participantInfoModel.isChecked());
        params.put("isRecordOpen", participantInfoModel.isRecordOpen());
        params.put("deviceName", participantInfoModel.getDeviceName());
        params.put("deviceOrgName", participantInfoModel.getDeviceOrgName());
        params.put("userOrgName", participantInfoModel.getUserOrgName());
        params.put("handStatus", participantInfoModel.getHandStatus());
        params.put("userId", participantInfoModel.getUserId());
        params.put("account", participantInfoModel.getAccount());
        params.put("role", participantInfoModel.getRole());
        params.put("audioActive", participantInfoModel.isAudioActive());
        params.put("videoActive", participantInfoModel.isVideoActive());
        params.put("speakerActive", participantInfoModel.isSpeakerActive());
        params.put("shareStatus", participantInfoModel.getShareStatus());
        params.put("onlineStatus", participantInfoModel.getOnlineStatus());
        params.put("sharePowerStatus", participantInfoModel.getSharePowerStatus());
        params.put("micStatusInRoom", participantInfoModel.getMicStatusInRoom());
        params.put("videoStatusInRoom", participantInfoModel.getVideoStatusInRoom());
        params.put("appShowName", participantInfoModel.getAppShowName());
        params.put("appShowDesc", participantInfoModel.getAppShowDesc());
        params.put("subject", participantInfoModel.getSubject());
        params.put("userBreak", participantInfoModel.isUserBreak());
        params.put("connectionId", participantInfoModel.getConnectionId());
        params.put("published", participantInfoModel.isPublished());
        params.put("localStreamType", participantInfoModel.getLocalStreamType());
        params.put("streamType", participantInfoModel.getStreamType());
        params.put("streamId", participantInfoModel.getStreamId());


        return params;
    }
}
