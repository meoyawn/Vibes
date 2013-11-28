package com.stiggpwnz.vibes;

import org.robolectric.Robolectric;

import java.util.ArrayList;

/**
 * Created by adel on 11/8/13
 */
public class TestVibesApplication extends VibesApplication {

    public static TestVibesApplication get() {
        return (TestVibesApplication) Robolectric.application;
    }

    @Override
    protected ArrayList<Object> getModules() {
        ArrayList<Object> modules = super.getModules();
        modules.add(new TestModule());
        return modules;
    }
}
