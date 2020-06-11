package com.usv.rqapp.controller;


import java.sql.Timestamp;
import java.util.Date;

public class DateHandler {

    public static Date getCurrentDate(){
        long millis=System.currentTimeMillis();
        java.sql.Date date=new java.sql.Date(millis);
        return date;
    }
    public static Timestamp getCurrentTimestamp(){
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        return timestamp;
    }
}
