package Main;


/**
 * Created by u016272 on 07/04/2016.
 */
public class Icon {
    public String path;
    public String id;

    public Icon(String path, String id) {
        this.path = path;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
