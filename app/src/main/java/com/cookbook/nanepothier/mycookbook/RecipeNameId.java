package com.cookbook.nanepothier.mycookbook;

public class RecipeNameId {

    String recipeId;
    String recipeName;
    boolean ownsRecipe = true;

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
