
package cn.closeli.rtc.room;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.FrameLayout;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketState;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import cn.closeli.rtc.App;
import cn.closeli.rtc.GroupActivity;
import cn.closeli.rtc.LoginActivity;
import cn.closeli.rtc.app.BaseParams;
import cn.closeli.rtc.cmd.UserCMD;
import cn.closeli.rtc.cmd.VideoCMD;

import cn.closeli.rtc.command.LoginStateListener;
import cn.closeli.rtc.command.WebSocketStateListener;
import cn.closeli.rtc.constract.Constract;
import cn.closeli.rtc.constract.WsErrorCode;
import cn.closeli.rtc.model.ControlModel;
import cn.closeli.rtc.model.EventPublish;
import cn.closeli.rtc.model.InviteModel;
import cn.closeli.rtc.model.LayoutInfoBean;
import cn.closeli.rtc.model.MixFlowsBean;
import cn.closeli.rtc.model.PartLinkedListBean;
import cn.closeli.rtc.model.ReJoinModel;
import cn.closeli.rtc.model.ReplaceRollcallModel;
import cn.closeli.rtc.model.RoomLayoutModel;
import cn.closeli.rtc.model.RoomTimeModel;
import cn.closeli.rtc.model.SingleLoginModel;
import cn.closeli.rtc.model.SlingModel;
import cn.closeli.rtc.model.StrategyModel;
import cn.closeli.rtc.model.UpdateModel;
import cn.closeli.rtc.model.http.GsonProvider;
import cn.closeli.rtc.model.info.CompanyModel;
import cn.closeli.rtc.model.info.GroupInfoModel;
import cn.closeli.rtc.model.info.GroupListInfoModel;
import cn.closeli.rtc.model.info.PartDeviceModel;
import cn.closeli.rtc.model.info.ParticipantInfoModel;
import cn.closeli.rtc.model.rtc.IceCandidateModel;
import cn.closeli.rtc.model.rtc.LeaveRoomModel;
import cn.closeli.rtc.model.rtc.OrgList;
import cn.closeli.rtc.model.rtc.ParticipantEvicted;
import cn.closeli.rtc.model.rtc.ParticipantJoinedModel;
import cn.closeli.rtc.model.rtc.ParticipantLeft;
import cn.closeli.rtc.model.rtc.ParticipantPublishedModel;
import cn.closeli.rtc.model.rtc.ParticipantUnPublishedModel;
import cn.closeli.rtc.model.rtc.UserDeviceList;
import cn.closeli.rtc.model.ws.AudioStstusResp;
import cn.closeli.rtc.model.ws.ConferenceLayoutChangedResp;
import cn.closeli.rtc.model.ws.CreatRoomResp;
import cn.closeli.rtc.model.ws.GetParticipantsResp;
import cn.closeli.rtc.model.ws.HandsStatusResp;
import cn.closeli.rtc.model.ws.JoinRoomResp;
import cn.closeli.rtc.model.ws.PresetInfoResp;
import cn.closeli.rtc.model.ws.PublishVideoResp;
import cn.closeli.rtc.model.ws.ReceiveVideoResp;
import cn.closeli.rtc.model.ws.VideoStatusResp;
import cn.closeli.rtc.model.ws.WsError;
import cn.closeli.rtc.net.ServerManager;
import cn.closeli.rtc.utils.CallUtil;
import cn.closeli.rtc.utils.Constants;
import cn.closeli.rtc.utils.DeviceSettingManager;
import cn.closeli.rtc.utils.L;
import cn.closeli.rtc.utils.LooperExecutor;
import cn.closeli.rtc.utils.RoomManager;
import cn.closeli.rtc.utils.SPEditor;
import cn.closeli.rtc.utils.StringUtils;
import cn.closeli.rtc.utils.UIUtils;
import cn.closeli.rtc.utils.trust.TrustAllCertsTrustManager;
import okhttp3.OkHttpClient;

import static cn.closeli.rtc.constract.Constract.MIX_MAJOR_AND_SHARING;
import static cn.closeli.rtc.constract.Constract.SFU_SHARING;
import static cn.closeli.rtc.room.WssMethodNames.accessIn;
import static cn.closeli.rtc.sdk.WebRTCManager.MAJOR;
import static cn.closeli.rtc.sdk.WebRTCManager.ROLE_PUBLISHER_SUBSCRIBER;
import static cn.closeli.rtc.sdk.WebRTCManager.SHARING;
import static cn.closeli.rtc.utils.Constants.LOCAL_SCREEN;

/**
 * 信令的消息
 */
public class RoomClient extends BaseWebSocket {

    private static final String TAG = "RoomClient";

    private String logTag = this.getClass().getCanonicalName();
    private static final Gson gson = new GsonBuilder().create();
    private String wssUrl;
    private WebSocket webSocket;
    private RoomEventCallBackWrapper roomEventCallback = new RoomEventCallBackWrapper();
    private WebSocketStateListener webSocketStateListener;
    private LoginStateListener loginStateListener;
    private OnLocalShareJoinListener onLocalShareJoinListener;
    private int reqId;
    private RoomRTCEvent roomRTCEvent;
    /**
     * ws 发送文字 requestId 计数
     */
    private AtomicInteger atomReqId;

    private int webSocketMsgId = 0;
    /**
     * ws 接收消息 Msg Id
     */
    private AtomicInteger atomWsMsgId;
    private WeakReference<AppCompatActivity> currentActivity;// 当前正在显示的Activity
    private int lastReceivedWsMsgId = -1;

    boolean hasAccessedIn = false;
    private int pingFailedCount = 0;
    int reconnCount = 0;
    private static final float[] reconnInternals = {0.5f, 1.0f, 2.0f, 4.0f, 8.0f, 16.0f, 32.0f};
    private String handsId;
    private boolean hasHands;
    boolean isForceLogin = false;           //是否强制登录
    private ServerManager manager;
    boolean isAccessInSuccess = false;      //登录成功标志。 error制false
    private boolean isShowRollCall;     //是否展示发言弹窗
    private int currentLeftHandsUp = 0;
    private String currentShareUser = "";

    //MCU
    private @LayoutMode
    int currentMode;               //当前布局模式

    private int getLastReceivedWsMsgId() {
        return lastReceivedWsMsgId;
    }

    private void setLastReceivedWsMsgId(int lastReceivedWsMsgId) {
        this.lastReceivedWsMsgId = lastReceivedWsMsgId;
    }

    /**
     * 存储 各个 WebSocket接口的 requestId
     * 用于WebSocket-onTextMessage 收到消息时,区分 各个接口 返回数据
     */
    private Map<Integer, String> wsReqReplyIds;

    private void saveReqIdByMethod(int lastReqId, String methodName) {
        wsReqReplyIds.put(lastReqId, methodName);
    }

    private ScheduledThreadPoolExecutor poolExecutor;
    Map<Class, RoomEventCallback> roomEventCallbacks = new HashMap<>();


    /**
     * 返回 methodName 的 上一个 缓存的 reqId,如果不存在 返回 -1
     *
     * @param reqId 返回数据json里 解析到的 id字段
     * @return
     */
    private @Nullable
    String getMethodNameById(int reqId) {
        for (Map.Entry<Integer, String> methodReqId : wsReqReplyIds.entrySet()) {
//            Log.i("addNewView", methodReqId.getKey() + "value:" + methodReqId.getValue());
            if (methodReqId.getKey() == reqId) {
                return wsReqReplyIds.remove(methodReqId.getKey());
            }
        }
        return null;
    }

    // 本机用户 摄像头
    private ParticipantInfoModel localParticipant;
    //本级用户 共享屏幕
    private ParticipantInfoModel localScreenParticipant;
    //本机用户 共享HDMI IN
    private ParticipantInfoModel localHdmiParticipant;
    //房间内 已有 与会者
    private LinkedHashMap<String, ParticipantInfoModel> remoteParticipants; // key 为connectId
    private ConcurrentHashMap<String, ParticipantInfoModel> mixFlows; // key 为connectId  混流地址集合
    private ConcurrentHashMap<Integer, String> pubAndSubParticipantIds; //reqId 对应 connectId
    private LooperExecutor executor;
    private HashMap<String, String> queueMaps = new LinkedHashMap<>();
    private List<PartLinkedListBean> listBeans = new CopyOnWriteArrayList<>();
    //<editor-fold desc="overrride BaseWebSocket - WebSocketListener">

    /**
     * 开始连接
     *
     * @param websocket
     * @param newState
     * @throws Exception
     */
    @Override
    public void onStateChanged(WebSocket websocket, WebSocketState newState) throws Exception {
        super.onStateChanged(websocket, newState);
        L.d("websocket onStateChanged: newState = " + newState + ", hasAccessedIn = " + hasAccessedIn);
        if (websocket != this.webSocket) {
            Log.w(L.TAG, "websocket is not current one, do nothing! websocket = " + websocket + ", this.webSocket = " + this.webSocket);
            return;
        }
        if (newState == WebSocketState.CLOSED && hasAccessedIn) {
            L.d("websocket onStateChanged: newState = " + newState);
        }
        if (webSocketStateListener != null) {
            webSocketStateListener.onStateChangeListener(newState);
        }
        if (newState == WebSocketState.CLOSED) {
            this.close();
            this.reconnCount++;
            float delayMs = this.reconnCount < reconnInternals.length ? reconnInternals[this.reconnCount] * 1000 : reconnInternals[reconnInternals.length - 1] * 1000;
            L.d("onStateChanged: reconnect webSocket after " + (long) delayMs + " ms.");
            if (reconnCount >= 2) {
                if (webSocketStateListener != null) {
                    webSocketStateListener.onSocketConnectedFail();
                }
            }
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (hasAccessedIn) {
                        L.d("reconnect webSocket now, reconnCount = " + RoomClient.this.reconnCount + ", delay " + (long) delayMs + " ms.");
                        connectWebSocket(true);
                    }
                }
            }, (long) delayMs);
        }
    }

    //websocket 连接成功回调
    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        L.d("websocket onConnected ws callback");
