package cn.closeli.rtc.room;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.closeli.rtc.model.ControlModel;
import cn.closeli.rtc.model.InviteModel;
import cn.closeli.rtc.model.ReJoinModel;
import cn.closeli.rtc.model.ReplaceRollcallModel;
import cn.closeli.rtc.model.RoomLayoutModel;
import cn.closeli.rtc.model.RoomTimeModel;
import cn.closeli.rtc.model.SlingModel;
import cn.closeli.rtc.model.StrategyModel;
import cn.closeli.rtc.model.UpdateModel;
import cn.closeli.rtc.model.info.CompanyModel;
import cn.closeli.rtc.model.info.GroupInfoModel;
import cn.closeli.rtc.model.info.GroupListInfoModel;
import cn.closeli.rtc.model.info.PartDeviceModel;
import cn.closeli.rtc.model.info.ParticipantInfoModel;
import cn.closeli.rtc.model.rtc.OrgList;
import cn.closeli.rtc.model.rtc.UserDeviceList;
import cn.closeli.rtc.model.ws.AudioStstusResp;
import cn.closeli.rtc.model.ws.HandsStatusResp;
import cn.closeli.rtc.model.ws.PresetInfoResp;
import cn.closeli.rtc.model.ws.VideoStatusResp;
import cn.closeli.rtc.model.ws.WsError;

/**
 * 主要是用于将roomEventCallBack回调分发到各个注册对象
 */
public class RoomEventCallBackWrapper implements RoomEventCallback {
    Map<Class, RoomEventCallback> roomEventCallbacks = new HashMap<>();

    public boolean isEmpty() {
        return roomEventCallbacks.isEmpty();
    }

    public void addRoomEventCallback(Class clazName, RoomEventCallback roomEventCallback) {
        this.roomEventCallbacks.put(clazName, roomEventCallback);
    }

    @Override
    public void onConnectSuccess() {
        for (RoomEventCallback roomEventCallback : roomEventCallbacks.values()) {
            roomEventCallback.onConnectSuccess();
        }
    }

    @Override
    public void onConnectError(String errMsg) {
        for (RoomEventCallback roomEventCallback : roomEventCallbacks.values()) {
            roomEventCallback.onConnectError(errMsg);
        }
    }

    @Override
    public void onParticipantJoined(ParticipantInfoModel participant) {
        for (RoomEventCallback roomEventCallback : roomEventCallbacks.values()) {
            roomEventCallback.onParticipantJoined(participant);
        }
    }

    @Override
    public void onParticipantLeft(ParticipantInfoModel participant) {
        for (RoomEventCallback roomEventCallback : roomEventCallbacks.values()) {
            roomEventCallback.onParticipantLeft(participant);
        }
    }

    @Override
    public void onParticipantEvicted(ParticipantInfoModel participant, String connectId) {
        for (RoomEventCallback roomEventCallback : roomEventCallbacks.values()) {
            roomEventCallback.onParticipantEvicted(participant, connectId);
        }
    }

    /**
     * @param roomId
     * @param sessionId
     */
    @Override
    public void onRoomCreated(String roomId, String sessionId) {
        for (RoomEventCallback roomEventCallback : roomEventCallbacks.values()) {
            roomEventCallback.onRoomCreated(roomId, sessionId);
        }
    }

    @Override
    public void onWebSocketFailMessage(WsError wsErrorr, String method) {
        for (RoomEventCallback roomEventCallback : roomEventCallbacks.values()) {
            roomEventCallback.onWebSocketFailMessage(wsErrorr, method);
        }
    }

    /**
     * @param participant
     */
    @Override
    public void onJoinRoomSuccess(ParticipantInfoModel participant) {
        for (RoomEventCallback roomEventCallback : roomEventCallbacks.values()) {
            roomEventCallback.onJoinRoomSuccess(participant);
        }
    }


    /**
     * @param participantsList
     * @see RoomClient#getParticipants(String)
     */
    @Override
    public void onGetParticipantsResult(ArrayList<ParticipantInfoModel> participantsList) {
        for (RoomEventCallback roomEventCallback : roomEventCallbacks.values()) {
            roomEventCallback.onGetParticipantsResult(participantsList);
        }
    }


    /**
     * @see RoomClient#lockSession(String, String)
     * 锁定 会议
     */
    @Override
    public void onLockSession(SlingModel slingModel) {
        for (RoomEventCallback roomEventCallback : roomEventCallbacks.values()) {
            roomEventCallback.onLockSession(slingModel);
        }
    }

