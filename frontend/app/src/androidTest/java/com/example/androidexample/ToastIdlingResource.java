package com.example.androidexample;

import android.view.WindowManager;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.Root;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Simple IdlingResource that waits until a Toast window appears.
 */
public class ToastIdlingResource implements IdlingResource {
    private ResourceCallback resourceCallback;
    private final AtomicBoolean isIdle = new AtomicBoolean(false);

    @Override
    public String getName() {
        return ToastIdlingResource.class.getName();
    }

    @Override
    public boolean isIdleNow() {
        return isIdle.get();
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) {
        this.resourceCallback = callback;
    }

    // Call this when Toast is detected
    public void onToastShown() {
        isIdle.set(true);
        if (resourceCallback != null) {
            resourceCallback.onTransitionToIdle();
        }
    }
}
