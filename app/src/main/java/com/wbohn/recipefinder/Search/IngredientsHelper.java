package com.wbohn.recipefinder.Search;

import android.content.res.Resources;
import android.content.res.TypedArray;

import com.wbohn.recipefinder.R;

public class IngredientsHelper {

    private Resources resources;
    private String[] categoryTitles;
    private String[][] subCategoryTitles;

    private int buttonPosition;

    public IngredientsHelper(Resources res, int position) {
        resources = res;
        buttonPosition = position;

        categoryTitles = resources.getStringArray(R.array.category_titles);
        subCategoryTitles = convertToStringArray(resources.obtainTypedArray(R.array.sub_category_titles));
    }

    private String[][] convertToStringArray(TypedArray array) {
        int n = array.length();

        String[][] convertedArray  = new String[n][];
        for (int i = 0; i < n; ++i) {
            int id = array.getResourceId(i, 0);
            if (id > 0) {
                convertedArray[i] = resources.getStringArray(id);
            } else {
                // something wrong with the XML
            }
        }
        array.recycle();
        return convertedArray;
    }

    public String getCategoryTitle() {
        return categoryTitles[buttonPosition];
    }

    public String[] getSubCategoryTitles() {
        return subCategoryTitles[buttonPosition];
    }

    public String[] getIngredients(int position) {
        TypedArray subCategories = resources.obtainTypedArray(R.array.sub_categories);

        int id = subCategories.getResourceId(buttonPosition, 0);

        TypedArray selectedSubCategory = resources.obtainTypedArray(id);
        String[][]  subCategory = convertToStringArray(selectedSubCategory);

        String[] ingredients = subCategory[position];
        return ingredients;
    }
}
