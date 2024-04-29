package com.androdocs.weatherapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.androdocs.httprequest.HttpRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class food_recommend extends AppCompatActivity {
    ImageView food_icon;
    Double curr_lon, curr_lat;
    TextView food_name, food_address, food_desc;
    JSONObject weatherData, food_remote_database;
    String unit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.food_recommend);

        food_icon = findViewById(R.id.food_rec_icon);
        food_name = findViewById(R.id.food_rec);
        food_address = findViewById(R.id.food_rec_address);
        food_desc = findViewById(R.id.food_rec_desc);

        readArguments();
        new get_remote_food_database().execute();
    }

    public void readArguments() {
        try {
            weatherData = new JSONObject(getIntent().getExtras().getString("weather"));
            unit = getIntent().getExtras().getString("unit");
            curr_lon = getIntent().getExtras().getDouble("lon");
            curr_lat = getIntent().getExtras().getDouble("lat");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Double convertUnitToC(Double temp, String current) {
        if( current.equals("F") )
            return (temp-32)*5/9 ;
        else
            return temp;
    }

    public void updateFoodView() {
        String status = null;
        Double temp = 0.0;
        String type;

        try {
            status = weatherData.getJSONArray("weather").getJSONObject(0).getString("main");
            temp = convertUnitToC( weatherData.getDouble("temp"), unit);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if( status.equals("Rain") ){
            type = "rain";
        } else if( status.equals("Snow") ){
            type = "snow";
        } else {
            if ( temp < 15.0 ) {
                type = "cold";
            } else if ( temp>=15.0 && temp<=30.0 ) {
                type = "mild";
            } else {
                type = "hot";
            }
        }

        try {
            JSONObject food_4currWeather = food_remote_database.getJSONObject(type);
            boolean found = false;

            for (int i = 0; i < food_4currWeather.length(); i++) {
                JSONObject food = food_4currWeather.getJSONObject(String.valueOf(i));
                double lon = food.getJSONObject("loc").getDouble("lon");
                double lat = food.getJSONObject("loc").getDouble("lat");

                if (calculateDistance(lat, lon) < 5.0) {
                    food_name.setText(food.getString("name"));
                    food_address.setText(food.getString("add"));
                    food_desc.setText(food.getString("desc"));
                    food_address.setVisibility(View.VISIBLE);
                    food_desc.setVisibility(View.VISIBLE);
                    new DownLoadImageTask(food_icon).execute(food.getString("url"));
                    found = true;
                    break;
                }
            }

            if(!found) {
                food_name.setText("Sorry!");
                food_address.setText("We found nothing in the 5 mile radius.");
                food_address.setVisibility(View.VISIBLE);
                food_icon.setImageResource(R.drawable.ic_warning);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public double calculateDistance(double lat, double lon) {
        double lat1 = Math.toRadians(curr_lat);
        double long1 = Math.toRadians(curr_lon);
        double lat2 = Math.toRadians(lat);
        double long2 = Math.toRadians(lon);

        // Haversine Formula
        double dlong = long2 - long1;
        double dlat = lat2 - lat1;

        double a = Math.pow(Math.sin(dlat / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlong / 2),2);

        // Radius of Earth in
        // Kilometers, R = 6371
        // Use R = 3956 for miles

        // Calculate the result
        double r = 3956;
        double c = 2 * Math.asin(Math.sqrt(a));
        double ans = c * r;

        // calculate the result
        return ans;
    }

    private class
    DownLoadImageTask extends AsyncTask<String,Void, Bitmap> {
        ImageView imageView;

        public DownLoadImageTask(ImageView imageView){
            this.imageView = imageView;
        }

        protected Bitmap doInBackground(String...urls){
            String urlOfImage = urls[0];
            Bitmap logo = null;
            try{
                InputStream is = new URL(urlOfImage).openStream();
                logo = BitmapFactory.decodeStream(is);
            }catch(Exception e){
                e.printStackTrace();
            }
            return logo;
        }

        protected void onPostExecute(Bitmap result){
            imageView.setImageBitmap(result);
            imageView.setBackgroundColor(0x3CF1EBF1);
        }
    }

    class
    get_remote_food_database extends AsyncTask<String, Void, String> {
        @Override
        public void onPreExecute() {
            super.onPreExecute();
        }

        public String doInBackground(String... args) {
            String response =  HttpRequest.excuteGet("https://raw.githubusercontent.com/ashishv8097/csc510_SmartWeatherApp/master/food_db.json");
            return response;
        }

        @Override
        public void onPostExecute(String result) {
            try {
                food_remote_database = new JSONObject(result);
                updateFoodView();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
