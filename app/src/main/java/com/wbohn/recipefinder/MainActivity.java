package com.wbohn.recipefinder;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.wbohn.recipefinder.Network.PuppyClient;

public class MainActivity extends AppCompatActivity {
    private PuppyClient puppyClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fragmentManager = getFragmentManager();

        puppyClient = (PuppyClient) fragmentManager.findFragmentByTag("puppyClient");

        if (puppyClient == null) {
            puppyClient = new PuppyClient();
            fragmentManager.beginTransaction().add(puppyClient, "puppyClient").commit();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        App.getEventBus().unregister(this);
        App.getEventBus().unregister(puppyClient);
    }

    @Override
    protected void onResume() {
        super.onResume();

        App.getEventBus().register(this);
        App.getEventBus().register(puppyClient);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
