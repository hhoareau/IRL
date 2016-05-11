package Main;

import com.googlecode.objectify.annotation.Ignore;

import java.util.*;

import static Main.Rest.randomGenerator;

/**
 * Created by u016272 on 27/03/2016.
 */

public class Position {

    String id=null;
    public static final Double R = 6378137.0;
    Double alt=0.0;
    Double lat=0.0;
    Double lng=0.0;
    Long lastUpdate=null;

    @Ignore
    public Double f=0.0;
    @Ignore
    public Double g=0.0;
    @Ignore
    public Double h=0.0;
    @Ignore
    public String parent=null;
    @Ignore
    public Integer index=0;





    Position(OSMData osm){
        Road r= osm.roads.get(randomGenerator.nextInt(osm.roads.size()));
        Integer i=randomGenerator.nextInt(r.nodes.size() - 1);
        Position A=osm.getPosition(r,i);
        Position B=osm.getPosition(r, i + 1);
        this.setMiddle(Arrays.asList(A,B));
    }


    public Position(String coord) {
        this.lat=Double.valueOf(coord.split(",")[0]);
        this.lng=Double.valueOf(coord.split(",")[1]);
        this.lastUpdate=System.currentTimeMillis();
    }


    //create position somewhere within polygon
   public Position(List<Position> polygon) {
        Random randomGenerator = new Random();
        Double coef=1.0;
        Double dist=0.0;
        Position center=new Position();
        center.setMiddle(polygon);

        Boolean rc=false;
        while(!rc){
            Double delta_lat=(randomGenerator.nextDouble()-.5)/coef;
            Double delta_lng=(randomGenerator.nextDouble()-.5)/coef;
            this.lat=center.lat+delta_lat;
            this.lng=center.lng+delta_lng;
            rc=this.PointInPolygon(this.lat,this.lng,polygon);
            if(dist>R)coef+=200;
        }

       this.lastUpdate=System.currentTimeMillis();
    }


    //Build position from cartesian coordinates
    public Position(Vector V) {
        Double d = Math.sqrt(V.x * V.x + V.y * V.y  + V.z * V.z);
        this.lat= 90-Math.toDegrees(Math.atan(V.y / V.x));
        this.lng= 90-Math.toDegrees(Math.acos(V.z / d));
        this.alt= d-R;
    }

    public Position(Position position) {
        this.setPosition(position);
    }


    @Override
    public String toString() {
        return "("+String.format("%.5g",this.lat)+"|"+String.format("%.5g",this.lng)+")";
    }


    public List<Position> getNear(Double distanceMax,List<Position> lp){
        List<Position> rc=new ArrayList<Position>();
        for(Position p:lp){
            Double d=this.distance(p);
            if (d>0.001 && d<distanceMax)rc.add(p);
        }
        return rc;
    }

    public List<Position> getNearMan(Double distanceMax,List<Position> lp){
        List<Position> rc=new ArrayList<Position>();
        for(Position p:lp){
            Double d=this.distanceMan(p);
            if (d>0.001 && d<distanceMax)rc.add(p);
        }
        return rc;
    }



    //Move the position to the nearest road
    public void onRoad(OSMData osm){
        Double min=1000000.0;
        Position rc=new Position();

        for(Road r:osm.roads) //For each road
            for(int i=0;i<r.nodes.size()-1;i++){ //For each part of road
                Position H=new Position(osm.getPosition(r,i),osm.getPosition(r,i+1),this); //H is a projection on the road
                Double d=H.distance(this);
                if(d<min){
                    min=d;
                    rc=H;
                }
            }
        this.setPosition(rc);
    }


    public static boolean coordinate_is_inside_polygon(double latitude, double longitude,List<Position> polygon) {
        int i;
        double angle=0;
        double point1_lat;
        double point1_long;
        double point2_lat;
        double point2_long;
        int n = polygon.size();

        for (i=0;i<n;i++) {
            point1_lat = polygon.get(i).getLat() - latitude;
            point1_long = polygon.get(i).getLng() - longitude;
            point2_lat = polygon.get((i+1)%n).getLat() - latitude;
            //you should have paid more attention in high school geometry.
            point2_long = polygon.get((i+1)%n).getLng() - longitude;
            angle += Angle2D(point1_lat,point1_long,point2_lat,point2_long);
        }

        if (Math.abs(angle) < Math.PI)
            return false;
        else
            return true;
    }


    public static double Angle2D(double y1, double x1, double y2, double x2)
    {
        double dtheta,theta1,theta2;

        theta1 = Math.atan2(y1,x1);
        theta2 = Math.atan2(y2,x2);
        dtheta = theta2 - theta1;

        while (dtheta > Math.PI)dtheta -= 2*Math.PI;
        while (dtheta < -Math.PI)dtheta += 2*Math.PI;

        return(dtheta);
    }


