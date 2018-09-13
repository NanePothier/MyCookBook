package com.cookbook.nanepothier.mycookbook;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class SectionRecyclerViewAdapter extends RecyclerView.Adapter<SectionRecyclerViewAdapter.SectionViewHolder> {

    private Context context;
    private ArrayList<HeaderRecipeModel> headerRecipeModelsArray;
    private String userEmail;

    class SectionViewHolder extends RecyclerView.ViewHolder {
        private TextView categoryLabel;
        private RecyclerView itemRecyclerView;

        public SectionViewHolder(View itemView) {
            super(itemView);
            categoryLabel = (TextView) itemView.findViewById(R.id.category_header);
            itemRecyclerView = (RecyclerView) itemView.findViewById(R.id.recipe_recycler_view);
        }
    }

    public SectionRecyclerViewAdapter(Context context, ArrayList<HeaderRecipeModel> array, String userEmail) {
        this.context = context;
        this.headerRecipeModelsArray = array;
        this.userEmail = userEmail;
    }

    @Override
    public SectionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_header, parent, false);
        return new SectionViewHolder(view);
    }


    @Override
    public void onBindViewHolder(SectionViewHolder holder, int position) {
        final HeaderRecipeModel sectionModel = headerRecipeModelsArray.get(position);
        holder.categoryLabel.setText(sectionModel.getCategory());

        //recycler view for items
        holder.itemRecyclerView.setHasFixedSize(true);
        holder.itemRecyclerView.setNestedScrollingEnabled(false);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        holder.itemRecyclerView.setLayoutManager(linearLayoutManager);

        ItemRecyclerViewAdapter adapter = new ItemRecyclerViewAdapter(context, sectionModel.getRecipesArray(), userEmail);
        holder.itemRecyclerView.setAdapter(adapter);
    }

    @Override
    public int getItemCount() {
        return headerRecipeModelsArray.size();
    }
}
