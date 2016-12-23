/*
package com.lena.tj;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;

import static java.security.AccessController.getContext;

public class SearchActivity extends AppCompatActivity {
    GoogleApiClient mGoogleApiClient;
    Adapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        AutoCompleteTextView textView;

    }

    @Override
    protected void onStop() {
        if( mGoogleApiClient != null && mGoogleApiClient.isConnected() ) {
          //  mAdapter.setGoogleApiClient( null );
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnected( Bundle bundle ) {
        if( mAdapter != null )
            mAdapter.setGoogleApiClient( mGoogleApiClient );*/
/**//*

    }


    class CustomAdapter  extends ArrayAdapter {
        public CustomAdapter(Context context, int resource) {
            super(context, resource);
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {

                    if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
                        Toast.makeText(getContext(), "Not connected", Toast.LENGTH_SHORT).show();
                        return null;
                    }
                    clear();
                    displayPredictiveResults(constraint.toString());

                    return null;
                }

                @Override
                protected void publishResults(CharSequence constraint, Filter.FilterResults results) {
                    notifyDataSetChanged();
                }
            };
        }
    }

}
*/
