/*
 * Created by Camment OY on 07/19/2018.
 * Copyright (c) 2018 Camment OY. All rights reserved.
 */

package tv.camment.cammentads;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

/**
 * A view displaying image banner ad with properties defined by {@link CMABanner} model
 */
public class CMABannerView extends FrameLayout {

    private ImageView ivAd;
    private ContentLoadingProgressBar clProgressBar;

    private CMABanner banner;
    private int bannerTimeLeft = -1;
    private Handler handler;

    private boolean bannerTimeHandled;

    private CMABannerViewListener bannerViewListener;

    public CMABannerView(@NonNull Context context) {
        this(context, null);
    }

    public CMABannerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CMABannerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.cmmad_banner_view, this);

        handler = new Handler();

        setSaveEnabled(true);

        if (getId() == -1) {
            setId(R.id.cmmad_banner_view_id);
        }

        ivAd = findViewById(R.id.cmmad_iv_ad);
        ivAd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                openRedirectUrl();
            }
        });

        clProgressBar = findViewById(R.id.cmmad_cl_progressbar);
        clProgressBar.getIndeterminateDrawable()
                .setColorFilter(ResourcesCompat.getColor(getResources(), android.R.color.white, context.getTheme()),
                        PorterDuff.Mode.SRC_IN);

        setVisibility(GONE);
    }

    public void onPause() {
        bannerTimeHandled = false;

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    public void onResume() {
        if (banner != null
                && !bannerTimeHandled) {
            handleBannerTime();
        }
    }

    public void setBanner(CMABanner banner) {
        this.banner = banner;

        if (banner != null
                && bannerTimeLeft == -1) {
            bannerTimeLeft = banner.getPrerollDuration() * 1000;
        }

        displayAd();
    }

    public void setListener(CMABannerViewListener bannerViewListener) {
        this.bannerViewListener = bannerViewListener;
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        SavedState ss = new SavedState(superState);
        ss.banner = banner;
        ss.bannerTimeLeft = bannerTimeLeft;

        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        this.banner = ss.banner;
        this.bannerTimeLeft = ss.bannerTimeLeft;

        displayAd();
    }

    private void displayAd() {
        if (banner == null || bannerTimeLeft <= 0) {
            return;
        }

        setVisibility(VISIBLE);

        notifyBannerDisplayedListener();

        clProgressBar.show();

        Glide.with(this).asBitmap().load(banner.getPrerollAssetUrl()).listener(new RequestListener<Bitmap>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                bannerTimeLeft = -1;

                notifyHideBannerListener();

                return true;
            }

            @Override
            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                clProgressBar.hide();

                ivAd.setImageBitmap(resource);

                bannerTimeHandled = true;
                handleBannerTime();

                return true;
            }
        }).submit();
    }

    private void notifyHideBannerListener() {
        if (bannerViewListener != null) {
            bannerViewListener.onHideBanner();
        }
    }

    private void notifyBannerDisplayedListener() {
        if (bannerViewListener != null) {
            bannerViewListener.onBannerDisplayed();
        }
    }

    private void handleBannerTime() {
        if (bannerTimeLeft <= 0) {
            if (getVisibility() == VISIBLE) {
                bannerTimeLeft = -1;

                notifyHideBannerListener();
            }
        } else {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    bannerTimeLeft -= 1000;

                    handleBannerTime();
                }
            }, bannerTimeLeft >= 1000 ? 1000 : bannerTimeLeft);
        }
    }

    static class SavedState extends BaseSavedState {
        CMABanner banner;
        int bannerTimeLeft = -1;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            this.banner = in.readParcelable(CMABanner.class.getClassLoader());
            this.bannerTimeLeft = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeParcelable(banner, 0);
            out.writeInt(bannerTimeLeft);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }


    private void openRedirectUrl() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(banner.getRedirectUrl()));
        getContext().startActivity(intent);
    }

}
