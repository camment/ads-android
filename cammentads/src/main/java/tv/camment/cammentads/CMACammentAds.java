/*
 * Created by Camment OY on 07/19/2018.
 * Copyright (c) 2018 Camment OY. All rights reserved.
 */

package tv.camment.cammentads;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.concurrent.Executors;

/**
 * A singleton to present a simple static interface for retrieving Camment preroll banner
 */
public class CMACammentAds {

    private static CMACammentAds INSTANCE;

    private String apiKey;

    @NonNull
    public static CMACammentAds get(@NonNull Context context) {
        if (INSTANCE == null) {
            synchronized (CMACammentAds.class) {
                if (INSTANCE == null) {
                    INSTANCE = new CMACammentAds(context);
                }
            }
        }

        return INSTANCE;
    }

    private CMACammentAds(Context context) {
        setApiKey(context);

        CMAUserIdentity.getInstance().initPrefs(context);
    }

    public void getPrerollBannerForShowMetadata(CMAShowMetadata showMetadata, CMACallback<CMABanner> getBannerCallback) {
        new CMAGetBannerCall(Executors.newSingleThreadExecutor()).execute(apiKey, showMetadata, getBannerCallback);
    }

    private void setApiKey(final Context context) {
        try {
            ApplicationInfo ai = context.getApplicationContext().getPackageManager()
                    .getApplicationInfo(context.getApplicationContext().getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            apiKey = bundle.getString("tv.camment.cammentsdk.ApiKey");
        } catch (PackageManager.NameNotFoundException | NullPointerException e) {
            throw new IllegalArgumentException("Missing CammentAds API key");
        }
    }

}
