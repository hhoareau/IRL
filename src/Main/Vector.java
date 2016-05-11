package Main;

/**
 * Created by u016272 on 24/04/2016.
 */
public class Vector {
    public Double x=0.0;
    public Double y=0.0;
    public Double z=0.0;

    public Vector(Double x, Double y, Double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector(Integer x, Integer y, Integer z) {
        this.x = Double.valueOf(x);
        this.y = Double.valueOf(y);
        this.z = Double.valueOf(z);
    }

    public Vector(Position P) {
        Vector V=new Vector(P.lat,P.lng);
        this.x=V.x;
        this.y=V.y;
        this.z=V.z;
    }

    public Vector(Position A, Position B) {
        Vector vA=new Vector(A);
        Vector vB=new Vector(B);
        this.x=vB.x-vA.x;
        this.y=vB.y-vA.y;
        this.z=vB.z-vA.z;
    }

    public Vector(Position A, Position B, double k) {
        Vector AB=new Vector(A,B);
        AB.normalize();
        Vector O=new Vector(A);

        this.x=O.x+AB.x*k;
        this.y=O.y+AB.y*k;
        this.z=O.z+AB.z*k;
    }

    public Vector(int x, int y) {
        this.x=(double) x;
        this.y=(double) y;
    }

    public Vector(Long x, Long y) {
        this.x=(double) x;
        this.y=(double) y;
        this.z=0.0;
    }

    @Override
    public String toString() {
        return "Vector{" +
                "x=" + x +
                ",y=" + y +
                ",z=" + z +
                '}';
    }

    public Vector() {
    }

    public Vector(Double lat,Double lng){
        double lt = Math.toRadians(90-lat);
        double ln = Math.toRadians(lng);

        Double d=Position.R;

        this.x = d * Math.cos(ln) * Math.cos(lt);
        this.y = d * Math.cos(ln) * Math.sin(lt);
        this.z = d * Math.sin(ln);
    }



    public Vector(Vector A,Vector B,Vector C){
        Vector AB=new Vector(B.x-A.x,B.y-A.y,B.z-A.z);
        AB.normalize();
        Double prod_scalaire=AB.x*C.x+AB.y*C.y+AB.z*C.z;
        this.x=A.x+AB.x*prod_scalaire;
        this.y=A.y+AB.y*prod_scalaire;
        this.z=A.z+AB.z*prod_scalaire;
    }


    public void normalize(){
        Double ratio=this.distance(new Vector());
        this.x=this.x/ratio;
        this.y=this.y/ratio;
        this.z=this.z/ratio;
    }

    public Double distance(Vector A){
        Double d=(this.x-A.x)*(this.x-A.x)+(this.y-A.y)*(this.y-A.y)+(this.z-A.z)*(this.z-A.z);
        return Math.sqrt(d);
    }

    public Double getX() {
        return x;
    }
    public void setX(Double x) {
        this.x = x;
    }
    public Double getY() {
        return y;
    }
    public void setY(Double y) {
        this.y = y;
    }
    public Double getZ() {
        return z;
    }
    public void setZ(Double z) {
        this.z = z;
    }
}
