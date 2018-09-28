package com.cookbook.nanepothier.mycookbook;

import java.util.ArrayList;

/**
 * class SortService contains functionality to sort categories and recipe names
 */

public class SortService {

    public static void swap(ArrayList<RecipeNameId> data, int index1, int index2)
    {
        RecipeNameId temp = data.get(index1);
        data.get(index1).setRecipeName(data.get(index2).getRecipeName());
        data.get(index1).setRecipeId(data.get(index2).getRecipeId());
        data.get(index1).setOwnsRecipe(data.get(index2).getOwnsRecipe());

        data.get(index2).setRecipeName(temp.getRecipeName());
        data.get(index2).setRecipeId(temp.getRecipeId());
        data.get(index2).setOwnsRecipe(temp.getOwnsRecipe());
    }

    // sort array by comparing recipe names
    public static void sortRecipeNames(ArrayList<RecipeNameId> data)
    {
        int position, scan;
        boolean swapped;//flag to test whether swapping occurred at least once in one pass

        for(position = data.size()-1; position >= 0; position--)
        {
            swapped = false;

            for(scan = 0; scan <= position-1; scan++)
            {
                if(data.get(scan).getRecipeName().compareTo(data.get(scan+1).getRecipeName()) > 0)
                    swap(data, scan, scan+1);
                swapped = true;
            }

            if(!swapped){
                position = -1; //if !swapped is true, it means that the list is already sorted, because no swapping occurred; therefore the algorithm can stop sorting
            }
        }
    }

    public static void swapCategoryObject(ArrayList<HeaderRecipeModel> data, int index1, int index2)
    {
        HeaderRecipeModel temp = data.get(index1);
        data.get(index1).setCategory(data.get(index2).getCategory());
        data.get(index1).setRecipesArray(data.get(index2).getRecipesArray());

        data.get(index2).setCategory(temp.getCategory());
        data.get(index2).setRecipesArray(temp.getRecipesArray());
    }

    // sort array by comparing category names
    public static void sortCategories(ArrayList<HeaderRecipeModel> data)
    {
        int position, scan;
        boolean swapped;//flag to test whether swapping occurred at least once in one pass

        for(position = data.size()-1; position >= 0; position--)
        {
            swapped = false;

            for(scan = 0; scan <= position-1; scan++)
            {
                if(data.get(scan).getCategory().compareTo(data.get(scan+1).getCategory()) > 0)
                    swapCategoryObject(data, scan, scan+1);
                swapped = true;
            }

            if(!swapped){
                position = -1; //if !swapped is true, it means that the list is already sorted, because no swapping occurred; therefore the algorithm can stop sorting
            }
        }
    }


}
