package com.example.valdizz.busstation;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.valdizz.busstation.Database.DatabaseAccess;
import com.example.valdizz.busstation.Model.Reminder;
import com.example.valdizz.busstation.Model.Shedule;
import com.example.valdizz.busstation.Receivers.ReminderReceiver;

import java.util.Calendar;

public class ReminderSettingsActivity extends AppCompatActivity {

    DatabaseAccess databaseAccess;
    EditText etReminderNote;
    TextView tvReminderDateTime, tvRouteNumReminder, tvRouteNameReminder, tvStationNameReminder, etReminderDate, etReminderTime;
    CheckBox chkMonday, chkTuesday, chkWednesday, chkThursday, chkFriday, chkSaturday, chkSunday;
    AlarmManager am;
    Reminder reminder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_settings);

        etReminderTime = (TextView) findViewById(R.id.etReminderTime);
        etReminderDate = (TextView) findViewById(R.id.etReminderDate);
        etReminderNote = (EditText) findViewById(R.id.etReminderNote);
        tvReminderDateTime = (TextView) findViewById(R.id.tvReminderDateTime);
        tvRouteNumReminder = (TextView) findViewById(R.id.tvRouteNumReminder);
        tvRouteNameReminder = (TextView) findViewById(R.id.tvRouteNameReminder);
        tvStationNameReminder = (TextView) findViewById(R.id.tvStationNameReminder);
        chkMonday = (CheckBox) findViewById(R.id.chkMonday);
        chkTuesday = (CheckBox) findViewById(R.id.chkTuesday);
        chkWednesday = (CheckBox) findViewById(R.id.chkWednesday);
        chkThursday = (CheckBox) findViewById(R.id.chkThursday);
        chkFriday = (CheckBox) findViewById(R.id.chkFriday);
        chkSaturday = (CheckBox) findViewById(R.id.chkSaturday);
        chkSunday = (CheckBox) findViewById(R.id.chkSunday);

        init();
        updateReminderDateTime();
    }

    private void init(){
        reminder = getIntent().getBundleExtra(Reminder.class.getCanonicalName()).getParcelable(Reminder.class.getCanonicalName());

        tvRouteNumReminder.setText(reminder.getStation().getRoute().getNumber());
        ((GradientDrawable)tvRouteNumReminder.getBackground().getCurrent()).setColor(Color.parseColor("#" + reminder.getStation().getRoute().getColor()));
        tvRouteNameReminder.setText(reminder.getStation().getRoute().getName());
        tvStationNameReminder.setText(reminder.getStation().getName());
        etReminderDate.setText(reminder.getDate());
        etReminderTime.setText(reminder.getTime());
        etReminderNote.setText(reminder.getNote());
        setPeriodicity(reminder.getPeriodicity());

        databaseAccess = DatabaseAccess.getInstance(this);
    }

    private void setPeriodicity(String periodicity){
        if (periodicity!=null && periodicity.length() > 0) {
            for (char ch : periodicity.toCharArray()){
                switch (Character.getNumericValue(ch)){
                    case (Calendar.MONDAY):{
                        chkMonday.setChecked(true);
                        break;
                    }
                    case (Calendar.TUESDAY):{
                        chkTuesday.setChecked(true);
                        break;
                    }
                    case (Calendar.WEDNESDAY):{
                        chkWednesday.setChecked(true);
                        break;
                    }
                    case (Calendar.THURSDAY):{
                        chkThursday.setChecked(true);
                        break;
                    }
                    case (Calendar.FRIDAY):{
                        chkFriday.setChecked(true);
                        break;
                    }
                    case (Calendar.SATURDAY):{
                        chkSaturday.setChecked(true);
                        break;
                    }
                    case (Calendar.SUNDAY):{
                        chkSunday.setChecked(true);
                        break;
                    }
                }
            }
        }
    }

    public void onReminderRimeClick(View view){
        Calendar currentTime = reminder.getCalendar(reminder.getDate(), reminder.getTime());
        int hour = currentTime.get(Calendar.HOUR);
        int minute = currentTime.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                etReminderTime.setText(String.format("%02d:%02d", hourOfDay, minute));
                updateReminderDateTime();
            }
        }, hour, minute, true);
        timePickerDialog.setTitle(getString(R.string.reminder_spectime));
        timePickerDialog.show();
    }

    public void onReminderDateClick(View view){
        Calendar currentDate = reminder.getCalendar(reminder.getDate(), reminder.getTime());
        int year = currentDate.get(Calendar.YEAR);
        int month = currentDate.get(Calendar.MONTH);
        int day = currentDate.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                etReminderDate.setText(String.format("%02d.%02d.%4d", dayOfMonth, month, year));
                updateReminderDateTime();
            }
        }, year, month, day);
        datePickerDialog.setTitle(getString(R.string.reminder_specdate));
        datePickerDialog.show();
    }

    private void updateReminderDateTime(){
        String reminderTime = isEmpty(etReminderTime) ? "["+getString(R.string.reminder_spectime)+"]" : etReminderTime.getText().toString();
        StringBuilder reminderDateBuilder = new StringBuilder();
        if (chkMonday.isChecked())
            reminderDateBuilder.append(getString(R.string.reminder_monday));
        if (chkTuesday.isChecked())
            reminderDateBuilder.append(reminderDateBuilder.length()!=0 ? ", ": "").append(getString(R.string.reminder_tuesday));
        if (chkWednesday.isChecked())
            reminderDateBuilder.append(reminderDateBuilder.length()!=0 ? ", ": "").append(getString(R.string.reminder_wednesday));
        if (chkThursday.isChecked())
            reminderDateBuilder.append(reminderDateBuilder.length()!=0 ? ", ": "").append(getString(R.string.reminder_thursday));
        if (chkFriday.isChecked())
            reminderDateBuilder.append(reminderDateBuilder.length()!=0 ? ", ": "").append(getString(R.string.reminder_friday));
        if (chkSaturday.isChecked())
            reminderDateBuilder.append(reminderDateBuilder.length()!=0 ? ", ": "").append(getString(R.string.reminder_saturday));
        if (chkSunday.isChecked())
            reminderDateBuilder.append(reminderDateBuilder.length()!=0 ? ", ": "").append(getString(R.string.reminder_sunday));
        String reminderDate = reminderDateBuilder.length()!=0 ? (chkMonday.isChecked()&&chkTuesday.isChecked()&&chkWednesday.isChecked()&&chkThursday.isChecked()&&chkFriday.isChecked()&&chkSaturday.isChecked()&&chkSunday.isChecked() ? getString(R.string.reminder_daily) : reminderDateBuilder.toString()) : (isEmpty(etReminderDate) ? "["+getString(R.string.reminder_specdate)+"]" : etReminderDate.getText().toString());
        tvReminderDateTime.setText(getString(R.string.reminder_datetime, reminderDate , reminderTime));
    }

    private boolean isEmpty(TextView textView) {
        return textView.getText().toString().trim().length() == 0;
    }

    public void onClickCheckBox(View view){
        updateReminderDateTime();
    }

    public void onClickCancel(View view){
        finish();
    }

    public void onClickOk(View view){
        reminder.setDate(etReminderDate.getText().toString());
        reminder.setTime(etReminderTime.getText().toString());
        reminder.setNote(etReminderNote.getText().toString());
        reminder.setPeriodicity(getReminderPeriodicity());

        if (reminder.getCalendar(reminder.getDate(), reminder.getTime()).getTimeInMillis() < Calendar.getInstance().getTimeInMillis() && reminder.getPeriodicity().isEmpty()){
            Toast.makeText(ReminderSettingsActivity.this, getString(R.string.reminder_error), Toast.LENGTH_SHORT).show();
        }
        else {
            addReminderToDB(String.valueOf(reminder.getStation().getId()), reminder.getDate(), reminder.getTime(), reminder.getPeriodicity(), reminder.getNote());
            setAlarm(reminder.getCalendar(reminder.getDate(), reminder.getTime()), reminder.getPeriodicity());
            finish();
        }
    }

    private void addReminderToDB(final String busstations_id, final String date, final String time, final String periodicity, final String note){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                databaseAccess.open();
                databaseAccess.addReminder(busstations_id, date, time, periodicity, note);
                databaseAccess.close();
            }
        });
    }

    private void setAlarm(Calendar reminderTime, String reminderPeriodicity){
        am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Bundle bundle = new Bundle();
        bundle.putParcelable(Reminder.class.getCanonicalName(), reminder);
        Intent intentReminderReceiver = new Intent(ReminderSettingsActivity.this, ReminderReceiver.class);
        intentReminderReceiver.putExtra(Reminder.class.getCanonicalName(), bundle);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intentReminderReceiver, PendingIntent.FLAG_CANCEL_CURRENT);
        if (reminderPeriodicity.length()==0){
            intentReminderReceiver.setAction(String.valueOf(reminderTime.getTimeInMillis()));
            am.set(AlarmManager.RTC_WAKEUP, reminderTime.getTimeInMillis(), pendingIntent);
        }
        else {
            for (char ch : reminderPeriodicity.toCharArray()){
                reminderTime.set(Calendar.DAY_OF_WEEK, Character.getNumericValue(ch));
                intentReminderReceiver.setAction(String.valueOf(reminderTime.getTimeInMillis()));
                am.setRepeating(AlarmManager.RTC_WAKEUP, reminderTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pendingIntent);
            }
        }
    }

    private String getReminderPeriodicity(){
        StringBuilder reminderPeriodicity = new StringBuilder();
        if (chkMonday.isChecked())
            reminderPeriodicity.append(Calendar.MONDAY);
        if (chkTuesday.isChecked())
            reminderPeriodicity.append(Calendar.TUESDAY);
        if (chkWednesday.isChecked())
            reminderPeriodicity.append(Calendar.WEDNESDAY);
        if (chkThursday.isChecked())
            reminderPeriodicity.append(Calendar.THURSDAY);
        if (chkFriday.isChecked())
            reminderPeriodicity.append(Calendar.FRIDAY);
        if (chkSaturday.isChecked())
            reminderPeriodicity.append(Calendar.SATURDAY);
        if (chkSunday.isChecked())
            reminderPeriodicity.append(Calendar.SUNDAY);
        return reminderPeriodicity.length()==0 ? "" :reminderPeriodicity.toString();
    }

}
