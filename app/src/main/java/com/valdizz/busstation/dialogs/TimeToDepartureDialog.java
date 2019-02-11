package com.valdizz.busstation.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;

import com.valdizz.busstation.model.Reminder;
import com.valdizz.busstation.model.Schedule;
import com.valdizz.busstation.R;
import com.valdizz.busstation.ReminderSettingsActivity;

public class TimeToDepartureDialog extends AppCompatDialogFragment {
    private String title;
    private Schedule schedule;
    private Reminder reminder;

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        reminder = new Reminder(schedule.getStation(), "", "", "", "");
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
        if (schedule.getDescription()!=null && schedule.getDescription().length()>0){
            builder.setMessage(schedule.getDescription());
        }

        return builder.create();
    }
}