//        roomEventCallback.onConnectSuccess();
//        pingMessageHandler(websocket);
        //客户端接入登录到云平台保持在线状态
        accessIn(isForceLogin);
        ping();
        reconnCount = 0;
    }

    @Override
    public void onConnectError(WebSocket websocket, WebSocketException cause) throws Exception {
        L.d("onConnectError: cause" + cause.toString());
        roomEventCallback.onConnectError(cause.toString());
    }


    @Override
    public void onTextMessage(final WebSocket websocket, String text) throws Exception {
        if (text.contains("ping")) {
            L.d("ping: %1$s", text);
        } else {
            L.d("onTextWebSocket: %1$s", text);
        }
        JsonObject json = GsonProvider.provide().fromJson(text, JsonObject.class);
        if (json.has("jsonrpc")) {

            if (json.has("params")) {//params 云平台推送消息,最外层 无id字段(推送的消息,与本地 非成对存在)
                // : 2019/9/23 handleSuccess
                manager.sendMessage(text);
                handleMethod(webSocket, new JSONObject(text), json);
                return;
            }
            int receivedMsgId = 0;
            if (json.has("id")) {
                receivedMsgId = json.get("id").getAsInt();
            }
            //收到重复 消息
            if (getLastReceivedWsMsgId() == receivedMsgId) {
                L.d("onTextMessage: Repeat received id: %1$d", receivedMsgId);
                return;
            }
            setLastReceivedWsMsgId(receivedMsgId);
            String method = getMethodNameById(receivedMsgId);
            if (!json.has("params")) {
                manager.sendIdToMessage(receivedMsgId, method);
                manager.sendMessage(text);
            }
            //根据reqId 没有查到 方法名,说明此ws接口,从未被 本客户端 发起过
            if (TextUtils.isEmpty(method)) {
                L.d("handleTextMessageByMethod methodName is null," +
                        "this method is never request by client");
                return;
            }
            if (json.has("error")) {
                // : 2019/9/23 handleFailed
                handleFailed(method, json.getAsJsonObject("error"));
            } else if (json.has("result")) {

                // : 2019/9/23 handleSuccess
                handleSuccess(method, json.getAsJsonObject("result"), receivedMsgId);
            } else {
                L.d("onTextMessage unkonow msg %1$s", text);
                closeWebSocket();
            }
        }
    }

    @Override
    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
        L.d("onDisconnected: closedByServer = " + closedByServer);
    }


    public void setRoomRTCEvent(RoomRTCEvent roomRTCEvent) {
        this.roomRTCEvent = roomRTCEvent;
    }

    /**
     * 监听添加
     *
     * @param clazz
     * @param roomEventCallback
     */
    public void addRoomEventCallback(Class clazz, RoomEventCallback roomEventCallback) {
        this.roomEventCallback.addRoomEventCallback(clazz, roomEventCallback);
    }

    /**
     * 移除监听,防止退出页面后内存泄漏
     *
     * @param clazz key
     */
    public void removeRoomEventCallback(Class clazz) {
        this.roomEventCallback.removeRoomEventCallback(clazz);
    }

    public void setRemoteParticipants(LinkedHashMap<String, ParticipantInfoModel> remoteParticipants) {
        this.remoteParticipants = remoteParticipants;
    }

    //<editor-fold desc="sendJson to Server by WebSocket">


