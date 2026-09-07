package com.example.androidexample;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidexample.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Set;

import okhttp3.WebSocket;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<SessionParticipant> participantList;
    private Long hostId;

    private Long currentUserId;
    private WebSocket webSocket;
    private Context context;

    public UserAdapter(List<SessionParticipant> participantList, Long hostId, WebSocket webSocket, Context context) {
        this.participantList = participantList;
        this.hostId = hostId;
        this.webSocket = webSocket;
        this.context = context;
        Log.d("UserAdapter", "Adapter created with " + participantList.size() + " participants. Host ID: " + hostId);
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        public TextView userName;
        public TextView readinessStatus;
        public ImageButton kickButton;

        public UserViewHolder(View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userName);
            readinessStatus = itemView.findViewById(R.id.readinessStatus);
            kickButton = itemView.findViewById(R.id.kickButton);
        }
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {

        currentUserId = SessionParticipantManager.getSessionPar().getUserId();
        hostId = SessionManager.getSession().getHostId();

        SessionParticipant user = participantList.get(position);

        Log.d("UserAdapter", "Binding user: " + user.getName() + " (User ID: " + user.getUserId() + ")");
        Log.d("UserAdapter", "Ready status: " + user.getIsReady());

     //Apply host styling to host row
        if (user.getUserId().equals(hostId)) {
            Log.d("UserAdapter", "Applying host styling for: " + user.getName());
            holder.userName.setText("HOST: " + user.getName());
            holder.userName.setTypeface(null, Typeface.BOLD);
        } else {
            holder.userName.setText(user.getName());
            holder.userName.setTypeface(null, Typeface.NORMAL);
        }

        holder.userName.setTextColor(Color.BLACK);

        //show kick buttons if logged in as host
        if(currentUserId.equals(hostId) && !user.getUserId().equals(hostId)) {
            holder.kickButton.setVisibility(View.VISIBLE);
        } else {
            holder.kickButton.setVisibility(View.GONE);
        }

        //readiness status:
        holder.readinessStatus.setText(user.getIsReady() ? "READY" : "NOT READY");
        holder.readinessStatus.setTextColor(user.getIsReady() ? Color.GREEN : Color.RED);

        //kick logic
        holder.kickButton.setOnClickListener(v -> {
            Log.d("UserAdapter", "Kick button clicked for: " + user.getName() + " (User ID: " + user.getParticipantId() + ")");
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Kick User")
                    .setMessage("Are you sure you want to remove " + user.getName() + "?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        JSONObject msg = new JSONObject();
                        try {
                            Log.d("WebSocket", "Constructed kick message: " + msg.toString());
                            msg.put("action", "KICK_PARTICIPANT");
                            msg.put("hostUserId", SessionManager.getSession().getHostId());
                            msg.put("participantToKickId", user.getParticipantId()); // or sessionParticipantId
                            if (webSocket != null) {
                                Log.d("WebSocket", "Sending: " + msg.toString());
                                boolean sent = webSocket.send(msg.toString());
                                Log.d("WebSocket", "Kick message sent: " + sent);
                                if (!sent) {
                                    Log.e("WebSocket", "WebSocket.send() returned false");
                                } else {
                                    Toast.makeText(context, user.getName() + " has been kicked.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(context, "WebSocket not connected", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("UserAdapter", "JSON error while constructing kick message: " + e.getMessage());
                            e.printStackTrace();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return participantList.size();
    }

    public void setWebSocket(WebSocket webSocket) {
        this.webSocket = webSocket;
        Log.d("UserAdapter", "WebSocket set: " + (webSocket != null));
    }

    public void updateList(List<SessionParticipant> newList) {
        participantList.clear();
        participantList.addAll(newList);
        notifyDataSetChanged();
    }

}
