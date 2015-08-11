package com.wbohn.recipefinder.RecipeList;

import android.app.ActionBar;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import com.squareup.otto.Subscribe;
import com.wbohn.recipefinder.App;
import com.wbohn.recipefinder.Bus.LoadNextRequest;
import com.wbohn.recipefinder.Bus.RecipesReceivedEvent;
import com.wbohn.recipefinder.R;
import com.wbohn.recipefinder.Network.Volley;

public class RecipeListFragment extends ListFragment {
    private static final String TAG = "RecipeListFragment";

    private RecipeAdapter recipeAdapter;
    private EndlessScrollListener endlessScrollListener;

    public RecipeListFragment() {
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
            int numRecipes = savedInstanceState.getInt("numRecipes");
            Recipe recipes[] = new Recipe[numRecipes];

            for (int i = 0; i < numRecipes; i++) {
                recipes[i] = savedInstanceState.getParcelable("recipe_" + String.valueOf(i));
            }
            addRecipesToList(recipes);
        }
        setEmptyText(getString(R.string.empty_text));
        getListView().getEmptyView().setPadding(10, 100, 10, 100);
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Subscribe
    public void onRecipesReceived(RecipesReceivedEvent event) {
        Log.i(TAG, "onRecipesReceived");
        if (!event.isLoadRequest()) {
            recipeAdapter.clear();
            recipeAdapter.notifyDataSetChanged();
            endlessScrollListener.reset();
        }

        addRecipesToList(event.getRecipes());
        if (!event.isLoadRequest()) {
            getListView().setSelection(0);
        }
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
