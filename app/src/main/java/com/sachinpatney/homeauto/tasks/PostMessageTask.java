package com.sachinpatney.homeauto.tasks;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class PostMessageTask extends AsyncTask<String, Void, JSONObject> {
    TaskHandler handler;

    public PostMessageTask(TaskHandler<JSONObject> handler){
        this.handler = handler;
    }

    protected JSONObject doInBackground(String... data) {
        try {
            return new JSONObject(makeRequest(data[0], data[1]));
        } catch (Exception e) {
            return null;
        }
    }

    protected void onPostExecute(JSONObject result) {
        super.onPostExecute(result);
        if(this.handler!=null){
            this.handler.done(result);
        }
    }

    private static String makeRequest(String uri, String json) {
        HttpURLConnection urlConnection;
        String result = null;
        try {
            //Connect
            URLConnection con = new URL(uri).openConnection();
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            urlConnection = (HttpURLConnection) (con);
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setRequestMethod("POST");
            urlConnection.connect();

            //Write
            OutputStream outputStream = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            writer.write(json);
            writer.close();
            outputStream.close();

            //Read
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(
                            urlConnection.getInputStream(),
                            "UTF-8"));

            String line;
            StringBuilder sb = new StringBuilder();

            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }

            bufferedReader.close();
            result = sb.toString();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i("POST", result);
        return result;
    }
}
