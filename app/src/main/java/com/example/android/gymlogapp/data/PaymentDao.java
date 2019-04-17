package com.example.android.gymlogapp.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.Date;
import java.util.List;

@Dao
public interface PaymentDao {

    @Query("SELECT * FROM payment WHERE clientId=:clientId AND " +
            "paidUntil=(SELECT MAX(paidUntil) FROM payment WHERE clientId=:clientId AND isValid=1)")
    LiveData<PaymentEntry> getLastPaymentByClient(int clientId);

    @Query("SELECT * FROM payment WHERE clientId=:clientId AND substr(paidFrom,1,10)<=substr(:date,1,10) AND substr(paidUntil,1,10)>=substr(:date,1,10) " +
            "AND ((SUBSTR(paidFrom,12,5)<=SUBSTR(:date,12,5) AND SUBSTR(paidUntil,12,5)>=SUBSTR(:date,12,5)) OR SUBSTR(paidUntil,12,5)='00:00')"+
            "AND isValid=1 ORDER BY paidUntil DESC LIMIT 1")
    LiveData<PaymentEntry> getCurrentPaymentByClient(int clientId, Date date);

    @Query("SELECT * FROM payment WHERE clientId=:clientId AND isValid=1 ORDER BY paidUntil DESC")
    LiveData<List<PaymentEntry>> getPaymentsByClient(int clientId);

    @Query("SELECT * FROM payment ORDER BY paidUntil DESC")
    List<PaymentEntry> getAllPayments();

    @Query("SELECT * FROM payment WHERE syncStatus!=1")
    List<PaymentEntry> getPaymentToBeSynced();

    @Query("UPDATE payment SET syncStatus=1 WHERE syncStatus!=1")
    void bulkUpdatePaymentSyncStatus();

    @Query("UPDATE payment SET syncStatus=0 WHERE syncStatus!=0")
    void backupResetPaymentSyncStatus();

    @Delete
    void deletePayment(PaymentEntry paymentEntry);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void restorePayment(PaymentEntry paymentEntry);

    @Insert
    void insertPayment(PaymentEntry paymentEntry);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updatePayment(PaymentEntry paymentEntry);


}
