package com.cookbook.nanepothier.mycookbook;

import java.util.ArrayList;

public class HeaderRecipeModel {

    private String category;
    private ArrayList<String> recipes;

    public HeaderRecipeModel(String category, ArrayList<String> recipes){

        this.category = category;
        this.recipes = recipes;
    }

    public String getCategory(){
        return this.category;
    }

    public ArrayList<String> getRecipesArray() {
        return recipes;
    }
}
