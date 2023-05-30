package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText edtSearch;
    private ImageView imgSearch;
    private TextView txtWeatherTemp,txtWeatherCondition,txtCityName,txtWindSpeed,txtHumidity,txtFeelsLike,txtWindDegree,txtMinTemp,txtMaxTemp;

    private TextView txtPrev;
    private String urlCity="https://api.api-ninjas.com/v1/city?name=";

    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // finding id's of view

        edtSearch=findViewById(R.id.edtSearch);
        txtWeatherTemp=findViewById(R.id.txtWeatherTemp);
        txtWeatherCondition=findViewById(R.id.txtWeatherCondition);
        txtCityName=findViewById(R.id.txtCityName);
        txtWindSpeed=findViewById(R.id.txtWindSpeed);
        txtHumidity=findViewById(R.id.txtHumidity);
        txtFeelsLike=findViewById(R.id.txtFeelsLike);
        txtWindDegree=findViewById(R.id.txtWindDegree);
        txtMinTemp=findViewById(R.id.txtMinTemp);
        txtMaxTemp=findViewById(R.id.txtMaxTemp);
        imgSearch=findViewById(R.id.imgSearch);
        progressBar=findViewById(R.id.progressBar);
        txtPrev=findViewById(R.id.txtPrev);


        txtPrev.setText("Previous Result");


        // for screen orientation
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);



        // storing weather data using sharedPreferences for showing default value when activity is launched

        SharedPreferences sharedPreferences=getSharedPreferences("weather_data", Context.MODE_PRIVATE);
        txtWeatherTemp.setText(Math.round(sharedPreferences.getFloat("temp",0)-273)+"°C");
        txtMinTemp.setText(Math.round(sharedPreferences.getFloat("temp_min",0)-273)+"°C");
        txtMaxTemp.setText(Math.round(sharedPreferences.getFloat("temp_max",0)-273)+"°C");
        txtFeelsLike.setText(Math.round(sharedPreferences.getFloat("feels_like",0)-273)+"°C");
        txtCityName.setText((sharedPreferences.getString("city","")));
        txtWindSpeed.setText((sharedPreferences.getFloat("wind_speed",0))+"m/s");
        txtHumidity.setText((sharedPreferences.getFloat("humidity",0)+"%"));
        txtWeatherCondition.setText(sharedPreferences.getString("description",""));
        txtWindDegree.setText(sharedPreferences.getInt("deg",0)+"°");


        imgSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressBar.setVisibility(View.VISIBLE);
                String cityName=edtSearch.getText().toString();
                fetchLatLong(cityName);
            }
        });
    }

    public void fetchLatLong(String cityName)
    {

        // using ninja api to get city latitude, longitude

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest getRequest = new StringRequest(Request.Method.GET, urlCity+cityName,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONArray array=new JSONArray(response);
                            JSONObject object=array.getJSONObject(0);

                            double latitude=object.getDouble("latitude");
                            double longitude=object.getDouble("longitude");

                            fetchWeatherData(latitude,longitude);


                        } catch (JSONException e) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(MainActivity.this, "Not Found..", Toast.LENGTH_SHORT).show();
                        }


                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        progressBar.setVisibility(View.GONE);

                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("X-Api-Key", "l3CB7yIzRyaX+apSTuon9A==AznszXjguj0ELYaM");
                return params;
            }
        };
        queue.add(getRequest);

    }

    public void fetchWeatherData(double latitude, double longitude)
    {


        // using openweathermap api for fetching weather data

        String urlWeather="https://api.openweathermap.org/data/2.5/weather?lat="+latitude+"&lon="+longitude+"&appid=f32e30df3253734be9f21c100befb52c";

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest getRequest = new StringRequest(Request.Method.GET, urlWeather,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject object=new JSONObject(response);

                            JSONArray array=object.getJSONArray("weather");
                            JSONObject object1=array.getJSONObject(0);

                            String main=object1.getString("main");
                            String description=object1.getString("description");

                            // main
                            JSONObject jsonObject=object.getJSONObject("main");

                            double temp=jsonObject.getDouble("temp");
                            double feels_like=jsonObject.getDouble("feels_like");
                            double temp_min=jsonObject.getDouble("temp_min");
                            double temp_max=jsonObject.getDouble("temp_max");
                            double humidity=jsonObject.getDouble("humidity");

                            progressBar.setVisibility(View.GONE);
                            txtPrev.setText("Current Result");

                            txtWeatherCondition.setText(description);
                            txtWeatherTemp.setText(Math.round(temp-273)+"°C");
                            txtCityName.setText(object.getString("name"));
                            txtHumidity.setText(humidity+" %");
                            txtFeelsLike.setText(Math.round(feels_like-273)+"°C");
                            txtMinTemp.setText(Math.round(temp_min-273)+"°C");
                            txtMaxTemp.setText(Math.round(temp_max-273)+"°C");

                            // wind
                            JSONObject jsonObject1=object.getJSONObject("wind");

                            double speed=jsonObject1.getDouble("speed");
                            int deg=jsonObject1.getInt("deg");
                            txtWindSpeed.setText(speed+" m/s");
                            txtWindDegree.setText(deg+"°");


                            SharedPreferences sharedPreferences=getSharedPreferences("weather_data", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor=sharedPreferences.edit();

                            editor.putFloat("temp",(float) temp);
                            editor.putFloat("temp_min",(float) temp_min);
                            editor.putFloat("temp_max",(float) temp_max);
                            editor.putFloat("wind_speed",(float) speed);
                            editor.putFloat("feels_like",(float) feels_like);
                            editor.putFloat("humidity",(float) humidity);
                            editor.putInt("deg",deg);
                            editor.putString("city",object.getString("name"));
                            editor.putString("description",description);

                            editor.apply();


                        } catch (JSONException e) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(MainActivity.this, "Not Found..", Toast.LENGTH_SHORT).show();
                        }


                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this, "Something Went Wrong..", Toast.LENGTH_SHORT).show();

                    }
                }
        );
        queue.add(getRequest);
    }
}