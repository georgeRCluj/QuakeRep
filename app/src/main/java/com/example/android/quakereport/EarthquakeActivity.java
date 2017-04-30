/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.quakereport;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Loader;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.content.AsyncTaskLoader;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static android.R.attr.data;
import static android.R.attr.order;
import static android.webkit.ConsoleMessage.MessageLevel.LOG;

public class EarthquakeActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Earthquake>> {

    // Tag for the log messages
    public static final String LOG_TAG = "CALLING_HISTORY";

    // Constant value for the earthquake loader ID. We can choose any integer. This really only comes into play if you're using multiple loaders.
    private static final int EARTHQUAKE_LOADER_ID = 1;

    // Adapter for the list of earthquakes
    private EarthQuakeAdapter itemsAdapter;

    // URL to query the USGS dataset for earthquake information
    private static final String USGS_REQUEST_URL =
            "https://earthquake.usgs.gov/fdsnws/event/1/query";
    private static final String empty_view_message = "No earthquakes found";
    private static final String no_net_connection_message = "No internet connection";

    // Variable used for listView
    TextView emptyTextView;
    View loadingIndicator;

    @Override
    public Loader<List<Earthquake>> onCreateLoader(int i, Bundle bundle) {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String minMagnitude = sharedPrefs.getString(
                getString(R.string.settings_min_magnitude_key),         // key	String: The name of the preference to retrieve.
                getString(R.string.settings_min_magnitude_default));    // defValue	String: Value to return if this preference does not exist.

        String orderBy = sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default));

        Uri baseUri = Uri.parse(USGS_REQUEST_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        uriBuilder.appendQueryParameter("format", "geojson");
        uriBuilder.appendQueryParameter("limit", "10");
        uriBuilder.appendQueryParameter("minmag", minMagnitude);
        uriBuilder.appendQueryParameter("orderby", orderBy);

        return new EarthquakeLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<Earthquake>> loader, List<Earthquake> earthquakes) {
        // Hide loading indicator because the data has been loaded
        loadingIndicator.setVisibility(View.GONE);

        // Clear the adapter of previous earthquake data
        itemsAdapter.clear();

        // If there is a valid list of {@link Earthquake}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (earthquakes != null && !earthquakes.isEmpty()) {
            itemsAdapter.addAll(earthquakes);
        } else {
            emptyTextView.setText(empty_view_message);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Earthquake>> loader) {
        // Loader reset, so we can clear out our existing data.
        itemsAdapter.clear();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.earthquake_activity);
        // Get a reference to the loadingIndicator, in order to show loading progress on the UI
        loadingIndicator = findViewById(R.id.loading_spinner);
        emptyTextView = (TextView) findViewById(R.id.empty_view);
        ListView listView = (ListView) this.findViewById(R.id.list);

        loadingIndicator.setVisibility(View.VISIBLE);
        final List<Earthquake> mEarthquakes = new ArrayList<>();
        listView.setEmptyView(emptyTextView);

        itemsAdapter = new EarthQuakeAdapter(this, mEarthquakes);
        listView.setAdapter(itemsAdapter);

        // Manage the network connection
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = (activeNetwork != null && activeNetwork.isConnected());
        if (isConnected) {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getLoaderManager();

            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            loaderManager.initLoader(EARTHQUAKE_LOADER_ID, null, this);
        } else {
            loadingIndicator.setVisibility(View.GONE);
            emptyTextView.setText(no_net_connection_message);
        }
        /** THE BELOW CODE APPLIES FOR USING ASYNCTASK WITHOUT LOADER!! DO NOT ERASE - FOR FUTURE USE!
         // Kick off an {@link AsyncTask} to perform the network request
         EarthQuakeAsyncTask task = new EarthQuakeAsyncTask();
         task.execute(USGS_REQUEST_URL); **/

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openWebPage(mEarthquakes.get(position).getUrl());
            }
        });

    }

    public void openWebPage(String url) {
        Uri webpage = Uri.parse(url);
        Intent urlIntent = new Intent(Intent.ACTION_VIEW, webpage);
        if (urlIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(urlIntent);
        }
    }

    // below we initialize the menu button
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
    /** THE BELOW CODE APPLIES FOR USING ASYNCTASK WITHOUT LOADER!! DO NOT ERASE - FOR FUTURE USE!
     * Update the UI with the given earthquake information.

     private class EarthQuakeAsyncTask extends AsyncTask<String, Void, List<Earthquake>> {

    @Override protected List<Earthquake> doInBackground(String... urls) {
    // Don't perform the request if there are no URLs, or the first URL is null.
    if (urls.length < 1 || urls[0] == null) {
    return null;
    }

    // Create the list of earthquakes.
    List<Earthquake> earthquakes = QueryUtils.fetchEarthquakeData(urls[0]);
    return earthquakes;
    }

    /**
     * This method runs on the main UI thread after the background work has been
     * completed. This method receives as input, the return value from the doInBackground()
     * method. First we clear out the adapter, to get rid of earthquake data from a previous
     * query to USGS. Then we update the adapter with the new list of earthquakes,
     * which will trigger the ListView to re-populate its list items.
     *
    @Override protected void onPostExecute(List<Earthquake> earthquakes) {
    // Clear the adapter of previous earthquake data
    itemsAdapter.clear();

    // If there is a valid list of {@link Earthquake}s, then add them to the adapter's
    // data set. This will trigger the ListView to update.
    if (earthquakes != null && !earthquakes.isEmpty()) {
    itemsAdapter.addAll(earthquakes);
    }
    }
    } */
}
