package com.lena.tj;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class IconChooserActivity extends ListActivity {
    public static String RESULT_ICON_ID = "icon id";
    public String[] iconsNames;
    private TypedArray icons;
    private List<Icon> iconList;
    private LatLng latLng; // transfer to another method in MapsActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        latLng = getIntent().getExtras().getParcelable(getString(R.string.sight_point));
        populateCountryList();
        ArrayAdapter<Icon> adapter = new IconListArrayAdapter(this, iconList);
        setListAdapter(adapter);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra(RESULT_ICON_ID, iconsNames[position]);
                returnIntent.putExtra(getString(R.string.sight_point), latLng);
                setResult(RESULT_OK, returnIntent);
                icons.recycle(); //recycle images
                finish();
            }
        });
    }

    private void populateCountryList() {
        iconList = new ArrayList<>();
        iconsNames = getResources().getStringArray(R.array.icon_names);
        icons = getResources().obtainTypedArray(R.array.icon_drawables);
        for (int i = 0; i < iconsNames.length; i++) {
            iconList.add(new Icon(icons.getDrawable(i)));
        }
    }

    public class Icon {
        private Drawable flag;

        public Icon(Drawable flag) {
            this.flag = flag;
        }

        public Drawable getFlag() {
            return flag;
        }
    }

    public class IconListArrayAdapter extends ArrayAdapter<Icon> {

        private final List<Icon> list;
        private final Activity context;

        class ViewHolder {
            protected ImageView flag;
        }

        public IconListArrayAdapter(Activity context, List<Icon> list) {
            super(context, R.layout.activity_icon_dialog_row, list);
            this.context = context;
            this.list = list;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = null;

            if (convertView == null) {
                LayoutInflater inflator = context.getLayoutInflater();
                view = inflator.inflate(R.layout.activity_icon_dialog_row, null);
                final ViewHolder viewHolder = new ViewHolder();
                viewHolder.flag = (ImageView) view.findViewById(R.id.flag);
                view.setTag(viewHolder);
            } else {
                view = convertView;
            }

            ViewHolder holder = (ViewHolder) view.getTag();
            holder.flag.setImageDrawable(list.get(position).getFlag());
            return view;
        }
    }
}
