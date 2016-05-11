package Main;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import org.xguzm.pathfinding.grid.GridCell;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by u016272 on 27/03/2016.
 */
@Entity
public class Game implements Serializable {
    static String[] colors= {"Red","Green","Blue","Black","Yellow","Pink"};

    @Id Long id=System.currentTimeMillis();
    public String name="Game"+id;

    //public EventBus eventBus=null;

    Integer duration=10;

    List<String> players=new ArrayList<String>();
    List<Flag> flags=new ArrayList<Flag>();
    List<Base> bases=new ArrayList<Base>();

    Long dtStart=System.currentTimeMillis();

    User owner=null;
    Position center=null;
    Integer nplayers=0;

    int killScore=1;
    int killPoint=10;
    int flagScore=2;
    double distanceContact=5.0;
    boolean isReady=false;
    public int speedToBase=20; //Vitesse de retour à la base des bots
    public int speedToEnemyBase=20;
    public OSMData map=null;

    private void initGame(String name, User owner) {
        this.name=name+" de "+owner.firstname;
        this.center=owner.getPosition();
        this.owner=owner;
    }

    Game() {
        super();
    }


    public Game(String name, User owner, Integer nFlags, String flag, Integer nTeams,OSMData osm) {
        initGame(name, owner);
        this.map=osm;
        this.setFlagList(nFlags,flag);
        this.setTeamList(nTeams,osm);
    }


    public Game(String name, User owner, Integer nTeams,OSMData osm) {
        initGame(name, owner);
        this.map=osm;
        this.setFlagList(nTeams,"flag");
        this.setTeamList(nTeams,osm);
    }



    static public List<Position> createCircle(Position center,Double Rayon,Integer steps){
        List<Position> rc=new ArrayList<Position>();
        for(Double ang=0.0;ang<2*Math.PI;ang+=Math.PI/steps)
            rc.add(new Position(center.lat+Rayon*Math.cos(ang),center.lat+Rayon*Math.cos(ang)));
        return rc;
    }


   protected void setFlagList(Integer nombre,String prefixe,List<Position> polygon){
        while(nombre>0){
            Flag f=new Flag(prefixe+String.valueOf(nombre--),this.center,polygon);
            this.flags.add(f);
        }
    }


    protected void setFlagList(Integer nombre,String prefixe){
        while(nombre>0){
            Flag f=new Flag(prefixe+String.valueOf(nombre--),this.map);
            this.flags.add(f);
        }
    }


    public void moveOnRoad(){
        for(Flag f:this.flags)
            f.position.onRoad(this.map);

        for(Base b:this.bases)
            b.position.onRoad(this.map);
    }


    static public String showCollection(List<? extends Object> l){
        String rc="";
        for(Object o:l)rc+=o.toString()+" ";
        return rc;
    }

    @Override
    public String toString() {
        String rc=this.getName()+":\nPlayers:";
        for(String s:this.players)rc+=s.split("@")[0]+";";
        rc=rc+"\nflags:"+showCollection(this.flags)+"\n";
        rc=rc+"bases:"+showCollection(this.bases)+"\n";
        return rc;
    }

    private User interractions(User u){
        for(Flag f:this.flags) {
            if (f.distance(u) < this.distanceContact)
                u = this.playerNearFlag(u, f);
        }

        for(Base b:this.bases)
            if (b.distance(u) < this.distanceContact)u = this.playerNearBase(u, b);

        return u;
    }

    public void flagNearBase(Base b, Flag f) {
    }

    private User playerNearBase(User u, Base b) {
        //Retour à la vie
        if(u.getLife()==0 && u.getColor().equals(b.getColor())){
            u.setLife(100);
        }
        return u;
    }

    public User find(List<User> lp,String email){
        for(User u:lp)
            if(u.getEmail().equals(email))return u;
        return null;
    }

    public List<User> calcul(List<User> lp){

        //Move all the objects
        for(Object o:this.getAllObjects(lp)){
            o.move(this.map);
            if(o.getOwner().length()>0)
                o.setPosition(find(lp,o.getOwner()).getPosition());
        }

        //Objects Interactions with player
        for(User u:lp){
            u.update(this);
            u.lastMoveDistance=u.move(this.map);
            this.interractions(u);
        }

        for(Base b:this.bases)
                for(Flag f:this.flags)
                        if(b.distance(f)<this.distanceContact)
                                this.flagNearBase(b, f);

        return lp;
    }


    public User[] playerKillPlayer(User killer, User killed){
        if(killer.isALive() && killed.isALive() && killer.distance(killed)<this.distanceContact){
            killer.setScore(killer.getScore()+this.killScore);
            killed.setLife(killed.getLife() - this.killPoint);
            for(Object o:this.getObjects(killed))
                o.setOwner("");
        }
        return new User[] {killer,killed};
    }


    public User playerNearFlag(User p, Flag f){
        //Version pacman
        if(p.isALive()){
            p.score+=this.flagScore;
            f.setPosition(new Position(this.center, 300.0));
        }
        return p;
    }

    public boolean playerNearPlayer(User p1,User p2){return false;}


