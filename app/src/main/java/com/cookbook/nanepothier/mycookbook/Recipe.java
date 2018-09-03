package com.cookbook.nanepothier.mycookbook;

import java.util.ArrayList;

public class Recipe {

    String recipeName;
    String recipeId;
    int servings;
    int preparationTime;
    int ovenTime;
    int ovenTemperature;
    int calories;
    String instructions;
    int totalTime;
    ArrayList<Ingredient> ingredients = new ArrayList<>();
    ArrayList<Category> categories = new ArrayList<>();

    public Recipe(){}

    public Recipe(String name, String id){

        recipeName = name;
        recipeId = id;
    }

    public Recipe(String name, String id, int servings, int prepTime, int ovenTime, int ovenTemp, int calories, String instructions, int totalTime){

        recipeName = name;
        recipeId = id;
        this.servings = servings;
        preparationTime = prepTime;
        this.ovenTime = ovenTime;
        ovenTemperature = ovenTemp;
        this.calories = calories;
        this.instructions = instructions;
        this.totalTime = totalTime;
    }

    public String getRecipeName(){
        return recipeName;
    }

    public void setServings(int s){
        servings = s;
    }

    public int getServings(){
        return servings;
    }

    public void setPreparationTime(int p){
        preparationTime = p;
    }

    public int getPreparationTime(){
        return preparationTime;
    }

    public void setOvenTime(int t){
        ovenTime = t;
    }

    public int getOvenTime(){
        return ovenTime;
    }

    public void setOvenTemperature(int t){
        ovenTemperature = t;
    }

    public int getOvenTemperature(){
        return ovenTemperature;
    }

    public void setCalories(int c){
        calories = c;
    }

    public int getCalories(){
        return calories;
    }

    public void setInstructions(String i){
        instructions = i;
    }

    public String getInstructions(){
        return instructions;
    }

    public void setTotalTime(int t){
        totalTime = t;
    }

    public int getTotalTime(){
        return totalTime;
    }

    public void addIngredient(Ingredient ingredient){

        ingredients.add(ingredient);
    }

    public ArrayList<Ingredient> getIngredientArray(){
        return ingredients;
    }

    public Ingredient getIngredient(int index){
        return ingredients.get(index);
    }

    public void addCategory(Category category){

        categories.add(category);
    }

    public ArrayList<Category> getCategoriesArray(){
        return categories;
    }

    public Category getPrimaryCategory(){

        Category prim = null;

        for(int x = 0; x < categories.size(); x++){

            if(categories.get(x).getCategory()){
                prim = categories.get(x);
            }
        }

        return prim;
    }

}
