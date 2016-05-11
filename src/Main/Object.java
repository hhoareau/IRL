package Main;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by u016272 on 27/03/2016.
 */
public abstract class Object implements Serializable {

    public static Logger log = Logger.getLogger(String.valueOf(Object.class));
    String id=null;

    String name=null;
    Position position=new Position();
    protected String owner="";
    Integer mapSymbole=2;
    Speed speed=new Speed(0.0,0.0);
    String label="";
    String color="green";

    Object(){}

    public Object(String name, String id, Position position) {
        this.name = name;
        this.position = position;
        this.label=this.name.substring(0,0);
        this.id=String.valueOf(System.currentTimeMillis())+name;
    }

    public Object(String name, Position position) {
        this.name = name;
        this.position = position;
        this.label=this.name.substring(0,0);
        this.id=String.valueOf(System.currentTimeMillis())+name;
    }


    public Object(String name, Position center,List<Position> polygon,String color) {
        this.name = name;
        this.position = new Position(polygon);
        this.label=this.name.substring(0,0);
        if(color!=null)this.color=color;
        this.id=String.valueOf(System.currentTimeMillis())+name;
    }


    public Object(String name, OSMData osm,String color) {
        this.name = name;
        this.position = new Position(osm);
        this.label=this.name.substring(0,0);
        if(color!=null)this.color=color;
        this.id=String.valueOf(System.currentTimeMillis())+name;
    }


    public abstract String toString();

    public Object(String id, String name) {
        this.name = name;
    }

    public boolean sameTeam(Object o){
        return this.color.equals(o.getColor());
    }


    public double distance(Object o){
        return this.getPosition().distance(o.getPosition());
    }

    public List<Object> getClosed(List<Object> l,Double distance){
        List<Object> rc=new ArrayList<Object>();
        for(Object o:l)
            if(this.distance(o)<distance && o!=this)
                rc.add(o);

        return rc;
    }


    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public Position getPosition() {
        return position;
    }
    public void setPosition(Position position) {
        this.position.setPosition(position);
    }
    public String getOwner() {
        return owner;
    }
    public void setOwner(String owner) {
        if(owner==null)owner="";
        this.owner = owner;
    }
    public Integer getMapSymbole() {
        return mapSymbole;
    }
    public void setMapSymbole(Integer mapSymbole) {
        this.mapSymbole = mapSymbole;
    }
    public Boolean moveTo(double lt, double ln) {
        if(lt!=this.getPosition().getLat() || ln!=this.getPosition().getLng()){
            this.setPosition(new Position(lt,ln));
            return true;
        }
        return false;
    }


    public Double move(OSMData osm) {
        return this.position.move(this.speed,osm);
    }

    public void setSpeed(Speed speed) {
        this.speed=speed;
        log.info("nouvelle vitesse de "+this.name+" : "+speed.toString());
    }

    public Speed getSpeed() {
        return speed;
    }
    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }
    public String getColor() {
        return color;
    }
    public void setColor(String color) {
        this.color = color;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Object)) return false;

        Object object = o;

        if (id != null ? !id.equals(object.id) : object.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public void setSpeed(Position pos, Double vitesse) {
        //this.setSpeed(new Speed((pos.lat-this.getPosition().lat)/step,(pos.lng-this.getPosition().lng)/step));
        this.speed=new Speed(this.getPosition(),pos,vitesse);
    }

    public abstract Boolean update(Game g);

    public List<Object> getNear(Double distance, List<Object> allObjects) {
        List<Object> rc=new ArrayList<Object>();
        for(Object o:allObjects)
            if(o.getPosition().distance(this.getPosition())<distance)
                rc.add(o);
        return rc;
    }

    public void stop() {
        this.setSpeed(new Speed());
    }
}
