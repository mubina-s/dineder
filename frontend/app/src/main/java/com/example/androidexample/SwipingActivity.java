package com.example.androidexample;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

public class SwipingActivity extends AppCompatActivity {

    List<Restaurant> restaurants;

    private TextSwitcher completedRestaurants;
    private TextSwitcher name;
    int index;

    private ImageSwitcher imageSwitcher;
    private TextSwitcher rating;
    private TextSwitcher price;
    private TextSwitcher cuisines;
    private Button dislikeButton;
    private Button likeButton;

    private VotingWebSocketManager wsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Log.d("SwipingActivity", "onCreate started");
            setContentView(R.layout.activity_swiping);
            Toast.makeText(this, "SwipingActivity launched", Toast.LENGTH_SHORT).show();

            Session session = SessionManager.getSession();
            long sessionId = session.getSessionId();

            wsManager = VotingWebSocketManager.getInstance();
            wsManager.connect(sessionId);

            SessionParticipant sessionParticipant = SessionParticipantManager.getSessionPar();

            restaurants = new ArrayList<>();
            if (session != null && session.getRestaurantList() != null) {
                restaurants.addAll(session.getRestaurantList());
            } else {
                Toast.makeText(this, "No restaurants found", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            completedRestaurants = findViewById(R.id.completedRestaurants);
            name = findViewById(R.id.TextSwitcherName);
            imageSwitcher = findViewById(R.id.imageSwitcher);
            rating = findViewById(R.id.TextSwitcherRating);
            price = findViewById(R.id.TextSwitcherPrice);
            cuisines = findViewById(R.id.TextSwitcherCuisines);
            dislikeButton = findViewById(R.id.dislike_btn);
            likeButton = findViewById(R.id.like_btn);

            index = 0;
            Restaurant first = restaurants.get(index);

            // Factories
            completedRestaurants.setFactory(() -> makeTextView(24));
            name.setFactory(() -> makeTextView(24));
            imageSwitcher.setFactory(() -> {
                ImageView imageView = new ImageView(this);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setLayoutParams(
                        new ImageSwitcher.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                        )
                );
                return imageView;
            });
            rating.setFactory(() -> makeTextView(24));
            price.setFactory(() -> makeTextView(24));
            cuisines.setFactory(() -> makeTextView(20));

            rating.setVisibility(View.VISIBLE);
            price.setVisibility(View.VISIBLE);

            // Initial UI
            completedRestaurants.setText(index + "/" + restaurants.size());
            name.setText(first.name);
            Glide.with(this).load(first.image).into((ImageView) imageSwitcher.getCurrentView());
            rating.setText(Integer.toString(first.rating));
            price.setText(first.priceRange);
            cuisines.setText(String.join(", ", first.cuisines));

            // Dislike button
            dislikeButton.setOnClickListener(v -> {
                if (index < restaurants.size()) {
                    wsManager.sendVote(sessionId,
                            sessionParticipant.getParticipantId(),
                            restaurants.get(index).id,
                            0, 1);

                    index++;
                    if (index == restaurants.size()) {
                        finishSwiping(sessionId, sessionParticipant);
                    } else {
                        updateUI();
                    }
                }
            });

            // Like button
            likeButton.setOnClickListener(v -> {
                if (index < restaurants.size()) {
                    wsManager.sendVote(sessionId,
                            sessionParticipant.getParticipantId(),
                            restaurants.get(index).id,
                            1, 1);

                    index++;
                    if (index == restaurants.size()) {
                        finishSwiping(sessionId, sessionParticipant);
                    } else {
                        updateUI();
                    }
                }
            });

        } catch (Exception e) {
            Log.e("SwipingActivity", "Error in onCreate", e);
            Toast.makeText(this, "Error starting activity", Toast.LENGTH_LONG).show();
        }
    }

    /** Helper to make TextViews for switchers */
    private TextView makeTextView(int size) {
        TextView textView = new TextView(this);
        textView.setTextSize(size);
        textView.setTextColor(Color.WHITE);
        textView.setGravity(Gravity.CENTER);
        return textView;
    }

    /** Called when user finishes swiping all restaurants */
    private void finishSwiping(long sessionId, SessionParticipant sessionParticipant) {
        Toast.makeText(this, "You're all done!", Toast.LENGTH_SHORT).show();
        likeButton.setEnabled(false);
        dislikeButton.setEnabled(false);

        // Notify backend
        wsManager.sendFinishedSwiping(sessionId, sessionParticipant.getParticipantId());

        // Move to FinalResultsActivity immediately
        Intent intent = new Intent(SwipingActivity.this, FinalResultsActivity.class);
        startActivity(intent);
    }

    /** Update UI for next restaurant */
    private void updateUI() {
        completedRestaurants.setText(index + "/" + restaurants.size());

        // Animations
        name.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_right));
        name.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_left));
        imageSwitcher.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_right));
        imageSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_left));
        rating.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_right));
        rating.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_left));
        price.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_right));
        price.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_left));
        cuisines.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_right));
        cuisines.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_left));

        // Update text + image
        Restaurant current = restaurants.get(index);
        name.setText(current.name);
        rating.setText(Integer.toString(current.rating));
        price.setText(current.priceRange);
        cuisines.setText(String.join(", ", current.cuisines));

        Glide.with(this)
                .load(current.image)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(false)
                .into((ImageView) imageSwitcher.getNextView());
        imageSwitcher.showNext();

        int nextIndex = (index + 1) % restaurants.size();
        Glide.with(this)
                .load(restaurants.get(nextIndex).image)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .preload();
    }
}
