package Main;


import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;
import com.google.gson.Gson;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Logger;

//http://localhost:8888/_ah/api/irl/v1/init
@Api(name = "irl",description= "irl rest service",version = "v1")
public class Rest {

    public static DAO dao = DAO.getInstance();
    public static Logger log = Logger.getLogger(String.valueOf(Rest.class));
    public static Random randomGenerator=new Random();

    @ApiMethod(name = "adduser", httpMethod = ApiMethod.HttpMethod.POST, path = "adduser")
    public User addUser(@Named("info") String s, @Named("UID") String uid, @Named("domain") String domain,@Named("bot") Boolean bot) {
        infoFacebook infos = null;
        if (s.startsWith("{"))
            infos = new Gson().fromJson(s, infoFacebook.class);
        else
            infos = new infoFacebook(s.replace("\"", ""));

        User u = dao.getUser(infos.email);
        if (u == null) {
            if(bot)
                u= new Bot(infos,domain);
            else
                u = new User(infos, domain);

            dao.save(u);
        }
        return u;
    }


    @ApiMethod(name = "kill", httpMethod = ApiMethod.HttpMethod.GET, path = "kill")
    public void kill(HttpServletRequest req, @Named("email") String email) {
        User killer = dao.getUser(req);
        User killed = dao.getUser(email);
        if (killer != null && killed != null) {
            if (killer.isALive()) {
                Game g = dao.getGame(killer);
                if (g != null)
                    if (g.contains(killed)){ //ils sont dans la même partie
                        User[] users = g.playerKillPlayer(killer, killed);
                        dao.save(users[0]);
                        dao.save(users[0]);
                    }

            }
        }
    }


    @ApiMethod(name = "getgames", httpMethod = ApiMethod.HttpMethod.GET, path = "getgames")
    public List<Game> getgames(@Named("email") String email, @Named("distance") Double distance) {
        User u = dao.getUser(email);
        Position center = new Position(0.0, 0.0);
        if (u != null) center = u.getPosition();
        return dao.getGames(center, distance);
    }


    @ApiMethod(name = "join", httpMethod = ApiMethod.HttpMethod.GET, path = "join")
    public User join(HttpServletRequest req, @Named("idgame") Long idGame, @Named("email") String email) {
        Game p = dao.getGame(idGame);
        User u = dao.getUser(email);

        u.ipDevice = req.getRemoteAddr() + req.getRequestedSessionId();
        u = p.add(u);
        dao.save(p);
        dao.save(u);
        return u;
    }


    @ApiMethod(name = "quit", httpMethod = ApiMethod.HttpMethod.GET, path = "quit")
    public Game quit(@Named("idgame") Long idGame, @Named("email") String email) {
        Game p = dao.getGame(idGame);
        User u = dao.getUser(email);
        if(p.remove(u))
            dao.save(p);

        return p;
    }


    @ApiMethod(name = "createfromkml", httpMethod = ApiMethod.HttpMethod.POST, path = "createfromkml")
    public Game createfromkml(@Named("flags") Long nFlags, @Named("teams") Integer nTeams, @Named("email") String email, @Named("kml") String kml) {
        User owner = dao.getUser(email);
        List<Position> polygon = dao.getPolygonFromKML(kml);
        Game g = new CaptureTheFlag(owner, nTeams,null);
        dao.save(g);
        return g;
    }

    @ApiMethod(name = "sendmap", httpMethod = ApiMethod.HttpMethod.POST, path = "sendmap")
    public void sendmap(@Named("idgame") Long id, OSMFile osm) {
        Game g=dao.getGame(id);
        String s=osm.getXml();
        g.map=new OSMData(s);
        g.moveOnRoad();
        dao.save(g);
    }

    @ApiMethod(name = "createfrompts", httpMethod = ApiMethod.HttpMethod.GET, path = "createfrompts")
    public Game createfrompts(@Named("flags") Integer nFlags, @Named("teams") Integer nTeams,
                              @Named("email") String email, @Named("pts") String pts,@Named("url") String url) {
        User owner = dao.getUser(email);
        List<Position> polygon = dao.getPolygonFromString(pts);
        Game g = new Game("Nouvelle Partie", owner, nFlags, "flag", nTeams,new OSMData(url));
        dao.save(g);
        return g;
    }


    @ApiMethod(name = "createcapturetheflag", httpMethod = ApiMethod.HttpMethod.GET, path = "createcapturetheflag")
    public Game createCaptureTheFlag(@Named("teams") Integer nTeams, @Named("email") String email,
                                     @Named("pts") String pts,@Named("url") String url) {
        User owner = dao.getUser(email);
        List<Position> polygon = dao.getPolygonFromString(pts);
        Game g = new CaptureTheFlag(owner,nTeams,new OSMData(url));
        dao.save(g);
        return g;
    }


