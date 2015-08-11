package com.wbohn.recipefinder.RecipeList;

import android.os.Parcel;
import android.os.Parcelable;

public class Recipe implements Parcelable {
    private static String TAG = "Recipe";

    private String title;
    private String sourceUrl;
    private String ingredients;
    private String thumbnailUrl;

    public Recipe(String title, String sourceUrl, String ingredients, String thumbnailUrl) {
        this.title = title;
        this.sourceUrl = sourceUrl;
        this.ingredients = ingredients;
        this.thumbnailUrl = thumbnailUrl;
    }

    @Override
    public String toString() {
        return  title + "\n"
                + ingredients;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(title);
        dest.writeString(sourceUrl);
        dest.writeString(ingredients);
        dest.writeString(sourceUrl);
    }

    public static final Parcelable.Creator<Recipe> CREATOR =
            new Parcelable.Creator<Recipe>() {

                @Override
                public Recipe createFromParcel(Parcel source) {
                    return new Recipe(source);
                }

                @Override
                public Recipe[] newArray(int size) {
                    return new Recipe[0];
                }
            };

    private Recipe(Parcel in) {
        title = in.readString();
        sourceUrl = in.readString();
        ingredients = in.readString();
        thumbnailUrl = in.readString();
    }

    public boolean hasThumbnail() {
        return !thumbnailUrl.equals("");
    }

    public String getImageUrl() {
        return thumbnailUrl;
    }

    public String getTitle() {
        return title.trim();
    }

    public String getIngredients() {
        return ingredients;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }
}
