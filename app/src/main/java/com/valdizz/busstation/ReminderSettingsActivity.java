package com.valdizz.busstation;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.valdizz.busstation.database.DatabaseAccess;
import com.valdizz.busstation.model.Reminder;

import java.util.Calendar;
import java.util.Locale;

public class ReminderSettingsActivity extends AppCompatActivity {

    private DatabaseAccess databaseAccess;
    private EditText etReminderNote;
    private TextView tvReminderDateTime;
    private TextView tvRouteNumReminder;
    private TextView tvRouteNameReminder;
    private TextView tvStationNameReminder;
    private TextView etReminderDate;
    private TextView etReminderTime;
    private CheckBox chkMonday;
    private CheckBox chkTuesday;
    private CheckBox chkWednesday;
    private CheckBox chkThursday;
    private CheckBox chkFriday;
    private CheckBox chkSaturday;
    private CheckBox chkSunday;
    private Reminder reminder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_settings);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        etReminderTime = findViewById(R.id.etReminderTime);
        etReminderDate = findViewById(R.id.etReminderDate);
        etReminderNote = findViewById(R.id.etReminderNote);
        tvReminderDateTime = findViewById(R.id.tvReminderDateTime);
        tvRouteNumReminder = findViewById(R.id.tvRouteNumReminder);
        tvRouteNameReminder = findViewById(R.id.tvRouteNameReminder);
        tvStationNameReminder = findViewById(R.id.tvStationNameReminder);
        chkMonday = findViewById(R.id.chkMonday);
        chkTuesday = findViewById(R.id.chkTuesday);
        chkWednesday = findViewById(R.id.chkWednesday);
        chkThursday = findViewById(R.id.chkThursday);
        chkFriday = findViewById(R.id.chkFriday);
        chkSaturday = findViewById(R.id.chkSaturday);
        chkSunday = findViewById(R.id.chkSunday);

        init();
        updateReminderDateTimeText();
    }

    private void init(){
        reminder = getIntent().getBundleExtra(Reminder.class.getCanonicalName()).getParcelable(Reminder.class.getCanonicalName());

        if (reminder != null) {
            tvRouteNumReminder.setText(reminder.getStation().getRoute().getNumber());
            ((GradientDrawable) tvRouteNumReminder.getBackground().getCurrent()).setColor(Color.parseColor("#" + reminder.getStation().getRoute().getColor()));
            tvRouteNameReminder.setText(reminder.getStation().getRoute().getName());
            tvStationNameReminder.setText(reminder.getStation().getName());
            etReminderDate.setText(reminder.getDate());
            etReminderTime.setText(reminder.getTime());
            etReminderNote.setText(reminder.getNote());
            setPeriodicity(reminder.getPeriodicity());
        }
        databaseAccess = DatabaseAccess.getInstance(this);
    }

    public void onReminderTimeClick(View view){
        Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);
        if (reminder.getTime() != null && reminder.getTime().length() == 5) {
            hour = Integer.valueOf(reminder.getTime().substring(0, 2));
            minute = Integer.valueOf(reminder.getTime().substring(3));
        }
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                etReminderTime.setText(String.format(Locale.getDefault(),"%02d:%02d", hourOfDay, minute));
                updateReminderDateTimeText();
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
        if (reminder.getDate() != null && reminder.getDate().length() == 10) {
            year = Integer.valueOf(reminder.getDate().substring(6));
            month = Integer.valueOf(reminder.getDate().substring(3, 5));
            day = Integer.valueOf(reminder.getDate().substring(0, 2));
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                etReminderDate.setText(String.format(Locale.getDefault(),"%02d.%02d.%4d", dayOfMonth, month, year));
                updateReminderDateTimeText();
            }
        }, year, month, day);
        datePickerDialog.setTitle(getString(R.string.reminder_specdate));
        datePickerDialog.show();
    }

    private void updateReminderDateTimeText(){
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
        updateReminderDateTimeText();
    }

    public void onClickCancel(View view){
        finish();
    }

    public void onClickOk(View view){
        if (etReminderDate.getText().length()!=10 || etReminderTime.getText().length()!=5){
            Toast.makeText(ReminderSettingsActivity.this, getString(R.string.reminder_setdatetimeerror), Toast.LENGTH_SHORT).show();
            return;
        }

        if (reminder.getDate()!=null && reminder.getDate().length()>0 && reminder.getTime()!=null && reminder.getTime().length()>0){
            reminder.remove(this);
            reminder.removeFromDB(databaseAccess, String.valueOf(reminder.getStation().getId()), reminder.getDate(), reminder.getTime(), reminder.getPeriodicity());
        }

        reminder.setDate(etReminderDate.getText().toString());
        reminder.setTime(etReminderTime.getText().toString());
        reminder.setNote(etReminderNote.getText().toString());
        reminder.setPeriodicity(getReminderPeriodicity());

        if (reminder.getNextReminderDatetime(reminder.getDate(), reminder.getTime(), -1).getTimeInMillis() < Calendar.getInstance().getTimeInMillis() && reminder.getPeriodicity().isEmpty()){
            Toast.makeText(ReminderSettingsActivity.this, getString(R.string.reminder_error), Toast.LENGTH_SHORT).show();
            return;
        }

        reminder.add(this);
        reminder.addToDB(databaseAccess);

        finish();
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

    @Override
    public  boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:{
                finish();
                return true;
            }
            case R.id.about_menu: {
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
