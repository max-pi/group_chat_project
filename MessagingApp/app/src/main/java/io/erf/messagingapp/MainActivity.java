package io.erf.messagingapp;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button button = (Button) findViewById(R.id.my_groups_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MakeRequest();
            }
        });
    }


    private void MakeRequest(){
        System.out.println("Clicked!");
        final TextView mTxtDisplay;
        ImageView mImageView;
        mTxtDisplay = (TextView) findViewById(R.id.txtDisplay);
        String url = "https://jsonplaceholder.typicode.com/users";

        JsonArrayRequest jsArrRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        System.out.println("Got Here!");
                        ArrayList<MessagingGroup> GroupList = new ArrayList<MessagingGroup>();
                        for (int i=0; i< response.length(); i++) {
                            MessagingGroup grp = new MessagingGroup();
                            try {
                                grp.id = response.getJSONObject(i).getString("id");
                                grp.name = response.getJSONObject(i).getString("name");
                            } catch (JSONException e){
                                System.out.println(e);
                            }
                            GroupList.add(grp);
                        }
                    }
                }, new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {

                        // Error handling
                        System.out.println("Something went wrong!");
                        error.printStackTrace();


                    }
                }
                );
        Volley.newRequestQueue(this).add(jsArrRequest);
    }
}
