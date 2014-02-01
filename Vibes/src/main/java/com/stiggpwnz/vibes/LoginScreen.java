package com.stiggpwnz.vibes;

import flow.Layout;
import mortar.Blueprint;

/**
 * Created by adel on 2/1/14
 */
@Layout(R.layout.login)
public class LoginScreen implements Blueprint {

    @Override
    public String getMortarScopeName() {
        return getClass().getName();
    }

    @Override
    public Object getDaggerModule() {
        return null;
    }
}
