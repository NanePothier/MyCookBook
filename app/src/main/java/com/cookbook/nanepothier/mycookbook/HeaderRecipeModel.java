package com.cookbook.nanepothier.mycookbook;

import java.util.ArrayList;
import java.util.Comparator;

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

    public void setCategory(String category){
        this.category = category;
    }

    public String getCategory(){
        return this.category;
    }

    public void setRecipesArray(ArrayList<RecipeNameId> recipes){
        this.recipes = recipes;
    }

    public ArrayList<RecipeNameId> getRecipesArray() {
        return recipes;
    }

}

class SortByCategory implements Comparator<HeaderRecipeModel>{

    public int compare(HeaderRecipeModel firstModel, HeaderRecipeModel secondModel){

        int firstCharAscii = (int) firstModel.getCategory().charAt(0);
        int secCharAscii = (int) secondModel.getCategory().charAt(0);

        if(firstCharAscii < secCharAscii){
            return -1;
        }else if(firstCharAscii < secCharAscii){
            return 1;
        }else{
            return 0;
        }
    }
}