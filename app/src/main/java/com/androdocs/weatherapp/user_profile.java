package com.androdocs.weatherapp;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.androdocs.httprequest.HttpRequest;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class user_profile extends AppCompatActivity {
    RadioButton male, female;
    RadioButton theme0, theme1, theme2, theme3, theme4;
    RelativeLayout viewActRec, viewUserProfile, viewMain, viewPlantRec;
    LinearLayout viewFoodRec;
    Button set_home, save_up;
    double curr_lat, curr_lon, old_lon, old_lat;
    String home;
    JSONObject upfJSON;
    TextView home_add, home_war;
    String API = "45f9df705df73346c2878fa217038c4d";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_user_profile);

        // Fetch new location
        curr_lon = getIntent().getExtras().getDouble("lon");
        curr_lat = getIntent().getExtras().getDouble("lat");

        // gender = findViewById(R.id.gender_switch);
        male = findViewById(R.id.male_select);
        female = findViewById(R.id.female_select);

        set_home = findViewById(R.id.set_home_location);
        save_up = findViewById(R.id.save_user_profile);

        theme0 = findViewById(R.id.th0);
        theme1 = findViewById(R.id.th1);
        theme2 = findViewById(R.id.th2);
        theme3 = findViewById(R.id.th3);
        theme4 = findViewById(R.id.th4);

        viewActRec =  findViewById(R.id.act_rec_bg);
        viewFoodRec = findViewById(R.id.food_rec_bg);
        viewUserProfile = findViewById(R.id.upf_bg);
        viewMain = findViewById(R.id.main_bg);
        viewPlantRec = findViewById(R.id.plant_care_bg);

        home_add = findViewById(R.id.curr_home);
        home_war = findViewById(R.id.home_updated);

        getOldLocation();
        updateView();

        set_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCurrentLocation();
            }
        });

        save_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserProfile();
                finish();
            }
        });

        theme0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewUserProfile.setBackgroundResource(R.drawable.bg_gradient);
            }
        });

        theme1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewUserProfile.setBackgroundResource(R.drawable.theme1);
            }
        });

        theme2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewUserProfile.setBackgroundResource(R.drawable.theme2);
            }
        });

        theme3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewUserProfile.setBackgroundResource(R.drawable.theme3);
            }
        });

        theme4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewUserProfile.setBackgroundResource(R.drawable.theme4);
            }
        });
    }

    public void getOldLocation() {
        try {
            FileInputStream fileInputStream = openFileInput("user_profile.json");
            InputStreamReader isr = new InputStreamReader(fileInputStream);
            BufferedReader buffer_reader = new BufferedReader(isr);
            String readString = buffer_reader.readLine();
            upfJSON = new JSONObject(readString.toString());
            old_lat = upfJSON.getDouble("lat");
            old_lon = upfJSON.getDouble("lon");
            home =  upfJSON.getString("home");
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            String jsonWritable = "{\"gender\":\"female\",\"lon\":\"" + curr_lat + "\",\"lat\":\"" + curr_lat + "\",\"home\":\"Default\"}";
            try {
                upfJSON = new JSONObject(jsonWritable);
                FileOutputStream fileOutputStream = openFileOutput("user_profile.json", MODE_PRIVATE);
                fileOutputStream.write(jsonWritable.getBytes());
                fileOutputStream.close();
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateView() {
        String gender_fetch = null;
        try {
            gender_fetch = upfJSON.getString("gender");
            home_add.setText(upfJSON.getString("home"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if( gender_fetch.equals("male") ) {
            male.setChecked(true);
            female.setChecked(false);
        } else {
            male.setChecked(false);
            female.setChecked(true);
        }
    }

    public void setCurrentLocation() {
        old_lat = curr_lat;
        old_lon = curr_lon;
        new getCity().execute();
    }

    public void saveUserProfile() {
        try {
            // Read the switch status
            String gender_text;
            if (female.isChecked())
                gender_text = "female";
            else
                gender_text = "male";

            String jsonWritable = "{\"gender\":\"" + gender_text + "\",\"lon\":\"" + old_lon + "\",\"lat\":\"" + old_lat + "\",\"home\":\"" + home + "\"}";
            FileOutputStream fileOutputStream = openFileOutput("user_profile.json", MODE_PRIVATE);
            fileOutputStream.write(jsonWritable.getBytes());
            fileOutputStream.close();
            home_war.setVisibility(View.GONE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setTheme() {
        if(theme0.isChecked()) {
            viewActRec.setBackgroundResource(R.drawable.bg_gradient);
            viewFoodRec.setBackgroundResource(R.drawable.bg_gradient);
            viewUserProfile.setBackgroundResource(R.drawable.bg_gradient);
            viewMain.setBackgroundResource(R.drawable.bg_gradient);
            viewPlantRec.setBackgroundResource(R.drawable.bg_gradient);
        } else if (theme1.isChecked()) {
            viewActRec.setBackgroundResource(R.drawable.theme1);
            viewFoodRec.setBackgroundResource(R.drawable.theme1);
            viewUserProfile.setBackgroundResource(R.drawable.theme1);
            viewMain.setBackgroundResource(R.drawable.theme1);
            viewPlantRec.setBackgroundResource(R.drawable.theme1);
        } else if (theme2.isChecked()) {
            viewActRec.setBackgroundResource(R.drawable.theme2);
            viewFoodRec.setBackgroundResource(R.drawable.theme2);
            viewUserProfile.setBackgroundResource(R.drawable.theme2);
            viewMain.setBackgroundResource(R.drawable.theme2);
            viewPlantRec.setBackgroundResource(R.drawable.theme2);
        } else if (theme3.isChecked()) {
            viewActRec.setBackgroundResource(R.drawable.theme3);
            viewFoodRec.setBackgroundResource(R.drawable.theme3);
            viewUserProfile.setBackgroundResource(R.drawable.theme3);
            viewMain.setBackgroundResource(R.drawable.theme3);
            viewPlantRec.setBackgroundResource(R.drawable.theme3);
        } else if (theme4.isChecked()) {
            viewActRec.setBackgroundResource(R.drawable.theme4);
            viewFoodRec.setBackgroundResource(R.drawable.theme4);
            viewUserProfile.setBackgroundResource(R.drawable.theme4);
            viewMain.setBackgroundResource(R.drawable.theme4);
            viewPlantRec.setBackgroundResource(R.drawable.theme4);
        }
    }

    class
    getCity extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            save_up.setVisibility(View.INVISIBLE);
        }

        protected String doInBackground(String... args) {
            String cityFetch = HttpRequest.excuteGet("https://api.openweathermap.org/data/2.5/weather?lat=" + old_lat + "&lon=" + old_lon + "&appid=" + API);
            return cityFetch;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                home = new JSONObject(result).getString("name");
                save_up.setVisibility(View.VISIBLE);
                home_add.setText(home);
                home_war.setVisibility(View.VISIBLE);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}