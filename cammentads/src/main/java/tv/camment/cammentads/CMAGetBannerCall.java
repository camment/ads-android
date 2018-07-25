/*
 * Created by Camment OY on 07/23/2018.
 * Copyright (c) 2018 Camment OY. All rights reserved.
 */

package tv.camment.cammentads;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

/**
 * Async client call to retrieve {@link CMABanner} from Camment API
 */
public class CMAGetBannerCall extends CMAAsyncClient {

    private static final String ENDPOINT = "/ads";

    CMAGetBannerCall(ExecutorService executorService) {
        super(executorService);
    }

    void execute(final String apiKey, final CMAShowMetadata showMetadata, final CMACallback<CMABanner> getBannerCallback) {
        submitTask(new Callable<CMABanner>() {
            @Override
            public CMABanner call() throws Exception {
                String userId = CMAUserIdentity.getInstance().getUserIdentity();

                if (TextUtils.isEmpty(userId)) {
                    Log.w("CMACammentAds", "UserId was null when trying to retrieve preroll banner");
                    return null;
                }

                final String url = getUrlWithParams(CAMMENT_API + ENDPOINT, userId, showMetadata);

                URL myUrl = new URL(url);

                HttpURLConnection conn = (HttpURLConnection) myUrl
                        .openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("x-api-key", apiKey);

                int responseCode = conn.getResponseCode();

                CMABanner banner = null;

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader responseReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    String responseLine;

                    StringBuilder response = new StringBuilder();
                    while ((responseLine = responseReader.readLine()) != null) {
                        response.append(responseLine);
                    }

                    responseReader.close();

                    banner = new Gson().fromJson(response.toString(), CMABanner.class);

                } else {
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));

                    String errorLine;

                    StringBuilder error = new StringBuilder();
                    while ((errorLine = errorReader.readLine()) != null) {
                        error.append(errorLine);
                    }

                    errorReader.close();

                    Log.e("CMACammentAds", "responseCode: " + responseCode + ": " + error.toString());

                    if (responseCode == HttpURLConnection.HTTP_FORBIDDEN) {
                        Log.e("CMACammentAds", "please check your API key");
                    }
                }

                conn.disconnect();

                return banner;
            }
        }, getBannerCallback);
    }

    private String getUrlWithParams(String baseUrl, String userId, CMAShowMetadata metadata) throws UnsupportedEncodingException {
        String url = baseUrl + "?" +
                "userId=" + userId;

        if (metadata == null) {
            return url;
        }

        if (!TextUtils.isEmpty(metadata.getUuid())) {
            url += "&uuid=" + URLEncoder.encode(metadata.getUuid(), "UTF-8");
        }

        if (!TextUtils.isEmpty(metadata.getGenre())) {
            url += "&genre=" + URLEncoder.encode(metadata.getGenre(), "UTF-8");
        }

        if (!TextUtils.isEmpty(metadata.getTitle())) {
            url += "&title=" + URLEncoder.encode(metadata.getTitle(), "UTF-8");
        }

        if (metadata.getLength() > 0) {
            url += "&length=" + metadata.getLength();
        }

        url += "&isLive=" + (metadata.getIsLive() ? 1 : 0);

        return url;
    }

}
