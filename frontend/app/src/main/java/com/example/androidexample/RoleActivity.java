package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class RoleActivity extends AppCompatActivity {

    private Button hostBtn, joinBtn, dashboardBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role);

        hostBtn = (Button) findViewById(R.id.host_btn);
        joinBtn = (Button) findViewById(R.id.join_btn);
        dashboardBtn = (Button) findViewById(R.id.dashboard_btn);

        hostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newIntent = new Intent(RoleActivity.this, SessionPrefsActivity.class);
                startActivity(newIntent);
            }
        });

        joinBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Intent newIntent = new Intent(RoleActivity.this, UserJoinActivity.class);
               startActivity(newIntent);
           }
        });

        dashboardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newIntent = new Intent(RoleActivity.this, DashboardActivity.class);
                startActivity(newIntent);
            }
        });
    }
}
