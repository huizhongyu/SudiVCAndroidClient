package cn.closeli.rtc.model.rtc;

import java.util.List;

import cn.closeli.rtc.model.MixFlowsBean;
import cn.closeli.rtc.model.StreamsBean;

public class ParticipantPublishedModel {
    private String id;
    private String appShowName;
    private String appShowDesc;
    private List<StreamsBean> streams;
    private List<MixFlowsBean> mixFlows;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<StreamsBean> getStreams() {
        return streams;
    }

    public void setStreams(List<StreamsBean> streams) {
        this.streams = streams;
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

    public List<MixFlowsBean> getMixFlows() {
        return mixFlows;
    }

    public void setMixFlows(List<MixFlowsBean> mixFlows) {
        this.mixFlows = mixFlows;
    }
}
