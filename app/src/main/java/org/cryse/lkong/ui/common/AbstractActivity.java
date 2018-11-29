package org.cryse.lkong.ui.common;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import org.cryse.lkong.R;
import org.cryse.lkong.event.AbstractEvent;
import org.cryse.lkong.event.RxEventBus;
import org.cryse.lkong.event.ScreenOrientationSettingsChangedEvent;
import org.cryse.lkong.utils.SubscriptionUtils;
import org.cryse.lkong.utils.snackbar.SimpleSnackbarType;
import org.cryse.lkong.utils.snackbar.SnackbarSupport;
import org.cryse.lkong.utils.snackbar.SnackbarUtils;
import org.cryse.lkong.utils.snackbar.ToastErrorConstant;
import org.cryse.utils.preference.BooleanPrefs;
import org.cryse.lkong.application.PreferenceConstant;
import org.cryse.utils.preference.Prefs;
import org.cryse.utils.preference.StringPrefs;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@SuppressWarnings({ "ALL", "AlibabaClassMustHaveAuthor" }) public abstract class AbstractActivity extends AppCompatActivity implements SnackbarSupport {
    private View mSnackbarRootView;
    private Subscription mEventBusSubscription;
    private boolean mIsDestroyed;
    protected String mATEKey;
    protected BooleanPrefs mIsNightMode;
    protected StringPrefs mScreenRotation;
    RxEventBus mEventBus = RxEventBus.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //mATEKey = getATEKey();
        super.onCreate(savedInstanceState);

        mScreenRotation = Prefs.getStringPrefs(
                PreferenceConstant.SHARED_PREFERENCE_SCREEN_ROTATION,
                PreferenceConstant.SHARED_PREFERENCE_SCREEN_ROTATION_VALUE
        );
        checkRotation();


        mEventBusSubscription = mEventBus.toObservable()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onEvent);
        mIsNightMode = Prefs.getBooleanPrefs(
                PreferenceConstant.SHARED_PREFERENCE_IS_NIGHT_MODE,
                PreferenceConstant.SHARED_PREFERENCE_IS_NIGHT_MODE_VALUE
        );

        //mTextPrimaryColor = android.R.attr.textColorPrimary;
    }

    @Nullable
    public final String getATEKey() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_theme", false) ?
                "dark_theme" : "light_theme";
    }

    protected void checkRotation() {
        switch (mScreenRotation.get()) {
            default:
            case "0":
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                break;
            case "1":
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case "2":
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        mSnackbarRootView =  findViewById(android.R.id.content);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkRotation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        analyticsTrackEnter();
    }

    @Override
    protected void onPause() {
        super.onPause();
        analyticsTrackExit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SubscriptionUtils.checkAndUnsubscribe(mEventBusSubscription);
        mIsDestroyed = true;
    }

    public boolean isNightMode() {
        return mIsNightMode.get();
    }

    public void toggleNightMode() {
        mIsNightMode.set(!mIsNightMode.get());
        recreate();
    }

    /**
     * Converts an intent into a {@link Bundle} suitable for use as fragment arguments.
     */
    public static Bundle intentToFragmentArguments(Intent intent) {
        Bundle arguments = new Bundle();
        if (intent == null) {
            return arguments;
        }

        final Uri data = intent.getData();
        if (data != null) {
            arguments.putParcelable("_uri", data);
        }

        final Bundle extras = intent.getExtras();
        if (extras != null) {
            arguments.putAll(intent.getExtras());
        }

        return arguments;
    }

    /**
     * Converts a fragment arguments bundle into an intent.
     */
    public static Intent fragmentArgumentsToIntent(Bundle arguments) {
        Intent intent = new Intent();
        if (arguments == null) {
            return intent;
        }

        final Uri data = arguments.getParcelable("_uri");
        if (data != null) {
            intent.setData(data);
        }

        intent.putExtras(arguments);
        intent.removeExtra("_uri");
        return intent;
    }

    protected abstract void injectThis();

    protected abstract void analyticsTrackEnter();

    protected abstract void analyticsTrackExit();


    protected void onEvent(AbstractEvent event) {
        if(event instanceof ScreenOrientationSettingsChangedEvent) {
            checkRotation();
        }
    }

    protected RxEventBus getEventBus() {
        return mEventBus;
    }

    public boolean isTablet() {
        return getResources().getBoolean(R.bool.isTablet);
    }

    protected View getSnackbarRootView() {
        if(mSnackbarRootView == null) {
          mSnackbarRootView = findViewById(android.R.id.content);
        }
        return mSnackbarRootView;
    }

    @Override
    public void showSnackbar(CharSequence text, SimpleSnackbarType type, Object... args) {
        SnackbarUtils.makeSimple(
                getSnackbarRootView(),
                text,
                type,
                SimpleSnackbarType.LENGTH_SHORT
        ).show();
    }

    @Override
    public void showSnackbar(int errorCode, SimpleSnackbarType type, Object... args) {
        SnackbarUtils.makeSimple(
                getSnackbarRootView(),
                getString(ToastErrorConstant.errorCodeToStringRes(errorCode)),
                type,
                SimpleSnackbarType.LENGTH_SHORT
        ).show();
    }

    protected int getPrimaryColor() {
        return  getResources().getColor(R.color.md_blue_400);
    }

    protected int getPrimaryDarkColor() {
        return Color.parseColor("#37474F");
    }

    protected int getAccentColor() {
        return Color.parseColor("#263238");
    }

    public boolean isActivityDestroyed() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return isDestroyed() || isFinishing();
        } else {
            return mIsDestroyed || isFinishing();
        }
    }
}