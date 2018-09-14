package com.cookbook.nanepothier.mycookbook;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
                ingredient.setQuantity(ingObject.getInt("quantity"));
                ingredient.setQuantityUnit(ingObject.getString("quantity_unit"));
                ingredient.setDefaultMeasurement(ingObject.getString("default_meas"));

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

    public static Map<String, ArrayList<RecipeNameId>> parseJSONRecipeNameCategory(String jsonData){

        Map<String, ArrayList<RecipeNameId>> map = new HashMap<>();

        try{

            JSONArray jsonArray = new JSONArray(jsonData);

            for(int x = 0; x < jsonArray.length(); x++){

                JSONObject jObject = jsonArray.getJSONObject(x);
                String categoryName = jObject.getString("category");

                if(jObject.getString("recipe_id").equals("noid") && jObject.getString("recipe_name").equals("noname")){

                    if(!map.containsKey(categoryName)){

                        ArrayList<RecipeNameId> list = new ArrayList<>();
                        map.put(categoryName, list);
                    }
                }else{

                    RecipeNameId nameIdObject = new RecipeNameId(jObject.getString("recipe_id"), jObject.getString("recipe_name"));

                    System.out.println("Category name, recipe name and recipe id: " + categoryName + " " + nameIdObject.getRecipeName() + " " + nameIdObject.getRecipeId());

                    // if map already contains the current category, add current object to that category's list
                    if(map.containsKey(categoryName)){

                        map.get(categoryName).add(nameIdObject);

                        // if this category does not yet exist in the map create new list and add category and list to map
                    }else{

                        ArrayList<RecipeNameId> list = new ArrayList<>();
                        list.add(nameIdObject);
                        map.put(categoryName, list);
                    }
                }
            }
        }catch(JSONException jsonException) {
            jsonException.printStackTrace();
        }
        return map;
    }

    public static ArrayList<ConversionObject> parseJSONConversionArray(String data){

        ArrayList<ConversionObject> conversionArray = new ArrayList<>();

        try{

            JSONArray jsonArray = new JSONArray(data);

            for(int x = 0; x < jsonArray.length(); x++){

                JSONObject jObject = jsonArray.getJSONObject(x);
                ConversionObject convObject = new ConversionObject();

                convObject.setMeasureFrom(jObject.getString("measure_from"));
                convObject.setMeasureTo(jObject.getString("measure_to"));
                convObject.setMeasureCategory(jObject.getString("measure_cat"));
                convObject.setFactor(jObject.getDouble("factor"));

                conversionArray.add(convObject);
            }

        }catch(JSONException jsonException){
            jsonException.printStackTrace();
        }

        return conversionArray;
    }

}//end class