//    /**
//     * 暂时未调用
//     */
//    public void sendUnPublishVideo() {
//        JsonObject request = UserCMD.sendUnPublishVideo(getAtomReqId());
//        localParticipant.setPublished(false);
//        sendJson(request.toString());
//    }
//
//    /**
//     * 暂时未调用
//     */
//    public void sendUnsubscribeFromVideo(String senderId) {
//        JsonObject request = UserCMD.sendUnsubscribeFromVideo(senderId, getAtomReqId());
//        sendJson(request.toString());
//    }

    /**
     * @param participantId
     * @param localIceCandidate
     */
    public void sendOnIceCandidate(String participantId, IceCandidate localIceCandidate, String streamType) {
        Map<String, Object> params = UserCMD.sendOnIceCandidate(participantId, localIceCandidate, streamType);
        sendJson(WssMethodNames.onIceCandidate, params);
    }

    private void sendJson(String request) {
        if (!request.contains("ping")) {
            Log.i(TAG, request);
        }
        if (webSocket != null) {
            webSocket.sendText(request);
        }
    }

    //<editor-fold desc="旧的 WebSocket 接口">


    // ------------------------- 上面 old  下面 new -------------------------
    private static final String JSON_RPC = "2.0";

    /**
     * 通过 WebSocket 发送数据
     *
     * @param method
     * @param params
     */
    private void sendJson(String method, @Nullable Map<String, Object> params) {
        Map<String, Object> up = new HashMap<>();
        up.put("jsonrpc", JSON_RPC);
        up.put("id", getAtomReqId());
        up.put("method", method);
        if (null != params)//ping时候 用
            up.put("params", params);

        saveReqIdByMethod((Integer) up.get("id"), method);
        //自增 reqId
        addAtomReqId();
        String json = GsonProvider.provide().toJson(up);
        if (!json.contains("ping")) {
            L.d("sendWebSocket -> %1$s", json);
        } else {
            L.d("ping -> %1$s", json);
        }
        if (webSocket != null) {
            webSocket.sendText(json);
        }
    }

    /**
     * 5.2 接入登录
     * 客户端接入登录到云平台保持在线状态
     */
    public void accessIn(boolean forceLogin) {
        Map<String, Object> params = UserCMD.accessIn(forceLogin);
        sendJson(WssMethodNames.accessIn, params);
    }

    /**
     * 5.3 接入登出
     * 客户端登出云平台处于离线状态
     */
    public void accessOut() {
        if (null != webSocket && webSocket.isOpen()) {
            L.d("webSocket is open try accessOut");
            Map<String, Object> params = new HashMap<>();
            sendJson(WssMethodNames.accessOut, params);
        } else {
            L.d("webSocket is not open, you may have called close() directly callback onAccessOut()");
            roomEventCallback.onAccessOut();
        }
    }

    /**
     * 5.4 发起会议
     * 客户端发起发起会议
     */
    public void createRoom(String roomId, String password) {
//        this.roomCreatedCallback = roomCreatedCallback;
        L.d("do this ----roomId>>> " + roomId);
        Map<String, Object> params = UserCMD.createRoom(roomId, password);
        sendJson(WssMethodNames.createRoom, params);
    }

    /**
     * 5.5 加入会议
     * 5.5.1  请求加入到会议室 (参考原生信令)
     * 客户端发起请求加入到会议室
     */
    public void joinRoom(String roomId, String password, SudiRole role, int localStreamType, String streamType, String joinType) {
        String userId = SPEditor.instance().getUserId();
        setRoomId(roomId);
        Map<String, Object> params = UserCMD.sendJoinRoom(roomId, password, role, localStreamType, streamType, joinType);// 主流，分享流
        if (localStreamType == 2) {
            this.localHdmiParticipant = new ParticipantInfoModel();
            localHdmiParticipant.setUserId(userId);
            localHdmiParticipant.setLocalStreamType(2);
            localHdmiParticipant.setStreamType(streamType);
        } else if (localStreamType == 1) {
            this.localScreenParticipant = new ParticipantInfoModel();
            localScreenParticipant.setUserId(userId);
            localScreenParticipant.setLocalStreamType(1);
            localScreenParticipant.setStreamType(streamType);
        } else {
            this.localParticipant = new ParticipantInfoModel();
            localParticipant.setUserId(userId);
            localParticipant.setStreamType(streamType);
        }

        sendJson(WssMethodNames.joinRoom, params);
    }

    /**
     *
     */
    public void getRoomLayout(String roomId) {
        Map<String, Object> params = UserCMD.getRoomLayout(roomId);// 主流，分享流
        sendJson(WssMethodNames.getRoomLayout, params);
    }

    /**
     *
     */
    public void getBroadcastMajorLayout(int mode, ArrayList<String> strings, String sendType) {
        Map<String, Object> params = UserCMD.getBroadcastMajorLayout(mode, strings, sendType);// 主流，分享流
        sendJson(WssMethodNames.broadcastMajorLayout, params);
    }

    /**
     * 5.5.2  新与会人请求推送音视频 (参考原生信令)
     * 新与会人客户端发起请求推送自己音视频到云平台
     */
    public void publishVideo(String sdpOffer, int localStreamType) {
        /**
         * 本地采集视频 和 屏幕共享区别
         */
        //设置 权限功能去除 localParticipant.getMicStatusInRoom().equals(Constract.VALUE_STATUS_ON) &&
        if (localStreamType == 2) {
            pubAndSubParticipantIds.put(getAtomReqId(), localHdmiParticipant.getConnectionId());
            if (remoteParticipants.get(localHdmiParticipant.getConnectionId()) != null) {
                remoteParticipants.get(localHdmiParticipant.getConnectionId()).setPublished(true);
            }
        } else if (localStreamType == 1) {
            pubAndSubParticipantIds.put(getAtomReqId(), localScreenParticipant.getConnectionId());
            if (remoteParticipants.get(localScreenParticipant.getConnectionId()) != null) {
                remoteParticipants.get(localScreenParticipant.getConnectionId()).setPublished(true);
            }
        } else {
            pubAndSubParticipantIds.put(getAtomReqId(), localParticipant.getConnectionId());
            if (remoteParticipants.get(localParticipant.getConnectionId()) != null) {
                remoteParticipants.get(localParticipant.getConnectionId()).setPublished(true);
            }
        }
        Map<String, Object> params = UserCMD.sendPublishVideo(sdpOffer, localStreamType, localParticipant);
        sendJson(WssMethodNames.publishVideo, params);
    }

    /**
     * 5.5.3  新与会人请求接收音视频 (参考原生信令)
     * 5.5.6  其他与会人请求接收新与会人音视频 (参考原生信令)
     * 新与会人客户端发起请求接收其他参与方音视频（会议中有2人及2人以上时调用）
     * sdp 由上层webRTC回调
     */
    public void receiveVideoFrom(String streamMode, String senderId, String sdpOffer) {
        Log.i("addNewOtherView", "receiveVideoFrom" + senderId);
        Map<String, Object> params = UserCMD.sendReceiveVideoFrom(streamMode, senderId, sdpOffer);
        pubAndSubParticipantIds.put(getAtomReqId(), senderId);
        sendJson(WssMethodNames.receiveVideoFrom, params);
    }

    /**
     * 5.5.3  新与会人请求接收音视频 (参考原生信令)
     * 5.5.6  其他与会人请求接收新与会人音视频 (参考原生信令)
     * 新与会人客户端发起请求接收其他参与方音视频（会议中有2人及2人以上时调用）
     * sdp 由上层webRTC回调
     */
    public void unsubscribeFromVideo(String senderId, String streamType) {
        Log.i("addNewView", "unsubscribeFromVideo" + senderId);
        Map<String, Object> params = UserCMD.sendUnsubscribeFromVideo(senderId, streamType);
        sendJson(WssMethodNames.unsubscribeFromVideo, params);
    }

    /**
     * 5.6 离开会议
     * 5.6.1  与会人离开会议 (参考原生信令)
     */
    public void leaveRoom(String sourceId, String roomId, String streamType) {
        Map<String, Object> params = UserCMD.leaveRoom(roomId, sourceId, streamType);
        sendJson(WssMethodNames.leaveRoom, params);
    }

    /**
     * 5.18  踢出与会者
     */
    public void forceUnpublish(String streamId) {
        Map<String, Object> params = UserCMD.forceUnpublish(streamId);
        sendJson(WssMethodNames.forceUnpublish, params);
    }

    /**
     * 5.18  踢出与会者
     */
    public void forceDisconnect(String connectionId) {
        Map<String, Object> params = UserCMD.forceDisconnect(connectionId);
        sendJson(WssMethodNames.forceDisconnect, params);
    }

    /**
     * 5.19  结束会议
     * 5.19.1 请求结束会议
     */
    public void closeRoom(String roomId) {
        Map<String, Object> params = UserCMD.closeRoom(roomId);
        sendJson(WssMethodNames.closeRoom, params);
    }

    /**
     * 5.10 获取参会人员列表
     * 客户端获取与会人员列表
     */
    public void getParticipants(String roomId) {
        Map<String, Object> params = VideoCMD.getAllParticipants(roomId);
        sendJson(WssMethodNames.getParticipants, params);
    }

    /**
     * 5.11 设置音频状态
     * 客户端设置音频状态同步接口，
     * 参会人员只能控制自己的音频状态，
     * 主持人有权限控制所有人的音频状态。
     */
    public void setAudioStatus(String roomId, String sourceId, List<String> targetId, boolean status) {
        Map<String, Object> params = VideoCMD.setAudioStatus(roomId, sourceId, targetId, status);
        sendJson(WssMethodNames.setAudioStatus, params);
    }

    public void setAudioStatus(String roomId, String sourceId, String targetId, boolean status) {
        List<String> userId = new ArrayList<>();
        userId.add(targetId);
        setAudioStatus(roomId, sourceId, userId, status);
    }

    /**
     * 5.12 设置视频状态
     * 客户端设置视频状态接口，
     * 参会人员只能控制自己的视频状态，
     * 主持人有权限控制所有人的视频状态。
     */
    public void setVideoStatus(String roomId, String sourceId, List<String> targetId, boolean status) {
        Map<String, Object> params = VideoCMD.setVideoStatus(roomId, sourceId, targetId, status);
        sendJson(WssMethodNames.setVideoStatus, params);
    }

    /**
     * 5.12 设置扬声器状态
     * 客户端设置视频状态接口，
     * 参会人员只能控制自己的视频状态，
     * 主持人有权限控制所有人的视频状态。
     */
    public void setAudioSpeakerStatus(String roomId, String sourceId, String targetId, boolean status) {
        Map<String, Object> params = VideoCMD.setAudioSpeakerStatus(roomId, sourceId, targetId, status);
        sendJson(WssMethodNames.setAudioSpeakerStatus, params);
    }

    /**
     * 5.13 举手
     * 客户端举手接口
     *
     * @param roomId
     * @param sourceId
     */
    public void raiseHand(String roomId, String sourceId) {
        Map<String, Object> params = VideoCMD.raiseHand(roomId, sourceId);
        sendJson(WssMethodNames.raiseHand, params);
    }

    /**
     * 5.14 放下手
     * 客户端放下手接口，参会人员只能放下自己的手，主持人有权限控制所有人放下手。
     *
     * @param roomId
     * @param sourceId
     */
    public void putDownHand(String roomId, String sourceId, String targetId) {
        Map<String, Object> params = VideoCMD.putDownHand(roomId, sourceId, targetId);
        sendJson(WssMethodNames.putDownHand, params);
    }

    /**
     * 5.15 会议锁定
     * 会议锁定功能接口，只有主持人有会议锁定的权限。
     *
     * @param roomId
     * @param sourceId
     */
    public void lockSession(String roomId, String sourceId) {
        Map<String, Object> params = VideoCMD.lockSession(roomId, sourceId);
        sendJson(WssMethodNames.lockSession, params);
    }

    /**
     * 5.17 会议锁定
     * 会议锁定功能接口，只有主持人有会议锁定的权限。
     *
     * @param roomId
     * @param sourceId
     */
    public void unlockSession(String roomId, String sourceId) {
        Map<String, Object> params = VideoCMD.unlockSession(roomId, sourceId);
        sendJson(WssMethodNames.unlockSession, params);
    }

    /**
     * 5.16 在线状态保持（参考原始信令）
     * 在线状态保持。
     */
    public static final int heartbeat_interval = 5;

    /**
     * 心跳包 维持链接
     */
    public void ping() {
        if (poolExecutor == null) {
            pingFailedCount = 0;
            poolExecutor = new ScheduledThreadPoolExecutor(1);
            poolExecutor.scheduleWithFixedDelay(() -> {
                if (pingFailedCount >= 3) {
                    L.d("ping failed count = " + pingFailedCount + ", close websocket.");
                    close();
                    return;
                }
                pingFailedCount++;
                sendJson(WssMethodNames.ping, null);
            }, heartbeat_interval, heartbeat_interval, TimeUnit.SECONDS); //原delay 60
        }
    }

    /**
     * 主持人邀请其他参会者
     */
    public void inviteParticipant(String roomId, String sourceId, List<String> targetId) {
        Map<String, Object> params = VideoCMD.inviteParticipant(roomId, sourceId, targetId);
        sendJson(WssMethodNames.inviteParticipant, params);
    }

    public void refuseInvite(String roomId, String sourceId) {
        Map<String, Object> params = VideoCMD.refuseInvite(roomId, sourceId);
        sendJson(WssMethodNames.refuseInvite, params);
    }

    /**
     * 5.25 主持人权限转让
     */
    public void transferModerator(String roomId, String sourceId, String targetId) {
        Map<String, Object> params = VideoCMD.transferModerator(roomId, sourceId, targetId);
        sendJson(WssMethodNames.transferModerator, params);
    }

    /**
     * 5.24 主持人 设置共享权限
     * targetId:
     */
    public void setSharePower(String roomId, String sourceId, String targetId) {
        Map<String, Object> params = VideoCMD.setSharePower(roomId, sourceId, targetId);
        sendJson(WssMethodNames.sharingControl, params);
    }


    /**
     * 5.26 设置会议预设置信息(预留)
     */
    public void setPresetInfo(String roomId, String subject, String micStatusInRoom, String sharePowerInRoom, String videoStatusInRoom) {
        Map<String, Object> params = VideoCMD.setPresetInfo(roomId, subject, micStatusInRoom, sharePowerInRoom, videoStatusInRoom);
        sendJson(WssMethodNames.setPresetInfo, params);
    }

    /**
     * 5.27 获取会议预设置信息
     */
    public void getPresetInfo(String roomId) {
        Map<String, Object> params = VideoCMD.getPresetInfo(roomId);
        sendJson(WssMethodNames.getPresetInfo, params);
    }

    /**
     * 5.30 点名发言
     */
    public void rollCall(String roomId, String sourceId, String targetId) {
        Map<String, Object> params = VideoCMD.rollcall(roomId, sourceId, targetId);
        sendJson(WssMethodNames.rollCall, params);
    }

    //替换发言
    public void replaceRollCall(String roomId, String sourceId, String endTargetId, String startTargetId) {
        Map<String, Object> params = VideoCMD.replaceRollCall(roomId, sourceId, endTargetId, startTargetId);
        sendJson(WssMethodNames.replaceRollCall, params);
    }

    /**
     * 5.30 结束点名发言
     */
    public void endRollCall(String roomId, String sourceId, String targetId) {
        Map<String, Object> params = VideoCMD.endRollcall(roomId, sourceId, targetId);
        sendJson(WssMethodNames.endRollCall, params);
    }

    /**
     * 5.28 获取组织树列表
     */
    public void getOrgList() {
        Map<String, Object> params = VideoCMD.getOrgList();
        sendJson(WssMethodNames.getOrgList, params);
    }

    /**
     * 5.28 根据组织树获取账号(设备)列表
     */
    public void getUserDeviceList(String orgId) {
        Map<String, Object> params = VideoCMD.getUserDeviceList(orgId);
        sendJson(WssMethodNames.getUserDeviceList, params);
    }

    /**
     * 5.36 会议延长
     */
    public void roomDelay(String roomId) {
        Map<String, Object> params = VideoCMD.getRoomId(roomId);
        sendJson(WssMethodNames.roomDelay, params);
    }

    /**
     * 获取本人参加且尚未结束的会议
     */
    public void getNotFinishedRoom() {
        sendJson(WssMethodNames.getNotFinishedRoom, new HashMap<>());
    }

    /**
     * 主持人设置 窗口布局
     */
    public void setConferenceLayout(int mode, boolean automatically) {
        Map<String, Object> params = new HashMap<>();
        params.put("mode", mode);
        params.put("automatically", automatically);
        sendJson(WssMethodNames.setConferenceLayout, params);
    }

