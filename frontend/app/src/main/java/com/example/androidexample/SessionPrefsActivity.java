package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class SessionPrefsActivity extends AppCompatActivity {

    private Button backBtn;
    private Button createNewPrefsBtn;
    private Button existingPrefsBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_prefs);

        backBtn = (Button) findViewById(R.id.btnBack);
        createNewPrefsBtn = (Button) findViewById(R.id.createNewPrefsBtn);
        existingPrefsBtn = (Button) findViewById(R.id.existingPrefsBtn);

        backBtn.setOnClickListener(v ->
                startActivity(new Intent(this, MainActivity.class)));

        createNewPrefsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SessionPrefsActivity.this, CreateSessionPrefsActivity.class);
                startActivity(intent);
            }
        });

        existingPrefsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SessionPrefsActivity.this, SavedSessionPrefsActivity.class);
                startActivity(intent);
            }
        });
    }
}

