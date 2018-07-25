/*
 * Created by Camment OY on 07/25/2018.
 * Copyright (c) 2018 Camment OY. All rights reserved.
 */

package tv.camment.cammentadsdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import tv.camment.cammentads.CMABanner;
import tv.camment.cammentads.CMABannerView;
import tv.camment.cammentads.CMABannerViewListener;
import tv.camment.cammentads.CMACallback;
import tv.camment.cammentads.CMACammentAds;
import tv.camment.cammentads.CMAShowMetadata;

public class BannerDynamicActivity extends AppCompatActivity {

    private static final String EXTRA_BANNER_DISPLAYED = "extra_banner_displayed";

    private FrameLayout flRootView;
    private CMABannerView bannerView;

    private boolean bannerDisplayed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_banner);

        flRootView = findViewById(R.id.fl_root_view);

        findViewById(R.id.btn_get_banner).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestBanner();
            }
        });

        if (savedInstanceState != null
                && savedInstanceState.getBoolean(EXTRA_BANNER_DISPLAYED, false)) {
            addBannerView();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(EXTRA_BANNER_DISPLAYED, bannerDisplayed);
        super.onSaveInstanceState(outState);
    }

    private void requestBanner() {
        CMAShowMetadata showMetadata = new CMAShowMetadata.Builder()
                .setUuid("test_comedy_show")
                .setGenre("comedy")
                .setTitle("friends")
                .setLength(20)
                .setIsLive(true)
                .build();

        CMACammentAds.get(this).getPrerollBannerForShowMetadata(showMetadata, new CMACallback<CMABanner>() {
            @Override
            public void onSuccess(CMABanner banner) {
                if (banner != null) {
                    addBannerView();

                    bannerView.setBanner(banner);
                }
            }

            @Override
            public void onException(Exception exception) {
                Log.e("CammentAd", "exception", exception);
            }
        });
    }

    @Override
    protected void onPause() {
        if (bannerView != null) {
            bannerView.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bannerView != null) {
            bannerView.onResume();
        }
    }

    private void addBannerView() {
        bannerView = new CMABannerView(this);

        bannerView.setListener(new CMABannerViewListener() {
            @Override
            public void onBannerDisplayed() {
                Log.d("CammentAd", "onBannerDisplayed");

                bannerDisplayed = true;
            }

            @Override
            public void onHideBanner() {
                Log.d("CammentAd", "onHideBanner");

                bannerDisplayed = false;

                flRootView.removeView(bannerView);
            }
        });

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);

        flRootView.addView(bannerView, params);
    }

}
