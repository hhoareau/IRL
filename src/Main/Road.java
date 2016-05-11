package Main;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by u016272 on 25/04/2016.
 */
public class Road {
    List<String> nodes=new ArrayList<String>();
    List<String> tags=new ArrayList<String>();

    public Road(List<String> nodes) {
        this.nodes = nodes;
    }

    public Road() {
    }

    public Road(String position, String position1) {
        nodes.add(position);
        nodes.add(position1);
    }

    public void add(String p){
        this.nodes.add(p);
    }

    public List<String> getNodes() {
        return nodes;
    }

    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
    }


    @Override
    public String toString() {
        return "Nodes : "+this.nodes.size();
    }

    public boolean isRoad() {
        return this.tags.contains("highway");
    }
}
