package com.example.android.gymlogapp.data;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "client")
public class ClientEntry {

    @PrimaryKey int id;
    private String firstName;
    private String lastName;
    private Date dob;
    private String gender;
    private String occupation;
    private String phone;
    private String photo;
    private String qrCode;
    private Date lastUpdated;
    private int syncStatus=0;

    public ClientEntry(int id, String firstName, String lastName, Date dob, String gender,
                       String occupation, String phone, String photo, String qrCode,
                       Date lastUpdated) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
        this.gender = gender;
        this.occupation = occupation;
        this.phone = phone;
        this.photo = photo;
        this.qrCode = qrCode;
        this.lastUpdated = lastUpdated;
    }

    @Ignore
    public ClientEntry(int id, String firstName, String lastName, Date dob, String gender, String occupation, String phone, String photo, String qrCode, Date lastUpdated, int syncStatus) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
        this.gender = gender;
        this.occupation = occupation;
        this.phone = phone;
        this.photo = photo;
        this.qrCode = qrCode;
        this.lastUpdated = lastUpdated;
        this.syncStatus = syncStatus;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public int getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(int syncStatus) {
        this.syncStatus = syncStatus;
    }
}
