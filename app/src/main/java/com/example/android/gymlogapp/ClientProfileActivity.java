package com.example.android.gymlogapp;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.gymlogapp.data.ClientEntry;
import com.example.android.gymlogapp.data.GymDatabase;
import com.example.android.gymlogapp.data.PaymentEntry;
import com.example.android.gymlogapp.data.VisitEntry;
import com.example.android.gymlogapp.utils.DateMethods;
import com.example.android.gymlogapp.utils.PhoneUtilities;

import java.io.File;
import java.util.Date;
import java.util.List;

public class ClientProfileActivity extends AppCompatActivity {

    //activity objects
    RecyclerView rvVisits;
    CpVisitsAdapter mVisitsAdapter;
    RecyclerView rvPayments;
    CpPaymentsAdapter mPaymentsAdapter;
    GymDatabase mDb;
    Context mContext;
    Toolbar mToolbar;
    SharedPreferences sharedPreferences;

    //visible objects
    ImageView mProfilePhoto;
    ImageView mQrCode;
    TextView mFirstName;
    TextView mLastName;
    TextView mPersonalInfo;
    TextView mPhone;
    TextView mId;
    Button mModify;
    TextView mPaymentsCount;
    Button mPay;
    TextView mVisitCounts;
    int clientId;
    String phoneNr=null;
    String firstName="";
    //SearchAdapter mAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_profile);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_client_profile);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);

        mProfilePhoto=(ImageView) findViewById(R.id.iv_photo_profile);
        mQrCode=(ImageView) findViewById(R.id.iv_qr_icon);
        mFirstName=(TextView) findViewById(R.id.tv_first_name_profile);
        mLastName=(TextView) findViewById(R.id.tv_last_name_profile);
        mPersonalInfo=(TextView) findViewById(R.id.tv_info_profile);
        mPhone=(TextView) findViewById(R.id.tv_tel_profile);
        mId=(TextView) findViewById(R.id.tv_id_profile);
        mPaymentsCount=(TextView) findViewById(R.id.tv_payments_profile);
        mVisitCounts=(TextView) findViewById(R.id.tv_visits_profile);
        mModify=(Button) findViewById(R.id.bt_modify_profile);
        mPay=(Button) findViewById(R.id.bt_pay_profile);

        rvPayments=(RecyclerView) findViewById(R.id.rv_client_profile_payments);
        rvVisits=(RecyclerView) findViewById(R.id.rv_client_profile_visits);


        mContext=getApplicationContext();
        mDb = GymDatabase.getInstance(getApplicationContext());

        //mAdapter = new SearchAdapter(this);
        mPaymentsAdapter=new CpPaymentsAdapter(this,mDb);
        mVisitsAdapter= new CpVisitsAdapter(this);
        rvVisits.setAdapter(mVisitsAdapter);
        rvPayments.setAdapter(mPaymentsAdapter);

        rvPayments.setLayoutManager(new LinearLayoutManager(this));
        rvVisits.setLayoutManager(new LinearLayoutManager(this));

        Intent i=getIntent();
        clientId=i.getExtras().getInt("CLIENT_ID");

        retrieveData(clientId);
        retrieveDataPayments(clientId);
        retrieveDataVisits(clientId);

        mPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (phoneNr!=null){
                    openWhatsAppContact(phoneNr,firstName);
                    //openWhatsApp2("00505"+phoneNr);
                }

            }
        });

        mQrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (phoneNr!=null){
                    shareQrOnWhatsApp(phoneNr);
                    //openWhatsApp2("00505"+phoneNr);
                }

            }
        });

        mModify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ClientProfileActivity.this,ModifyClientActivity.class);
                i.putExtra("CLIENT_ID",clientId);
                startActivity(i);
            }
        });

        mPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ClientProfileActivity.this,PayClientActivity.class);
                i.putExtra("CLIENT_ID",clientId);
                startActivity(i);
            }
        });


    }

    private void retrieveDataPayments(int clientId) {

        final LiveData<List<PaymentEntry>> payments = mDb.paymentDao().getPaymentsByClient(clientId);
        payments.observe(this, new Observer<List<PaymentEntry>>() {
            @Override
            public void onChanged(@Nullable List<PaymentEntry> paymentEntries) {
                mPaymentsAdapter.setPayments(paymentEntries);
                String paymentsText=mPaymentsAdapter.getItemCount()+" "+getString(R.string.payments);
                mPaymentsCount.setText(paymentsText);

            }
        });
    }

    private void retrieveDataVisits(int clientId) {

        final LiveData<List<VisitEntry>> visits = mDb.visitDao().getVisitsByClient(clientId);
        visits.observe(this, new Observer<List<VisitEntry>>() {
            @Override
            public void onChanged(@Nullable List<VisitEntry> visitEntries) {
                mVisitsAdapter.setVisits(visitEntries);
                String visitsText=mVisitsAdapter.getItemCount()+" "+getString(R.string.visits);
                mVisitCounts.setText(visitsText);

            }
        });
    }

    private void retrieveData(int clientId){
        final LiveData<ClientEntry> client = mDb.clientDao().getClientById(clientId);
        client.observe(this, new Observer<ClientEntry>() {
            @Override
            public void onChanged(@Nullable ClientEntry clientEntry) {
                client.removeObserver(this);
                //now set all vars
                mId.setText("ID: "+clientEntry.getId());
                mFirstName.setText(clientEntry.getFirstName());
                mLastName.setText(clientEntry.getLastName());
                firstName=clientEntry.getFirstName();

                //dob string
                String str_occ="--";
                if (clientEntry.getOccupation()!=null && !clientEntry.getOccupation().isEmpty()) {
                    str_occ=clientEntry.getOccupation();
                }
                //dob string
                String str_dob="--";
                Date dateOfBirth=clientEntry.getDob();
                if (dateOfBirth!=null) {
                    int age= DateMethods.getDiffYears(dateOfBirth,new Date());
                    str_dob=""+age;
                }
                String str_info="("+str_dob+", "+clientEntry.getGender()+", "+str_occ+")";
                mPersonalInfo.setText(str_info);

                String str_phone="--";
                if (clientEntry.getPhone()!=null && !clientEntry.getPhone().isEmpty()){
                    str_phone=clientEntry.getPhone();
                    phoneNr=clientEntry.getPhone();
                }
                mPhone.setText(str_phone);

                //set photo
                String idPart = String.valueOf(clientEntry.getId());
                String imageFileName = "MEDIUM_" + idPart ;
                File storageDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                final File medium = new File(storageDir, imageFileName + ".jpg");
                if (medium.exists()) {
                    String thumb = medium.getAbsolutePath();
                    Bitmap bitmap = BitmapFactory.decodeFile(thumb);
                    RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(mContext.getResources(), bitmap);
                    roundedBitmapDrawable.setCircular(true);
                    mProfilePhoto.setImageDrawable(roundedBitmapDrawable);
                }
                else{
                    mProfilePhoto.setImageResource(R.drawable.camera);
                }


            }
        });

    }


    public void openWhatsAppContact(String phone, String name){
        try {
            String toNumber= PhoneUtilities.depuratePhone(phone);
            toNumber=toNumber.replaceFirst("^0+(?!$)", "");
            String text = getString(R.string.hi)+" "+name;// Replace with your message.
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("http://api.whatsapp.com/send?phone="+toNumber +"&text="+text));
            startActivity(intent);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void shareQrOnWhatsApp(String phone) {
        String toNumber= PhoneUtilities.depuratePhone(phone);
        toNumber=toNumber.replaceFirst("^0+(?!$)", "");
        String idPart = String.valueOf(clientId);
        String qrFileName = "QR_CODE_" + idPart ;
        File storageDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File qrCode = new File(storageDir, qrFileName + ".jpg");
        Uri qrUri = Uri.parse(qrCode.getAbsolutePath());

        Intent sendIntent = new Intent("android.intent.action.SEND");

        sendIntent.setComponent(new ComponentName("com.whatsapp","com.whatsapp.ContactPicker"));
        sendIntent.setType("image");
        sendIntent.putExtra(Intent.EXTRA_STREAM,qrUri);
        sendIntent.putExtra("jid", ""+toNumber+"@s.whatsapp.net");
        sendIntent.putExtra(Intent.EXTRA_TEXT,getString(R.string.hi)+" "+
                firstName+", "+sharedPreferences.getString("gymname",getString(R.string.your_gym))+" "+getString(R.string.welcome_whatsapp_qr_code)+clientId+getString(R.string.asterisc_to_bolden_whatsapp));
        startActivity(sendIntent);
    }



}
