package com.stiggpwnz.vibes;

import android.webkit.CookieManager;

import com.stiggpwnz.vibes.activities.MainActivityTest;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static org.mockito.Mockito.mock;

/**
 * Created by adel on 11/8/13
 */
@Module(injects = {MainActivityTest.class},
        includes = VibesModule.class,
        overrides = true)
public class TestModule {

    @Provides
    @Singleton
    CookieManager provideMockCookieManager() {
        return mock(CookieManager.class);
    }
}
