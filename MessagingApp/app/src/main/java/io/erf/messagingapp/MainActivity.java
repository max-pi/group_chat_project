package io.erf.messagingapp;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.acl.Group;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    static Integer USER_ID;
    SharedPreferences sharedPref;
    BroadcastReceiver receiver;

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter("connection")
        );
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent backintent = new Intent(MainActivity.this , ConnectionService.class);
        startService(backintent);
        sharedPref = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
        final Button button = (Button) findViewById(R.id.createButton);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Boolean s = intent.getBooleanExtra("CONNECTED", false);
                if (s) {
                    setTitle("Connected");
                }
                else {
                    setTitle("Not Connected");
                }
            }
        };

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText EditName = (EditText)findViewById(R.id.name);
                String name = EditName.getText().toString();

                JSONArray postData = new JSONArray();
                JSONObject jo = new JSONObject();
                int userID;
                try {


                    userID = sharedPref.getInt("USER_ID", -1);
                    jo.put("Name", name);
                    jo.put("UserId", userID);
                }
                catch (JSONException e) {
                    System.out.println(e);
                }
                postData.put(jo);
                makeName(postData);
                Intent intent = new Intent(MainActivity.this , MyGroupsActivity.class);
                startActivity(intent);
            }
        });
    }

    public interface Method {
        int DEPRECATED_GET_OR_POST = -1;
        int GET = 0;
        int POST = 1;
        int PUT = 2;
        int DELETE = 3;
        int HEAD = 4;
        int OPTIONS = 5;
        int TRACE = 6;
        int PATCH = 7;
    }

    public interface VolleyCallback{
        void onSuccess(JSONArray result);
    }
    private void makeName(JSONArray postData){
        MakeRequest("https://erf.io/user/new", Method.POST, postData, new VolleyCallback() {
            @Override
            public void onSuccess(JSONArray response) {
                User usr = new User();
                try {
                    usr.id = response.getJSONObject(0).getInt("UserId");
                    usr.name = response.getJSONObject(0).getString("Name");
                    } catch (JSONException e) {
                        System.out.println(e);
                    }

                USER_ID = usr.id;
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("USER_ID", usr.id);
                editor.apply();

            }


        });
    }



    public void MakeRequest(String url, int method, JSONArray postData, final VolleyCallback callback){
        System.out.println("Clicked!");

        JsonArrayRequest jsArrRequest = new JsonArrayRequest
                (method, url, postData, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        callback.onSuccess(response);
                    }
                }, new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {

                        // Error handling
                        System.out.println("Something went wrong!");
                        error.printStackTrace();


                    }
                });
        Volley.newRequestQueue(this).add(jsArrRequest);
    }

}
