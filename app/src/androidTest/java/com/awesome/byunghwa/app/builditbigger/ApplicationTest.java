package com.awesome.byunghwa.app.builditbigger;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.util.Pair;
import android.test.ActivityInstrumentationTestCase2;

import com.example.byunghwa.myapplication.backend.myApi.MyApi;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ActivityInstrumentationTestCase2 {
    String joke;

    // this constructor is key!! By extending ActivityInstrumentationTestCase2, we provide functional testing of a specific Activity
    public ApplicationTest() {
        super(MainActivity.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
        joke = null;
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testAsyncTask() throws Throwable {

        // create  a signal to let us know when our task is done.
        final CountDownLatch signal = new CountDownLatch(1);


    /* Just create an in line implementation of an asynctask. Note this
     * would normally not be done, and is just here for completeness.
     * You would just use the task you want to unit test in your project.
     */
        final AsyncTask<Pair<Context, String>, Void, String> myTask = new AsyncTask<Pair<Context, String>, Void, String>() {

            private MyApi myApiService = null;
            private Context context;

            @Override
            protected String doInBackground(Pair<Context, String>... params) {
                //Do something meaningful.
                if (myApiService == null) {  // Only do this once
                    // end options for devappserver
                    MyApi.Builder builder = new MyApi.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
                            .setRootUrl("https://.appspot.com/_ah/api/");

                    myApiService = builder.build();
                }

                context = params[0].first;
                String index = params[0].second;

                try {
                    return myApiService.sayHi(index).execute().getData();
                } catch (IOException e) {
                    return e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(String result) {
                joke = result;
                signal.countDown();
            }
        };

        //start task
        myTask.execute(new Pair<Context, String>(getActivity(), String.valueOf(new Random().nextInt(10))));

        /* The testing thread will wait here until the UI thread releases it
	     * above with the countDown() or 30 seconds passes and it times out.
	     */
        signal.await(10, TimeUnit.SECONDS);

        //Assert.assertNotNull(joke);
        assertNotNull(joke);
    }
}
