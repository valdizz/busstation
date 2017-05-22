package com.example.valdizz.busstation;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.Calendar;


public class AboutActivity extends AppCompatActivity {

    TextView tvAppVersion;
    TextView tvAndroidVersion;
    TextView tvCopyright;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        tvAppVersion = (TextView) findViewById(R.id.tvAppVersion);
        tvAndroidVersion = (TextView) findViewById(R.id.tvAnroidVersion);
        tvCopyright = (TextView) findViewById(R.id.tvCopyright);

        init();
    }

    private void init(){
        tvAppVersion.setText("v. " + BuildConfig.VERSION_NAME);
        tvAndroidVersion.setText("Android ver: " + Build.VERSION.RELEASE + " (" + Build.VERSION.SDK_INT + ")");
        tvCopyright.setText("Copyright © " + ((Calendar.getInstance().get(Calendar.YEAR))==2017 ? "2017" : "2017 - "+Calendar.getInstance().get(Calendar.YEAR)) + "\nby Vlad Kornev");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_menu, menu);
        menu.removeItem(R.id.about_menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.back_menu:{
                finish();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
