package com.stiggpwnz.vibes.media;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.companyname.appname.dagger.Dagger;

import javax.inject.Inject;

import dagger.Lazy;

/**
 * Created by adel on 11/28/13
 */
public class PlayerService extends Service {

    @Inject Lazy<Player> playerLazy;

    @Override
    public void onCreate() {
        super.onCreate();
        Dagger.inject(this);
    }

    @Override
    public void onDestroy() {
        playerLazy.get().release();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        //noinspection ReturnOfNull
        return null;
    }
}
