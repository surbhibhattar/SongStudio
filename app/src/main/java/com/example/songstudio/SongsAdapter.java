package com.example.songstudio;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class SongsAdapter extends ArrayAdapter<Song_details> {
    public SongsAdapter(Context context, ArrayList<Song_details> songs) {
        super(context, 0, songs);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Song_details song_details = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }
        // Lookup view for data population
        TextView song_label = (TextView) convertView.findViewById(R.id.textview);
        // Populate the data into the template view using the data object
        song_label.setText(song_details.name);
        // Return the completed view to render on screen
        return convertView;
    }
}