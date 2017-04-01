package io.erf.messagingapp;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

import static io.erf.messagingapp.MainActivity.MakeRequest;

public class MessagingActivity extends AppCompatActivity {
    GroupMessageAdapter messageAdapter;
    static Integer groupID;
    ListView messagesView;
    Button joinGroupButton;
    Button leaveGroupButton;
    SharedPreferences sharedPref;
    BroadcastReceiver receiver;
    LinkedList<JSONArray> failedMessageQueue;

    public BroadcastReceiver createReceiver(){
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("connection")) {
                    Boolean s = intent.getBooleanExtra("CONNECTED", false);
                    if (s) {
                        if (failedMessageQueue != null){
                            for (int i = 0; i<failedMessageQueue.size(); i++){
                                sendMessage(failedMessageQueue.poll());
                            }
                        }
                        setTitle("Connected");
                    } else {
                        setTitle("Not Connected");
                    }
                }
                else {
                    ArrayList<Notification> NotificationList = (ArrayList<Notification>) intent.getSerializableExtra("notifications");
                    for (int i = 0; i< NotificationList.size(); i++){

                        int GroupId = NotificationList.get(i).GroupId;
                        if (groupID == GroupId){
                            getMessages();
                        }
                        else {
                            Intent resultIntent = new Intent(MessagingActivity.this, MessagingActivity.class);
                            resultIntent.putExtra("NAME", NotificationList.get(i).GroupName);
                            resultIntent.putExtra("ID", GroupId);
                            TaskStackBuilder stackBuilder = TaskStackBuilder.create(MessagingActivity.this);
                            stackBuilder.addParentStack(MessagingActivity.class);
                            stackBuilder.addNextIntent(resultIntent);
                            PendingIntent resultPendingIntent =
                                    stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                            NotificationCompat.Builder mBuilder =
                                    new NotificationCompat.Builder(MessagingActivity.this)
                                            .setSmallIcon(R.drawable.new_message_icon)
                                            .setContentTitle(NotificationList.get(i).SenderName + " (Group " + GroupId + ")")
                                            .setContentText(NotificationList.get(i).message)
                                            .setContentIntent(resultPendingIntent)
                                            .setAutoCancel(true);
                            NotificationManager mNotificationManager =
                                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            mNotificationManager.notify(GroupId, mBuilder.build());
                        }
                    }

                }
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);
        String groupName = (String) getIntent().getStringExtra("NAME");
        failedMessageQueue = new LinkedList<JSONArray>();
        groupID = (Integer) getIntent().getIntExtra("ID", -1);
        messageAdapter = new GroupMessageAdapter(this, new ArrayList<GroupMessage>());
        messagesView = (ListView) findViewById(R.id.messages_view);
        messagesView.setAdapter(messageAdapter);
        receiver = createReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter("connection")
        );
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter("notification")
        );
        // Capture the layout's TextView and set the string as its text
        TextView textView = (TextView) findViewById(R.id.messaging_group_name);
        textView.setText("Group: " +groupName);

        final Button SendButton = (Button) findViewById(R.id.SendButton);
        final Button showGroupButton = (Button) findViewById(R.id.showGroupButton);
        joinGroupButton = (Button) findViewById(R.id.joinGroupButton);
        leaveGroupButton = (Button) findViewById(R.id.leaveGroupButton);
        sharedPref = getApplicationContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
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
                    jo.put("UserId", sharedPref.getInt("USER_ID", MainActivity.USER_ID));
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
        MakeRequest("https://erf.io/group/" + groupID, MainActivity.Method.GET, null, new MainActivity.VolleyCallback() {
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
            @Override
            public void onError(VolleyError error){
                System.out.println();
            }
        });
    }

    public void sendMessage(final JSONArray postData) {
        MakeRequest("https://erf.io/group/messages/send", MainActivity.Method.POST, postData, new MainActivity.VolleyCallback() {
            @Override
            public void onSuccess(JSONArray response) {
                getMessages();
                EditText EditMessage = (EditText)findViewById(R.id.message);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(EditMessage.getWindowToken(), 0);

            }
            @Override
            public void onError(VolleyError error){
                EditText EditMessage = (EditText)findViewById(R.id.message);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(EditMessage.getWindowToken(), 0);
                failedMessageQueue.add(postData);
                GroupMessage message = new GroupMessage();
                try {
                    message.failed = true;
                    message.id= -1;
                    message.name = sharedPref.getString("NAME", "");
                    message.message = postData.getJSONObject(0).getString("Body");
                    message.user_id = postData.getJSONObject(0).getInt("UserId");
                    message.group_id = groupID;
                } catch (JSONException e) {
                    System.out.println(e);
                }
                messageAdapter.add(message);

                //notify it failed?
            }

        });
    }

    private void joinGroup(JSONArray postData) {
        MakeRequest("https://erf.io/group/join", MainActivity.Method.POST, postData, new MainActivity.VolleyCallback() {
            @Override
            public void onSuccess(JSONArray response) {
                leaveGroupButton.setVisibility(View.VISIBLE);
                joinGroupButton.setVisibility(View.INVISIBLE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("Group_" + groupID, groupID);
                editor.commit();
            }
            @Override
            public void onError(VolleyError error){
                System.out.println();
            }
        });
    }
    private void leaveGroup(JSONArray postData) {
        MakeRequest("https://erf.io/group/kick", MainActivity.Method.POST, postData, new MainActivity.VolleyCallback() {
            @Override
            public void onSuccess(JSONArray response) {
                leaveGroupButton.setVisibility(View.INVISIBLE);
                joinGroupButton.setVisibility(View.VISIBLE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.remove("Group_" + groupID);
                editor.commit();
            }
            @Override
            public void onError(VolleyError error){
                System.out.println();
            }
        });
    }

    public void getMessages() {
        MakeRequest("https://erf.io/group/messages/" + groupID, MainActivity.Method.GET, null, new MainActivity.VolleyCallback() {
                    @Override
                    public void onSuccess(JSONArray response) {
                        messageAdapter.purgeFailed();
                        for (int i = 0; i < response.length(); i++) {
                            GroupMessage message = new GroupMessage();
                            try {
                                message.id = response.getJSONObject(i).getInt("Id");
                                message.name = response.getJSONObject(i).getString("Name");
                                message.message = response.getJSONObject(i).getString("Body");
                                message.user_id = response.getJSONObject(i).getInt("UserId");
                                message.group_id = groupID;
                            } catch (JSONException e) {
                                System.out.println(e);
                            }

                            messageAdapter.add(message);
                        }
                        messagesView.setSelection(messageAdapter.getCount() - 1);
                    }
                    @Override
                    public void onError(VolleyError error){
                        System.out.println();
                    }
                }
        );

    }

    @Override
    protected void onStop() {
        super.onStop();
        MainActivity.disableReceiver = false;

    }
    @Override
    protected void onStart() {
        super.onStart();
        MainActivity.disableReceiver = true;

    }
}
