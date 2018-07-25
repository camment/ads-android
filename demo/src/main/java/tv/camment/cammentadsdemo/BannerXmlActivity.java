/*
 * Created by Camment OY on 07/25/2018.
 * Copyright (c) 2018 Camment OY. All rights reserved.
 */

package tv.camment.cammentadsdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import tv.camment.cammentads.CMABanner;
import tv.camment.cammentads.CMABannerView;
import tv.camment.cammentads.CMABannerViewListener;
import tv.camment.cammentads.CMACallback;
import tv.camment.cammentads.CMACammentAds;
import tv.camment.cammentads.CMAShowMetadata;

public class BannerXmlActivity extends AppCompatActivity {

    private CMABannerView bannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xml_banner);

        bannerView = findViewById(R.id.bannerView);

        bannerView.setListener(new CMABannerViewListener() {
            @Override
            public void onBannerDisplayed() {
                Log.d("CammentAd", "onBannerDisplayed");
            }

            @Override
            public void onHideBanner() {
                Log.d("CammentAd", "onHideBanner");
                bannerView.setVisibility(View.GONE);
            }
        });

        if (savedInstanceState == null) {
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
                        BannerXmlActivity.this.bannerView.setBanner(banner);
                    }
                }

                @Override
                public void onException(Exception exception) {
                    Log.e("CammentAd", "exception", exception);
                }
            });
        }
    }

    @Override
    protected void onPause() {
        bannerView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bannerView.onResume();
    }

}
