package com.example.valdizz.busstation.Dialogs;


import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.View;
import android.widget.TextView;

import com.example.valdizz.busstation.Database.DatabaseAccess;
import com.example.valdizz.busstation.Database.TransferRoutesCursorLoader;
import com.example.valdizz.busstation.Model.Route;
import com.example.valdizz.busstation.Model.Station;
import com.example.valdizz.busstation.R;
import com.example.valdizz.busstation.SheduleActivity;

public class TransferRoutesDialog extends AppCompatDialogFragment  implements LoaderManager.LoaderCallbacks<Cursor> {

    private SimpleCursorAdapter scTransferRoutesAdapter;
    private DatabaseAccess databaseAccess;
    private Station station;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseAccess = DatabaseAccess.getInstance(getContext());
        databaseAccess.open();
        Bundle bundle = new Bundle();
        bundle.putStringArray(DatabaseAccess.BUNDLE_PARAMS, new String[]{this.getArguments().getString(DatabaseAccess.STATION_NAME), this.getArguments().getString(DatabaseAccess.ROUTE_NUMBER)});
        getLoaderManager().initLoader(0, bundle, this);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        scTransferRoutesAdapter = new SimpleCursorAdapter(getActivity(), R.layout.route_item_dialog, null, new String[]{DatabaseAccess.ROUTE_NUMBER, DatabaseAccess.ROUTE_NAME}, new int[]{R.id.tvRouteNumDialog, R.id.tvRouteNameDialog},0);
        scTransferRoutesAdapter.setViewBinder(scTransferRoutesAdapterBinder);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.route_transfer))
                .setCancelable(true)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setAdapter(scTransferRoutesAdapter, onDialogClickListener);
        return builder.create();
    }

    DialogInterface.OnClickListener onDialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Route route = new Route(
                    scTransferRoutesAdapter.getCursor().getString(scTransferRoutesAdapter.getCursor().getColumnIndex(DatabaseAccess.ROUTE_NUMBER)),
                    scTransferRoutesAdapter.getCursor().getString(scTransferRoutesAdapter.getCursor().getColumnIndex(DatabaseAccess.ROUTE_NAME)),
                    scTransferRoutesAdapter.getCursor().getString(scTransferRoutesAdapter.getCursor().getColumnIndex(DatabaseAccess.ROUTE_COLOR)),
                    scTransferRoutesAdapter.getCursor().getShort(scTransferRoutesAdapter.getCursor().getColumnIndex(DatabaseAccess.ROUTE_DIRECTION))!=0);
            station = new Station(
                    scTransferRoutesAdapter.getCursor().getInt(scTransferRoutesAdapter.getCursor().getColumnIndex(DatabaseAccess.BUSSTATION_ID)),
                    route,
                    scTransferRoutesAdapter.getCursor().getString(scTransferRoutesAdapter.getCursor().getColumnIndex(DatabaseAccess.STATION_NAME)),
                    scTransferRoutesAdapter.getCursor().getShort(scTransferRoutesAdapter.getCursor().getColumnIndex(DatabaseAccess.BUSSTATION_FAVORITE))!=0,
                    scTransferRoutesAdapter.getCursor().getString(scTransferRoutesAdapter.getCursor().getColumnIndex(DatabaseAccess.BUSSTATION_GPS)));

            Bundle bundle = new Bundle();
            bundle.putParcelable(Station.class.getCanonicalName(), station);
            Intent intentShedule = new Intent(getActivity(), SheduleActivity.class);
            intentShedule.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intentShedule.putExtra(Station.class.getCanonicalName(), bundle);
            startActivity(intentShedule);
        }
    };

    SimpleCursorAdapter.ViewBinder scTransferRoutesAdapterBinder = new SimpleCursorAdapter.ViewBinder() {
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
        return new TransferRoutesCursorLoader(getActivity(), databaseAccess, args);
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        scTransferRoutesAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        scTransferRoutesAdapter.swapCursor(null);
    }
}
