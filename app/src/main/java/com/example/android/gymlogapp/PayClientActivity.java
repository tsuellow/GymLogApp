package com.example.android.gymlogapp;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.example.android.gymlogapp.data.ClientEntry;
import com.example.android.gymlogapp.data.GymDatabase;
import com.example.android.gymlogapp.data.PaymentEntry;
import com.example.android.gymlogapp.utils.DateMethods;
import com.github.aakira.expandablelayout.ExpandableLinearLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class PayClientActivity extends AppCompatActivity {

    GymDatabase mDb;
    DatePickerDialog.OnDateSetListener onDateSetListenerFrom, onDateSetListenerTo;
    TextView mName, mDateLastPaid, mAltAmount, mExchangeRate;
    EditText mFrom, mTo, mAmount, mToTime, mFromTime, mComment;
    AutoCompleteTextView mProduct, mCurrency;
    TextInputLayout loFrom, loTo, loProduct, loAmount, loFromTime, loToTime;
    Button mSubmit;
    ExpandableLinearLayout mExpandable;
    LinearLayout mAdvancedOptions;
    ImageView mArrow;
    String extra;

    Date dateFrom, dateTo, dateLastPaidUntil;
    int clientId, hourFrom, minuteFrom, hourTo, minuteTo;

    SharedPreferences sharedPreferences;

    Toolbar toolbar;

    int oldEditTextColor;

    //datasources
    PaymentEntry mPaymentEntry;
    ClientEntry mClientEntry;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_client);
        toolbar=(Toolbar) findViewById(R.id.toolbar_pay);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getBackToProfile();
            }
        });

        sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);

        extra="";

        loFrom=(TextInputLayout) findViewById(R.id.lo_from);
        loTo=(TextInputLayout) findViewById(R.id.lo_to);
        loProduct=(TextInputLayout) findViewById(R.id.lo_product);
        loAmount=(TextInputLayout) findViewById(R.id.lo_price);
        loFromTime=(TextInputLayout) findViewById(R.id.lo_from_time);
        loToTime=(TextInputLayout) findViewById(R.id.lo_to_time);

        mFrom=(EditText) findViewById(R.id.ev_from);
        mTo=(EditText) findViewById(R.id.ev_to);
        mFromTime=(EditText) findViewById(R.id.ev_from_time);
        mToTime=(EditText) findViewById(R.id.ev_to_time);
        hourFrom=0;
        minuteFrom=0;
        hourTo=23;
        minuteTo=59;
        mFromTime.setText(DateMethods.getTimeString(hourFrom,minuteFrom));
        mToTime.setText(DateMethods.getTimeString(hourTo,minuteTo));

        mComment=(EditText) findViewById(R.id.ev_comment);

        mProduct=(AutoCompleteTextView) findViewById(R.id.actv_product);
        final ArrayAdapter<String> prodAdapter=new ArrayAdapter<String>(PayClientActivity.this,
                android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.product_array));
        mProduct.setAdapter(prodAdapter);
        mProduct.setKeyListener(null);
        mProduct.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                ((AutoCompleteTextView) v).showDropDown();
                return false;
            }
        });
        oldEditTextColor=mTo.getCurrentTextColor();
        mProduct.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getApplicationContext(),adapterView.getItemAtPosition(i).toString(),Toast.LENGTH_LONG).show();
                Calendar cal=Calendar.getInstance();
                cal.setTime(dateFrom);
                String selected=adapterView.getItemAtPosition(i).toString();
                if(selected.contentEquals(getString(R.string.week))){
                    cal.add(Calendar.DAY_OF_MONTH,6);
                    mTo.setText(getDateString(cal));
                }else if(selected.contentEquals(getString(R.string.fortnight))){
                    cal.add(Calendar.DAY_OF_MONTH,14);
                    mTo.setText(getDateString(cal));
                }else if(selected.contentEquals(getString(R.string.month))){
                    cal.add(Calendar.MONTH,1);
                    cal.add(Calendar.DAY_OF_MONTH,-1);
                    mTo.setText(getDateString(cal));
                }else if(selected.contentEquals(getString(R.string.other))){
                    mTo.setText(getDateString(cal));
                }
                dateTo=cal.getTime();

                onSelectProductAnimator(mTo);
                onSelectProductAnimator(mFrom);
            }
        });

        mCurrency=(AutoCompleteTextView) findViewById(R.id.actv_currency);
        mAltAmount=(TextView) findViewById(R.id.tv_equivalent_to);

        if (sharedPreferences.getString("preferredcurrency","usd_key").contentEquals("usd_key")){
            mCurrency.setText("USD");
            mAltAmount.setText(getString(R.string.equivalent_to)+" C$ --");
        }else{
            mCurrency.setText("C$");
            mAltAmount.setText(getString(R.string.equivalent_to)+" USD --");
        }

        ArrayAdapter<String> currAdapter=new ArrayAdapter<String>(PayClientActivity.this,
                android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.currency_array));
        mCurrency.setAdapter(currAdapter);
        mCurrency.setKeyListener(null);
        mCurrency.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                ((AutoCompleteTextView) v).showDropDown();
                return false;
            }
        });


        mExchangeRate=(TextView) findViewById(R.id.tv_exchange_rate);
        mExchangeRate.setText(getString(R.string.exchange_rate_text)+" "+sharedPreferences.getString("usd2cs","32.50")+" C$/USD");
        mAmount=(EditText) findViewById(R.id.ev_price);


        TextWatcher textWatcher=new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String text;
                String curr;
                try {
                    float amount=Float.parseFloat(mAmount.getText().toString().trim());
                    float exchageRate=Float.parseFloat(sharedPreferences.getString("usd2cs","32.5"));
                    float amountAlt;
                    if (mCurrency.getText().toString().contentEquals("USD")){
                        amountAlt=amount*exchageRate;
                        amountAlt=Math.round(amountAlt);
                    }else{
                        amountAlt=amount/exchageRate;
                        amountAlt=Math.round(amountAlt*100.0f)/100.0f;
                    }
                    text=String.valueOf(amountAlt);
                }catch (Exception e){
                    text="--";
                }
                if (mCurrency.getText().toString().contentEquals("USD")){
                    curr="C$";
                }else{
                    curr="USD";
                }
                mAltAmount.setText(getString(R.string.equivalent_to)+" "+curr+" "+text);

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
        mAmount.addTextChangedListener(textWatcher);
        mCurrency.addTextChangedListener(textWatcher);


        mSubmit=(Button) findViewById(R.id.bt_submit_pay);

        mName=(TextView) findViewById(R.id.tv_client_name_pay);
        mDateLastPaid=(TextView) findViewById(R.id.tv_last_paid_pay);

        //get client id that started the intent
        Intent i=getIntent();
        clientId=i.getExtras().getInt("CLIENT_ID");
        mDb=GymDatabase.getInstance(getApplicationContext());
        retrieveData(clientId);


        mFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayDatePickerDialog(dateFrom,"from",getString(R.string.from));
            }
        });


        mTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayDatePickerDialog(dateTo,"to",getString(R.string.to));
            }
        });

        //Time pickers
        mFromTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayTimePickerDialog(hourFrom,minuteFrom,getString(R.string.restriction_from),"from");
            }
        });


        mToTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayTimePickerDialog(hourTo,minuteTo,getString(R.string.restriction_to),"to");
            }
        });

        mArrow=(ImageView) findViewById(R.id.iv_expandable_arrow);
        mExpandable=(ExpandableLinearLayout) findViewById(R.id.ell_advanced);
        mAdvancedOptions=(LinearLayout) findViewById(R.id.ll_advanced_options);
        mAdvancedOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mExpandable.isExpanded()){
                    mArrow.setImageResource(R.drawable.sort_down);
                    mExpandable.toggle();
                }else{
                    mArrow.setImageResource(R.drawable.sort_up);
                    mExpandable.toggle();
                }

            }
        });

        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSubmit();


            }
        });
    }

    //object select animator
    private void onSelectProductAnimator(EditText editText){
        ObjectAnimator colorAnim = ObjectAnimator.ofInt(editText, "textColor",
                getResources().getColor(R.color.colorAccent), oldEditTextColor);
        colorAnim.setDuration(1000);
        colorAnim.setEvaluator(new ArgbEvaluator());
        colorAnim.start();
    }

    private void getBackToProfile(){
        Intent i = new Intent(PayClientActivity.this,ClientProfileActivity.class);
        i.putExtra("CLIENT_ID",clientId);
        startActivity(i);
    }

    //to be evaluated jointly on submit
    private void onSubmit(){
        boolean productCorrect=checkProduct();
        boolean amountCorrect=checkAmount();


        if(productCorrect && amountCorrect){
            populateDb();

            getBackToProfile();
        }

    }

    //single conditions for evaluation
    private boolean checkProduct(){
        if (mProduct.getText().toString().trim().isEmpty()){
            loProduct.setErrorEnabled(true);
            loProduct.setError(getString(R.string.enter_product));
            mProduct.setError(getString(R.string.input_required));
            return false;
        }
        loProduct.setErrorEnabled(false);
        return true;
    }
    private boolean checkAmount(){
        if (mAmount.getText().toString().trim().isEmpty()){
            loAmount.setErrorEnabled(true);
            loAmount.setError(getString(R.string.enter_valid_amount));
            mAmount.setError(getString(R.string.input_required));
            return false;
        }else{
            try{
                Float.parseFloat(mAmount.getText().toString().trim());
                loAmount.setErrorEnabled(false);
                return true;
            }catch (Exception e){
                loAmount.setErrorEnabled(true);
                loAmount.setError(getString(R.string.enter_valid_amount));
                mAmount.setError(getString(R.string.input_has_to_be_numeric));
                return false;
            }
        }

    }



    //method to get payment and profile data related to the client
    private void retrieveData(int clientId){
        final LiveData<PaymentEntry> payments = mDb.paymentDao().getLastPaymentByClient(clientId);
        payments.observe(this, new Observer<PaymentEntry>() {
            @Override
            public void onChanged(@Nullable PaymentEntry paymentEntry) {
                payments.removeObserver(this);
                mPaymentEntry=paymentEntry;
                //now populate the vars
                Calendar cal = Calendar.getInstance();
                Date now=getDateWithoutTime();
                if (mPaymentEntry!=null) {
                    dateLastPaidUntil = mPaymentEntry.getPaidUntil();
                    cal.setTime(dateLastPaidUntil);
                    mDateLastPaid.setText(getDateString(cal));
                    //set edit texts
                    if (dateLastPaidUntil.before(now)){
                        cal.setTime(now);
                        mFrom.setText(getDateString(cal));
                        dateFrom=cal.getTime();
                        mTo.setText(getDateString(cal));
                        dateTo=cal.getTime();
                    }else{
                        cal.setTime(dateLastPaidUntil);
                        cal.add(Calendar.DATE,1);
                        mFrom.setText(getDateString(cal));
                        dateFrom=cal.getTime();
                        mTo.setText(getDateString(cal));
                        dateTo=cal.getTime();
                    }

                }else{
                    mDateLastPaid.setText(getString(R.string.never));
                    cal.setTime(now);
                    mFrom.setText(getDateString(cal));
                    dateFrom=cal.getTime();
                    mTo.setText(getDateString(cal));
                    dateTo=cal.getTime();
                }
            }
        });

        final LiveData<ClientEntry> client = mDb.clientDao().getClientById(clientId);
        client.observe(this, new Observer<ClientEntry>() {
            @Override
            public void onChanged(@Nullable ClientEntry clientEntry) {
                client.removeObserver(this);
                mClientEntry=clientEntry;
                if (mClientEntry!=null) {
                    String nameFull = mClientEntry.getFirstName() + " " + mClientEntry.getLastName();
                    mName.setText(nameFull);
                }

            }
        });
    }

    private void populateDb(){
        String product=mProduct.getText().toString();
        float amount=Float.parseFloat(mAmount.getText().toString().trim());
        String exchangeRateStr = sharedPreferences.getString("usd2cs", "1");
        float exchangeRate = Float.valueOf(exchangeRateStr);
        String currency=mCurrency.getText().toString();
        float amountUsd;
        if (currency.contentEquals("USD")){
            amountUsd=amount;
        }else{
            amountUsd=amount/exchangeRate;
        }

        Date timestamp=new Date();
        String comment=mComment.getText().toString();

        Calendar calFrom=Calendar.getInstance();
        calFrom.setTime(dateFrom);
        calFrom.set(Calendar.HOUR_OF_DAY, hourFrom);
        calFrom.set(Calendar.MINUTE, minuteFrom);
        dateFrom=calFrom.getTime();
        Calendar calTo=Calendar.getInstance();
        calTo.setTime(dateTo);
        calTo.set(Calendar.HOUR_OF_DAY, hourTo);
        calTo.set(Calendar.MINUTE, minuteTo);
        dateTo=calTo.getTime();


        final PaymentEntry paymentEntry=new PaymentEntry(clientId,product,amountUsd,dateFrom,dateTo,timestamp,exchangeRate,currency,comment,extra);
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDb.paymentDao().insertPayment(paymentEntry);
            }
        });
    }

    public static Date getDateWithoutTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    public String getDateString(Calendar cal){
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        month = 1 + month;
        return day + "/" + month + "/" + year;
    }

    private void displayTimePickerDialog(int hour, int minute, String title, final String objective){
        AlertDialog.Builder mBuilder=new AlertDialog.Builder(PayClientActivity.this);
        View mView=getLayoutInflater().inflate(R.layout.dialog_time_picker,null);
        final TimePicker mTimePicker=(TimePicker) mView.findViewById(R.id.tp_time_picker);
        mTimePicker.setIs24HourView(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mTimePicker.setHour(hour);
            mTimePicker.setMinute(minute);
        } else {
            mTimePicker.setCurrentHour(hour);
            mTimePicker.setCurrentMinute(minute);
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
               if (objective.contentEquals("from")){
                   mFromTime.setText(DateMethods.getTimeString(newHour,newMinute));
                   hourFrom=newHour;
                   minuteFrom=newMinute;
               } else if (objective.contentEquals("to")){
                   mToTime.setText(DateMethods.getTimeString(newHour,newMinute));
                   hourTo=newHour;
                   minuteTo=newMinute;
               }
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

    private void displayDatePickerDialog(Date date, final String objective, String title){
        AlertDialog.Builder mBuilder=new AlertDialog.Builder(PayClientActivity.this);
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
        mBuilder.setTitle(title);
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
                if (objective.contentEquals("from")){
                    mFrom.setText(getDateString(cal));
                    dateFrom=cal.getTime();
                } else if (objective.contentEquals("to")){
                    mTo.setText(getDateString(cal));
                    dateTo=cal.getTime();
                }
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
