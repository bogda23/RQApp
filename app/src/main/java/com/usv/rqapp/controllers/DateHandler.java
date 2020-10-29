package com.usv.rqapp.controllers;


import com.google.firebase.Timestamp;
import com.usv.rqapp.models.firestoredb.NewsFeed;

import org.joda.time.DateTime;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.Period;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateHandler {

    public static int EXPIRATION_TIME = 32;
    private FirestoreController db = new FirestoreController();

    public static Date getCurrentDate() {
        long millis = System.currentTimeMillis();
        java.sql.Date date = new java.sql.Date(millis);
        return date;
    }

    public static java.sql.Timestamp getCurrentTimestamp() {
        java.sql.Timestamp timestamp = new java.sql.Timestamp(System.currentTimeMillis());
        return timestamp;
    }

    public static Timestamp getCurrentFirestoreTimestamp() {
        Timestamp timestamp = Timestamp.now();
        return timestamp;
    }

    /**
     * @param timestamp
     * @return
     */
    public static String convertTimestampToString(Timestamp timestamp) {
        final DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date data_stop = timestamp.toDate();
        final String stop = formatter.format(data_stop);

        return stop;
    }


    /**
     * @param timestamp
     * @return
     */
    public static String getTimeBetweenNowAndThen(Timestamp timestamp) {
        final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSX");

        Date postDate = null;
        Date currentDate = null;
        int hoursBetween;
        int minutesBetween;

        try {

            postDate = formatter.parse(formatter.format(timestamp.toDate()));
            currentDate = formatter.parse(formatter.format(getCurrentDate()));

            DateTime currentDateTime = new DateTime(postDate);
            DateTime postDateTime = new DateTime(currentDate);

            hoursBetween = (Hours.hoursBetween(currentDateTime, postDateTime).getHours() /*% 24*/);
            minutesBetween = (Minutes.minutesBetween(currentDateTime, postDateTime).getMinutes() % 60);
            if (hoursBetween == 0) {
                return "acum " + minutesBetween + " minute";
            } else if (hoursBetween == 1) {
                return "acum o oră";
            } else if (hoursBetween < 24) {
                return "acum " + hoursBetween + " ore";
            } else if (hoursBetween >= 24 && hoursBetween < EXPIRATION_TIME) {
                return "acum o zi";
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


        return "eroare";
    }

    public static int getTimeBetween(Timestamp timestamp) {
        final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSX");

        Date postDate = null;
        Date currentDate = null;
        int hoursBetween = 0;

        try {

            postDate = formatter.parse(formatter.format(timestamp.toDate()));
            currentDate = formatter.parse(formatter.format(getCurrentDate()));

            DateTime currentDateTime = new DateTime(postDate);
            DateTime postDateTime = new DateTime(currentDate);

            hoursBetween = (Hours.hoursBetween(currentDateTime, postDateTime).getHours() /*% 24*/);

        } catch (Exception e) {
            e.printStackTrace();
        }


        return hoursBetween;
    }


}
