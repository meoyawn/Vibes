package com.stiggpwnz.vibes.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.stiggpwnz.vibes.db.models.AudioUrl;
import com.stiggpwnz.vibes.vk.VKAuth;
import com.stiggpwnz.vibes.vk.models.Audio;
import com.stiggpwnz.vibes.vk.models.Feed;
import com.stiggpwnz.vibes.vk.models.Post;

import lombok.Cleanup;
import nl.qbusict.cupboard.DatabaseCompartment;

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
        VKAuth.assertBgThread();

        return cupboard().withDatabase(getReadableDatabase())
                .query(AudioUrl.class)
                .byId(audio.getAid())
                .get()
                .getUrl();
    }

    public void putUrl(Audio audio) {
        VKAuth.assertBgThread();

        cupboard().withDatabase(getWritableDatabase())
                .put(audio.createAudioUrl());
    }

    public void putUrls(Feed feed) {
        @Cleanup SQLiteDatabase writableDatabase = getWritableDatabase();
        if (writableDatabase != null) {
            writableDatabase.beginTransaction();
            try {
                AudioUrl audioUrl = new AudioUrl();
                DatabaseCompartment databaseCompartment = cupboard().withDatabase(writableDatabase);

                for (Post post : feed.getItems()) {
                    for (Audio audio : post.getAudios()) {
                        audioUrl.set_id((long) audio.getAid()).setUrl(audio.getUrl());
                        databaseCompartment.put(audioUrl);
                    }
                }
                writableDatabase.setTransactionSuccessful();
            } finally {
                writableDatabase.endTransaction();
            }
        }
    }
}