    @Override
    public void unLockSession(SlingModel slingModel) {
        for (RoomEventCallback roomEventCallback : roomEventCallbacks.values()) {
            roomEventCallback.unLockSession(slingModel);
        }
    }

    /**
     *
     */
    @Override
    public void onLeaveRoomSuccess(String role) {
        for (RoomEventCallback roomEventCallback : roomEventCallbacks.values()) {
            roomEventCallback.onLeaveRoomSuccess(role);
        }
    }

    @Override
    public void onForceUnPublished(String connectionId) {
        for (RoomEventCallback roomEventCallback : roomEventCallbacks.values()) {
            roomEventCallback.onForceUnPublished(connectionId);
        }
    }

    /**
     * @see RoomClient#accessOut()
     */
    @Override
    public void onAccessOut() {
        for (RoomEventCallback roomEventCallback : roomEventCallbacks.values()) {
            roomEventCallback.onAccessOut();
        }
    }


    /**
     * @see RoomClient#raiseHand(String, String)
     * 举手发送 成功
     */
    @Override
    public void onRaiseHandsSended() {
        for (RoomEventCallback roomEventCallback : roomEventCallbacks.values()) {
            roomEventCallback.onRaiseHandsSended();
        }
    }


    /**
     * @see RoomClient#raiseHand(String, String)
     * 举手发送 成功
     */
    @Override
    public void onPutDownHandsSended() {
        for (RoomEventCallback roomEventCallback : roomEventCallbacks.values()) {
            roomEventCallback.onPutDownHandsSended();
        }
    }


    /**
     * 切换音频状态 触发 成功
     */
    @Override
    public void onAudioStatusSwitchSuccess() {
        for (RoomEventCallback roomEventCallback : roomEventCallbacks.values()) {
            roomEventCallback.onAudioStatusSwitchSuccess();
        }
    }

    /**
     * 收到云平台 推送 音频状态变更
     *
     * @param audioStstusResp
     */
    @Override
    public void onReceiveAudioStatusChange(AudioStstusResp audioStstusResp) {
        for (RoomEventCallback roomEventCallback : roomEventCallbacks.values()) {
            roomEventCallback.onReceiveAudioStatusChange(audioStstusResp);
        }
    }

    /**
     * 收到云平台 推送 视频状态变更
     *
     * @param videoStatusResp
     */
    @Override
    public void onReceiveVideoStatusChange(VideoStatusResp videoStatusResp) {
        for (RoomEventCallback roomEventCallback : roomEventCallbacks.values()) {
            roomEventCallback.onReceiveVideoStatusChange(videoStatusResp);
        }
    }

    /**
     * 收到云平台 推送 扬声器状态变更
     *
     * @param videoStatusResp
     */
    @Override
    public void onReceiveSpeakerStatusChange(SlingModel videoStatusResp) {
        for (RoomEventCallback roomEventCallback : roomEventCallbacks.values()) {
            roomEventCallback.onReceiveSpeakerStatusChange(videoStatusResp);
        }
    }

    /**
     * 收到云平台 推送 举手
     *
     * @param handsStatusResp
     */
    @Override
    public void onReceiveHandsUp(HandsStatusResp handsStatusResp) {
        for (RoomEventCallback roomEventCallback : roomEventCallbacks.values()) {
            roomEventCallback.onReceiveHandsUp(handsStatusResp);
        }
    }

    /**
     * 手放下
     *
     * @param handsStatusResp
     */
    @Override
    public void onReceiveHandsDown(HandsStatusResp handsStatusResp) {
        for (RoomEventCallback roomEventCallback : roomEventCallbacks.values()) {
            roomEventCallback.onReceiveHandsDown(handsStatusResp);
        }
    }

    /**
     * 切换视频状态 触发 成功
     */
    @Override
    public void onVideoStatusSwitchSuccess() {
        for (RoomEventCallback roomEventCallback : roomEventCallbacks.values()) {
            roomEventCallback.onVideoStatusSwitchSuccess();
        }
    }

    /**
     * 主持人邀请入会 成功
     */
    @Override
    public void onInviteJoinSuccess(InviteModel slingModel) {
        for (RoomEventCallback roomEventCallback : roomEventCallbacks.values()) {
            roomEventCallback.onInviteJoinSuccess(slingModel);
        }
    }

    @Override
    public void onInviteJoinCallback() {
        for (RoomEventCallback roomEventCallback : roomEventCallbacks.values()) {
            roomEventCallback.onInviteJoinCallback();
        }
    }

