package com.company.g1.g1extrateamlab;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

public class AboutUsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aboutus);
        ((TextView)findViewById(R.id.aboutUsText)).setMovementMethod(new ScrollingMovementMethod());
    }
}
