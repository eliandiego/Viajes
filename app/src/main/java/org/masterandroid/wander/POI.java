package org.masterandroid.wander;

import com.google.android.gms.maps.model.PointOfInterest;

/**
 * Created by Ivan on 11/11/2017.
 */

public class POI {
    String title, description,identificador, img, localidad;
    Float lon, lat;
    int categoria;

    public POI() {
        img = null;
        description = null;
    }

    public POI(PointOfInterest poi){
        img = null;
        description = null;
        title = poi.name;
        identificador = poi.placeId;
        lon = (float)poi.latLng.longitude;
        lat = (float)poi.latLng.latitude;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
       // title = title.replace("\'","\'\'");  //para evitar problemas con la base de datos, deberia haberlo puesto en el constructor
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        //description = description.replace("\'","\'\'");
        this.description = description;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
       // img = img.replace("\'","\'\'");
        this.img = img;
    }

    public float getLon() {
        return lon;
    }

    public void setLon(float lon) {
        this.lon = lon;
    }

    public float getLat() {
        return lat;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public String getIdentificador() {
        return identificador;
    }

    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }

    public void setLon(Float lon) {
        this.lon = lon;
    }

    public void setLat(Float lat) {
        this.lat = lat;
    }

    public int getCategoria() {
        return categoria;
    }

    public void setCategoria(int categoria) {
        this.categoria = categoria;
    }

    public String getLocalidad() {
        return localidad;
    }

    public void setLocalidad(String localidad) {
        this.localidad = localidad;
    }
}
