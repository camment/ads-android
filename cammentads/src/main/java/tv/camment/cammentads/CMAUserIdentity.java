/*
 * Created by Camment OY on 07/24/2018.
 * Copyright (c) 2018 Camment OY. All rights reserved.
 */

package tv.camment.cammentads;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.UUID;

import tv.camment.cammentsdk.helpers.IdentityPreferences;

/**
 * Helper class to retrieve current user's identity.
 * Retrieves it from CammentSDK if available or generates random one if needed.
 */
public class CMAUserIdentity {

    private static final String PREFS_NAME = "camment_ad_prefs";

    private static final String PREFS_AD_USER_ID = "prefs_ad_user_id";

    private static CMAUserIdentity INSTANCE;

    private SharedPreferences prefs;

    public static CMAUserIdentity getInstance() {
        if (INSTANCE == null) {
            synchronized (CMAUserIdentity.class) {
                if (INSTANCE == null) {
                    INSTANCE = new CMAUserIdentity();
                }
            }
        }
        return INSTANCE;
    }

    private CMAUserIdentity() {

    }

    void initPrefs(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, 0);
    }

    String getUserIdentity() {
        if (hasCammentSDKOnClasspath()) {
            return IdentityPreferences.getInstance().getIdentityId();
        }
        return generateIdentityId();
    }

    private boolean hasCammentSDKOnClasspath() {
        try {
            Class.forName("tv.camment.cammentsdk.CammentSDK");
            return true;
        } catch (ClassNotFoundException e) {
            //intentionally left empty
        }
        return false;
    }

    private String generateIdentityId() {
        if (prefs != null) {
            String userId = prefs.getString(PREFS_AD_USER_ID, "");
            if (TextUtils.isEmpty(userId)) {
                userId = UUID.randomUUID().toString();
                prefs.edit()
                        .putString(PREFS_AD_USER_ID, userId)
                        .apply();
            }
            return userId;
        }
        return null;
    }

}
