package com.stiggpwnz.vibes.util;

import com.stiggpwnz.vibes.player.PlayerQueue;

import de.devland.esperandro.annotations.SharedPreferences;

@SharedPreferences
public interface Persistence {
    public String accessToken();

    public void accessToken(String accessToken);

    public long expiresIn();

    public void expiresIn(long expiresIn);

    public int userId();

    public void userId(int userId);

    public PlayerQueue.Repeat repeat();

    public void repeat(PlayerQueue.Repeat repeat);
}