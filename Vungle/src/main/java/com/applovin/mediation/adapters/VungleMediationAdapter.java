package com.applovin.mediation.adapters;

import static com.applovin.sdk.AppLovinSdkUtils.runOnUiThread;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.applovin.mediation.MaxAdFormat;
import com.applovin.mediation.MaxReward;
import com.applovin.mediation.adapter.MaxAdViewAdapter;
import com.applovin.mediation.adapter.MaxAdapterError;
import com.applovin.mediation.adapter.MaxAppOpenAdapter;
import com.applovin.mediation.adapter.MaxInterstitialAdapter;
import com.applovin.mediation.adapter.MaxNativeAdAdapter;
import com.applovin.mediation.adapter.MaxRewardedAdapter;
import com.applovin.mediation.adapter.MaxSignalProvider;
import com.applovin.mediation.adapter.listeners.MaxAdViewAdapterListener;
import com.applovin.mediation.adapter.listeners.MaxAppOpenAdapterListener;
import com.applovin.mediation.adapter.listeners.MaxInterstitialAdapterListener;
import com.applovin.mediation.adapter.listeners.MaxNativeAdAdapterListener;
import com.applovin.mediation.adapter.listeners.MaxRewardedAdapterListener;
import com.applovin.mediation.adapter.listeners.MaxSignalCollectionListener;
import com.applovin.mediation.adapter.parameters.MaxAdapterInitializationParameters;
import com.applovin.mediation.adapter.parameters.MaxAdapterParameters;
import com.applovin.mediation.adapter.parameters.MaxAdapterResponseParameters;
import com.applovin.mediation.adapter.parameters.MaxAdapterSignalCollectionParameters;
import com.applovin.mediation.adapters.vungle.BuildConfig;
import com.applovin.mediation.nativeAds.MaxNativeAd;
import com.applovin.mediation.nativeAds.MaxNativeAdView;
import com.applovin.sdk.AppLovinPrivacySettings;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkConfiguration;
import com.applovin.sdk.AppLovinSdkUtils;
import com.vungle.ads.AdConfig;
import com.vungle.ads.AdSize;
import com.vungle.ads.BannerAd;
import com.vungle.ads.BannerView;
import com.vungle.ads.BaseAd;
import com.vungle.ads.BaseAdListener;
import com.vungle.ads.InitializationListener;
import com.vungle.ads.InterstitialAd;
import com.vungle.ads.NativeAd;
import com.vungle.ads.NativeAdListener;
import com.vungle.ads.Plugin;
import com.vungle.ads.RewardedAd;
import com.vungle.ads.RewardedAdListener;
import com.vungle.ads.VungleAds;
import com.vungle.ads.VungleException;
import com.vungle.ads.VungleSettings;
import com.vungle.ads.internal.network.VungleApiClient;
import com.vungle.ads.internal.privacy.PrivacyConsent;
import com.vungle.ads.internal.ui.view.MediaView;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class VungleMediationAdapter
        extends MediationAdapterBase
        implements MaxSignalProvider, MaxInterstitialAdapter, MaxRewardedAdapter, MaxAdViewAdapter, MaxNativeAdAdapter {
    private static final AtomicBoolean initialized = new AtomicBoolean();
    private static InitializationStatus status;

    private BannerAd bannerAd;
    private VungleNativeAd vungleMaxNativeAd;
    private InterstitialAd interstitialAd;
    private RewardedAd rewardedAd;
    private InterstitialAd appOpenAd;

    // Explicit default constructor declaration
    public VungleMediationAdapter(final AppLovinSdk sdk) {
        super(sdk);
    }

    @Override
    public void initialize(final MaxAdapterInitializationParameters parameters, final Activity activity, final OnCompletionListener onCompletionListener) {
        updateUserPrivacySettings(parameters);
        AppLovinPrivacySettings.setDoNotSell(false, getApplicationContext());
        if (initialized.compareAndSet(false, true)) {
            String appId = parameters.getServerParameters().getString("app_id", null);
            log("Initializing Vungle SDK with app id: " + appId + "...");

            status = InitializationStatus.INITIALIZING;

            Plugin.addWrapperInfo(Plugin.WrapperFramework.max, getAdapterVersion());

            // NOTE: `activity` can only be null in 11.1.0+, and `getApplicationContext()` is introduced in 11.1.0
            Context context = (activity != null) ? activity.getApplicationContext() : getApplicationContext();

            updateUserPrivacySettings(parameters);

            // Note: Vungle requires the Application Context
            AppLovinPrivacySettings.setDoNotSell(false, context);
            VungleAds.init(context, appId, new InitializationListener() {
                @Override
                public void onSuccess() {
                    log("Vungle SDK initialized");

                    status = InitializationStatus.INITIALIZED_SUCCESS;
                    onCompletionListener.onCompletion(status, null);
                }

                @Override
                public void onError(@NonNull VungleException vungleException) {
                    log("Vungle SDK failed to initialize with error: ", vungleException);

                    status = InitializationStatus.INITIALIZED_FAILURE;
                    onCompletionListener.onCompletion(status, vungleException.getLocalizedMessage());
                }
            }, new VungleSettings());
        } else {
            log("Vungle SDK already initialized");
            onCompletionListener.onCompletion(status, null);
        }

    }

    @Override
    public String getSdkVersion() {
        return getVersionString(com.vungle.ads.BuildConfig.class, "VERSION_NAME");
    }

    @Override
    public String getAdapterVersion() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public void onDestroy() {
        if (bannerAd != null) {
            bannerAd.finishAd();
            bannerAd = null;
        }

        if (vungleMaxNativeAd != null) {
            vungleMaxNativeAd.destroyAd();
            vungleMaxNativeAd = null;
        }
        interstitialAd = null;
        rewardedAd = null;
    }

    //region Signal Collection

    @Override
    public void collectSignal(final MaxAdapterSignalCollectionParameters parameters, final Activity activity, final MaxSignalCollectionListener callback) {
        log("Collecting signal...");

        updateUserPrivacySettings(parameters);

        String signal = VungleAds.getBiddingToken();
        log(signal);
        callback.onSignalCollected(signal);
    }

    //endregion

    //region MaxInterstitialAdapter

    @Override
    public void loadInterstitialAd(final MaxAdapterResponseParameters parameters, final Activity activity, final MaxInterstitialAdapterListener listener) {
        String bidResponse = parameters.getBidResponse();
        boolean isBiddingAd = AppLovinSdkUtils.isValidString(bidResponse);
        String placementId = parameters.getThirdPartyAdPlacementId();
        log("Loading " + (isBiddingAd ? "bidding " : "") + "interstitial ad for placement: " + placementId + "...");

        if (!VungleAds.isInitialized()) {
            log("Vungle SDK not successfully initialized: failing interstitial ad load...");
            listener.onInterstitialAdLoadFailed(MaxAdapterError.NOT_INITIALIZED);

            return;
        }

        AdConfig adConfig = createAdConfig(parameters.getServerParameters());
        interstitialAd = new InterstitialAd(placementId, adConfig);
        interstitialAd.setAdListener(new BaseAdListener() {

            @Override
            public void adLoaded(@NonNull BaseAd baseAd) {
                log("Interstitial ad loaded");
                listener.onInterstitialAdLoaded();
            }

            @Override
            public void adStart(@NonNull BaseAd baseAd) {
                log("Interstitial ad started");
            }

            @Override
            public void adImpression(@NonNull BaseAd baseAd) {
                log("Interstitial ad displayed");
                String creativeId = baseAd.getCreativeId();
                // Passing extra info such as creative id supported in 9.15.0+
                if (AppLovinSdk.VERSION_CODE >= 9150000 && AppLovinSdkUtils.isValidString(creativeId)) {
                    Bundle extraInfo = new Bundle(1);

                    extraInfo.putString("creative_id", creativeId);

                    listener.onInterstitialAdDisplayed(extraInfo);
                } else {
                    listener.onInterstitialAdDisplayed();
                }
            }


            @Override
            public void adEnd(@NonNull BaseAd baseAd) {
                log("Interstitial ad hidden");
                listener.onInterstitialAdHidden();
            }

            @Override
            public void adClick(@NonNull BaseAd baseAd) {
                log("Interstitial ad clicked");
                listener.onInterstitialAdClicked();
            }

            @Override
            public void onAdLeftApplication(@NonNull BaseAd baseAd) {
                log("Interstitial ad left application");
            }

            @Override
            public void error(@NonNull BaseAd baseAd, @NonNull VungleException adError) {
                MaxAdapterError error = toMaxError(adError);
                log("Interstitial ad for placement " + placementId + " failed to load with error: " + error);
                listener.onInterstitialAdLoadFailed(error);
            }
        });

        String adMarkup = getAdMarkup(parameters);
        if (interstitialAd.canPlayAd()) {
            log("Interstitial ad loaded");
            listener.onInterstitialAdLoaded();
            return;
        }
        interstitialAd.load(adMarkup);
    }

    @Override
    public void showInterstitialAd(final MaxAdapterResponseParameters parameters, final Activity activity, final MaxInterstitialAdapterListener listener)
    {
        if (interstitialAd != null && interstitialAd.canPlayAd())
        {
            String bidResponse = parameters.getBidResponse();
            boolean isBiddingAd = AppLovinSdkUtils.isValidString(bidResponse);
            String placementId = parameters.getThirdPartyAdPlacementId();
            log("Showing " + (isBiddingAd ? "bidding " : "") + "interstitial ad for placement: " + placementId + "...");
            interstitialAd.play();
        } else {
            log("Interstitial ad not ready");
            listener.onInterstitialAdDisplayFailed(new MaxAdapterError(-4205, "Ad Display Failed"));
        }
    }
    //endregion

    //region MaxAppOpenAdapter
    public void loadAppOpenAd(final MaxAdapterResponseParameters parameters, @Nullable final Activity activity, final MaxAppOpenAdapterListener listener)
    {
        String bidResponse = parameters.getBidResponse();
        boolean isBiddingAd = AppLovinSdkUtils.isValidString(bidResponse);
        String placementId = parameters.getThirdPartyAdPlacementId();
        log("Loading " + (isBiddingAd ? "bidding " : "") + "app open ad for placement: " + placementId + "...");

        if (!VungleAds.isInitialized())
        {
            log("Vungle SDK not successfully initialized: failing app open ad load...");
            listener.onAppOpenAdLoadFailed(MaxAdapterError.NOT_INITIALIZED);

            return;
        }

        updateUserPrivacySettings( parameters );

        AdConfig adConfig = createAdConfig(parameters.getServerParameters());
        appOpenAd = new InterstitialAd(placementId, adConfig);
        appOpenAd.setAdListener(new BaseAdListener()
        {

            @Override
            public void adLoaded(@NonNull BaseAd baseAd) {
                log("Interstitial ad loaded");
                listener.onAppOpenAdLoaded();
            }

            @Override
            public void adStart(@NonNull BaseAd baseAd) {
                log("Interstitial ad started");
            }

            @Override
            public void adImpression(@NonNull BaseAd baseAd)
            {
                log("Interstitial ad displayed");
                String creativeId = baseAd.getCreativeId();
                // Passing extra info such as creative id supported in 9.15.0+
                if (AppLovinSdk.VERSION_CODE >= 9150000 && AppLovinSdkUtils.isValidString(creativeId))
                {
                    Bundle extraInfo = new Bundle(1);

                    extraInfo.putString("creative_id", creativeId);

                    listener.onAppOpenAdDisplayed(extraInfo);
                } else {
                    listener.onAppOpenAdDisplayed();
                }
            }


            @Override
            public void adEnd(@NonNull BaseAd baseAd)
            {
                log("Interstitial ad hidden");
                listener.onAppOpenAdHidden();
            }

            @Override
            public void adClick(@NonNull BaseAd baseAd)
            {
                log("Interstitial ad clicked");
                listener.onAppOpenAdClicked();
            }

            @Override
            public void onAdLeftApplication(@NonNull BaseAd baseAd)
            {
                log("Interstitial ad left application");
            }

            @Override
            public void error(@NonNull BaseAd baseAd, @NonNull VungleException adError)
            {
                MaxAdapterError error = toMaxError(adError);
                log("Interstitial ad for placement " + placementId + " failed to load with error: " + error);
                listener.onAppOpenAdLoadFailed(error);
            }
        });

        String adMarkup = getAdMarkup(parameters);
        if (interstitialAd.canPlayAd())
        {
            log("Interstitial ad loaded");
            listener.onAppOpenAdLoaded();
            return;
        }
        interstitialAd.load(adMarkup);
    }

    public void showAppOpenAd(final MaxAdapterResponseParameters parameters, @Nullable final Activity activity, final MaxAppOpenAdapterListener listener)
    {
        if (appOpenAd != null && appOpenAd.canPlayAd()) {
            String bidResponse = parameters.getBidResponse();
            boolean isBiddingAd = AppLovinSdkUtils.isValidString(bidResponse);
            String placementId = parameters.getThirdPartyAdPlacementId();
            log("Showing " + (isBiddingAd ? "bidding " : "") + "app open ad for placement: " + placementId + "...");

            appOpenAd.play();
        } else {
            log("App open ad not ready");
            listener.onAppOpenAdDisplayFailed(new MaxAdapterError(-4205, "Ad Display Failed"));
        }
        listener.onAppOpenAdDisplayFailed(new MaxAdapterError(-4205, "Ad Display Failed"));
    }
    //endregion
        //region MaxRewardedAdapter

        @Override
        public void loadRewardedAd ( final MaxAdapterResponseParameters parameters,
        final Activity activity, final MaxRewardedAdapterListener listener)
        {
            String bidResponse = parameters.getBidResponse();
            String placementId = parameters.getThirdPartyAdPlacementId();

            if (!VungleAds.isInitialized()) {
                log("Vungle SDK not successfully initialized: failing rewarded ad load...");
                listener.onRewardedAdLoadFailed(MaxAdapterError.NOT_INITIALIZED);

                return;
            }
            updateUserPrivacySettings(parameters);
            AdConfig adConfig = createAdConfig(parameters.getServerParameters());
            rewardedAd = new RewardedAd(placementId, adConfig);
            rewardedAd.setAdListener(new RewardedAdListener() {
                boolean hasGrantedReward = false;

                @Override
                public void adLoaded(@NonNull BaseAd baseAd) {
                    log("Rewarded ad loaded");
                    listener.onRewardedAdLoaded();
                }

                @Override
                public void adStart(@NonNull BaseAd baseAd) {
                    log("Rewarded ad started");
                }

                @Override
                public void adRewarded(@NonNull BaseAd baseAd) {
                    log("Rewarded ad user did earn reward");
                    hasGrantedReward = true;
                }

                @Override
                public void adImpression(@NonNull BaseAd baseAd) {
                    log("Rewarded ad displayed");
                    String creativeId = baseAd.getCreativeId();
                    // Passing extra info such as creative id supported in 9.15.0+
                    if (AppLovinSdk.VERSION_CODE >= 9150000 && AppLovinSdkUtils.isValidString(creativeId)) {
                        Bundle extraInfo = new Bundle(1);

                        extraInfo.putString("creative_id", creativeId);

                        listener.onRewardedAdDisplayed(extraInfo);
                    } else {
                        listener.onRewardedAdDisplayed();
                    }
                }


                @Override
                public void adEnd(@NonNull BaseAd baseAd) {
                    log("Rewarded ad video completed");
                    listener.onRewardedAdVideoCompleted();

                    if (hasGrantedReward || shouldAlwaysRewardUser()) {
                        final MaxReward reward = getReward();
                        log("Rewarded user with reward: " + reward);
                        listener.onUserRewarded(reward);
                    }

                    log("Rewarded ad hidden");
                    listener.onRewardedAdHidden();
                }

                @Override
                public void adClick(@NonNull BaseAd baseAd) {
                    log("Rewarded ad clicked");
                    listener.onRewardedAdClicked();
                }

                @Override
                public void onAdLeftApplication(@NonNull BaseAd baseAd) {
                    log("Rewarded ad left application");
                }

                @Override
                public void error(@NonNull BaseAd baseAd, @NonNull VungleException adError) {
                    MaxAdapterError error = toMaxError(adError);
                    log("Rewarded ad for placement " + placementId + " failed to load with error: " + error);
                    listener.onRewardedAdLoadFailed(error);
                }
            });

            if (rewardedAd.canPlayAd()) {
                log("Rewarded ad loaded");
                listener.onRewardedAdLoaded();
                return;
            }
            boolean isBiddingAd = AppLovinSdkUtils.isValidString(bidResponse);
            log("Loading " + (isBiddingAd ? "bidding " : "") + "rewarded ad for placement: " + placementId + "...");
            String adMarkup = getAdMarkup(parameters);
            rewardedAd.load(adMarkup);
        }

        @Override
        public void showRewardedAd ( final MaxAdapterResponseParameters parameters,
        final Activity activity, final MaxRewardedAdapterListener listener)
        {
            String bidResponse = parameters.getBidResponse();
            boolean isBiddingAd = AppLovinSdkUtils.isValidString(bidResponse);
            String placementId = parameters.getThirdPartyAdPlacementId();
            log("Showing " + (isBiddingAd ? "bidding " : "") + "rewarded ad for placement: " + placementId + "...");


            if (rewardedAd != null && rewardedAd.canPlayAd()) {
                configureReward(parameters);
                rewardedAd.play();
            } else {
                log("Rewarded ad not ready");
                listener.onRewardedAdDisplayFailed(new MaxAdapterError(-4205, "Ad Display Failed"));
            }
        }

        //endregion

        //region MaxAdViewAdapter

        @Override
        public void loadAdViewAd ( final MaxAdapterResponseParameters parameters,
        final MaxAdFormat adFormat, final Activity activity, final MaxAdViewAdapterListener listener)
        {
            String bidResponse = parameters.getBidResponse();
            final Bundle serverParameters = parameters.getServerParameters();

            final String adFormatLabel = adFormat.getLabel();
            String placementId = parameters.getThirdPartyAdPlacementId();

            if (!VungleAds.isInitialized()) {
                log("Vungle SDK not successfully initialized: failing " + adFormatLabel + " ad load...");
                listener.onAdViewAdLoadFailed(MaxAdapterError.NOT_INITIALIZED);

                return;
            }
            final AdConfig adConfig = new AdConfig();
            AdSize adSize = vungleAdSize(adFormat);
            adConfig.setAdSize(adSize);

            if (serverParameters.containsKey("is_muted")) {
                // TODO:
                //  adConfig.setMuted( serverParameters.getBoolean( "is_muted" ) );
            }
            bannerAd = new BannerAd(getApplicationContext(), placementId, adConfig);
            bannerAd.setAdListener(new BaseAdListener() {

                @Override
                public void adLoaded(@NonNull BaseAd baseAd) {
                    showAdViewAd(adFormat, parameters, listener);
                }

                @Override
                public void adStart(@NonNull BaseAd baseAd) {
                    log(adFormatLabel + " ad started");
                }

                @Override
                public void adImpression(@NonNull BaseAd baseAd) {
                    log(adFormatLabel + " ad displayed");

                    String creativeId = baseAd.getCreativeId();
                    // Passing extra info such as creative id supported in 9.15.0+
                    if (AppLovinSdk.VERSION_CODE >= 9150000 && AppLovinSdkUtils.isValidString(creativeId)) {
                        Bundle extraInfo = new Bundle(1);
                        extraInfo.putString("creative_id", creativeId);

                        listener.onAdViewAdDisplayed(extraInfo);
                    } else {
                        listener.onAdViewAdDisplayed();
                    }
                }


                @Override
                public void adEnd(@NonNull BaseAd baseAd) {
                    log(adFormatLabel + " ad hidden");
                    listener.onAdViewAdHidden();
                }

                @Override
                public void adClick(@NonNull BaseAd baseAd) {
                    log(adFormatLabel + " ad clicked");
                    listener.onAdViewAdClicked();
                }

                @Override
                public void onAdLeftApplication(@NonNull BaseAd baseAd) {
                    log(adFormatLabel + " ad left application");
                }

                @Override
                public void error(@NonNull BaseAd baseAd, @NonNull VungleException adError) {
                    MaxAdapterError error = toMaxError(adError);
                    log(adFormatLabel + " ad display failed with error: " + error);
                    listener.onAdViewAdDisplayFailed(error);
                }
            });

            if (bannerAd.canPlayAd()) {
                log("Banner ad loaded");
                showAdViewAd(adFormat, parameters, listener);
                return;
            }
            String adMarkup = getAdMarkup(parameters);
            boolean isBiddingAd = AppLovinSdkUtils.isValidString(bidResponse);
            log("Loading " + (isBiddingAd ? "bidding " : "") + adFormatLabel + " ad for placement: " + placementId + "...");
            bannerAd.load(adMarkup);
        }

        private void showAdViewAd ( final MaxAdFormat adFormat,
        final MaxAdapterResponseParameters parameters,
        final MaxAdViewAdapterListener listener)
        {
            String bidResponse = parameters.getBidResponse();
            boolean isBiddingAd = AppLovinSdkUtils.isValidString(bidResponse);
            String adFormatLabel = adFormat.getLabel();
            String placementId = parameters.getThirdPartyAdPlacementId();
            log("Showing " + (isBiddingAd ? "bidding " : "") + adFormatLabel + " ad for placement: " + placementId + "...");

            if (bannerAd != null && bannerAd.getBannerView() != null) {
                log(adFormatLabel + " ad loaded");
                View bannerView = bannerAd.getBannerView();
                ((BannerView) bannerView).setGravity(Gravity.CENTER);
                listener.onAdViewAdLoaded(bannerView);
            } else {
                MaxAdapterError error = MaxAdapterError.INVALID_LOAD_STATE;
                log(adFormatLabel + " ad failed to load: " + error);
                listener.onAdViewAdLoadFailed(error);
            }
        }

        @Override
        public void loadNativeAd (MaxAdapterResponseParameters
        maxAdapterResponseParameters, Activity activity, MaxNativeAdAdapterListener
        maxNativeAdAdapterListener){
            final String placementId = maxAdapterResponseParameters.getThirdPartyAdPlacementId();
            vungleMaxNativeAd = new VungleNativeAd(activity, placementId, maxNativeAdAdapterListener);
            String adMarkup = getAdMarkup(maxAdapterResponseParameters);
            vungleMaxNativeAd.loadAd(adMarkup);
        }
        //endregion

        //region Helper Methods
        private AdConfig createAdConfig(final Bundle serverParameters)
        {
            final AdConfig config = new AdConfig();
            if (serverParameters.containsKey("ordinal")) {
//            config.setOrdinal( serverParameters.getInt( "ordinal" ) );
            }

            if (serverParameters.containsKey("immersive_mode")) {
//            config.setImmersiveMode( serverParameters.getBoolean( "immersive_mode" ) );
            }

            // Overwritten by `mute_state` setting, unless `mute_state` is disabled
            if (serverParameters.containsKey("is_muted")) // Introduced in 9.10.0
            {
//            config.setMuted( serverParameters.getBoolean( "is_muted" ) );
            }

            if (serverParameters.containsKey("app_orientation")) {
                // 0 = PORTRAIT, 1 = LANDSCAPE, 2 = ALL/AUTO_ROTATE
                int orientation = serverParameters.getInt("app_orientation");
                config.setAdOrientation(orientation);
            }

            return config;
        }

        private void updateUserPrivacySettings ( final MaxAdapterParameters parameters)
        {
            if (getWrappingSdk().getConfiguration().getConsentDialogState() == AppLovinSdkConfiguration.ConsentDialogState.APPLIES) {
                Boolean hasUserConsent = getPrivacySetting("hasUserConsent", parameters);
                if (hasUserConsent != null) {
                    PrivacyConsent consentStatus = hasUserConsent ? PrivacyConsent.OPT_IN : PrivacyConsent.OPT_OUT;
                    VungleAds.updateGDPRConsent(consentStatus, "");
                }
            }

            if (AppLovinSdk.VERSION_CODE >= 91100) {
                Boolean isDoNotSell = getPrivacySetting("isDoNotSell", parameters);
                if (isDoNotSell != null) {
                    PrivacyConsent ccpaStatus = isDoNotSell ? PrivacyConsent.OPT_OUT : PrivacyConsent.OPT_IN;
                    VungleAds.updateCCPAStatus(ccpaStatus);
                }
            }

            Boolean isAgeRestrictedUser = getPrivacySetting("isAgeRestrictedUser", parameters);

            if (isAgeRestrictedUser != null && !VungleAds.isInitialized()) {
                VungleAds.updateUserCoppaStatus(isAgeRestrictedUser);
            }
        }

        private Boolean getPrivacySetting ( final String privacySetting, final MaxAdapterParameters parameters)
        {
            try {
                // Use reflection because compiled adapters have trouble fetching `boolean` from old SDKs and `Boolean` from new SDKs (above 9.14.0)
                Class<?> parametersClass = parameters.getClass();
                Method privacyMethod = parametersClass.getMethod(privacySetting);
                return (Boolean) privacyMethod.invoke(parameters);
            } catch (Exception exception) {
                log("Error getting privacy setting " + privacySetting + " with exception: ", exception);
                return (AppLovinSdk.VERSION_CODE >= 9140000) ? null : false;
            }
        }

        private static AdSize vungleAdSize ( final MaxAdFormat adFormat)
        {
            if (adFormat == MaxAdFormat.BANNER) {
                return AdSize.BANNER;
            } else if (adFormat == MaxAdFormat.LEADER) {
                return AdSize.BANNER_LEADERBOARD;
            } else if (adFormat == MaxAdFormat.MREC) {
                return AdSize.VUNGLE_MREC;
            } else {
                throw new IllegalArgumentException("Unsupported ad view ad format: " + adFormat.getLabel());
            }
        }

        private static MaxAdapterError toMaxError ( final VungleException vungleError)
        {
            final int vungleErrorCode = vungleError.getExceptionCode();
            MaxAdapterError adapterError = MaxAdapterError.UNSPECIFIED;
            switch (vungleErrorCode) {
                case VungleException.NO_SERVE:
                    adapterError = MaxAdapterError.NO_FILL;
                    break;
                case VungleException.UNKNOWN_ERROR:
                    adapterError = MaxAdapterError.UNSPECIFIED;
                    break;
                case VungleException.CONFIGURATION_ERROR:
                case VungleException.INCORRECT_BANNER_API_USAGE:
                case VungleException.INCORRECT_DEFAULT_API_USAGE:
                case VungleException.INVALID_SIZE:
                case VungleException.MISSING_HBP_EVENT_ID:
                case VungleException.NETWORK_PERMISSIONS_NOT_GRANTED:
                case VungleException.NO_AUTO_CACHED_PLACEMENT:
                case VungleException.NO_SPACE_TO_DOWNLOAD_ASSETS:
                case VungleException.NO_SPACE_TO_LOAD_AD:
                case VungleException.NO_SPACE_TO_LOAD_AD_AUTO_CACHED:
                case VungleException.PLACEMENT_NOT_FOUND:
                case VungleException.SDK_VERSION_BELOW_REQUIRED_VERSION:
                case VungleException.UNSUPPORTED_CONFIGURATION:
                    adapterError = MaxAdapterError.INVALID_CONFIGURATION;
                    break;
                case VungleException.AD_EXPIRED:
                case VungleException.AD_PAST_EXPIRATION:
                    adapterError = MaxAdapterError.AD_EXPIRED;
                    break;
                case VungleException.APPLICATION_CONTEXT_REQUIRED:
                case VungleException.MISSING_REQUIRED_ARGUMENTS_FOR_INIT:
                case VungleException.NO_SPACE_TO_INIT:
                case VungleException.VUNGLE_NOT_INTIALIZED:
                    adapterError = MaxAdapterError.NOT_INITIALIZED;
                    break;
                case VungleException.AD_UNABLE_TO_PLAY:
                case VungleException.OPERATION_CANCELED:
                    adapterError = MaxAdapterError.INTERNAL_ERROR;
                    break;
                case VungleException.AD_FAILED_TO_DOWNLOAD:
                case VungleException.AD_RENDER_NETWORK_ERROR:
                case VungleException.ASSET_DOWNLOAD_ERROR:
                case VungleException.ASSET_DOWNLOAD_RECOVERABLE:
                case VungleException.NETWORK_ERROR:
                case VungleException.NETWORK_UNREACHABLE:
                    adapterError = MaxAdapterError.NO_CONNECTION;
                    break;
                case VungleException.DB_ERROR:
                case VungleException.SERVER_RETRY_ERROR:
                case VungleException.SERVER_ERROR:
                case VungleException.SERVER_TEMPORARY_UNAVAILABLE:
                    adapterError = MaxAdapterError.SERVER_ERROR;
                    break;
                case VungleException.ALREADY_PLAYING_ANOTHER_AD:
                case VungleException.OPERATION_ONGOING:
                    adapterError = MaxAdapterError.INVALID_LOAD_STATE;
                    break;
                case VungleException.RENDER_ERROR:
                case VungleException.WEBVIEW_RENDER_UNRESPONSIVE:
                case VungleException.WEB_CRASH:
                    adapterError = MaxAdapterError.WEBVIEW_ERROR;
                    break;
            }

            return new MaxAdapterError(adapterError.getErrorCode(), adapterError.getErrorMessage(), vungleErrorCode, vungleError.getLocalizedMessage());
        }
        //endregion
        class VungleNativeAd {
            private NativeAd nativeAd;
            public MediaView mediaView;
            private ImageView iconView;
            private VungleMaxNativeAd vungleMaxNativeAd;

            public VungleNativeAd(Activity activity, String placementId, MaxNativeAdAdapterListener maxNativeAdAdapterListener) {
                AdConfig adConfig = new AdConfig();
                nativeAd = new NativeAd(activity, placementId, adConfig);
                mediaView = new MediaView(activity);
                iconView = new ImageView(activity);
                nativeAd.setAdListener(new NativeAdListener() {
                    @Override
                    public void adLoaded(@NonNull BaseAd baseAd) {
                        runOnUiThread(() -> {
                            NativeAd ad = (NativeAd) baseAd;
                            vungleMaxNativeAd = new VungleMaxNativeAd(mediaView, ad, iconView);
                            maxNativeAdAdapterListener.onNativeAdLoaded(vungleMaxNativeAd, null);
                        });
                    }

                    @Override
                    public void adStart(@NonNull BaseAd baseAd) {

                    }

                    @Override
                    public void adImpression(@NonNull BaseAd baseAd) {
                        maxNativeAdAdapterListener.onNativeAdDisplayed(null);
                    }

                    @Override
                    public void adEnd(@NonNull BaseAd baseAd) {

                    }

                    @Override
                    public void adClick(@NonNull BaseAd baseAd) {
                        maxNativeAdAdapterListener.onNativeAdClicked();
                    }

                    @Override
                    public void onAdLeftApplication(@NonNull BaseAd baseAd) {

                    }

                    @Override
                    public void error(@NonNull BaseAd baseAd, @NonNull VungleException adError) {
                        maxNativeAdAdapterListener.onNativeAdLoadFailed(MaxAdapterError.NO_FILL);
                    }
                });
            }

            public void loadAd(String adMarkup) {
                // TODO: relocate
//            nativeAd.unregisterView();
                nativeAd.load(adMarkup);
            }

            public void destroyAd() {
                if (mediaView != null) {
                    mediaView.removeAllViews();
                    if (mediaView.getParent() != null) {
                        ((ViewGroup) mediaView.getParent()).removeView(mediaView);
                    }
                }

                if (nativeAd != null) {
                    nativeAd.unregisterView();
//                nativeAd.destroy();
                }

                if (vungleMaxNativeAd != null) {
                    vungleMaxNativeAd.destroyAd();
                }
            }
        }

        private static class VungleMaxNativeAd extends MaxNativeAd {

            NativeAd nativeAd;
            MaxNativeAdView maxNativeAdView;
            MediaView mediaView;

            public VungleMaxNativeAd(MediaView mediaView, NativeAd nativeAd, ImageView iconView) {
                super(
                        new MaxNativeAd.Builder()
                                .setAdFormat(MaxAdFormat.NATIVE)
                                .setTitle(nativeAd.getAdTitle())
                                .setBody(nativeAd.getAdBodyText())
                                .setMediaView(mediaView)
                                .setIconView(iconView)
                                .setCallToAction(nativeAd.getAdCallToActionText())
                );
                this.mediaView = mediaView;
                this.nativeAd = nativeAd;
            }

            public MaxNativeAdView getMaxNativeAdView() {
                return maxNativeAdView;
            }

            @Override
            public void prepareViewForInteraction(final MaxNativeAdView maxNativeAdView) {
                this.maxNativeAdView = maxNativeAdView;

                List<View> clickableViews = new ArrayList<>();
                clickableViews.add(getMaxNativeAdView().getCallToActionButton());
                clickableViews.add(getMaxNativeAdView().getMainView());
                clickableViews.add(mediaView);

                ViewGroup mediaContentGroup = maxNativeAdView.getMediaContentViewGroup();

                FrameLayout frameLayout = new FrameLayout(maxNativeAdView.getContext());
                if (mediaContentGroup != null) {
                    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    mediaContentGroup.addView(frameLayout, params);
                    if (mediaView.getParent() != null) {
                        ((ViewGroup) mediaView.getParent()).removeView(mediaView);
                    }
                    frameLayout.addView(mediaView);
                }
                nativeAd.registerViewForInteraction(frameLayout,
                        mediaView,
                        getMaxNativeAdView().getIconImageView(),
                        clickableViews);
            }

            public void destroyAd() {
                maxNativeAdView.removeAllViews();
            }
        }

        private String getAdMarkup ( final MaxAdapterResponseParameters parameters)
        {
            String bidResponse = parameters.getBidResponse();
            String adMarkup;
            if (AppLovinSdkUtils.isValidString(bidResponse)) {
                adMarkup = bidResponse;
            } else {
                adMarkup = null;
            }
            return adMarkup;
        }
    }