package io.erf.messagingapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.acl.Group;
import java.util.ArrayList;

public class MessagingActivity extends MainActivity {
    GroupMessageAdapter messageAdapter;
    static Integer groupID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);
        String groupName = (String) getIntent().getStringExtra("NAME");
        groupID = (Integer) getIntent().getIntExtra("ID", -1);
//        TODO make id an int
        messageAdapter = new GroupMessageAdapter(this, new ArrayList<GroupMessage>());
        final ListView messagesView = (ListView) findViewById(R.id.messages_view);
        messagesView.setAdapter(messageAdapter);

        // Capture the layout's TextView and set the string as its text
        TextView textView = (TextView) findViewById(R.id.messaging_group_name);
        textView.setText("Group: " +groupName);

        final Button button = (Button) findViewById(R.id.SendButton);

        button.setOnClickListener(new View.OnClickListener() {
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
        getMessages();
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

    public void getMessages() {

        MakeRequest("https://erf.io/group/messages/" + groupID, Method.GET, null, new VolleyCallback() {
                    @Override
                    public void onSuccess(JSONArray response) {
                        for (int i = 0; i < response.length(); i++) {
                            GroupMessage message = new GroupMessage();
                            try {
                                message.id = response.getJSONObject(i).getInt("Id");
                                message.name = response.getJSONObject(i).getString("Name"); // todo change when name returned
                                message.message = response.getJSONObject(i).getString("Body");
                            } catch (JSONException e) {
                                System.out.println(e);
                            }

                            messageAdapter.add(message);
                        }
                    }
                }
        );

    }
}
