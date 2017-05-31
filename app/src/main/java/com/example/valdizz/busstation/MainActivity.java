package com.example.valdizz.busstation;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity{

    Button btnRoutes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.app_name_full);

        btnRoutes = (Button) findViewById(R.id.btnRoutes);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_menu, menu);
        menu.removeItem(R.id.back_menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.back_menu:{
                finish();
                return true;
            }
            case R.id.about_menu:{
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onClickRoutes(View view){
        Intent intent = new Intent(this, RoutesActivity.class);
        startActivity(intent);
    }

    public void onClickFavoritesStations(View view){
        Intent intent = new Intent(this, FavoriteStationsActivity.class);
        startActivity(intent);
    }

    public void onClickFoundStations(View view){
        Intent intent = new Intent(this, FoundStationsActivity.class);
        startActivity(intent);
    }

    public void onClickMapStations(View view){
        Intent intent = new Intent(this, MapStationsActivity.class);
        startActivity(intent);
    }

    public void onClickReminders(View view){
        Intent intent = new Intent(this, RemindersActivity.class);
        startActivity(intent);
    }

    public void onClickExit(View view){
        finish();
    }
}
