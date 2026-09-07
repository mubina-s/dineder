package com.example.androidexample;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * Simple singleton WebSocket manager for the App Reviews WebSocket.
 * Uses OkHttp under the hood.
 */
public class WebSocketManager {

    public interface WebSocketCallback {
        void onConnected();
        void onDisconnected(int code, String reason);
        void onMessage(String text);
        void onFailure(Throwable t);
    }

    private static WebSocketManager instance;

    private final OkHttpClient client;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private WebSocket socket;
    private WebSocketCallback callback;

    private WebSocketManager() {
        client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS) // keep alive
                .build();
    }

    public static synchronized WebSocketManager getInstance() {
        if (instance == null) {
            instance = new WebSocketManager();
        }
        return instance;
    }

    /**
     * Connect once. Subsequent calls while socket is non-null are ignored.
     */
    public synchronized void connect(String url, @Nullable WebSocketCallback cb) {
        this.callback = cb;

        if (socket != null) {
            // already connected or connecting
            return;
        }

        Request request = new Request.Builder()
                .url(url)
                .build();

        socket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                mainHandler.post(() -> {
                    if (callback != null) callback.onConnected();
                });
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                mainHandler.post(() -> {
                    if (callback != null) callback.onMessage(text);
                });
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                webSocket.close(code, reason);
                socket = null;
                mainHandler.post(() -> {
                    if (callback != null) callback.onDisconnected(code, reason);
                });
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                socket = null;
                mainHandler.post(() -> {
                    if (callback != null) callback.onDisconnected(code, reason);
                });
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                socket = null;
                mainHandler.post(() -> {
                    if (callback != null) callback.onFailure(t);
                });
            }
        });
    }

    /**
     * Send a raw text message, returns false if not connected.
     */
    public synchronized boolean sendMessage(String text) {
        return socket != null && socket.send(text);
    }

    /**
     * Disconnect the socket if open.
     */
    public synchronized void disconnect() {
        if (socket != null) {
            try {
                socket.close(1000, "Client disconnect");
            } catch (Exception ignored) {}
            socket = null;
        }
    }
}
