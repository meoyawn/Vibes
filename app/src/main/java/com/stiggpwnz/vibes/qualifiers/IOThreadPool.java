package com.stiggpwnz.vibes.qualifiers;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;

/**
 * Created by adel on 3/5/14
 */
@Qualifier @Retention(RetentionPolicy.RUNTIME)
public @interface IOThreadPool {}
