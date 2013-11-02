package com.stiggpwnz.vibes.vk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Error extends Throwable {

    private static final int USER_AUTHORIZATION_FAILED = 5;

    public int    error_code;
    public String error_msg;

    public boolean isAuthError() {
        return error_code == USER_AUTHORIZATION_FAILED;
    }

    @Override
    public String getMessage() {
        return error_msg;
    }
}
