package cn.closeli.rtc.utils;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.webrtc.MediaStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import cn.closeli.rtc.model.MixFlowsBean;
import cn.closeli.rtc.model.RoomLayoutModel;
import cn.closeli.rtc.model.info.ParticipantInfoModel;
import cn.closeli.rtc.room.RoomClient;
import cn.closeli.rtc.sdk.CLRtcSinaling;

import static cn.closeli.rtc.constract.Constract.ROLLCALL_STATUS_SPEAKER;
import static cn.closeli.rtc.constract.Constract.SFU_SHARING;
import static cn.closeli.rtc.sdk.WebRTCManager.ROLE_PUBLISHER_SUBSCRIBER;
import static cn.closeli.rtc.sdk.WebRTCManager.SHARING;

public class RoomManager {
    private Context mContext;
    private String roomId;
    private String roomName;
    private String userId = "";
    private String hostId = "";
    private String hostConnectId = "";
    private String role;
    private String connectionId;
    private String connectionMixId;
    private String connectionMaxId;
    private String connectionMainId;
    private String connectionShareId;
    private boolean nativeAudio = true;
    private boolean nativeVideo = true;
    private MediaStream mediaStream;
    private String subject;         //会议主题
    private int roomCapacity;             //最大人数
    private int currentNum;             //当前人数
    private boolean isRoomAllowShare = true;    //当前房间 是否允许被主持人关闭共享权限的前提下 自己打开共享权限
    private boolean isRoomAllowMic = true;      //当前房间 是否允许被主持人关闭麦克风的前提下 自己打开麦克风
    private String conferenceMode;              //会议模式
    //维护一个list
    private List<ParticipantInfoModel> participantModels = new CopyOnWriteArrayList<>();
    private ArrayList<ParticipantInfoModel> handsPants;
    private RoomLayoutModel roomLayoutModel = new RoomLayoutModel();
    public static HashMap<Integer, Integer> hashMap = new HashMap<>();
    private boolean isHostByClose;
    //存放会前邀请的人员的id
    public List<String> listOfInvited = new ArrayList<>();
    public long createAt;       //会议创建时间
    private volatile boolean isHostFirst = true;

    public RoomManager() {
    }

    private static class SingletonHolder {
        private static RoomManager instance = new RoomManager();
    }

    public static RoomManager get() {
        return RoomManager.SingletonHolder.instance;
    }

