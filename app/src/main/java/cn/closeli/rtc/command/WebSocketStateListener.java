package cn.closeli.rtc.command;

import com.neovisionaries.ws.client.WebSocketState;

public interface WebSocketStateListener {
    void onStateChangeListener(WebSocketState socketState);
    void onSocketConnectedFail();
    void onAccessInSuccessCallback();
}
