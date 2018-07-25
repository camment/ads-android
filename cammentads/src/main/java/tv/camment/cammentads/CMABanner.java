/*
 * Created by Camment OY on 07/23/2018.
 * Copyright (c) 2018 Camment OY. All rights reserved.
 */

package tv.camment.cammentads;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * A model representing ad banner returned by Camment API
 */
public class CMABanner implements Parcelable {

    private String uuid;
    private String imageUrl;
    private String redirectUrl;
    private int timeToShow;

    public CMABanner() {

    }

    private CMABanner(Parcel in) {
        uuid = in.readString();
        imageUrl = in.readString();
        redirectUrl = in.readString();
        timeToShow = in.readInt();
    }

    public String getUuid() {
        return uuid;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public int getTimeToShow() {
        return timeToShow;
    }

    public static final Creator<CMABanner> CREATOR = new Creator<CMABanner>() {
        @Override
        public CMABanner createFromParcel(Parcel in) {
            return new CMABanner(in);
        }

        @Override
        public CMABanner[] newArray(int size) {
            return new CMABanner[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uuid);
        dest.writeString(imageUrl);
        dest.writeString(redirectUrl);
        dest.writeInt(timeToShow);
    }

}
