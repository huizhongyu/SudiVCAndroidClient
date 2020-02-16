package cn.closeli.rtc.net;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.JsonObject;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import cn.closeli.rtc.app.BasePadMsg;
import cn.closeli.rtc.app.BaseParams;
import cn.closeli.rtc.cmd.PadCMD;
import cn.closeli.rtc.model.InviteModel;
import cn.closeli.rtc.model.PadModel;
import cn.closeli.rtc.model.RoomTimeModel;
import cn.closeli.rtc.model.SingleLoginModel;
import cn.closeli.rtc.model.SlingModel;
import cn.closeli.rtc.model.http.GsonProvider;
import cn.closeli.rtc.model.info.ParticipantInfoModel;
import cn.closeli.rtc.model.rtc.ParticipantEvicted;
import cn.closeli.rtc.model.ws.AudioStstusResp;
import cn.closeli.rtc.room.PadEvent;
import cn.closeli.rtc.room.WssMethodNames;
import cn.closeli.rtc.room.WssPadName;
import cn.closeli.rtc.room.WssPadName;
import cn.closeli.rtc.utils.Constants;
import cn.closeli.rtc.utils.L;
import cn.closeli.rtc.utils.LooperExecutor;
import cn.closeli.rtc.utils.SPEditor;

public class ServerManager {
    private ServerSocket webServer = null;
    private Map<WebSocket, String> userMap = new HashMap<WebSocket, String>();
    private LooperExecutor executor;
    private PadEvent padEvent;
    private final static String TAG = "webServer";
    /**
     * ws 发送文字 requestId 计数
     */
    private AtomicInteger atomReqId;
    private WebSocket webSocket;

    public ServerManager() {
    }

    private static class Singlet {
        private static ServerManager instance = new ServerManager();
    }

    public static ServerManager get() {
        return ServerManager.Singlet.instance;
    }

