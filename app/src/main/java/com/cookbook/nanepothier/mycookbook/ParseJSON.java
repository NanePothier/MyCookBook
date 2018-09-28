package com.cookbook.nanepothier.mycookbook;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * ParseJSON class implements methods
 * to parse JSON data of various forms
 * coming in from Java servlets
 */

public class ParseJSON {

    /**
     * method parseJSON parses a JSON object containing a single string
     * this method is used when only one string is returned from a Java
     * servlet that indicates whether storing, updating, deleting of data
     * was successful
     *
     * @param jsonData
     * @return
     */
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

    /**
     * method parseJSONArray parses JSON array containing a JSON Object holding one string
     *
     * @param jsonData
     * @param placeholder
     * @return
     */
    public static ArrayList<String> parseJSONArray(String jsonData, String placeholder){

        ArrayList<String> listItems = new ArrayList<>();

        try{
            JSONArray json = new JSONArray(jsonData);

            for(int x = 0; x < json.length(); x++){

                JSONObject jObject = json.getJSONObject(x);
                String s = jObject.getString(placeholder);

                listItems.add(s);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return listItems;
    }

    /**
     * method parseJSONArrayModel parses a JSON array containing JSON objects that contain two strings:
     * recipe Id and recipe name
     *
     * @param jsonData
     * @return
     */
    public static ArrayList<RecipeNameId> parseJSONArrayModel(String jsonData){

        ArrayList<RecipeNameId> listItems = new ArrayList<>();
        RecipeNameId nameIdObject;

        try{
            JSONArray json = new JSONArray(jsonData);

            for(int x = 0; x < json.length(); x++){

                JSONObject jObject = json.getJSONObject(x);
                String id = jObject.getString("recipe_id");
                String name = jObject.getString("recipe_name");

                nameIdObject = new RecipeNameId(id, name);
                listItems.add(nameIdObject);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return listItems;
    }

    /**
     * method parseJSONRecipe parses recipe data
     * it receives a JSON object containing all that data
     * and returns a recipe object
     *
     * @param jsonData
     * @param recipeName
     * @param recipeId
     * @return
     */
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

                recipe.addCategory(category);
            }
        }catch(JSONException jsonException){
            jsonException.printStackTrace();
        }

        return recipe;
    }

    /**
     * method parseJSONRecipeNameCategory parses JSON array containing JSON objects
     * that include the recipe Id, recipe name and recipe category
     * this method is used to display the user's recipes and the categories they belong
     * to in the user's Cookbook UI
     * it generates a map containing all recipes, a map containing only recipes this user
     * created and a map that contains recipes that were shared with this user
     *
     * @param jsonData
     * @return
     */
    public static ArrayList<HashMap<String, ArrayList<RecipeNameId>>> parseJSONRecipeNameCategory(String jsonData){

        HashMap<String, ArrayList<RecipeNameId>> allMap = new HashMap<>();
        HashMap<String, ArrayList<RecipeNameId>> sharedMap = new HashMap<>();
        HashMap<String, ArrayList<RecipeNameId>> ownMap = new HashMap<>();
        ArrayList<HashMap<String, ArrayList<RecipeNameId>>> mapsArray = new ArrayList<>();
        boolean ownsRecipe;

        try{

            JSONArray jsonArray = new JSONArray(jsonData);

            for(int x = 0; x < jsonArray.length(); x++){

                JSONObject jObject = jsonArray.getJSONObject(x);
                String categoryName = jObject.getString("category");

                if(jObject.getString("recipe_id").equals("noid") && jObject.getString("recipe_name").equals("noname")){

                    if(!allMap.containsKey(categoryName)){

                        ArrayList<RecipeNameId> list = new ArrayList<>();
                        allMap.put(categoryName, list);
                        ownMap.put(categoryName, list);
                        sharedMap.put(categoryName, list);
                    }
                }else{

                    if(jObject.getString("own").equals("n")){
                        ownsRecipe = false;
                    }else{
                        ownsRecipe = true;
                    }

                    RecipeNameId nameIdObject = new RecipeNameId(jObject.getString("recipe_id"), jObject.getString("recipe_name"), ownsRecipe);

                    System.out.println("Category name, recipe name and recipe id: " + categoryName + " " + nameIdObject.getRecipeName() + " " + nameIdObject.getRecipeId());

                    /**
                     * add all recipes to allMap
                     * if map already contains the current category, add current object to that category's list
                     * if this category does not yet exist in the map create new list and add category and list to map
                     */
                    if(allMap.containsKey(categoryName)){

                        allMap.get(categoryName).add(nameIdObject);

                    }else{

                        ArrayList<RecipeNameId> list = new ArrayList<>();
                        list.add(nameIdObject);
                        allMap.put(categoryName, list);
                    }

                    // add recipes that the user owns to ownMap
                    if(ownMap.containsKey(categoryName) && ownsRecipe){

                        ownMap.get(categoryName).add(nameIdObject);

                    }else if(!ownMap.containsKey(categoryName) && ownsRecipe){

                        ArrayList<RecipeNameId> list = new ArrayList<>();
                        list.add(nameIdObject);
                        ownMap.put(categoryName, list);
                    }

                    // add recipes that have been shared with this user to sharedMap
                    if(sharedMap.containsKey(categoryName) && !ownsRecipe){

                        sharedMap.get(categoryName).add(nameIdObject);

                    }else if(!sharedMap.containsKey(categoryName) && !ownsRecipe){

                        ArrayList<RecipeNameId> list = new ArrayList<>();
                        list.add(nameIdObject);
                        sharedMap.put(categoryName, list);
                    }
                }
            }

            mapsArray.add(allMap);
            mapsArray.add(sharedMap);
            mapsArray.add(ownMap);

        }catch(JSONException jsonException) {
            jsonException.printStackTrace();
        }

        return mapsArray;
    }

    /**
     * method parseJSONConversionArray parses the JSON array containing all
     * the conversion data needed to perform conversions on ingredient quantities
     *
     * @param data
     * @return
     */
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

}
