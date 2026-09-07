package com.example.androidexample;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.Request;
import okhttp3.WebSocketListener;

public class VotingWebSocketManager {

    private static VotingWebSocketManager instance;
    private WebSocket webSocket;
    private OkHttpClient client;
    private final List<VotingUpdateListener> listeners = new ArrayList<>();

    private VotingWebSocketManager() {
        client = new OkHttpClient();
    }

    public static synchronized VotingWebSocketManager getInstance() {
        if (instance == null) {
            instance = new VotingWebSocketManager();
        }
        return instance;
    }

    /** Connect to backend WebSocket with sessionId */
    public void connect(long sessionId) {
        String url = "ws://10.0.2.2:8080/ws/voting?sessionId=" + sessionId;
        Request request = new Request.Builder().url(url).build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket ws, Response response) {
                Log.d("VotingWS", "Connected to session " + sessionId);
            }

            @Override
            public void onMessage(WebSocket ws, String text) {
                Log.d("VotingWS", "Message: " + text);
                notifyListeners(text);
            }

            @Override
            public void onFailure(WebSocket ws, Throwable t, Response response) {
                Log.e("VotingWS", "Error: " + t.getMessage());
            }

            @Override
            public void onClosed(WebSocket ws, int code, String reason) {
                Log.d("VotingWS", "Closed: " + reason);
            }
        });
    }

    /** Send CAST_VOTE action */
    public void sendVote(long sessionId, long participantId, long restaurantId, int value, int round) {
        try {
            JSONObject voteData = new JSONObject();
            voteData.put("action", "CAST_VOTE");
            voteData.put("sessionId", sessionId);
            voteData.put("sessionParticipantId", participantId);
            voteData.put("restaurantId", restaurantId);
            voteData.put("value", value);
            voteData.put("round", round);
            webSocket.send(voteData.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /** Send GET_RESULTS action */
    public void getResults(long sessionId, int round) {
        try {
            JSONObject data = new JSONObject();
            data.put("action", "GET_RESULTS");
            data.put("sessionId", sessionId);
            data.put("round", round);
            webSocket.send(data.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /** Send FINALIZE_VOTING action */
    public void finalizeVoting(long sessionId, long restaurantId) {
        try {
            JSONObject data = new JSONObject();
            data.put("action", "FINALIZE_VOTING");
            data.put("sessionId", sessionId);
            data.put("restaurantId", restaurantId);
            webSocket.send(data.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /** NEW: Send FINISHED_SWIPING action */
    public void sendFinishedSwiping(long sessionId, long participantId) {
        try {
            JSONObject data = new JSONObject();
            data.put("action", "FINISHED_SWIPING");
            data.put("sessionId", sessionId);
            data.put("sessionParticipantId", participantId);
            webSocket.send(data.toString());
            Log.d("VotingWS", "Sent FINISHED_SWIPING for participant " + participantId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /** Listener registration */
    public void addListener(VotingUpdateListener listener) {
        listeners.add(listener);
    }

    private void notifyListeners(String message) {
        for (VotingUpdateListener l : listeners) {
            l.onVotingUpdate(message);
        }
    }

    /** Listener interface */
    public interface VotingUpdateListener {
        void onVotingUpdate(String message);
    }
}