    public void initStartServer() {
        initExecutor();
        create();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                webServer = new ServerSocket(new InetSocketAddress(8177), ServerManager.this);
                webServer.run();
            }
        });
    }

    public void initExecutor() {
        this.executor = new LooperExecutor();
    }

    public void create() {
        executor.requestStart();
    }

    public void UserLogin(String userName, WebSocket socket) {
        if (userName != null || socket != null) {
            userMap.put(socket, userName);
            Log.i(TAG, "LOGIN:" + userName);
            SendMessageToAll(userName + "...Login...");
        }
    }

    public void UserLeave(WebSocket socket) {
        if (userMap.containsKey(socket)) {
            String userName = userMap.get(socket);
            Log.i(TAG, "Leave:" + userName);
            userMap.remove(socket);
            SendMessageToAll(userName + "...Leave...");
        }
    }

    public void SendMessageToUser(WebSocket socket, String message) {
        if (socket != null) {
            socket.send(message);
        }
    }

    public void SendMessageToUser(String userName, String message) {
        Set<WebSocket> ketSet = userMap.keySet();
        for (WebSocket socket : ketSet) {
            String name = userMap.get(socket);
            if (name != null) {
                if (name.equals(userName)) {
                    socket.send(message);
                    break;
                }
            }
        }
    }

    public void SendMessageToAll(String message) {
        Set<WebSocket> ketSet = userMap.keySet();
        for (WebSocket socket : ketSet) {
            String name = userMap.get(socket);
            if (name != null) {
                socket.send(message);
            }
        }
    }

    public void stop() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {

                    webServer.stop();

                    Log.i("TAG", "Stop ServerSocket Success...");
                } catch (Exception e) {
                    Log.i("TAG", "Stop ServerSocket Failed...");
                    e.printStackTrace();
                }
            }
        });
    }

    private synchronized int getAtomReqId() {
        return atomReqId.get();
    }

    /**
     * 通过 WebSocket 发送数据
     *
     * @param method
     * @param params
     */
    private static final String JSON_RPC = "2.0";

    private void sendResultJson(WebSocket webSocket, @Nullable Map<String, Object> params, BasePadMsg basePadMsg) {
        Map<String, Object> up = new HashMap<>();
        up.put("jsonrpc", JSON_RPC);
        up.put("id", basePadMsg.getId());
        up.put("method", basePadMsg.getMethod());
        if (null != params)//ping时候 用
            up.put("result", params);

        //自增 reqId
        String json = GsonProvider.provide().toJson(up);
        if (!json.contains("ping")) {
            L.d("sendWebServer -> %1$s", json);
        } else {
            L.d("ping -> %1$s", json);
        }
        if (webSocket != null) {
            webSocket.send(json);
        }
    }

    private void sendMethodJson(WebSocket webSocket, @Nullable Map<String, Object> params, BaseParams baseParams) {
        Map<String, Object> up = new HashMap<>();
        up.put("jsonrpc", JSON_RPC);
        up.put("id", baseParams.getId());
        up.put("method", baseParams.getMethod());
        if (null != params)//ping时候 用
            up.put("params", params);

        //自增 reqId
        String json = GsonProvider.provide().toJson(up);
        if (!json.contains("ping")) {
            L.d("sendWebServer -> %1$s", json);
        } else {
            L.d("ping -> %1$s", json);
        }
        if (webSocket != null) {
            webSocket.send(json);
        }
    }

    /**
     * 5.2 接入登录
     * 客户端接入登录到云平台保持在线状态
     */
    public void accessIn(WebSocket webSocket, BasePadMsg baseParams) {
        Map<String, Object> params = PadCMD.accessPadIn(baseParams.getMethod());
        sendResultJson(webSocket, params, baseParams);
    }

    /**
     * 5.2 接入登录
     * 设置固定
     */
    public void userInfoToPad() {
        Map<String, Object> params = PadCMD.userInfoToPad();
        BaseParams baseParams = new BaseParams();
        baseParams.setId(100000);
        baseParams.setMethod(WssPadName.userInfoToPad);
        sendMethodJson(webSocket, params, baseParams);
    }

    /**
     * 5.2 接入登录
     * 设置固定
     */
    public void participantJoin(ParticipantInfoModel participantInfoModel) {
        Map<String, Object> params = PadCMD.participantJoin(participantInfoModel);
        BaseParams baseParams = new BaseParams();
        baseParams.setId(100001);
        baseParams.setMethod(WssPadName.participantJoin);
        sendMethodJson(webSocket, params, baseParams);
    }

    /**
     * 5.2 接入登录
     * 设置固定
     */
    public void participantLocalJoin(ParticipantInfoModel participantInfoModel) {
        Map<String, Object> params = PadCMD.participantJoin(participantInfoModel);
        BaseParams baseParams = new BaseParams();
        baseParams.setId(100002);
        baseParams.setMethod(WssPadName.participantLocalJoin);
        sendMethodJson(webSocket, params, baseParams);
    }

    /**
     * 5.2 接入登录
     * 客户端接入登录到云平台保持在线状态
     */
    public void pingpong(WebSocket webSocket, BaseParams baseParams) {
        Map<String, Object> params = PadCMD.pingpong();
        BasePadMsg basePadMsg = new BasePadMsg();
        basePadMsg.setId(baseParams.getId());
        basePadMsg.setJsonrpc(baseParams.getJsonrpc());
        basePadMsg.setMethod(baseParams.getMethod());
        sendResultJson(webSocket, params, basePadMsg);
    }

    public void handlePadMsg(WebSocket socket, JSONObject json, JsonObject text) throws
            JSONException {
        this.webSocket = socket;

        if (!json.has(Constants.PADMSG)) {
            Log.e(TAG, "No params");
        } else {
//            final JsonObject params = json.get(Constants.PARAMS).getAsJsonObject();
//            String method = json.get(Constants.METHOD).getAsString();


            final JSONObject params = new JSONObject(json.getString(Constants.PADMSG));
            String method = json.getString(Constants.METHOD);
            switch (method) {
                case WssPadName.accessPadIn:
                    BasePadMsg<PadModel> audio = BasePadMsg.fromJson(text, PadModel.class);
                    PadModel padModel = audio.getPadMsg();
                    accessIn(socket, audio);
//                    padEvent.accessPadIn(padModel);
                    break;
                case WssPadName.setAudio:
                    padEvent.setAudio();
                    break;
                case WssPadName.setSpeak:
                    padEvent.setSpeak();
                    break;
                case WssPadName.setVideo:
                    padEvent.setVideo();
                    break;
                case WssPadName.leaveRoom:
                    padEvent.leavePadRoom();
                    break;
                case WssPadName.closeRoom:
                    padEvent.leavePadRoom();
                    break;
                case WssPadName.shareHDMI:
                    padEvent.shareHDMI();
                    break;
                default:
                    throw new JSONException("Can't understand method: " + method);
            }
        }
    }

    public void handleParams(WebSocket webSocket, JSONObject json, JsonObject result) throws JSONException {
        this.webSocket = webSocket;

        String method = json.getString(Constants.METHOD);

        switch (method) {
            case WssPadName.ping:
                L.d("handleSuccess heartbeat ping-pong  result -> %1$s", result);
                BaseParams<PadModel> ping = BaseParams.fromJson(result, PadModel.class);
                pingpong(webSocket, ping);
                break;
            default:
                throw new JSONException("Can't understand method: " + method);
        }
    }

    public void sendIdToMessage(int id, String method) {
        Map<String, Object> params = PadCMD.receivedMethod(id, method);
        BaseParams basePadMsg = new BaseParams();
        basePadMsg.setId(id + 100);
        basePadMsg.setMethod(WssPadName.idMethod);
        sendMethodJson(webSocket, params, basePadMsg);
    }

    public void sendMessage(String text) {
        if (webSocket != null) {
            L.d("sendWebSocketServer -> %1$s", text);
            webSocket.send(text);
        }

    }

    public PadEvent getPadEvent() {
        return padEvent;
    }

    public void setPadEvent(PadEvent padEvent) {
        this.padEvent = padEvent;
    }
}
