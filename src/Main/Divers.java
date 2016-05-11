package Main;

import java.io.Serializable;

/**
 * Created by u016272 on 27/03/2016.
 */
public class Divers extends Object implements Serializable {

    public Divers(String name, Position p) {
        super(name,p);
        this.mapSymbole=3;
    }

    public Divers() {
        super();
        this.mapSymbole=3;
    }

    @Override
    public String toString() {
        String rc="";
        return name+rc+":"+this.getPosition();
    }

    @Override
    public Boolean update(Game g) {
        return false;
    }

}
