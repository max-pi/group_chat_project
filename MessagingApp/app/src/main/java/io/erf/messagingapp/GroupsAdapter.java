package io.erf.messagingapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by nathan on 2017-03-06.
 */

public class GroupsAdapter extends ArrayAdapter<MessagingGroup> {
    public GroupsAdapter(Context context, ArrayList<MessagingGroup> users) {
        super(context, 0, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        MessagingGroup group = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.group_item, parent, false);
        }
        // Lookup view for data population
        TextView name = (TextView) convertView.findViewById(R.id.text1);
        // Populate the data into the template view using the data object
        name.setText(MessagingGroup.name);
        name.setTag(MessagingGroup.id);
        // Return the completed view to render on screen
        return convertView;
    }
}