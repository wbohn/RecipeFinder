package com.wbohn.recipefinder.Bus;

import java.util.ArrayList;

public class RefreshRequest {
    private ArrayList<String> currentKeywords;
    private ArrayList<String> currentIngredients;

    public RefreshRequest(ArrayList<String> currentKeywords, ArrayList<String> currentIngredients) {
        this.currentKeywords = currentKeywords;
        this.currentIngredients = currentIngredients;
    }

    public ArrayList<String> getCurrentKeywords() {
        return currentKeywords;
    }

    public ArrayList<String> getCurrentIngredients() {
        return currentIngredients;
    }
}
