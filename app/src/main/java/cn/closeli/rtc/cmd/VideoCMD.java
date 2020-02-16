package cn.closeli.rtc.cmd;

import android.util.Log;

import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.closeli.rtc.model.Call;
import cn.closeli.rtc.sdk.CLRtcSinaling;

public class VideoCMD {

    /**
     * 获取所有与会者
     *
     * @param roomId
     */
    public static  Map<String, Object> getAllParticipants(String roomId) {
        Map<String, Object> params = new HashMap<>();
        params.put("roomId", roomId);
        return params;
    }

    /**
     * 设置音频状态
     */
    public static Map<String, Object> setAudioStatus(String roomId, String sourceId, List<String> targetId, boolean status) {
        Map<String, Object> params = new HashMap<>();
        //会议ID
        params.put("roomId", roomId);
        //控制发起人员用户ID
        params.put("sourceId", sourceId);
        //目标人员用户ID，缺省时，表示会议中除主持人外的所有参会人员（主持人权限）
        params.put("targetIds", targetId);
        //音频状态，on:打开，off:关闭
        params.put("status", status ? "on" : "off");
        return params;

    }

    /**
     * 设置视频状态
     *
     * @param roomId
     * @param sourceId
     * @param targetId
     * @param status
     * @return
     */
    public static Map<String, Object> setVideoStatus(String roomId, String sourceId, List<String> targetId, boolean status) {
        Map<String, Object> params = new HashMap<>();
        //会议ID
        params.put("roomId", roomId);
        //控制发起人员用户ID
        params.put("sourceId", sourceId);
        //目标人员用户ID，缺省时，表示会议中除主持人外的所有参会人员（主持人权限）
        params.put("targetIds", targetId);
        //音频状态，on:打开，off:关闭
        params.put("status", status ? "on" : "off");
        return params;
    }
    /**
     * 设置扬声器状态
     *
     * @param roomId
     * @param sourceId
     * @param targetId
     * @param status
     * @return
     */
    public static Map<String, Object> setAudioSpeakerStatus(String roomId, String sourceId, String targetId, boolean status) {
        Map<String, Object> params = new HashMap<>();
        //会议ID
        params.put("roomId", roomId);
        //控制发起人员用户ID
        params.put("sourceId", sourceId);
        //目标人员用户ID，缺省时，表示会议中除主持人外的所有参会人员（主持人权限）
        params.put("targetId", targetId);
        //音频状态，on:打开，off:关闭
        params.put("status", status ? "on" : "off");
        return params;
    }
    /**
     * 举手
     *
     * @param roomId
     * @param sourceId
     * @return
     */
    public static Map<String, Object> raiseHand(String roomId, String sourceId) {
        Map<String, Object> params = new HashMap<>();
        //会议ID
        params.put("roomId", roomId);
        //控制发起人员用户ID
        params.put("sourceId", sourceId);
        return params;
    }

    /*
        放手
     */
    public static  Map<String, Object> putDownHand(String roomId, String sourceId, String targetId) {
        Map<String, Object> params = new HashMap<>();
        //会议ID
        params.put("roomId", roomId);
        //控制发起人员用户ID
        params.put("sourceId", sourceId);
        //目标人员用户ID，缺省时，表示会议中除主持人外的所有参会人员（主持人权限）
        params.put("targetId", targetId);
        return params;
    }

    /**
     * 锁定
     *
     * @param roomId
     * @param sourceId
     * @return
     */
    public static  Map<String, Object> lockSession(String roomId, String sourceId) {
        Map<String, Object> params = new HashMap<>();
        //会议ID
        params.put("roomId", roomId);
        //控制发起人员用户ID
        params.put("sourceId", sourceId);
        return params;
    }

    /**
     * 解锁
     *
     * @param roomId
     * @param sourceId
     * @return
     */
    public static  Map<String, Object> unlockSession(String roomId, String sourceId) {
        Map<String, Object> params = new HashMap<>();
        //会议ID
        params.put("roomId", roomId);
        //控制发起人员用户ID
        params.put("sourceId", sourceId);
        return params;
    }


    /*
     屏幕共享
     */
    public static JsonObject shareScreen(String sessionId, String sourceId, int webSocket) {
        JsonObject request = new JsonObject();
        request.addProperty(CLRtcSinaling.FIELD_JSON_RPC, CLRtcSinaling.VALUE_JSON_RPC_VERSION);
        request.addProperty(CLRtcSinaling.FIELD_MSG_ID, webSocket);
        request.addProperty(CLRtcSinaling.FIELD_METHOD, CLRtcSinaling.METHOD_SHARE_SCREEN);

        JsonObject params = new JsonObject();
        params.addProperty(CLRtcSinaling.FIELD_SESSION_ID, sessionId);
        params.addProperty(CLRtcSinaling.FIELD_SOURCE_ID, sourceId);
        request.add(CLRtcSinaling.FIELD_PARAMS, params);
        return request;
    }

