package com.cookbook.nanepothier.mycookbook;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class ItemRecyclerViewAdapter extends RecyclerView.Adapter<ItemRecyclerViewAdapter.ItemViewHolder>{

    private Context context;
    private ArrayList<RecipeNameId> arrayRecipeNames;
    private String userEmail;

    class ItemViewHolder extends RecyclerView.ViewHolder {
        private TextView recipeName;

        public ItemViewHolder(View itemView) {
            super(itemView);
            recipeName = (TextView) itemView.findViewById(R.id.item_recipe_name);
        }
    }

    public ItemRecyclerViewAdapter(Context context, ArrayList<RecipeNameId> arrayList, String userEmail) {
        this.context = context;
        this.arrayRecipeNames = arrayList;
        this.userEmail = userEmail;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe_name, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        holder.recipeName.setText(arrayRecipeNames.get(position).getRecipeName());

        final String name = holder.recipeName.getText().toString();
        final String id = arrayRecipeNames.get(position).getRecipeId();

        holder.recipeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Pressed recipe name " + name + " Recipe id is: " + id);
                Intent intent = new Intent(context, ViewRecipe.class);
                intent.putExtra("recipe_name", name);
                intent.putExtra("user_email", userEmail);
                intent.putExtra("recipe_id", id);
                intent.putExtra("action", "coming_from_cookbook");
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayRecipeNames.size();
    }

}
