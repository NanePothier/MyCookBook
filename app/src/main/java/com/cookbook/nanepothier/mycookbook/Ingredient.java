package com.cookbook.nanepothier.mycookbook;

public class Ingredient {

    String ingredientName, quantityUnit;
    String quantity;

    public Ingredient(){}

    public Ingredient(String name, String qu, String unit){

        ingredientName = name;
        quantity = qu;
        quantityUnit = unit;
    }

    public void setName(String name){
        ingredientName = name;
    }

    public void setQuantity(String q){
        quantity = q;
    }

    public void setQuantityUnit(String unit){
        quantityUnit = unit;
    }

    public String getName(){
        return ingredientName;
    }

    public String getQuantity(){
        return quantity;
    }

    public String getQuantityUnit(){
        return quantityUnit;
    }
}
