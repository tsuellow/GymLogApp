package com.example.android.gymlogapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.support.design.widget.TextInputLayout;

import com.example.android.gymlogapp.data.ClientEntry;
import com.example.android.gymlogapp.data.GymDatabase;
import com.example.android.gymlogapp.utils.DateMethods;
import com.example.android.gymlogapp.utils.PhoneUtilities;
import com.example.android.gymlogapp.utils.QrCodeUtilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class NewClientActivity extends AppCompatActivity {

    static final int REQUEST_TAKE_PHOTO = 1;
    static final int MY_PERMISSIONS_REQUEST_WRITE_CONTACTS =2;
    public static final int REQUEST_CODE = 10;
    EditText mId, mFirstName,mLastName,mPhone, mDob;
    AutoCompleteTextView mOccupation;
    TextInputLayout ilId,ilFirstName,ilLastName,ilPhone, ilOccupation, ilDob;
    RadioGroup mGender;
    RadioButton mChosenGender;

    ImageView mPhoto;
    Button mTakePic;
    Button mSubmit;
    DatePickerDialog.OnDateSetListener onDateSetListener;
    Date dateOfBirth=null;
    int isNew=-1;


    private GymDatabase mDb;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_client);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mId=(EditText) findViewById(R.id.ev_id);
        //temporary workaround to see if id is new
        mId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                try{
                    String id=mId.getText().toString().trim();
                    final int parseId=Integer.parseInt(id);
                    AppExecutors.getInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            final int result=mDb.clientDao().isIdNew(parseId);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setIsNewId(result);
                                }
                            });
                        }
                    });
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        });



        mFirstName =(EditText) findViewById(R.id.ev_first_name);
        mLastName=(EditText) findViewById(R.id.ev_last_name);
        mPhone=(EditText) findViewById(R.id.ev_phone);

        mOccupation=(AutoCompleteTextView) findViewById(R.id.actv_occupation);
        ArrayAdapter<String> occupationAdapter=new ArrayAdapter<String>(NewClientActivity.this,
                android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.occupation_array));
        mOccupation.setAdapter(occupationAdapter);
        mOccupation.setKeyListener(null);
        mOccupation.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                ((AutoCompleteTextView) view).showDropDown();
                return false;
            }
        });

        mDob=(EditText) findViewById(R.id.ev_dob);
        mDob.setInputType(InputType.TYPE_NULL);

        ilId=(TextInputLayout) findViewById(R.id.lo_id);
        ilFirstName=(TextInputLayout) findViewById(R.id.lo_first_name);
        ilLastName=(TextInputLayout) findViewById(R.id.lo_last_name);
        ilPhone=(TextInputLayout) findViewById(R.id.lo_phone);
        ilOccupation=(TextInputLayout) findViewById(R.id.lo_occupation);
        ilDob=(TextInputLayout) findViewById(R.id.lo_dob);

        mGender=(RadioGroup) findViewById(R.id.rg_gender);




        mPhoto=(ImageView) findViewById(R.id.iv_photo);
        mTakePic=(Button) findViewById(R.id.bt_take_photo);
        mSubmit=(Button) findViewById(R.id.bt_submit);
        mDb=GymDatabase.getInstance(getApplicationContext());

        mPhone.addTextChangedListener(new PhoneNumberFormattingTextWatcher("NI"));

        mDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal=Calendar.getInstance();
                cal.set(2000,0,1);
                Date defaultDate=cal.getTime();
                displayDatePickerDialog(defaultDate);
            }
        });

        //take photo click listener
        mTakePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkId()){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkSelfPermission(Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED) {
                            dispatchTakePictureIntent();

                        }else{
                            String[] permissionRequested={Manifest.permission.CAMERA};
                            requestPermissions(permissionRequested, REQUEST_CODE);
                        }

                    }
                }

            }
        });

        //submit click listener
        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSubmit();

            }
        });

        //check contacts permission
        if (ContextCompat.checkSelfPermission(NewClientActivity.this,
                Manifest.permission.WRITE_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {


            if (ActivityCompat.shouldShowRequestPermissionRationale(NewClientActivity.this,
                    Manifest.permission.WRITE_CONTACTS)) {

            } else {

                ActivityCompat.requestPermissions(NewClientActivity.this,
                        new String[]{Manifest.permission.WRITE_CONTACTS},
                        MY_PERMISSIONS_REQUEST_WRITE_CONTACTS);

            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setPicture();
    }

    //to be evaluated jointly on submit
    private void onSubmit(){
        boolean firstNameCorrect=checkFirstName();
        boolean lastNameCorrect=checkLastName();
        boolean phoneCorrect=checkPhoneNumber();
        boolean idCorrect=checkId();

        if(firstNameCorrect && lastNameCorrect && phoneCorrect && idCorrect){
            populateDb();
            //preparing background tasks
            final Context context=getApplicationContext();

            final String qrText="{\"obj\":\"l\",\""+MainActivity.GYM_ID+"id\":"+mId.getText().toString()+"}";
            final File qrFile = createQrCodeFile();

            final String displayName=mFirstName.getText().toString()+" "+mLastName.getText().toString()+" ID: "+mId.getText().toString();
            final String mobileNumber=mPhone.getText().toString();

            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    saveQrCode(qrText,qrFile,context);
                    String contactsToast;
                    if(addContact(displayName,mobileNumber,context)){
                        contactsToast=getString(R.string.added_to_contacts);
                    }else{
                        contactsToast=getString(R.string.error_saving_contacts);
                    }
                    final String toast=contactsToast;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });

            Intent i=new Intent(getApplicationContext(),SearchActivity.class);
            startActivity(i);
        }

    }

    //single conditions for evaluation
    private boolean checkFirstName(){
        if (mFirstName.getText().toString().trim().isEmpty()){
            ilFirstName.setErrorEnabled(true);
            ilFirstName.setError(getString(R.string.err_first_name));
            mFirstName.setError(getString(R.string.input_required));
            return false;
        }
        ilFirstName.setErrorEnabled(false);
        return true;
    }
    private boolean checkLastName(){
        if (mLastName.getText().toString().trim().isEmpty()){
            ilLastName.setErrorEnabled(true);
            ilLastName.setError(getString(R.string.err_last_name));
            mLastName.setError(getString(R.string.input_required));
            return false;
        }
        ilLastName.setErrorEnabled(false);
        return true;
    }
    private boolean checkPhoneNumber(){
        if (mPhone.getText().toString().length()==0){
            ilPhone.setErrorEnabled(false);
            return true;
        }
        String phoneText= PhoneUtilities.depuratePhone(mPhone.getText().toString());
        try{
            String checkText=phoneText.replaceFirst("^0+(?!$)", "");
            Long.parseLong(checkText);
            if (checkText.length()>=11){
                ilPhone.setErrorEnabled(false);
                return true;
            }else{
                ilPhone.setErrorEnabled(true);
                ilPhone.setError(getString(R.string.err_phone));
                mPhone.setError(getString(R.string.valid_phone_required));
                return false;
            }
        }catch(Exception e){
            Toast.makeText(getApplicationContext(),phoneText,Toast.LENGTH_SHORT).show();
            ilPhone.setErrorEnabled(true);
            ilPhone.setError(getString(R.string.err_phone));
            mPhone.setError(getString(R.string.valid_phone_required));
            return  false;
        }
    }

    //check id validity with two different error messages
    void setIsNewId(int result){
        isNew=result;
    }
    private boolean checkId(){
        String id=mId.getText().toString().trim();
        try {
            Integer.parseInt(id);
        }catch(Exception e){
            ilId.setErrorEnabled(true);
            ilId.setError(getString(R.string.id_has_to_be_numeric));
            mId.setError(getString(R.string.id_has_to_be_numeric));
            //Toast.makeText(this,getString(R.string.id_has_to_be_numeric), Toast.LENGTH_LONG).show();
            return false;
        }

        if (isNew==1){
            ilId.setErrorEnabled(false);
            return  true;
        }else if (isNew==0){
            ilId.setErrorEnabled(true);
            ilId.setError(getString(R.string.id_has_to_be_new));
            mId.setError(getString(R.string.id_has_to_be_new));
            //Toast.makeText(this,"Id needs to be new", Toast.LENGTH_LONG).show();
            return false;
        }else{
            Toast.makeText(this, R.string.click_again, Toast.LENGTH_LONG).show();
            return false;
        }


    }






    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==REQUEST_CODE){
            if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
                dispatchTakePictureIntent();
            }else{
                Toast.makeText(this, R.string.camera_permission_request,Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode==REQUEST_TAKE_PHOTO && resultCode==RESULT_OK){
            //setPic();
            takePic();
            setPicture();
        }
    }

    private void populateDb() {
            int genderId=mGender.getCheckedRadioButtonId();
            mChosenGender=(RadioButton) findViewById(genderId);
            String gender=mChosenGender.getText().toString().substring(0, 1);


            int id=Integer.parseInt(mId.getText().toString());
            String firstName= mFirstName.getText().toString();
            String lastName=mLastName.getText().toString();
            String phone=mPhone.getText().toString();
            String occupation=mOccupation.getText().toString();
            String photoDir=createImageFile().getAbsolutePath();
            Date date=new Date();

            final ClientEntry clientEntry=new ClientEntry(id,firstName,lastName,dateOfBirth,gender,occupation,phone, photoDir, null, date);
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    mDb.clientDao().insertClient(clientEntry);
                }
            });

    }

    //take a photo functionality

    private File createImageFile()  {
        // Create an image file name
        String idPart = mId.getText().toString();//new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "PHOTO_ID_" + idPart ;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = new File(storageDir, imageFileName + ".jpg");

        return image;
    }
    private File createQrCodeFile()  {
        // Create an image file name
        String idPart = mId.getText().toString();//new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "QR_CODE_" + idPart ;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File qrCode = new File(storageDir, imageFileName + ".jpg");
        // Save a file: path for use with ACTION_VIEW intents
        return qrCode;
    }
    private File createMediumFile()  {
        // Create an image file name
        String idPart = mId.getText().toString();//new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "MEDIUM_" + idPart ;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File medium = new File(storageDir, imageFileName + ".jpg");
        // Save a file: path for use with ACTION_VIEW intents
        return medium;
    }
    private File createThumbnailFile()  {
        // Create an image file name
        String idPart = mId.getText().toString();
        String imageFileName = "THUMB_" + idPart ;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        //File image = new File(storageDir, imageFileName + ".jpg");
        File thumbnail = new File(storageDir, imageFileName + ".jpg");
        return thumbnail;
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;

                photoFile = createImageFile();

            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider_gymlogapp",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                Log.d("bello",  photoURI.toString());

            }

        }

    }


    //code to frame and display pic
    private void takePic(){
        Bitmap bitmap = BitmapFactory.decodeFile(createImageFile().getAbsolutePath());
        int width  = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newWidth = (height > width) ? width : height;
        int newHeight = (height > width)? height - ( height - width) : height;
        int cropW = (width - height) / 2;
        cropW = (cropW < 0)? 0: cropW;
        int cropH = (height - width) / 2;
        cropH = (cropH < 0)? 0: cropH;
        Bitmap cropImg = Bitmap.createBitmap(bitmap, cropW, cropH, newWidth, newHeight);
        savePhotoThumbMed(cropImg);
        if (createImageFile().exists()){
            createImageFile().delete();
        }
    }
    private void setPicture(){
        File medium=createMediumFile();
        String clientMedium=medium.getAbsolutePath();
        if (medium.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(clientMedium);
            RoundedBitmapDrawable roundedBitmapDrawable=RoundedBitmapDrawableFactory.create(getResources(),bitmap);
            roundedBitmapDrawable.setCircular(true);
            mPhoto.setImageDrawable(roundedBitmapDrawable);
        }else{
            mPhoto.setImageResource(R.drawable.camera);
        }

    }



    private void savePhotoThumbMed(final Bitmap bitmap) {
        try {
            File thumbFile = createThumbnailFile();
            File mediumFile = createMediumFile();

            if (thumbFile.exists()){
                thumbFile.delete();
            }
            if (mediumFile.exists()){
                mediumFile.delete();
            }

            FileOutputStream thumbOut = new FileOutputStream(thumbFile);
            FileOutputStream mediumOut = new FileOutputStream(mediumFile);

            Bitmap thumb = Bitmap.createScaledBitmap(bitmap, 96, 96, false);
            Bitmap medium = Bitmap.createScaledBitmap(bitmap, 1000, 1000, false);
//            String qrText="{\"obj\":\"l\",\"ufid\":"+mId.getText().toString()+"}";
//            Bitmap qrCode= QrCodeUtilities.GenerateQrCode(getApplicationContext(),qrText);
            thumb.compress(Bitmap.CompressFormat.JPEG, 100, thumbOut);
            medium.compress(Bitmap.CompressFormat.JPEG, 100, mediumOut);

            thumbOut.flush();
            mediumOut.flush();

            thumbOut.close();
            mediumOut.close();
            Log.d("ThumbMed saved", "Thumb and Medium ok");
        } catch (IOException ex) {
            ex.printStackTrace();
            Log.d("ThumbMed saved", "Thumb Medium IOException");
        }
    }

    private void saveQrCode(String qrText, File qrFile, Context context){
        try {

            if (qrFile.exists()){
                qrFile.delete();
            }
            FileOutputStream qrOut = new FileOutputStream(qrFile);
            //String qrText="{\"obj\":\"l\",\""+MainActivity.GYM_ID+"id\":"+mId.getText().toString()+"}";
            Bitmap qrCode= QrCodeUtilities.GenerateQrCode(context,qrText);
            qrCode.compress(Bitmap.CompressFormat.JPEG, 100, qrOut);
            qrOut.flush();
            qrOut.close();
            Log.d("QR saved", "QR ok");
        } catch (IOException ex) {
            ex.printStackTrace();
            Log.d("QR saved", "QR IOException");
        }
    }

    private boolean addContact(String DisplayName,String MobileNumber, Context context)
    {

        MobileNumber = MobileNumber.replace(" ", "");

        ArrayList < ContentProviderOperation > ops = new ArrayList< ContentProviderOperation >();

        ops.add(ContentProviderOperation.newInsert(
                ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        //------------------------------------------------------ Names
            ops.add(ContentProviderOperation.newInsert(
                    ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(
                            ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                            DisplayName).build());

        //------------------------------------------------------ Mobile Number
        if (MobileNumber != null && !MobileNumber.isEmpty()) {
            MobileNumber=PhoneUtilities.depuratePhone(MobileNumber);
            ops.add(ContentProviderOperation.
                    newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, MobileNumber)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .build());
        }
        // Asking the Contact provider to create a new contact
        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("contacts fail","Exception: " + e.getMessage());
            return  false;
        }
    }

    private void displayDatePickerDialog(Date date){
        AlertDialog.Builder mBuilder=new AlertDialog.Builder(NewClientActivity.this);
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
        mBuilder.setTitle(R.string.dob);
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
                mDob.setText(DateMethods.getDateString(cal));
                dateOfBirth=DateMethods.getRoundDate(cal.getTime());
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
