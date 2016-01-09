package com.sachinpatney.homeauto.tasks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.net.URL;
import java.net.URLConnection;

public class UpdateImageTask extends AsyncTask<String, Void, Bitmap>{
    TaskHandler<Bitmap> handler;
    public UpdateImageTask(TaskHandler<Bitmap> handler){
        this.handler = handler;
    }
    protected Bitmap doInBackground(String... data) {
        try {
            URL url = new URL(data[0]);
            URLConnection con = url.openConnection();
            con.setConnectTimeout(1000);
            con.setReadTimeout(5000);
            Bitmap bmp = BitmapFactory.decodeStream(con.getInputStream());
            return bmp;
        }catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    protected void onPostExecute(Bitmap result) {
        super.onPostExecute(result);
        this.handler.done(result);
        Log.i("UpdateImage","image updated");
    }
}