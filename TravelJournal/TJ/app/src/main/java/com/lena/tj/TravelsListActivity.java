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
import android.widget.Toast;

import com.google.gson.Gson;
import com.lena.tj.dataobjects.DOTravel;
import com.lena.tj.db.DbOperations;

import java.util.ArrayList;

public class TravelsListActivity extends AppCompatActivity {
    ArrayList<DOTravel> travels;
    ArrayAdapter<DOTravel> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travels_list);

        showTravels();
    }

    private void showTravels() {
        //TODO: In service or loader
        travels = DbOperations.getAllTravels(this);
        //In service or loader

        if (travels != null && travels.size() != 0) {
            arrayAdapter = new ArrayAdapter<DOTravel>(this,
                    android.R.layout.simple_list_item_1, travels) {
                @NonNull
                @Override
                public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView text1 = (TextView) view.findViewById(android.R.id.text1);

                    text1.setText(travels.get(position).getName());
                    return view;
                }
            };

            ListView allTravelsListView = (ListView) findViewById(R.id.all_travels_listview);
            allTravelsListView.setAdapter(arrayAdapter);
            allTravelsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long id) {
                    DOTravel travel = arrayAdapter.getItem(position);

                    Context context = getApplicationContext();
                    Intent intent = new Intent(context, TravelActivity.class);
                    intent.putExtra(context.getString(R.string.travel), new Gson().toJson(travel));
                    startActivity(intent);
                }
            });
        } else {
            Toast.makeText(this, getString(R.string.travels_no_any_travels), Toast.LENGTH_LONG).show();
        }
    }
}
