package com.stiggpwnz.vibes.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.stiggpwnz.vibes.activities.MainActivity;
import com.stiggpwnz.vibes.fragments.base.BaseFragment;
import com.stiggpwnz.vibes.util.Persistance;
import com.stiggpwnz.vibes.vk.VKontakte;

import java.util.Map;

public class LoginFragment extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return new WebView(getActivity());
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        WebView webView = (WebView) view;
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                if (url.startsWith(VKontakte.REDIRECT_URL)) {
                    CookieSyncManager.getInstance().sync();
                    Map<String, String> result = VKontakte.parseRedirectUrl(url);
                    Persistance.saveVK(result);
                    startActivity(new Intent(getActivity(), MainActivity.class));
                    getActivity().finish();
                }
            }
        });
        if (webView.getUrl() == null) {
            webView.loadUrl(VKontakte.authUrl());
        }
    }

    public WebView getWebView() {
        return (WebView) getView();
    }
}
