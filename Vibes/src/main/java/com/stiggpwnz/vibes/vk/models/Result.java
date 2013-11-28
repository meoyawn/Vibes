package com.stiggpwnz.vibes.vk.models;

public abstract class Result<T> {

    public T     response;
    public Error error;

    public boolean isError() {
        return error != null;
    }
}