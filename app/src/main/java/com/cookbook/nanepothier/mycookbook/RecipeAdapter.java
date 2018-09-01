package com.cookbook.nanepothier.mycookbook;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    private Context context;
    private ArrayList<String> recipeNames;

    public RecipeAdapter(Context context, ArrayList<String>recipes){

        this.context = context;
        recipeNames = recipes;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recipe_name, parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final String nameAtPos = recipeNames.get(position);

        holder.recipe_name.setText(nameAtPos);

        holder.linLayout.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){

                Intent intent = new Intent(view.getContext(), ViewRecipe.class);
                intent.putExtra("recipe", nameAtPos);

                view.getContext().startActivity(intent);

            }
        });

    }

    @Override
    public int getItemCount() {
        return recipeNames.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView recipe_name;
        LinearLayout linLayout;

        public ViewHolder(View itemView) {
            super(itemView);

            recipe_name = itemView.findViewById(R.id.item_recipe_name);

            linLayout = (LinearLayout) itemView.findViewById(R.id.linearlayoutrecipeitem);
        }
    }
}
