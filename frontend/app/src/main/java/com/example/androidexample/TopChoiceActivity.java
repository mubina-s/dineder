package com.example.androidexample;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;

public class TopChoiceActivity extends AppCompatActivity {

    private TextView topChoiceName, topChoiceWebsite, topChoicePhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_choice);

        topChoiceName = findViewById(R.id.topChoiceName);
        topChoiceWebsite = findViewById(R.id.topChoiceWebsite);
        topChoicePhone = findViewById(R.id.topChoicePhone);

        long restaurantId = getIntent().getLongExtra("restaurantId", -1);

        if (restaurantId != -1) {
            String url = "http://10.0.2.2:8080/restaurants/" + restaurantId;

            RequestQueue queue = Volley.newRequestQueue(this);
            JsonObjectRequest request = new JsonObjectRequest(
                    com.android.volley.Request.Method.GET,
                    url,
                    null,
                    response -> {
                        try {
                            String name = response.getString("name");
                            String website = response.optString("website", "N/A");
                            String phone = response.optString("phone", "N/A");

                            topChoiceName.setText(name);
                            topChoiceWebsite.setText(website);
                            topChoicePhone.setText(phone);
                        } catch (JSONException e) {
                            Log.e("TopChoiceActivity", "JSON parse error", e);
                        }
                    },
                    error -> Log.e("TopChoiceActivity", "Error fetching restaurant details", error)
            );
            queue.add(request);
        } else {
            topChoiceName.setText("Unknown");
            topChoiceWebsite.setText("N/A");
            topChoicePhone.setText("N/A");
        }
    }

}
