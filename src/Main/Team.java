package Main;

import java.io.Serializable;
import java.util.List;

/**
 * Created by u016272 on 28/03/2016.
 */
public class Team implements Serializable {
    String name="team";

    Base base=new Base();
    String color;

    public Team(String name,Position center,List<Position> polygon,String color) {
        this.name = name;
        this.color=color;
        base=new Base(name,center,polygon,color);
    }


    public Team() {
    }


    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Base getBase() {
        return base;
    }

    public void setBase(Base base) {
        this.base = base;
    }

}
