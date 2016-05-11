package Main;

import com.googlecode.objectify.annotation.Subclass;

/**
 * Created by u016272 on 15/04/2016.
 */
@Subclass(index=true)
public class CaptureTheFlag extends Game {

    public CaptureTheFlag() {
        super();
    }

    public CaptureTheFlag(User owner, Integer nTeams,OSMData osm){
        super("Capture the flag by "+owner.firstname, owner,  nTeams, "Flag", nTeams,osm);
        while(this.map.bestWay(this.bases.get(0).getPosition(),this.bases.get(1).getPosition()).size()<6){ //it must exist a way between two base
            this.bases.get(0).getPosition().setPosition(new Position(this.map));
            this.bases.get(1).getPosition().setPosition(new Position(this.map));
        }

        for(int i=0;i<nTeams;i++){
            this.flags.get(i).setPosition(this.bases.get(i).getPosition());
            this.flags.get(i).setColor(this.bases.get(i).getColor());
        }
    }

    @Override
    public void flagNearBase(Base b, Flag f) {
        if(!b.color.equals(f.color)){ //si le drapeau est d'une autre couleur que la base
            for(Flag f2:this.flags)
                if(f2.color.equals(b.color))   //si l'autre drapeau est bien à la base
                    if(f2.distance(b)<this.distanceContact){
                        b.score+=this.flagScore;
                        for(Base c:this.bases)
                            if(c.color.equals(f.color)){
                                f.setPosition(c.getPosition());
                                f.setOwner("");
                            }
                    }
        }
    }



    @Override
    public User playerNearFlag(User p, Flag f){

        if(f.getOwner().equals(p.email))return p;

        if(f.getOwner().equals("")){
            if(f.color.equals(p.color))
                f.setPosition(this.getBase(p).getPosition());
            else
                f.setOwner(p.getEmail());
        }

        return p;
    }




}
