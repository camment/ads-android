# CammentAds for Android
**current version: 1.0.0**

To get started with the CammentAds for Android you can use the existing demo application project, or you can use the library in your existing project. 

The instructions were written for the following configuration:
- Android Studio 3.1.1
- Java 1.8.0_144
- Gradle 3.1.1 (distribution gradle-4.4-all.zip)

## Technical specification
**Android SDK version**
Library is built with the following configuration:
```gradle
minSdkVersion 19
targetSdkVersion 27
compileSdkVersion 27
buildToolsVersion "27.0.3"
supportLibVersion "27.1.0"
```
*Note:* If your application supports also lower Android SDK versions, you have to handle enabling/disabling CammentAds by yourself.

**Dependencies**
CammentAds relies on following dependencies: 
- BumpTech Glide (v4.7.1)
- Google Gson (v2.8.5)

*Note:* If you use some of these dependencies in your application too, you can remove them from your app gradle file. In case you want to override some dependencies, you can do it using gradle.

## Add CammentAds to your project
Library is available on the github in a maven structure, containing 2 important files: 
- CammentAds:
    - ```cammentads-<sdk_version>.aar```
    - ```cammentads-<sdk_version>.pom```

Add following repository url into your **project level** ```build.gradle``` file:
```gradle
allprojects {
    repositories {
        ... // your other repositories
        maven {
            url 'https://raw.githubusercontent.com/camment/ads-android/master/aar/'
        }
    }
}
```
Add following dependencies into your **application level** ```build.gradle``` file:
```gradle
dependencies {
    ... //your other dependencies
    compile ('tv.camment.cammentads:cammentads:<sdk_version>@aar') {
        transitive true
    }
}
```
*Note*: ```transitive true``` means that gradle will download also the library dependencies

Now **sync the project with your gradle files** and **clean the project**. 

## Modify AndroidManifest.xml
*Note: You don't have to do this step if you've already integrated main CammentSDK and thus it's been already added.*

Open your application's (or demo's) ```AndroidManifest.xml``` and specify your API key (security of the API key you have to handle by yourself):
```xml
<application
    ...>
    
    <meta-data
        android:name="tv.camment.cammentsdk.ApiKey"
        android:value="YOUR API KEY" />
       
</application>            
```
## Adding BannerView into your layout
1. Add CMABannerView into your xml layout or dynamically in your code.
- adding into xml:
```xml
<tv.camment.cammentads.CMABannerView
    android:id="@+id/bannerView"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```
- adding dynamically:
```java
CMABannerView bannerView = new CMABannerView(this);

//add according to your ViewGroup and layout hierarchy
FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
flRootView.addView(bannerView, params);
```
2. Notify CMABannerView about Activity/Fragment onPause/onResume lifecycle events:
```java
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
```
3. Set CMABannerViewListener listener to be notified when banner is displayed or should be hidden:
```java
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
```
Example of implementation:
```java
bannerView.setListener(new CMABannerViewListener() {
    @Override
    public void onBannerDisplayed() {
        //do nothing or any actions needed for your code, e.g. pause video, hide view
    }

    @Override
    public void onHideBanner() {
        //remove bannerView or hide it
        flRootView.removeView(bannerView);
        //bannerView.setVisibility(View.GONE);
    }
});
```
*Note: Check implementation of demo app activities ```BannerDynamicActivity``` and ```BannerXmlActivity``` for more information*

## Retrieving CammentAd Banner
Banner is retrieved using method ```getPrerollBannerForShowMetadata(CMAShowMetadata showMetadata, CMACallback<CMABanner> getBannerCallback)```.
```java
//Set CMAShowMetadata fields according to your current video show to get more relevant banner
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
        //banner is null when there is no banner available or an error occurred
        if (banner != null) { 
            bannerView.setBanner(banner);
        }
    }

    @Override
    public void onException(Exception exception) {
        Log.e("CammentAd", "exception", exception);
    }
});
```
*Note: Check implementation of demo app activities ```BannerDynamicActivity``` and ```BannerXmlActivity``` for more information*
