package cn.closeli.rtc.cmd;

import android.text.TextUtils;

import com.google.gson.JsonObject;

import org.webrtc.IceCandidate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.closeli.rtc.BuildConfig;
import cn.closeli.rtc.constract.Constract;
import cn.closeli.rtc.model.ShareCastPlayStrategyModel;
import cn.closeli.rtc.model.StrategyModel;
import cn.closeli.rtc.model.info.ParticipantInfoModel;
import cn.closeli.rtc.room.RoomClient;
import cn.closeli.rtc.room.SudiRole;
import cn.closeli.rtc.sdk.CLRtcSinaling;
import cn.closeli.rtc.utils.Constants;
import cn.closeli.rtc.utils.DeviceSettingManager;
import cn.closeli.rtc.utils.L;
import cn.closeli.rtc.utils.SPEditor;
import cn.closeli.rtc.utils.SystemUtil;

import static cn.closeli.rtc.utils.Constants.STREAM_MAJOR;
import static cn.closeli.rtc.utils.Constants.STREAM_SHARING;

public class UserCMD {


    public static JsonObject normalInfo(int webSocket, String method) {
        JsonObject request = new JsonObject();
        request.addProperty(CLRtcSinaling.FIELD_JSON_RPC, CLRtcSinaling.VALUE_JSON_RPC_VERSION);
        request.addProperty(CLRtcSinaling.FIELD_MSG_ID, webSocket);
        request.addProperty(CLRtcSinaling.FIELD_METHOD, method);
        return request;
    }

    public static JsonObject pingMessage(int webSocket) {
        JsonObject request;
        request = normalInfo(webSocket, "ping");
        JsonObject params = new JsonObject();
        if (webSocket == 0) {
            params.addProperty("interval", "3000");
        }
        request.add(CLRtcSinaling.FIELD_PARAMS, params);
        return request;
    }

    /**
     * 新的与会人加入
     *
     * @return
     */
    public static Map<String, Object> sendJoinRoom(String roomId, String password, SudiRole role, int localStreamType, String streamType, String joinType) {
        String userId = SPEditor.instance().getUserId();
        Map<String, Object> params = new HashMap<>();
        params.put("metadata", String.format("{\"clientData\": %1$s}", userId));
        params.put("session", roomId);
        params.put("password", password);
        params.put("dataChannels", false);
        params.put("secret", "");
        params.put("platform", "android");
        params.put("role", role.value());
        params.put("streamType", streamType); // 主流，分享流
        params.put("joinType", joinType); // 主流，分享流
        return params;
    }

    /**
     * 开始推流
     *
     * @param sdpOffer
     * @return
     */
    public static Map<String, Object> sendPublishVideo(String sdpOffer, int localStreamType, ParticipantInfoModel participant) {
        Map<String, Object> params = new HashMap<>();
        params.put("videoActive", String.valueOf(participant.isVideoActive()));
        params.put("audioActive", String.valueOf(participant.isAudioActive()));
        params.put("hasVideo", "true");
        params.put("hasAudio", "true");
        if (localStreamType > 0) {
            params.put("streamType", STREAM_SHARING);
        } else {
            params.put("streamType", STREAM_MAJOR);
        }
//        params.put("sdpOffer", "v=0");
        params.put("sdpOffer", sdpOffer);//
        params.put("audioOnly", false);
        params.put("doLoopback", false);

        return params;
    }

    /**
     * 新的与会人加入
     *
     * @return
     */
    public static Map<String, Object> getRoomLayout(String roomId) {
        Map<String, Object> params = new HashMap<>();
        params.put("roomId", roomId);
        return params;
    }

    /**
     * 新的与会人加入
     *
     * @return
     */
    public static Map<String, Object> getBroadcastMajorLayout(int mode, ArrayList<String> strings, String sendType) {
        Map<String, Object> params = new HashMap<>();
        params.put("mode", mode);
        params.put("layout", strings);
        params.put("type", sendType);
        return params;
    }

    public static JsonObject sendUnPublishVideo(int webSocket) {
        JsonObject request;
        request = normalInfo(webSocket, "unpublishVideo");
        JsonObject params = new JsonObject();

        request.add(CLRtcSinaling.FIELD_PARAMS, params);
        return request;
    }

    /**
     * 拉流
     *
     * @param senderId
     * @param sdpOffer
     * @return
     */
    public static Map<String, Object> sendReceiveVideoFrom(String streamMode, String senderId, String sdpOffer) {
        Map<String, Object> params = new HashMap<>();
        if (Constract.MIX_MAJOR_AND_SHARING.equals(streamMode) || Constract.SFU_SHARING.equals(streamMode)) {
            params.put("streamMode", streamMode);
        }
        params.put("sdpOffer", sdpOffer);
        params.put("sender", senderId + "_webcam");
        return params;
    }

