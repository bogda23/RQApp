package com.usv.rqapp.models.firestoredb;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.Map;

public class FavoriteLocation {

    private Map<String, Object> favoriteMap = new HashMap<>();

    //Fields
    private String id_locatie;
    private String titlul_locatiei;
    private Timestamp moment_postare;
    private GeoPoint coords;


    //FIELD KEYS
    public final static String LOC_FAVORITE = "loc_favorite";
    public final static String LOCATII = "locatii";
    public final static int QUERY_LIMIT = 16;

    //Keys
    public final static String ID_LOCATIE = "id_locatie";
    public final static String TITLUL_LOCATIEI = "titlul_locatiei";
    public final static String MOMENT_POSTARE = "moment_postare";
    public final static String COORDS = "coords";


    /**Constructors**/

    /**
     *
     */
    public FavoriteLocation() {
    }

    /**
     * @param titlul_locatiei
     * @param moment_postare
     * @param coords
     */
    public FavoriteLocation(String titlul_locatiei, Timestamp moment_postare, GeoPoint coords) {
        this.titlul_locatiei = titlul_locatiei;
        this.moment_postare = moment_postare;
        this.coords = coords;
        this.id_locatie = String.valueOf(coords.getLatitude()+","+coords.getLongitude()+ moment_postare.toDate().getTime());
    }


    /**
     * Map Convertor
     */
    public Map<String, Object> convertFavotiteLocationToMap(FavoriteLocation favLocation) {
        Map<String, Object> map = new HashMap<>();

        if (favLocation != null) {
             map.put(ID_LOCATIE,favLocation.getId_locatie());
            map.put(TITLUL_LOCATIEI, favLocation.getTitlul_locatiei());
            map.put(MOMENT_POSTARE, favLocation.getMoment_postare());
            map.put(COORDS, favLocation.getCoords());

        }
        return map;
    }


    /**
     * Getters & Setters
     **/

    public Map<String, Object> getFavoriteMap() {
        return favoriteMap;
    }

    public void setFavoriteMap(Map<String, Object> favoriteMap) {
        this.favoriteMap = favoriteMap;
    }

    public String getId_locatie() {
        return id_locatie;
    }

    public void setId_locatie(String id_locatie) {
        this.id_locatie = id_locatie;
    }

    public String getTitlul_locatiei() {
        return titlul_locatiei;
    }

    public void setTitlul_locatiei(String titlul_locatiei) {
        this.titlul_locatiei = titlul_locatiei;
    }

    public Timestamp getMoment_postare() {
        return moment_postare;
    }

    public void setMoment_postare(Timestamp moment_postare) {
        this.moment_postare = moment_postare;
    }

    public GeoPoint getCoords() {
        return coords;
    }

    public void setCoords(GeoPoint coords) {
        this.coords = coords;
    }
}
