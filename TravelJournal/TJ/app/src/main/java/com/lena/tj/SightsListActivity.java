package com.lena.tj;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lena.tj.dataobjects.DOSight;
import com.lena.tj.db.DbOperations;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class SightsListActivity extends AppCompatActivity {
    private ArrayList<DOSight> sights;
    private String mode;
    private ArrayAdapter<DOSight> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sights_list);

        String json = getIntent().getStringExtra(getString(R.string.travel_data));
        if (json != null) {
            Type type = new TypeToken<ArrayList<DOSight>>() {}.getType();
            sights = new Gson().fromJson(json, type);
            mode = getString(R.string.travel);
            TextView headerTextView = (TextView) findViewById(R.id.sights_header);
            headerTextView.setText(getIntent().getStringExtra(getString(R.string.travel_name)));
        } else {
            mode = getString(R.string.sight);
            loadSights();
        }

        showSights(sights);
    }

    private void loadSights() {
        sights = DbOperations.getAllSights(this);
    }

    private void showSights(final ArrayList<DOSight> sights) {
        arrayAdapter = new ArrayAdapter<DOSight>(this,
                android.R.layout.simple_list_item_1, sights) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);

                text1.setText(sights.get(position).getDescription());
                return view;
            }
        };

        ListView allSightsListView = (ListView) findViewById(R.id.all_sights_listview);
        allSightsListView.setAdapter(arrayAdapter);
        allSightsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long id) {
                DOSight sight = arrayAdapter.getItem(position);

                Context context = getApplicationContext();
                Intent intent = new Intent(context, SightActivity.class);
                if (mode.equals(getString(R.string.travel))) {
                    intent.putExtra(getString(R.string.sight_mode), getString(R.string.travel));
                } else {
                    intent.putExtra(getString(R.string.sight_mode), getString(R.string.sight));
                }
                intent.putExtra(context.getString(R.string.sight), new Gson().toJson(sight));
                startActivity(intent);
            }
        });
    }
}
