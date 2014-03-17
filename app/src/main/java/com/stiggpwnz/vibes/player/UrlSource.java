package com.stiggpwnz.vibes.player;

import com.stiggpwnz.vibes.db.DatabaseHelper;
import com.stiggpwnz.vibes.vk.VKontakte;
import com.stiggpwnz.vibes.vk.models.Audio;

import lombok.RequiredArgsConstructor;

/**
 * Created by adelnizamutdinov on 17/03/2014
 */
@RequiredArgsConstructor(suppressConstructorProperties = true)
public class UrlSource {
    final VKontakte      vKontakte;
    final DatabaseHelper databaseHelper;

    public String getSavedUrl(Audio audio) {
        return databaseHelper.getUrl(audio);
    }

    public String tryToGetAndMaybeSaveUrl(Audio audio) {
        String url = getSavedUrl(audio);
        if (url == null) {
            url = vKontakte.getUrl(audio);
            databaseHelper.putUrl(audio);
        }
        return url;
    }
}
