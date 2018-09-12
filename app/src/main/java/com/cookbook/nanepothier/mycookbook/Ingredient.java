package com.cookbook.nanepothier.mycookbook;

import java.io.Serializable;

public class Ingredient implements Serializable{

    private String ingredientName, quantityUnit;
    private int quantity;
    private String defaultMeasurement;

    public Ingredient(){}

    public Ingredient(String name, int qu, String unit, String defaultM){

        ingredientName = name;
        quantity = qu;
        quantityUnit = unit;
        defaultMeasurement = defaultM;
    }

    public void setName(String name){
        ingredientName = name;
    }

    public void setQuantity(int q){
        quantity = q;
    }

    public void setQuantityUnit(String unit){
        quantityUnit = unit;
    }

    public void setDefaultMeasurement(String defM){
        defaultMeasurement = defM;
    }

    public String getName(){
        return ingredientName;
    }

    public int getQuantity(){
        return quantity;
    }

    public String getQuantityUnit(){
        return quantityUnit;
    }

    public String getDefaultMeasurement(){
        return defaultMeasurement;
    }
}