//    /**
//     * 处理其他与会者登录请求
//     */
//    public void confirmApplyForLogin(boolean accept, String applicantSessionId) {
//        Map<String, Object> map = UserCMD.confirmApplyForLogin(accept, applicantSessionId);
//        sendJson(WssMethodNames.confirmApplyForLogin, map);
//    }

    /**
     * 获取部门所有设备
     */
    public void getSubDevOrUser(String orgId) {
        Map<String, Object> params = new HashMap<>();
        params.put("orgId", orgId);
        sendJson(WssMethodNames.getSubDevOrUser, params);
    }

    /**
     * 云台控制
     *
     * @param connectionId 序列号
     * @param operateCode  操作码
     * @param maxDuration  持续时长
     */
    public void startPtzControl(String connectionId, int operateCode, long maxDuration) {
        Map<String, Object> params = new HashMap<>();
        params.put("connectionId", connectionId);
        params.put("operateCode", operateCode);
        params.put("maxDuration", maxDuration);
        sendJson(WssMethodNames.startPtzControl, params);
    }


    public void stopPtzControl(String serialNumber) {
        Map<String, Object> params = new HashMap<>();
        params.put("connectionId", serialNumber);
        sendJson(WssMethodNames.stopPtzControl, params);
    }


    /**
     * 获取企业子部门及所有设备
     */
    public void getDepartmentTree() {
        sendJson(WssMethodNames.getDepartmentTree, new HashMap<>());
    }

    /**
     * 获取群组
     */
    public void getGroupList() {
        Map<String,Object> params = new HashMap<>();
        params.put("userId",Long.parseLong(SPEditor.instance().getUserId()));
        sendJson(WssMethodNames.getGroupList, params);
    }

    /**
     * 获取群组信息
     */
    public void getGroupInfo(long groupId) {
        Map<String,Object> params = new HashMap<>();
        params.put("groupId",groupId);
        sendJson(WssMethodNames.getGroupInfo,params);
    }

    private String roomId;

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    //<editor-fold desc="handler text whitch form onTextMsg callback">
    private void handleFailed(String methodName, JsonElement error) {
        //
        WsError wsError = GsonProvider.provide().fromJson(error, WsError.class);
        if (accessIn.equals(methodName) && !hasAccessedIn && currentActivity != null && currentActivity.get() instanceof LoginActivity) {
            if (loginStateListener != null) {
                App.post(() -> loginStateListener.onLoginFailed());
            }
        }
        if (roomEventCallback != null) {
            roomEventCallback.onWebSocketFailMessage(wsError, methodName);
        }

    }

    /**
     * 断线重连标志位，webSocket
     */
    public void markAccessed() {
        this.hasAccessedIn = true;
    }

    public void invalidAccessedFlag() {
        this.hasAccessedIn = false;
    }

    public void JoinRoomParticipant(ParticipantInfoModel participant, JoinRoomResp joinRoomResp) {
        participant.setMicStatusInRoom(joinRoomResp.getMicStatusInRoom());
        participant.setVideoStatusInRoom(joinRoomResp.getVideoStatusInRoom());
        participant.setSharePowerInRoom(joinRoomResp.getSharePowerInRoom());
        participant.setSubject(joinRoomResp.getSubject());
        participant.setAppShowName(joinRoomResp.getAppShowName());
        participant.setAppShowDesc(joinRoomResp.getAppShowDesc());
        participant.setSpeakerActive(true);
        participant.setStreamType(joinRoomResp.getStreamType());
        if (RoomManager.get().isNativeAudio()) {
            participant.setAudioActive(true);
        } else {
            participant.setAudioActive(false);
        }
        //设置 权限功能去除 localParticipant.getVideoStatusInRoom().equals(Constract.VALUE_STATUS_ON) &&
        if (RoomManager.get().isNativeVideo()) {
            participant.setVideoActive(true);
        } else {
            participant.setVideoActive(false);
        }

    }

    private void handleSuccess(String methodName, JsonObject result, int requestId) {

        if (WssMethodNames.ping.equals(methodName)) {//心跳返回数据 处理
            L.d("handleSuccess heartbeat ping-pong  result -> %1$s", result);
            pingFailedCount--;
            return;
        }
        String sessionId = result.get("sessionId").getAsString();
        //保存 sessionId 到 sp
        SPEditor.instance().saveSessionId(sessionId);
        if (accessIn.equals(methodName)) {
            SPEditor.instance().setDeviceName(result.get("deviceName") == null ? "" : result.get("deviceName").getAsString());
            if (!hasAccessedIn) {
                if (currentActivity != null && currentActivity.get() instanceof GroupActivity) {
//                    Intent intent = new Intent(context, GroupActivity.class);
//                    context.startActivity(intent);
                    RoomClient.get().setForceLogin(false);
                    markAccessed();
                    if (loginStateListener != null) {
                        App.post(() -> loginStateListener.onLoginSuccess());
                    }
                }
            } else {
                if (webSocketStateListener != null) {
                    webSocketStateListener.onAccessInSuccessCallback();
                }
            }
            isAccessInSuccess = true;
        } else if (WssMethodNames.accessOut.equals(methodName)) {
            roomEventCallback.onAccessOut();
            setForceLogin(false);
            invalidAccessedFlag();
        } else if (WssMethodNames.createRoom.equals(methodName)) {
            CreatRoomResp roomInfo = GsonProvider.provide().fromJson(result, CreatRoomResp.class);
            setRoomId(roomInfo.getRoomId());
            roomEventCallback.onRoomCreated(roomInfo.getRoomId(), roomInfo.getSessionId());
        } else if (WssMethodNames.getRoomLayout.equals(methodName)) {
            RoomLayoutModel roomInfo = GsonProvider.provide().fromJson(result, RoomLayoutModel.class);
            L.d("do this ----------------getRoomLayout " + roomInfo.getLayout());
            RoomManager.get().setRoomLayoutModel(roomInfo);
            roomEventCallback.getRoomLayoutCallback(roomInfo);
        } else if (WssMethodNames.joinRoom.equals(methodName)) {
            JoinRoomResp joinRoomResp = GsonProvider.provide().fromJson(result, JoinRoomResp.class);
            JsonObject metaData = GsonProvider.provide().fromJson(joinRoomResp.getMetadata(), JsonObject.class);
            String joinerId = metaData.get("clientData").getAsString();
            String role = metaData.get("role").getAsString();
            RoomManager.get().setConferenceMode(joinRoomResp.getConferenceMode());
            RoomManager.get().setSubject(joinRoomResp.getSubject());
            RoomManager.get().setRoomCapacity(joinRoomResp.getRoomCapacity());
            RoomManager.get().setRoomAllowShare("on".equals(joinRoomResp.getAllowPartOperShare()));
            RoomManager.get().setRoomAllowMic("on".equals(joinRoomResp.getAllowPartOperMic()));
            RoomManager.get().setCreateAt(joinRoomResp.getRoomCreateAt());
            if (null != localHdmiParticipant && TextUtils.equals(localHdmiParticipant.getUserId(), joinerId) && !TextUtils.equals(role, ROLE_PUBLISHER_SUBSCRIBER)) {
                localHdmiParticipant.setConnectionId(joinRoomResp.getId());
                localHdmiParticipant.setRole(role);
                JoinRoomParticipant(localHdmiParticipant, joinRoomResp);
                localParticipant.setShareStatus("on");
                remoteParticipants.put(joinRoomResp.getId(), localHdmiParticipant);
                roomRTCEvent.onShareJoinRoomSuccess(localHdmiParticipant);
                queueMaps.put(joinerId + LOCAL_SCREEN, joinRoomResp.getId());
                if (onLocalShareJoinListener != null) {
                    onLocalShareJoinListener.OnLocalShareJoin(localHdmiParticipant);
                }
            } else if (null != localScreenParticipant && TextUtils.equals(localScreenParticipant.getUserId(), joinerId) && !TextUtils.equals(role, ROLE_PUBLISHER_SUBSCRIBER)) {
                localScreenParticipant.setConnectionId(joinRoomResp.getId());
                localScreenParticipant.setRole(role);
                JoinRoomParticipant(localScreenParticipant, joinRoomResp);
                roomRTCEvent.onShareJoinRoomSuccess(localScreenParticipant);
            } else {
                localParticipant.setConnectionId(joinRoomResp.getId());
                localParticipant.setRole(role);
                JoinRoomParticipant(localParticipant, joinRoomResp);
                if (role.equals(ROLE_PUBLISHER_SUBSCRIBER)) {
                    RoomManager.get().setHostId(joinerId);
                }
                RoomManager.get().setConnectionId(joinRoomResp.getId());
                remoteParticipants.put(joinRoomResp.getId(), localParticipant);
                queueMaps.put(joinerId, joinRoomResp.getId());
                L.d("do this --->>>>>WssMethodNames.joinRoom ： " + joinRoomResp.getValue().size());
                if (joinRoomResp.getValue().size() > 0) {//房间内 已有与会者
                    parseParticipantsInRooms(joinRoomResp);
                } else {
                    setHasHands(false);
                }

                if (joinRoomResp.getLayoutInfo() != null) {//房间内 已有与会者
                    parseLayoutInfo(joinRoomResp);
                }
                roomEventCallback.onJoinRoomSuccess(localParticipant);
                RoomManager.get().setNativeAudio("on".equals(DeviceSettingManager.getInstance().getFromSp().getIsSettingMicroOn()));
                RoomManager.get().setNativeVideo("on".equals(DeviceSettingManager.getInstance().getFromSp().getIsSettingCameraOn()));
            }
        } else if (WssMethodNames.publishVideo.equals(methodName)) {
            PublishVideoResp pushVideoResp = GsonProvider.provide().fromJson(result, PublishVideoResp.class);
            //handle sdpAnswer id sessionId
            handleSdpAnswer(requestId, pushVideoResp.getSdpAnswer());
        } else if (WssMethodNames.receiveVideoFrom.equals(methodName)) {
            ReceiveVideoResp receiveVideoResp = GsonProvider.provide().fromJson(result, ReceiveVideoResp.class);
            //handle sdpOffer sender
            handleSdpAnswer(requestId, receiveVideoResp.getSdpAnswer());
        } else if (WssMethodNames.leaveRoom.equals(methodName)) {
            LeaveRoomModel leaveRoomModel = GsonProvider.provide().fromJson(result, LeaveRoomModel.class);
            if (localScreenParticipant != null && localScreenParticipant.getConnectionId().equals(leaveRoomModel.getConnectionId())) {
                roomEventCallback.onLeaveRoomSuccess(SHARING);
            }
            if (localHdmiParticipant != null && localHdmiParticipant.getConnectionId().equals(leaveRoomModel.getConnectionId())) {
                roomEventCallback.onLeaveRoomSuccess(SHARING);
            }

        } else if (WssMethodNames.getParticipants.equals(methodName)) {
            //获取 房间内 所有 与会者 - 回调
            //参见: object.has(CLRtcSinaling.PARTICIPANTS)
            GetParticipantsResp getParticipantsResp = GsonProvider.provide().fromJson(result, GetParticipantsResp.class);
            // 把获取到的 与会人员列表 回调出去 通过 RoomEventCallback
            roomEventCallback.onGetParticipantsResult(getParticipantsResp.getParticipantList());

        } else if (WssMethodNames.setAudioStatus.equals(methodName)) {
            //handle sessionId
            roomEventCallback.onAudioStatusSwitchSuccess();
        } else if (WssMethodNames.setVideoStatus.equals(methodName)) {
            //handle sessionId
            roomEventCallback.onVideoStatusSwitchSuccess();
        } else if (WssMethodNames.raiseHand.equals(methodName)) {
            //handle sessionId
            roomEventCallback.onRaiseHandsSended();
        } else if (WssMethodNames.putDownHand.equals(methodName)) {
            //handle sessionId
            roomEventCallback.onPutDownHandsSended();
        } else if (WssMethodNames.forceUnpublish.equals(methodName)) {
            //handle sessionId
//            ParticipantUnPublishedModel pushVideoResp = GsonProvider.provide().fromJson(result, ParticipantUnPublishedModel.class);
//           forceDisconnect(pushVideoResp.getConnectionId());
        } else if (WssMethodNames.getPresetInfo.equals(methodName)) {
            PresetInfoResp presetInfoResp = GsonProvider.provide().fromJson(result, PresetInfoResp.class);
            roomEventCallback.onGetPresetInfo(presetInfoResp);
        } else if (WssMethodNames.rollCall.equals(methodName)) {        //点名发言

        } else if (WssMethodNames.endRollCall.equals(methodName)) {
            //结束点名发言

        } else if (WssMethodNames.getOrgList.equals(methodName)) {     //结束点名发言
            OrgList orgList = GsonProvider.provide().fromJson(result, OrgList.class);
            roomEventCallback.getOrgList(orgList);
        } else if (WssMethodNames.getUserDeviceList.equals(methodName)) {     //结束点名发言
            UserDeviceList userDeviceList = GsonProvider.provide().fromJson(result, UserDeviceList.class);
            roomEventCallback.getUserDeviceList(userDeviceList);
        } else if (WssMethodNames.roomDelay.equals(methodName)) {       //申请延长会议时间
            roomEventCallback.onRoomDelay();
        } else if (WssMethodNames.getNotFinishedRoom.equals(methodName)) {                                                  //获取本人参加且尚未结束的会议
            ReJoinModel reJoinModel = GsonProvider.provide().fromJson(result, ReJoinModel.class);
            roomEventCallback.getNotFinishedRoom(reJoinModel);
        }
//        else if (WssMethodNames.confirmApplyForLogin.equals(methodName)) {                        //单点登录 操作确认
//            SingleLoginModel singleLoginModel = GsonProvider.provide().fromJson(result, SingleLoginModel.class);
//            roomEventCallback.confirmApplyForLogin(singleLoginModel);
//        }
        else if (WssMethodNames.refuseInvite.equals(methodName)) {
            SlingModel slingModel = GsonProvider.provide().fromJson(result, SlingModel.class);
            roomEventCallback.refuseInvite(slingModel);
        } else if (WssMethodNames.inviteParticipant.equals(methodName)) {
            roomEventCallback.onInviteJoinCallback();
        } else if (WssMethodNames.getSubDevOrUser.equals(methodName)) {
            PartDeviceModel orgList = GsonProvider.provide().fromJson(result, PartDeviceModel.class);
            roomEventCallback.getSubDevOrUser(orgList);
        } else if (WssMethodNames.getDepartmentTree.equals(methodName)) {
            CompanyModel companyModel = GsonProvider.provide().fromJson(result, CompanyModel.class);
            roomEventCallback.getDepartmentTree(companyModel);
        } else if (WssMethodNames.startPtzControl.equals(methodName)) {
            roomEventCallback.startPtzControl();
        } else if (WssMethodNames.stopPtzControl.equals(methodName)) {
            roomEventCallback.stopPtzControl();
        } else if (WssMethodNames.getGroupList.equals(methodName)){
            GroupListInfoModel groupListInfoModel = GsonProvider.provide().fromJson(result,GroupListInfoModel.class);
            roomEventCallback.getGroupList(groupListInfoModel);
        } else if (WssMethodNames.getGroupInfo.equals(methodName)){
            GroupInfoModel groupInfoModel = GsonProvider.provide().fromJson(result,GroupInfoModel.class);
            roomEventCallback.getGroupInfo(groupInfoModel);
        }
    }

    /**
     * 云平台推送相关
     *
     * @param webSocket
     * @param json
     * @param text
     * @throws JSONException
     */
    private void handleMethod(final WebSocket webSocket, JSONObject json, JsonObject text) throws
            JSONException {
        if (!json.has(Constants.PARAMS)) {
            Log.e(TAG, "No params");
        } else {
//            final JsonObject params = json.get(Constants.PARAMS).getAsJsonObject();
//            String method = json.get(Constants.METHOD).getAsString();


            final JSONObject params = new JSONObject(json.getString(Constants.PARAMS));
            String method = json.getString(Constants.METHOD);
            switch (method) {
                case WssMethodNames.setAudioStatus:
                    //audio
                    //{"method":"setAudioStatus","params":{"roomId":"abcdefgh","sourceId":"1234","targetId":"1234","status":"off"},"jsonrpc":"2.0"}
                    BaseParams<AudioStstusResp> audio = BaseParams.fromJson(text, AudioStstusResp.class);
                    AudioStstusResp audioStatus = audio.getParams();
                    roomEventCallback.onReceiveAudioStatusChange(audioStatus);
                    break;
                case WssMethodNames.setVideoStatus:
                    //video
                    //{"method":"setVideoStatus","params":{"roomId":"abcdefgh","sourceId":"1234","targetId":"1234","status":"off"},"jsonrpc":"2.0"}
                    BaseParams<VideoStatusResp> video = BaseParams.fromJson(text, VideoStatusResp.class);
                    VideoStatusResp videoStatus = video.getParams();
                    roomEventCallback.onReceiveVideoStatusChange(videoStatus);
                    break;
                case WssMethodNames.setAudioSpeakerStatus:
                    //video
                    //{"method":"setVideoStatus","params":{"roomId":"abcdefgh","sourceId":"1234","targetId":"1234","status":"off"},"jsonrpc":"2.0"}
                    BaseParams<SlingModel> speaker = BaseParams.fromJson(text, SlingModel.class);
                    SlingModel speakerStatus = speaker.getParams();
                    roomEventCallback.onReceiveSpeakerStatusChange(speakerStatus);
                    break;
                case WssMethodNames.putDownHand:
                    //hands
                    //{"method":"putDownHand","params":{"roomId":"abcdefgh","sourceId":"1234","targetId":"1234"},"jsonrpc":"2.0"}
                    BaseParams<HandsStatusResp> hands = BaseParams.fromJson(text, HandsStatusResp.class);
                    HandsStatusResp handsStatus = hands.getParams();
                    roomEventCallback.onReceiveHandsDown(handsStatus);
                    break;
                case WssMethodNames.raiseHand:
                    //hands
                    //{"method":"putDownHand","params":{"roomId":"abcdefgh","sourceId":"1234","targetId":"1234"},"jsonrpc":"2.0"}
                    HandsStatusResp handsStatus2 = (HandsStatusResp) BaseParams.fromJson(text, HandsStatusResp.class).getParams();
                    roomEventCallback.onReceiveHandsUp(handsStatus2);
                    break;
                case WssMethodNames.lockSession:
                    SlingModel lockSession = (SlingModel) BaseParams.fromJson(text, SlingModel.class).getParams();
                    roomEventCallback.onLockSession(lockSession);
                    break;
                case WssMethodNames.unlockSession:
                    SlingModel unlockSession = (SlingModel) BaseParams.fromJson(text, SlingModel.class).getParams();
                    roomEventCallback.unLockSession(unlockSession);
                    break;
                case Constants.ICE_CANDIDATE:
                    IceCandidateModel iceCandidateModel = (IceCandidateModel) BaseParams.fromJson(text, IceCandidateModel.class).getParams();
                    iceCandidateMethod(iceCandidateModel);
                    break;
                case Constants.PARTICIPANT_JOINED:
//                    ParticipantJoinedModel participantJoinedModel = (ParticipantJoinedModel)BaseParams.fromJson(text,ParticipantJoinedModel.class).getParams();
                    participantJoinedMethod(params);
                    break;
                case Constants.PARTICIPANT_PUBLISHED:
                    ParticipantPublishedModel participantPublishedModel = (ParticipantPublishedModel) BaseParams.fromJson(text, ParticipantPublishedModel.class).getParams();

                    participantPublishedMethod(participantPublishedModel);

                    break;
                case Constants.PARTICIPANT_UNPUBLISHED:
                    ParticipantUnPublishedModel participantUnPublishedModel = (ParticipantUnPublishedModel) BaseParams.fromJson(text, ParticipantUnPublishedModel.class).getParams();
                    participantUnPublishedMethod(participantUnPublishedModel);
                    break;
                case Constants.PARTICIPANT_LEFT:
                    ParticipantLeft participantLeft = (ParticipantLeft) BaseParams.fromJson(text, ParticipantLeft.class).getParams();
                    participantLeftMethod(participantLeft);
                    break;
                case Constants.participant_Evicted:
                    ParticipantEvicted participantEvicted = (ParticipantEvicted) BaseParams.fromJson(text, ParticipantEvicted.class).getParams();
                    participantEvictedMethod(participantEvicted);
                    break;
                case WssMethodNames.inviteParticipant:      //主持人邀请入会
                    InviteModel inviteModel = (InviteModel) BaseParams.fromJson(text, InviteModel.class).getParams();
                    roomEventCallback.onInviteJoinSuccess(inviteModel);
                    break;
                case WssMethodNames.transferModerator:      //主持人权限转让
                    SlingModel transModel = (SlingModel) BaseParams.fromJson(text, SlingModel.class).getParams();
                    roomEventCallback.onTransferModerator(transModel);
                    break;
                case WssMethodNames.stopPublishSharingNotify:          //主持人设置共享权限
                    SlingModel model = (SlingModel) BaseParams.fromJson(text, SlingModel.class).getParams();
                    roomEventCallback.onSharePower(model);
                    break;

                case WssMethodNames.rollCall:           //点名发言
                    SlingModel rollCall = (SlingModel) BaseParams.fromJson(text, SlingModel.class).getParams();
                    roomEventCallback.rollcall(rollCall);
                    break;

                case WssMethodNames.endRollCall:        //结束发言
                    SlingModel slingModel = (SlingModel) BaseParams.fromJson(text, SlingModel.class).getParams();
                    roomEventCallback.endRollcall(slingModel);
                    break;

                case WssMethodNames.roomCountDown:      //会议倒计时
                    RoomTimeModel roomTimeModel = (RoomTimeModel) BaseParams.fromJson(text, RoomTimeModel.class).getParams();
                    roomEventCallback.getRoomTimeCountDown(roomTimeModel);
                    break;

                case WssMethodNames.userBreakLine:
                    ParticipantInfoModel participantInfoModel = (ParticipantInfoModel) BaseParams.fromJson(text, ParticipantInfoModel.class).getParams();
//                    roomEventCallback.userBreakLine(participantInfoModel);
                    participantUserBreakMethod(participantInfoModel);
                    break;
                case WssMethodNames.reconnectPartStopPublishSharing:
                    ParticipantLeft infoModel = (ParticipantLeft) BaseParams.fromJson(text, ParticipantLeft.class).getParams();
                    reconnectPartStopPublishSharing(infoModel);
                    break;
//                case WssMethodNames.applyForLogin:
//                    SingleLoginModel singleLoginModel = (SingleLoginModel) BaseParams.fromJson(text, SingleLoginModel.class).getParams();
//                    roomEventCallback.applyForLogin(singleLoginModel);
//                    break;

//                case WssMethodNames.confirmApplyForLogin:
//                    SingleLoginModel singleLogin = (SingleLoginModel) BaseParams.fromJson(text, SingleLoginModel.class).getParams();
//                    roomEventCallback.confirmApplyForLogin(singleLogin);
//                    break;
//
//                case WssMethodNames.resultOfLoginApplyNotify:
//                    SingleLoginModel singleLoginModel1 = (SingleLoginModel) BaseParams.fromJson(text, SingleLoginModel.class).getParams();
//                    roomEventCallback.resultOfLoginApplyNotify(singleLoginModel1);
//                    break;

                case WssMethodNames.refuseInvite:
                    SlingModel slingModel1 = (SlingModel) BaseParams.fromJson(text, SlingModel.class).getParams();
                    roomEventCallback.refuseInvite(slingModel1);
                    break;

                case WssMethodNames.remoteLoginNotify:
                    roomEventCallback.remoteLoginNotify();
                    break;

                case WssMethodNames.replaceRollCall:
                    ReplaceRollcallModel replaceRollcallModel = (ReplaceRollcallModel) BaseParams.fromJson(text, ReplaceRollcallModel.class).getParams();
                    roomEventCallback.replaceRollCall(replaceRollcallModel);
                    break;
                case WssMethodNames.majorLayoutNotify:
                    RoomLayoutModel roomLayoutModel = (RoomLayoutModel) BaseParams.fromJson(text, RoomLayoutModel.class).getParams();
                    L.d("do this ----------------majorLayoutNotify " + roomLayoutModel.getLayout());
                    RoomManager.get().setRoomLayoutModel(roomLayoutModel);
                    roomEventCallback.majorLayoutNotify(roomLayoutModel);
                    break;
                case WssMethodNames.closeRoomNotify:

                    roomEventCallback.closeRoomNotify();
                    break;

                case WssMethodNames.conferenceLayoutChanged:        //窗口布局更改通知
                    ConferenceLayoutChangedResp conferenceLayoutChangedResp = (ConferenceLayoutChangedResp) BaseParams.fromJson(text, ConferenceLayoutChangedResp.class).getParams();
                    currentMode = conferenceLayoutChangedResp.getMode();
                    layoutChange(conferenceLayoutChangedResp.getPartLinkedList());
                    break;
                case WssMethodNames.upgradeNotify:
                    UpdateModel updateModel = (UpdateModel) BaseParams.fromJson(text, UpdateModel.class).getParams();
                    roomEventCallback.updateApp(updateModel);
                    break;
                case WssMethodNames.distributeShareCastPlayStrategyNotify:
                    StrategyModel strategyModel = (StrategyModel) BaseParams.fromJson(text, StrategyModel.class).getParams();
                    roomEventCallback.distributeShareCastPlayStrategyNotify(strategyModel);
                    break;
                case WssMethodNames.sharingControlNotify:
                    SlingModel slingModel2 = (SlingModel) BaseParams.fromJson(text, SlingModel.class).getParams();
                    roomEventCallback.sharingControlNotify(slingModel2);
                    break;
                case WssMethodNames.startPtzControlNotify: {
                    ControlModel controlModel = (ControlModel) BaseParams.fromJson(text, ControlModel.class).getParams();
                    roomEventCallback.startPtzControlNotify(controlModel);
                }
                break;
                case WssMethodNames.stopPtzControlNotify:
                    ControlModel controlModel = (ControlModel) BaseParams.fromJson(text, ControlModel.class).getParams();
                    roomEventCallback.stopPtzControlNotify(controlModel);
                    break;
                case WssMethodNames.reconnectSMS:
                    roomEventCallback.mediaServerReconnect();
                    break;
                default:
                    throw new JSONException("Can't understand method: " + method);
            }
        }
    }
    //</editor-fold>


    //<editor-fold desc="reqId 计数">
    private synchronized int getAtomReqId() {
        return atomReqId.get();
    }

    private void addAtomReqId() {
        atomReqId.incrementAndGet();
    }

    private int getAtomWsMsgId() {
        return atomWsMsgId.getAndIncrement();
    }

    private synchronized int getWebSocketMsgId() {
        return this.webSocketMsgId++;
    }

    //</editor-fold>


    public RoomClient() {
        this.reqId = 0;
        this.atomReqId = new AtomicInteger(0);
        this.atomWsMsgId = new AtomicInteger(0);
        this.wsReqReplyIds = new HashMap<>();

        this.remoteParticipants = new LinkedHashMap<>();
        this.mixFlows = new ConcurrentHashMap<>();
        this.pubAndSubParticipantIds = new ConcurrentHashMap<>();
        this.executor = new LooperExecutor();
    }

    private static class SingletonHolder {
        private static RoomClient instance = new RoomClient();
    }

    public static RoomClient get() {
        return SingletonHolder.instance;
    }

    private OkHttpClient okHttpClient;

    public void init(Application application) {

        manager = ServerManager.get();
    }

    /**
     * Context
     */
    private Context context;

    public void prepare(Context context, String wssUrl) {
        this.context = context;
        this.wssUrl = wssUrl;
        executor.requestStart();
        //开启 websocket
        connectWebSocket(true);
    }

    public void close() {
        if (this.poolExecutor != null) {
            this.poolExecutor.shutdown();
            this.poolExecutor = null;
        }
        closeWebSocket();
        L.d("RoomClient - close()");
    }

    public void onlyCloseWebsocket() {
        closeWebSocket();
    }

    /**
     * 解析: 房间内 已有 与会者
     *
     * @param joinRoomResp
     */
    private void parseLayoutInfo(JoinRoomResp joinRoomResp) {
        LayoutInfoBean layoutInfoBeans = joinRoomResp.getLayoutInfo();
        currentMode = layoutInfoBeans.getMode();
        for (PartLinkedListBean bean : layoutInfoBeans.getLinkedCoordinates()) {                        //设置当前与会者的坐标信息
            ParticipantInfoModel partModel;
            if (remoteParticipants.get(bean.getConnectionId()) == null) {                               //添加本人信息
                continue;
            } else {
                partModel = remoteParticipants.get(bean.getConnectionId());
            }
            partModel.setVideoActive(true);
            partModel.setAudioActive(true);
            remoteParticipants.put(bean.getConnectionId(), partModel);
        }
        setListBeans(layoutInfoBeans.getLinkedCoordinates());
    }

    //布局 更新变更通知
    public void layoutChange(List<PartLinkedListBean> listBeans) {
        setListBeans(listBeans);
        List<ParticipantInfoModel> tmpList = new ArrayList<>();
        for (Map.Entry<String, ParticipantInfoModel> entry : remoteParticipants.entrySet()) {
            L.d("layoutChange", "layoutChange...." + entry.getKey());
        }
        for (PartLinkedListBean linkedListBean : listBeans) {
            L.d("layoutChange", "listBeans...." + linkedListBean.getConnectionId());
            ParticipantInfoModel participant = remoteParticipants.get(linkedListBean.getConnectionId());
            if (participant == null) {
                participant = new ParticipantInfoModel();
            }
            participant.setLeft(linkedListBean.getLeft());
            participant.setTop(linkedListBean.getTop());
            participant.setWidth(linkedListBean.getWidth());
            participant.setHeight(linkedListBean.getHeight());
            tmpList.add(participant);
        }

        roomEventCallback.conferenceLayoutChanged(tmpList);
    }

    //布局 更新变更通知
    public List<ParticipantInfoModel> getLayoutData() {
        if (listBeans == null || listBeans.size() == 0) {
            return getRemoteParticipants();
        }
        List<ParticipantInfoModel> tmpList = new ArrayList<>();
//        for (Map.Entry<String, ParticipantInfoModel> entry : remoteParticipants.entrySet()) {
//            Log.i("layoutChange", "layoutChangeKey...." + entry.getKey());
//            Log.i("layoutChange", "layoutChangeValue...." + entry.getValue().getConnectionId());
//        }
        for (PartLinkedListBean linkedListBean : listBeans) {
            Log.i("layoutChange", "listBeans...." + linkedListBean.getConnectionId());
            ParticipantInfoModel participant = remoteParticipants.get(linkedListBean.getConnectionId());
            if (participant == null) {
                participant = new ParticipantInfoModel();
            }
            participant.setLeft(linkedListBean.getLeft());
            participant.setTop(linkedListBean.getTop());
            participant.setWidth(linkedListBean.getWidth());
            participant.setHeight(linkedListBean.getHeight());
            tmpList.add(participant);
        }
        return tmpList;
    }

    public ParticipantInfoModel getLocalParticipant() {
        return localParticipant;
    }

    public ParticipantInfoModel getLocalScreenParticipant() {
        return localScreenParticipant;
    }

    public ParticipantInfoModel getLocalHdmiParticipant() {
        return localHdmiParticipant;
    }

    public void setLocalParticipant(ParticipantInfoModel localParticipant) {
        this.localParticipant = localParticipant;
    }

    public void setLocalScreenParticipant(ParticipantInfoModel localScreenParticipant) {
        this.localScreenParticipant = localScreenParticipant;
    }

    public void setLocalHdmiParticipant(ParticipantInfoModel localHdmiParticipant) {
        this.localHdmiParticipant = localHdmiParticipant;
    }

    public ArrayList<ParticipantInfoModel> getRemoteParticipants() {
        if (remoteParticipants.values() != null) {
            return new ArrayList<>(remoteParticipants.values());
        }
        return null;
    }

    public ArrayList<ParticipantInfoModel> getMixFlow() {
        if (mixFlows.values() != null) {
            return new ArrayList<>(mixFlows.values());
        }
        return null;
    }

    public Map<String, ParticipantInfoModel> getMixFlows() {
        return mixFlows;
    }

    public void setMixFlows(ConcurrentHashMap<String, ParticipantInfoModel> mixFlows) {
        this.mixFlows = mixFlows;
    }

    public Map<String, ParticipantInfoModel> getRemoteParticipant() {
        return remoteParticipants;
    }

    /**
     * 解析: 房间内 已有 与会者
     *
     * @param joinRoomResp
     */
    private void parseParticipantsInRooms(JoinRoomResp joinRoomResp) {
        List<JoinRoomResp.ValueBean> participants = joinRoomResp.getValue();
        LayoutInfoBean layoutInfoBeans = joinRoomResp.getLayoutInfo();
        boolean isHasSpeak = false;
        for (JoinRoomResp.ValueBean tmpPart : participants) {
            JsonObject mtData = GsonProvider.provide().fromJson(tmpPart.getMetadata(), JsonObject.class);
            String tmpUserId = mtData.get("clientData").getAsString();
            String role = mtData.get("role").getAsString();
            if (
                    TextUtils.equals(tmpUserId, localParticipant.getUserId())
                            || (null != localScreenParticipant
                            && TextUtils.equals(tmpUserId, localScreenParticipant.getUserId()))
                            || (null != localHdmiParticipant
                            && TextUtils.equals(tmpUserId, localHdmiParticipant.getUserId()))
            ) {
                //列表返回的 房间内 已有与会者如果是 自己 则 跳过
                L.d("do this --->>>continue ");
                continue;
            }
            String connectionId = tmpPart.getId();
            String speakerStatus = tmpPart.getSpeakerStatus();
            String shareStatus = tmpPart.getShareStatus();
            String appShowName = tmpPart.getAppShowName();
            String appShowDesc = tmpPart.getAppShowDesc();
            String onlineStatus = tmpPart.getOnlineStatus();
            String handStatus = tmpPart.getHandStatus();
            String streamType = tmpPart.getStreamType();
            L.d("do this ---parseParticipantsInRooms>>>> " + tmpPart.getHandStatus());
            if ("speaker".equals(tmpPart.getHandStatus())) {
                isHasSpeak = isHasSpeak || true;
                setHandsId(tmpUserId);
                RoomManager.get().setConnectionMainId(connectionId);
            } else {
                isHasSpeak = isHasSpeak || false;
            }
            if ("up".equals(tmpPart.getHandStatus()) || "speaker".equals(tmpPart.getHandStatus())) {
                isShowRollCall = true;
            }

            ParticipantInfoModel participant = new ParticipantInfoModel();
            participant.setConnectionId(connectionId);
            participant.setUserId(tmpUserId);
            participant.setRole(role);
            participant.setSpeakerActive(Constract.VALUE_STATUS_ON.equals(speakerStatus));
            participant.setShareStatus(shareStatus);
            participant.setAppShowName(appShowName);
            participant.setAppShowDesc(appShowDesc);
            participant.setOnlineStatus(onlineStatus);
            participant.setStreamType(streamType);
            if (role.equals(ROLE_PUBLISHER_SUBSCRIBER)) {
                RoomManager.get().setHostId(tmpUserId);
                RoomManager.get().setHostConnectId(connectionId);
            }

            remoteParticipants.put(connectionId, participant);

            if (tmpPart.getStreams() != null && tmpPart.getStreams().size() > 0) {
                participant.setVideoActive(tmpPart.getStreams().get(0).isVideoActive());
                participant.setAudioActive(tmpPart.getStreams().get(0).isAudioActive());
                participant.setPublished(tmpPart.getStreams().size() > 0);
                participant.setStreamId(tmpPart.getStreams().get(0).getId());

                remoteParticipants.put(connectionId, participant);
                if (SHARING.equals(participant.getStreamType())) {
                    queueMaps.put(participant.getUserId() + LOCAL_SCREEN, connectionId);
                } else {
                    queueMaps.put(participant.getUserId(), connectionId);
                }
            } else {
                if (SHARING.equals(participant.getStreamType())) {
                    queueMaps.put(participant.getUserId() + LOCAL_SCREEN, connectionId);
                } else {
                    queueMaps.put(participant.getUserId(), connectionId);
                }
            }
            if (SHARING.equals(participant.getStreamType())) {
                currentShareUser = participant.getAppShowName();
                RoomManager.get().setConnectionShareId(tmpUserId);

            }
        }

        setHasHands(isHasSpeak);
    }


    private void handleSdpAnswer(int requestId, String answer) {
        String connectionId = pubAndSubParticipantIds.get(requestId);
        if (connectionId != null) {
            ParticipantInfoModel participant = null;
            if (connectionId.equalsIgnoreCase(localParticipant.getConnectionId())) {
                participant = localParticipant;
            } else if (localScreenParticipant != null && connectionId.equals(localScreenParticipant.getConnectionId())) {
                participant = localScreenParticipant;
            } else if (localHdmiParticipant != null && connectionId.equals(localHdmiParticipant.getConnectionId())) {
                participant = localHdmiParticipant;
            }
            if (participant == null) {
                participant = mixFlows.get(connectionId);
            }
            if (participant == null) {
                participant = remoteParticipants.get(connectionId);
            }


            if (participant != null) {
                SessionDescription sessionDescription = new SessionDescription(SessionDescription.Type.ANSWER, answer);
                roomRTCEvent.onRemoteSdpAnswerReceived(participant, sessionDescription);
            }
        }
    }

    private void participantJoinedMethod(JSONObject params) throws JSONException {
        String userId = new JSONObject(params.getString(Constants.METADATA)).getString("clientData");
        String role = new JSONObject(params.getString(Constants.METADATA)).getString("role");
        boolean isReconnected = params.getBoolean(Constants.ISRECONNECTED);
        String streamType = params.getString(Constants.STREAMTYPE);
        if (userId.equals(localParticipant.getUserId())
                || (localScreenParticipant != null && userId.equals(localScreenParticipant.getUserId()))
                || (localHdmiParticipant != null && userId.equals(localHdmiParticipant.getUserId()))) {
            return;
        }
        //与会者加入放入集合 加入信息不全， 具体信息通过publish取
        final ParticipantInfoModel participant = new ParticipantInfoModel();
        participant.setConnectionId(params.getString(Constants.ID));
        participant.setUserId(userId);
        participant.setRole(role);
        participant.setStreamType(streamType);
        //断线重连后的用户原来的链接直接移除
        if (isReconnected) {
            for (ParticipantInfoModel participantInfoModel : remoteParticipants.values()) {
                if (participantInfoModel.getUserId().equals(userId) && role.equals(participantInfoModel.getRole()) && streamType.equals(participantInfoModel.getStreamType())) {
                    remoteParticipants.remove(participantInfoModel.getConnectionId());
                    break;
                }
            }
            participant.setOnlineStatus(Constract.online);
        }
        if (SHARING.equals(streamType)) {
            queueMaps.put(participant.getUserId() + LOCAL_SCREEN, params.getString(Constants.ID));
        } else {
            queueMaps.put(participant.getUserId(), params.getString(Constants.ID));
        }
        //如果已经存在则不添加      且角色不是Thor（web会控）
        if (!remoteParticipants.containsKey(params.getString(Constants.ID)) && !"THOR".equals(role)) {
            remoteParticipants.put(params.getString(Constants.ID), participant);
        }
        Log.i("participantJoinedMethod", params.getString(Constants.ID));

        roomEventCallback.onParticipantJoined(participant);
    }

    private void participantLeftMethod(ParticipantLeft params) {
        final String connectionId = params.getConnectionId();
        currentLeftHandsUp = params.getRaiseHandNum();
        roomEventCallback.onParticipantLeft(remoteParticipants.remove(connectionId));
    }

    private void participantUserBreakMethod(ParticipantInfoModel params) {
        final String connectionId = params.getConnectionId();
        roomEventCallback.userBreakLine(remoteParticipants.get(connectionId));
    }

    private void reconnectPartStopPublishSharing(ParticipantLeft params) {
        final String connectionId = params.getConnectionId();
        currentLeftHandsUp = params.getRaiseHandNum();
        roomEventCallback.reconnectPartStopPublishSharing(remoteParticipants.remove(connectionId));
    }


    private void participantEvictedMethod(ParticipantEvicted params) {
        final String connectionId = params.getConnectionId();
        roomEventCallback.onParticipantEvicted(remoteParticipants.remove(connectionId), connectionId);
    }


    private void participantUnPublishedMethod(ParticipantUnPublishedModel params) {
        String connectionId = params.getConnectionId();
        ParticipantInfoModel participant = remoteParticipants.get(connectionId);
        if (participant != null) {
            participant.setPublished(false);
        }
        roomRTCEvent.onParticipantUnPublished(participant);
    }

    /**
     * 解析都是需要改的
     */
    private void participantPublishedMethod(ParticipantPublishedModel params) throws
            JSONException {
        Log.i("participantPub", "participantPublishedMethod..." + mixFlows.size());
        String connectionId = params.getId();
        if ((params.getMixFlows() != null && params.getMixFlows().size() > 0 && (params.getId().equals(RoomManager.get().getConnectionId())) || params.getMixFlows() != null && params.getMixFlows().size() > 0)) {
            for (int i = 0; i < params.getMixFlows().size(); i++) {//mcu
                ParticipantInfoModel participant1 = new ParticipantInfoModel();
                participant1.setPublished(true);
                participant1.setAppShowDesc(params.getAppShowDesc());
                participant1.setAppShowName(params.getAppShowName());
                participant1.setStreamMode(params.getMixFlows().get(i).getStreamMode());
                participant1.setStreamType(params.getMixFlows().get(i).getStreamMode());
                participant1.setStreamId(params.getMixFlows().get(i).getStreamId());
                participant1.setConnectionId(StringUtils.streamIdToConnect(params.getMixFlows().get(i).getStreamId(), participant1.getStreamType()));
                if (MIX_MAJOR_AND_SHARING.equals(params.getMixFlows().get(i).getStreamMode())) {
                    RoomManager.get().setConnectionMixId(participant1.getConnectionId());
                }
                if (!mixFlows.containsKey(participant1.getConnectionId())) {
                    Iterator<Map.Entry<String, ParticipantInfoModel>> it = mixFlows.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<String, ParticipantInfoModel> entry = it.next();
                        if (SFU_SHARING.equals(entry.getValue().getStreamMode())) {
                            it.remove();
                        }
                    }
                    Log.i("containsKey", "containsKey" + mixFlows.size());
                    roomRTCEvent.onMixPublished(participant1, params.getMixFlows().size() == 1 ? false : true);
                    mixFlows.put(participant1.getConnectionId(), participant1);
                } else {
                    ParticipantInfoModel participantInfoModel = mixFlows.get(participant1.getConnectionId());
                    roomRTCEvent.onMixPublished(participantInfoModel, params.getMixFlows().size() == 1 ? false : true);
                }
            }
        }
        if (params.getStreams() != null && params.getStreams().size() > 0) {                        //sfu
            for (int i = 0; i < params.getStreams().size(); i++) {
                ParticipantInfoModel participant = remoteParticipants.get(connectionId);
                participant.setPublished(true);
                participant.setStreamType(params.getStreams().get(i).getStreamType());
                participant.setAudioActive(params.getStreams().get(i).isAudioActive());
                participant.setVideoActive(params.getStreams().get(i).isVideoActive());
                participant.setSpeakerActive(true);
                participant.setShareStatus(params.getStreams().get(i).getShareStatus());
                participant.setStreamId(params.getStreams().get(i).getId());
                participant.setAppShowName(params.getAppShowName());
                participant.setAppShowDesc(params.getAppShowDesc());
//                roomRTCEvent.onParticipantPublished(participant);
                if (SHARING.equals(participant.getStreamType())) {
                    currentShareUser = participant.getAppShowName();
                }
                if ("SFU".equals(RoomManager.get().getConferenceMode())) {
                    roomRTCEvent.onParticipantPublished(participant);
                }
            }
        } else {                                                                                    //mcu
            ParticipantInfoModel participant1 = remoteParticipants.get(connectionId);
            participant1.setPublished(true);
            participant1.setShareStatus("off");
            participant1.setSpeakerActive(true);
            participant1.setAudioActive(true);
            participant1.setVideoActive(true);
            participant1.setAppShowName(params.getAppShowName());
            participant1.setAppShowDesc(params.getAppShowDesc());
//            roomRTCEvent.onParticipantPublished(participant1);
        }

    }

    /**
     * @param params
     * @throws JSONException
     */
    private void iceCandidateMethod(IceCandidateModel params) throws JSONException {
        ParticipantInfoModel participant = null;
        String endPointName = params.getSenderConnectionId();
        if (endPointName.equalsIgnoreCase(localParticipant.getConnectionId())) {
            participant = localParticipant;
        } else if (localScreenParticipant != null && endPointName.equals(localScreenParticipant.getConnectionId())) {
            participant = localScreenParticipant;
        } else if (localHdmiParticipant != null && endPointName.equals(localHdmiParticipant.getConnectionId())) {
            participant = localHdmiParticipant;
        }
        if (participant == null) {
            participant = mixFlows.get(endPointName);
        }
        if (participant == null) {
            participant = remoteParticipants.get(endPointName);
        }
        Log.i("iceCandidateMethod", "iceCandidateMethod" + participant);
        IceCandidate iceCandidate = new IceCandidate(params.getSdpMid(), params.getSdpMLineIndex(), params.getCandidate());
        roomRTCEvent.onRemoteIceCandidateReceived(participant, iceCandidate);
    }

    /**
     * 开启 WebSocket 连接
     *
     * @see #onDisconnected(WebSocket, WebSocketFrame, WebSocketFrame, boolean)
     * @see #prepare(Context, String)
     */
    private void connectWebSocket(boolean wss) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    WebSocketFactory factory = new WebSocketFactory();
                    SSLContext sslContext;
                    if (wss) {
                        CertificateFactory cAf = CertificateFactory.getInstance("X.509");
                        InputStream caIn = context.getAssets().open("certs/client.crt");

                        X509Certificate ca = (X509Certificate) cAf.generateCertificate(caIn);
                        KeyStore caKs = KeyStore.getInstance("PKCS12");
                        caKs.load(null, null);
                        caKs.setCertificateEntry("ca-certificate", ca);
                        TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");
                        tmf.init(caKs);

                        sslContext = SSLContext.getInstance("TLS");
                        sslContext.init(null, tmf.getTrustManagers(), new java.security.SecureRandom());
                    } else {
                        sslContext = SSLContext.getInstance("TLS");
                        sslContext.init(null, new TrustManager[]{new TrustAllCertsTrustManager()}, new java.security.SecureRandom());
                    }

                    factory.setSSLContext(sslContext);
                    factory.setVerifyHostname(false);
