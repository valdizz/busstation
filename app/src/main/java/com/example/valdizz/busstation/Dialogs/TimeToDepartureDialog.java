package com.example.valdizz.busstation.Dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;

import com.example.valdizz.busstation.Model.Reminder;
import com.example.valdizz.busstation.Model.Shedule;
import com.example.valdizz.busstation.R;
import com.example.valdizz.busstation.ReminderSettingsActivity;

public class TimeToDepartureDialog extends AppCompatDialogFragment {
    private String title;
    private Shedule shedule;
    private Reminder reminder;

    public void setTitle(String title) {
        this.title = title;
    }

    public void setShedule(Shedule shedule) {
        this.shedule = shedule;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        reminder = new Reminder(shedule.getStation(), "", "", "", "");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setCancelable(true)
                .setNeutralButton(R.string.dialog_set_reminder, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Bundle bundle = new Bundle();
                        bundle.putParcelable(Reminder.class.getCanonicalName(), reminder);
                        Intent intentReminderSettings = new Intent(getActivity(), ReminderSettingsActivity.class);
                        intentReminderSettings.putExtra(Reminder.class.getCanonicalName(), bundle);
                        startActivity(intentReminderSettings);
                    }
                })
                .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                   }
               });
        if (shedule.getDescription()!=null && shedule.getDescription().length()>0){
            builder.setMessage(shedule.getDescription());
        }

        return builder.create();
    }
}
