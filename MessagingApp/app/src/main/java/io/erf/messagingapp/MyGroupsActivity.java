package io.erf.messagingapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MyGroupsActivity extends AppCompatActivity {

    ListView listView ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_groups);

        listView = (ListView) findViewById(R.id.list);

        ArrayList<MessagingGroup> groups = (ArrayList<MessagingGroup>) getIntent().getSerializableExtra("GROUPS");

        GroupsAdapter adapter = new GroupsAdapter(this, groups);

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

}
