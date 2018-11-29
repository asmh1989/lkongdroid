package org.cryse.lkong.widget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.GeolocationPermissions;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;


@SuppressWarnings({ "ALL", "AlibabaClassMustHaveAuthor" }) public class HTML5WebView extends WebView {

    private Context mContext;
    private MyWebChromeClient mWebChromeClient;
    private OnLoadProgressChangedListener mOnLoadProgressChangedListener;

    private void init(Context context) {
        mContext = context;
        if(isInEditMode()) {
          return;
        }
        Activity a = (Activity) mContext;

        mWebChromeClient = new MyWebChromeClient();
        setWebChromeClient(mWebChromeClient);

        setWebViewClient(new MyWebViewClient());

        // Configure the webview
        WebSettings s = getSettings();
        s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false);
        s.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        s.setSavePassword(true);
        s.setSaveFormData(true);
        s.setJavaScriptEnabled(true);
        s.setAppCacheEnabled(false);
        s.setPluginState(WebSettings.PluginState.OFF);

        clearCache(true);

        // enable navigator.geolocation
        s.setGeolocationEnabled(true);
        s.setGeolocationDatabasePath("/data/data/org.itri.html5webview/databases/");

        // enable Web Storage: localStorage, sessionStorage
        s.setDomStorageEnabled(true);

    }

    public HTML5WebView(Context context) {
        super(context);
        init(context);
    }

    public HTML5WebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public HTML5WebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void hideCustomView() {
        mWebChromeClient.onHideCustomView();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (canGoBack()){
                goBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private class MyWebChromeClient extends WebChromeClient {

        @Override
        public void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback)
        {
            Log.v("talon_webview", "showing custom view of youtube");
        }

        @Override
        public void onHideCustomView() {
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if(mOnLoadProgressChangedListener != null) {
                mOnLoadProgressChangedListener.onProgressChanged(view, newProgress);
            }
            if (newProgress > 90) {
                setBackgroundColor(getResources().getColor(android.R.color.white));
            } else {
                setBackgroundColor(getResources().getColor(android.R.color.transparent));
            }
        }

        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            callback.invoke(origin, true, false);
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
            return false;
        }
    }

    static final FrameLayout.LayoutParams COVER_SCREEN_PARAMS =
            new FrameLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

    public class MyWebViewClient extends WebViewClient {
        @Override

        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.v("talon_url", "url: " + url);

            if (url.contains("play.google.com") || url.contains("youtube.com") || url.contains("youtu.be")) {
                Uri weburi;
                weburi = Uri.parse(url);
                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, weburi);
                getContext().startActivity(launchBrowser);
            } else {
                view.loadUrl(url);
            }

            return true;

        }

    }

    public void setOnLoadProgressChangedListener(OnLoadProgressChangedListener onLoadProgressChangedListener) {
        this.mOnLoadProgressChangedListener = onLoadProgressChangedListener;
    }

    public interface OnLoadProgressChangedListener {
        void onProgressChanged(WebView view, int newProgress);
    }
}