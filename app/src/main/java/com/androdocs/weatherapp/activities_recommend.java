package com.androdocs.weatherapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class activities_recommend extends AppCompatActivity {
    ImageView cloth_icon, act_icon;
    String gender;
    Double curr_lon, curr_lat;
    TextView gender_text, cloth, cloth_desc, act, act_add, act_desc;
    JSONObject weatherData, cloth_remote_database, activity_remote_database;
    String unit;
    String API = "45f9df705df73346c2878fa217038c4d";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activities_recommend);

        gender_text = findViewById(R.id.cloth_gender);
        cloth_icon = findViewById(R.id.cloth_icon);
        cloth = findViewById(R.id.cloth);
        cloth_desc = findViewById(R.id.cloth_desc);
        act_icon = findViewById(R.id.activity_rec_icon);
        act = findViewById(R.id.activity_rec);
        act_add = findViewById(R.id.act_address);
        act_desc = findViewById(R.id.activity_rec_desc);

        curr_lon = getIntent().getExtras().getDouble("lon");
        curr_lat = getIntent().getExtras().getDouble("lat");

        gender = readGender();
        new weatherTask().execute();
    }

    public String readGender() {
        String gender = null;

        try {
            FileInputStream fileInputStream = openFileInput("user_profile.json");
            InputStreamReader isr = new InputStreamReader(fileInputStream);
            BufferedReader buffer_reader = new BufferedReader(isr);
            String readString = buffer_reader.readLine();
            JSONObject upfJSON = new JSONObject(readString);
            gender = upfJSON.getString("gender");
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return gender;
    }

    public void updateClothingView() {
        String status = null;
        Double temp = 0.0;
        String type;

        if( gender.equals("male") ) {
            gender_text.setText("Male");
        } else {
            gender_text.setText("Female");
        }

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
            cloth.setText(cloth_remote_database.getJSONObject(gender).getJSONObject(type).getString("name"));
            cloth_desc.setText(cloth_remote_database.getJSONObject(gender).getJSONObject(type).getString("desc"));
            cloth_desc.setVisibility(View.VISIBLE);
            new DownLoadImageTask(cloth_icon).execute(cloth_remote_database.getJSONObject(gender).getJSONObject(type).getString("url"));
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

    public void updateActivityView() {
        try {
            boolean found = false;
            for (int i = 0; i < activity_remote_database.length(); i++) {
                JSONObject activity = activity_remote_database.getJSONObject(String.valueOf(i));
                double lon = activity.getJSONObject("loc").getDouble("lon");
                double lat = activity.getJSONObject("loc").getDouble("lat");

                if (calculateDistance(lat, lon) < 5.0) {
                    act.setText(activity.getString("name"));
                    act_add.setText(activity.getString("add"));
                    act_desc.setText(activity.getString("desc"));
                    act_add.setVisibility(View.VISIBLE);
                    act_desc.setVisibility(View.VISIBLE);
                    new DownLoadImageTask(act_icon).execute(activity.getString("url"));
                    found = true;
                    break;
                }
            }

            if(!found){
                act.setText("Sorry!");
                act_add.setText("We found nothing in the 5 mile radius.");
                act_add.setVisibility(View.VISIBLE);
                act_icon.setImageResource(R.drawable.ic_warning);
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
    DownLoadImageTask extends AsyncTask<String,Void, Bitmap>{
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
    get_remote_cloth_database extends AsyncTask<String, Void, String> {
        @Override
        public void onPreExecute() {
            super.onPreExecute();
        }

        public String doInBackground(String... args) {
            String response =  HttpRequest.excuteGet("https://raw.githubusercontent.com/ashishv8097/csc510_SmartWeatherApp/master/cloth_db.json");
            return response;
        }

        @Override
        public void onPostExecute(String result) {
            try {
                cloth_remote_database = new JSONObject(result);
                updateClothingView();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class
    get_remote_activity_database extends AsyncTask<String, Void, String> {
        @Override
        public void onPreExecute() {
            super.onPreExecute();
        }

        public String doInBackground(String... args) {
            String response =  HttpRequest.excuteGet("https://raw.githubusercontent.com/ashishv8097/csc510_SmartWeatherApp/master/activity_db.json");
            return response;
        }

        @Override
        public void onPostExecute(String result) {
            try {
                activity_remote_database = new JSONObject(result);
                updateActivityView();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class
    weatherTask extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... args) {
            String response = HttpRequest.excuteGet("https://api.openweathermap.org/data/2.5/weather?units=metric&lat=" + curr_lat + "&lon=" + curr_lon + "&appid=" + API);
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                weatherData = new JSONObject(result);
                new get_remote_cloth_database().execute();
                new get_remote_activity_database().execute();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
