package com.stiggpwnz.vibes.vk.models;

import org.jetbrains.annotations.Nullable;

import lombok.Data;

public @Data class Result<T> {
    @Nullable T     response;
    @Nullable Error error;

    public boolean isError() { return error != null; }
}