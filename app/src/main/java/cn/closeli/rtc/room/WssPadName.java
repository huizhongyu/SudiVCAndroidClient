package cn.closeli.rtc.room;

public class WssPadName {
    //5.2 接入登录
    public static final String accessPadIn = "accessPadIn";
    //5.3 接入登出
    public static final String accessOut = "accessPadOut";
    //5.4 发起会议
    public static final String ping = "ping";

    public static final String idMethod = "idMethod";

    public static final String userInfoToPad = "userInfoToPad";

    public static final String setAudio = "setAudio";
    public static final String setVideo = "setVideo";
    public static final String setSpeak = "setSpeak";
    //5.5 加入会议
    //5.5.1  请求加入到会议室 (参考原生信令)
    public static final String joinRoom = "joinPadRoom";
    //5.5.2  新与会人请求推送音视频 (参考原生信令)
    public static final String publishVideo = "publishVideo";
    //5.5.3  新与会人请求接收音视频 (参考原生信令)
    public static final String receiveVideoFrom = "receiveVideoFrom";
    public static final String leaveRoom = "leaveRoom";
    public static final String closeRoom = "closeRoom";
    public static final String shareHDMI = "shareHDMI";
    public static final String participantJoin = "participantJoin";
    public static final String participantLocalJoin = "participantLocalJoin";
}