    public static Map<String, Object> sendUnsubscribeFromVideo(String senderId, String streamType) {
        Map<String, Object> params = new HashMap<>();
        params.put("sender", senderId + "_webcam");
        if (Constract.MIX_MAJOR_AND_SHARING.equals(streamType)) {
            params.put("streamMode", streamType);
        }
        return params;
    }

    public static Map<String, Object> sendOnIceCandidate(String participantId, IceCandidate localIceCandidate, String streamMode) {
        Map<String, Object> params = new HashMap<>();
        params.put("sdpMid", localIceCandidate.sdpMid);
        params.put("sdpMLineIndex", Integer.toString(localIceCandidate.sdpMLineIndex));
        params.put("candidate", localIceCandidate.sdp);
        params.put("endpointName", participantId);
//        if (Constract.SFU_SHARING.equals(streamMode)) {
//            params.put("streamMode", streamMode);
//        }
        return params;
    }

    /*
      离开房间
     */
    public static Map<String, Object> leaveRoom(String roomId, String sourceId, String streamType) {
        Map<String, Object> params = new HashMap<>();
        params.put("sourceId", sourceId);
        params.put("roomId", roomId);
        params.put("streamType", streamType);
        return params;
    }

    /**
     * 关闭房间
     *
     * @param roomId
     * @return
     */
    public static Map<String, Object> closeRoom(String roomId) {
        Map<String, Object> params = new HashMap<>();
        params.put("roomId", roomId);
        return params;
    }

    /**
     * 强制与会者停止推流
     *
     * @return
     */
    public static Map<String, Object> forceUnpublish(String streamId) {
        Map<String, Object> params = new HashMap<>();
        params.put("streamId", streamId);
        return params;
    }

    /**
     * 踢出与会者
     *
     * @return
     */
    public static Map<String, Object> forceDisconnect(String streamId) {
        Map<String, Object> params = new HashMap<>();
        params.put("connectionId", streamId);
        return params;
    }

    /**
     * 用户登录
     *
     * @return
     */
    public static Map<String, Object> accessIn(boolean forceLogin) {
        String account = SPEditor.instance().getAccount();
        String token = SPEditor.instance().getToken();
        Map<String, Object> params = new HashMap<>();
        params.put("account", account);
        params.put("token", token);
        params.put("mac", SystemUtil.getMacAddress());
        params.put("deviceVersion", BuildConfig.VERSION_NAME);
        params.put("serialNumber", !TextUtils.isEmpty(SystemUtil.getSerial()) ? SystemUtil.getSerial() : "");
        params.put("forceLogin", forceLogin);
        params.put("accessType", "terminal");
        if (SystemUtil.getSystemModel().equals("C9Z")) {
            params.put("ability", Constants.MULTICASTPLAY + "," + Constants.SCREENSHARE);
        } else {
            params.put("ability", "");
        }
        ShareCastPlayStrategyModel shareCastPlayStrategyModel = new ShareCastPlayStrategyModel();
        shareCastPlayStrategyModel.setShareCastPlayStrategy(Constants.MINORPLAY);
        params.put("terminalConfig", shareCastPlayStrategyModel);
        params.put("deviceModel", SystemUtil.getSystemModel());
        params.put("clientTimestamp", System.currentTimeMillis());
        return params;
    }

    public static Map<String, Object> createRoom(String roomId, String password) {
        Map<String, Object> params = new HashMap<>();

        params.put("roomId", roomId);
        params.put("password", password);
        params.put("micStatusInRoom", "on");
        params.put("sharePowerInRoom", "on");
        params.put("videoStatusInRoom", "on");
        params.put("subject", DeviceSettingManager.getInstance().getFromSp().getSubject());
        params.put("roomCapacity", DeviceSettingManager.getInstance().getFromSp().getRoomCapacity());
        if (DeviceSettingManager.getInstance().getFromSp().getDuration() != 9999f) {
            params.put("duration", DeviceSettingManager.getInstance().getFromSp().getDuration());
        }
        params.put("useIdInRoom", DeviceSettingManager.getInstance().getFromSp().isAllIdInto() ? "allParticipants" : "onlyModerator");
        params.put("allowPartOperMic", DeviceSettingManager.getInstance().getFromSp().isOpenMicroSelf() ? "on" : "off");
        params.put("allowPartOperShare", DeviceSettingManager.getInstance().getFromSp().isOpenShareSelf() ? "on" : "off");
        params.put("conferenceMode", DeviceSettingManager.getInstance().getFromSp().isSFUMode() ? "SFU" : "MCU");
        return params;
    }

//    /**
//     * 单点登录  处理其他与会者请求
//     */
//    public static Map<String,Object> confirmApplyForLogin(boolean accept, String applicantSessionId) {
//        Map<String, Object> params = new HashMap<>();
//        params.put("accept",accept);
//        params.put("applicantSessionId",applicantSessionId);
//        return params;
//    }
}
