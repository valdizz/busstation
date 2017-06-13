package com.example.valdizz.busstation.Dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.View;
import android.widget.TextView;

import com.example.valdizz.busstation.Database.DatabaseAccess;
import com.example.valdizz.busstation.Database.RoutesOnStationCursorLoader;
import com.example.valdizz.busstation.Model.Route;
import com.example.valdizz.busstation.Model.Station;
import com.example.valdizz.busstation.R;
import com.example.valdizz.busstation.SheduleActivity;

public class StationListDialog extends AppCompatDialogFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private SimpleCursorAdapter scRoutesOnStationAdapter;
    private DatabaseAccess databaseAccess;
    private Station station;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseAccess = DatabaseAccess.getInstance(getContext());
        databaseAccess.open();
        Bundle bundle = new Bundle();
        bundle.putStringArray(DatabaseAccess.BUNDLE_PARAMS, new String[]{getTag().toString()});
        getLoaderManager().initLoader(0, bundle, this);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        scRoutesOnStationAdapter = new SimpleCursorAdapter(getActivity(), R.layout.route_item_dialog, null, new String[]{DatabaseAccess.ROUTE_NUMBER, DatabaseAccess.ROUTE_NAME}, new int[]{R.id.tvRouteNumDialog, R.id.tvRouteNameDialog},0);
        scRoutesOnStationAdapter.setViewBinder(scRoutesOnStationAdapterBinder);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(this.getArguments().get(DatabaseAccess.STATION_NAME).toString())
                .setCancelable(true)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setAdapter(scRoutesOnStationAdapter, onDialogClickListener);
    return builder.create();
    }

    DialogInterface.OnClickListener onDialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Route route = new Route(
                    scRoutesOnStationAdapter.getCursor().getString(scRoutesOnStationAdapter.getCursor().getColumnIndex(DatabaseAccess.ROUTE_NUMBER)),
                    scRoutesOnStationAdapter.getCursor().getString(scRoutesOnStationAdapter.getCursor().getColumnIndex(DatabaseAccess.ROUTE_NAME)),
                    scRoutesOnStationAdapter.getCursor().getString(scRoutesOnStationAdapter.getCursor().getColumnIndex(DatabaseAccess.ROUTE_COLOR)),
                    scRoutesOnStationAdapter.getCursor().getShort(scRoutesOnStationAdapter.getCursor().getColumnIndex(DatabaseAccess.ROUTE_DIRECTION))!=0);
            station = new Station(
                    scRoutesOnStationAdapter.getCursor().getInt(scRoutesOnStationAdapter.getCursor().getColumnIndex(DatabaseAccess.BUSSTATION_ID)),
                    route,
                    scRoutesOnStationAdapter.getCursor().getString(scRoutesOnStationAdapter.getCursor().getColumnIndex(DatabaseAccess.STATION_NAME)),
                    scRoutesOnStationAdapter.getCursor().getShort(scRoutesOnStationAdapter.getCursor().getColumnIndex(DatabaseAccess.BUSSTATION_FAVORITE))!=0,
                    scRoutesOnStationAdapter.getCursor().getString(scRoutesOnStationAdapter.getCursor().getColumnIndex(DatabaseAccess.BUSSTATION_GPS)));

            Bundle bundle = new Bundle();
            bundle.putParcelable(Station.class.getCanonicalName(), station);
            Intent intentShedule = new Intent(getActivity(), SheduleActivity.class);
            intentShedule.putExtra(Station.class.getCanonicalName(), bundle);
            startActivity(intentShedule);
        }
    };

    SimpleCursorAdapter.ViewBinder scRoutesOnStationAdapterBinder = new SimpleCursorAdapter.ViewBinder() {
        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            if (view.getId() == R.id.tvRouteNumDialog){
                view.setBackgroundColor(Color.parseColor("#" + cursor.getString(cursor.getColumnIndex(DatabaseAccess.ROUTE_COLOR))));
                ((TextView)view).setText(cursor.getString(cursor.getColumnIndex(DatabaseAccess.ROUTE_NUMBER)));
                return true;
            }
            return false;
        }
    };

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new RoutesOnStationCursorLoader(getActivity(), databaseAccess, args);
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        scRoutesOnStationAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        scRoutesOnStationAdapter.swapCursor(null);
    }
}
