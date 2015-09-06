package com.wbohn.recipefinder.Search;


import android.app.FragmentManager;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.wbohn.recipefinder.App;
import com.wbohn.recipefinder.Bus.IngredientSelectedEvent;
import com.wbohn.recipefinder.Bus.RecipeRequest;
import com.wbohn.recipefinder.Bus.RefreshRequest;
import com.wbohn.recipefinder.R;

import java.util.ArrayList;

public class KeywordFragment extends Fragment {
    private static final String SEARCH_SPACER = "   ";
    private CategoryAdapter categoryAdapter;
    private GridView gridView;

    private EditText keywordEditText;
    private ImageView addKeywordButton;
    private TextView keywords;
    private ImageView cancelButton;
    private ImageView expandImage;
    private LinearLayout expandButton;

    private RelativeLayout content;

    private ArrayList<String> currentKeywords;
    private ArrayList<String> currentIngredients;

    private boolean expanded = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            currentKeywords = savedInstanceState.getStringArrayList("keywords");
            currentIngredients = savedInstanceState.getStringArrayList("ingredients");
            expanded = savedInstanceState.getBoolean("expanded");
        } else {
            currentKeywords = new ArrayList<String>();
            currentIngredients = new ArrayList<String>();
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_keyword, container, false);

        categoryAdapter = new CategoryAdapter(getActivity(), getResources(), getFragmentManager() );

        gridView = (GridView) root.findViewById(R.id.gridview);
        gridView.setAdapter(categoryAdapter);

        keywordEditText = (EditText) root.findViewById(R.id.editText_keyword);
        addKeywordButton = (ImageView) root.findViewById(R.id.addKeyword);
        keywords = (TextView) root.findViewById(R.id.textView_keywords);
        cancelButton = (ImageView) root.findViewById(R.id.button_cancel);
        expandButton = (LinearLayout) root.findViewById(R.id.expand_button);
        expandImage = (ImageView) root.findViewById(R.id.expand_image);

        content = (RelativeLayout) root.findViewById(R.id.content);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentKeywords.clear();
                currentIngredients.clear();
                showKeywords();
            }
        });

        addKeywordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentKeywords.add(0, keywordEditText.getText().toString());
                showKeywords();
                keywordEditText.setText("");
                App.getEventBus().post(new RecipeRequest(currentKeywords, currentIngredients));
            }
        });

        expandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleContent();
            }
        });

        if (!expanded) {
            hideContent();
        }

        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onResume() {
        super.onResume();
        App.getEventBus().register(this);
        showKeywords();
    }

    @Override
    public void onPause() {
        super.onPause();
        App.getEventBus().unregister(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("keywords", currentKeywords);
        outState.putStringArrayList("ingredients", currentIngredients);
        outState.putBoolean("expanded", expanded);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            App.getEventBus().post(new RefreshRequest(currentKeywords, currentIngredients));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Subscribe
    public void onIngredientEvent(IngredientSelectedEvent event) {
        for (String s : event.getIngredients()) {
            if (s != null) {
                currentIngredients.add(0, s);
            }
        }

        showKeywords();
        App.getEventBus().post(new RecipeRequest(currentKeywords, currentIngredients));
    }

    private void toggleContent() {
        if (content.isShown()) {
            hideContent();
        } else {
            showContent();
        }
    }

    private void showContent() {
        expandImage.setImageResource(R.drawable.ic_action_collapse);
        content.setVisibility(View.VISIBLE);
        expanded = true;
    }

    private void hideContent() {
        expandImage.setImageResource(R.drawable.ic_action_expand);
        content.setVisibility(View.GONE);
        keywordEditText.clearFocus();
        expanded = false;
    }

    private  void showKeywords() {
        String displayText = "";
        for (String s : currentKeywords) {
            displayText += s +  "   ";
        }
        for (String s : currentIngredients) {
            displayText += s +  "   ";
        }
        keywords.setText(displayText);
    }

    private static class CategoryAdapter extends BaseAdapter {
        private Context context;
        private Resources resources;
        private FragmentManager fragmentManager;

        public CategoryAdapter(Context context, Resources resources, FragmentManager fragmentManager) {
            super();
            this.context = context;
            this.resources = resources;
            this.fragmentManager = fragmentManager;
        }

        @Override
        public int getCount() {
            return 6;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CategoryButton categoryButton;

            String[] categoryTitles = resources.getStringArray(R.array.category_titles);
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(context);

                categoryButton = new CategoryButton(context);

                categoryButton.setText(categoryTitles[position]);
                categoryButton.setTextColor(resources.getColor(R.color.icons));
                categoryButton.setBackgroundDrawable(resources.getDrawable(R.drawable.button_shape));
                categoryButton.setCategoryPosition(position);

                categoryButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CategoryButton clickedButton = (CategoryButton) v;
                        int position = clickedButton.getCategoryPosition();

                        IngredientDialog dialog = IngredientDialog.newInstance(position);
                        dialog.show(fragmentManager, "ingredients_dialog");
                    }
                });

            } else {
                categoryButton = (CategoryButton) convertView;
            }

            return categoryButton;
        }
    }
}
