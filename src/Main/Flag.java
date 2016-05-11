package Main;

import java.io.Serializable;
import java.util.List;

/**
 * Created by u016272 on 27/03/2016.
 */
public class Flag extends Object implements Serializable {

    public Flag(String name,Position center,List<Position> polygon) {
        super(name,center,polygon,null);
        this.mapSymbole=2;
    }

    public Flag(String name,OSMData osm) {
        super(name,osm,null);
        this.mapSymbole=2;
    }

    public Flag() {
        super();
        this.mapSymbole=2;
    }

    @Override
    public String toString() {
        String rc="";
        if(this.owner!=null)rc="("+this.owner+")";
        return name+rc+":"+this.getPosition();
    }

    @Override
    public Boolean update(Game g) {
        return false;
    }

}
