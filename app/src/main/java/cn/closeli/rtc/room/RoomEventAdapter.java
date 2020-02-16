package cn.closeli.rtc.room;

import java.util.ArrayList;
import java.util.List;

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

public class RoomEventAdapter implements RoomEventCallback {
    @Override
    public void onConnectSuccess() {

    }

    @Override
    public void onConnectError(String errMsg) {

    }


    @Override
    public void onParticipantJoined(ParticipantInfoModel participant) {

    }

    @Override
    public void onParticipantLeft(ParticipantInfoModel participant) {

    }

    @Override
    public void onParticipantEvicted(ParticipantInfoModel participant, String connectId) {

    }



    //------------------------

    /**
     * @param roomId
     * @param sessionId
     */
    @Override
    public void onRoomCreated(String roomId, String sessionId) {

    }

    @Override
    public void onWebSocketFailMessage(WsError wsErrorr, String method) {

    }


    @Override
    public void onJoinRoomSuccess(ParticipantInfoModel participant) {

    }
    /**
     * @param participantsList
     * @see RoomClient#getParticipants(String)
     */
    @Override
    public void onGetParticipantsResult(ArrayList<ParticipantInfoModel> participantsList) {

    }

    /**
     * @see RoomClient#lockSession(String, String)
     * 锁定 会议
     */
    @Override
    public void onLockSession(SlingModel slingModel) {

    }

    @Override
    public void unLockSession(SlingModel slingModel) {

    }

    @Override
    public void onLeaveRoomSuccess(String role) {

    }

    @Override
    public void onForceUnPublished(String connectionId) {

    }

    /**
     * @see RoomClient#setAudioStatus(String, String, String, boolean)
     */





    /**
     * @see RoomClient#accessOut()
     */
    @Override
    public void onAccessOut() {

    }



    /**
     * @see RoomClient#raiseHand(String, String)
     * 举手发送 成功
     */
    @Override
    public void onRaiseHandsSended() {

    }


    /**
     * @see RoomClient#raiseHand(String, String)
     * 举手发送 成功
     */
    @Override
    public void onPutDownHandsSended() {

    }



    /**
     * 切换音频状态 触发 成功
     */
    @Override
    public void onAudioStatusSwitchSuccess() {

    }



    /**
     * 收到云平台 推送 音频状态变更
     *
     * @param audioStstusResp
     */
    @Override
    public void onReceiveAudioStatusChange(AudioStstusResp audioStstusResp) {

    }

    /**
     * 收到云平台 推送 视频状态变更
     *
     * @param videoStatusResp
     */
    @Override
    public void onReceiveVideoStatusChange(VideoStatusResp videoStatusResp) {

    }

    @Override
    public void onReceiveSpeakerStatusChange(SlingModel slingModel) {

    }

    /**
     * 收到云平台 推送 举手
     *
     * @param handsStatusResp
     */
    @Override
    public void onReceiveHandsUp(HandsStatusResp handsStatusResp) {

    }

    /**
     * 手放下
     *
     * @param handsStatusResp
     */
    @Override
    public void onReceiveHandsDown(HandsStatusResp handsStatusResp) {

    }

    /**
     * 切换视频状态 触发 成功
     */
    @Override
    public void onVideoStatusSwitchSuccess() {

    }

    /**
     * 主持人邀请入会 成功
     *
     * @param model
     */
    @Override
    public void onInviteJoinSuccess(InviteModel model) {
    }

    @Override
    public void onInviteJoinCallback() {
        
    }

    /**
     * 被邀请人 拒绝
     *
     * @param model
     */
    @Override
    public void refuseInvite(SlingModel model) {

    }

    /**
     * 主持人权限转让
     *
     * @param model
     */
    @Override
    public void onTransferModerator(SlingModel model) {
    }

    /**
     * 设置共享权限
     *
     * @param model
     */
    @Override
    public void onSharePower(SlingModel model) {
    }

    /**
     * 设置会议预设信息
     */
    @Override
    public void onSetPresetInfo() {

    }

    /**
     * 获取会议预设信息
     *
     * @param resp
     */
    @Override
    public void onGetPresetInfo(PresetInfoResp resp) {
    }

    /**
     * 点名发言
     */
    @Override
    public void rollcall(SlingModel slingModel) {

    }

    /**
     * 替换发言
     *
     * @param slingModel
     */
    @Override
    public void replaceRollCall(ReplaceRollcallModel slingModel) {

    }

    /**
     * 结束点名发言
     */
    @Override
    public void endRollcall(SlingModel slingModel) {

    }

    @Override
    public void getOrgList(OrgList orgList) {

    }

    @Override
    public void getUserDeviceList(UserDeviceList userDeviceList) {

    }

    /**
     * 云平台推送 会议倒计时
     *
     * @param model
     */
    @Override
    public void getRoomTimeCountDown(RoomTimeModel model) {

    }

    /**
     * 会议延长时间
     */
    @Override
    public void onRoomDelay() {

    }

    /**
     * 获取本人参加 且尚未结束的会议
     *
     * @param model
     */
    @Override
    public void getNotFinishedRoom(ReJoinModel model) {

    }

    /**
     * 广播参会者掉线
     *
     * @param model
     */
    @Override
    public void userBreakLine(ParticipantInfoModel model) {

    }

//    @Override
//    public void applyForLogin(SingleLoginModel model) {
//
//    }
//
//    @Override
//    public void confirmApplyForLogin(SingleLoginModel model) {
//
//    }
//
//    @Override
//    public void resultOfLoginApplyNotify(SingleLoginModel model) {
//
//    }

    @Override
    public void remoteLoginNotify() {

    }

    @Override
    public void reconnectPartStopPublishSharing(ParticipantInfoModel model) {

    }

    @Override
    public void majorLayoutNotify(RoomLayoutModel roomLayoutModel) {

    }

    @Override
    public void closeRoomNotify() {

    }

    @Override
    public void conferenceLayoutChanged(List<ParticipantInfoModel> participantInfoModels) {

    }

    @Override
    public void getSubDevOrUser(PartDeviceModel orgList) {

    }

    @Override
    public void getDepartmentTree(CompanyModel companyModel) {

    }

    @Override
    public void startPtzControl() {

    }

    @Override
    public void startPtzControlNotify(ControlModel controlModel) {

    }

    @Override
    public void stopPtzControl() {

    }

    @Override
    public void stopPtzControlNotify(ControlModel controlModel) {

    }

    @Override
    public void updateApp(UpdateModel version) {

    }

    @Override
    public void distributeShareCastPlayStrategyNotify(StrategyModel strategyModel) {

    }

    @Override
    public void sharingControlNotify(SlingModel slingModel) {

    }

    @Override
    public void getRoomLayoutCallback(RoomLayoutModel model) {

    }

    @Override
    public void mediaServerReconnect() {

    }

    @Override
    public void getGroupList(GroupListInfoModel groupListInfoModel) {

    }

    @Override
    public void getGroupInfo(GroupInfoModel groupInfoModel) {

    }
}
