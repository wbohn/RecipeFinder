package com.wbohn.recipefinder.Network;

import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;

import android.text.TextUtils;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.squareup.otto.Subscribe;
import com.wbohn.recipefinder.App;
import com.wbohn.recipefinder.Bus.ErrorEvent;
import com.wbohn.recipefinder.Bus.KeywordsClearedEvent;
import com.wbohn.recipefinder.Bus.LoadNextRequest;
import com.wbohn.recipefinder.Bus.RecipeRequest;
import com.wbohn.recipefinder.Bus.RecipesReceivedEvent;
import com.wbohn.recipefinder.Bus.RefreshRequest;
import com.wbohn.recipefinder.RecipeList.Recipe;

import org.json.JSONObject;

import java.util.ArrayList;

public class PuppyClient extends Fragment {
    private static final String TAG = "PuppyClient";

    // API info
    private static final String API_BASE_URL = "http://www.recipepuppy.com/api/?";
    private static final String KEYWORD_PARAM = "q";
    private static final String INGREDIENTS_PARAM = "i";
    private static final String PAGE_PARAM = "p";

    private Uri baseUri = Uri.parse(API_BASE_URL);
    private String[] requestParams;
    private ArrayList<String> keywords;
    private ArrayList<String> ingredients;

    private int retryCount = 0;
    private boolean loadingNext = false;
    private int currentPage = 1;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Subscribe
    public void onRecipeRequest(RecipeRequest event) {
        Log.i(TAG, "onRecipeRequest");

        retryCount = 0;
        loadingNext = false;
        keywords = event.getKeywords();
        ingredients = event.getIngredients();

        currentPage = 1;

        requestRecipes(currentPage);
    }

    @Subscribe
    public void onLoadRequest(LoadNextRequest event) {
        currentPage = event.getPage();
        loadingNext = true;
        requestRecipes(currentPage);
    }

    @Subscribe
    public void onRefreshRequest(RefreshRequest request) {
        keywords = request.getCurrentKeywords();
        ingredients = request.getCurrentIngredients();

        requestRecipes(currentPage);
    }

    @Subscribe
    public void onKeywordsCleared(KeywordsClearedEvent event) {
        keywords = null;
        ingredients = null;
        currentPage = 1;
    }

    private void requestRecipes(int page) {
        String delimitedKeywords = TextUtils.join(",", keywords);
        String delimitedIngredients = TextUtils.join(",", ingredients);

        requestParams = new String[] {delimitedKeywords, delimitedIngredients, String.valueOf(page)};
        String jsonUrl = buildUrlString(requestParams);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, jsonUrl, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        createRecipes(response);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i(TAG, "ERROR");
                        NetworkResponse response = error.networkResponse;
                        if (response != null) {
                            Log.i(TAG, String.valueOf(response.statusCode));
                            /* Due to quirks in Recipe Puppy database, some ingredients
                             (e.g. bacon) will cause a page not found error when
                            they are the only ingredient in the search query. a keyword search
                             of the ingredient will likely yield results */
                            if (response.statusCode == 500) {
                                tryAsKeyword();
                            } else {
                                App.getEventBus().post(new ErrorEvent());
                            }
                        } else {
                            App.getEventBus().post(new ErrorEvent());
                        }
                        //tryAsKeyword();
                    }
                });

        Volley.getInstance(getActivity()).addToRequestQueue(jsObjRequest);
    }

    private String buildUrlString(String[] params) {
        Uri builtUri = baseUri.buildUpon()
                .appendQueryParameter(KEYWORD_PARAM, params[0])
                .appendQueryParameter(INGREDIENTS_PARAM, params[1])
                .appendQueryParameter(PAGE_PARAM, params[2])
                .build();
        Log.i(TAG, "builtUrlString: " + builtUri.toString());
        return builtUri.toString();
    }

    private void createRecipes(JSONObject response) {
        Recipe recipes[] = RecipeParser.getRecipeArray(response);
        App.getEventBus().post(new RecipesReceivedEvent(recipes, keywords, ingredients, loadingNext));
    }

    private void tryAsKeyword() {
        if (retryCount <= 0 && ingredients.size() > 0) {
            String ingredient = ingredients.get(0);
            ingredients.remove(0);

            keywords.add(0, ingredient);
            requestRecipes(1);
            retryCount++;
        } else {
            if (currentPage > 1) {
                /* there are no more recipes available. do nothing */
            } else {
                /* actual communication error. alert ui */
                App.getEventBus().post(new ErrorEvent());
            }
        }
    }
}
