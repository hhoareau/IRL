package Main;

import com.googlecode.objectify.ObjectifyService;

import javax.servlet.http.HttpServletRequest;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.List;

import static com.googlecode.objectify.ObjectifyService.ofy;

public class DAO {
    public static DAO dao=null;
    public Long dtStart=System.currentTimeMillis();
    public long calls=0L;
    public static List<Game> activeGames=new ArrayList<Game>();
    public static List<User> activeUsers=new ArrayList<User>();

    static {
        ObjectifyService.register(User.class);
        ObjectifyService.register(Game.class);
        ObjectifyService.register(CaptureTheFlag.class);
        ObjectifyService.register(Bot.class);
    }


    public List<Position> getPolygonFromKML(String kml)  {
        XPathFactory factory= XPathFactory.newInstance();
        XPath xPath=factory.newXPath();
        XPathExpression xPathExpression= null;

        List<Position> rc=new ArrayList<Position>();

        try {
            xPathExpression = xPath.compile("/Document/Placemark/outerBoundaryIs/LinearRing/coordinates/");
            String path=xPathExpression.evaluate(kml);
            for(String coord:path.split(" "))
                rc.add(new Position(Double.valueOf(coord.split(",")[0]),Double.valueOf(coord.split(",")[0])));

        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

        return rc;
    }

    public List<Position> getPolygonFromString(String pts)  {
        List<Position> rc=new ArrayList<Position>();
        for(String coord:pts.split(" "))
            rc.add(new Position(coord));
        return rc;
    }




    public static synchronized DAO getInstance() {
		  if (null == dao) {
			  dao = new DAO();
		  }
		  return dao;
		 }

    public User getUser(String email) {
        for(User u:activeUsers)
            if(u.email.equals(email))return u;
        return null;

        /*
        if(email==null)return null;
        User u=ofy().load().type(User.class).id(email).now();
        return u;
        */
    }

    public void save(User u) {
        int i=activeUsers.indexOf(u);
        if(i>-1)
            activeUsers.set(i,u);
        else
            activeUsers.add(u);

        //ofy().save().entities(u);
    }

    public void save(Game g) {
        int i=activeGames.indexOf(g);
        if(i>-1)
            activeGames.set(i,g);
        else
            activeGames.add(g);

        //ofy().save().entities(g);
    }

    public void flush(){
        ofy().save().entities(activeUsers);
        ofy().save().entities(activeGames);
        activeGames.clear();
        activeUsers.clear();
    }

    public List<Game> getGames(Position position, Double d) {
        List<Game> rc=new ArrayList<Game>();
        for(Game p:activeGames){
            if(p.center.distance(position)<d)rc.add(p);
        }
        return rc;
    }

    public Game getGame(Long id) {
        for(Game g:activeGames)
            if(g.id.equals(id))return g;
        return null;

        //return ofy().load().type(Game.class).id(id).now();
    }

    public List<User> getUsers(List<String> players) {
        List<User> rc=new ArrayList<User>();
        for(String email:players)
            rc.add(getUser(email));
        return rc;

    }

    public void raz() {
        activeUsers.clear();
        activeGames.clear();
        ofy().delete().keys(ofy().load().type(Game.class).keys().list());
        ofy().delete().keys(ofy().load().type(User.class).keys().list());
    }

    public List<User> getAllUsers() {
        return activeUsers;
        //return ofy().load().type(User.class).list();
    }

    //Nombre d'appels à la base par minutes
    public Long getCallsByMinute(){
        Long delay=(System.currentTimeMillis()-dtStart)/(1000*60); //Nombre de minutes depuis lancement
        if(delay==0)delay=1L;
        return this.calls/delay;
    }

    public User getUser(HttpServletRequest req) {
        for(User u:activeUsers)
            if(u.ipDevice.equals(req.getRemoteAddr()+req.getRemoteUser()))return u;

        return null;

        //List<User> l=ofy().load().type(User.class).filter("ipDevice", req.getRemoteAddr()+req.getRemoteUser()).list();
        //if(l.size()==0)return null;
        //return l.get(0);
    }

    public Game getGame(User u) {
        if(u==null)return null;
        for(Game g:this.getGames(u.getPosition(),100000.0))
            if(g.players.contains(u.getEmail()))return g;
        return null;
    }

    public void save(List<User> lu) {
        for(User u:lu)this.save(u);

        //ofy().save().entities(lu);
    }

    public List<Game> getActiveGames() {
        return activeGames;
        //return ofy().load().type(Game.class).filter("nplayers",">0").list();
    }

    public List<User> getActiveUsers() {
        return activeUsers;
    }
}


