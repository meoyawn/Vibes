package com.stiggpwnz.vibes.vk.models;

public abstract class Result<T> {

    public Error error;

    public boolean isError() {
        return error != null;
    }

    public abstract T getResponse();
}