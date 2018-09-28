package com.cookbook.nanepothier.mycookbook;

import java.io.Serializable;

/**
 * class Category is used to store a category name and
 * if this category is a primary category
 */
public class Category implements Serializable{

    private String categoryName;
    private Boolean isPrimary;

    public Category(){}

    public Category(String catName, String primary){

        categoryName = catName;
        isPrimaryCategory(primary);
    }

    public void setName(String n){
        categoryName = n;
    }

    public String getName(){
        return categoryName;
    }

    public void setCategory(String c){

        isPrimaryCategory(c);
    }

    public boolean isPrimaryCategory(){
        return isPrimary;
    }

    public void isPrimaryCategory(String primary){

        if(primary.equals("y")){
            isPrimary = true;
        }else{
            isPrimary = false;
        }
    }
}
