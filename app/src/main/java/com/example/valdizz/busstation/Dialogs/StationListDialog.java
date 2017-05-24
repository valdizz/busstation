package com.example.valdizz.busstation.Dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;

import com.example.valdizz.busstation.Database.DatabaseAccess;
import com.example.valdizz.busstation.Database.RoutesOnStationCursorLoader;
import com.example.valdizz.busstation.R;

public class StationListDialog extends AppCompatDialogFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private SimpleCursorAdapter scRoutesOnStationAdapter;
    private DatabaseAccess databaseAccess;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseAccess = DatabaseAccess.getInstance(getContext());
        databaseAccess.open();
        Bundle bundle = new Bundle();
        bundle.putStringArray("routeonstation_params", new String[]{getTag().toString()});
        getLoaderManager().initLoader(0, bundle, this);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        scRoutesOnStationAdapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1, null, new String[]{"route_number","route_name"}, new int[]{android.R.id.text1, android.R.id.text2},0);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.btn_routes)
                .setCancelable(true)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //TODO
                    }
                })
                .setAdapter(scRoutesOnStationAdapter, null);
    return builder.create();
    }

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
