package com.awesome.byunghwa.app.androidlibrary;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class JokeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joke);
        TextView joke = (TextView) findViewById(R.id.jokebody);

        // get joke body from JokeActivity in the form of intent extra
        Intent intent = getIntent();
        if (intent.hasExtra("joke")) {
            String jokebody = intent.getStringExtra("joke");
            joke.setText(jokebody);
        }

    }

}
