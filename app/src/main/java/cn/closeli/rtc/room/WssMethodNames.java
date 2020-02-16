package cn.closeli.rtc.room;

public class WssMethodNames {

    //5.2 接入登录
    public static final String accessIn = "accessIn";
    //5.3 接入登出
    public static final String accessOut = "accessOut";
    //5.4 发起会议
    public static final String createRoom = "createRoom";
    //5.5 加入会议
    //5.5.1  请求加入到会议室 (参考原生信令)
    public static final String joinRoom = "joinRoom";
    //5.5.2  新与会人请求推送音视频 (参考原生信令)
    public static final String publishVideo = "publishVideo";
    //5.5.3  新与会人请求接收音视频 (参考原生信令)
    public static final String receiveVideoFrom = "receiveVideoFrom";

    public static final String unsubscribeFromVideo = "unsubscribeFromVideo";
    //5.5.4  云平台通知其他参与方有新人入会 (参考原生信令)
    public static final String participantJoined = "participantJoined";
    //5.5.5  云平台通知其他参与方新人发布音视频 (参考原生信令)
    public static final String participantPublished = "participantPublished";
    //5.5.6  其他与会人请求接收新与会人音视频 (参考原生信令)
    public static final String receiveVideoFrom_  = "receiveVideoFrom ";
    //5.5.7  交换候选地址 (参考原生信令)
    public static final String onIceCandidate  = "onIceCandidate";
    //5.6 离开会议
    //5.6.1  与会人离开会议 (参考原生信令)
    public static final String leaveRoom = "leaveRoom";
    //5.6.2  云平台通知参与方有人离开会议 (参考原生信令)
    public static final String participantLeft = "participantLeft";
    //5.8 开始屏幕共享
    @Deprecated
    public static final String shareScreen = "shareScreen";
    //5.9 结束屏幕共享
    @Deprecated
    public static final String stopShareScreen = "stopShareScreen";
    //5.10 获取参会人员列表
    public static final String getParticipants = "getParticipants";
    //5.11 设置音频状态
    public static final String setAudioStatus = "setAudioStatus";
    //5.12 设置视频状态
    public static final String setVideoStatus = "setVideoStatus";
    //5.13 举手
    public static final String raiseHand = "raiseHand";
    //5.14 放下手
    public static final String putDownHand = "putDownHand";
    //5.15 会议锁定
    public static final String lockSession = "lockSession";
    //5.17 会议取消锁定
    public static final String unlockSession = "unlockSession";
    //5.18 踢出与会者
    public static final String forceUnpublish = "forceUnpublish";
    //5.18 踢出与会者
    public static final String forceDisconnect = "forceDisconnect";
    //5.19 结束会议
    public static final String closeRoom = "closeRoom";
    //5.16 在线状态保持（参考原始信令）
    public static final String ping = "ping";

    //5.22 主持人邀请入会
    public static final String inviteParticipant = "inviteParticipant";
    //被邀请人 拒绝入会
    public static final String refuseInvite = "refuseInvite";

    //5.23 设置音频状态（扬声器）
    public static final String setAudioSpeakerStatus = "setAudioSpeakerStatus";
    //5.24 设置共享权限
    public static final String setSharePower = "setSharePower";
    //5.25 主持人权限转让
    public static final String transferModerator = "transferModerator";
    //5.26 设置会议房间信息
    public static final String setPresetInfo = "setPresetInfo";
    //5.27 获取会议预设置信息
    public static final String getPresetInfo = "getPresetInfo";

    //5.30 获取点名人员列表
    public static final String getRollCallList = "getRollCallList";

    //5.30 点名发言
    public static final String rollCall = "setRollCall";

    //5.30 结束点名
    public static final String endRollCall = "endRollCall";
    //替换发言
    public static final String replaceRollCall = "replaceRollCall";
    //5.28获取组织树列表
    public static final String getOrgList = "getOrgList";
    //5.29 根据组织树获取账号(设备)列表
    public static final String getUserDeviceList = "getUserDeviceList";

    //5.36 会议延长
    public static final String roomCountDown = "roomCountDown";
    //申请延长
    public static final String roomDelay ="roomDelay";

    //断线重连
    //获取本人参加且尚未结束的会议
    public static final String getNotFinishedRoom = "getNotFinishedRoom";
    // 广播断线重连
    public static final String userBreakLine = "userBreakLine";
    //平台通知所有参与方，断线重连的与会者原sharing流停止推送
    public static final String reconnectPartStopPublishSharing = "reconnectPartStopPublishSharing";

    public static final String stopPublishSharingNotify = "stopPublishSharingNotify";

    //其他与会者申请登录 通知
    public static final String applyForLogin = "applyForLogin";
    //处理其他与会者登录申请
    public static final String confirmApplyForLogin = "confirmApplyForLogin";
    //通知申请登录者 确认结果
    public static final String resultOfLoginApplyNotify ="resultOfLoginApplyNotify";

    public static final String sharingControl ="sharingControl";

    //通知已登录用户强制下线
    public static final String remoteLoginNotify = "remoteLoginNotify";
    public static final String getRoomLayout = "getRoomLayout";
    public static final String majorLayoutNotify = "majorLayoutNotify";
    public static final String broadcastMajorLayout = "broadcastMajorLayout";
    public static final String closeRoomNotify = "closeRoomNotify";
    public static final String distributeShareCastPlayStrategyNotify = "distributeShareCastPlayStrategyNotify";
    public static final String sharingControlNotify = "sharingControlNotify";


    /***********************  MCU  ************************/
    /**
     * 主持人设置窗口布局
     */
    public static final String setConferenceLayout = "setConferenceLayout";

    /**
     *  窗口布局更改通知
     */
    public static final String conferenceLayoutChanged = "conferenceLayoutChanged";

    /**
     * 获取企业通讯录
     */
    public static final String getSubDevOrUser = "getSubDevOrUser";

    /**
     * 获取公司部门
     */
    public static final String getDepartmentTree = "getDepartmentTree";

    /**
     * 开始云台部署
     */
    public static final String startPtzControl = "startPtzControl";

    /**
     * 通知设备云台控制
     */
    public static final String startPtzControlNotify = "startPtzControlNotify";

    /**
     * 停止云台部署
     */
    public static final String stopPtzControl = "stopPtzControl";

    /**
     * 通知设备云台停止控制
     */
    public static final String stopPtzControlNotify = "stopPtzControlNotify";

    /**
     * 升级通知
     */
    public static final String upgradeNotify = "upgradeNotify";

    public static final String MajorPlay = "MajorPlay";
    public static final String MinorPlay = "MinorPlay";

    /**
     * 媒体服务断线重连
     */
    public static final String reconnectSMS = "reconnectSMS";

    /**
     * 获取群组
     */
    public static final String getGroupList = "getGroupList";

    /**
     * 获取群组信息
     */
    public static final String getGroupInfo = "getGroupInfo";
}
