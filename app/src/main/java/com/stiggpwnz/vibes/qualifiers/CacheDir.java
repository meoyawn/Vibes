package com.stiggpwnz.vibes.qualifiers;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;

/**
 * Created by adel on 14/03/14
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheDir {
}
