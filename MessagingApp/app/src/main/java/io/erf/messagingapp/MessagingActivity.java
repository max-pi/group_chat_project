package io.erf.messagingapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MessagingActivity extends MainActivity {
    GroupMessageAdapter messageAdapter;
    static Integer groupID;
    ListView messagesView;
    Button joinGroupButton;
    Button leaveGroupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);
        String groupName = (String) getIntent().getStringExtra("NAME");
        groupID = (Integer) getIntent().getIntExtra("ID", -1);
        messageAdapter = new GroupMessageAdapter(this, new ArrayList<GroupMessage>());
        messagesView = (ListView) findViewById(R.id.messages_view);
        messagesView.setAdapter(messageAdapter);

        // Capture the layout's TextView and set the string as its text
        TextView textView = (TextView) findViewById(R.id.messaging_group_name);
        textView.setText("Group: " +groupName);

        final Button SendButton = (Button) findViewById(R.id.SendButton);
        final Button showGroupButton = (Button) findViewById(R.id.showGroupButton);
        joinGroupButton = (Button) findViewById(R.id.joinGroupButton);
        leaveGroupButton = (Button) findViewById(R.id.leaveGroupButton);
        if (sharedPref.getAll().containsKey("Group_" + groupID)){
            leaveGroupButton.setVisibility(View.VISIBLE);
            joinGroupButton.setVisibility(View.INVISIBLE);
        }
        SendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText EditMessage = (EditText)findViewById(R.id.message);
                String message = EditMessage.getText().toString();
                if (message.equals("")) {
                    return;
                }
                JSONArray postData = new JSONArray();
                JSONObject jo = new JSONObject();
                try {
                    jo.put("Body", message);
                    jo.put("GroupId", groupID);
                    jo.put("UserId", MainActivity.USER_ID);
                }
                catch (JSONException e) {
                    System.out.println(e);
                }
                postData.put(jo);
                EditMessage.setText("");
                sendMessage(postData);
            }
        });
        joinGroupButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                JSONArray postData = new JSONArray();
                JSONObject jo = new JSONObject();
                try {
                    jo.put("GroupId", groupID);
                    jo.put("UserId", MainActivity.USER_ID);
                }
                catch (JSONException e) {
                    System.out.println(e);
                }
                postData.put(jo);
                joinGroup(postData);

            }
        });
        leaveGroupButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                JSONArray postData = new JSONArray();
                JSONObject jo = new JSONObject();
                try {
                    jo.put("GroupId", groupID);
                    jo.put("UserId", MainActivity.USER_ID);
                }
                catch (JSONException e) {
                    System.out.println(e);
                }
                postData.put(jo);
                leaveGroup(postData);


            }
        });
        showGroupButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ShowGroup();

            }
        });

        getMessages();
    }
    public void ShowGroup(){
        MakeRequest("https://erf.io/group/" + groupID, Method.GET, null, new VolleyCallback() {
            @Override
            public void onSuccess(JSONArray response) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MessagingActivity.this);
                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MessagingActivity.this, android.R.layout.simple_list_item_1);
                try {
                    JSONArray Users = response.getJSONObject(0).getJSONArray("Users");
                    for (int i = 0; i < Users.length(); i++) {
                        arrayAdapter.add(Users.getJSONObject(i).getString("Name"));
                    }
                }
                catch (JSONException e) {
                    System.out.println(e);
                }

                alertBuilder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.out.println();
                    }
                });
                alertBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alertBuilder.show();
            }
        });
    }

    public void sendMessage(final JSONArray postData) {
        MakeRequest("https://erf.io/group/messages/send", Method.POST, postData, new VolleyCallback() {
            @Override
            public void onSuccess(JSONArray response) {
                getMessages();
                EditText EditMessage = (EditText)findViewById(R.id.message);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(EditMessage.getWindowToken(), 0);

            }

        });
    }

    private void joinGroup(JSONArray postData) {
        MakeRequest("https://erf.io/group/join", Method.POST, postData, new VolleyCallback() {
            @Override
            public void onSuccess(JSONArray response) {
                leaveGroupButton.setVisibility(View.VISIBLE);
                joinGroupButton.setVisibility(View.INVISIBLE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("Group_" + groupID, groupID);
                editor.commit();
            }
        });
    }
    private void leaveGroup(JSONArray postData) {
        MakeRequest("https://erf.io/group/kick", Method.POST, postData, new VolleyCallback() {
            @Override
            public void onSuccess(JSONArray response) {
                leaveGroupButton.setVisibility(View.INVISIBLE);
                joinGroupButton.setVisibility(View.VISIBLE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.remove("Group_" + groupID);
                editor.commit();
            }
        });
    }

    public void getMessages() {
        MakeRequest("https://erf.io/group/messages/" + groupID, Method.GET, null, new VolleyCallback() {
                    @Override
                    public void onSuccess(JSONArray response) {
                        for (int i = 0; i < response.length(); i++) {
                            GroupMessage message = new GroupMessage();
                            try {
                                message.id = response.getJSONObject(i).getInt("Id");
                                message.name = response.getJSONObject(i).getString("Name");
                                message.message = response.getJSONObject(i).getString("Body");
                                message.user_id = response.getJSONObject(i).getInt("UserId");
                            } catch (JSONException e) {
                                System.out.println(e);
                            }

                            messageAdapter.add(message);
                        }
                        messagesView.setSelection(messageAdapter.getCount() - 1);
                    }
                }
        );

    }
}
