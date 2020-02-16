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
import cn.closeli.rtc.model.info.GroupModel;
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
 * websocket到页面层的一些回调，主要是信令服务一块
 */
public interface RoomEventCallback {
    void onConnectSuccess();

    void onConnectError(String errMsg);


    void onParticipantJoined(ParticipantInfoModel participant);

    void onParticipantLeft(ParticipantInfoModel participant);

    void onParticipantEvicted(ParticipantInfoModel participant, String connectId);

    /**
     * @param roomId
     */
    void onRoomCreated(String roomId, String sessionId);

    /**
     * @param wsErrorr
     */
    void onWebSocketFailMessage(WsError wsErrorr, String method);

    /**
     *
     */
//    void onJoinRoomSuccess();
    void onJoinRoomSuccess(ParticipantInfoModel participant);


    /**
     * @param participantsList
     * @see RoomClient#getParticipants(String)
     */

    void onGetParticipantsResult(ArrayList<ParticipantInfoModel> participantsList);

    /**
     * @see RoomClient#lockSession(String, String)
     * 锁定 会议
     */
    void onLockSession(SlingModel slingModel);

    /**
     * 取消会议锁定
     */
    void unLockSession(SlingModel slingModel);

    /**
     *
     */
    void onLeaveRoomSuccess(String role);

    /**
     * 停止推流
     */
    void onForceUnPublished(String connectionId);

    /**
     * @see RoomClient#accessOut()
     * accessOut 成功
     */
    void onAccessOut();


    /**
     * @see RoomClient#raiseHand(String, String)
     * 举手发送 成功
     */
    void onRaiseHandsSended();


    /**
     * @see RoomClient#raiseHand(String, String)
     * 举手发送 成功
     */
    void onPutDownHandsSended();


    /**
     * 切换音频状态 触发 成功
     */
    void onAudioStatusSwitchSuccess();


    /**
     * 收到云平台 推送 音频状态变更
     *
     * @param audioStstusResp
     */
    void onReceiveAudioStatusChange(AudioStstusResp audioStstusResp);

    /**
     * 收到云平台 推送 视频状态变更
     *
     * @param videoStatusResp
     */
    void onReceiveVideoStatusChange(VideoStatusResp videoStatusResp);

    /**
     * 收到云平台 推送 扬声器状态变更
     *
     * @param slingModel
     */
    void onReceiveSpeakerStatusChange(SlingModel slingModel);

    /**
     * 收到云平台 推送 举手
     *
     * @param handsStatusResp
     */
    void onReceiveHandsUp(HandsStatusResp handsStatusResp);

    /**
     * 手放下
     *
     * @param handsStatusResp
     */
    void onReceiveHandsDown(HandsStatusResp handsStatusResp);

    /**
     * 切换视频状态 触发 成功
     */
    void onVideoStatusSwitchSuccess();


    /**
     * 主持人邀请入会 成功
     */
    void onInviteJoinSuccess(InviteModel model);

    void onInviteJoinCallback();

    /**
     * 被邀请人 拒绝
     */
    void refuseInvite(SlingModel model);

    /**
     * 主持人权限转让
     */
    void onTransferModerator(SlingModel model);

    /**
     * 设置共享权限
     */
    void onSharePower(SlingModel model);

    /**
     * 设置会议预设信息
     */
    void onSetPresetInfo();

    /**
     * 获取会议预设信息
     */
    void onGetPresetInfo(PresetInfoResp resp);

    /**
     * 获取点名人员列表
     */
//    void getRollCallList();


    /**
     * 点名开启发言回调
     */
    void rollcall(SlingModel slingModel);

    /**
     * 结束点名发言
     */
    void endRollcall(SlingModel slingModel);

    /**
     * 替换发言
     */
    void replaceRollCall(ReplaceRollcallModel slingModel);

    /**
     * 获取组织树
     */
    void getOrgList(OrgList orgList);

    /**
     * 根据组织树获取账号(设备)列表
     */
    void getUserDeviceList(UserDeviceList userDeviceList);

    /**
     * 云平台推送 会议倒计时
     *
     * @param model
     */
    void getRoomTimeCountDown(RoomTimeModel model);

    /**
     * 会议延长时间
     */
    void onRoomDelay();

    /**
     * 获取本人参加 且尚未结束的会议
     */
    void getNotFinishedRoom(ReJoinModel model);

    /**
     * 广播参会者掉线
     */
    void userBreakLine(ParticipantInfoModel model);

    //    //其他与会者申请登录通知
//    void applyForLogin(SingleLoginModel model);
//    //处理其他与会者登录请求
//    void confirmApplyForLogin(SingleLoginModel model);
//    //通知申请登录者 结果
//    void resultOfLoginApplyNotify(SingleLoginModel model);
    //通知已登录的用户强制下线
    void remoteLoginNotify();

    void reconnectPartStopPublishSharing(ParticipantInfoModel model);

    //通知布局更改
    void majorLayoutNotify(RoomLayoutModel roomLayoutModel);

    void closeRoomNotify();

    //窗口布局更换通知
    void conferenceLayoutChanged(List<ParticipantInfoModel> participantInfoModels);

    //获取企业通讯录
    void getSubDevOrUser(PartDeviceModel partDeviceModel);

    //获取企业子部门及所有设备
    void getDepartmentTree(CompanyModel companyModel);

    //开始云台部署
    void startPtzControl();

    //通知设备云台控制
    void startPtzControlNotify(ControlModel controlModel);

    //停止云台部署
    void stopPtzControl();

    //通知设备云台停止控制
    void stopPtzControlNotify(ControlModel controlModel);

    //更新通知
    void updateApp(UpdateModel version);

    void distributeShareCastPlayStrategyNotify(StrategyModel strategyModel);

    void sharingControlNotify(SlingModel slingModel);

    //获取当前布局
    void getRoomLayoutCallback(RoomLayoutModel model);

    //媒体服务断线重连
    void mediaServerReconnect();

    //获取通讯录 群组
    void getGroupList(GroupListInfoModel groupListInfoModel);

    //获取群组信息
    void getGroupInfo(GroupInfoModel groupInfoModel);
}
