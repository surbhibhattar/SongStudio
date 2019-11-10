package com.example.songstudio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.listView);

        new RetrieveFeedTask().execute();



    }


class RetrieveFeedTask extends AsyncTask<Void, Void, String> {

    private Exception exception;

    protected void onPreExecute() {

    }

    protected String doInBackground(Void... urls) {

        //fetch data from given url
        try {
            URL url = new URL("http://starlord.hackerearth.com/studio");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                return stringBuilder.toString();
            }
            finally{
                urlConnection.disconnect();
            }
        }
        catch(Exception e) {
            Log.e("ERROR", e.getMessage(), e);
            return null;
        }
    }

    protected void onPostExecute(String response) {
        if(response == null) {
            response = "THERE WAS AN ERROR";
        }
        Log.i("INFO", response);


            try {

                JSONArray jsonarray = new JSONArray(response);

                // Construct the data source
                ArrayList<Song_details> arrayOfSongs = new ArrayList<Song_details>(); //here Song_details is the data model consisting of several parameters like name, artists, url, etc.

                for (int i = 0; i < jsonarray.length(); i++) {
                    JSONObject jsonobject = jsonarray.getJSONObject(i);

                    Song_details newSong = new Song_details(jsonobject.getString("song"), jsonobject.getString("url"), jsonobject.getString("artists"),  jsonobject.getString("cover_image"));
                  arrayOfSongs.add(newSong);
                }
                ;

                // Create the adapter to convert the array to views
                SongsAdapter adapter = new SongsAdapter(getApplicationContext(), arrayOfSongs);
                // Attach the adapter to a ListView
                listView.setAdapter(adapter);


                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        Song_details currentSong = (Song_details) listView.getItemAtPosition(position);
                        Intent i = new Intent(getApplicationContext(), SecondActivity.class);
                        // sending data to new activity
                        i.putExtra("song", currentSong);
                        startActivity(i);

                    }
                });




            } catch (Exception e) {
                e.printStackTrace();
            }
    }
}
}
