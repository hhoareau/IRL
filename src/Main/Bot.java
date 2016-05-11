package Main;

import com.googlecode.objectify.annotation.Subclass;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by u016272 on 19/04/2016.
 */
@Subclass(index=true)
public class Bot extends User {
    public  static final Integer OBJ_FLAG = 0;
    public static final Integer OBJ_MYBASE = 1;
    public static final Integer OBJ_ENEMYBASE = 2;
    private static final Integer OBJ_KILL = 3;

    List<Position> currentWay=new ArrayList<Position>();
    public Double maxSpeed=2.0;
    Integer objective=OBJ_FLAG;

    public Bot() {
        super(new infoFacebook("myBot"),"https://opt-adopt.appspot.com");
    }

    public Bot(infoFacebook infos, String Domain) {
        super(infos, Domain);
    }

    @Override
    public Boolean update(Game g) {

        if (this.objective != OBJ_MYBASE) {
            if (!this.isALive()) {
                this.setObjective(OBJ_MYBASE, g);
                return true;
            }

            for (Object o : g.getObjects(this)) {
                if (!o.color.equals(this.color)) {
                    this.setObjective(OBJ_MYBASE,g);
                    return true;
                }

                //Si il n'a pas le drapeau des autres
                if (this.objective != OBJ_ENEMYBASE) {
                    this.setObjective(OBJ_ENEMYBASE,g);
                    return true;
                }
            }
        } else {
            if (this.distance(g.getBase(this)) < g.distanceContact) {
                this.setObjective(OBJ_ENEMYBASE,g);
                this.stop();
                return true;
            }
        }

        if(this.currentWay!=null && this.currentWay.size()>0){
            Double dist=this.getPosition().distance(this.currentWay.get(0));
            if(dist<g.distanceContact){
                log.info("waypoint atteind");
                this.currentWay.remove(0);
                this.stop();
            }

            if(this.speed.norme()<1e-10 && this.currentWay.size()>0){
                log.info("Nouveau waypoint en "+this.currentWay.get(0));
                this.setSpeed(this.currentWay.get(0), this.maxSpeed);
            }

        }
        else this.setObjective(OBJ_MYBASE,g);

        return false;
    }

    public void setObjective(Integer objective,Game g) {
        if(objective!=this.objective){
            this.objective = objective;
            log.info("Chagement d'objectif : "+objective);

            if(objective==OBJ_ENEMYBASE)
                this.currentWay=g.map.bestWay(this.getPosition(),g.getEnemyBase(this).getPosition());

            if(objective==OBJ_MYBASE)
                this.currentWay=g.map.bestWay(this.getPosition(),g.getBase(this).getPosition());

            this.stop();
        }
    }
}
