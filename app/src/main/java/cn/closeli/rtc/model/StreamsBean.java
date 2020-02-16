package cn.closeli.rtc.model;

public class StreamsBean {
    /**
     * id : yhxrroewpbfzmevk_null_EVLZO
     * hasAudio : true
     * hasVideo : true
     * filter : {}
     */

    private String id;
    private boolean hasAudio;
    private boolean hasVideo;
    private FilterBean filter;
    private String streamType;
    private boolean audioActive;
    private boolean videoActive;
    private String shareStatus;
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isHasAudio() {
        return hasAudio;
    }

    public void setHasAudio(boolean hasAudio) {
        this.hasAudio = hasAudio;
    }

    public boolean isHasVideo() {
        return hasVideo;
    }

    public void setHasVideo(boolean hasVideo) {
        this.hasVideo = hasVideo;
    }

    public boolean isAudioActive() {
        return audioActive;
    }

    public void setAudioActive(boolean audioActive) {
        this.audioActive = audioActive;
    }

    public boolean isVideoActive() {
        return videoActive;
    }

    public void setVideoActive(boolean videoActive) {
        this.videoActive = videoActive;
    }

    public FilterBean getFilter() {
        return filter;
    }

    public void setFilter(FilterBean filter) {
        this.filter = filter;
    }

    public String getStreamType() {
        return streamType;
    }

    public void setStreamType(String streamType) {
        this.streamType = streamType;
    }

    public static class FilterBean {
    }

    public String getShareStatus() {
        return shareStatus;
    }

    public void setShareStatus(String shareStatus) {
        this.shareStatus = shareStatus;
    }
}
