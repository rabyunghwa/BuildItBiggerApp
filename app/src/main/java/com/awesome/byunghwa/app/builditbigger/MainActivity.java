package com.awesome.byunghwa.app.builditbigger;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.awesome.byunghwa.app.androidlibrary.JokeActivity;
import com.example.byunghwa.myapplication.backend.myApi.MyApi;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import java.io.IOException;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    static String jokeBody;
    public static EndpointsAsyncTask asyncTask;

    private ProgressBar progressBar;

    private PublisherInterstitialAd mPublisherInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = (ProgressBar) findViewById(R.id.progress_indicator);

        // Create the InterstitialAd and set the adUnitId.
        mPublisherInterstitialAd = new PublisherInterstitialAd(this);
        // Defined in res/values/strings.xml
        mPublisherInterstitialAd.setAdUnitId(getString(R.string.ad_unit_id));

        mPublisherInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
                beginPlayingJoke();
            }
        });

        requestNewInterstitial();
    }

    // button on click event
    public void tellJoke(View view) {
        showInterstitial();
    }

    private void showInterstitial() {
        // Show the ad if it's ready. Otherwise toast and restart the game.
        if (mPublisherInterstitialAd != null && mPublisherInterstitialAd.isLoaded()) {
            mPublisherInterstitialAd.show();
        } else {
            Toast.makeText(this, "Ad did not load", Toast.LENGTH_SHORT).show();
            beginPlayingJoke();
        }
    }

    private void requestNewInterstitial() {
        PublisherAdRequest adRequest = new PublisherAdRequest.Builder().build();

        // Start loading the ad in the background.
        mPublisherInterstitialAd.loadAd(adRequest);
    }

    private void beginPlayingJoke() {
        // before starting joke activity, load another interstitial ad
        requestNewInterstitial();

        String index = String.valueOf(new Random().nextInt(10));
        asyncTask = new EndpointsAsyncTask();
        asyncTask.execute(new Pair<Context, String>(this, index));
    }

    class EndpointsAsyncTask extends AsyncTask<Pair<Context, String>, Void, String> {
        private MyApi myApiService = null;
        private Context context;

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @SafeVarargs
        @Override
        protected final String doInBackground(Pair<Context, String>... params) {
            if (myApiService == null) {  // Only do this once
                // end options for devappserver
                MyApi.Builder builder = new MyApi.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
                        .setRootUrl("https://spratsmith.appspot.com/_ah/api/");

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
            jokeBody = result;

            if (jokeBody != null) {
                progressBar.setVisibility(View.INVISIBLE);
                Intent intent = new Intent(MainActivity.this, JokeActivity.class);
                intent.putExtra("joke", jokeBody);
                startActivity(intent);
            }

        }
    }
}
