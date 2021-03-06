package com.cookbook.nanepothier.mycookbook;

/**
 * class ViewCountService is used to count the ingredient
 * and category views that are currently displayed on the
 * screen in the NewRecipe activity
 */
public class ViewCountService {

    private static int ingredientViewCount = 3;
    private static int categoryViewCount = 0;

    public static void incrementIngredientViewCount(){

        ingredientViewCount++;
    }

    public static void decrementIngredientViewCount(){

        if(ingredientViewCount > 3){
            ingredientViewCount--;
        }
    }

    public static void setIngredientViewCount(int number){
        ingredientViewCount = number;
    }

    public static int getIngredientViewCount(){
        return ingredientViewCount;
    }

    public static void incrementCategoryViewCount(){

        categoryViewCount++;
    }

    public static void decrementCategoryViewCount(){

        if(categoryViewCount > 0){

            categoryViewCount--;
        }
    }

    public static void setCategoryViewCount(int number){
        categoryViewCount = number;
    }

    public static int getCategoryViewCount(){
        return categoryViewCount;
    }

}
