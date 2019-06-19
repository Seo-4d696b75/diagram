package jp.ac.u_tokyo.t.eeic.seo.diagram;

/**
 * @author Seo-4d696b75
 * @version 2018/05/13
 */
public class Rectangle {

    public Rectangle(double left, double top, double right, double bottom){
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public final double left,top,right,bottom;

    public Triangle getContainer(){
        double x = (left + right) / 2;
        double y = (top + bottom) / 2;
        double r = Math.sqrt(Math.pow(left-right,2) + Math.pow(top-bottom,2));
        Point a = new BasePoint(x-Math.sqrt(3)*r, y+r);
        Point b = new BasePoint(x+Math.sqrt(3)*r, y+r);
        Point c = new BasePoint(x, y-2*r);
        return new Triangle(a,b,c);
    }

}
