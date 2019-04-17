package com.example.android.gymlogapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import com.example.android.gymlogapp.data.ClientEntry;
import com.example.android.gymlogapp.data.GymDatabase;
import com.example.android.gymlogapp.data.PaymentEntry;
import com.example.android.gymlogapp.data.VisitEntry;

import org.json.JSONObject;

import java.util.List;

public class BackupBroadcastReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(final Context context, Intent intent) {

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        final GymDatabase mDb = GymDatabase.getInstance(context);
        final DataBackup dataBackup=new DataBackup(context,pref);
        if (dataBackup.hasInternetConnectivity()){
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    if (dataBackup.hasHostAccess()){
                        final List<ClientEntry> clients=mDb.clientDao().getClientToBeSynced();
                        final List<PaymentEntry> payments=mDb.paymentDao().getPaymentToBeSynced();
                        final List<VisitEntry> visits=mDb.visitDao().getVisitToBeSynced();

                        JSONObject jsonObject=dataBackup.createAllJson(clients,payments,visits);
                        dataBackup.syncAllAutomatic(jsonObject,context);
                    }else{
                        DataBackup.showNotification(context,context.getString(R.string.gymlog_backup_failed),context.getString(R.string.no_host_access));
                    }
                }
            });

        }else{
            DataBackup.showNotification(context,context.getString(R.string.gymlog_backup_failed),context.getString(R.string.no_internet_at_planned_time));
        }
    }




}
