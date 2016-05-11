package Main;

import java.io.Serializable;

/**
 * Created by u016272 on 29/03/2016.
 */
public class Speed implements Serializable {
    Double v_lat=0.0;
    Double v_lng=0.0;

    Speed() {}

    public Speed(Double v_lat, Double v_lng) {
            this.v_lat = v_lat;
            this.v_lng = v_lng;
    }

    public Speed(Position origine,Position cible,Double vitesse){
        Double distance=origine.distance(cible);
        if(distance==0){
            this.v_lng=0.0;
            this.v_lat=0.0;
        } else {
            Long step=(long) (distance/(vitesse*5));
            this.v_lat=(cible.lat-origine.lat)/step;
            this.v_lng=(cible.lng-origine.lng)/step;
        }
    }

    public Double getV_lat() {
        return v_lat;
    }

    public void setV_lat(Double v_lat) {
        this.v_lat = v_lat;
    }

    public Double getV_lng() {
        return v_lng;
    }

    public void setV_lng(Double v_lng) {
        this.v_lng = v_lng;
    }


    @Override
    public int hashCode() {
        int result = v_lat != null ? v_lat.hashCode() : 0;
        result = 31 * result + (v_lng != null ? v_lng.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "(lat:" + v_lat +",lng:" + v_lng+")";
    }

    public Double norme() {
        Double n=this.v_lat*this.v_lat+this.v_lng*this.v_lng;
        return n;
    }
}
