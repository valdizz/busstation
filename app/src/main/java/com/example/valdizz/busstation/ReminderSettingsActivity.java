package com.example.valdizz.busstation;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

public class ReminderSettingsActivity extends AppCompatActivity {

    EditText etReminderNote;
    TextView tvReminderDateTime, tvRouteNumReminder, tvRouteNameReminder, tvStationNameReminder, etReminderDate, etReminderTime;
    CheckBox chkMonday, chkTuesday, chkWednesday, chkThursday, chkFriday, chkSaturday, chkSunday;
    String busstation_id;



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
        Intent intent = getIntent();
        tvRouteNumReminder.setText(intent.getStringExtra("route_num"));
        ((GradientDrawable)tvRouteNumReminder.getBackground().getCurrent()).setColor(Color.parseColor("#" + intent.getStringExtra("route_color")));
        tvRouteNameReminder.setText(intent.getStringExtra("route_name"));
        tvStationNameReminder.setText(intent.getStringExtra("station_name"));
        busstation_id = intent.getStringExtra("busstation_id");
    }

    public void onReminderRimeClick(View view){
        Calendar currentTime = Calendar.getInstance();
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
        Calendar currentDate = Calendar.getInstance();
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
        //TODO
    }

}
