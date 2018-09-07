package com.cookbook.nanepothier.mycookbook;

import android.os.AsyncTask;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;


public class SaveItemTask extends AsyncTask<String, Void, String> {

    String indicator, item;
    String defaultMeasurement, userEmail;

    public SaveItemTask(String indicator, String item, String email, String defaultMeasurement){

        this.indicator = indicator;
        this.item = item;
        userEmail = email;
        this.defaultMeasurement = defaultMeasurement;
    }

    @Override
    protected String doInBackground(String... params){

        InputStream inputStream = null;
        OutputStream outputStream = null;
        HttpURLConnection connection;
        URL url = null;
        String result = "";

        try{

            url = new URL("http://10.0.0.18:9999/mycookbookservlets/SaveItem");

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("indicator", indicator);
            jsonObject.put("item", item);

            if(indicator.equals("ingredient")){
                jsonObject.put("def", defaultMeasurement);
            }else if(indicator.equals("category")){
                jsonObject.put("userEmail", userEmail);
            }

            String send = jsonObject.toString();

            connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setFixedLengthStreamingMode(send.getBytes().length);
            connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");

            connection.connect();
            System.out.println("Connection established");

            outputStream = new BufferedOutputStream(connection.getOutputStream());
            outputStream.write(send.getBytes());
            outputStream.flush();

            int responseCode = connection.getResponseCode();

            if(responseCode == HttpURLConnection.HTTP_OK){

                System.out.println("Connection is ok");
                inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;

                while((line = reader.readLine())!= null){
                    result += line;
                }
            }

            System.out.println("result string: " + result);

        }catch(Exception ioe){
            ioe.printStackTrace();
        }finally{

            try{
                if(inputStream != null){
                    inputStream.close();
                }

                if(outputStream != null){
                    outputStream.close();
                }

            }catch(Exception ie){
                ie.printStackTrace();
            }
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result){

        String finalResult = ParseJSON.parseJSON(result);
        System.out.println("Final response string: " + finalResult);

    }
}
