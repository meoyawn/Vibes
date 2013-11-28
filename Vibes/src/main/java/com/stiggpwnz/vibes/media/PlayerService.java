package com.stiggpwnz.vibes.media;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.squareup.otto.Bus;
import com.stiggpwnz.vibes.util.Injector;

import javax.inject.Inject;

import dagger.Lazy;

/**
 * Created by adel on 11/28/13
 */
public class PlayerService extends Service {

    @Inject Lazy<Player> playerLazy;
    @Inject Lazy<Bus>    busLazy;

    @Override
    public void onCreate() {
        super.onCreate();
        Injector.inject(this);
        busLazy.get().register(this);
    }

    @Override
    public void onDestroy() {
        busLazy.get().unregister(this);
        playerLazy.get().release();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
