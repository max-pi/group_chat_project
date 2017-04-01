package io.erf.messagingapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static io.erf.messagingapp.MainActivity.MakeRequest;

public class MyGroupsActivity extends AppCompatActivity{

    ListView listView ;
    Button newGroupButton;
    RelativeLayout createGroup;
    EditText createGroupName;
    Button createGroupButton;
    GroupsAdapter adapter;
    BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_groups);
        getGroups();
        newGroupButton = (Button) findViewById(R.id.NewGroupButton);
        createGroup = (RelativeLayout) findViewById(R.id.CreateGroup);
        createGroupName = (EditText) findViewById(R.id.createGroupName);
        createGroupButton = (Button) findViewById(R.id.createGroupButton);

        newGroupButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                createGroup.setVisibility(View.VISIBLE);
                newGroupButton.setVisibility(View.INVISIBLE);
            }
        });

        createGroupButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String newName = createGroupName.getText().toString();
                JSONArray postData = new JSONArray();
                JSONObject jo = new JSONObject();
                try {
                    jo.put("GroupName", newName);
                }
                catch (JSONException e) {
                    System.out.println(e);
                }
                postData.put(jo);
                createGroupName.setText("");
                createNewGroup(postData);
                createGroup.setVisibility(View.INVISIBLE);
                newGroupButton.setVisibility(View.VISIBLE);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(createGroupName.getWindowToken(), 0);
            }
        });




    }

    private void createNewGroup(JSONArray postData){
        MakeRequest("https://erf.io/group/new", MainActivity.Method.POST, postData, new MainActivity.VolleyCallback() {
            @Override
            public void onSuccess(JSONArray response) {
                MessagingGroup grp = new MessagingGroup();
                try {
                    grp.id = response.getJSONObject(0).getInt("Id");
                    grp.name = response.getJSONObject(0).getString("Name");
                } catch (JSONException e) {
                    System.out.println(e);
                }
                adapter.add(grp);
                adapter.notifyDataSetChanged();

            }
            @Override
            public void onError(VolleyError error){
                System.out.println();
            }

        });
    }
    /// add refresh groups with adapter.add (newItem) and notifyDataSetChanged
    private void getGroups() {
        MakeRequest("https://erf.io/group/all", MainActivity.Method.GET, null, new MainActivity.VolleyCallback() {
            @Override
            public void onSuccess(JSONArray response) {
                ArrayList<MessagingGroup> GroupList = new ArrayList<MessagingGroup>();
                for (int i = 0; i < response.length(); i++) {
                    MessagingGroup grp = new MessagingGroup();
                    try {
                        grp.id = response.getJSONObject(i).getInt("Id");
                        grp.name = response.getJSONObject(i).getString("Name");
                    } catch (JSONException e) {
                        System.out.println(e);
                    }
                    GroupList.add(grp);
                }
                listView = (ListView) findViewById(R.id.list);
                adapter = new GroupsAdapter(MyGroupsActivity.this, GroupList);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {

                        MessagingGroup itemValue = (MessagingGroup) listView.getItemAtPosition(position);

                        Toast.makeText(getApplicationContext(),
                                "ID :"+itemValue.id+"  Group Name : " +itemValue.name , Toast.LENGTH_LONG)
                                .show();
                        Intent intent = new Intent(MyGroupsActivity.this, MessagingActivity.class);
                        intent.putExtra("NAME", itemValue.name);
                        intent.putExtra("ID", itemValue.id);
                        startActivity(intent);

                    }

                });

            }
            @Override
            public void onError(VolleyError error){
                System.out.println();
            }

        });
    }


}