    /*
     停止屏幕共享
     */
    public static JsonObject stopShareScreen(String sessionId, String sourceId, int webSocket) {
        JsonObject request = new JsonObject();
        request.addProperty(CLRtcSinaling.FIELD_JSON_RPC, CLRtcSinaling.VALUE_JSON_RPC_VERSION);
        request.addProperty(CLRtcSinaling.FIELD_MSG_ID, webSocket);
        request.addProperty(CLRtcSinaling.FIELD_METHOD, CLRtcSinaling.METHOD_STOP_SHARE_SCREEN);

        JsonObject params = new JsonObject();
        params.addProperty(CLRtcSinaling.FIELD_SESSION_ID, sessionId);
        params.addProperty(CLRtcSinaling.FIELD_SOURCE_ID, sourceId);
        request.add(CLRtcSinaling.FIELD_PARAMS, params);
        return request;
    }

    /**
     * 主持人邀请其他参与方入会
     */
    public static Map<String,Object> inviteParticipant(String roomId, String sourceId, List<String> targetId) {
        Map<String,Object> params = new HashMap<>();
        params.put("roomId",roomId);
        params.put("sourceId",sourceId);
        params.put("targetId",targetId);
        return params;
    }

    public static Map<String,Object> refuseInvite(String roomId,String sourceId) {
        Map<String,Object> params = new HashMap<>();
        params.put("roomId",roomId);
        params.put("sourceId",sourceId);
        return params;
    }

    /**
     * 主持人权限转让
     */
    public static Map<String,Object> transferModerator(String roomId, String sourceId,String targetId) {
        Map<String, Object> params = new HashMap<>();
        params.put("roomId",roomId);
        params.put("sourceId",sourceId);
        params.put("targetId",targetId);
        return params;
    }

    /**
     * 设置共享权限
     */
    public static Map<String,Object> setSharePower(String roomId, String sourceId,String targetId){
        Map<String,Object> params = new HashMap<>();
        params.put("roomId",roomId);
        params.put("sourceId",sourceId);
        params.put("targetId",targetId);
        params.put("operation","off");
        return params;
    }

    /**
     * 设置会议预设信息
     */
    public static Map<String,Object> setPresetInfo(String roomId,String subject,String micStatusInRoom, String sharePowerInRoom, String videoStatusInRoom){
        Map<String,Object> params = new HashMap<>();
        params.put("roomId",roomId);
        params.put("subject",subject);
        params.put("micStatusInRoom",micStatusInRoom);
        params.put("sharePowerInRoom",sharePowerInRoom);
        params.put("videoStatusInRoom",videoStatusInRoom);
        return params;
    }

    /**
     * 获取会议预设信息
     */
    public static Map<String,Object> getPresetInfo(String roomId) {
        Map<String,Object> params = new HashMap<>();
        params.put("roomId",roomId);
        return params;
    }

    /**
     * 点名发言
     */
    public static Map<String, Object> rollcall(String roomId,String sourceId,String targetId) {
        Map<String,Object> parames = new HashMap<>();
        parames.put("roomId",roomId);
        parames.put("sourceId",sourceId);
        parames.put("targetId",targetId);
        return parames;
    }

    public static Map<String,Object> replaceRollCall(String roomId,String sourceId, String endTargetId, String startTargetId) {
        Map<String,Object> parames = new HashMap<>();
        parames.put("roomId",roomId);
        parames.put("sourceId",sourceId);
        parames.put("endTargetId",endTargetId);
        parames.put("startTargetId",startTargetId);
        return parames;
    }

    /**
     * 结束点名发言
     */
    public static Map<String,Object> endRollcall(String roomId,String sourceId,String targetId) {
        Map<String,Object> params = new HashMap<>();
        params.put("roomId",roomId);
        params.put("sourceId",sourceId);
        params.put("targetId",targetId);
        return params;
    }
    /**
     * 获取组织树列表
     */
    public static Map<String,Object> getOrgList() {
        Map<String,Object> params = new HashMap<>();
        return params;
    }
    /**
     * 获取组织树列表
     */
    public static Map<String,Object> getUserDeviceList(String orgId) {
        Map<String,Object> params = new HashMap<>();
        params.put("orgId",orgId);
        return params;
    }

    /**
     * 获取RoomId
     */
    public static Map<String,Object> getRoomId(String roomId) {
        Map<String,Object> params = new HashMap<>();
        params.put("roomId",roomId);
        return params;
    }
}
