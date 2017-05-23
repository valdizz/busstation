package com.example.valdizz.busstation.Fragments;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.TextView;

import com.example.valdizz.busstation.Database.DatabaseAccess;
import com.example.valdizz.busstation.Database.FavoriteStationsCursorLoader;
import com.example.valdizz.busstation.Database.FoundStationsCursorLoader;
import com.example.valdizz.busstation.R;
import com.example.valdizz.busstation.SheduleActivity;

public class RouteStationListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    DatabaseAccess databaseAccess;
    SimpleCursorAdapter scRouteStationAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.routestation_list_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initializeContentLoader();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("DDD", getActivity().getClass().getSimpleName());
        if (getActivity().getClass().getSimpleName().equals("FoundStationsActivity")) {
            EditText userFilter = (EditText) getActivity().findViewById(R.id.userFilter);
            Log.d("DDD", userFilter.getText().toString());
            scRouteStationAdapter.getFilter().filter(userFilter.getText().toString());

            userFilter.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    scRouteStationAdapter.getFilter().filter(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            scRouteStationAdapter.setFilterQueryProvider(new FilterQueryProvider() {
                @Override
                public Cursor runQuery(CharSequence constraint) {
                    return databaseAccess.getFoundStations(new String[]{(constraint == null || constraint.length() == 0 ? "%%" : "%" + constraint.toString() + "%")});
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        databaseAccess.close();
    }

    private void initializeContentLoader(){
        databaseAccess = DatabaseAccess.getInstance(getActivity());
        databaseAccess.open();

        String[] from = new String[]{"route_number", "route_name", "station_name"};
        int[] to = new int[]{R.id.tvRSrouteNum, R.id.tvRSroute, R.id.tvRSstation};
        scRouteStationAdapter = new SimpleCursorAdapter(getActivity(), R.layout.routestation_item, null, from, to, 0);
        scRouteStationAdapter.setViewBinder(new RouteStationAdapterViewBinder());
        setListAdapter(scRouteStationAdapter);
        getLoaderManager().initLoader(0, null, this);
    }

    private class RouteStationAdapterViewBinder implements SimpleCursorAdapter.ViewBinder{
        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            String color = cursor.getString(cursor.getColumnIndex("route_color"));
            String number = cursor.getString(cursor.getColumnIndex("route_number"));
            if (view.getId() == R.id.tvRSrouteNum){
                ((GradientDrawable)view.getBackground().getCurrent()).setColor(Color.parseColor("#" + color));
                ((TextView)view).setText(number);
                view.setTag(color);
                return true;
            }
            return false;
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent intentShedule = new Intent(getActivity(), SheduleActivity.class);
        intentShedule.putExtra("route_num", ((TextView)v.findViewById(R.id.tvRSrouteNum)).getText());
        intentShedule.putExtra("route_name", ((TextView)v.findViewById(R.id.tvRSroute)).getText());
        intentShedule.putExtra("route_color", (v.findViewById(R.id.tvRSrouteNum)).getTag().toString());
        intentShedule.putExtra("station_name", ((TextView)v.findViewById(R.id.tvRSstation)).getText());
        intentShedule.putExtra("busstation_id", String.valueOf(id));
        startActivity(intentShedule);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (getActivity().getClass().getSimpleName()){
            case "FoundStationsActivity":
                return new FoundStationsCursorLoader(getActivity(), databaseAccess, args);
            default:
                return new FavoriteStationsCursorLoader(getActivity(), databaseAccess);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        scRouteStationAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

}
