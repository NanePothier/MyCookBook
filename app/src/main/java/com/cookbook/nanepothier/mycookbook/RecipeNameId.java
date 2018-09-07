package com.cookbook.nanepothier.mycookbook;

public class RecipeNameId {

    String recipeId;
    String recipeName;

    public RecipeNameId(String id, String name){

        recipeId = id;
        recipeName = name;
    }

    public String getRecipeName(){
        return recipeName;
    }

    public String getRecipeId(){
        return recipeId;
    }

}