    @ApiMethod(name = "allplayers", httpMethod = ApiMethod.HttpMethod.GET, path = "allplayers")
    public List<User> allusers() {
        return dao.getAllUsers();
    }


    @ApiMethod(name = "players", httpMethod = ApiMethod.HttpMethod.GET, path = "players")
    public List<String> players(@Named("idpartie") Long id) {
        Game p = DAO.getInstance().getGame(id);
        return p.players;
    }

    @ApiMethod(name = "position", httpMethod = ApiMethod.HttpMethod.GET, path = "position")
    public User position(@Named("email") String email, @Named("lat") Double lat, @Named("lng") Double lng) {
        User p = dao.getUser(email);
        if (p != null) {
            if (p.moveTo(lat, lng))
                dao.save(p);
        }
        return p;
    }

    @ApiMethod(name = "refreshgame", httpMethod = ApiMethod.HttpMethod.GET, path = "refreshgame")
    public Game refreshGame(@Named("idgame") Long id) {
        Game g = dao.getGame(id);
        dao.save(g.calcul(dao.getUsers(g.players)));
        dao.save(g);
        return g;
    }



    //Call this methode every 5 secondes from any computer
    //commande bash -c 'for i in {1..12}; do; eval "curl https://opt-adopt.appspot.com/_ah/api/irl/v1/refresh?password=hh4271"; sleep 5; done'
    //bash -c 'for i in {1..12}; eval "curl http://192.168.0.11:8080/_ah/api/irl/v1/refresh?password=hh4271"; sleep 5; done'
    @ApiMethod(name = "refresh", httpMethod = ApiMethod.HttpMethod.GET, path = "refresh")
    public Vector refresh() {
        for(Game g:dao.getActiveGames()) {
            dao.save(g.calcul(dao.getUsers(g.players)));
            dao.save(g);
        }

        return new Vector(dao.getActiveGames().size(),dao.getActiveUsers().size());
    }



    @ApiMethod(name = "initicons", httpMethod = ApiMethod.HttpMethod.GET, path = "initicons")
    public List<Icon> initicons() {
        InputStream resourceStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("/WEB-INF/dessin.svg");
        List<Icon> rc = new ArrayList<Icon>();
        return rc;
    }

    @ApiMethod(name = "callsbymin", httpMethod = ApiMethod.HttpMethod.GET, path = "callsbymin")
    public SysInfo callsbymin() {
        SysInfo rc = new SysInfo(dao.getCallsByMinute());
        return rc;
    }


    @ApiMethod(name = "getgame", httpMethod = ApiMethod.HttpMethod.GET, path = "getgame")
    public Game getgame(@Named("idgame") Long id) {
        Game g = dao.getGame(id);
        g.refreshIsReady();
        return g;
    }


    @ApiMethod(name = "getflags", httpMethod = ApiMethod.HttpMethod.GET, path = "getflags")
    public List<Flag> getflags() {
        List<Flag> rc = new ArrayList<Flag>();
        for (Game g : dao.getGames(new Position(), 1000000.0))
            rc.addAll(g.flags);
        return rc;
    }


    @ApiMethod(name = "raz", httpMethod = ApiMethod.HttpMethod.GET, path = "raz")
    public void raz() {
        dao.raz();
    }


    @ApiMethod(name = "getobjects", httpMethod = ApiMethod.HttpMethod.GET, path = "getobjects")
    public List<Object> getAllObjects(@Named("idgame") Long id, @Named("email") String email, @Named("distance") Double distance) {
        User u = dao.getUser(email);
        Game g = dao.getGame(id);
        //rc.add(u);
        return u.getNear(distance, g.getAllObjects(dao.getUsers(g.players)));
    }

    @ApiMethod(name = "bestway", httpMethod = ApiMethod.HttpMethod.GET, path = "bestway")
    public Collection<Position> bestway(@Named("idgame") Long id,@Named("lat1") Double lat1,@Named("lng1") Double lng1,@Named("lat2") Double lat2,@Named("lng2") Double lng2) {
        Game g=dao.getGame(id);
        Position start=new Position(lat1,lng1);
        Position end=new Position(lat2,lng2);
        return g.map.bestWay(start,end);
    }


        @ApiMethod(name = "getallobjects", httpMethod = ApiMethod.HttpMethod.GET, path = "getallobjects")
    public List<Object> getAll() {
        List<Object> rc = new ArrayList<Object>();
        for (Game g : dao.getGames(new Position(), 1000000000.0))
            rc.addAll(g.getAllObjects(dao.getUsers(g.players)));

        return rc;
    }


