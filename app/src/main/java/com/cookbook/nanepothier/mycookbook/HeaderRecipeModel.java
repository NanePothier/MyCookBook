package com.cookbook.nanepothier.mycookbook;

import java.util.ArrayList;

/**
 * class HeaderRecipeModel is used to store a category and an
 * array of recipes that belong to that category
 * this class is used to display the recipes in each category
 * in a recycler view in the Cookbook activity
 */
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
