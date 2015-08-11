package com.wbohn.recipefinder.Search;

import android.content.Context;
import android.util.AttributeSet;

public class CategoryButton extends SAutoBgButton {
    private int categoryPosition;

    public CategoryButton(Context context) {
        super(context);
    }

    public CategoryButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setCategoryPosition(int position) {
       categoryPosition = position;
    }
    public int getCategoryPosition() {
        return categoryPosition;
    }
}
