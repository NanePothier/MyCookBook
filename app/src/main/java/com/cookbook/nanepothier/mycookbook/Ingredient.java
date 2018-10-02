package com.cookbook.nanepothier.mycookbook;

import java.io.Serializable;

/**
 * class Ingredient is used to store an ingredient's name,
 * quantity, quantity unit and default measurement property
 */
public class Ingredient implements Serializable{

    private String ingredientName, quantityUnit;
    private double quantity;
    private String defaultMeasurement;

    public Ingredient(){}

    public Ingredient(String name, double qu, String unit, String defaultM){

        ingredientName = name;
        quantity = qu;
        quantityUnit = unit;
        defaultMeasurement = defaultM;
    }

    public void setName(String name){
        ingredientName = name;
    }

    public void setQuantity(double q){
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

    public double getQuantity(){
        return quantity;
    }

    public String getQuantityUnit(){
        return quantityUnit;
    }

    public String getDefaultMeasurement(){
        return defaultMeasurement;
    }
}
