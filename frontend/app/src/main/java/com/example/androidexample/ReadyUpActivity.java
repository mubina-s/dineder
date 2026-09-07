package com.example.androidexample;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;

public class ReadyUpActivity extends AppCompatActivity {

    private Button readyBtn, hostStartBtn;
    private TextView joinCodeTxt;

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<SessionParticipant> participantsList;

    private TextView percentText;

    private List<Restaurant> restaurants;

    private TextSwitcher restaurantSwitcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ready_up);

        Session session = SessionManager.getSession();
        long sessionId = SessionManager.getSession().getSessionId();
        long participantId = SessionParticipantManager.getSessionPar().getParticipantId();
        long userId = SessionParticipantManager.getSessionPar().getUserId();
        Log.d("ReadyUpActivity", "Session ID: " + sessionId + ", Participant ID: " + participantId);

        //Initalize participantsList
        participantsList = new ArrayList<>();
        if(session.getParticipantsDTOList() != null) {
            participantsList.addAll(session.getParticipantsDTOList());
        }

        Long hostId = session.getHostId();

        //Create adapter
        adapter = new UserAdapter(participantsList, hostId, null, this);

        //Percent TextView
        percentText = findViewById(R.id.percentText);

        //Create listener
        LobbyWebSocketListener listener = new LobbyWebSocketListener(adapter, participantsList, percentText, this);

        //Open websocket
        String wsUrl = "ws://10.0.2.2:8080/ws/lobby/" + sessionId + "/" + participantId;
        OkHttpClient client = new OkHttpClient();
        Log.d("WebSocket", "Connecting to: " + wsUrl);
        Request request = new Request.Builder().url(wsUrl).build();
        WebSocket webSocket = client.newWebSocket(request, listener);
        Log.d("WebSocket", "WebSocket created and listener attached");

        //Add websocket to adapter
        adapter.setWebSocket(webSocket);

        //set up UI
        readyBtn = (Button) findViewById(R.id.ready_btn);
        //TODO: check logic so only visible if userID = hostID
        hostStartBtn = (Button) findViewById(R.id.hostStart_btn);
        if(userId != hostId) {
            hostStartBtn.setVisibility(View.GONE);
        }
        joinCodeTxt = (TextView) findViewById(R.id.joinCode);
        joinCodeTxt.setText(session.getJoinCode());

        //participants recycler view
        recyclerView = findViewById(R.id.userRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        // Add simple dividers between rows
        DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(divider);

        //Restaurant TextSwitcher
        restaurants = session.getRestaurantList();
        List<String> restaurantNames = new ArrayList<>();
        for (Restaurant r : restaurants) {
            restaurantNames.add(r.name); // or r.getName() + " - " + r.getCuisine()
        }
        String[] messages = restaurantNames.toArray(new String[0]);
        restaurantSwitcher = (TextSwitcher) findViewById(R.id.textSwitcher);
        restaurantSwitcher.setFactory(() -> {
            TextView textView = new TextView(this);
            textView.setTextSize(24);
            textView.setTextColor(Color.WHITE);
            textView.setGravity(Gravity.CENTER);
            return textView;
        });
        final int[] index = {0};  // mutable wrapper
        Handler handler = new Handler();
        Runnable switchText = new Runnable() {
            @Override
            public void run() {
                restaurantSwitcher.setText(messages[index[0]]);
                index[0] = (index[0] + 1) % messages.length;
                handler.postDelayed(this, 3000);
            }
        };
        handler.post(switchText);

        readyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject msg = new JSONObject();
                try {
                    msg.put("action", "SET_READY");
                    msg.put("isReady", true); // or false to toggle off
                    if (webSocket != null) {
                        Log.d("WebSocket", "Sending: " + msg.toString());
                        boolean sent = webSocket.send(msg.toString());
                        Log.d("WebSocket", "Message send status: " + sent);
                    } else {
                        Toast.makeText(ReadyUpActivity.this, "WebSocket not connected", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        hostStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject msg = new JSONObject();
                try {
                    msg.put("action", "FORCE_START");
                    msg.put("hostId", hostId); // your current user ID
                    if (webSocket != null) {
                        Log.d("WebSocket", "Sending: " + msg.toString());
                        webSocket.send(msg.toString());
                        Toast.makeText(ReadyUpActivity.this, "Host started session", Toast.LENGTH_SHORT).show();
                            Intent newIntent = new Intent(ReadyUpActivity.this, SwipingActivity.class);
                            startActivity(newIntent);
                    } else {
                        Toast.makeText(ReadyUpActivity.this, "WebSocket not connected", Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

}
