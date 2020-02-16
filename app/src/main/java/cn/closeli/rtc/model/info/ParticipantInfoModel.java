package cn.closeli.rtc.model.info;

import android.graphics.Rect;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import org.webrtc.VideoSink;

import java.util.Objects;

import cn.closeli.rtc.model.ParticipantModel;
import cn.closeli.rtc.peer.CLPeerConnection;
import cn.closeli.rtc.sdk.ProxyVideoSink;
import cn.closeli.rtc.utils.Constants;
import cn.closeli.rtc.utils.RoomManager;

//与会者信息
public class ParticipantInfoModel extends ParticipantModel {
    private String avatar = "";          //头像
    private String post = "";            //职位
    private boolean isChecked;      //是否选中


    private boolean isRecordOpen;   //录制是否打开

    private String deviceName = "";        //设备名
    private String deviceOrgName = "";        //设备组织名
    private String userOrgName = "";         //组织名
    private String handStatus = "";          //举手状态，up:举手，speaker:发言, down:未举手

    private String userId = "";
    private String account = "";
    private String role = "";
    private boolean audioActive = true;//麦克风是否打开
    private boolean videoActive = true;//相机是否打开
    private boolean speakerActive = true;
    private String shareStatus = "";  //共享屏幕是否打开
    private String onlineStatus = "";  //判断是否在线 online


    private String sharePowerStatus = "";
    private String micStatusInRoom = ""; //权限暂时没用
    private String sharePowerInRoom = "";
    private String videoStatusInRoom = "";

    private String appShowName = "";
    private String appShowDesc = "";

    private String subject = "";
    private boolean userBreak = false;


    /**
     * webRTC相关字段
     */
    private String connectionId;
    private boolean published;
    private CLPeerConnection peerConnection;
    private int localStreamType = 0;
    private String streamType;
    private String streamId;
    private String streamMode;
    //MCU
    private int left;              //x坐标
    private int top;              //y坐标
    private int width;          //宽
    private int height;         //高

    //对应布局中的位置
    private int layoutX;
    private int layoutY;
    private int layoutWidth;
    private int layoutHeight;
    private ProxyVideoSink videoSink;
    private Rect rect = new Rect();

    public ParticipantInfoModel(String userId) {
        this.userId = getUserId();
    }

    public ParticipantInfoModel() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
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

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }


    public boolean isRecordOpen() {
        return isRecordOpen;
    }

    public void setRecordOpen(boolean recordOpen) {
        isRecordOpen = recordOpen;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceOrgName() {
        return deviceOrgName;
    }

    public void setDeviceOrgName(String deviceOrgName) {
        this.deviceOrgName = deviceOrgName;
    }

    public String getUserOrgName() {
        return userOrgName;
    }

    public void setUserOrgName(String userOrgName) {
        this.userOrgName = userOrgName;
    }

    public String getHandStatus() {
        return handStatus;
    }

    public void setHandStatus(String handStatus) {
        this.handStatus = handStatus;
    }

    public String getSharePowerStatus() {
        return sharePowerStatus;
    }

    public void setSharePowerStatus(String sharePowerStatus) {
        this.sharePowerStatus = sharePowerStatus;
    }

    public boolean isSpeakerActive() {
        return speakerActive;
    }

    public void setSpeakerActive(boolean speakerActive) {
        this.speakerActive = speakerActive;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public CLPeerConnection getPeerConnection() {
        return peerConnection;
    }

    public void setPeerConnection(CLPeerConnection peerConnection) {
        this.peerConnection = peerConnection;
    }

    public int getLocalStreamType() {
        return localStreamType;
    }

    public void setLocalStreamType(int localStreamType) {
        this.localStreamType = localStreamType;
    }

    public String getStreamType() {
        return streamType;
    }

    public void setStreamType(String streamType) {
        this.streamType = streamType;
    }

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
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

    public String getShareStatus() {
        return shareStatus;
    }

    public void setShareStatus(String shareStatus) {
        this.shareStatus = shareStatus;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public boolean isUserBreak() {
        return userBreak;
    }

    public void setUserBreak(boolean userBreak) {
        this.userBreak = userBreak;
    }

    public String getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(String onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public String getStreamMode() {
        return streamMode;
    }

    public void setStreamMode(String streamMode) {
        this.streamMode = streamMode;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }


    public int getLayoutX() {
        return layoutX;
    }

    public void setLayoutX(int layoutX) {
        rect.left = layoutX;
        this.layoutX = layoutX;
    }

    public int getLayoutY() {
        return layoutY;
    }

    public void setLayoutY(int layoutY) {
        rect.top = layoutY;
        this.layoutY = layoutY;
    }

    public int getLayoutWidth() {
        return layoutWidth;
    }

    public void setLayoutWidth(int layoutWidth) {
        rect.right = layoutX + layoutWidth;
        this.layoutWidth = layoutWidth;
    }

    public int getLayoutHeight() {
        return layoutHeight;
    }

    public void setLayoutHeight(int layoutHeight) {
        rect.bottom = layoutY + layoutHeight;
        this.layoutHeight = layoutHeight;
    }

    public Rect getRect() {
        return rect;
    }

    public boolean isShareable() {
        return "on".equals(shareStatus);
    }

    public boolean isOnline() {
        return TextUtils.isEmpty(onlineStatus) || Constants.ONLINE.equals(onlineStatus);
    }

    public boolean isPlaceholder() {
        return TextUtils.isEmpty(connectionId);
    }

    public boolean isHost() {
        return RoomManager.get().getUserId().equals(userId);
    }

    /**
     * 是否是发言中的状态
     *
     * @return
     */
    public boolean isHandSpeaker() {
        return "speaker".equals(handStatus);
    }

    public ProxyVideoSink getVideoSink() {
        return videoSink;
    }

    public void setVideoSink(ProxyVideoSink videoSink) {
        this.videoSink = videoSink;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof ParticipantInfoModel) {
            ParticipantInfoModel participantInfoModel = (ParticipantInfoModel) obj;
            return participantInfoModel.getUserId().equals(this.userId) && participantInfoModel.streamType.equals(this.streamType);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
