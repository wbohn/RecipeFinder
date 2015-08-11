package com.wbohn.recipefinder.Bus;

import com.wbohn.recipefinder.RecipeList.Recipe;

import java.util.ArrayList;

public class RecipesReceivedEvent {
    private Recipe recipes[];
    private ArrayList<String> keywords;
    private ArrayList<String> ingredients;
    private boolean loadRequest;

    public RecipesReceivedEvent(Recipe[] recipes, ArrayList<String> keywords, ArrayList<String> ingredients, boolean loadRequest) {
        this(recipes, keywords, ingredients);
        this.loadRequest = loadRequest;
    }

    public RecipesReceivedEvent(Recipe[] recipes, ArrayList<String> keywords, ArrayList<String> ingredients) {
        this.recipes = recipes;
        this.keywords = keywords;
        this.ingredients = ingredients;
        loadRequest = false;
    }

    public Recipe[] getRecipes() {
        return recipes;
    }

    public ArrayList<String> getKeywords() {
        return keywords;
    }

    public ArrayList<String> getIngredients() {
        return ingredients;
    }

    public boolean isLoadRequest() {
        return loadRequest;
    }
}
