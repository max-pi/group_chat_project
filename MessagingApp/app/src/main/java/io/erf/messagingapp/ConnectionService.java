package io.erf.messagingapp;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class ConnectionService extends IntentService {
    LocalBroadcastManager broadcaster;
    SharedPreferences sharedPref;
    int userID;
    static Thread t;

    public ConnectionService() {
        super("ConnectionService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        broadcaster = LocalBroadcastManager.getInstance(this);
        Runnable r = new Runnable() {
            public void run() {
                while (true) {
                    try {
                        getStatus();
                        getNotifications();
                        Thread.sleep(5000);
                    }
                    catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        };
        if (t != null) {
            if (!t.isAlive()) {
                t.start();
            }
        }
        else {
            t = new Thread(r);
            t.start();
        }


    }

    private void getNotifications() {
        sharedPref = getApplicationContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        userID = sharedPref.getInt("USER_ID", -1);
        MakeRequest("https://erf.io/notifications/" + userID, MainActivity.Method.GET, null, new ConnectionService.VolleyCallback() {
            @Override
            public void onSuccess(JSONArray response) {
                Intent intent = new Intent("notification");
                Notification notification;
                ArrayList<Notification> NotificationList= new ArrayList<Notification>();
                try {
                    for (int i = 0; i < response.length(); i++) {
                        notification = new Notification();
                        notification.GroupId = response.getJSONObject(i).getInt("GroupId");
                        notification.SenderName = response.getJSONObject(i).getString("SenderName");
                        notification.GroupName = response.getJSONObject(i).getString("GroupName");
                        notification.message = response.getJSONObject(i).getString("Body");
                        NotificationList.add(notification);
                    }
                } catch (JSONException e){
                    System.out.println("oops");
                }
                intent.putExtra("notifications", NotificationList);
                broadcaster.sendBroadcast(intent);
            }
            public void onError(VolleyError error){
                System.out.println("o no");
            }

        });
    }


    private void getStatus() {
        MakeRequest("https://erf.io/status", MainActivity.Method.GET, null, new ConnectionService.VolleyCallback() {
            @Override
            public void onSuccess(JSONArray response) {
                Intent intent = new Intent("connection");
                intent.putExtra("CONNECTED", true);
                broadcaster.sendBroadcast(intent);
            }
            public void onError(VolleyError error){
                Intent intent = new Intent("connection");
                intent.putExtra("CONNECTED", false);
                broadcaster.sendBroadcast(intent);
            }

        });
    }
    private interface VolleyCallback{
        void onSuccess(JSONArray result);
        void onError(VolleyError error);
    }

    public void MakeRequest(String url, int method, JSONArray postData, final ConnectionService.VolleyCallback callback){

        JsonArrayRequest jsArrRequest = new JsonArrayRequest
                (method, url, postData, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        callback.onSuccess(response);
                    }
                }, new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {

                        callback.onError(error);


                    }
                });
        Volley.newRequestQueue(this).add(jsArrRequest);
    }
}
