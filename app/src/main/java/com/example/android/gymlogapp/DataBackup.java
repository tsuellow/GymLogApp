package com.example.android.gymlogapp;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.android.gymlogapp.data.ClientEntry;
import com.example.android.gymlogapp.data.DateConverter;
import com.example.android.gymlogapp.data.GymDatabase;
import com.example.android.gymlogapp.data.PaymentEntry;
import com.example.android.gymlogapp.data.VisitEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class DataBackup {
    Context mContext;
    SharedPreferences sharedPreferences;


    public DataBackup(Context context, SharedPreferences sharedPrefs) {
        mContext=context;
        sharedPreferences=sharedPrefs;
    }

    //String HOST_ADDRESS = sharedPreferences.getString("serverip", "192.168.1.6");
    String HOST_ADDRESS = "https://mysqlsvr71admin.world4you.com";

    //functions and logic for data backup

    //ckeck internet connectivity
    public  boolean hasInternetConnectivity(){
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return ((netInfo != null) );
    }

    public boolean hasHostAccess() {
        try{
            URL myUrl = new URL(HOST_ADDRESS);
            URLConnection connection = myUrl.openConnection();
            connection.setConnectTimeout(5000);
            connection.connect();
            return true;
        } catch (Exception e) {
            // Handle your exceptions
            return false;
        }
    }



//    public String SERVER_URL="http://"+HOST_ADDRESS.trim()+"/gymlog/";
    public String SERVER_URL="https://www.id-ex.de/GymLog/php/";


    private int COUNTER_SYNCED_CLIENT;
    private int SYNC_CLIENT_VOLUME;
    //for payments
    private int COUNTER_SYNCED_PAYMENT;
    private int SYNC_PAYMENT_VOLUME;
    //for visits
    private int COUNTER_SYNCED_VISIT;
    private int SYNC_VISIT_VOLUME;

    //backup entire client synclist
    public void backupTables(List<ClientEntry> clients, List<PaymentEntry> payments, List<VisitEntry>  visits){
        COUNTER_SYNCED_CLIENT =0;
        SYNC_CLIENT_VOLUME=clients.size();
        COUNTER_SYNCED_PAYMENT =0;
        SYNC_PAYMENT_VOLUME=payments.size();
        COUNTER_SYNCED_VISIT =0;
        SYNC_VISIT_VOLUME=visits.size();
        CountDownLatch requestCountDown = new CountDownLatch(1);

        JSONObject backupJson= createAllJson(clients,payments,visits);

        syncAll(backupJson,mContext,requestCountDown);

        new Thread(new ThreadToBeHeld(requestCountDown)).start();
    }


    class ThreadToBeHeld implements Runnable {
        CountDownLatch requestCountDown;
        Handler mainThreadHandler = new Handler(Looper.getMainLooper());
        public ThreadToBeHeld(CountDownLatch requestCountDown) {
            this.requestCountDown = requestCountDown;
        }
        public void run() {
            try {
                //requestCountDown.countDown();
                requestCountDown.await();

            }catch(InterruptedException ex){}
            mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
//                    Toast.makeText(getApplicationContext(),"clients "+COUNTER_SYNCED_CLIENT+
//                            "payments "+COUNTER_SYNCED_PAYMENT+"" +
//                            "visits "+COUNTER_SYNCED_VISIT,Toast.LENGTH_LONG).show();
                    showPositiveDialog();
                }
            });
        }
    };

    class CountdownExecuted implements Runnable {
        CountDownLatch requestCountDown;
        public CountdownExecuted(CountDownLatch requestCountDown) {
            this.requestCountDown = requestCountDown;
        }
        public void run() {
            requestCountDown.countDown();
        }};


    //sync to server
    private void syncAll(final JSONObject backupJson, final Context context, final CountDownLatch countDownLatch){

        JsonObjectRequest jsonObjectRequest;

            jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.POST, SERVER_URL + "backup_all.php", backupJson, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String res = response.getString("response");
                                COUNTER_SYNCED_CLIENT=response.getInt("counter_client");
                                COUNTER_SYNCED_PAYMENT=response.getInt("counter_payment");
                                COUNTER_SYNCED_VISIT=response.getInt("counter_visit");
                                Log.d("belloy","works");
                                if (res.equals("OK")) {
                                    //update sqlite db
                                    AppExecutors.getInstance().diskIO().execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            GymDatabase.getInstance(context).clientDao().bulkUpdateClientSyncStatus();
                                            GymDatabase.getInstance(context).paymentDao().bulkUpdatePaymentSyncStatus();
                                            GymDatabase.getInstance(context).visitDao().bulkUpdateVisitSyncStatus();
                                        }
                                    });
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.d("belloy"," wtf");
                            }
                            new Thread(new CountdownExecuted(countDownLatch)).start();

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                            Log.d("belloy"," error");
                            new Thread(new CountdownExecuted(countDownLatch)).start();
                        }
                    });
        MySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    public void syncAllAutomatic(final JSONObject backupJson, final Context context){

        JsonObjectRequest jsonObjectRequest;

        jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, SERVER_URL + "backup_all.php", backupJson, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String res = response.getString("response");
                            COUNTER_SYNCED_CLIENT=response.getInt("counter_client");
                            COUNTER_SYNCED_PAYMENT=response.getInt("counter_payment");
                            COUNTER_SYNCED_VISIT=response.getInt("counter_visit");
                            Log.d("belloy","works");
                            if (res.equals("OK")) {
                                //update sqlite db
                                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        GymDatabase.getInstance(context).clientDao().bulkUpdateClientSyncStatus();
                                        GymDatabase.getInstance(context).paymentDao().bulkUpdatePaymentSyncStatus();
                                        GymDatabase.getInstance(context).visitDao().bulkUpdateVisitSyncStatus();
                                    }
                                });
                                String notificationText=""+COUNTER_SYNCED_CLIENT+" "+mContext.getString(R.string.clients)+ " \n"+
                                        COUNTER_SYNCED_PAYMENT+" "+mContext.getString(R.string.payments)+ " \n"+
                                        COUNTER_SYNCED_VISIT+" "+mContext.getString(R.string.visits)+ " \n"+
                                        mContext.getString(R.string.were_synced);
                                showNotification(context,context.getString(R.string.gymlog_backup_successful),notificationText);
                            }else{
                                showNotification(context,context.getString(R.string.gymlog_backup_failed),context.getString(R.string.failed_no_connection));
                            }
                            //notification
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d("belloy","malformed JSON response");
                            showNotification(context,context.getString(R.string.gymlog_backup_failed),context.getString(R.string.failed_malformed_json));
                        }


                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Log.d("belloy"," error");
                        showNotification(context,context.getString(R.string.gymlog_backup_failed),context.getString(R.string.failed_response_w_errors));

                    }
                });
        MySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }


    //convert null to ""
    private String null2String(String textField){
        if (textField==null){
            textField="";
        }
        return textField;
    }

    private void showPositiveDialog(){
        final AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
        alertDialog.setTitle(mContext.getString(R.string.backup_finished));
        alertDialog.setMessage(""+COUNTER_SYNCED_CLIENT+"/"+SYNC_CLIENT_VOLUME+" "+mContext.getString(R.string.clients)+ " \n"+
                COUNTER_SYNCED_PAYMENT+"/"+SYNC_PAYMENT_VOLUME+" "+mContext.getString(R.string.payments)+ " \n"+
                COUNTER_SYNCED_VISIT+"/"+SYNC_VISIT_VOLUME+" "+mContext.getString(R.string.visits)+ " \n"+
                mContext.getString(R.string.were_synced));

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, mContext.getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    public void showNegativeDialog(){
        final AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
        alertDialog.setTitle(mContext.getString(R.string.backup_failed));
        alertDialog.setMessage(mContext.getString(R.string.backup_failed_explained));

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, mContext.getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }


    public JSONObject createAllJson(List<ClientEntry> clients, List<PaymentEntry> payments, List<VisitEntry>  visits){

        JSONObject backupJson=new JSONObject();

        JSONArray clientDataArray=new JSONArray();
        for (int i=0;i<clients.size();i++){
            ClientEntry client=clients.get(i);
            JSONObject clientObject=new JSONObject();
            try {
                //add client data to each object
                clientObject.put("id", client.getId());
                clientObject.put("firstname", null2String(client.getFirstName()));
                clientObject.put("lastname", null2String(client.getLastName()));
                clientObject.put("dob", null2String(DateConverter.getDateString(client.getDob())));
                clientObject.put("gender", null2String(client.getGender()));
                clientObject.put("occupation", null2String(client.getOccupation()));
                clientObject.put("phone", null2String(client.getPhone()));
                clientObject.put("photo", null2String(client.getPhoto()));
                clientObject.put("qrcode", null2String(client.getQrCode()));
                clientObject.put("lastupdated", null2String(DateConverter.getDateString(client.getLastUpdated())));
                clientDataArray.put(clientObject);
            }catch (JSONException e){
                e.printStackTrace();
            }
        }

        JSONArray paymentDataArray=new JSONArray();
        for (int i=0;i<payments.size();i++){
            PaymentEntry payment=payments.get(i);
            JSONObject paymentObject=new JSONObject();
            try {
                //add client data to each object
                paymentObject.put("id", payment.getId());
                paymentObject.put("clientid", payment.getClientId());
                paymentObject.put("product", null2String(payment.getProduct()));
                paymentObject.put("amountusd", payment.getAmountUsd());
                paymentObject.put("paidfrom", null2String(DateConverter.getDateString(payment.getPaidFrom())));
                paymentObject.put("paiduntil", null2String(DateConverter.getDateString(payment.getPaidUntil())));
                paymentObject.put("timestamp", null2String(DateConverter.getDateString(payment.getTimestamp())));
                paymentObject.put("isvalid", payment.getIsValid());
                paymentObject.put("exchangerate", payment.getExchangeRate());
                paymentObject.put("currency", payment.getCurrency());
                paymentObject.put("comment", payment.getComment());
                paymentObject.put("extra", payment.getExtra());
                paymentDataArray.put(paymentObject);
            }catch (JSONException e){
                e.printStackTrace();
            }
        }

        JSONArray visitDataArray=new JSONArray();
        for (int i=0;i<visits.size();i++){
            VisitEntry visit=visits.get(i);
            JSONObject visitObject=new JSONObject();
            try {
                //add client data to each object
                visitObject.put("id", visit.getId());
                visitObject.put("clientid", visit.getClientId());
                visitObject.put("timestamp", null2String(DateConverter.getDateString(visit.getTimestamp())));
                visitObject.put("access", null2String(visit.getAccess()));
                visitDataArray.put(visitObject);
            }catch (JSONException e){
                e.printStackTrace();
            }
        }

        try {
            String gymName=sharedPreferences.getString("gymname","MyGym");
            String gymOwner=sharedPreferences.getString("gymowner","Fulano de Tal");
            String pin=sharedPreferences.getString("changeownerpin","1234");

            backupJson.put("gym_id", MainActivity.GYM_ID);
            backupJson.put("backup_date", DateConverter.getDateString(new Date()));
            backupJson.put("user", MainActivity.USER_NAME);
            backupJson.put("pin", pin);
            backupJson.put("gym_name", gymName);
            backupJson.put("gym_owner", gymOwner);

            backupJson.put("clientData", clientDataArray);
            backupJson.put("paymentData", paymentDataArray);
            backupJson.put("visitData", visitDataArray);
        }catch (JSONException e){
            e.printStackTrace();
        }

        return backupJson;
    }

    public static void showNotification(Context context,String title, String text) {

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context,MainActivity.CHANNEL_ID)
                        .setSmallIcon(R.drawable.logo_small)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(text));

        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        mBuilder.setAutoCancel(true);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());
    }

    public void restoreAll(){
        JSONObject restoreJson=new JSONObject();
        try {
            restoreJson.put("gym_id", MainActivity.GYM_ID);
        }catch (JSONException e){
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest;

        jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, SERVER_URL + "restore_all.php", restoreJson, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String res = response.getString("response");
                            if (res.equals("OK")) {
                                GymDatabase mDb=GymDatabase.getInstance(mContext);
                                JSONArray jsonClientArray=response.getJSONArray("client");
                                JSONArray jsonPaymentArray=response.getJSONArray("payment");
                                JSONArray jsonVisitArray=response.getJSONArray("visit");
                                int totalClient=jsonClientArray.length();
                                int totalPayment=jsonPaymentArray.length();
                                int totalVisit=jsonVisitArray.length();
                                int countClient=replaceOrInsertClient(jsonClientArray,mDb);
                                int countPayment=replaceOrInsertPayment(jsonPaymentArray,mDb);
                                int countVisit=replaceOrInsertVisit(jsonVisitArray,mDb);

                                String countMessage=""+countClient+"/"+totalClient+" "+ DataBackup.this.mContext.getString(R.string.clients)+ " \n"+
                                        countPayment+"/"+totalPayment+" "+ DataBackup.this.mContext.getString(R.string.payments)+ " \n"+
                                        countVisit+"/"+totalVisit+" "+ DataBackup.this.mContext.getString(R.string.visits)+ " \n"+
                                        mContext.getString(R.string.records_imported);
                                showRestoreDialog(mContext.getString(R.string.data_restore_finished),countMessage);

                            }else{
                                showRestoreDialog(mContext.getString(R.string.data_restore_failed),mContext.getString(R.string.failed_no_connection));
                            }
                            //notification
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d("belloy","malformed JSON response");
                            showRestoreDialog(mContext.getString(R.string.data_restore_failed),mContext.getString(R.string.failed_malformed_json));
                        }


                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Log.d("belloy"," error");
                        showRestoreDialog(mContext.getString(R.string.data_restore_failed),mContext.getString(R.string.failed_response_w_errors));

                    }
                });
        MySingleton.getInstance(mContext).addToRequestQueue(jsonObjectRequest);
    }

    private void showRestoreDialog(String title, String text){
        final AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(text);

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, mContext.getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private int replaceOrInsertClient(JSONArray jsonArray, final GymDatabase mDb){
        int count=0;
        for(int i=0;i<jsonArray.length();i++){
            try {
                JSONObject json = jsonArray.getJSONObject(i);
                int id = json.getInt("id");
                String firstName = json.getString("firstName");
                String lastName = json.getString("lastName");
                String dob = json.getString("dob");
                String gender = json.getString("gender");
                String occupation = json.getString("occupation");
                occupation = !occupation.contentEquals("null") ? occupation : null;
                String phone = json.getString("phone");
                phone = !phone.contentEquals("null") ? phone : null;
                String photo = json.getString("photo");
                photo = !photo.contentEquals("null") ? photo : null;
                String qrCode = json.getString("qrCode");
                qrCode = !qrCode.contentEquals("null") ? qrCode : null;
                String lastUpdated = json.getString("lastUpdated");
                try {
                    final ClientEntry clientEntry = new ClientEntry(id, firstName, lastName, DateConverter.String2Date(dob),
                            gender, occupation, phone, photo, qrCode, DateConverter.String2Date(lastUpdated), 1);
                    AppExecutors.getInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            mDb.clientDao().restoreClient(clientEntry);
                        }
                    });
                    count++;
                }catch (Exception e){
                    Log.d("gymlog_failed_restore","inserting client "+id+" failed. please investigate");
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return count;
    }

    private int replaceOrInsertPayment(JSONArray jsonArray, final GymDatabase mDb){
        int count=0;
        for(int i=0;i<jsonArray.length();i++){
            try {
                JSONObject json = jsonArray.getJSONObject(i);
                int id = json.getInt("id");
                int clientId = json.getInt("clientId");
                String product = json.getString("product");
                Double amountUsd = json.getDouble("amountUsd");
                Double exchangeRate = json.getDouble("exchangeRate");
                String currency = json.getString("currency");
                String comment = json.getString("comment");
                String extra = json.getString("extra");
                String paidFrom = json.getString("paidFrom");
                String paidUntil = json.getString("paidUntil");
                String timestamp = json.getString("timestamp");
                int isValid = json.getInt("isValid");
                try{
                final PaymentEntry paymentEntry=new PaymentEntry(id,clientId,product,amountUsd.floatValue(),DateConverter.String2Date(paidFrom),
                        DateConverter.String2Date(paidUntil), DateConverter.String2Date(timestamp),isValid,1,
                        exchangeRate.floatValue(),currency,comment,extra);
                    AppExecutors.getInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            mDb.paymentDao().restorePayment(paymentEntry);
                        }
                    });
                    count++;
                }catch (Exception e){
                    Log.d("gymlog_failed_restore","inserting payment "+id+" failed. please investigate");
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return count;
    }

    private int replaceOrInsertVisit(JSONArray jsonArray, final GymDatabase mDb){
        int count=0;
        for(int i=0;i<jsonArray.length();i++){
            try {
                JSONObject json = jsonArray.getJSONObject(i);
                int id = json.getInt("id");
                int clientId = json.getInt("clientId");
                String timestamp = json.getString("timestamp");
                String access = json.getString("access");
                try{
                    final VisitEntry visitEntry=new VisitEntry(id,clientId,DateConverter.String2Date(timestamp),access,1);
                    AppExecutors.getInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            mDb.visitDao().restoreVisit(visitEntry);
                        }
                    });
                    count++;
                }catch (Exception e){
                    Log.d("gymlog_failed_restore","inserting visit "+id+" failed. please investigate");
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return count;
    }

}
