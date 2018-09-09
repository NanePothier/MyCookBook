package com.cookbook.nanepothier.mycookbook;

public class ConversionObject {

    private String measureFrom;
    private String measureTo;
    private String measCategory;
    private double factor;

    public void setMeasureFrom(String measureFrom){
        this.measureFrom = measureFrom;
    }

    public String getMeasureFrom(){
        return this.measureFrom;
    }

    public void setMeasureTo(String measureTo){
        this.measureTo = measureTo;
    }

    public String getMeasureTo(){
        return this.measureTo;
    }

    public void setMeasureCategory(String measureCat){
        measCategory = measureCat;
    }

    public String getMeasureCategory(){
        return measCategory;
    }

    public void setFactor(double factor){
        this.factor = factor;
    }

    public double getFactor(){
        return this.factor;
    }
}
