package com.example.valdizz.busstation.Dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.widget.TextView;

import com.example.valdizz.busstation.R;
import com.example.valdizz.busstation.ReminderSettingsActivity;
import com.example.valdizz.busstation.SheduleActivity;

public class TimeToDepartureDialog extends AppCompatDialogFragment {
    private String message;

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message)
                .setCancelable(true)
                .setNeutralButton(R.string.dialog_set_reminder, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intentRemider = new Intent(getActivity(), ReminderSettingsActivity.class);
                        intentRemider.putExtra("route_num", ((TextView) getActivity().findViewById(R.id.tvRouteNumShedule)).getText());
                        intentRemider.putExtra("route_name", ((TextView) getActivity().findViewById(R.id.tvRouteNameShedule)).getText());
                        intentRemider.putExtra("route_color", (getActivity().findViewById(R.id.tvRouteNumShedule)).getTag().toString());
                        intentRemider.putExtra("station_name", ((TextView)getActivity().findViewById(R.id.tvStationNameShedule)).getText());
                        intentRemider.putExtra("busstation_id",((SheduleActivity)getActivity()).getBusstation_id());
                        startActivity(intentRemider);
                    }
                })
                .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                   }
               });

        return builder.create();
    }
}
