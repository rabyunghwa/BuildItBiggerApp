package com.awesome.byunghwa.app.builditbigger;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.awesome.byunghwa.app.androidlibrary.JokeActivity;
import com.example.byunghwa.myapplication.backend.myApi.MyApi;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import java.io.IOException;
import java.util.Random;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements View.OnClickListener {

    private ProgressBar progressBar;
    public static EndpointsAsyncTask asyncTask;
    static String jokeBody;


    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main, container, false);

        progressBar = (ProgressBar) root.findViewById(R.id.progress_indicator);

        Button tellJokeBtn = (Button) root.findViewById(R.id.button);
        tellJokeBtn.setOnClickListener(this);

        return root;
    }

    @Override
    public void onClick(View v) {
        tellJoke();
    }

    private void tellJoke() {
        String index = String.valueOf(new Random().nextInt(10));
        asyncTask = new EndpointsAsyncTask();
        asyncTask.execute(new Pair<Context, String>(getActivity(), index));
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
