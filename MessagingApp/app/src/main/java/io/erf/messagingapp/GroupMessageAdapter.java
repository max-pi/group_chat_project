package io.erf.messagingapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.security.acl.Group;
import java.util.ArrayList;

/**
 * Created by nathan on 2017-03-17.
 */

public class GroupMessageAdapter extends BaseAdapter {

    Context messageContext;
    ArrayList<GroupMessage> messageList;
    SharedPreferences sharedPref;

    public GroupMessageAdapter(Context context, ArrayList<GroupMessage> messages) {
        messageList = messages;
        messageContext = context;
    }
    private static class MessageViewHolder {
        public TextView senderView;
        public TextView bodyView;
    }
    @Override
    public int getCount() {
        return messageList.size();
    }

    @Override
    public Object getItem(int position) {
        return messageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void add(GroupMessage message){
        //check here to make sure message isn't already in list
        for (int i = 0; i<messageList.size(); i++){
            if (messageList.get(i).id == message.id)
                return;
        }
        messageList.add(message);
        notifyDataSetChanged();
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        MessageViewHolder holder;
        sharedPref = MainActivity.context.getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        if (convertView == null){
            LayoutInflater messageInflater = (LayoutInflater) messageContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = messageInflater.inflate(R.layout.group_message, null);
            holder = new MessageViewHolder();
            holder.senderView = (TextView) convertView.findViewById(R.id.message_sender);
            holder.bodyView = (TextView) convertView.findViewById(R.id.message_body);
            convertView.setTag(holder);

        } else {
            holder = (MessageViewHolder) convertView.getTag();
        }

        GroupMessage message = (GroupMessage) getItem(position);
        holder.bodyView.setText(message.message);

        if (message.user_id == sharedPref.getInt("USER_ID", -1)) {
            holder.senderView.setGravity(Gravity.END);
            holder.bodyView.setGravity(Gravity.END);
            holder.senderView.setText("Me");
        }
        else {
            holder.senderView.setText(message.name);
            holder.senderView.setGravity(Gravity.START);
            holder.bodyView.setGravity(Gravity.START);
        }
        return convertView;

    }
}