package com.stiggpwnz.vibes.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.stiggpwnz.vibes.db.models.AudioUrl;
import com.stiggpwnz.vibes.vk.models.Audio;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

/**
 * Created by adel on 15/03/14
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    static final String NAME    = "vibes.db";
    static final int    VERSION = 1;

    static {
        cupboard().register(AudioUrl.class);
    }

    public DatabaseHelper(Context context) { super(context, NAME, null, VERSION); }

    @Override public void onCreate(SQLiteDatabase db) {
        cupboard().withDatabase(db).createTables();
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        cupboard().withDatabase(db).upgradeTables();
    }

    public String getUrl(Audio audio) {
        return cupboard().withDatabase(getReadableDatabase())
                .query(AudioUrl.class)
                .byId(audio.getAid())
                .get()
                .getUrl();
    }

    public void putUrl(Audio audio, String url) {
        cupboard().withDatabase(getWritableDatabase())
                .put(new AudioUrl((long) audio.getAid(), url));
    }
}
