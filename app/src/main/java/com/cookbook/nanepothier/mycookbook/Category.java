package com.cookbook.nanepothier.mycookbook;

public class Category {

    String categoryName;
    Boolean primaryCategory;

    public Category(){}

    public Category(String catName, String primary){

        categoryName = catName;

        if(primary.equals('y')){
            primaryCategory = true;
        }else{
            primaryCategory = false;
        }
    }

    public void setName(String n){
        categoryName = n;
    }

    public String getName(){
        return categoryName;
    }

    public void setCategory(String c){

        if(c.equals('y')){
            primaryCategory = true;
        }else{
            primaryCategory = false;
        }
    }

    public boolean getCategory(){
        return primaryCategory;
    }
}
