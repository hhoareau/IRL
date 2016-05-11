package Main;

/**
 * Created by u016272 on 28/04/2016.
 */
public class Node {
    Long id=0L;
    Long parentId=0L;
    Double x,y;
    Double cout=0.0;
    Double heuristique=0.0;


    public Node() {
    }

    public Node(Long id, Long parentId, Double x, Double y, Double cout, Double heuristique) {
        this.id = id;
        this.parentId = parentId;
        this.x = x;
        this.y = y;
        this.cout = cout;
        this.heuristique = heuristique;
    }
}
