package cn.closeli.rtc.model.ws;

import java.util.ArrayList;
import java.util.List;

import cn.closeli.rtc.model.ParticipantModel;
import cn.closeli.rtc.model.info.ParticipantInfoModel;

public class GetParticipantsResp {
    /**
     * participantList : [{"userId":"111","account":"1@qq","role":"moderator"}]
     * sessionId : 6if78ift1m5s1quil4t0dktjka
     */

    private String sessionId;
    private ArrayList<ParticipantInfoModel> participantList;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public ArrayList<ParticipantInfoModel> getParticipantList() {
        return participantList;
    }

    public void setParticipantList(ArrayList<ParticipantInfoModel> participantList) {
        this.participantList = participantList;
    }


}
