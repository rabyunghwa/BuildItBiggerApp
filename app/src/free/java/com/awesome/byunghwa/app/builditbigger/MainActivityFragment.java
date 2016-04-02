package com.awesome.byunghwa.app.builditbigger;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.awesome.byunghwa.app.androidlibrary.JokeActivity;
import com.example.byunghwa.myapplication.backend.myApi.MyApi;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import java.io.IOException;
import java.util.Random;

/**
 * Created by ByungHwa on 8/14/2015.
 */
public class MainActivityFragment extends Fragment implements View.OnClickListener {

    private PublisherInterstitialAd mPublisherInterstitialAd;
    static String jokeBody;
    private ProgressBar progressBar;
    public static EndpointsAsyncTask asyncTask;


    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main, container, false);
        AdView mAdView = (AdView) root.findViewById(R.id.adView);
        // Create an ad request. Check logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mAdView.loadAd(adRequest);

        progressBar = (ProgressBar) root.findViewById(R.id.progress_indicator);

        Button tellJokeBtn = (Button) root.findViewById(R.id.button);
        tellJokeBtn.setOnClickListener(this);

        mPublisherInterstitialAd = new PublisherInterstitialAd(getActivity());
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

        return root;
    }

    // button on click event
    public void tellJoke() {
        showInterstitial();
    }

    private void showInterstitial() {
        // Show the ad if it's ready. Otherwise toast and restart the game.
        if (mPublisherInterstitialAd != null && mPublisherInterstitialAd.isLoaded()) {
            mPublisherInterstitialAd.show();
        } else {
            Toast.makeText(getActivity(), "Ad did not load", Toast.LENGTH_SHORT).show();
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
        asyncTask.execute(new Pair<Context, String>(getActivity(), index));
    }

    @Override
    public void onClick(View v) {
        tellJoke();
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
                Intent intent = new Intent(getActivity(), JokeActivity.class);
                intent.putExtra("joke", jokeBody);
                startActivity(intent);
            }

        }
    }
}
