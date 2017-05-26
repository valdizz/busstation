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
import com.example.valdizz.busstation.R;
import com.example.valdizz.busstation.SheduleActivity;

public class StationListDialog extends AppCompatDialogFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private SimpleCursorAdapter scRoutesOnStationAdapter;
    private DatabaseAccess databaseAccess;

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
            Intent intentShedule = new Intent(getActivity(), SheduleActivity.class);
            intentShedule.putExtra("route_num", scRoutesOnStationAdapter.getCursor().getString(scRoutesOnStationAdapter.getCursor().getColumnIndex(DatabaseAccess.ROUTE_NUMBER)));
            intentShedule.putExtra("route_name", scRoutesOnStationAdapter.getCursor().getString(scRoutesOnStationAdapter.getCursor().getColumnIndex(DatabaseAccess.ROUTE_NAME)));
            intentShedule.putExtra("route_color", scRoutesOnStationAdapter.getCursor().getString(scRoutesOnStationAdapter.getCursor().getColumnIndex(DatabaseAccess.ROUTE_COLOR)));
            intentShedule.putExtra("station_name", scRoutesOnStationAdapter.getCursor().getString(scRoutesOnStationAdapter.getCursor().getColumnIndex(DatabaseAccess.STATION_NAME)));
            intentShedule.putExtra("busstation_id", scRoutesOnStationAdapter.getCursor().getString(scRoutesOnStationAdapter.getCursor().getColumnIndex(DatabaseAccess.BUSSTATION_ID)));
            startActivity(intentShedule);
        }
    };

    SimpleCursorAdapter.ViewBinder scRoutesOnStationAdapterBinder = new SimpleCursorAdapter.ViewBinder() {
        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            String color = cursor.getString(cursor.getColumnIndex(DatabaseAccess.ROUTE_COLOR));
            String number = cursor.getString(cursor.getColumnIndex(DatabaseAccess.ROUTE_NUMBER));
            if (view.getId() == R.id.tvRouteNumDialog){
                view.setBackgroundColor(Color.parseColor("#" + color));
                ((TextView)view).setText(number);
                view.setTag(color);
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
