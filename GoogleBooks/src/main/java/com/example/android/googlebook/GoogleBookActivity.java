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
package com.example.android.googlebook;

import android.app.SearchManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class GoogleBookActivity extends AppCompatActivity {

    public static final String LOG_TAG = GoogleBookActivity.class.getName();
    private ListView bookListView;
    private MyArrayAdapter adapter;
    private TextView textInfo;
    private ProgressBar loading_indicator;
    private LoaderManager loaderManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_activity);

        bookListView = (ListView) findViewById(R.id.list);
        textInfo = (TextView)findViewById(R.id.empty_view);
        loading_indicator = (ProgressBar)findViewById(R.id.loading_indicator);
        loaderManager = getSupportLoaderManager();

        final String URL = "https://www.googleapis.com/books/v1/volumes?q=android&maxResults=20";
        loadGoogleBook(URL);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                textInfo.setVisibility(View.GONE);
                loading_indicator.setVisibility(View.VISIBLE);

                final String URL = "https://www.googleapis.com/books/v1/volumes?q=";
                final String MAX = "&maxResults=20";
                try {
                    query = URLEncoder.encode(query,"utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                loadGoogleBook(URL+query+MAX);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }
    private void loadGoogleBook(final String url){
        if(!isNetworkConnected()){
            if(adapter!=null){
                adapter.clear();
            }
            textInfo.setText(R.string.no_connection);
            textInfo.setVisibility(View.VISIBLE);
            loading_indicator.setVisibility(View.GONE);
            return;
        }
        loaderManager.restartLoader(1, null, new LoaderManager.LoaderCallbacks<ArrayList<BookInfo>>(){
            @Override
            public Loader<ArrayList<BookInfo>> onCreateLoader(int id, Bundle args) {
                if(adapter!=null){
                    adapter.clear();
                }
                Log.e("loadGoogleBook",url);
                return new LoadGoogleBook(GoogleBookActivity.this,url);
            }

            @Override
            public void onLoadFinished(Loader<ArrayList<BookInfo>> loader, ArrayList<BookInfo> data) {
                if(adapter!=null){
                    adapter.clear();
                }
                updateViews(data);
            }

            @Override
            public void onLoaderReset(Loader<ArrayList<BookInfo>> loader) {
                if(adapter!=null){
                    adapter.clear();
                }
            }
        }).forceLoad();
    }
    private void updateViews(ArrayList<BookInfo> info){
        loading_indicator.setVisibility(View.GONE);
        if(info==null || info.isEmpty()){
            textInfo.setText(R.string.no_book_found);
            textInfo.setVisibility(View.VISIBLE);
            return;
        }
        adapter = new MyArrayAdapter(GoogleBookActivity.this, R.layout.list_layout, info);
        bookListView.setAdapter(adapter);
    }
    private boolean isNetworkConnected() {
        // BEGIN_INCLUDE(connect)
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()) {
            boolean wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
            boolean mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
            if(wifiConnected) {
                return true;
            } else if (mobileConnected){
                return true;
            }
        }
        return false;
    }
    private static class LoadGoogleBook extends AsyncTaskLoader<ArrayList<BookInfo>> {
        String url;
        public LoadGoogleBook(Context context, String url) {
            super(context);
            this.url = url;
        }
        @Override
        public ArrayList<BookInfo> loadInBackground() {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return extractBooks(url);
        }
        public ArrayList<BookInfo> extractBooks(String url) {
            ArrayList<BookInfo> books = new ArrayList<>();
            try {
                //JSONObject json = new JSONObject(SAMPLE_JSON_RESPONSE);
                JSONObject json = readJsonFromUrl(url);
                JSONArray jsonArray = json.optJSONArray("items");

                if(jsonArray!=null){
                    for(int i=0; i < jsonArray.length(); i++){
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        JSONObject properties = jsonObject.getJSONObject("volumeInfo");

                        String title = properties.optString("title");
                        String authors = properties.optString("authors");
                        String infoLink = properties.optString("infoLink");
                        String publishedDate = properties.optString("publishedDate");

                        books.add(new BookInfo(title,authors,infoLink,publishedDate));
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            // Return the list of earthquakes
            return books;
        }
        private JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;

            try {
                urlConnection = (HttpURLConnection) new URL(url).openConnection();
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(15000 /* milliseconds */);
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                if (urlConnection.getResponseCode() == 200) {
                    inputStream = urlConnection.getInputStream();
                    BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
                    String jsonText = readAll(rd);
                    JSONObject json = new JSONObject(jsonText);
                    return json;
                } else {
                    Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
                }
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            }
            return null;
        }
        private String readAll(Reader rd) throws IOException {
            StringBuilder sb = new StringBuilder();
            int cp;
            while ((cp = rd.read()) != -1) {
                sb.append((char) cp);
            }
            return sb.toString();
        }
    }
}