    @ApiMethod(name = "getmapobjects", httpMethod = ApiMethod.HttpMethod.GET, path = "getmapobjects")
    public List<Divers> getmapobjects(@Named("idgame") Long id) {
        Game g=dao.getGame(id);
        return g.map.getMapObjects();
    }



    //Test http://localhost:8080/_ah/api/irl/v1/distance?lat1=48.86606&long1=2.35652&lat2=48.9&long2=2.4
    @ApiMethod(name = "distance", httpMethod = ApiMethod.HttpMethod.GET, path = "distance")
    public void distance(@Named("lat1") Double lat1, @Named("long1") Double long1, @Named("lat2") Double lat2, @Named("long2") Double long2) {
        Position p1 = new Position(lat1, long1);
        Position p2 = new Position(lat2, long2);
        Double d = p1.distance(p2);
    }




    @ApiMethod(name = "test", httpMethod = ApiMethod.HttpMethod.GET, path = "test")
    public Vector test(@Named("id") Integer id,@Named("lat") Double lat,@Named("lng") Double lng) {
        if(id==1){
            List<User> players=Arrays.asList(
                    new User(new infoFacebook("hhoareau@gmail.com"),"http://localhost:8080"),
                    new User(new infoFacebook("paul.dudule@gmail.com"),"http://localhost:8080"));

            List<Position> polygon=new ArrayList<Position>();
            polygon.add(new Position("48,2"));
            polygon.add(new Position("49,2"));
            polygon.add(new Position("49,3"));

            int step=6;
            Game g=new CaptureTheFlag(players.get(0),2,null);
            log.info("players:"+Game.showCollection(dao.getUsers(g.players)));
            log.info(g.toString());

            g.add(players);
            //for(int i=0;i<players.size();i++) players.get(i).setSpeed(g.getBase(players.get(i)).getPosition(), step);

            log.info(g.toString());

            for(int i=0;i<step;i++)
                players=g.calcul(players);

            //players.get(0).setSpeed(g.getBase(players.get(1)).getPosition(), 3);

            for(int i=0;i<step;i++){
                players=g.calcul(players);
                log.info(players.get(0).toString());
            }

            players=Arrays.asList(g.playerKillPlayer(players.get(0),players.get(1)));

            //players.get(0).setSpeed(g.getBase(players.get(1)).getPosition(),step);

            for(int i=0;i<step;i++)
                players=g.calcul(players);
        }

        if(id==2){
            Position p1=new Position("48,2");
            Vector v1=new Vector(p1);
            Position p2=new Position(v1);
            Vector v2=new Vector(p2);
            Double d1=v1.distance(v2);
            Double d=p2.distance(p1);

        }

        if(id==4){
            Vector B=new Vector(2,2,2);
            Vector A=new Vector();
            Vector C=new Vector(2,2,0);
            Vector H=new Vector(A,B,C);
        }


        if(id==3){
            Position P=new Position("48.924942,2.3616392");
            Position A=new Position("48.924561,2.36032");
            Position B=new Position("48.924603,2.362187");
            Position H=new Position(P,A,B);
        }

        if(id==5){
            OSMData osm=new OSMData("http://api.openstreetmap.org/api/0.6/map?bbox=2.351958286967374,49,2.3557625130325732,48.8761235");
            for(Road r:osm.roads)
                for(String p:r.nodes)
                    log.info("Nombre de connexion:"+osm.getConnected(p).size());
        }

        if(id==6){
            OSMData osm=new OSMData(new Position(lat,lng),0.003);
            //OSMData osm=new OSMData("http://localhost:8080/map.osm");
            if(osm.roads.size()>0){
                Game g=new CaptureTheFlag(new User("hhoareau@gmail.com"),2,osm);
                dao.save(g);
                return new Vector(g.id,g.id);
            }
            return null;
        }

        //http://localhost:8080/_ah/api/irl/v1/test?id=7
        if(id==7){
            //OSMData osm=new OSMData(new Position(lat,lng),0.003);
            OSMData osm=new OSMData("http://localhost:8080/map.osm");

            for(Position A:osm.positions.values())
                for(String s:osm.voisins.get(A.id)){
                    Position B=osm.positions.get(s);
                    if(!osm.voisins.get(s).contains(A.id))
                        log.info("Probleme avec "+A.toString());
                }

            Game g=new CaptureTheFlag(new User("hhoareau@gmail.com"),2,osm);
            Position A=g.map.positions.get(g.map.positions.keySet().toArray()[8]);
            Position B=g.map.positions.get(g.map.positions.keySet().toArray()[13]);
            List<Position> way2=g.map.bestWay(A, B);
            List<Position> way1=g.map.bestWay(B,A);
            return new Vector(way1.size(),way2.size());
        }

        return new Vector();
    }

}