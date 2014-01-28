package com.stiggpwnz.vibes.vk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Error extends Exception {

    private static final int USER_AUTHORIZATION_FAILED = 5;

    private int    errorCode;
    private String errorMsg;

    public boolean isAuthError() { return errorCode == USER_AUTHORIZATION_FAILED; }

    @Override
    @JsonProperty("error_msg")
    public String getMessage() { return errorMsg; }

    @JsonProperty("error_msg")
    public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }

    @JsonProperty("error_code")
    public int getErrorCode() { return errorCode; }

    @JsonProperty("error_code")
    public void setErrorCode(int errorCode) { this.errorCode = errorCode; }
}