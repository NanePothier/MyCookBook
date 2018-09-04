package com.cookbook.nanepothier.mycookbook;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

public class ParseJSON {

    public static ArrayList<String> parseJSONArray(String jsonData, String placeholder){

        ArrayList<String> listItems = new ArrayList<>();

        try{
            System.out.println("parsing json array");

            JSONArray json = new JSONArray(jsonData);

            for(int x = 0; x < json.length(); x++){

                JSONObject jObject = json.getJSONObject(x);
                String s = jObject.getString(placeholder);

                System.out.println("Category string: " + s);

                listItems.add(s);
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        System.out.println("Done parsing JSON array");

        return listItems;
    }

    public static String parseJSON(String jsonData){

        String stringResult = "";

        try{

            JSONObject json = new JSONObject(jsonData);
            stringResult = json.getString("successIndicator");

        }catch(Exception e){
            e.printStackTrace();
        }

        return stringResult;
    }

    public static Recipe parseJSONRecipe(String jsonData, String recipeName, String recipeId){

        Recipe recipe = new Recipe(recipeName, recipeId);

        try{

            JSONObject json = new JSONObject(jsonData);

            recipe.setServings(json.getInt("servings"));
            recipe.setCalories(json.getInt("calories"));
            recipe.setOvenTime(json.getInt("oven_time"));
            recipe.setOvenTemperature(json.getInt("oven_temp"));
            recipe.setInstructions(json.getString("instructions"));
            recipe.setPreparationTime(json.getInt("prep_time"));
            recipe.setTotalTime(json.getInt("total_time"));

            JSONArray ingredientsArray;
            ingredientsArray = json.getJSONArray("ingredients");
            JSONObject ingObject;

            for(int i = 0; i < ingredientsArray.length(); i++){

                Ingredient ingredient = new Ingredient();

                ingObject = ingredientsArray.getJSONObject(i);
                ingredient.setName(ingObject.getString("ingredient_name"));
                ingredient.setQuantity(ingObject.getString("quantity"));
                ingredient.setQuantityUnit(ingObject.getString("quantity_unit"));

                System.out.println("ingredient name in json: " + ingObject.getString("ingredient_name"));

                recipe.addIngredient(ingredient);
            }

            JSONArray categoryArray;
            categoryArray = json.getJSONArray("categories");
            JSONObject catObject;

            for(int j = 0; j < categoryArray.length(); j++){

                Category category = new Category();

                catObject = categoryArray.getJSONObject(j);
                category.setName(catObject.getString("category"));
                category.setCategory(catObject.getString("primary"));

                System.out.println("category flag in json: " + catObject.getString("primary"));

                recipe.addCategory(category);
            }


        }catch(JSONException jsonException){
            jsonException.printStackTrace();
        }

        return recipe;
    }


}//end class
