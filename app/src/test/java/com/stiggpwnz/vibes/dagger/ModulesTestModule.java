package com.stiggpwnz.vibes.dagger;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.Picasso;
import com.stiggpwnz.vibes.fragments.MainFragment;

import dagger.Module;

/**
 * Created by adelnizamutdinov on 03/03/2014
 */
@Module(injects = {OkHttpClient.class, Picasso.class, MainFragment.class})
public class ModulesTestModule {}
