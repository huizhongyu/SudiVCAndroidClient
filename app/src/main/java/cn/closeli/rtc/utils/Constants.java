package cn.closeli.rtc.utils;

/**
 * Created by sergiopaniegoblanco on 19/02/2018.
 */

public final class Constants {
    public static final String VALUE = "value";
    public static final String PARAMS = "params";
    public static final String PADMSG = "padmsg";
    public static final String METHOD = "method";
    public static final String ID = "id";
    public static final String RESULT = "result";
    public static final String ICE_CANDIDATE = "iceCandidate";
    public static final String PARTICIPANT_JOINED = "participantJoined";
    public static final String PARTICIPANT_PUBLISHED = "participantPublished";
    public static final String PARTICIPANT_UNPUBLISHED = "participantUnpublished";
    public static final String PARTICIPANT_LEFT = "participantLeft";
    public static final String participant_Evicted = "participantEvicted";
    public static final String SESSION_ID = "sessionId";
    public static final String CONNECTION_ID = "connectionId";
    public static final String SDP_ANSWER = "sdpAnswer";
    public static final String JOIN_ROOM = "joinRoom";
    public static final String METADATA = "metadata";
    public static final String STREAMS = "streams";
    public static final String STREAMTYPE = "streamType";
    public static final String  ISRECONNECTED = "isReconnected";

    public static final String FIELD_TYPE = "type";
    public static final String FIELD_VALUE = "value";
    public static final String TYPE_REGISTER = "register";
    public static final String TYPE_CALL = "call";
    public static final String TYPE_ANSWER = "answer";

    public static final String FIELD_JSON_RPC = "jsonrpc";
    public static final String FIELD_MSG_ID = "id";
    public static final String FIELD_METHOD = "method";
    public static final String FIELD_PARAMS = "params";
    public static final String FIELD_SESSION_ID = "sessionId";
    public static final String FIELD_RESULT = "result";
    public static final String FIELD_PARTICIPANT_LIST = "participantList";
    public static final String FIELD_ACOUNT = "account";
    public static final String FIELD_ROLE = "role";
    public static final String FIELD_SOURCE_ID = "sourceId";
    public static final String FIELD_TARGET_ID = "targetId";
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_ACTION = "action";

    public static final String METHOD_GET_PARTICIPANTS = "getParticipants";
    public static final String METHOD_SET_AUDIO_STATUS = "setAudioStatus";
    public static final String METHOD_SET_VIDEO_STATUS = "setVideoStatus";
    public static final String METHOD_RAISE_HAND = "raiseHand";
    public static final String METHOD_PUT_DOWN_HAND = "putDownHand";
    public static final String METHOD_LOCK_CONFERENCE = "lockConference";
    public static final String METHOD_LEAVE_CONFERENCE = "leaveConference";
    public static final String METHOD_PING = "ping";
    public static final String METHOD_PONG = "pong";
    public static final String PARTICIPANTS = "participantList";

    public static final String VALUE_JSON_RPC_VERSION = "2.0";
    public static final String VALUE_STATUS_ON = "on";
    public static final String VALUE_STATUS_OFF = "off";
    public static final String VALUE_STATUS_UP = "up";
    public static final String VALUE_STATUS_DOWN = "down";
    public static final String VALUE_LOCK = "lock";
    public static final String VALUE_UNLOCK = "unlock";
    public static final String LOCAL_SCREEN = "_screen";
    public static final String LOCAL_HDMI = "_hdmi";
    public static final String STREAM_MAJOR = "MAJOR";
    public static final String STREAM_MINOR = "MINOR";
    public static final String STREAM_SHARING = "SHARING";

    public static final String ONLINE = "online";           //在线
    public static final String OFFLINE = "offline";         //离线
    public static final String UPGRADING = "upgrading";     //升级中
    public static final String MEETING = "meeting";         //会议中

    public static final String MAJORPLAY = "MajorPlay";         //主屏
    public static final String MINORPLAY = "MinorPlay";         //辅屏

    public static final String MULTICASTPLAY = "MultiCastPlay";         //多屏能力
    public static final String SCREENSHARE = "ScreenShare";         //屏幕共享





    private Constants() {
    }
}
