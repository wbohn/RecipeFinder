package com.wbohn.recipefinder.Search;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.wbohn.recipefinder.App;
import com.wbohn.recipefinder.Bus.IngredientSelectedEvent;
import com.wbohn.recipefinder.R;

public class IngredientDialog extends DialogFragment {
    public static final String TAG = "IngredientDialog";

    private ListView ingredientsList;
    private ArrayAdapter<String> ingredientsAdapter;

    private int buttonPosition;
    private boolean showingSubCategories;

    private IngredientsHelper helper;

    public IngredientDialog() {
    }

    public static IngredientDialog newInstance(int position) {
        IngredientDialog dialog = new IngredientDialog();
        Bundle args = new Bundle();

        args.putInt("buttonPosition", position);
        dialog.setArguments(args);

        return dialog;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        App.getEventBus().register(this);

        buttonPosition = getArguments().getInt("buttonPosition");

        helper = new IngredientsHelper(getResources(), buttonPosition);
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View root = inflater.inflate(R.layout.fragment_ingredient_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setView(root)
                .setTitle(helper.getCategoryTitle())
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveSelectedIngredient();
                    }
                })
                .setNegativeButton("Back", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /* do nothing because this onClick is overridden in onStart
                        to prevent automatically dismissing the dialog */
                    }
                });

        ingredientsList = (ListView) root.findViewById(R.id.list_ingredients);
        ingredientsAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_activated_1);

        ingredientsList.setAdapter(ingredientsAdapter);
        ingredientsList.setOnItemClickListener(menuListener);

        showSubCategories();

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog d = (AlertDialog) getDialog();

        if (d != null) {
            Button negativeButton = d.getButton(DialogInterface.BUTTON_NEGATIVE);
            negativeButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (showingSubCategories) {
                        dismiss();
                    } else {
                        showSubCategories();
                    }
                }
            });
        }
    }

    private AdapterView.OnItemClickListener menuListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (showingSubCategories) {
                showIngredients(position);
            }
        }
    };

    private void saveSelectedIngredient() {
        SparseBooleanArray checked = ingredientsList.getCheckedItemPositions();
        int size = checked.size(); // number of name-value pairs in the array

        String[] selectedIngredients = new String[size];

        for (int i = 0; i < size; i++) {
            int key = checked.keyAt(i);
            boolean value = checked.get(key);
            if (value) { // item has not been unselected
                selectedIngredients[i] = ingredientsAdapter.getItem(key);
            }
        }
        App.getEventBus().post(new IngredientSelectedEvent(selectedIngredients));
    }

    @Override
    public void dismiss() {
        super.dismiss();
        App.getEventBus().unregister(this);
    }

    private void showSubCategories() {
        String[] subCategoryTitles = helper.getSubCategoryTitles();

        Dialog dialog = getDialog();
        if (dialog != null) {
            getDialog().setTitle(helper.getCategoryTitle());
        }

        ingredientsList.clearChoices();
        ingredientsAdapter.clear();
        ingredientsList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        for(String s : subCategoryTitles) {
            ingredientsAdapter.add(s);
        }
        ingredientsAdapter.notifyDataSetChanged();
        showingSubCategories = true;
    }

    private void showIngredients(int position) {
        String[] subCategoryTitles = helper.getSubCategoryTitles();
        getDialog().setTitle(subCategoryTitles[position]);

        ingredientsList.clearChoices();
        ingredientsAdapter.clear();
        ingredientsList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);

        String[] ingredients = helper.getIngredients(position);
        for (String ingredient : ingredients) {
            ingredientsAdapter.add(ingredient);
        }

        ingredientsAdapter.notifyDataSetChanged();
        showingSubCategories = false;
    }
}
