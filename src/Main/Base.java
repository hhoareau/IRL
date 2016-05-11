package Main;

import java.util.List;

/**
 * Created by u016272 on 27/03/2016.
 */
public class Base extends Object {

    Integer nPlayer=0;
    Integer score=0;

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Base(String team,Position c,List<Position> polygon,String color) {
        super("Base "+team,c,polygon,color);
        this.mapSymbole=0;
    }

    public Base(String team,OSMData osm,String color) {
        super("Base "+team,osm,color);
        this.mapSymbole=0;
    }


    @Override
    public String toString() {
        return this.name+"("+this.color+"):"+String.valueOf(nPlayer)+" players";
    }

    @Override
    public Boolean update(Game g) {
        return false;
    }

    public Base() {
        super();
    }

    public void inc(){
        this.nPlayer++;
    }

    public int getnPlayer() {
        return nPlayer;
    }

    public void setnPlayer(int nPlayer) {
        this.nPlayer = nPlayer;
    }
}
