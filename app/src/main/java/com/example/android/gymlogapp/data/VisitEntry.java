package com.example.android.gymlogapp.data;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "visit", foreignKeys = @ForeignKey(
        entity = ClientEntry.class,
        parentColumns = "id",
        childColumns ="clientId" ),
        indices = {@Index(value = {"clientId"})})
public class VisitEntry {


    @PrimaryKey(autoGenerate = true)
    private int id;
    private int clientId;
    private Date timestamp;
    private String access;
    private int syncStatus=0;

    public VisitEntry(int clientId, Date timestamp, String access) {
        this.clientId = clientId;
        this.timestamp = timestamp;
        this.access = access;
    }

    @Ignore
    public VisitEntry(int id, int clientId, Date timestamp, String access, int syncStatus) {
        this.id = id;
        this.clientId = clientId;
        this.timestamp = timestamp;
        this.access = access;
        this.syncStatus = syncStatus;
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

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getAccess() {
        return access;
    }

    public void setAccess(String access) {
        this.access = access;
    }

    public int getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(int syncStatus) {
        this.syncStatus = syncStatus;
    }
}
