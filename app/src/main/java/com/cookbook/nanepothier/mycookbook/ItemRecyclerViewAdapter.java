package com.cookbook.nanepothier.mycookbook;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class ItemRecyclerViewAdapter extends RecyclerView.Adapter<ItemRecyclerViewAdapter.ItemViewHolder>{

    class ItemViewHolder extends RecyclerView.ViewHolder {
        private TextView recipeName;

        public ItemViewHolder(View itemView) {
            super(itemView);
            recipeName = (TextView) itemView.findViewById(R.id.item_recipe_name);
        }
    }

    private Context context;
    private ArrayList<RecipeNameId> arrayRecipeNames;

    public ItemRecyclerViewAdapter(Context context, ArrayList<RecipeNameId> arrayList) {
        this.context = context;
        this.arrayRecipeNames = arrayList;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe_name, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        holder.recipeName.setText(arrayRecipeNames.get(position).getRecipeName());
    }

    @Override
    public int getItemCount() {
        return arrayRecipeNames.size();
    }

}
