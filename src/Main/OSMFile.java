package Main;

import java.io.Serializable;

/**
 * Created by u016272 on 24/04/2016.
 */
public class OSMFile implements Serializable {
    public String xml;

    public OSMFile() {
    }

    public String getXml() {
        return xml;
    }
    public void setXml(String xml) {
        this.xml = xml;
    }
}
