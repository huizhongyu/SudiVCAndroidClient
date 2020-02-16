package cn.closeli.rtc.model.ws;

import java.util.ArrayList;
import java.util.List;

import cn.closeli.rtc.model.LayoutInfoBean;
import cn.closeli.rtc.model.MixFlowsBean;
import cn.closeli.rtc.model.PartLinkedListBean;
import cn.closeli.rtc.model.StreamsBean;

public class JoinRoomResp {
    /**
     * id : d2rsrwmjixrimyak
     * metadata : {"clientData": 13}
     * value : [{"id":"yhxrroewpbfzmevk","metadata":"{\"clientData\": 7 }","streams":[{"id":"yhxrroewpbfzmevk_null_EVLZO","hasAudio":true,"hasVideo":true,"filter":{}}]}]
     * sessionId : c89liegnsrkprdbr7slijepo80
     */
/*
{
  "id": "d2rsrwmjixrimyak",
  "createdAt": 1569415002757,
  "metadata": "{\"clientData\":1,\"role\":\"MODERATOR\"}",
  "value": [
    {
      "id": "yhxrroewpbfzmevk",
      "createdAt": 1569415002757,
      "metadata": "{\"clientData\": 7,\"role\":\"PUBLISHER\"}",
      "streams": [
        {
          "id": "yhxrroewpbfzmevk_null_EVLZO",
          "streamType": "MAJOR",
          "hasAudio": true,
          "hasVideo": true,
          "filter": {}
        }
      ]
    }
  ],//value end
  "sessionId": "c89liegnsrkprdbr7slijepo80"
}
 */
    private String id;
    private String metadata;
    private String sessionId;
    private String micStatusInRoom;
    private String sharePowerInRoom;
    private String videoStatusInRoom;
    private String subject;
    private String appShowName;
    private String appShowDesc;
    private int roomCapacity;       //会议最大人数
    private String allowPartOperShare;  //允许被主持人关闭共享权限的情况下自己打开
    private String allowPartOperMic;    //允许被主持人
    private String streamType;
    private String conferenceMode;      //会议模式
    private ArrayList<ValueBean> value;
    private ArrayList<MixFlow> mixFlows;
    private LayoutInfoBean layoutInfo;
    private long createdAt;         //参加会议时间戳
    private long roomCreateAt;      //会议房间创建时间戳

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getStreamType() {
        return streamType;
    }

    public void setStreamType(String streamType) {
        this.streamType = streamType;
    }

    public ArrayList<ValueBean> getValue() {
        return value;
    }

    public void setValue(ArrayList<ValueBean> value) {
        this.value = value;
    }

    public String getMicStatusInRoom() {
        return micStatusInRoom;
    }

    public void setMicStatusInRoom(String micStatusInRoom) {
        this.micStatusInRoom = micStatusInRoom;
    }

    public String getSharePowerInRoom() {
        return sharePowerInRoom;
    }

    public void setSharePowerInRoom(String sharePowerInRoom) {
        this.sharePowerInRoom = sharePowerInRoom;
    }

    public String getVideoStatusInRoom() {
        return videoStatusInRoom;
    }

    public void setVideoStatusInRoom(String videoStatusInRoom) {
        this.videoStatusInRoom = videoStatusInRoom;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getAppShowName() {
        return appShowName;
    }

    public void setAppShowName(String appShowName) {
        this.appShowName = appShowName;
    }

    public String getAppShowDesc() {
        return appShowDesc;
    }

    public void setAppShowDesc(String appShowDesc) {
        this.appShowDesc = appShowDesc;
    }

    public int getRoomCapacity() {
        return roomCapacity;
    }

    public void setRoomCapacity(int roomCapacity) {
        this.roomCapacity = roomCapacity;
    }

    public String getAllowPartOperShare() {
        return allowPartOperShare;
    }

    public void setAllowPartOperShare(String allowPartOperShare) {
        this.allowPartOperShare = allowPartOperShare;
    }

    public String getAllowPartOperMic() {
        return allowPartOperMic;
    }

    public void setAllowPartOperMic(String allowPartOperMic) {
        this.allowPartOperMic = allowPartOperMic;
    }

    public ArrayList<MixFlow> getMixFlows() {
        return mixFlows;
    }

    public void setMixFlows(ArrayList<MixFlow> mixFlows) {
        this.mixFlows = mixFlows;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getRoomCreateAt() {
        return roomCreateAt;
    }

    public void setRoomCreateAt(long roomCreateAt) {
        this.roomCreateAt = roomCreateAt;
    }

    public String getConferenceMode() {
        return conferenceMode;
    }

    public void setConferenceMode(String conferenceMode) {
        this.conferenceMode = conferenceMode;
    }

    public static class MixFlow {
        /**
         * id : yhxrroewpbfzmevk
         * metadata : {"clientData": 7 }
         * streams : [{"id":"kzjfnkxx3fiq9pcx","streamMode":"MIX_MAJOR_AND_SHARING"}]
         */

        private String streamId = "";
        private String streamMode = "";

        public String getStreamId() {
            return streamId;
        }

        public void setStreamId(String streamId) {
            this.streamId = streamId;
        }

        public String getStreamMode() {
            return streamMode;
        }

        public void setStreamMode(String streamMode) {
            this.streamMode = streamMode;
        }
    }

    public LayoutInfoBean getLayoutInfo() {
        return layoutInfo;
    }

    public void setLayoutInfo(LayoutInfoBean layoutInfo) {
        this.layoutInfo = layoutInfo;
    }

    public static class ValueBean {
        /**
         * id : yhxrroewpbfzmevk
         * metadata : {"clientData": 7 }
         * streams : [{"id":"yhxrroewpbfzmevk_null_EVLZO","hasAudio":true,"hasVideo":true,"filter":{}}]
         */

        private String id;
        private String metadata;
        private String speakerStatus;
        private String shareStatus;
        private String handStatus;
        private String appShowName;
        private String appShowDesc;
        private String onlineStatus;
        private String streamType;
        private List<StreamsBean> streams;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getMetadata() {
            return metadata;
        }

        public void setMetadata(String metadata) {
            this.metadata = metadata;
        }

        public String getStreamType() {
            return streamType;
        }

        public void setStreamType(String streamType) {
            this.streamType = streamType;
        }

        public List<StreamsBean> getStreams() {
            return streams;
        }

        public void setStreams(List<StreamsBean> streams) {
            this.streams = streams;
        }

        public String getSpeakerStatus() {
            return speakerStatus;
        }

        public void setSpeakerStatus(String speakerStatus) {
            this.speakerStatus = speakerStatus;
        }

        public String getShareStatus() {
            return shareStatus;
        }

        public void setShareStatus(String shareStatus) {
            this.shareStatus = shareStatus;
        }

        public String getAppShowName() {
            return appShowName;
        }

        public void setAppShowName(String appShowName) {
            this.appShowName = appShowName;
        }

        public String getAppShowDesc() {
            return appShowDesc;
        }

        public void setAppShowDesc(String appShowDesc) {
            this.appShowDesc = appShowDesc;
        }

        public String getHandStatus() {
            return handStatus;
        }

        public void setHandStatus(String handStatus) {
            this.handStatus = handStatus;
        }

        public String getOnlineStatus() {
            return onlineStatus;
        }

        public void setOnlineStatus(String onlineStatus) {
            this.onlineStatus = onlineStatus;
        }
    }
}
