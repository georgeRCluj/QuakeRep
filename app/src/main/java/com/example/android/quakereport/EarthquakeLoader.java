package com.example.android.quakereport;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import java.util.List;

/**
 * Loads a list of earthquakes by using an AsyncTask to perform the
 * network request to the given URL.
 */
public class EarthquakeLoader extends AsyncTaskLoader<List<Earthquake>> {

    /** Tag for the log messages */
    public static final String LOG_TAG = "CALLING_HISTORY";

    String[] urls;
    public EarthquakeLoader(Context context, String... urls ) {
        super(context);
        this.urls = urls;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    /**
     * This is on a background thread.
     */
    @Override
    public List<Earthquake> loadInBackground() {
        // Don't perform the request if there are no URLs, or the first URL is null.
        if (urls.length < 1 || urls[0] == null) {
            return null;
        }
        // Create the list of earthquakes.
        List<Earthquake> earthquakes = QueryUtils.fetchEarthquakeData(urls[0]);
        return earthquakes;
    }
}
