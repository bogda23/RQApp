package com.usv.rqapp;

import android.content.res.Resources;

import com.google.android.gms.common.util.Strings;

public final class CONSTANTS {


    //LastName  First Name
    public static final CharSequence INVALID_LASTNAME = "Introduceți numele";
    public static final CharSequence INVALID_FIRSTNAME = "Introduceți prenumele";
    public static final int MINIMUM_LENGTH_FOR_NAME = 3;

    //Login email
    public static final CharSequence UNVERIFIED_EMAIL = "Confirmați adresa de email mai întâi";
    public static final String INVALIDE_EMAIL = "Email invalid";
    public static final String INCORECT_EMAIL = "Email incorect";
    public static final String EMAIL_ALREADY_EXISTS = "Aveti deja cont cu acest email";
    public static final String YOU_DONT_HAVE_ACCOUNT = "Nu aveți cont! Creați unul mai întai";

    //Login pass
    public static final String MIN_SIX_CHARS_PASSWORD = "minim 6 caractere";
    public static final String INVALIDE_PASSWORD = "Parolă invalidă";
    public static final CharSequence TO_MANY_REQUESTS = "Prea multe încercări eșuate";
    public final static String SHOW = "SHOW";
    public final static String HIDE = "HIDE";

    //Ads
    public static final String BANNER_ID = "ca-app-pub-3550303973503881/1149433866";
    public static final String BANNER_ID_SAMPLE = "ca-app-pub-3940256099942544/6300978111";
    public static final String APPLICATION_ID = "ca-app-pub-3550303973503881~3123972467";
    public static final String HUAWEI_ID = "E57FF2B8557ED59E682F29B0AE32A805";

    //Register with Google
    public static final String RC_SIGN_IN = "977131281111-5fufu7a8a12insv8t4rlhmk0d63cehsu.apps.googleusercontent.com";

    //Network status
    public static final String WIFI_ACTIVATED = "Wifi activat";
    public static final String DATA_ACTIVATED = "Date mobile activate";
    public static final String NO_INTERNET = "Nu ești conectat la internet!";

    //reCaptcha
    public static final String RECAPTCHA_SITE_KEY = "6Lf29eQUAAAAAM83w-hHl8B9sN4RFyP4ca6DX7Fm";

    public static final String EMAIL_SENT_SUCCESS = "Verifică email-ul";
    public static final String EMAIL_SENT_ERROR = "Email-ul nu a fost trimis";


    //Permissions
    public static final String PERMISSION_DENIED = "Permisiune respinsă";
    public static final String PERMISSION_DENIED_MESSAGE = "Permisiunea pentru accesul la locația este respinsă permanent. Trebuie să mergi în setări pentru a schimba permisiunea";

    //Account
    public static final String DELETE_ACCOUNT = "Ești sigur?";
    public static final String DELETE_ACCOUNT_MESSAGE = "Ștergerea contului va avea ca rezultat eliminarea datelor contului tău definitiv și ireversibil. ";
    public static final String ACCOUNT_DELETED = "Contul a fost șters";

    public static final String CHANGE_PASSWORD = "Schimbarea parolei";
    public static final String CHANGE_PASSWORD_MESSAGE = "Pentru a-ți schimba parola vei fi scos din cont! Dorești să continui?";

    //MapBox
    public final static String MAPBOX_ACCESS_TOKEN = "sk.eyJ1IjoiYm9nZGEyMyIsImEiOiJja2JzZm4xZWgwMTRyMnRwZzJmYmdrMnE0In0.SSVBus-BkkMCRURbw6Stvg";


    //News Feed event
    public static final String INVALID_EVENT_TITLE = "Introduceți un titlu";
    public static final String INVALID_EVENT_DESCRIPTION ="Introduceți o descriere" ;
    public static final String INEXISTENT_LOCATION = "Locație inexistentă/invalidă";
    public static final String ADDING_FEED_EVENT = "Se adaugă evenimentul..";
    public static final String CAN_T_SAVE_DATA = "Datele nu au putut fi salvate";
}
