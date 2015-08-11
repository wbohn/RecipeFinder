package com.wbohn.recipefinder.Bus;

import java.util.ArrayList;

public class RecipeRequest {
    private ArrayList<String> keywords;
    private ArrayList<String> ingredients;
    private int page;

    public RecipeRequest(ArrayList<String> currentKeywords, ArrayList<String> currentIngredients) {
        keywords = currentKeywords;
        ingredients = currentIngredients;
    }

    public RecipeRequest(ArrayList<String> keywords, ArrayList<String> ingredients, int page) {
        this(keywords, ingredients);
        this.page = page;
    }

    public ArrayList<String> getKeywords() {
        return keywords;
    }

    public ArrayList<String> getIngredients() {
        return ingredients;
    }

    public int getPage() {
        return page;
    }
}
