package cn.closeli.rtc.utils;

import android.app.Application;
import android.content.Context;
import android.view.View;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import cn.closeli.rtc.room.RoomClient;

public class RoomControl {

    private Context mContext;

    public RoomControl() {
    }

    private static class SingletonHolder {
        private static RoomControl instance = new RoomControl();
    }

    public static RoomControl get() {
        return RoomControl.SingletonHolder.instance;
    }

    public void init(Application application) {
        mContext = application.getApplicationContext();
    }

    public void allMute(boolean status) {
        setAudioStatus(RoomClient.get().getRoomId(),
                SPEditor.instance().getUserId(),
                new ArrayList<>(),
                !status);
    }
    /**
     * 音频开关
     *
     */
    public void switchVideo(boolean status,List<String> targetId) {
        setVideoStatus(RoomClient.get().getRoomId(),
                SPEditor.instance().getUserId(),
                targetId,
                !status);
    }
    /**
     * 扬声器
     *
     */
    public void switchSpeaker(boolean status,String targetId) {
        setAudioSpeakerStatus(RoomClient.get().getRoomId(),
                SPEditor.instance().getUserId(),
                targetId,
                !status);
    }

    /**
     * 音频开关
     *
     */
    public void switchAudio(boolean status,List<String> targetId) {
//        enabled = audioStatus(enabled);
        setAudioStatus(RoomClient.get().getRoomId(),
                SPEditor.instance().getUserId(),
                targetId,
                !status);
    }

    /**
     * 设置频道 音频 状态
     *
     * @param roomId
     * @param sourceId
     * @param targetId
     * @param status
     */
    private void setAudioStatus(String roomId, String sourceId, List<String> targetId, boolean status) {
        RoomClient.get().setAudioStatus(roomId, sourceId, targetId, status);
    }

    /**
     * 设置 频道 视频 状态
     *
     * @param roomId
     * @param sourceId
     * @param targetId
     * @param status
     */
    private void setVideoStatus(String roomId, String sourceId, List<String> targetId, boolean status) {
        RoomClient.get().setVideoStatus(roomId, sourceId, targetId, status);
    }
    /**
     * 设置 扬声器状态
     *
     * @param roomId
     * @param sourceId
     * @param targetId
     * @param status
     */
    private void setAudioSpeakerStatus(String roomId, String sourceId, String targetId, boolean status) {
        RoomClient.get().setAudioSpeakerStatus(roomId, sourceId, targetId, status);
    }

    /**
     *  开始云台控制接口
     * @param connectionId 设备序列号
     * @param operateCode 操作码
     * @param maxDuration 最大持续时间
     */
    public void startPtzControl(String connectionId,int operateCode,long maxDuration){
        RoomClient.get().startPtzControl(connectionId,operateCode,maxDuration);
    }

    /**
     * 停止云台控制
     * @param connectionId 设备序列号
     */
    public void stopPtzControl(@NotNull String connectionId) {
        RoomClient.get().stopPtzControl(connectionId);
    }

}
