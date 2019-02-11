package com.valdizz.busstation;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Calendar;


public class AboutActivity extends AppCompatActivity {

    private TextView tvAppVersion;
    private TextView tvCopyright;
    private Button btnRate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        tvAppVersion = findViewById(R.id.tvAppVersion);
        tvCopyright = findViewById(R.id.tvCopyright);
        btnRate = findViewById(R.id.btnRate);

        init();
    }

    private void init(){
        tvAppVersion.setText(getString(R.string.about_version, BuildConfig.VERSION_NAME));
        tvCopyright.setText(getString(R.string.about_copyright,((Calendar.getInstance().get(Calendar.YEAR))==2017 ? "2017" : "2017 - "+Calendar.getInstance().get(Calendar.YEAR))));
        btnRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=com.valdizz.busstation"));
                startActivity(intent);
            }
        });
    }

}
