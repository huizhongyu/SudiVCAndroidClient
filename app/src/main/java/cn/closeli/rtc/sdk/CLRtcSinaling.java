package cn.closeli.rtc.sdk;

public class CLRtcSinaling {
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
    public static final String METHOD_SHARE_SCREEN = "shareScreen";
    public static final String METHOD_STOP_SHARE_SCREEN = "stopShareScreen";
    public static final String PARTICIPANTS = "participantList";

    public static final String VALUE_JSON_RPC_VERSION = "2.0";
    public static final String VALUE_STATUS_ON = "on";
    public static final String VALUE_STATUS_OFF = "off";
    public static final String VALUE_STATUS_UP = "up";
    public static final String VALUE_STATUS_DOWN = "down";
    public static final String VALUE_STATUS_SPEAKER = "speaker";
    public static final String VALUE_LOCK = "lock";
    public static final String VALUE_UNLOCK = "unlock";
    public static final boolean ACTIVE_TRUE = true;
    public static final boolean ACTIVE_FALSE = false;

    public static class Register {
        private String userId;

        public Register(String userId) {
            this.userId = userId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }
    }



}
