/*
 * Created by Camment OY on 07/20/2018.
 * Copyright (c) 2018 Camment OY. All rights reserved.
 */

package tv.camment.cammentads;

/**
 * A listener for {@link CMABannerView} actions
 */
public interface CMABannerViewListener {

    /**
     * Called to notify the host app the banner was displayed
     */
    void onBannerDisplayed();

    /**
     * Called to notify the host app the banner should be hidden and can be removed
     */
    void onHideBanner();

}