//                    webSocket = factory.createSocket(wssUrl);
                    webSocket = factory.createSocket(wssUrl.concat("/openvidu"));
                    webSocket.addListener(RoomClient.this);
                    webSocket.connect();
                } catch (IOException | CertificateException | KeyStoreException | KeyManagementException | WebSocketException | NoSuchAlgorithmException | IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 关闭 webSocket
     */
    private void closeWebSocket() {
        if (webSocket != null) {
            webSocket.disconnect();
            webSocket = null;
        }
    }


    public AppCompatActivity getCurrentActivity() {
        return currentActivity != null ? currentActivity.get() : null;
    }

    public void setCurrentActivity(AppCompatActivity activity) {
        currentActivity = new WeakReference<>(activity);
    }

    public String getHandsId() {
        return handsId;
    }

    public void setHandsId(String handsId) {
        this.handsId = handsId;
    }

    public boolean isHasHands() {
        return hasHands;
    }

    public void setHasHands(boolean hasHands) {
        this.hasHands = hasHands;
    }

    //设置是否强制登录 标志位
    public boolean isForceLogin() {
        return isForceLogin;
    }

    public void setForceLogin(boolean forceLogin) {
        isForceLogin = forceLogin;
    }

    public WebSocketStateListener getWebSocketStateListener() {
        return webSocketStateListener;
    }

    public void setWebSocketStateListener(WebSocketStateListener webSocketStateListener) {
        this.webSocketStateListener = webSocketStateListener;
    }


    //判断当前websocket是否连接正常
    public boolean isWebSocketOpen() {
        return null != webSocket && webSocket.isOpen();
    }
    //登录成功 判断

    public boolean isAccessInSuccess() {
        return isAccessInSuccess;
    }

    public void setAccessInSuccess(boolean accessInSuccess) {
        isAccessInSuccess = accessInSuccess;
    }

    public boolean isShowRollCall() {
        return isShowRollCall;
    }

    public void setShowRollCall(boolean showRollCall) {
        isShowRollCall = showRollCall;
    }

    public int getCurrentLeftHandsUp() {
        return currentLeftHandsUp;
    }

    public void setCurrentLeftHandsUp(int currentLeftHandsUp) {
        this.currentLeftHandsUp = currentLeftHandsUp;
    }

    @LayoutMode
    public int getCurrentMode() {
        return currentMode;
    }

    public void setCurrentMode(@LayoutMode int currentMode) {
        this.currentMode = currentMode;
    }

    public LoginStateListener getLoginStateListener() {
        return loginStateListener;
    }

    public void setLoginStateListener(LoginStateListener loginStateListener) {
        this.loginStateListener = loginStateListener;
    }

    public List<PartLinkedListBean> getListBeans() {
        return listBeans;
    }

    public void setListBeans(List<PartLinkedListBean> listBeans) {
        this.listBeans = listBeans;
    }

    public String getCurrentShareUser() {
        return currentShareUser;
    }

    public void setCurrentShareUser(String currentShareUser) {
        this.currentShareUser = currentShareUser;
    }

    public void setOnLocalShareJoinListener(OnLocalShareJoinListener onLocalShareJoinListener) {
        this.onLocalShareJoinListener = onLocalShareJoinListener;
    }

    //本地分享流 添加
    public interface OnLocalShareJoinListener {
        void OnLocalShareJoin(ParticipantInfoModel participantInfoModel);
    }

    public HashMap<String, String> getQueueMaps() {
        return queueMaps;
    }

    public void setQueueMaps(HashMap<String, String> queueMaps) {
        this.queueMaps = queueMaps;
    }
}