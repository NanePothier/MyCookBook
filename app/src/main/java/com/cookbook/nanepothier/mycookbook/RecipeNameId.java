package com.cookbook.nanepothier.mycookbook;

import java.util.Comparator;

/**
 * class RecipeNameId is used to store a recipe name
 * and its corresping Id
 * This class is used with the recycler view to display
 * recipes in the Cookbook activity
 * Need to store Id, because Id needs to be send to
 * View Recipe activity when user clicks on a recipe name
 */
public class RecipeNameId {

    private String recipeId;
    private String recipeName;
    private boolean ownsRecipe = true;

    public RecipeNameId(String id, String name){

        recipeId = id;
        recipeName = name;
    }

    public RecipeNameId(String id, String name, boolean ownsRecipe){

        recipeId = id;
        recipeName = name;
        this.ownsRecipe = ownsRecipe;
    }

    public void setRecipeName(String name){
        recipeName = name;
    }

    public String getRecipeName(){
        return recipeName;
    }

    public void setRecipeId(String id){
        recipeId = id;
    }

    public String getRecipeId(){
        return recipeId;
    }

    public void setOwnsRecipe(boolean own){
        ownsRecipe = own;
    }

    public boolean getOwnsRecipe(){
        return ownsRecipe;
    }


}

class SortByName implements Comparator<RecipeNameId>{

    public int compare(RecipeNameId firstObject, RecipeNameId secondObject){

        int firstCharAscii = (int) firstObject.getRecipeName().charAt(0);
        int secCharAscii = (int) secondObject.getRecipeName().charAt(0);

        if(firstCharAscii < secCharAscii){
            return -1;
        }else if(firstCharAscii < secCharAscii){
            return 1;
        }else{
            return 0;
        }
    }
}
