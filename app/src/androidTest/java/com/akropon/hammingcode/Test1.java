package com.akropon.hammingcode;

import android.test.ActivityInstrumentationTestCase2;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class Test1 extends ActivityInstrumentationTestCase2<MainActivity> {

    public Test1() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        getActivity();
    }

    public void testClick1() {
        // Click on the button
        onView(withId(R.id.btn_addone)).perform(click());

        // Check the text is displayed
        onView(withId(R.id.edittext_input)).check(matches(withText("1")));
    }

    public void testClick0() {
        // Click on the button
        onView(withId(R.id.btn_addzero)).perform(click());

        // Check the text is displayed
        onView(withId(R.id.edittext_input)).check(matches(withText("0")));
    }

    public void testClickEncode() {
        // Click on the button
        onView(withId(R.id.btn_encode)).perform(click());

        onView(withId(R.id.textview_result)).check(matches(isDisplayed()));
    }

    public void testClickDecode() {
        // Click on the button
        onView(withId(R.id.btn_decode)).perform(click());

        onView(withId(R.id.textview_result)).check(matches(isDisplayed()));
    }

    public void testClickBtnRightBtnLeft() {
        // Click on the button
        onView(withId(R.id.btn_addzero)).perform(click());
        onView(withId(R.id.btn_addzero)).perform(click());
        onView(withId(R.id.btn_addzero)).perform(click());
        onView(withId(R.id.btn_addzero)).perform(click());
        onView(withId(R.id.btn_left)).perform(click());
        onView(withId(R.id.btn_addone)).perform(click());
        onView(withId(R.id.btn_left)).perform(click());
        onView(withId(R.id.btn_left)).perform(click());
        onView(withId(R.id.btn_addone)).perform(click());

        onView(withId(R.id.btn_right)).perform(click());
        onView(withId(R.id.btn_addone)).perform(click());
        onView(withId(R.id.btn_right)).perform(click());
        onView(withId(R.id.btn_addzero)).perform(click());

        // Check the text is displayed
        onView(withId(R.id.edittext_input)).check(matches(withText("00101100")));
    }
}
