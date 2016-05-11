package Main;

import com.google.appengine.repackaged.com.google.common.util.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by u016272 on 25/04/2016.
 */
public class OSMData {

    public static Logger log = Logger.getLogger(String.valueOf(Rest.class));

    List<Road> roads=new ArrayList<Road>();
    Map<String,Position> positions=new HashMap<String, Position>();
    Road r=null;
    Map<String,List<String>> voisins=new HashMap<String, List<String>>();

    public OSMData() {}

    public Boolean buildFromUrl(String url){
        DefaultHttpClient httpclient = new DefaultHttpClient();
        log.info("Récupération de "+url);
        HttpGet httpget = new HttpGet(url);
        String authent="hhoareau@gmail.com:hh040271";
        httpget.setHeader("Authorization","Basic "+ Base64.encode(authent.getBytes()));
        try {
            HttpResponse response = httpclient.execute(httpget);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200 ){
                HttpEntity entity = response.getEntity();
                buildFromIStream(entity.getContent());
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    public OSMData(String url){
        buildFromUrl(url);
        initGraph();
    }


    public OSMData(Position center,Double rayon){
        Double x1=0.0,y1=0.0,x2=0.0,y2=0.0;
        do{
            x1=center.lat-rayon;
            y1=center.lng-rayon;
            x2=center.lat+rayon;
            y2=center.lng+rayon;
            rayon=rayon/10;
        }
        while(!buildFromUrl("http://api.openstreetmap.org/api/0.6/map?bbox="+y1+","+x1+","+y2+","+x2));
        initGraph();
    }


    public List<String> getConnected(String p){
        List<String> rc=new ArrayList<String>();
        for(Road r:this.roads){
            int i=r.nodes.indexOf(p);
            if(i>=0){
                if(i>0){
                    String pos=r.nodes.get(i - 1);
                    rc.add(pos);
                }
                if(i<r.nodes.size()-1){
                    String pos=r.nodes.get(i+1);
                    rc.add(pos);
                }
            }
        }
        return rc;
    }

    public Collection<Position> getAllPosition(){
        return this.positions.values();
    }


    public List<String> getAllId(){
        List<String> rc=new ArrayList<String>();
        for(Road r:this.roads)rc.addAll(r.nodes);
        return this.removeDouble(this.positions.keySet());
    }


    private List<Position> getPositions(Collection<String> nodes) {
        List<Position> rc=new ArrayList<Position>();
        for(String i:nodes)
            rc.add(this.positions.get(i));
        return rc;
    }


    public Position nearestPosition(Position p){
        return p.getNearestFrom(this.getAllPosition());
    }


    public void completeGraph(){
        for(String s:voisins.keySet()){
            for(String v:voisins.get(s)) {
                List<String> lv = voisins.get(v);
                if (!lv.contains(s)){
                    lv.add(s);
                    voisins.put(v, lv);
                }
            }
        }
    }


    public void initGraph(){
        for(String l:this.getAllId()){
            voisins.put(l,this.getConnected(l));
            this.positions.get(l).f=0.0;
            this.positions.get(l).g=0.0;
            this.positions.get(l).h=0.0;
            this.positions.get(l).parent=null;
        }
        completeGraph();
    }


    public List<Position> bestWay(Position start,Position end){
        //Find all neigtborhood off all point

        for(String l:this.getAllId()){
            this.positions.get(l).f=0.0;
            this.positions.get(l).g=0.0;
            this.positions.get(l).h=0.0;
            this.positions.get(l).parent=null;
        }

        int i=0;

        List<String> open=new ArrayList<String>();
        List<String> close=new ArrayList<String>();

        if (nearestPosition(start) == null || nearestPosition(end) == null)return null;

        String lStart= nearestPosition(start).getId();
        String lEnd= nearestPosition(end).getId();

        log.info("start:"+lStart+",end:"+lEnd);
        open.add(lStart);

        while(open.size()>0){
            String currentNode=open.get(0);
            for(String s:open)
                if(this.positions.get(s).f<this.positions.get(currentNode).f)currentNode=s;

            if(currentNode.equals(lEnd)){
                List<Position> rc=new ArrayList<Position>();
                String p=currentNode;
                while(this.positions.get(p).parent!=null){
                    Position pos=this.positions.get(p);
                    rc.add(pos);
                    p=pos.parent;
                }
                Collections.reverse(rc);
                return(rc);
            }else{
                open.remove(currentNode);
                close.add(currentNode);

                for(String v:voisins.get(currentNode)){
                    if(close.contains(v))continue;
                    Double gScore=this.positions.get(currentNode).g+1;
                    Boolean gScoreIsBest=false;

                    if(!open.contains(v)){
                        gScoreIsBest=true;
                        this.positions.get(v).h=this.positions.get(v).distance(end);
                        open.add(v);
                    } else
                        if (gScore<this.positions.get(v).g){
                            gScoreIsBest=true;
                        }

                    if(gScoreIsBest){
                        this.positions.get(v).parent=currentNode;
                        this.positions.get(v).g=gScore;
                        this.positions.get(v).f=this.positions.get(v).g+this.positions.get(v).h;
                    }
                }
            }
        }
        return new ArrayList<Position>();
    }


    private void buildFromIStream(InputStream xml){
        DefaultHandler myHandler = new DefaultHandler() {
            public void startElement(String uri, String localName, String qName,Attributes attributes) throws SAXException {
                if (qName.equals("node")) {
                    Double lat= Double.valueOf(attributes.getValue(7));
                    Double lng= Double.valueOf(attributes.getValue(8));
                    String id=attributes.getValue(0);
                    positions.put(id, new Position(lat,lng,id));
                }

                if (qName.equals("tag") && r!=null){
                    r.tags.add(attributes.getValue(0));
                }

                if (qName.equals("way")){
                    r=new Road();
                }

                if (qName.equals("nd") && r!=null){
                    String idNode=attributes.getValue(0);
                    r.nodes.add(idNode);
                    /*
                    Position p=positions.get(idNode);
                    if(p!=null){
                        List<Position> voisins=p.getNear(30.0,getAllPosition());
                        if(voisins.size()==0)
                            r.nodes.add(idNode);
                        else{
                            String idVoisin=getId(voisins.get(0));
                            r.nodes.add(idVoisin);
                            positions.remove(idNode);
                        }
                    }
                    */
                }
            }

            public void endElement(String uri, String localName,String qName) throws SAXException {
                if (qName.equals("way")){
                    if(r.isRoad())
                        roads.add(r);
                    r=null;
                }
            }
        };

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            parser.parse(new InputSource(xml), myHandler);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        removeNotUsed();
        log.info("Regroupement des points "+this.positions.size());
        groupPosition(15.0);
        log.info("Carte ok. "+this.positions.size()+" positions");
    }

    protected void removeNotUsed(){
        boolean bDel=true;
        List<String> usenode=new ArrayList<String>();
        for(Road r:this.roads)usenode.addAll(r.nodes);
        List<String> toRemove=new ArrayList<String>();
        for(String id:this.positions.keySet())
            if(!usenode.contains(id))toRemove.add(id);

        for(String r:toRemove)
            this.positions.remove(r);

    }

    protected void replace(String idOld,String idNew){
        for(Road r:this.roads){
            int i=r.nodes.indexOf(idOld);
            if(i>-1)r.nodes.set(i,idNew);
        }
        this.positions.remove(idOld);
    }



    protected void groupPosition(Double min){
        boolean rc=true;
        while(rc){
            rc=false;
            for (String idnew:this.positions.keySet()) {
                for(Position v:positions.get(idnew).getNear(min, this.getPositions(this.positions.keySet()))){
                    rc = true;
                    replace(v.getId(), idnew);
                }
                if(rc){
                    //log.info("Positions : "+this.positions.size());
                    break;
                }
            }
        }
    }

    protected List<String> groupNodes(List<String> lp,Double min){
        for (int i = 0; i < lp.size(); i++)
            for (int j = 0; i < lp.size(); i++)
                if(i!=j && this.positions.get(lp.get(i)).distance(this.positions.get(lp.get(j)))<min)
                    lp.set(i,lp.get(j));
        return lp;
    }


    protected List<String> removeDouble(Collection<String> lp){
        List<String> rc=new ArrayList<String>();
        for(String p:lp)if(!rc.contains(p))rc.add(p);
        return rc;
    }



    public Road nearest(Position P){
        Double seuil=5.0;
        for(Road r:this.roads)
            for(int i=0;i<r.nodes.size()-1;i++)
                if(P.distance(getPosition(r,i))+P.distance(getPosition(r,i+1))-(getPosition(r,i+1).distance(getPosition(r, i)))<seuil)
                    return new Road(r.nodes.get(i),r.nodes.get(i+1));
        return null;
    }

    public List<Road> getRoads() {
        return roads;
    }

    public void setRoads(List<Road> roads) {
        this.roads = roads;
    }

    public Map<String, Position> getPositions() {
        return positions;
    }

    public void setPositions(Map<String, Position> positions) {
        this.positions = positions;
    }

    @Override
    public String toString() {
        String rc=this.roads.size()+" roads";
        for(Road r:this.roads)
            rc+=r.toString()+"\n";
        return rc;
    }

    public List<Divers> getMapObjects() {
        List<Divers> rc=new ArrayList<Divers>();
        int i=0;
        for(Road r:this.roads) {
            int j=0;
            i++;
            for (Position p : this.getPositions(r.nodes)) {
                j++;
                Divers o = new Divers("R"+i+"P"+j,p);
                rc.add(o);
            }
        }
        return rc;
    }

    public Position getPosition(Road r, int i) {
        String id=r.nodes.get(i);
        return this.positions.get(id);
    }


}
