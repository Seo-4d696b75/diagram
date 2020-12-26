package jp.ac.u_tokyo.t.eeic.seo.diagram;

/**
 * 円を定義
 * @author Seo-4d696b75
 * @version 2018/05/13
 */
public class Circle {

    /**
     * 中心と半径で円を定義する.
     * @param center 中心の座標
     * @param radius 半径
     * @throws IllegalArgumentException if radius <= 0
     */
    public Circle(Point center, double radius){
        if ( radius <= 0 ) throw new IllegalArgumentException("negative radius");
        this.center = center;
        this.radius = radius;
    }

    public final Point center;
    public final double radius;

    /**
     * 指定された点が円の内部に存在するか判定.<br>
     * 円周上は含まない.
     * @return 内部にあるならtrue, otherwise false
     */
    public boolean containsPoint(Point point){
        return Point.measure(point, center) < radius;
    }

}
