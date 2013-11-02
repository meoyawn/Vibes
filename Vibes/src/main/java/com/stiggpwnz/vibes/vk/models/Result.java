package com.stiggpwnz.vibes.vk.models;

public abstract class Result<T> {

    public Error error;

    public boolean isResponse() {
        return error == null;
    }

    public boolean isError() {
        return error != null;
    }

    public abstract T getResponse();
}