package com.example.android.gymlogapp;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.android.gymlogapp.data.ClientEntry;
import com.example.android.gymlogapp.data.GymDatabase;
import com.example.android.gymlogapp.data.PaymentEntry;
import com.example.android.gymlogapp.data.VisitEntry;
import com.example.android.gymlogapp.utils.DateMethods;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;
import com.journeyapps.barcodescanner.camera.CameraSettings;
import android.view.Menu;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity{

    public static final String GYM_ID="zz";
    public static final String USER_NAME="GenericTestGym";

    public static final String CHANNEL_ID="111";
    private Button mManualSearch;
    private Context mContext;
    private GymDatabase mDb;
    private DecoratedBarcodeView barcodeScannerView;
    private String lastText;
    private ClientEntry mClientData;
    private PaymentEntry mPaymentData;
    private Toolbar mToolbar;
    private ImageView mFlashLight;
    private ConstraintLayout mTopBar;
    private LinearLayout mBottomBar;
    private int mBarColor, mTextColor;
    private TextView mOrientation;
    private boolean mIsAnimated;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set toolbar programatically to allow for logo and text from xml to display
        mToolbar = (Toolbar) findViewById(R.id.toolbar_main);
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);

        //define which camera to use
        sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);
        boolean frontCamera=sharedPreferences.getBoolean("camera",true);

        CameraSettings cameraSettings = new CameraSettings();
        if (frontCamera) {
            cameraSettings.setRequestedCameraId(1);
        }else{
            cameraSettings.setRequestedCameraId(0);
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //set multiple vars
        mOrientation=(TextView) findViewById(R.id.tv_orientation);
        mFlashLight=(ImageView) findViewById(R.id.iv_flash_light);
        mTopBar=(ConstraintLayout) findViewById(R.id.cl_main_top);
        mBottomBar=(LinearLayout) findViewById(R.id.ll_main_bottom);
        mBarColor=mBottomBar.getSolidColor();
        mTextColor=mOrientation.getCurrentTextColor();
        mIsAnimated=false;
        mFlashLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mIsAnimated) {
                    mIsAnimated=true;
                    mOrientation.setTextColor(Color.parseColor("#757575"));
                    mTopBar.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                    mBottomBar.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                    //dalay change to normal by 3 seconds
                    Handler handler = new Handler();
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            flashLightAnimation();
                            mIsAnimated=false;
                        }
                    };
                    handler.postDelayed(runnable, 3000);
                }
            }
        });

        mManualSearch =(Button) findViewById(R.id.bt_search);

        mContext=getApplicationContext();

        mDb=GymDatabase.getInstance(mContext);

        //define bar scanner settings
        barcodeScannerView = (DecoratedBarcodeView)findViewById(R.id.zxing_barcode_scanner);
        Collection<BarcodeFormat> formats = Arrays.asList(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_39);
        barcodeScannerView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(formats));
        barcodeScannerView.initializeFromIntent(getIntent());
        barcodeScannerView.getBarcodeView().setCameraSettings(cameraSettings);
        barcodeScannerView.setStatusText(getString(R.string.focus_barcode));
        barcodeScannerView.decodeContinuous(callback);

        //manual search button
        final boolean secureManualSearch=sharedPreferences.getBoolean("manualsearch",false);
        mManualSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!secureManualSearch) {
                    Intent i = new Intent(getApplicationContext(), ClientsSearchActivity.class);
                    startActivity(i);
                }else{
                    Intent i = new Intent(getApplicationContext(), LoginScreen.class);
                    i.putExtra("goal","manual_search");
                    startActivity(i);
                }
            }
        });

        createNotificationChannel();
    }

    //flashlight method
    private void flashLightAnimation(){
        ObjectAnimator colorAnimTop = ObjectAnimator.ofInt(mTopBar, "backgroundColor",
                getResources().getColor(R.color.colorWhite), mBarColor);
        colorAnimTop.setDuration(1000);
        colorAnimTop.setEvaluator(new ArgbEvaluator());
        colorAnimTop.start();
        ObjectAnimator colorAnimBottom = ObjectAnimator.ofInt(mBottomBar, "backgroundColor",
                getResources().getColor(R.color.colorWhite), mBarColor);
        colorAnimBottom.setDuration(1000);
        colorAnimBottom.setEvaluator(new ArgbEvaluator());
        colorAnimBottom.start();
        ObjectAnimator colorAnimText = ObjectAnimator.ofInt(mOrientation, "textColor",
                Color.parseColor("#757575"), mTextColor);
        colorAnimText.setDuration(1000);
        colorAnimText.setEvaluator(new ArgbEvaluator());
        colorAnimText.start();
    }


    //Callback from reader logic
    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if(result.getText() == null
                    || result.getText().equals(lastText)
            ) {
                // Prevent duplicate scans
                return;
            }
            //remember previous scan to prevent continuous scan
            lastText = result.getText();
            try {
                //decode JSON
                JSONObject jObj = new JSONObject(lastText);
                int jsonId = jObj.getInt(GYM_ID+"id");
                retrieveClientData(jsonId);
            }catch(JSONException e){
                Toast.makeText(getApplicationContext(), R.string.unrecognized_code, Toast.LENGTH_LONG).show();
            }
        }
        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    //necessary boilerplate
    @Override
    protected void onResume() {
        super.onResume();

        barcodeScannerView.resume();

    }

    @Override
    protected void onPause() {
        super.onPause();

        barcodeScannerView.pause();
    }

    //inflate menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.opt_admin:{
                Intent login =new Intent(getApplicationContext(),LoginScreen.class);
                login.putExtra("goal","admin");
                startActivity(login);
                break;
            }
            case R.id.opt_class:{
                Intent currentClass= new Intent(getApplicationContext(),CurrentClassActivity.class);
                startActivity(currentClass);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }


    //logic for retrieving data
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
    //sync db response to prevent asynchronous response issue (avoidable with room, now you know better)
    private  final int NUMBER_DB_CALLS = 2;
    private  int counter = 0;
    public synchronized void taskCompleted(){
        counter++;
        if(counter == NUMBER_DB_CALLS){

            //remember las text scanned and forget after 5 secs
            Handler forgetLastQR  = new Handler();
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    lastText=null;
                }
            };

            boolean isPayingClient=true;
            String access="G";

            if (mClientData==null){
                Toast toast=Toast.makeText(getApplicationContext(), R.string.unassigned_qr_code,Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER|Gravity.CENTER, 0, 0);
                toast.show();
                forgetLastQR.postDelayed(run, 5000);
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

                //logic to add payment when visitor enters gym
                if (mClientData.getId()<0) {
                    int singleVisitId = mClientData.getId();
                    String singlePaymentStr1 = sharedPreferences.getString("passminus1", "0");
                    String singlePaymentStr2 = sharedPreferences.getString("passminus2", "0");
                    String exchangeRateStr = sharedPreferences.getString("usd2cs", "1");
                    String currencyKey = sharedPreferences.getString("preferredcurrency","USD");
                    String currency=null;
                    if (currencyKey.contentEquals("usd_key")){currency="USD";}else{currency="C$";}

                    if (singleVisitId == -1) {
                        try {
                            float singlePayment1 = Float.valueOf(singlePaymentStr1);
                            float exchangeRate = Float.valueOf(exchangeRateStr);
                            final PaymentEntry singlePassPay = new PaymentEntry(singleVisitId, getString(R.string.single_day_pass), singlePayment1/exchangeRate,
                                    new Date(), new Date(), new Date(),exchangeRate,"C$",null,null);
                            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                                @Override
                                public void run() {
                                    mDb.paymentDao().insertPayment(singlePassPay);
                                }
                            });
                        } catch (Exception e) {
                            Toast.makeText(this, R.string.wrong_day_pass_price,Toast.LENGTH_LONG).show();
                        }
                    }
                    else if (singleVisitId==-2){
                        try {
                            float singlePayment2=Float.valueOf(singlePaymentStr2);
                            float exchangeRate = Float.valueOf(exchangeRateStr);
                            final PaymentEntry singlePassPay=new PaymentEntry(singleVisitId,getString(R.string.single_day_pass),singlePayment2/exchangeRate,
                                    new Date(),new Date(),new Date(),exchangeRate,"C$",null,null);
                            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                                @Override
                                public void run() {
                                    mDb.paymentDao().insertPayment(singlePassPay);
                                }
                            });
                        }catch (Exception e){
                            Toast.makeText(this, R.string.wrong_day_pass_price,Toast.LENGTH_LONG).show();
                        }
                    }
                }

                //display dialog box
                displayDialog(isPayingClient);
            }
        }
    }



    MediaPlayer mSoundPass;
    MediaPlayer mSoundFail;

    //make dialog pop up
    private void displayDialog(final boolean isPayingClient){
        AlertDialog.Builder mBuilder=new AlertDialog.Builder(MainActivity.this);
        View mView=getLayoutInflater().inflate(R.layout.dialog_welcome,null);
        Date today= DateMethods.getRoundDate(new Date());
        //find views
        TextView mFirstLineTop=(TextView) mView.findViewById(R.id.tv_welcome);
        TextView mSecondLineTop=(TextView) mView.findViewById(R.id.tv_welcome_name);
        TextView mFirstLineBottom=(TextView) mView.findViewById(R.id.tv_payment_info1);
        TextView mSecondLineBottom=(TextView) mView.findViewById(R.id.tv_payment_info2);
        ImageView mPhoto=(ImageView) mView.findViewById(R.id.iv_welcome_image);
        View mTopStrip=(View) mView.findViewById(R.id.v_top_view);
        View mBottomStrip=(View) mView.findViewById(R.id.v_bottom_view);
        //create sounds
        mSoundPass=MediaPlayer.create(getApplicationContext(),R.raw.correct_sound);
        mSoundFail=MediaPlayer.create(getApplicationContext(),R.raw.error_sound);

        //set photo
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
                mFirstLineBottom.setTextColor(getResources().getColor(android.R.color.secondary_text_dark));
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

        // Handler to self dismiss dialog after 9 secs
        Handler handler  = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        };
        handler.postDelayed(runnable, 3000);

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
        //drop all data for when next client comes
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                mClientData=null;
                mPaymentData=null;
                mSoundPass.release();
                mSoundPass=null;
                mSoundFail.release();
                mSoundFail=null;

                lastText=null;
            }
        });
    }

    //function to show image
    private void setPic(ImageView imageView, int clientId) {
        String imageFileName = "MEDIUM_" + clientId;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File medium = new File(storageDir, imageFileName + ".jpg");
        if (medium.exists()) {
            String clientMedium = medium.getAbsolutePath();
            Bitmap medBit = BitmapFactory.decodeFile(clientMedium);
            //Bitmap bitScaled = Bitmap.createScaledBitmap(medBit, 180, 180, false);
            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), medBit);
            roundedBitmapDrawable.setCircular(true);
            imageView.setImageDrawable(roundedBitmapDrawable);
        } else {
            imageView.setImageResource(R.drawable.camera);
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "gymlog_notification_channel";
            String description = "channel for backup notifications from server";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }



}
