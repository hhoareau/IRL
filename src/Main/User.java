package Main;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.io.Serializable;

@Entity
public class User extends Object implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final long ONLINE_DELAY = 10000L;
    //private static final String DOMAIN = "https://opt-adopt.appspot.com";
    //private static final String DOMAIN = "http://localhost:8080";

    private String home="";

    @Id
    String email = "";

    String username = "";
    String firstname = "";
    String lastname = "";
    String photo = "";
    String lang = "en";
    String friends = "";
    String url="";

    public Double lastMoveDistance=0.0;

    @Index
    String ipDevice="";

    Integer cartouches=100;

    Integer life=100;
    int score=0;

    Long dtCreate = System.currentTimeMillis();

    protected void initUser(infoFacebook infos,String Domain) {
        this.setMapSymbole(1);
        if(infos.email!=null)
            this.email = infos.email;
        else
            this.email = infos.id;

        this.firstname = infos.first_name;
        if (infos.locale.length() > 0) this.lang = infos.locale;
        this.photo = infos.link;
        this.lastname = infos.last_name;
        this.home = "https://www.facebook.com/" + infos.id;

        if (infos.first_name.length() == 0) this.username = infos.last_name;
        this.username= this.getEmail().split("@")[0];
        this.id= this.getEmail();
        this.url=Domain+"/_ah/api/irl/v1/kill?email="+ this.getEmail();
    }

    public User(String email) {
        initUser(new infoFacebook(email),"http://localhost:8080");
    }

    public User(infoFacebook infos,String Domain){
        super(infos.first_name,new Position());
        initUser(infos,Domain);
    }

    @Override
    public String toString() {
        return this.firstname+"("+this.life+"pt)"+":"+this.getPosition();
    }

    @Override
    public Boolean update(Game g) {return false;}

    User() {
        super();
    }

    public Integer getLife() {
        return life;
    }

    public void setLife(Integer life) {
        if(life<0)life=0;
        this.life = life;
    }

    public boolean isOnline(){
        return ((System.currentTimeMillis()-this.getPosition().lastUpdate)/1000<ONLINE_DELAY);
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getFriends() {
        return friends;
    }

    public void setFriends(String friends) {
        this.friends = friends;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }




    public String getHome() {
        return home;
    }

    public void setHome(String home) {
        this.home = home;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public Long getDtCreate() {
        return dtCreate;
    }

    public void setDtCreate(Long dtCreate) {
        this.dtCreate = dtCreate;
    }

    public Integer getCartouches() {
        return cartouches;
    }

    public void setCartouches(Integer cartouches) {
        this.cartouches = cartouches;
    }

    public int getScore() {
        return score;
    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIpDevice() {
        return ipDevice;
    }

    public void setIpDevice(String ipDevice) {
        this.ipDevice = ipDevice;
    }

    public boolean isALive() {
        return this.life>0;
    }


    public boolean equals(User o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (email != null ? !email.equals(user.email) : user.email != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (email != null ? email.hashCode() : 0);
        return result;
    }
}