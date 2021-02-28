package com.example.quakereport;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;


import androidx.annotation.RequiresApi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.android.quakereport.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class EarthquakeActivity extends AppCompatActivity {
    ArrayList<Earthquake> earthquakes = new ArrayList<>();
    public static final String LOG_TAG = EarthquakeActivity.class.getName();
    public TextView emptyTextView;
    public View loadingView;
    public SwipeRefreshLayout swipeRefresh;
    private RequestQueue mQueue;
    private boolean isConnected;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emptyTextView = findViewById(R.id.empty_view);
        loadingView = findViewById(R.id.loading_indicator);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        swipeRefresh.setRefreshing(false);

        mQueue = Volley.newRequestQueue(this);

        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        httpRequest();

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            @Override
            public void onRefresh() {
                Intent earthquakeIntent = getIntent();
                finish();
                swipeRefresh.setRefreshing(false);
                startActivity(earthquakeIntent);
            }
        });
    }

    public void httpRequest(){
        if (isConnected) {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            String minMagnitude = sharedPrefs.getString(
                    getString(R.string.settings_min_magnitude_key),
                    getString(R.string.settings_min_magnitude_default));


            final String url =
                    "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson"+
                            "&orderby=time&minmag="+minMagnitude;

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>() {
                        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                        @Override
                        public void onResponse(JSONObject response) {
                            loadingView.setVisibility(View.GONE);
                            try {
                                JSONArray jsonArray = response.getJSONArray("features");

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonobject = jsonArray.getJSONObject(i);
                                    JSONObject properties = jsonobject.getJSONObject("properties");
                                    double mag = properties.getDouble("mag");
                                    String place = properties.getString("place");
                                    long timestamp = properties.getLong("time");
                                    String urls = properties.getString("url");
                                    Earthquake earthquake = new Earthquake(mag, place, timestamp, urls);
                                    earthquakes.add(earthquake);
                                    updateUI(earthquakes);
                                }

                            } catch (JSONException e) {
                                Log.e(LOG_TAG, Objects.requireNonNull(e.getMessage()));
                            }
                        }
                    }, new Response.ErrorListener() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(LOG_TAG, Objects.requireNonNull(error.getMessage()));
                }
            });

            mQueue.add(request);
        }
        else {
            loadingView.setVisibility(View.GONE);
            emptyTextView.setText(R.string.no_internet_connection);
            /**swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            @Override
            public void onRefresh() {
            Intent earthquakeIntent = getIntent();
            finish();
            swipeRefresh.setRefreshing(false);
            startActivity(earthquakeIntent);
            swipeRefresh.setRefreshing(false);
            httpRequest();
            }
            });*/
        }
    }


    public void updateUI(final ArrayList<Earthquake> earthquake) {
            ListView earthquakeListView = (ListView) findViewById(R.id.list);

            // Create a new {@link ArrayAdapter} of earthquakes
            final EarthquakeAdapter adapter = new EarthquakeAdapter
                    (EarthquakeActivity.this, earthquake);

            // Set the adapter on the {@link ListView}
            // so the list can be populated in the user interface

            earthquakeListView.setAdapter(adapter);
            earthquakeListView.setEmptyView(emptyTextView);
            earthquakeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Earthquake currentEarthquake = earthquake.get(position);
                    String url = currentEarthquake.getUrl();
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                }
            });
        }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    }


