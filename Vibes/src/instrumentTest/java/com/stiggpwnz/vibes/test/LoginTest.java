package com.stiggpwnz.vibes.test;

import android.test.ActivityInstrumentationTestCase2;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.jayway.android.robotium.solo.By;
import com.jayway.android.robotium.solo.Condition;
import com.jayway.android.robotium.solo.Solo;
import com.stiggpwnz.vibes.activities.LoginActivity;
import com.stiggpwnz.vibes.activities.MainActivity;

/**
 * Created by adel on 11/4/13
 */
public class LoginTest extends ActivityInstrumentationTestCase2<LoginActivity> {

    Solo solo;

    public LoginTest() {
        super(LoginActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        solo = new Solo(getInstrumentation(), getActivity());
        CookieSyncManager.createInstance(getActivity());
        CookieManager.getInstance().removeAllCookie();
        solo.enterTextInWebElement(By.name("email"), "stiggpwnz@gmail.com");
    }

    public void testLogin() {
        solo.enterTextInWebElement(By.name("pass"), "omgpifpif");
        solo.clickOnWebElement(By.cssSelector("input.button"));
        solo.clickOnText("Not now");
        solo.waitForActivity(MainActivity.class);
    }

    public void testWrongPassword() {
        solo.enterTextInWebElement(By.name("pass"), "omglol");
        solo.clickOnWebElement(By.cssSelector("input.button"));
        solo.clickOnText("Not now");
        solo.waitForWebElement(By.cssSelector("div.service_msg_box"));
    }

    public void testUserDenied() {
        solo.clickOnWebElement(By.cssSelector("div.near_btn"));
        solo.waitForCondition(new Condition() {

            @Override
            public boolean isSatisfied() {
                return getActivity().isFinishing();
            }
        }, 5000);
    }
}
