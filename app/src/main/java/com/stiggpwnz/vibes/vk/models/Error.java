package com.stiggpwnz.vibes.vk.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class Error extends Exception implements Serializable {
    private static final int USER_AUTHORIZATION_FAILED = 5;

    @JsonProperty("error_code") int    errorCode;
    @JsonProperty("error_msg")  String errorMsg;

    public boolean isAuthError() { return errorCode == USER_AUTHORIZATION_FAILED; }
}