    /**
     * 被邀请人 拒绝
     *
     * @param model
     */
    @Override
    public void refuseInvite(SlingModel model) {
        for (RoomEventCallback roomEventCallback : roomEventCallbacks.values()) {
            roomEventCallback.refuseInvite(model);
        }
    }

    /**
     * 主持人权限转让
     *
     * @param model
     */
    @Override
    public void onTransferModerator(SlingModel model) {
        for (RoomEventCallback roomEventCallback : roomEventCallbacks.values()) {
            roomEventCallback.onTransferModerator(model);
        }
    }

    /**
     * 设置共享权限
     *
     * @param model
     */
    @Override
    public void onSharePower(SlingModel model) {
        for (RoomEventCallback roomEventCallback : roomEventCallbacks.values()) {
            roomEventCallback.onSharePower(model);
        }
    }

    /**
     * 设置会议预设信息
     */
    @Override
    public void onSetPresetInfo() {
        for (RoomEventCallback roomEventCallback : roomEventCallbacks.values()) {
            roomEventCallback.onSetPresetInfo();
        }
    }

    /**
     * 获取会议预设信息
     *
     * @param resp
     */
    @Override
    public void onGetPresetInfo(PresetInfoResp resp) {
        for (RoomEventCallback roomEventCallback : roomEventCallbacks.values()) {
            roomEventCallback.onGetPresetInfo(resp);
        }
    }

    /**
     * 点名发言
     */
    @Override
    public void rollcall(SlingModel slingModel) {
        for (RoomEventCallback roomEventCallback : roomEventCallbacks.values()) {
            roomEventCallback.rollcall(slingModel);
        }
    }

    /**
     * 替换发言
     *
     * @param slingModel
     */
    @Override
    public void replaceRollCall(ReplaceRollcallModel slingModel) {
        for (RoomEventCallback roomEventCallback : roomEventCallbacks.values()) {
            roomEventCallback.replaceRollCall(slingModel);
        }
    }

    /**
     * 结束点名发言
     */
    @Override
    public void endRollcall(SlingModel slingModel) {
        for (RoomEventCallback roomEventCallback : roomEventCallbacks.values()) {
            roomEventCallback.endRollcall(slingModel);
        }
    }

    @Override
    public void getOrgList(OrgList orgList) {
        for (RoomEventCallback roomEventCallback : roomEventCallbacks.values()) {
            roomEventCallback.getOrgList(orgList);
        }
    }

    @Override
    public void getUserDeviceList(UserDeviceList userDeviceList) {
        for (RoomEventCallback roomEventCallback : roomEventCallbacks.values()) {
            roomEventCallback.getUserDeviceList(userDeviceList);
        }
    }

    /**
     * 云平台推送 会议倒计时
     *
     * @param model
     */
    @Override
    public void getRoomTimeCountDown(RoomTimeModel model) {
        for (RoomEventCallback roomEventCallback : roomEventCallbacks.values()) {
            roomEventCallback.getRoomTimeCountDown(model);
        }
    }

    /**
     * 会议延长时间
     */
    @Override
    public void onRoomDelay() {
        for (RoomEventCallback roomEventCallback : roomEventCallbacks.values()) {
            roomEventCallback.onRoomDelay();
        }
    }

    /**
     * 获取本人参加 且尚未结束的会议
     *
     * @param model
     */
    @Override
    public void getNotFinishedRoom(ReJoinModel model) {
        for (RoomEventCallback roomEventCallback : roomEventCallbacks.values()) {
            roomEventCallback.getNotFinishedRoom(model);
        }
    }

    /**
     * 通知有与会者掉线
     */
    @Override
    public void userBreakLine(ParticipantInfoModel model) {
        for (RoomEventCallback roomEventCallback : roomEventCallbacks.values()) {
            roomEventCallback.userBreakLine(model);
        }
    }

    /**
     * 通知有与会者掉线
     */
    @Override
    public void reconnectPartStopPublishSharing(ParticipantInfoModel model) {
        for (RoomEventCallback roomEventCallback : roomEventCallbacks.values()) {
            roomEventCallback.reconnectPartStopPublishSharing(model);
        }
    }
//    /**
//     * //其他与会者申请登录通知
//     * @param model
//     */
//    public void applyForLogin(SingleLoginModel model) {
//        for (RoomEventCallback roomEventCallback: roomEventCallbacks.values()) {
//            roomEventCallback.applyForLogin(model);
//        }
//    }
//
//    /**
//     * 处理其他与会者登录请求
//     */
//    public void confirmApplyForLogin(SingleLoginModel model) {
//        for (RoomEventCallback roomEventCallback: roomEventCallbacks.values()) {
//            roomEventCallback.confirmApplyForLogin(model);
//        }
//    }
//
//    /**
//     * 通知申请登录者 结果
//     */
//    public void resultOfLoginApplyNotify(SingleLoginModel model) {
//        for (RoomEventCallback roomEventCallback : roomEventCallbacks.values()){
//            roomEventCallback.resultOfLoginApplyNotify(model);
//        }
//    }

