package com.wbohn.recipefinder.Network;

import com.wbohn.recipefinder.RecipeList.Recipe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RecipeParser {
    private static final String RESULTS_TAG = "results";
    private static final String TITLE_TAG = "title";
    private static final String SOURCE_URL_TAG = "href";
    private static final String INGREDIENTS_TAG = "ingredients";
    private static final String THUMBNAIL_TAG = "thumbnail";

    public static Recipe[] getRecipeArray(JSONObject receivedJson) {
        try {

            JSONArray jsonRecipes = receivedJson.getJSONArray(RESULTS_TAG);
            Recipe convertedRecipes[] = new Recipe[jsonRecipes.length()];

            for (int i = 0; i < jsonRecipes.length(); i++) {
                convertedRecipes[i] = getRecipe(jsonRecipes.getJSONObject(i));
            }

            return convertedRecipes;

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Recipe getRecipe(JSONObject jsonObject) {
        try {
            return new Recipe(
                    jsonObject.getString(TITLE_TAG),
                    jsonObject.getString(SOURCE_URL_TAG),
                    jsonObject.getString(INGREDIENTS_TAG),
                    jsonObject.getString(THUMBNAIL_TAG));
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
