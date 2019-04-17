package com.example.android.gymlogapp.data;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;



@Entity(tableName = "payment", foreignKeys = @ForeignKey(entity = ClientEntry.class,
        parentColumns = "id",
        childColumns = "clientId",
        onDelete = ForeignKey.NO_ACTION),
        indices = {@Index(value = {"clientId"})})
public class PaymentEntry {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int clientId;
    private String product;
    private float amountUsd;
    private Date paidFrom;
    private Date paidUntil;
    private Date timestamp;
    private int isValid=1;
    private int syncStatus=0;
    private float exchangeRate;
    private String currency;
    private String comment;
    private String extra;

    public PaymentEntry(int clientId, String product, float amountUsd, Date paidFrom, Date paidUntil, Date timestamp, float exchangeRate, String currency, String comment, String extra) {
        this.clientId = clientId;
        this.product = product;
        this.amountUsd = amountUsd;
        this.paidFrom = paidFrom;
        this.paidUntil = paidUntil;
        this.timestamp = timestamp;
        this.exchangeRate = exchangeRate;
        this.currency = currency;
        this.comment = comment;
        this.extra = extra;
    }

    @Ignore
    public PaymentEntry(int id, int clientId, String product, float amountUsd, Date paidFrom, Date paidUntil, Date timestamp, int isValid, int syncStatus, float exchangeRate, String currency, String comment, String extra) {
        this.id = id;
        this.clientId = clientId;
        this.product = product;
        this.amountUsd = amountUsd;
        this.paidFrom = paidFrom;
        this.paidUntil = paidUntil;
        this.timestamp = timestamp;
        this.isValid = isValid;
        this.syncStatus = syncStatus;
        this.exchangeRate = exchangeRate;
        this.currency = currency;
        this.comment = comment;
        this.extra = extra;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public float getAmountUsd() {
        return amountUsd;
    }

    public void setAmountUsd(float amountUsd) {
        this.amountUsd = amountUsd;
    }

    public Date getPaidFrom() {
        return paidFrom;
    }

    public void setPaidFrom(Date paidFrom) {
        this.paidFrom = paidFrom;
    }

    public Date getPaidUntil() {
        return paidUntil;
    }

    public void setPaidUntil(Date paidUntil) {
        this.paidUntil = paidUntil;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public int getIsValid() {
        return isValid;
    }

    public void setIsValid(int isValid) {
        this.isValid = isValid;
    }

    public int getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(int syncStatus) {
        this.syncStatus = syncStatus;
    }

    public float getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(float exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }
}