    /**
     * 通知已经登录的用户强制下线
     */
    @Override
    public void remoteLoginNotify() {
        for (RoomEventCallback roomEventCallback : roomEventCallbacks.values()) {
            roomEventCallback.remoteLoginNotify();
        }
    }

    @Override
    public void majorLayoutNotify(RoomLayoutModel roomLayoutModel) {
        for (RoomEventCallback roomEventCallback : roomEventCallbacks.values()) {
            roomEventCallback.majorLayoutNotify(roomLayoutModel);
        }
    }

    @Override
    public void closeRoomNotify() {
        for (RoomEventCallback roomEventCallback : roomEventCallbacks.values()) {
            roomEventCallback.closeRoomNotify();
        }
    }

    @Override
    public void conferenceLayoutChanged(List<ParticipantInfoModel> participantInfoModels) {
        for (RoomEventCallback roomEventCallback : roomEventCallbacks.values()) {
            roomEventCallback.conferenceLayoutChanged(participantInfoModels);
        }
    }

    void removeRoomEventCallback(Class clazz) {
        this.roomEventCallbacks.remove(clazz);
    }

    @Override
    public void getSubDevOrUser(PartDeviceModel orgList) {
        for (RoomEventCallback roomEventCallback : roomEventCallbacks.values()) {
            roomEventCallback.getSubDevOrUser(orgList);
        }
    }

    @Override
    public void getDepartmentTree(CompanyModel companyModel) {
        for (RoomEventCallback roomEventCallback : roomEventCallbacks.values()) {
            roomEventCallback.getDepartmentTree(companyModel);
        }
    }

    @Override
    public void startPtzControl() {
        for (RoomEventCallback roomEventCallback: roomEventCallbacks.values()) {
            roomEventCallback.startPtzControl();
        }
    }

    @Override
    public void startPtzControlNotify(ControlModel controlModel) {
        for (RoomEventCallback roomEventCallback:roomEventCallbacks.values()) {
            roomEventCallback.startPtzControlNotify(controlModel);
        }
    }

    @Override
    public void stopPtzControl() {
        for (RoomEventCallback roomEventCallback: roomEventCallbacks.values()) {
            roomEventCallback.stopPtzControl();
        }
    }

    @Override
    public void stopPtzControlNotify(ControlModel controlModel) {
        for (RoomEventCallback roomEventCallback: roomEventCallbacks.values()) {
            roomEventCallback.stopPtzControlNotify(controlModel);
        }
    }

    @Override
    public void updateApp(UpdateModel version) {
        for (RoomEventCallback roomEventCallback: roomEventCallbacks.values()) {
            roomEventCallback.updateApp(version);
        }
    }

    @Override
    public void distributeShareCastPlayStrategyNotify(StrategyModel strategyModel) {
        for (RoomEventCallback roomEventCallback: roomEventCallbacks.values()) {
            roomEventCallback.distributeShareCastPlayStrategyNotify(strategyModel);
        }
    }

    @Override
    public void sharingControlNotify(SlingModel slingModel) {
        for (RoomEventCallback roomEventCallback: roomEventCallbacks.values()) {
            roomEventCallback.sharingControlNotify(slingModel);
        }
    }

    @Override
    public void getRoomLayoutCallback(RoomLayoutModel model) {
        for (RoomEventCallback roomEventCallback: roomEventCallbacks.values()) {
            roomEventCallback.getRoomLayoutCallback(model);
        }
    }

    @Override
    public void mediaServerReconnect() {
        for (RoomEventCallback roomEventCallback: roomEventCallbacks.values()) {
            roomEventCallback.mediaServerReconnect();
        }
    }

    @Override
    public void getGroupList(GroupListInfoModel groupListInfoModel) {
        for (RoomEventCallback roomEventCallback: roomEventCallbacks.values()) {
            roomEventCallback.getGroupList(groupListInfoModel);
        }
    }

    @Override
    public void getGroupInfo(GroupInfoModel groupInfoModel) {
        for (RoomEventCallback roomEventCallback:roomEventCallbacks.values()) {
            roomEventCallback.getGroupInfo(groupInfoModel);
        }
    }
}

