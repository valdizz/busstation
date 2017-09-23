package com.example.valdizz.busstation;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.Calendar;


public class AboutActivity extends AppCompatActivity {

    TextView tvAppVersion;
    TextView tvCopyright;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        tvAppVersion = (TextView) findViewById(R.id.tvAppVersion);
        tvCopyright = (TextView) findViewById(R.id.tvCopyright);

        init();
    }

    private void init(){
        tvAppVersion.setText(getString(R.string.about_version, BuildConfig.VERSION_NAME));
        tvCopyright.setText(getString(R.string.about_copyright,((Calendar.getInstance().get(Calendar.YEAR))==2017 ? "2017" : "2017 - "+Calendar.getInstance().get(Calendar.YEAR))));
    }

}
