package com.wbohn.recipefinder.RecipeList;

import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import com.squareup.otto.Subscribe;
import com.wbohn.recipefinder.App;
import com.wbohn.recipefinder.Bus.ErrorEvent;
import com.wbohn.recipefinder.Bus.KeywordsClearedEvent;
import com.wbohn.recipefinder.Bus.LoadNextRequest;
import com.wbohn.recipefinder.Bus.RecipeRequest;
import com.wbohn.recipefinder.Bus.RecipesReceivedEvent;
import com.wbohn.recipefinder.Bus.RefreshRequest;
import com.wbohn.recipefinder.R;
import com.wbohn.recipefinder.Network.Volley;

public class RecipeListFragment extends ListFragment {
    private static final String TAG = "RecipeListFragment";

    private RecipeAdapter recipeAdapter;
    private EndlessScrollListener endlessScrollListener;

    private ProgressBar loadingIcon;
    private TextView emptyText;
    private TextView recipePuppyLink;
    private Menu menu;

    private boolean showingError = false;
    private boolean showingInstructions = true;

    public RecipeListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceSate) {
        super.onCreate(savedInstanceSate);
        setHasOptionsMenu(true);

        if (savedInstanceSate != null) {
            showingError = savedInstanceSate.getBoolean("showingError");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_list, null);

        loadingIcon = (ProgressBar) view.findViewById(R.id.loadingIcon);
        emptyText = (TextView) view.findViewById(R.id.empty_text);
        recipePuppyLink = (TextView) view.findViewById(R.id.link);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        recipeAdapter = new RecipeAdapter(getActivity(), 0);

        setListAdapter(recipeAdapter);

        endlessScrollListener = new EndlessScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                App.getEventBus().post(new LoadNextRequest(page));
            }
        };

        getListView().setOnScrollListener(endlessScrollListener);

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Recipe recipe = recipeAdapter.getItem(position);
                String url = recipe.getSourceUrl();

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });

        if (savedInstanceState != null) {
            showingError = savedInstanceState.getBoolean("showingError");
            showingInstructions = savedInstanceState.getBoolean("showingInstructions");

            int numRecipes = savedInstanceState.getInt("numRecipes");
            Recipe recipes[] = new Recipe[numRecipes];

            for (int i = 0; i < numRecipes; i++) {
                recipes[i] = savedInstanceState.getParcelable("recipe_" + String.valueOf(i));
            }
            addRecipesToList(recipes);

            if (showingError) {
                showError();
                return;
            } else {
                hideError();
                if (showingInstructions) {
                    emptyText.setVisibility(View.VISIBLE);
                    emptyText.setText(getResources().getString(R.string.empty_text));
                }
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        this.menu = menu;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (showingError) {
            menu.findItem(R.id.action_refresh).setVisible(true);
        } else {
            menu.findItem(R.id.action_refresh).setVisible(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        App.getEventBus().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        App.getEventBus().unregister(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        int numRecipes = recipeAdapter.getCount();
        outState.putInt("numRecipes", numRecipes);

        for (int i = 0; i < numRecipes; i++) {
            String key = "recipe_" + String.valueOf(i);
            outState.putParcelable(key, recipeAdapter.getItem(i));
        }

        outState.putBoolean("showingError", showingError);
        outState.putBoolean("showingInstructions", showingInstructions);
    }

    @Subscribe
    public void onRecipeRequest(RecipeRequest request) {
        hideError();
        loadingIcon.setVisibility(View.VISIBLE);
    }

    @Subscribe
    public void onRecipesReceived(RecipesReceivedEvent event) {
        Log.i(TAG, "onRecipesReceived");
        hideError();
        loadingIcon.setVisibility(View.GONE);
        if (menu != null) {
            menu.findItem(R.id.action_refresh).setVisible(false);
        }

        if (!event.isLoadRequest()) {
            recipeAdapter.clear();
            recipeAdapter.notifyDataSetChanged();
            endlessScrollListener.reset();
        }

        addRecipesToList(event.getRecipes());
        if (!event.isLoadRequest()) {
            getListView().setSelection(0);
        }
        showingInstructions = false;
    }

    @Subscribe
    public void onRefreshRequest(RefreshRequest request) {
        hideError();
        loadingIcon.setVisibility(View.VISIBLE);
    }

    @Subscribe
    public void onError(ErrorEvent error) {
        showError();
        if (menu != null) {
            menu.findItem(R.id.action_refresh).setVisible(true);
        }
    }

    @Subscribe
    public void onKeywordsCleared(KeywordsClearedEvent event) {
        if (showingError) {
            hideError();
            if (menu != null) {
                menu.findItem(R.id.action_refresh).setVisible(false);
            }
            if (getListAdapter().getCount() <= 0) {
                if (showingInstructions) {
                    emptyText.setVisibility(View.VISIBLE);
                    emptyText.setText(getResources().getString(R.string.empty_text));
                }
            }
        }
    }

    private void showError() {
        showingError = true;
        showingInstructions = false;
        loadingIcon.setVisibility(View.GONE);
        emptyText.setVisibility(View.VISIBLE);
        emptyText.setText(getResources().getString(R.string.network_error));
        recipePuppyLink.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        showingError = false;
        emptyText.setVisibility(View.GONE);
        recipePuppyLink.setVisibility(View.GONE);
    }

    private void addRecipesToList(Recipe[] recipes) {
        if (recipes.length > 0) {
            for (Recipe r : recipes) {
                recipeAdapter.add(r);
            }
        }
        recipeAdapter.notifyDataSetChanged();
    }

    private static class RecipeAdapter extends ArrayAdapter<Recipe> {
        private Context context;

        public RecipeAdapter(Context context, int resource) {
            super(context, resource);
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Recipe recipe = getItem(position);
            ViewHolder holder;

            ImageLoader imageLoader = Volley.getInstance(context).getImageLoader();

            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.list_item_recipe, parent, false);

                holder = new ViewHolder();
                holder.titleView = (TextView) convertView.findViewById(R.id.textView_title);
                holder.ingredientsView =  (TextView) convertView.findViewById(R.id.textView_ingredients);
                holder.thumbnailView = (NetworkImageView) convertView.findViewById(R.id.imageView);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.titleView.setText(recipe.getTitle());
            holder.ingredientsView.setText(recipe.getIngredients());
            holder.thumbnailView.setDefaultImageResId(R.mipmap.placeholder);

            if (recipe.hasThumbnail()) {
                holder.thumbnailView.setImageUrl(recipe.getImageUrl(), imageLoader);
            }

            return convertView;
        }
    }

    static class ViewHolder {
        TextView titleView;
        TextView ingredientsView;
        NetworkImageView thumbnailView;
    }
}