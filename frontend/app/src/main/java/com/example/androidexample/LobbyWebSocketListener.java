package com.example.androidexample;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.SparseArray;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Set;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class LobbyWebSocketListener extends WebSocketListener {
    private UserAdapter adapter;
    private List<SessionParticipant> participantsList;
    private Context context;

    private TextView percentTxt;

    public LobbyWebSocketListener(UserAdapter adapter, List<SessionParticipant> participantsList, TextView percentTxt, Context context) {
        this.adapter = adapter;
        this.participantsList = participantsList;
        this.percentTxt = percentTxt;
        this.context = context;
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        Log.d("WebSocket", "Connected to lobby");
        Log.d("WebSocket", "Response code: " + response.code());
        Log.d("WebSocket", "Response message: " + response.message());
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        try {
            Log.d("WebSocket", "Received message: " + text);

            JSONObject obj = new JSONObject(text);
            String type = obj.optString("type");

            switch (type) {
                case "LOBBY_UPDATE":
                    JSONObject sessionObj = obj.getJSONObject("session");
                    JSONArray participantsArray = sessionObj.optJSONArray("participantsDTOList");
                    int percentReady = obj.optInt("percentReady", -1);

                    if (participantsArray != null) {
                        Log.d("WebSocket", "Updating participant list. Count: " + participantsArray.length());
                        List<SessionParticipant> updatedUsers = SessionParser.parseParticipants(participantsArray);
                        Log.d("WebSocket", "Parsed participants: " + updatedUsers.size());
                        new Handler(Looper.getMainLooper()).post(() -> adapter.updateList(updatedUsers));
                        Log.d("WebSocket", "LOBBY_UPDATE received. Participants: " + participantsArray.toString());
                        new Handler(Looper.getMainLooper()).post(() -> percentTxt.setText(percentReady + "%"));
                    } else {
                        Log.w("WebSocket", "participantsList missing from session object");
                    }

                    break;

                case "SESSION_START":
                    Intent intent = new Intent(context, SwipingActivity.class);
                    context.startActivity(intent);
                    break;

                case "KICKED":
                    new Handler(Looper.getMainLooper()).post(() -> {
                        Toast.makeText(context, "You have been kicked by the host.", Toast.LENGTH_LONG).show();
                        if (context instanceof android.app.Activity) {
                            Intent newIntent = new Intent(context, UserJoinActivity.class);
                            newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(newIntent);
                            ((android.app.Activity) context).finish(); // Optional: close current screen
                        } else {
                            Log.e("WebSocket", "Context is not an Activity — cannot redirect");
                        }
                     });

                    break;

                case "ERROR":
                    String message = obj.optString("message");
                    new Handler(Looper.getMainLooper()).post(() ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    );
                    break;

                default:
                    Log.w("WebSocket", "Unknown message type: " + type);
                    break;
            }
        } catch (JSONException e) {
            Log.e("WebSocket", "JSON parsing error: " + e.getMessage());
            e.printStackTrace();
        }

    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        Log.e("WebSocket", "Connection failed: " + t.getMessage());
        if (response != null) {
            Log.e("WebSocket", "Response code: " + response.code());
            Log.e("WebSocket", "Response message: " + response.message());
        }
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(context, "WebSocket connection failed", Toast.LENGTH_LONG).show()
        );
    }
}

