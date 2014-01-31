package com.stiggpwnz.vibes;

import com.stiggpwnz.vibes.activities.FlowActivity;
import com.stiggpwnz.vibes.views.LoginView;

import dagger.Module;
import flow.Layout;

/**
 * Created by adel on 1/31/14
 */
public @interface App {

    @Layout(R.layout.login)
    @Module(injects = LoginView.class, addsTo = FlowActivity.ActivityModule.class)
    public class LoginScreen {}

    @Layout(R.layout.navigation)
    public class NavigationScreen {}
}