    public void init(Application application) {
        mContext = application.getApplicationContext();

        hashMap.put(12, 4);
        hashMap.put(4, 2);
        hashMap.put(9, 3);
        hashMap.put(6, 3);
    }


    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }


    public String getAccountByUesrId(String userId, String defaultValue) {
        for (ParticipantInfoModel participantModel : participantModels) {
            if (participantModel.getUserId().equals(userId)) {
                return participantModel.getAccount();
            }
        }
        return defaultValue;
    }

    // 获取除SHARING 流 以外的 与会者列表
    public List<ParticipantInfoModel> getParticipants() {
        isHostFirst = true;
        participantModels.clear();
        ArrayList<ParticipantInfoModel> participantInfoModels = RoomClient.get().getRemoteParticipants();
        for (ParticipantInfoModel participantInfoModel : participantInfoModels) {
            if (ROLLCALL_STATUS_SPEAKER.equals(participantInfoModel.getHandStatus())) {
                isHostFirst = false;
            } else {
                isHostFirst = isHostFirst && true;
            }
            if (!SHARING.equals(participantInfoModel.getStreamType())) {
                if (!participantModels.contains(participantInfoModel)) {
                    participantModels.add(participantInfoModel);
                }

                isHostFirst = isHostFirst && true;
            } else {
                isHostFirst = false;
            }
        }
        return participantModels;
    }

    //获取含共享流，且第一个的与会者集合
    public List<ParticipantInfoModel> getParticipantsWithShare() {
        isHostFirst = true;
        participantModels.clear();
        ParticipantInfoModel sharePart = null;
        ArrayList<ParticipantInfoModel> participantInfoModels = RoomClient.get().getRemoteParticipants();
        for (ParticipantInfoModel participantInfoModel : participantInfoModels) {
            if (ROLLCALL_STATUS_SPEAKER.equals(participantInfoModel.getHandStatus())) {
                isHostFirst = false;
            } else {
                isHostFirst = isHostFirst && true;
            }
            if (!SHARING.equals(participantInfoModel.getStreamType())) {
                if (!participantModels.contains(participantInfoModel)) {
                    participantModels.add(participantInfoModel);
                }
                isHostFirst = isHostFirst && true;
            } else {
                isHostFirst = false;
                sharePart = participantInfoModel;
            }
        }
        if (sharePart != null) {
            participantModels.add(0, sharePart);
        }
        return participantModels;
    }

    // 返回不含share的与会者列表
    public List<ParticipantInfoModel> getFilterParticipants(List<ParticipantInfoModel> partLists) {
        List<ParticipantInfoModel> filterList = new ArrayList<>();
        for (ParticipantInfoModel participantInfoModel : partLists) {
            if (!SHARING.equals(participantInfoModel.getStreamType())) {
                filterList.add(participantInfoModel);
            }
        }

        return filterList;
    }

    public ArrayList<ParticipantInfoModel> getConnectParticipants() {
        return RoomClient.get().getRemoteParticipants();
    }

    public void setParticipants(ArrayList<ParticipantInfoModel> participants) {
        ArrayList<ParticipantInfoModel> participantInfoModels = RoomClient.get().getRemoteParticipants();
        for (ParticipantInfoModel participantInfoModel : participantInfoModels) {
            for (ParticipantInfoModel participant : participants) {
                if (participantInfoModel.getUserId().equals(participant.getUserId()) && (!SHARING.equals(participantInfoModel.getStreamType()) && !SFU_SHARING.equals(participantInfoModel.getStreamType()))) {
                    participantInfoModel.setAccount(participant.getAccount());
                    participantInfoModel.setRole(participant.getRole());
                    participantInfoModel.setUserOrgName(participant.getUserOrgName());
                    participantInfoModel.setDeviceName(participant.getDeviceName());
                    participantInfoModel.setDeviceOrgName(participant.getDeviceOrgName());
                    participantInfoModel.setVideoActive(participant.isVideoActive());
                    participantInfoModel.setAudioActive(participant.isAudioActive());
                    participantInfoModel.setHandStatus(participant.getHandStatus());
                    participantInfoModel.setAppShowName(participant.getAppShowName());
                    participantInfoModel.setAppShowDesc(participant.getAppShowDesc());
                    participantInfoModel.setShareStatus(participant.getShareStatus());
                }
            }
        }
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    /**
     * 全员静音 主持人不静音
     *
     * @param targetId
     * @return
     */
    public boolean isAllMute(String targetId) {
        return TextUtils.isEmpty(targetId) && !role.equals(ROLE_PUBLISHER_SUBSCRIBER);
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }


    public ArrayList<ParticipantInfoModel> particalToHands() {
        handsPants = new ArrayList<>();
        handsPants.clear();
        for (ParticipantInfoModel participant : participantModels) {
            if (CLRtcSinaling.VALUE_STATUS_UP.equals(participant.getHandStatus()) || CLRtcSinaling.VALUE_STATUS_SPEAKER.equals(participant.getHandStatus())) {
                handsPants.add(participant);
            }
        }
        return handsPants;
    }

    public boolean isNativeAudio() {
        return nativeAudio;
    }

    public void setNativeAudio(boolean nativeAudio) {
        this.nativeAudio = nativeAudio;
    }

    public boolean isNativeVideo() {
        return nativeVideo;
    }

    public void setNativeVideo(boolean nativeVideo) {
        this.nativeVideo = nativeVideo;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public int getRoomCapacity() {
        return roomCapacity;
    }

    public void setRoomCapacity(int roomCapacity) {
        this.roomCapacity = roomCapacity;
    }

    public int getCurrentNum() {
        return participantModels.size();
    }

    public MediaStream getMediaStream() {
        return mediaStream;
    }

    public void setMediaStream(MediaStream mediaStream) {
        this.mediaStream = mediaStream;
    }

    public boolean isRoomAllowShare() {
        return isRoomAllowShare;
    }

    public void setRoomAllowShare(boolean roomAllowShare) {
        isRoomAllowShare = roomAllowShare;
    }

    public boolean isRoomAllowMic() {
        return isRoomAllowMic;
    }

    public void setRoomAllowMic(boolean roomAllowMic) {
        isRoomAllowMic = roomAllowMic;
    }

    public RoomLayoutModel getRoomLayoutModel() {
        return roomLayoutModel;
    }

    public void setRoomLayoutModel(RoomLayoutModel roomLayoutModel) {
        this.roomLayoutModel = roomLayoutModel;
    }

    public String getConnectionMixId() {
        return connectionMixId;
    }

    public void setConnectionMixId(String connectionMixId) {
        this.connectionMixId = connectionMixId;

    }

    public String getConferenceMode() {
        return conferenceMode;
    }

    public void setConferenceMode(String conferenceMode) {
        this.conferenceMode = conferenceMode;
    }

    public List<String> getListOfInvited() {
        return listOfInvited;
    }

    public void setListOfInvited(List<String> listOfInvited) {
        this.listOfInvited = listOfInvited;
    }

    public boolean isHost() {
        if (!TextUtils.isEmpty(userId) && userId.equals(hostId)) {
            return true;
        }
        return false;
    }

    public long getCreateAt() {
        return createAt;
    }

    public void setCreateAt(long createAt) {
        this.createAt = createAt;
    }

    /**
     * 过滤主持人
     *
     * @param models
     */
    public void filterHost(List<ParticipantInfoModel> models) {
        if (models == null || models.isEmpty()) {
            return;
        }
        for (int i = models.size() - 1; i >= 0; i--) {
            ParticipantInfoModel model = models.get(i);
            if (!TextUtils.isEmpty(userId) && userId.equals(model.getUserId())) {
                models.remove(model);
            }
        }
    }

    public boolean isHostByClose() {
        return isHostByClose;
    }

    public void setHostByClose(boolean hostByClose) {
        isHostByClose = hostByClose;
    }

    public boolean isHostFirst() {
        return isHostFirst;
    }

    public void setHostFirst(boolean hostFirst) {
        isHostFirst = hostFirst;
    }

    public String getConnectionMainId() {
        return connectionMainId;
    }

    public void setConnectionMainId(String connectionMainId) {
        this.connectionMainId = connectionMainId;
    }

    public String getConnectionShareId() {
        return connectionShareId;
    }

    public void setConnectionShareId(String connectionShareId) {
        this.connectionShareId = connectionShareId;
    }

    public String getHostConnectId() {
        return hostConnectId;
    }

    public void setHostConnectId(String hostConnectId) {
        this.hostConnectId = hostConnectId;
    }
}
