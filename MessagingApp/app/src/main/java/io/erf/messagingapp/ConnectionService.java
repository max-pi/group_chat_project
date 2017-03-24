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

public class ConnectionService extends IntentService {
    LocalBroadcastManager broadcaster;
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
