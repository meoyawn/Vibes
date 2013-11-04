package com.stiggpwnz.vibes.activities;

import android.app.Fragment;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewFragment;

import com.stiggpwnz.vibes.activities.base.BaseActivity;
import com.stiggpwnz.vibes.fragments.LoginFragment;

public class LoginActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(android.R.id.content, new LoginFragment())
                    .commit();
        }
    }

    public WebView getWebView() {
        Fragment fragmentById = getFragmentManager().findFragmentById(android.R.id.content);
        if (fragmentById != null && fragmentById instanceof WebViewFragment) {
            WebViewFragment webViewFragment = (WebViewFragment) fragmentById;
            return webViewFragment.getWebView();
        }
        return null;
    }

    @Override
    public void onBackPressed() {
        WebView webView = getWebView();
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
            return;
        }
        super.onBackPressed();
    }
}
