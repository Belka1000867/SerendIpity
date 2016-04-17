package com.example.bel.softwarefactory.ui.activities;

import android.support.v7.app.ActionBar;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.example.bel.softwarefactory.R;
import com.example.bel.softwarefactory.api.Api;
import com.example.bel.softwarefactory.preferences.UserLocalStore;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@EActivity(R.layout.activity_splash)
public class SplashScreenActivity extends BaseActivity {

    @Bean
    protected UserLocalStore userLocalStore;

    @ViewById
    protected ImageView logo_imageView;

    @AfterViews
    protected void afterView() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        logo_imageView.startAnimation(animation);

        Api api = new Api();
        api.getAudioRecordsList()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .doOnError((throwable) -> {
                    if (userLocalStore.getAudioRecordsList().isEmpty()) {
                        runOnUiThread(() -> showToast(getString(R.string.something_wrong_cant_start)));
                        finish();
                    } else {
                        proceedToNextActivity();
                    }
                })
                .subscribe(audioRecordEntities -> {
                    userLocalStore.saveAudioRecordsList(audioRecordEntities);
                    proceedToNextActivity();
                });
    }

    private void proceedToNextActivity() {
        boolean serendipityUser = userLocalStore.isUserLoggedIn() && userLocalStore.isRememberMe();
        if (serendipityUser || userLocalStore.isFacebookLoggedIn()) {
            MenuActivity_.intent(SplashScreenActivity.this).start();
        } else {
            FirstActivity_.intent(SplashScreenActivity.this).start();
        }
        finish();
    }
}
