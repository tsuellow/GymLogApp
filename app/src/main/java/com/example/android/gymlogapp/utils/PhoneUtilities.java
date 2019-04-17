package com.example.android.gymlogapp.utils;

public class PhoneUtilities {

    //delivers all phone numbers with country code and leading zeros
    public static String depuratePhone(String rawPhone){
        String phone=rawPhone.replace(" ","").replace("+","00").replace("-","");
        String depPhone=null;
        if (phone.length()>2){
            if (phone.substring(0,2).contentEquals("00")){
                depPhone=phone;
            }else{
                depPhone="00505"+phone;
            }
        }
        return depPhone;

    }

}
