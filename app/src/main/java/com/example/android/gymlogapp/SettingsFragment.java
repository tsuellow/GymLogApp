package com.example.android.gymlogapp;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
//import android.support.v7.preference.PreferenceFragmentCompat;
import com.example.android.gymlogapp.data.GymDatabase;
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;

import android.support.v7.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    EditTextPreference gymName, gymOwner;
    Preference changePin, changeOwnerPin, backupAll, restoreAll, backupTime, exchangeRate, singlePassMinus1, singlePassMinus2;
    ListPreference preferredCurrency;
    TimePickerDialog.OnTimeSetListener onTimeSetListenerBackup;
    GymDatabase mDb;

    SharedPreferences sharedPreferences;


    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.app_settings,rootKey);

        sharedPreferences=PreferenceManager.getDefaultSharedPreferences(getContext());
        mDb=GymDatabase.getInstance(getContext());

        gymName = (EditTextPreference) findPreference("gymname");
        gymOwner = (EditTextPreference) findPreference("gymowner");


        //alternative solution
        backupTime =(Preference) findPreference("timebackup");

        backupTime.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                displayTimePickerDialog(getString(R.string.backup_time));
                return false;
            }
        });

        exchangeRate = (Preference) findPreference("usd2cs");
        exchangeRate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                displayValueDialog("usd2cs",exchangeRate.getTitle().toString(),getString(R.string.value_in_cs));
                return false;
            }
        });
        singlePassMinus1 = (Preference) findPreference("passminus1");
        singlePassMinus1.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                displayValueDialog("passminus1",singlePassMinus1.getTitle().toString(),getString(R.string.value_in_cs));
                return false;
            }
        });
        singlePassMinus2 = (Preference) findPreference("passminus2");
        singlePassMinus2.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                displayValueDialog("passminus2",singlePassMinus2.getTitle().toString(),getString(R.string.value_in_cs));
                return false;
            }
        });
        backupAll=(Preference) findPreference("backupall");
        backupAll.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                displayConfirmDialog("backupall",backupAll.getTitle().toString());
                return false;
            }
        });
        restoreAll=(Preference) findPreference("recoverall");
        restoreAll.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                displayConfirmDialog("recoverall",restoreAll.getTitle().toString());
                return false;
            }
        });

        changePin=(Preference) findPreference("changepin");
        changePin.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                displayPinDialog("changepin",changePin.getTitle().toString());
                return false;
            }
        });

        changeOwnerPin=(Preference) findPreference("changeownerpin");
        changeOwnerPin.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                displayPinDialog("changeownerpin",changeOwnerPin.getTitle().toString());
                return false;
            }
        });

        preferredCurrency = (ListPreference) findPreference("preferredcurrency");



        setPrefSummary();
    }

    private void displayPinDialog(final String key, String title){
        AlertDialog.Builder mBuilder=new AlertDialog.Builder(getContext());
        View mView=getLayoutInflater().inflate(R.layout.dialog_change_pin,null);
        final EditText mOldPin=(EditText) mView.findViewById(R.id.ev_old_pin);
        TextInputLayout loOldPin=(TextInputLayout) mView.findViewById(R.id.lo_old_pin);
        final EditText mNewPin1=(EditText) mView.findViewById(R.id.ev_new_pin1);
        TextInputLayout loNewPin1=(TextInputLayout) mView.findViewById(R.id.lo_new_pin1);
        final EditText mNewPin2=(EditText) mView.findViewById(R.id.ev_new_pin2);
        TextInputLayout loNewPin2=(TextInputLayout) mView.findViewById(R.id.lo_new_pin2);
        Button mOk=(Button) mView.findViewById(R.id.btn_ok_pin);
        Button mCancel=(Button) mView.findViewById(R.id.btn_cancel_pin);
        mBuilder.setTitle(title);
        mBuilder.setView(mView);

        final AlertDialog dialog=mBuilder.create();

        //button to dismiss dialog
        mOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String oldPin=mOldPin.getText().toString();
                String newPin1=mNewPin1.getText().toString();
                String newPin2=mNewPin2.getText().toString();
                String currPin=sharedPreferences.getString(key,"1234");
                String currOwnerPin=sharedPreferences.getString("changeownerpin","1234");
                if((currPin.contentEquals(oldPin)||currOwnerPin.contentEquals(oldPin)) && newPin1.contentEquals(newPin2)){
                    if (newPin1.length()==4){
                        SharedPreferences sharedPreferences=PreferenceManager.getDefaultSharedPreferences(getContext());
                        SharedPreferences.Editor editor=sharedPreferences.edit();
                        editor.putString(key,newPin1);
                        editor.apply();
                        dialog.dismiss();
                        Toast.makeText(getContext(), R.string.pin_change_success,Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getContext(), R.string.error_pin_too_long,Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getContext(),getString(R.string.error_pin_wrong),Toast.LENGTH_SHORT).show();
                }
            }
        });

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    private void displayValueDialog(final String key, String title, String inputText){
        AlertDialog.Builder mBuilder=new AlertDialog.Builder(getContext());
        View mView=getLayoutInflater().inflate(R.layout.dialog_change_owner_setting,null);
        final EditText mOwnerPin=(EditText) mView.findViewById(R.id.owner_pin);
        TextInputLayout loOwnerPin=(TextInputLayout) mView.findViewById(R.id.lo_owner_pin);
        final EditText mSettingValue=(EditText) mView.findViewById(R.id.setting_value);

        TextInputLayout loSettingValue=(TextInputLayout) mView.findViewById(R.id.lo_setting_value);
        loSettingValue.setHint(inputText);

        Button mOk=(Button) mView.findViewById(R.id.btn_ok_pin);
        Button mCancel=(Button) mView.findViewById(R.id.btn_cancel_pin);
        mBuilder.setTitle(title);
        mBuilder.setView(mView);

        final AlertDialog dialog=mBuilder.create();

        //button to dismiss dialog
        mOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ownerPin=mOwnerPin.getText().toString();
                String settingValue=mSettingValue.getText().toString();
                String currPin=sharedPreferences.getString("changeownerpin","1234");
                if(currPin.contentEquals(ownerPin)){
                    try{
                        Float.valueOf(settingValue);
                        SharedPreferences.Editor editor=sharedPreferences.edit();
                        editor.putString(key,settingValue);
                        editor.apply();
                        dialog.dismiss();
                        Toast.makeText(getContext(), R.string.value_success,Toast.LENGTH_SHORT).show();
                    }catch (Exception e){
                        Toast.makeText(getContext(), R.string.value_has_to_be_numeric,Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getContext(), R.string.wrong_owner_pin,Toast.LENGTH_SHORT).show();
                }
            }
        });

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    private void displayConfirmDialog(final String key, String title){
        AlertDialog.Builder mBuilder=new AlertDialog.Builder(getContext());
        View mView=getLayoutInflater().inflate(R.layout.dialog_confirm_owner_setting,null);
        final EditText mOwnerPin=(EditText) mView.findViewById(R.id.owner_pin);
        TextInputLayout loOwnerPin=(TextInputLayout) mView.findViewById(R.id.lo_owner_pin);

        Button mOk=(Button) mView.findViewById(R.id.btn_confirm);
        Button mCancel=(Button) mView.findViewById(R.id.btn_cancel_pin);
        mBuilder.setTitle(title);
        mBuilder.setView(mView);

        final AlertDialog dialog=mBuilder.create();

        //button to dismiss dialog
        mOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ownerPin=mOwnerPin.getText().toString();
                String currPin=sharedPreferences.getString("changeownerpin","1234");
                if(currPin.contentEquals(ownerPin)){
                    if (key.contentEquals("backupall")){
                        AppExecutors.getInstance().diskIO().execute(new Runnable() {
                            @Override
                            public void run() {
                                    mDb.clientDao().backupResetClientSyncStatus();
                                    mDb.paymentDao().backupResetPaymentSyncStatus();
                                    mDb.visitDao().backupResetVisitSyncStatus();
                            }
                        });
                        Toast.makeText(getContext(), R.string.toast_backup_all,Toast.LENGTH_SHORT).show();
                    }else if (key.contentEquals("recoverall")){
                        DataBackup dataBackup=new DataBackup(getContext(),sharedPreferences);
                        dataBackup.restoreAll();
                    }
                    dialog.dismiss();

                }else{
                    Toast.makeText(getContext(), R.string.wrong_owner_pin,Toast.LENGTH_SHORT).show();
                }
            }
        });

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void setBackupTime(long timeInMillis) {

        try {
            //getting the alarm manager
            AlarmManager am = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);

            //creating a new intent specifying the broadcast receiver
            Intent i = new Intent(getContext(), BackupBroadcastReceiver.class);

            //creating a pending intent using the intent
            PendingIntent pi = PendingIntent.getBroadcast(getContext(), 0, i, PendingIntent.FLAG_CANCEL_CURRENT);

            //setting the repeating alarm that will be fired every day
            am.cancel(pi);
            am.setRepeating(AlarmManager.RTC_WAKEUP, timeInMillis, AlarmManager.INTERVAL_HOUR, pi);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Toast.makeText(getContext(), R.string.backup_time_toast, Toast.LENGTH_SHORT).show();
    }


    private void displayTimePickerDialog(String title){
        AlertDialog.Builder mBuilder=new AlertDialog.Builder(getContext());
        View mView=getLayoutInflater().inflate(R.layout.dialog_time_picker,null);
        final TimePicker mTimePicker=(TimePicker) mView.findViewById(R.id.tp_time_picker);
        mTimePicker.setIs24HourView(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mTimePicker.setHour(12);
            mTimePicker.setMinute(0);
        } else {
            mTimePicker.setCurrentHour(12);
            mTimePicker.setCurrentMinute(0);
        }

        Button mOk=(Button) mView.findViewById(R.id.btn_ok_tp);
        Button mCancel=(Button) mView.findViewById(R.id.btn_cancel_tp);
        mBuilder.setTitle(title);
        mBuilder.setView(mView);

        final AlertDialog dialog=mBuilder.create();

        //button to dismiss dialog
        mOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int newHour, newMinute;
                if (Build.VERSION.SDK_INT >= 23 ){
                    newHour = mTimePicker.getHour();
                    newMinute = mTimePicker.getMinute();
                }
                else{
                    newHour = mTimePicker.getCurrentHour();
                    newMinute = mTimePicker.getCurrentMinute();
                }
                String timePicked = "";
                String sHour = "00";
                if(newHour < 10){
                    sHour = "0"+newHour;
                } else {
                    sHour = String.valueOf(newHour);
                }
                String sMinute = "00";
                if(newMinute < 10){
                    sMinute = "0"+newMinute;
                } else {
                    sMinute = String.valueOf(newMinute);
                }
                timePicked=""+sHour+":"+sMinute;
                String textTimePicked=getString(R.string.each_full_hour)+" "+sMinute+" "+getString(R.string.minutes);
                SharedPreferences.Editor editor=sharedPreferences.edit();
                editor.putString("timebackup",timePicked);
                editor.apply();
                backupTime.setSummary(textTimePicked);
                //now trigger backup
                Calendar cal=Calendar.getInstance();
                int year=cal.get(Calendar.YEAR);
                int month=cal.get(Calendar.MONTH);
                int day=cal.get(Calendar.DAY_OF_MONTH);
                cal.set(year,month,day,newHour,newMinute,0);
                long timeInMillis=cal.getTimeInMillis();
                setBackupTime(timeInMillis);
                dialog.dismiss();
            }
        });

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    private void setPrefSummary(){
        gymName.setSummary(gymName.getText());
        gymOwner.setSummary(gymOwner.getText());
        exchangeRate.setSummary("C$ "+sharedPreferences.getString("usd2cs","1"));
        singlePassMinus1.setSummary("C$ "+sharedPreferences.getString("passminus1","0"));
        singlePassMinus2.setSummary("C$ "+sharedPreferences.getString("passminus2","0"));
        backupTime.setSummary(getString(R.string.each_full_hour)+" "+sharedPreferences.getString("timebackup","00:00").substring(3)+" "+getString(R.string.minutes));
        //languagePref.setSummary(languagePref.getEntry());
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        setPrefSummary();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
