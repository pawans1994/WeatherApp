package com.androdocs.weatherapp;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import com.androdocs.httprequest.HttpRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class plant_care extends AppCompatActivity {
    JSONObject plant_remote_database, plant_local_database;
    boolean local_database_created = false;
    Button add_plant, delete_plant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.plant_care);

        add_plant = findViewById(R.id.add_plant);
        delete_plant = findViewById(R.id.delete_plant);

        createPlantJSON();
        updateView();

        new get_remote_database().execute();

        // add plant
        add_plant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPlant();
            }
        });
        delete_plant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePlant();
            }
        });
    }

    public void updateView() {
        LinearLayout plant_container, plant_1, plant_2, plant_3, plant_4, plant_5;
        ImageView plant_1_icon, plant_2_icon, plant_3_icon, plant_4_icon, plant_5_icon;
        TextView plant_1_name, plant_2_name, plant_3_name, plant_4_name, plant_5_name;
        TextView plant_1_desc, plant_2_desc, plant_3_desc, plant_4_desc, plant_5_desc;
        Button add_plant_button, delete_plant_button;
        TextView no_plant;
        plant_container = findViewById(R.id.plants_container);

        plant_1 = findViewById(R.id.myplant_1);
        plant_1_icon = findViewById(R.id.picon1);
        plant_1_name = findViewById(R.id.pname1);
        plant_1_desc =findViewById(R.id.pdesc1);

        plant_2 = findViewById(R.id.myplant_2);
        plant_2_icon = findViewById(R.id.picon2);
        plant_2_name = findViewById(R.id.pname2);
        plant_2_desc =findViewById(R.id.pdesc2);

        plant_3 = findViewById(R.id.myplant_3);
        plant_3_icon = findViewById(R.id.picon3);
        plant_3_name = findViewById(R.id.pname3);
        plant_3_desc =findViewById(R.id.pdesc3);

        plant_4 = findViewById(R.id.myplant_4);
        plant_4_icon = findViewById(R.id.picon4);
        plant_4_name = findViewById(R.id.pname4);
        plant_4_desc =findViewById(R.id.pdesc4);

        plant_5 = findViewById(R.id.myplant_5);
        plant_5_icon = findViewById(R.id.picon5);
        plant_5_name = findViewById(R.id.pname5);
        plant_5_desc =findViewById(R.id.pdesc5);

        no_plant = findViewById(R.id.no_plants_added);

        add_plant_button = findViewById(R.id.add_plant);
        delete_plant_button = findViewById(R.id.delete_plant);

        int numPlants = 0;
        try {
            numPlants = plant_local_database.getInt("number_of_plants");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (numPlants == 0){
            plant_5.setVisibility(View.INVISIBLE);
            plant_4.setVisibility(View.INVISIBLE);
            plant_3.setVisibility(View.INVISIBLE);
            plant_2.setVisibility(View.INVISIBLE);
            plant_1.setVisibility(View.INVISIBLE);
            no_plant.setVisibility(View.VISIBLE);
            plant_container.setVisibility(View.INVISIBLE);
            add_plant_button.setVisibility(View.VISIBLE);
            delete_plant_button.setVisibility(View.INVISIBLE);
        } else if (numPlants == 1) {
            plant_5.setVisibility(View.INVISIBLE);
            plant_4.setVisibility(View.INVISIBLE);
            plant_3.setVisibility(View.INVISIBLE);
            plant_2.setVisibility(View.INVISIBLE);
            plant_1.setVisibility(View.VISIBLE);
            no_plant.setVisibility(View.INVISIBLE);
            plant_container.setVisibility(View.VISIBLE);
            add_plant_button.setVisibility(View.VISIBLE);
            delete_plant_button.setVisibility(View.VISIBLE);

            try {
                plant_1_name.setText(plant_local_database.getJSONArray("my_plant_list").getString(0));
                plant_1_desc.setText(buildDesc(plant_local_database.getJSONObject(plant_1_name.getText().toString())));
                new DownLoadImageTask(plant_1_icon).execute(plant_local_database.getJSONObject(plant_1_name.getText().toString()).getString("url"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (numPlants == 2) {
            plant_5.setVisibility(View.INVISIBLE);
            plant_4.setVisibility(View.INVISIBLE);
            plant_3.setVisibility(View.INVISIBLE);
            plant_2.setVisibility(View.VISIBLE);
            plant_1.setVisibility(View.VISIBLE);
            no_plant.setVisibility(View.INVISIBLE);
            plant_container.setVisibility(View.VISIBLE);
            add_plant_button.setVisibility(View.VISIBLE);
            delete_plant_button.setVisibility(View.VISIBLE);

            try {
                plant_1_name.setText(plant_local_database.getJSONArray("my_plant_list").getString(0));
                plant_2_name.setText(plant_local_database.getJSONArray("my_plant_list").getString(1));
                plant_1_desc.setText(buildDesc(plant_local_database.getJSONObject(plant_1_name.getText().toString())));
                plant_2_desc.setText(buildDesc(plant_local_database.getJSONObject(plant_2_name.getText().toString())));
                new DownLoadImageTask(plant_1_icon).execute(plant_local_database.getJSONObject(plant_1_name.getText().toString()).getString("url"));
                new DownLoadImageTask(plant_2_icon).execute(plant_local_database.getJSONObject(plant_2_name.getText().toString()).getString("url"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (numPlants == 3) {
            plant_5.setVisibility(View.INVISIBLE);
            plant_4.setVisibility(View.INVISIBLE);
            plant_3.setVisibility(View.VISIBLE);
            plant_2.setVisibility(View.VISIBLE);
            plant_1.setVisibility(View.VISIBLE);
            no_plant.setVisibility(View.INVISIBLE);
            plant_container.setVisibility(View.VISIBLE);
            add_plant_button.setVisibility(View.VISIBLE);
            delete_plant_button.setVisibility(View.VISIBLE);

            try {
                plant_1_name.setText(plant_local_database.getJSONArray("my_plant_list").getString(0));
                plant_2_name.setText(plant_local_database.getJSONArray("my_plant_list").getString(1));
                plant_3_name.setText(plant_local_database.getJSONArray("my_plant_list").getString(2));
                plant_1_desc.setText(buildDesc(plant_local_database.getJSONObject(plant_1_name.getText().toString())));
                plant_2_desc.setText(buildDesc(plant_local_database.getJSONObject(plant_2_name.getText().toString())));
                plant_3_desc.setText(buildDesc(plant_local_database.getJSONObject(plant_3_name.getText().toString())));
                new DownLoadImageTask(plant_1_icon).execute(plant_local_database.getJSONObject(plant_1_name.getText().toString()).getString("url"));
                new DownLoadImageTask(plant_2_icon).execute(plant_local_database.getJSONObject(plant_2_name.getText().toString()).getString("url"));
                new DownLoadImageTask(plant_3_icon).execute(plant_local_database.getJSONObject(plant_3_name.getText().toString()).getString("url"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (numPlants == 4) {
            plant_5.setVisibility(View.INVISIBLE);
            plant_4.setVisibility(View.VISIBLE);
            plant_3.setVisibility(View.VISIBLE);
            plant_2.setVisibility(View.VISIBLE);
            plant_1.setVisibility(View.VISIBLE);
            no_plant.setVisibility(View.INVISIBLE);
            plant_container.setVisibility(View.VISIBLE);
            add_plant_button.setVisibility(View.VISIBLE);
            delete_plant_button.setVisibility(View.VISIBLE);

            try {
                plant_1_name.setText(plant_local_database.getJSONArray("my_plant_list").getString(0));
                plant_2_name.setText(plant_local_database.getJSONArray("my_plant_list").getString(1));
                plant_3_name.setText(plant_local_database.getJSONArray("my_plant_list").getString(2));
                plant_4_name.setText(plant_local_database.getJSONArray("my_plant_list").getString(3));
                plant_1_desc.setText(buildDesc(plant_local_database.getJSONObject(plant_1_name.getText().toString())));
                plant_2_desc.setText(buildDesc(plant_local_database.getJSONObject(plant_2_name.getText().toString())));
                plant_3_desc.setText(buildDesc(plant_local_database.getJSONObject(plant_3_name.getText().toString())));
                plant_4_desc.setText(buildDesc(plant_local_database.getJSONObject(plant_4_name.getText().toString())));
                new DownLoadImageTask(plant_1_icon).execute(plant_local_database.getJSONObject(plant_1_name.getText().toString()).getString("url"));
                new DownLoadImageTask(plant_2_icon).execute(plant_local_database.getJSONObject(plant_2_name.getText().toString()).getString("url"));
                new DownLoadImageTask(plant_3_icon).execute(plant_local_database.getJSONObject(plant_3_name.getText().toString()).getString("url"));
                new DownLoadImageTask(plant_4_icon).execute(plant_local_database.getJSONObject(plant_4_name.getText().toString()).getString("url"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            plant_5.setVisibility(View.VISIBLE);
            plant_4.setVisibility(View.VISIBLE);
            plant_3.setVisibility(View.VISIBLE);
            plant_2.setVisibility(View.VISIBLE);
            plant_1.setVisibility(View.VISIBLE);
            no_plant.setVisibility(View.INVISIBLE);
            plant_container.setVisibility(View.VISIBLE);
            add_plant_button.setVisibility(View.INVISIBLE);
            delete_plant_button.setVisibility(View.VISIBLE);

            try {
                plant_1_name.setText(plant_local_database.getJSONArray("my_plant_list").getString(0));
                plant_2_name.setText(plant_local_database.getJSONArray("my_plant_list").getString(1));
                plant_3_name.setText(plant_local_database.getJSONArray("my_plant_list").getString(2));
                plant_4_name.setText(plant_local_database.getJSONArray("my_plant_list").getString(3));
                plant_5_name.setText(plant_local_database.getJSONArray("my_plant_list").getString(4));
                plant_1_desc.setText(buildDesc(plant_local_database.getJSONObject(plant_1_name.getText().toString())));
                plant_2_desc.setText(buildDesc(plant_local_database.getJSONObject(plant_2_name.getText().toString())));
                plant_3_desc.setText(buildDesc(plant_local_database.getJSONObject(plant_3_name.getText().toString())));
                plant_4_desc.setText(buildDesc(plant_local_database.getJSONObject(plant_4_name.getText().toString())));
                plant_5_desc.setText(buildDesc(plant_local_database.getJSONObject(plant_5_name.getText().toString())));
                new DownLoadImageTask(plant_1_icon).execute(plant_local_database.getJSONObject(plant_1_name.getText().toString()).getString("url"));
                new DownLoadImageTask(plant_2_icon).execute(plant_local_database.getJSONObject(plant_2_name.getText().toString()).getString("url"));
                new DownLoadImageTask(plant_3_icon).execute(plant_local_database.getJSONObject(plant_3_name.getText().toString()).getString("url"));
                new DownLoadImageTask(plant_4_icon).execute(plant_local_database.getJSONObject(plant_4_name.getText().toString()).getString("url"));
                new DownLoadImageTask(plant_5_icon).execute(plant_local_database.getJSONObject(plant_5_name.getText().toString()).getString("url"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void addPlant() {
        JSONArray dbArrJSON, userArrJSON;

        // Creating list of plants which is in remote, but not in local
        String[] plantList = null;
        try {
            userArrJSON = plant_local_database.getJSONArray("my_plant_list");
            List userPlantList = new ArrayList();
            for(int i = 0; i < userArrJSON.length(); i++)
                userPlantList.add(userArrJSON.getString(i));

            dbArrJSON = plant_remote_database.getJSONArray("plant_list");
            plantList = new String[dbArrJSON.length()];
            int x=0;
            for(int i = 0; i < dbArrJSON.length(); i++)
                if( !userPlantList.contains(dbArrJSON.getString(i)) )
                    plantList[x++] = dbArrJSON.getString(i);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Creating a dialog
        final AlertDialog.Builder plant_selector = new AlertDialog.Builder(this);
        View plantAddView = getLayoutInflater().inflate(R.layout.plant_selector, null);
        plant_selector.setTitle("Select a plant:");
        final Spinner plantAddSpinner = (Spinner) plantAddView.findViewById(R.id.plantListSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, plantList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // when user selects a plant
        plant_selector.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String plantSelected = plantAddSpinner.getSelectedItem().toString();
                try {
                    plant_local_database.put("number_of_plants",plant_local_database.getInt("number_of_plants")+1 );
                    plant_local_database.put("my_plant_list", plant_local_database.getJSONArray("my_plant_list").put(plantSelected) );
                    plant_local_database.put(plantSelected, plant_remote_database.get(plantSelected));
                    updateView();
                    FileOutputStream fileOutputStream = new FileOutputStream(new File(getFilesDir(), "plant_module.json"));
                    fileOutputStream.write(plant_local_database.toString().getBytes());
                    fileOutputStream.close();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        });
        plantAddSpinner.setAdapter(adapter);
        plant_selector.setView(plantAddView);
        plant_selector.show();
    }

    public void deletePlant() {
        JSONArray userArrJSON;
        HashMap<String, Integer> plantHashMap = new HashMap<String, Integer>();
        String[] plantList = null;

        try {
            userArrJSON = plant_local_database.getJSONArray("my_plant_list");
            plantList = new String[userArrJSON.length()];
            for(int i = 0; i < userArrJSON.length(); i++) {
                plantList[i] = userArrJSON.getString(i);
                plantHashMap.put(plantList[i], i);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final AlertDialog.Builder plant_selector = new AlertDialog.Builder(this);
        View plantAddView = getLayoutInflater().inflate(R.layout.plant_selector, null);
        plant_selector.setTitle("Select a plant:");
        final Spinner plantAddSpinner = (Spinner) plantAddView.findViewById(R.id.plantListSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, plantList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        plant_selector.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String plantSelected = plantAddSpinner.getSelectedItem().toString();
                try {
                    JSONArray tempPlantArrJSON = plant_local_database.getJSONArray("my_plant_list");
                    HashMap<String, Integer> plantHashMap = new HashMap<String, Integer>();

                    plant_local_database.put("number_of_plants",plant_local_database.getInt("number_of_plants")-1 );
                    String[] mpl = new String[plant_local_database.getInt("number_of_plants")];
                    int indexer=0;
                    for(int i = 0; i < tempPlantArrJSON.length(); i++) {
                        if ( tempPlantArrJSON.getString(i) != plantSelected ) {
                            mpl[indexer++] = tempPlantArrJSON.getString(i);
                        }
                    }

                    plant_local_database.put("my_plant_list", new JSONArray(mpl));

                    plant_local_database.remove(plantSelected);
                    updateView();
                    FileOutputStream fileOutputStream = new FileOutputStream(new File(getFilesDir(), "plant_module.json"));
                    fileOutputStream.write(plant_local_database.toString().getBytes());
                    fileOutputStream.close();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        });
        plantAddSpinner.setAdapter(adapter);
        plant_selector.setView(plantAddView);
        plant_selector.show();
    }

    public void createPlantJSON() {
        try {
            if (!local_database_created) {
                FileInputStream fileInputStream = openFileInput("plant_module.json");
                InputStreamReader isr = new InputStreamReader(fileInputStream);
                BufferedReader buffer_reader = new BufferedReader(isr);
                String readString = buffer_reader.readLine();
                plant_local_database = new JSONObject(readString.toString());
                local_database_created = true;
                fileInputStream.close();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            try {
                plant_local_database = new JSONObject("{\"number_of_plants\":0,\"my_plant_list\":[]}");
                local_database_created = true;
                FileOutputStream fileOutputStream = openFileOutput("plant_module.json", MODE_PRIVATE);
                fileOutputStream.write(plant_local_database.toString().getBytes());
                fileOutputStream.close();
            } catch (JSONException ex) {
                ex.printStackTrace();
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String buildDesc(JSONObject details) {
        String unit = getIntent().getExtras().getString("unit");
        Double from, to;
        String rain, snow;
        String ret = null;

        try {
            from = convertUnit(details.getJSONArray("temp").getDouble(0),unit);
            to = convertUnit(details.getJSONArray("temp").getDouble(1), unit);
            rain = details.getString("rain");
            snow = details.getString("snow");
            ret = "Ideal temperature from " + from.toString() + unit + " to " + to.toString() + unit + ", " + rain + " with rain, and " + snow + " with snow.";
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public Double convertUnit(Double temp, String to) {
        if( to.equals("F") )
            return ((temp-273.15)*(9/5)) + 32;
        else
            return temp-273.15;
    }

    class
    get_remote_database extends AsyncTask<String, Void, String> {
        @Override
        public void onPreExecute() {
            super.onPreExecute();
            add_plant.setVisibility(View.GONE);
        }

        public String doInBackground(String... args) {
            String response =  HttpRequest.excuteGet("https://raw.githubusercontent.com/ashishv8097/csc510_SmartWeatherApp/master/plant_db.json");
            return response;
        }

        @Override
        public void onPostExecute(String result) {
            try {
                plant_remote_database = new JSONObject(result);
                add_plant.setVisibility(View.VISIBLE);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
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
        }
    }
}
