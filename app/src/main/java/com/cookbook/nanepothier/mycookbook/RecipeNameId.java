package com.cookbook.nanepothier.mycookbook;

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

    public String getRecipeName(){
        return recipeName;
    }

    public String getRecipeId(){
        return recipeId;
    }

    public boolean getOwnsRecipe(){
        return ownsRecipe;
    }
}
