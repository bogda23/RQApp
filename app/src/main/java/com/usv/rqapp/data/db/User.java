package com.usv.rqapp.data.db;

import java.sql.Blob;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class User {

    //Fields
    private Map<String, Object> user = new HashMap<>();
    private String id_utilizator;
    private String nume;
    private String prenume;
    private String email;
    private String parola;
    private Date data_nastere;
    private Blob avatar;
    private Date data_inscriere;
    private Timestamp ultima_logare;
    private Boolean vip;

    // Keys
    public final static String UTILIZATORI = "utilizatori";
    public final static String ID_UTILIZATOR = "id_utilizator";
    public final static String NUME = "nume";
    public final static String PRENUME = "prenume";
    public final static String EMAIL = "email";
    public final static String PAROLA = "parola";
    public final static String DATA_NASTERE = "data_nastere"; //Date
    public final static String AVATAR = "avatar"; //Blob
    public final static String DATA_INSCRIERE = "data_inscriere"; //Date
    public final static String ULTIMA_LOGARE = "ultima_logare"; //Timestamp
    public final static String VIP = "vip"; //Boolean


    /**
     * @param user
     * @return
     */
    public Map<String, Object> convertUserToMap(User user) {
        Map<String, Object> map = new HashMap<>();

        if (user != null) {
            map.put(ID_UTILIZATOR, user.getId_utilizator());
            map.put(NUME, user.getNume());
            map.put(PRENUME, user.getPrenume());
            map.put(EMAIL, user.getEmail());
            map.put(DATA_INSCRIERE, user.getData_inscriere());
            map.put(ULTIMA_LOGARE, user.getUltima_logare());
            map.put(VIP, user.getVip());
        }
        return map;
    }

    /**
     * @return
     */
    public Map<String, Object> getUser() {
        return user;
    }

    /**
     * @param user
     */
    public void setUser(Map<String, Object> user) {
        this.user = user;
    }

    /**
     * @param nume
     * @param prenume
     * @param email
     * @param parola
     * @param data_inscriere
     * @param ultima_logare
     * @param vip
     */
    public User(String nume, String prenume, String email, String parola, Date data_inscriere, Timestamp ultima_logare, Boolean vip) {
        this.nume = nume;
        this.prenume = prenume;
        this.email = email;
        this.parola = parola;
        this.data_inscriere = data_inscriere;
        this.ultima_logare = ultima_logare;
        this.vip = vip;
    }

    /**
     * @param email
     * @param parola
     */
    public User(String email, String parola) {
        this.id_utilizator = id_utilizator;
        this.email = email;
        this.parola = parola;
    }

    /**
     * @return
     */
    public String getId_utilizator() {
        return id_utilizator;
    }

    /**
     * @param id_utilizator
     */
    public void setId_utilizator(String id_utilizator) {
        this.id_utilizator = id_utilizator;
    }

    public String getNume() {
        return nume;
    }

    public void setNume(String nume) {
        this.nume = nume;
    }

    public String getPrenume() {
        return prenume;
    }

    public void setPrenume(String prenume) {
        this.prenume = prenume;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getParola() {
        return parola;
    }

    public void setParola(String parola) {
        this.parola = parola;
    }

    public Date getData_nastere() {
        return data_nastere;
    }

    public void setData_nastere(Date data_nastere) {
        this.data_nastere = data_nastere;
    }

    public Blob getAvatar() {
        return avatar;
    }

    public void setAvatar(Blob avatar) {
        this.avatar = avatar;
    }

    public Date getData_inscriere() {
        return data_inscriere;
    }

    public void setData_inscriere(Date data_inscriere) {
        this.data_inscriere = data_inscriere;
    }

    public Timestamp getUltima_logare() {
        return ultima_logare;
    }

    public void setUltima_logare(Timestamp ultima_logare) {
        this.ultima_logare = ultima_logare;
    }

    public Boolean getVip() {
        return vip;
    }

    public void setVip(Boolean vip) {
        this.vip = vip;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user1 = (User) o;
        return Objects.equals(getUser(), user1.getUser()) &&
                Objects.equals(getId_utilizator(), user1.getId_utilizator()) &&
                Objects.equals(getNume(), user1.getNume()) &&
                Objects.equals(getPrenume(), user1.getPrenume()) &&
                Objects.equals(getEmail(), user1.getEmail()) &&
                Objects.equals(getParola(), user1.getParola()) &&
                Objects.equals(getData_nastere(), user1.getData_nastere()) &&
                Objects.equals(getAvatar(), user1.getAvatar()) &&
                Objects.equals(getData_inscriere(), user1.getData_inscriere()) &&
                Objects.equals(getUltima_logare(), user1.getUltima_logare()) &&
                Objects.equals(getVip(), user1.getVip());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUser(), getId_utilizator(), getNume(), getPrenume(), getEmail(), getParola(), getData_nastere(), getAvatar(), getData_inscriere(), getUltima_logare(), getVip());
    }
}
