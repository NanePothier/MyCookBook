package com.cookbook.nanepothier.mycookbook;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;

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


}//end class
