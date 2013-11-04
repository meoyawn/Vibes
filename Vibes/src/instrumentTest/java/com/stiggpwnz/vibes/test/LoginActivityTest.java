package com.stiggpwnz.vibes.test;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;

import com.jayway.android.robotium.solo.By;
import com.jayway.android.robotium.solo.Solo;
import com.stiggpwnz.vibes.fragments.LoginFragment;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;

/**
 * Created by adel on 11/4/13
 */
@SmallTest
public class LoginActivityTest extends ActivityInstrumentationTestCase2<LoginActivity> {

    private Solo solo;

    public LoginActivityTest() {
        super(LoginActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        solo = new Solo(getInstrumentation(), getActivity());
    }

    public void testLogin() {
        onView(withId(LoginFragment.ID)).check(matches(isDisplayed()));
        solo.enterTextInWebElement(By.name("email"), "stiggpwnz@gmail.com");
    }
}
