package com.wbohn.recipefinder;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.wbohn.recipefinder.Bus.ErrorEvent;
import com.wbohn.recipefinder.Bus.RecipeRequest;
import com.wbohn.recipefinder.Bus.RecipesReceivedEvent;
import com.wbohn.recipefinder.Bus.RefreshRequest;
import com.wbohn.recipefinder.Network.PuppyClient;

public class MainActivity extends AppCompatActivity {
    private PuppyClient puppyClient;

    private ProgressBar loadingIcon;
    private TextView emptyText;
    private TextView recipePuppyLink;

    private Menu menu;

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

        loadingIcon = (ProgressBar) findViewById(R.id.loadingIcon);
        emptyText = (TextView) findViewById(R.id.empty_text);
        recipePuppyLink = (TextView) findViewById(R.id.link);
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
        this.menu = menu;
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

    @Subscribe
    public void onRecipeRequest(RecipeRequest request) {
        hideError();
        loadingIcon.setVisibility(View.VISIBLE);
    }

    @Subscribe
    public void onRecipesReceived(RecipesReceivedEvent event) {
        hideError();

        loadingIcon.setVisibility(View.GONE);
        if (menu != null) {
            menu.findItem(R.id.action_refresh).setVisible(false);
        }
    }

    @Subscribe
    public void onError(ErrorEvent error) {
        loadingIcon.setVisibility(View.GONE);
        emptyText.setVisibility(View.VISIBLE);
        emptyText.setText(getResources().getString(R.string.network_error));
        recipePuppyLink.setVisibility(View.VISIBLE);

        if (menu != null) {
            menu.findItem(R.id.action_refresh).setVisible(true);
        }
    }

    @Subscribe
    public void onRefreshRequest(RefreshRequest request) {
        hideError();
        loadingIcon.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        emptyText.setVisibility(View.GONE);
        recipePuppyLink.setVisibility(View.GONE);
    }
}
