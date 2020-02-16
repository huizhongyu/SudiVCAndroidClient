package cn.closeli.rtc.sdk;

import android.util.Log;

import org.webrtc.MediaStream;

import java.util.HashMap;
import java.util.Map;

import cn.closeli.rtc.model.info.ParticipantInfoModel;
import cn.closeli.rtc.utils.L;

public class IViewCallBackWrapper implements IViewCallback{
    Map<Class, IViewCallback> iViewCallbacks = new HashMap<>();

    public boolean isEmpty() {
        return iViewCallbacks.isEmpty();
    }

    public void addIViewCallBac(Class clazName, IViewCallback iViewCallback) {
        this.iViewCallbacks.put(clazName, iViewCallback);
    }

    public void removeIViewCallBac(Class clazName) {
        this.iViewCallbacks.remove(clazName);
    }

    @Override
    public void onSetLocalStream(MediaStream stream) {
        for (IViewCallback iViewCallback : iViewCallbacks.values()) {
            iViewCallback.onSetLocalStream(stream);
        }
    }

    @Override
    public void onSetLocalScreenStream(MediaStream stream) {
        for (IViewCallback iViewCallback : iViewCallbacks.values()) {
            iViewCallback.onSetLocalScreenStream(stream);
        }
    }

    @Override
    public void onSetLocalHDMIStream(MediaStream stream) {
        for (IViewCallback iViewCallback : iViewCallbacks.values()) {
            iViewCallback.onSetLocalHDMIStream(stream);
        }
    }

    @Override
    public void onAddRemoteStream(MediaStream stream, ParticipantInfoModel participant) {

        for (IViewCallback iViewCallback : iViewCallbacks.values()) {
            L.d("onRemoteStreamAdded >>>" + "IViewCallback ," + participant.getConnectionId() + " , streamType : " + participant.getAppShowName());

            iViewCallback.onAddRemoteStream(stream,participant);
        }
    }

    @Override
    public void onCloseRemoteStream(MediaStream stream, ParticipantInfoModel participant) {
        for (IViewCallback iViewCallback : iViewCallbacks.values()) {
            iViewCallback.onCloseRemoteStream(stream,participant);
        }
    }

    @Override
    public void onMixStream(ParticipantInfoModel participant,boolean isHasShare) {
        for (IViewCallback iViewCallback : iViewCallbacks.values()) {
            iViewCallback.onMixStream(participant,isHasShare);
        }
    }
    @Override
    public void publishParticipantInfo(ParticipantInfoModel participant) {
        for (IViewCallback iViewCallback : iViewCallbacks.values()) {
            iViewCallback.publishParticipantInfo(participant);
        }
    }
}
