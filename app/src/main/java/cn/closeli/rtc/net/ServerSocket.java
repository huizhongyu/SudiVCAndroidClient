package cn.closeli.rtc.net;

import com.google.gson.JsonObject;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import cn.closeli.rtc.model.http.GsonProvider;
import cn.closeli.rtc.room.PadEvent;
import cn.closeli.rtc.utils.Constants;

public class ServerSocket extends WebSocketServer {
    private PadEvent padEvent;
    private ServerManager manager;

    public ServerSocket(InetSocketAddress address, ServerManager serverManager) {
        super(address);
        this.manager = serverManager;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        conn.send("Welcome to the server!"); //This method sends a message to the new client
        broadcast("new connection: " + handshake.getResourceDescriptor()); //This method sends a message to all clients connected
        System.out.println("new connection to " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("closed " + conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("onTextdWebServer" + conn.getRemoteSocketAddress() + ": " + message);
        JsonObject json = GsonProvider.provide().fromJson(message, JsonObject.class);
        if (json.has("jsonrpc")) {
            try {
                if (json.has((Constants.PADMSG))) {
                    manager.handlePadMsg(conn, new JSONObject(message), json);
                } else {
                    manager.handleParams(conn, new JSONObject(message), json);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        System.out.println("received ByteBuffer from " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        if(conn != null) {
            System.err.println("an error occurred on connection " + conn.getRemoteSocketAddress() + ":" + ex);
        }
    }

    @Override
    public void onStart() {
        System.out.println("server started successfully");
    }

    public PadEvent getPadEvent() {
        return padEvent;
    }

    public void setPadEvent(PadEvent padEvent) {
        this.padEvent = padEvent;
    }
}