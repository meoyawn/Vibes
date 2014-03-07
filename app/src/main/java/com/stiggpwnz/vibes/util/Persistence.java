package com.stiggpwnz.vibes.util;

import de.devland.esperandro.annotations.SharedPreferences;

@SharedPreferences
public interface Persistence {
    public String accessToken();

    public void accessToken(String accessToken);

    public long expiresIn();

    public void expiresIn(long expiresIn);

    public int userId();

    public void userId(int userId);
}