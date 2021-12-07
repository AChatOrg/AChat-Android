package com.hyapp.achat.ui;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.hyapp.achat.R;

import org.greenrobot.eventbus.EventBus;

public class EventActivity extends BaseActivity {

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    protected void alert(@StringRes int titleRes, String message) {
        new AlertDialog.Builder(this, R.style.RoundedCornersDialog)
                .setTitle(titleRes)
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    protected void alert(@StringRes int titleRes, @StringRes int messageRes) {
        alert(titleRes, getString(messageRes));
    }
}
