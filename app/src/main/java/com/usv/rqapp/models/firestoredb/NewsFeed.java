package com.usv.rqapp.models.firestoredb;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

public class NewsFeed {


    //Fields
    private Map<String, Object> newsFeed = new HashMap<>();
    public String id_postare;
    private String titlu_eveniment;
    private String descriere;
    private String utilizator;
    private Timestamp moment_postare;

    private Timestamp ultima_editare;
    private Integer aprecieri;
    private String loc_eveniment;
    private Boolean isNewPost;

    //Query limit
    public static final long QUERY_LIMIT = 16;

    //Fields Keys
    public final static String POSTARI = "postari";
    public static final String APRECIERI_UTILIZATOR = "aprecieri_utilizator";
    //Keys
    public final static String APRECIATION_VAL = "val";
    public final static String ID_POSTARE = "id_postare";
    public final static String TITLUL_EVENIMENTULUI = "titlu_eveniment";
    public final static String DESCRIERE = "descriere";
    public final static String UTILIZATOR = "utilizator";
    public final static String MOMENT_POSTARE = "moment_postare";
    public final static String ULTIMA_EDITARE = "ultima_editare";
    public final static String APRECIERI = "aprecieri";
    public final static String LOC_EVENIMENT = "loc_eveniment";


    /**
     *
     */
    public NewsFeed() {
    }

    /**
     * @param titlu_eveniment
     * @param descriere
     * @param utilizator
     * @param moment_postare
     * @param ultima_editare
     * @param aprecieri
     * @param loc_eveniment
     * @param isNewPost
     */
    public NewsFeed(String titlu_eveniment, String descriere, String utilizator, Timestamp moment_postare, Timestamp ultima_editare, Integer aprecieri, String loc_eveniment, Boolean isNewPost) {
        this.titlu_eveniment = titlu_eveniment;
        this.descriere = descriere;
        this.utilizator = utilizator;
        this.moment_postare = moment_postare;
        this.ultima_editare = ultima_editare;
        this.aprecieri = aprecieri;
        this.loc_eveniment = loc_eveniment;
        this.isNewPost = isNewPost;
        this.id_postare = String.valueOf(getUtilizator() + getMoment_postare().toDate());
    }

    /**
     * @param newsFeed
     * @return
     */
    public Map<String, Object> convertUsereToMap(NewsFeed newsFeed) {
        Map<String, Object> map = new HashMap<>();

        if (newsFeed != null) {
            map.put(ID_POSTARE, newsFeed.getId_postare());
            map.put(TITLUL_EVENIMENTULUI, newsFeed.getTitlu_eveniment());
            map.put(DESCRIERE, newsFeed.getDescriere());
            map.put(UTILIZATOR, newsFeed.getUtilizator());
            if (newsFeed.getNewPost()) {
                map.put(MOMENT_POSTARE, newsFeed.getMoment_postare());
            }
            map.put(ULTIMA_EDITARE, newsFeed.getUltima_editare());
            map.put(APRECIERI, newsFeed.getAprecieri());
            map.put(LOC_EVENIMENT, newsFeed.getLoc_eveniment());
        }
        return map;
    }


    public Map<String, Object> getNewsFeed() {
        return newsFeed;
    }

    public void setNewsFeed(Map<String, Object> newsFeed) {
        this.newsFeed = newsFeed;
    }

    public String getId_postare() {
        return id_postare;
    }

    public void setId_postare(String id_postare) {
        this.id_postare = id_postare;
    }

    public String getTitlu_eveniment() {
        return titlu_eveniment;
    }

    public void setTitlu_eveniment(String titlu_eveniment) {
        this.titlu_eveniment = titlu_eveniment;
    }

    public String getDescriere() {
        return descriere;
    }

    public void setDescriere(String descriere) {
        this.descriere = descriere;
    }

    public String getUtilizator() {
        return utilizator;
    }

    public void setUtilizator(String utilizator) {
        this.utilizator = utilizator;
    }

    public Timestamp getMoment_postare() {
        return moment_postare;
    }

    public void setMoment_postare(Timestamp moment_postare) {
        this.moment_postare = moment_postare;
    }

    public Timestamp getUltima_editare() {
        return ultima_editare;
    }

    public void setUltima_editare(Timestamp ultima_editare) {
        this.ultima_editare = ultima_editare;
    }

    public Integer getAprecieri() {
        return aprecieri;
    }

    public void setAprecieri(Integer aprecieri) {
        this.aprecieri = aprecieri;
    }

    public String getLoc_eveniment() {
        return loc_eveniment;
    }

    public void setLoc_eveniment(String loc_eveniment) {
        this.loc_eveniment = loc_eveniment;
    }

    public Boolean getNewPost() {
        return isNewPost;
    }

    public void setNewPost(Boolean newPost) {
        isNewPost = newPost;
    }
}
