package com.example.android.gymlogapp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import com.example.android.gymlogapp.data.ClientEntry;
import com.example.android.gymlogapp.data.GymDatabase;
import com.example.android.gymlogapp.utils.DateMethods;
import com.example.android.gymlogapp.utils.PhoneUtilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SendReminderActivity extends AppCompatActivity implements SendReminderAdapter.ItemClickListener {

    RecyclerView rvReminder;
    GymDatabase mDb;
    SendReminderAdapter mAdapter;
    Context mContext;
    Toolbar mToolbar;
    DatePickerDialog.OnDateSetListener onDateSetListenerToday;
    Date dateSet;

    EditText mDateField, mMessageField;
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_reminder);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_send_reminder);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);

        mContext=getApplicationContext();
        rvReminder=(RecyclerView) findViewById(R.id.rv_send_reminder);
        mDb=GymDatabase.getInstance(mContext);

        mAdapter=new SendReminderAdapter(mContext,this);
        rvReminder.setAdapter(mAdapter);
        rvReminder.setLayoutManager(new LinearLayoutManager(this));

        mDateField=(EditText) findViewById(R.id.ev_date_rem);
        mMessageField=(EditText) findViewById(R.id.ev_message_rem);
        String messageText=getString(R.string.hi_xxxx)+" "+sharedPreferences.getString("gymname","Your Gym")+" "+getString(R.string.hi_xxxx_second_part);
        mMessageField.setText(messageText);
        dateSet= DateMethods.getRoundDate(new Date());
        Calendar cal =Calendar.getInstance();
        cal.setTime(dateSet);
        String dateString=DateMethods.getDateString(cal);
        mDateField.setText(dateString);

        populateDataSource(dateSet);


        mDateField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayDatePickerDialog(dateSet);
            }
        });

    }

    private void populateDataSource(Date date) {
        final LiveData<List<ClientEntry>> clients = mDb.clientDao().getPaymentDueClients(date);
        clients.observe(this, new Observer<List<ClientEntry>>() {
            @Override
            public void onChanged(@Nullable List<ClientEntry> clientEntries) {
                mAdapter.setClients(clientEntries);
                mToolbar.setSubtitle(mAdapter.getItemCount()+" "+getString(R.string.clients));

            }
        });
    }



    @Override
    public void onItemClickListener(String phone, String firstName) {
        String message=mMessageField.getText().toString();
        message=message.replace("XXXX",firstName);
        String toNumber= PhoneUtilities.depuratePhone(phone);
        toNumber=toNumber.replaceFirst("^0+(?!$)", "");
        openWhatsAppContact(toNumber,message);



    }

    public void openWhatsAppContact(String toNumber, String message){
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("http://api.whatsapp.com/send?phone="+toNumber +"&text="+message));
            startActivity(intent);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void displayDatePickerDialog(Date date){
        AlertDialog.Builder mBuilder=new AlertDialog.Builder(SendReminderActivity.this);
        View mView=getLayoutInflater().inflate(R.layout.dialog_date_picker,null);
        final DatePicker mDatePicker=(DatePicker) mView.findViewById(R.id.dp_date_picker);
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        mDatePicker.init(year,month,day,null);

        Button mOk=(Button) mView.findViewById(R.id.btn_ok_dp);
        Button mCancel=(Button) mView.findViewById(R.id.btn_cancel_dp);
        mBuilder.setTitle(R.string.date_of_expiration);
        mBuilder.setView(mView);

        final AlertDialog dialog=mBuilder.create();
        //button to dismiss dialog
        mOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal=Calendar.getInstance();
                int year, month, day;
                year=mDatePicker.getYear();
                month=mDatePicker.getMonth();
                day=mDatePicker.getDayOfMonth();
                cal.set(year,month,day);
                dateSet=cal.getTime();
                mDateField.setText(DateMethods.getDateString(cal));
                populateDataSource(dateSet);
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
}