    public User add(User u) {
        int index=getBestBase();

        if(!this.players.contains(u.getEmail())){
            //u.setPosition(this.bases.get(index).getPosition());
            u.setLife(0); //Mort au début, le joueur doit rejoindre la base
            this.bases.get(index).inc();
            this.players.add(u.getEmail());

            this.nplayers=this.players.size();

            u.setColor(this.bases.get(index).color);
            if(u.getClass().getSimpleName().equals("Bot"))
                u.setPosition(this.getBase(u).getPosition());
        }
        return u;
    }


    private int getBaseIndex(String color) {
        for(int i=0;i<this.bases.size();i++)
            if(this.bases.get(i).getColor().equals(color))return i;

        return -1;
    }


    public List<Object> getAllObjects(List<User> lu){
        List<Object> objs=new ArrayList<Object>();
        if(lu!=null)objs.addAll(lu);
        objs.addAll(this.flags);
        objs.addAll(this.bases);
        return objs;
    }

    public void initPathFinder(){
        GridCell[][] cells = new GridCell[5][5];
    }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Flag> getFlags() {
        return flags;
    }

    public void setFlags(List<Flag> flags) {
        this.flags = flags;
    }

    public Long getDtStart() {
        return dtStart;
    }

    public void setDtStart(Long dtStart) {
        this.dtStart = dtStart;
    }

    public int getKillPoint() {
        return killPoint;
    }

    public void setKillPoint(int killPoint) {
        this.killPoint = killPoint;
    }

    public int getSpeedToBase() {
        return speedToBase;
    }

    public void setSpeedToBase(int speedToBase) {
        this.speedToBase = speedToBase;
    }

    public int getSpeedToEnemyBase() {
        return speedToEnemyBase;
    }

    public void setSpeedToEnemyBase(int speedToEnemyBase) {
        this.speedToEnemyBase = speedToEnemyBase;
    }

    public OSMData getMap() {
        return map;
    }

    public void setMap(OSMData map) {
        this.map = map;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Position getCenter() {
        return center;
    }

    public void setCenter(Position center) {
        this.center = center;
    }

    public int getKillScore() {
        return killScore;
    }

    public void setKillScore(int killScore) {
        this.killScore = killScore;
    }

    public int getFlagScore() {
        return flagScore;
    }

    public void setFlagScore(int flagScore) {
        this.flagScore = flagScore;
    }

    public double getDistanceContact() {
        return distanceContact;
    }

    public void setDistanceContact(double distanceContact) {
        this.distanceContact = distanceContact;
    }

    public static String[] getColors() {
        return colors;
    }

    public static void setColors(String[] colors) {
        Game.colors = colors;
    }

    public List<String> getPlayers() {
        return players;
    }

    public void setPlayers(List<String> players) {
        this.players = players;
    }

    public List<Base> getBases() {
        return bases;
    }

    public void setBases(List<Base> bases) {
        this.bases = bases;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean isReady) {
        this.isReady = isReady;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }


    public void setTeamList(Integer nTeams,List<Position> polygon) {
        while(nTeams-->0)
            this.bases.add(new Base(String.valueOf(nTeams),this.center,polygon,colors[nTeams]));
    }

    public void setTeamList(Integer nTeams,OSMData osm) {
        while(nTeams-->0)
            this.bases.add(new Base(String.valueOf(nTeams),osm,colors[nTeams]));
    }



    public int getBestBase() {
        int min=1000;
        int rc=0;
        for(int i=0;i<this.bases.size();i++)
            if(this.bases.get(i).getnPlayer()<min){
                min=this.bases.get(i).getnPlayer();
                rc=i;
            }
        return rc;
    }


    public boolean contains(User u) {
        return this.players.contains(u.getEmail());
    }

    public Base getBase(User p2) {
        for(Base b:this.bases)
            if(p2.getColor().equals(b.getColor()))return b;
        return null;
    }

    public void add(User[] players) {
        this.add(Arrays.asList(players));
        this.nplayers=this.players.size();
    }

    public Boolean remove(User u){
        int i=this.players.indexOf(u);
        if(i>=0){
            this.players.remove(i);
            return true;
        }
        return false;
    }

    public void add(List<User> players) {
        for(User u:players)this.add(u);
    }

    public void refreshIsReady() {
        this.isReady=true;
        for(Base b:this.bases)
            if(b.getnPlayer()==0)this.isReady=false;
    }

    public List<Object> getObjects(User u) {
        List<Object> rc=new ArrayList<Object>();
        for(Object o:this.getAllObjects(null))
            if(o.getOwner().equals(u.getEmail()))rc.add(o);
        return rc;
    }

    public Base getEnemyBase(User u) {
        for(Base b:this.bases)
            if(!b.color.equals(u.color))return b;
        return null;
    }

    public boolean equals(Game o) {
        if (this == o) return true;
        if (!(o instanceof Game)) return false;

        Game game = (Game) o;

        if (!id.equals(game.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public List<Position> getAllPositions(Iterable<Object> allObjects) {
        List<Position> rc=new ArrayList<Position>();
        for(Object o:allObjects)
            rc.add(o.getPosition());
        return rc;
    }
}