    Boolean PointInPolygon(Double lat,Double lng, List<Position> points) {
        return coordinate_is_inside_polygon(lat,lng,points);
    }


    public void setMiddle(Collection<Position> l){
        this.lat=0.0;
        this.lng=0.0;
        for(Position p:l){
            this.lat+=p.lat;
            this.lng+=p.lng;
        }
        this.setPosition(new Position(this.lat/l.size(),this.lng/l.size()));
    }


    public void setPosition(Double lat,Double lng,Double R){
        Random randomGenerator = new Random();
        Double coef=100/R;
        Double dist=0.0;
        do{
            Double delta_lat=(randomGenerator.nextDouble()-.5)/coef;
            Double delta_lng=(randomGenerator.nextDouble()-.5)/coef;
            this.lat=lat+delta_lat;
            this.lng=lng+delta_lng;
            dist=this.distance(this.lat,this.lng,lat,lng);
            if(dist>R)coef+=200;
        } while(dist>R);
        this.lastUpdate=System.currentTimeMillis();
    }



    protected double distanceMan(double lat_a, double lon_a, double lat_b, double lon_b){
        return Math.abs(lat_a-lat_b)+Math.abs(lon_a-lon_b);
    }

    protected double distance(double lat_a, double lon_a, double lat_b, double lon_b) {
        if(lat_a==lat_b & lon_a==lon_b)return 0.0;
        double a = Math.PI / 180;
        double lat1 = lat_a * a;
        double lat2 = lat_b * a;
        double lon1 = lon_a * a;
        double lon2 = lon_b * a;

        double t1 = Math.sin(lat1) * Math.sin(lat2);
        double t2 = Math.cos(lat1) * Math.cos(lat2);
        double t3 = Math.cos(lon1 - lon2);
        double t4 = t2 * t3;
        double t5 = t1 + t4;
        double rad_dist = Math.atan(-t5/Math.sqrt(-t5 * t5 +1)) + 2 * Math.atan(1);

        return (rad_dist * 3437.74677 * 1.1508) * 1609.3470878864446;
    }



    public double distance(Position p){
        return this.distance(this.lat,this.lng,p.lat,p.lng);
    }

    public double distanceMan(Position p){
        return this.distanceMan(this.lat, this.lng, p.lat, p.lng);
    }


    public Position(Double lat, Double lng) {
        this.lat = lat;
        this.lng = lng;
        this.lastUpdate=System.currentTimeMillis();
    }

    public Position(Double lat, Double lng,String id) {
        this.lat = lat;
        this.lng = lng;
        this.id = id;
        this.lastUpdate=System.currentTimeMillis();
    }


    //Build position as projection of C on (AB)
    public Position(Position A,Position B,Position C){
        Vector H=new Vector(new Vector(A),new Vector(B),new Vector(C));
        this.setPosition(new Position(H));
    }

    private Position getXY() {
        return new Position(this.getX(),this.getY());
    }


    public Position(Position center,Double R){
        this.setPosition(center.lat,center.lng,R);
    }

    Position(){
        this.setPosition(0.0,0.0,200.0);
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
        this.lastUpdate=System.currentTimeMillis();
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
        this.lastUpdate=System.currentTimeMillis();
    }

    //Set the move and return the real speed
    public Double move(Speed speed,OSMData osm) {
        if(speed.v_lat==0 && speed.v_lng==0)return 0.0;

        Position save=new Position(this.lat,this.lng);

        this.lat+=speed.v_lat;
        this.lng+=speed.v_lng;
        if(osm!=null)this.onRoad(osm);

        this.lastUpdate=System.currentTimeMillis();

        return this.distance(save);
    }

    public Position intersec(Position A,Position B,Position C,Position D){
     return null;
    }

    public void setPosition(Position p){
        this.lat=p.lat;
        this.lng=p.lng;
        this.alt=p.alt;
        this.lastUpdate=System.currentTimeMillis();
    }

    public Double getX(){
        return R * Math.cos(this.lat) * Math.cos(this.lng);
    }

    public Double getY(){
        return R * Math.cos(this.lat) * Math.sin(this.lng);
    }

    public Long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public boolean equals(Position o) {
        if (this == o) return true;
        if (!(o instanceof Position)) return false;

        Position position = (Position) o;

        if (alt != null ? !alt.equals(position.alt) : position.alt != null) return false;
        if (lat != null ? !lat.equals(position.lat) : position.lat != null) return false;
        if (lng != null ? !lng.equals(position.lng) : position.lng != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = alt != null ? alt.hashCode() : 0;
        result = 31 * result + (lat != null ? lat.hashCode() : 0);
        result = 31 * result + (lng != null ? lng.hashCode() : 0);
        return result;
    }

    public Position getNearestFrom(Collection<Position> lp) {
        Double min=10000000.0;
        Position rc=null;
        for(Position p:lp){
            if(this.distance(p)<min){
                rc=p;
                min=this.distance(p);
            }
        }
        return rc;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
