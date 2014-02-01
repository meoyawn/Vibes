package com.stiggpwnz.vibes;

import flow.Layout;
import mortar.Blueprint;

/**
 * Created by adel on 2/1/14
 */
@Layout(R.layout.main_root)
public class MainScreen implements Blueprint {

    @Override
    public String getMortarScopeName() {
        return getClass().getName();
    }

    @Override
    public Object getDaggerModule() {
        return null;
    }
}
