package com.wbohn.recipefinder.Bus;

public class IngredientSelectedEvent {
    private String[] ingredients;

    public IngredientSelectedEvent(String[] ingredients) {
        this.ingredients = ingredients;
    }

    public String[] getIngredients() {
        return ingredients;
    }
}
