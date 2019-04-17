package com.example.android.gymlogapp;

import android.app.AlertDialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.gymlogapp.data.ClientEntry;
import com.example.android.gymlogapp.data.GymDatabase;
import com.example.android.gymlogapp.data.PaymentEntry;
import com.example.android.gymlogapp.data.VisitEntry;
import com.example.android.gymlogapp.utils.DateMethods;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ClientsSearchActivity extends AppCompatActivity implements ClientsSearchAdapter.ItemClickListener {


    public static final String SEARCH_STRING = "SEARCH_STRING";
    RecyclerView rvClients;
    GymDatabase mDb;
    ClientsSearchAdapter mAdapter;
    Context mContext;
    SearchView searchView;
    Toolbar mToolbar;
    String searchString;
    private ClientEntry mClientData;
    private PaymentEntry mPaymentData;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //normal variable instantiation
        setContentView(R.layout.activity_clients_search);
        mToolbar = (Toolbar) findViewById(R.id.toolbar_clients);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        mContext = getApplicationContext();
        rvClients = (RecyclerView) findViewById(R.id.rv_clients_search_activity);
        mAdapter = new ClientsSearchAdapter(mContext, this);
        mDb = GymDatabase.getInstance(getApplicationContext());

        rvClients.setAdapter(mAdapter);
        rvClients.setLayoutManager(new LinearLayoutManager(this));
        if (savedInstanceState==null){
            searchString = "";
        }else{
            searchString=savedInstanceState.getString(SEARCH_STRING);
        }

        populateDataSource(searchString);
    }

    private void populateDataSource(String s) {
        String str = s + "%";

        final LiveData<List<ClientEntry>> clients = mDb.clientDao().getRegularClientByName(str);
        clients.observe(this, new Observer<List<ClientEntry>>() {
            @Override
            public void onChanged(@Nullable List<ClientEntry> clientEntries) {
                mAdapter.setClients(clientEntries);
                mToolbar.setSubtitle(mAdapter.getItemCount()+" "+getString(R.string.clients));

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_only, menu);
        searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(onQueryTextListener);
        searchView.setQuery(searchString,true);
        return super.onCreateOptionsMenu(menu);
    }

    private SearchView.OnQueryTextListener onQueryTextListener =
            new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    populateDataSource(query);
                    searchView.clearFocus();
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    populateDataSource(newText);
                    searchString=newText;

                    return true;
                }
            };

    //remember if phone is flipped
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        savedInstanceState.putString(SEARCH_STRING, searchString);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onItemClickListener(int itemId) {

        retrieveClientData(itemId);

    }


    private void retrieveClientData(int clientId){
        counter=0;
        final LiveData<ClientEntry> client = mDb.clientDao().getClientById(clientId);
        client.observe(this, new Observer<ClientEntry>() {
            @Override
            public void onChanged(@Nullable ClientEntry clientEntry) {
                client.removeObserver(this);
                mClientData=clientEntry;
                //Toast.makeText(getApplicationContext(),mClientData.getFirstName(),Toast.LENGTH_LONG).show();
                taskCompleted();
            }
        });

        final LiveData<PaymentEntry> currentPayment = mDb.paymentDao().getCurrentPaymentByClient(clientId,new Date());
        currentPayment.observe(this, new Observer<PaymentEntry>() {
            @Override
            public void onChanged(@Nullable PaymentEntry paymentEntry) {
                currentPayment.removeObserver(this);
                mPaymentData=paymentEntry;
                //Toast.makeText(getApplicationContext(),mPaymentData.getProduct(),Toast.LENGTH_LONG).show();
                taskCompleted();
            }
        });

    }
    //sync db response
    private  final int NUMBER_DB_CALLS = 2;
    private  int counter = 0;
    public synchronized void taskCompleted(){
        counter++;
        if(counter == NUMBER_DB_CALLS){

            boolean isPayingClient=true;
            String access="G";

            if (mClientData==null){
                Toast.makeText(getApplicationContext(),"This QR code has not been assigned",Toast.LENGTH_LONG).show();

            }else {

                if (mPaymentData==null){
                    isPayingClient=false;
                    access="D";
                }

                final VisitEntry visitEntry= new VisitEntry(mClientData.getId(),new Date(),access);
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        mDb.visitDao().insertVisit(visitEntry);
                    }
                });

                //display dialog box
                displayDialog(isPayingClient);
            }
        }
    }

    MediaPlayer mSoundPass;
    MediaPlayer mSoundFail;

    //make dialog pop up
    private void displayDialog(final boolean isPayingClient){
        AlertDialog.Builder mBuilder=new AlertDialog.Builder(ClientsSearchActivity.this);
        View mView=getLayoutInflater().inflate(R.layout.dialog_welcome,null);
        Date today= DateMethods.getRoundDate(new Date());
        //view assignment
        TextView mFirstLineTop=(TextView) mView.findViewById(R.id.tv_welcome);
        TextView mSecondLineTop=(TextView) mView.findViewById(R.id.tv_welcome_name);
        TextView mFirstLineBottom=(TextView) mView.findViewById(R.id.tv_payment_info1);
        TextView mSecondLineBottom=(TextView) mView.findViewById(R.id.tv_payment_info2);
        ImageView mPhoto=(ImageView) mView.findViewById(R.id.iv_welcome_image);
        View mTopStrip=(View) mView.findViewById(R.id.v_top_view);
        View mBottomStrip=(View) mView.findViewById(R.id.v_bottom_view);

        mSoundPass=MediaPlayer.create(getApplicationContext(),R.raw.correct_sound);
        mSoundFail=MediaPlayer.create(getApplicationContext(),R.raw.error_sound);

        setPic(mPhoto,mClientData.getId());

        Button mDismiss=(Button) mView.findViewById(R.id.bt_back);

        //conditional on passing logic
        if (isPayingClient){
            long daysLeft=TimeUnit.DAYS.convert(mPaymentData.getPaidUntil().getTime()-today.getTime(), TimeUnit.MILLISECONDS);
            mTopStrip.setBackgroundColor(getResources().getColor(R.color.colorGreen));
            mBottomStrip.setBackgroundColor(getResources().getColor(R.color.colorGreen));
            mFirstLineTop.setText(R.string.welcome);
            mSecondLineTop.setText(mClientData.getFirstName()+",");
            mFirstLineBottom.setText(""+daysLeft);
            mSecondLineBottom.setText(R.string.days_access_remaining);
            if (daysLeft<4){
                mFirstLineBottom.setTextColor(getResources().getColor(R.color.colorRed));
            }else{
                mFirstLineBottom.setTextColor(getResources().getColor(android.R.color.tertiary_text_dark));
            }
        }else{
            mTopStrip.setBackgroundColor(getResources().getColor(R.color.colorRed));
            mBottomStrip.setBackgroundColor(getResources().getColor(R.color.colorRed));
            mFirstLineTop.setText(getString(R.string.sorry)+" "+mClientData.getFirstName()+",");
            mSecondLineTop.setText(R.string.your_access_has_expired);
            mFirstLineBottom.setText(R.string.please_pay_access);
            mSecondLineBottom.setText(R.string.thanks_for_staying);
            //Format
            mFirstLineTop.setTextSize(28);
            mFirstLineTop.setTypeface(Typeface.DEFAULT_BOLD);
            mSecondLineTop.setTextSize(12);
            mSecondLineTop.setTypeface(Typeface.DEFAULT);
            mFirstLineBottom.setTextSize(12);
            mFirstLineBottom.setTypeface(Typeface.DEFAULT);
            mSecondLineBottom.setTypeface(Typeface.DEFAULT);
        }

        mBuilder.setView(mView);
        final AlertDialog dialog=mBuilder.create();

        //button to dismiss dialog
        mDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

            }
        });

        // Handler to
        Handler handler  = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        };
        handler.postDelayed(runnable, 2000);

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {

                if (isPayingClient){
                    mSoundPass.start();
                }else{
                    mSoundFail.start();
                }

            }
        });
        dialog.show();

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                mClientData=null;
                mPaymentData=null;
                mSoundPass.release();
                mSoundPass=null;
                mSoundFail.release();
                mSoundFail=null;
                //in this case we go back to the scanner
                Intent i= new Intent(getApplicationContext(),MainActivity.class);
                startActivity(i);
            }
        });
    }

    //logic for setting the pick
    private void setPic(ImageView imageView, int clientId) {
        String imageFileName = "MEDIUM_" + clientId ;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File medium = new File(storageDir, imageFileName + ".jpg");
        if (medium.exists()) {
            String clientMedium = medium.getAbsolutePath();
            Bitmap medBit = BitmapFactory.decodeFile(clientMedium);
            //Bitmap bitScaled = Bitmap.createScaledBitmap(medBit, 180, 180, false);
            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), medBit);
            roundedBitmapDrawable.setCircular(true);
            imageView.setImageDrawable(roundedBitmapDrawable);
        }else{
            imageView.setImageResource(android.R.drawable.ic_menu_camera);
        }
    }
}
