package com.akropon.hammingcode;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.support.annotation.NonNull;
import android.support.test.espresso.Espresso;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;
import android.test.ActivityInstrumentationTestCase2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;

import javax.annotation.Nonnull;

import static android.content.Context.CLIPBOARD_SERVICE;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.doubleClick;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class FuncTests extends ActivityInstrumentationTestCase2<MainActivity> {

    public FuncTests() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        getActivity();
    }

    public void testEncode() {
        ArrayList<String> infoVectors = new ArrayList<>();
        infoVectors.add("0");
        infoVectors.add("1");
        infoVectors.add("00");
        infoVectors.add("01");
        infoVectors.add("10");
        infoVectors.add("11");
        infoVectors.add("111111111");
        infoVectors.add("000000000");
        infoVectors.add("10101");
        infoVectors.add("01010");

        _testEncodeDecodeEmpty();

        for (String word: infoVectors) {
            _testEncodeDecodeNotEmpty(word);
        }
    }

    /**
     *
     * @param inputWord - непустая строка - входное слово
     */
    public void _testEncodeDecodeNotEmpty(@Nonnull String inputWord) {

        onView(withId(R.id.btn_deleteall)).perform(doubleClick());

        for (int i=0; i<inputWord.length(); i++)
            if (inputWord.charAt(i)=='1')
                onView(withId(R.id.btn_addone)).perform(click());
            else
                onView(withId(R.id.btn_addzero)).perform(click());

        onView(withId(R.id.btn_encode)).perform(click());

        onView(withId(R.id.btn_copy_result_word)).perform(click());

        // читаем из буффера слово
        String buffer = null;
        ClipboardManager clipboard = (ClipboardManager) getActivityInstance()
                .getSystemService(CLIPBOARD_SERVICE);
        ClipData clipData = clipboard.getPrimaryClip();
        if (clipData != null)
            buffer = clipData.getItemAt(0).getText().toString();

        if (buffer == null)
            fail("\nOutput word after encoding in buffer is null. Input="+inputWord);

        onView(withId(R.id.btn_back)).perform(click());


        onView(withId(R.id.btn_deleteall)).perform(doubleClick());

        for (int i=0; i<buffer.length(); i++)
            if (buffer.charAt(i)=='1')
                onView(withId(R.id.btn_addone)).perform(click());
            else
                onView(withId(R.id.btn_addzero)).perform(click());


        onView(withId(R.id.btn_decode)).perform(click());

        onView(withId(R.id.btn_copy_result_word)).perform(click());

        // читаем из буффера слово
        buffer = null;
        clipboard = (ClipboardManager) getActivityInstance()
                .getSystemService(CLIPBOARD_SERVICE);
        clipData = clipboard.getPrimaryClip();
        if (clipData != null)
            buffer = clipData.getItemAt(0).getText().toString();

        // проверяем, совпало ли
        if (buffer == null)
            fail("\nOutput word after encoding-decoding in buffer is null. Input="+inputWord);
        if (buffer.compareTo(inputWord) != 0)
            fail("\nOutput word after encoding-decoding in buffer differs from input word. Input="
                    +inputWord+" Real="+buffer);

        onView(withId(R.id.btn_back)).perform(click());
    }


    public void _testEncodeDecodeEmpty() {

        onView(withId(R.id.btn_deleteall)).perform(doubleClick());

        onView(withId(R.id.btn_encode)).perform(click());

        onView(withId(R.id.btn_copy_result_word)).perform(click());

        // читаем из буффера слово
        String buffer = null;
        ClipboardManager clipboard = (ClipboardManager) getActivityInstance()
                .getSystemService(CLIPBOARD_SERVICE);
        ClipData clipData = clipboard.getPrimaryClip();
        if (clipData != null)
            buffer = clipData.getItemAt(0).getText().toString();

        onView(withId(R.id.btn_back)).perform(click());


        onView(withId(R.id.btn_deleteall)).perform(doubleClick());

        for (int i=0; i<buffer.length(); i++)
            if (buffer.charAt(i)=='1')
                onView(withId(R.id.btn_addone)).perform(click());
            else
                onView(withId(R.id.btn_addzero)).perform(click());

        if (buffer == null)
            fail("\nOutput word after encoding in buffer is null. Input=\"\"");
        if (buffer.compareTo(Cnst.undefinedOutputFieldFiller) != 0)
            fail("\nOutput word after encoding in buffer differs "
                    + "from undefinedOutputFieldFiller. Input=\"\""
                    + " Expexted="+Cnst.undefinedOutputFieldFiller+" Out="+buffer);

        onView(withId(R.id.btn_decode)).perform(click());

        onView(withId(R.id.btn_copy_result_word)).perform(click());

        if (buffer == null)
            fail("\nOutput word after decoding in buffer is null. Input=\"\"");
        if (buffer.compareTo(Cnst.undefinedOutputFieldFiller) != 0)
            fail("\nOutput word after decoding in buffer differs "
                    + "from undefinedOutputFieldFiller. Input=\"\""
                    + " Expexted="+Cnst.undefinedOutputFieldFiller+" Out="+buffer);

        onView(withId(R.id.btn_back)).perform(click());
    }


    private Activity getActivityInstance(){
        final Activity[] currentActivity = {null};

        getInstrumentation().runOnMainSync(new Runnable(){
            public void run(){
                Collection<Activity> resumedActivity = ActivityLifecycleMonitorRegistry
                        .getInstance().getActivitiesInStage(Stage.RESUMED);
                Iterator<Activity> it = resumedActivity.iterator();
                currentActivity[0] = it.next();
            }
        });

        return currentActivity[0];
    }
}