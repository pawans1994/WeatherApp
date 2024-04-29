package com.androdocs.weatherapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

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
import java.util.Locale;;

public class MainActivity extends AppCompatActivity {
    private NotificationManagerCompat notificationManager;
    private LocationManager locationManager;
    private LocationListener locationListener;

    String cityText = "Raleigh";
    String API = "45f9df705df73346c2878fa217038c4d";
    double lat, lon;
    boolean temp_f = true, gps_on = false;

    TextView statusTxt, tempTxt, temp_minTxt, temp_maxTxt, sunriseTxt, sunsetTxt, windTxt, pressureTxt, humidityTxt, cityName, unit_txt, morn_temp, noon_temp, eve_temp, nigh_temp;
    ImageView unit_img;
    Button search_Button, location_button, user_button;
    LinearLayout changeUnit, open_act_rec, open_plant_care, open_food_rec;

    JSONObject current = null;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        statusTxt = findViewById(R.id.status);
        tempTxt = findViewById(R.id.temp);
        temp_minTxt = findViewById(R.id.temp_min);
        temp_maxTxt = findViewById(R.id.temp_max);
        sunriseTxt = findViewById(R.id.sunrise);
        sunsetTxt = findViewById(R.id.sunset);
        windTxt = findViewById(R.id.wind);
        pressureTxt = findViewById(R.id.pressure);
        humidityTxt = findViewById(R.id.humidity);
        changeUnit = findViewById(R.id.ftoc);
        unit_txt = findViewById(R.id.unit);
        unit_img = findViewById(R.id.fimg);
        cityName = findViewById(R.id.City_Name);
        search_Button = findViewById(R.id.search);
        user_button = findViewById(R.id.user);

        //for temp range
        morn_temp = findViewById(R.id.dailyMorning);
        noon_temp = findViewById(R.id.dailyAfternoon);
        eve_temp = findViewById(R.id.dailyEvening);
        nigh_temp = findViewById(R.id.dailyNight);

        // For getting current location
        location_button = findViewById(R.id.location);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                lat = location.getLatitude();
                lon = location.getLongitude();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

        locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 5, 1, locationListener);

        open_act_rec = findViewById(R.id.open_activity);
        open_plant_care = findViewById(R.id.open_plant);
        open_food_rec = findViewById(R.id.open_food);

        new weatherTask().execute();

        // Search button
        search_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cityText = cityName.getText().toString().trim();
                gps_on = false;
                new weatherTask().execute();
            }
        });

        // Search button
        location_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gps_on = true;
                new weatherTask().execute();
            }
        });

        // Change unit
        changeUnit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (temp_f) {
                    new weatherTask().execute();
                    temp_f = false;
                    unit_txt.setText("Change to °F");
                    unit_img.setImageResource(R.drawable.celcius);
                }
                else {
//                    new weatherTaskF().execute();
                    new weatherTask().execute();
                    temp_f = true;
                    unit_txt.setText("Change to °C");
                    unit_img.setImageResource(R.drawable.fahrenheit);
                }
            }
        });

        // open user profile
        user_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUserProfile();
            }
        });

        // Open activities
        open_act_rec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivitiesRecommend();
            }
        });

        // Open plant
        open_plant_care.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPlantCare();
            }
        });

        // Open food
        open_food_rec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFoodRecommend();
            }
        });

        checkUserProfile();
        new plantNotification().execute();
    }

    protected void onResume(Bundle savedInstanceState) {

    }

    protected void onStart(Bundle savedInstanceState) {

    }

    public void checkUserProfile() {
        try {
            FileInputStream fileInputStream = openFileInput("user_profile.json");
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            openUserProfile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openUserProfile() {
        Intent intent = new Intent(MainActivity.this, user_profile.class);
        intent.putExtra( "lon", lon );
        intent.putExtra( "lat", lat );
        startActivity(intent);
    }

    public void openActivitiesRecommend() {
        Intent intent = new Intent(this, activities_recommend.class);
        intent.putExtra("weather", current.toString() );
        if(temp_f)
            intent.putExtra( "unit", "F" );
        else
            intent.putExtra( "unit", "C" );
        intent.putExtra( "lon", lon );
        intent.putExtra( "lat", lat );
        startActivity(intent);
    }

    public void openPlantCare() {
        Intent intent = new Intent(MainActivity.this, plant_care.class);
        String unit = null;
        if(temp_f)
            unit = "F";
        else
            unit = "C";
        intent.putExtra("unit", unit );
        startActivity(intent);
    }

    public void openFoodRecommend() {
        Intent intent = new Intent(this, food_recommend.class);
        intent.putExtra("weather", current.toString() );
        if(temp_f)
            intent.putExtra( "unit", "F" );
        else
            intent.putExtra( "unit", "C" );
        intent.putExtra( "lon", lon );
        intent.putExtra( "lat", lat );
        startActivity(intent);
    }

    class
    weatherTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* Showing the ProgressBar, Making the main design GONE */
            findViewById(R.id.loader).setVisibility(View.VISIBLE);
            findViewById(R.id.mainContainer).setVisibility(View.GONE);
            findViewById(R.id.errorText).setVisibility(View.GONE);
        }

        protected String doInBackground(String... args) {
            String response;

            if (gps_on) {
                if (temp_f) {
                    response = HttpRequest.excuteGet("https://api.openweathermap.org/data/2.5/onecall?units=imperial&lat=" + lat + "&lon=" + lon + "&appid=" + API);
                } else {
                    response = HttpRequest.excuteGet("https://api.openweathermap.org/data/2.5/onecall?units=metric&lat=" + lat + "&lon=" + lon + "&appid=" + API);
                }

                // To get city name
                String cityFetch = HttpRequest.excuteGet("https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&appid=" + API);
                try {
                    cityName.setText( new JSONObject(cityFetch).getString("name") + ", " + new JSONObject(cityFetch).getJSONObject("sys").getString("country"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                // To lon and lat
                Double lon=0.0, lat=0.0;
                String cityFetch = HttpRequest.excuteGet("https://api.openweathermap.org/data/2.5/weather?q=" + cityName.getText() + "&appid=" + API);
                try {
                    cityName.setText( new JSONObject(cityFetch).getString("name") + ", " + new JSONObject(cityFetch).getJSONObject("sys").getString("country"));
                    lon = new JSONObject(cityFetch).getJSONObject("coord").getDouble("lon");
                    lat = new JSONObject(cityFetch).getJSONObject("coord").getDouble("lat");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (temp_f) {
                    response = HttpRequest.excuteGet("https://api.openweathermap.org/data/2.5/onecall?units=imperial&lat=" + lat + "&lon=" + lon + "&appid=" + API);
                } else {
                    response = HttpRequest.excuteGet("https://api.openweathermap.org/data/2.5/onecall?units=metric&lat=" + lat + "&lon=" + lon + "&appid=" + API);
                }
            }

            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                String unit_symbol;
                if(temp_f){
                    unit_symbol = "°F";
                } else {
                    unit_symbol = "°C";
                }

                JSONObject weatherData = new JSONObject(result);
                current = weatherData.getJSONObject("current");
                JSONObject weather = current.getJSONArray("weather").getJSONObject(0);
                JSONObject daily = weatherData.getJSONArray("daily").getJSONObject(0);
                JSONObject dailyTemp = daily.getJSONObject("temp");

                String temp = current.getString("temp") + unit_symbol;
                String pressure = current.getString("pressure");
                String humidity = current.getString("humidity");
                Long sunrise = current.getLong("sunrise");
                Long sunset = current.getLong("sunset");
                String windSpeed = current.getString("wind_speed");
                String weatherDescription = weather.getString("description");

                String tempMin = "Min Temp: " + dailyTemp.getString("min") + unit_symbol;
                String tempMax = "Max Temp: " + dailyTemp.getString("max") + unit_symbol;
                String morning = dailyTemp.getString("morn") + unit_symbol;
                String day = dailyTemp.getString("day") + unit_symbol;
                String evening = dailyTemp.getString("eve") + unit_symbol;
                String night = dailyTemp.getString("night") + unit_symbol;

                /* Populating extracted data into our views */
                statusTxt.setText(weatherDescription.toUpperCase());
                tempTxt.setText(temp);
                temp_minTxt.setText(tempMin);
                temp_maxTxt.setText(tempMax);
                sunriseTxt.setText(new SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(new Date(sunrise * 1000)));
                sunsetTxt.setText(new SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(new Date(sunset * 1000)));
                windTxt.setText(windSpeed);
                pressureTxt.setText(pressure);
                humidityTxt.setText(humidity);

                morn_temp.setText(morning);
                noon_temp.setText(day);
                eve_temp.setText(evening);
                nigh_temp.setText(night);

                /* Views populated, Hiding the loader, Showing the main design */
                findViewById(R.id.loader).setVisibility(View.GONE);
                findViewById(R.id.mainContainer).setVisibility(View.VISIBLE);
            } catch (JSONException e) {
                findViewById(R.id.loader).setVisibility(View.GONE);
                findViewById(R.id.errorText).setVisibility(View.VISIBLE);
            }
        }
    }

    class
    plantNotification extends AsyncTask<Void, Void, String> {
        protected String doInBackground(Void... args) {
            try {
                Thread.sleep(10000);

                // Load local plant database
                JSONObject plant_local_database = null;

                try {
                    FileInputStream fileInputStream = openFileInput("plant_module.json");
                    InputStreamReader isr = new InputStreamReader(fileInputStream);
                    BufferedReader buffer_reader = new BufferedReader(isr);
                    String readString = buffer_reader.readLine();
                    plant_local_database = new JSONObject(readString.toString());
                    fileInputStream.close();
                } catch (FileNotFoundException ex) {
                    try {
                        plant_local_database = new JSONObject("{\"number_of_plants\":0,\"my_plant_list\":[]}");
                        FileOutputStream fileOutputStream = openFileOutput("plant_module.json", MODE_PRIVATE);
                        fileOutputStream.write(plant_local_database.toString().getBytes());
                        fileOutputStream.close();
                    } catch (JSONException exc) {
                        exc.printStackTrace();
                    } catch (FileNotFoundException exception) {
                        exception.printStackTrace();
                    } catch (IOException exception) {
                        exception.printStackTrace();
                    }
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }

                // Load home location
                double lon=0.0, lat=0.0;
                try {
                    FileInputStream fileInputStream = openFileInput("user_profile.json");
                    InputStreamReader isr = new InputStreamReader(fileInputStream);
                    BufferedReader buffer_reader = new BufferedReader(isr);
                    String readString = buffer_reader.readLine();
                    lon = new JSONObject(readString.toString()).getDouble("lon");
                    lat = new JSONObject(readString.toString()).getDouble("lat");
                    fileInputStream.close();
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }

                // fetch current weather
                JSONObject current = new JSONObject(HttpRequest.excuteGet("https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&appid=" + API));

                boolean rain = current.getJSONArray("weather").getJSONObject(0).getString("main").equals("Rain");
                boolean snow = current.getJSONArray("weather").getJSONObject(0).getString("main").equals("Snow");
                double temp = current.getJSONObject("main").getDouble("temp");

                for(int i=0; i<plant_local_database.getInt("number_of_plants"); i++) {
                    JSONObject plantObject = plant_local_database.getJSONObject(plant_local_database.getJSONArray("my_plant_list").getString(i));
                    double p_low_temp = plantObject.getJSONArray("temp").getDouble(0);
                    double p_high_temp = plantObject.getJSONArray("temp").getDouble(1);
                    boolean p_snow = plantObject.getString("snow").equals("OK");
                    boolean p_rain = plantObject.getString("rain").equals("OK");

                    if( xorGate(rain, p_rain) || xorGate(snow, p_snow) || temp<p_low_temp || temp>p_high_temp ) {
                        notificationManager = NotificationManagerCompat.from(MainActivity.this);
                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                        PendingIntent contentIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, 0);

                        Notification notification = new NotificationCompat.Builder(MainActivity.this, App.CHANNEL_ID)
                                .setSmallIcon(R.drawable.ic_warning)
                                .setContentTitle("Plant needs attention!")
                                .setContentText("Your " + plant_local_database.getJSONArray("my_plant_list").getString(i) + " plant needs attention.")
                                .setContentIntent(contentIntent)
                                .build();
                        notificationManager.notify(i+1, notification);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return "Complete";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            System.out.println(result);
        }
    }

    public double convertToK(double temp, boolean temp_f) {
        if(temp_f)
            return ((temp-32)*(5/9)) + 273.15;
        else
            return temp + 273.15;
    }

    public boolean xorGate(boolean a, boolean b) {
        return (a && !b) || (!a && a);
    }
}