package com.cookbook.nanepothier.mycookbook;

import java.util.ArrayList;

public class HeaderRecipeModel {

    private String category;
    private ArrayList<RecipeNameId> recipes;

    public HeaderRecipeModel(String category, ArrayList<RecipeNameId> recipes){

        this.category = category;
        this.recipes = recipes;
    }

    public String getCategory(){
        return this.category;
    }

    public ArrayList<RecipeNameId> getRecipesArray() {
        return recipes;
    }

    // TODO: sort recipes?
}
