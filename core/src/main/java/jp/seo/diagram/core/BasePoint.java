package jp.seo.diagram.core;


/**
 * {@link Point}のもっとも基本的な実現クラス.<br>
 * X,Y座標のメンバのみ保持する
 * @author Seo-4d696b75
 * @version 2018/05/13
 */
public class BasePoint extends Point{

    public BasePoint(double x, double y){
        this.x = x;
        this.y = y;
    }

    private final double x,y;

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

}
