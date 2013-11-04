package com.stiggpwnz.vibes.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.webkit.WebView;

import com.stiggpwnz.vibes.activities.base.BaseActivity;
import com.stiggpwnz.vibes.fragments.LoginFragment;

public class LoginActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(android.R.id.content, new LoginFragment()).commit();
        }
    }

    public WebView getWebView() {
        Fragment fragmentById = getSupportFragmentManager().findFragmentById(android.R.id.content);
        if (fragmentById != null && fragmentById instanceof LoginFragment) {
            LoginFragment loginFragment = (LoginFragment) fragmentById;
            return loginFragment.getWebView();
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
