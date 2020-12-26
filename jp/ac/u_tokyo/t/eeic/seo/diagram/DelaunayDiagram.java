package jp.ac.u_tokyo.t.eeic.seo.diagram;

import java.util.*;

/**
 * ドロネー図を制作するクラス
 * @author Seo-4d696b75
 * @version 2018/05/13
 */
public class DelaunayDiagram {
    //base algorithm is retrieved from
    //https://qiita.com/edo_m18/items/7b3c70ed97bac52b2203
    //http://tercel-sakuragaoka.blogspot.jp/2011/06/processingdelaunay_3958.html

    /**
     * 分割する母点集合を指定してインスタンス化
     * @param points Setで重複をチェックする
     */
    public DelaunayDiagram(Collection<? extends Point> points){
        this.points = new HashSet<>();
        this.points.addAll(points);
    }

    private Set<Point> points;
    private Set<Triangle> solvedTriangle;
    private Set<Edge> solvedEdge;
    private HashMap<Edge, TrianglePair> solvedPair;

    private Set<Triangle> triangles;
    private HashMap<Edge, TrianglePair> trianglePairs;
    private Queue<Edge> edges;

    /**
     * {@link DelaunayDiagram#DelaunayDiagram(Collection)}で指定した母点集合
     * @return 重複のない点の集合
     */
    protected Set<Point> getPoints(){
        return points;
    }

    /**
     * 分割された三角形の集合.
     * @return Null if {@link #split(Rectangle)} not called yet
     */
    public Set<Triangle> getTriangles(){
        return solvedTriangle;
    }

    /**
     * 分割された三角形の辺の集合
     * @return Null if {@link #split(Rectangle)} not called yet
     */
    public Set<Edge> getEdges(){
        return solvedEdge;
    }

    /**
     * {@link #getEdges()}の辺に対しその辺を共有する三角形の組のマッピング
     * @return Null if {@link #split(Rectangle)} not called yet
     */
    public Map<Edge, TrianglePair> getEdgeTriangleMap(){
        return solvedPair;
    }

    /**
     * ひとつの辺を共有する二つの三角形のペアを表します
     */
    public static class TrianglePair{
        private TrianglePair(Edge edge, Point point1, Point point2){
            t1 = new Triangle(edge.a, edge.b, point1);
            t2 = new Triangle(edge.a, edge.b, point2);
            this.point1 = point1;
            this.point2 = point2;
            this.edge = edge;
        }
        private TrianglePair(Edge edge, Point point){
            t1 = new Triangle(edge, point);
            t2 = null;
            point1 = point;
            point2 = null;
            this.edge = edge;
        }

        private Triangle t1,t2;
        private Point point1,point2;
        private Edge edge;

        public Edge getEdge(){
            return edge;
        }

        Triangle getTriangle1(){
            return t1;
        }

        Triangle getTriangle2(){
            return t2;
        }

        Point getPoint1(){return point1;}

        Point getPoint2(){return point2;}

        private void replace(Point change){
            if ( t2 == null ){
                t2 = new Triangle(edge, change);
                return;
            }
            double apx = point1.getX() - edge.a.getX();
            double apy = point1.getY() - edge.a.getY();
            double bpx = point1.getX() - edge.b.getX();
            double bpy = point1.getY() - edge.b.getY();
            double aqx = change.getX() - edge.a.getX();
            double aqy = change.getY() - edge.a.getY();
            double bqx = change.getX() - edge.b.getX();
            double bqy = change.getY() - edge.b.getY();
            if ( (apx*bpy - apy*bpx) * (aqx*bqy - aqy*bqx) > 0 ){
                t1 = new Triangle(edge, change);
                point1 = change;
            }else{
                t2 = new Triangle(edge, change);
                point2 = change;
            }
        }

        private void removeBoundary(Triangle container){
            if ( container.isVertex(point1) ){
                point1 = point2;
                t1 = t2;
                point2 = null;
                t2 = null;
            }else if ( point2 != null && container.isVertex(point2) ){
                point2 = null;
                t2 = null;
            }
        }

        private boolean isFlip(){
            return point2 != null && t1.getCircumscribed().containsPoint(point2);
        }

        /**
         * フリップ操作
         * 現在：this.edge を共有する2つの三角形 edge-point1, edge-point2
         * => 操作後： 辺point1-point2を共有する2つ三角形
         */
        private void flip(){
            Edge old = this.edge;
            this.edge = new Edge(point1, point2);
            point1 = old.a;
            point2 = old.b;
            t1 = new Triangle(edge, point1);
            t2 = new Triangle(edge, point2);
        }
    }

    /**
     * ドロネー図分割を計算
     * @param border すべての母点を内部に含むような矩形
     */
    public void split(Rectangle border){
        long time = System.currentTimeMillis();

        // すべての点を内部に含む三角形を適当に設定
        final Triangle container = border.getContainer();

        // 初期化
        triangles = new HashSet<>();
        edges = new LinkedList<>();
        trianglePairs = new HashMap<>();
        triangles.add(container);
        Edge ab = new Edge(container.a, container.b);
        Edge bc = new Edge(container.b, container.c);
        Edge ca = new Edge(container.c, container.a);
        trianglePairs.put(ab, new TrianglePair(ab, container.c));
        trianglePairs.put(bc, new TrianglePair(bc, container.a));
        trianglePairs.put(ca, new TrianglePair(ca, container.b));

        System.out.println("calculating delaunay diagram...");

        // 点を三角形内部に逐次的に追加していく
        int size = points.size();
        int cnt = 0;
        for ( Point point : points ){
            Triangle t = getContainer(point);
            if ( t == null ){
                throw new IllegalArgumentException("point outside border Rectangle");
            }
            addPoint(point, t);
            System.out.print(String.format(Locale.US,"\r%.2f%% complete  ", (double)cnt++ *100 / size));
        }




        // 最初に適当に設定した外郭の三角形と頂点を共有するものを取り除く
        // 同時に残った三角形を内包する多角形を探す
        final Polygon.Builder frameBuilder = new Polygon.Builder();
        /* Java7
        for ( Iterator<Triangle> iterator = triangles.iterator() ; iterator.hasNext() ; ){
            Triangle next = iterator.next();
            if ( outside(next, container, frameBuilder) ) iterator.remove();
        }*/
        triangles.removeIf( next -> outside(next, container, frameBuilder));

        if ( !frameBuilder.isClosed() ){
            throw new RuntimeException("fail to calc frame");
        }
        Polygon frame = frameBuilder.build();
        List<Point> list = frame.getPoints();
        // ポリゴンの頂点が左回りに並ぶように調整する
        normalizeDirection(list);
        final int length = list.size();

        //=======改良ポイント==================================
        // すべての点を内包する多角形が凸形になるように調整する
        Point previous = list.get(length-1);
        List<Point> newList = new LinkedList<>();
        for ( int i=0 ; i<length ; i++ ){
            Point current = list.get(i);
            Point next = list.get((i+1)%length);
            double ax = current.getX() - previous.getX();
            double ay = current.getY() - previous.getY();
            double bx = next.getX() - current.getX();
            double by = next.getY() - current.getY();
            double cross = ax*by - ay*bx;
            if ( cross > 0 ){
                // 凸
                newList.add(current);
                previous = current;
            }else{
                // 凹
                addPointOutside(previous, current, next);
            }
        }
        list = newList;
        //===================================================

        System.out.println("outline solved.");
        for ( Point point : list){
            System.out.println(point.toString());
        }


        solvedTriangle = triangles;

        solvedEdge = new HashSet<>();
        for ( Triangle item : solvedTriangle ){
            solvedEdge.add(new Edge(item.a, item.b));
            solvedEdge.add(new Edge(item.b, item.c));
            solvedEdge.add(new Edge(item.c, item.a));
        }

        solvedPair = new HashMap<>();
        for ( Edge edge : solvedEdge ){
            TrianglePair pair = trianglePairs.get(edge);
            if ( pair == null ) throw new NullPointerException();
            pair.removeBoundary(container);
            solvedPair.put(edge, pair);
        }

        triangles = null;
        edges = null;
        trianglePairs = null;
        System.out.println("time:" + (System.currentTimeMillis() - time) + "ms");
    }

    private boolean outside(Triangle next, Triangle container, Polygon.Builder frameBuilder){
        /*
        boolean a = next.isVertex(container.a);
        boolean b = next.isVertex(container.b);
        boolean c = next.isVertex(container.c);*/
        int in = 0;
        int out = 0;
        Point[] inside = new Point[3];
        Point[] outside = new Point[3];
        if ( next.isVertex(container.a) ){
            outside[out++] = container.a;
        }else{
            inside[in++] = container.a;
        }
        if ( next.isVertex(container.b) ){
            outside[out++] = container.b;
        }else{
            inside[in++] = container.b;
        }
        if ( next.isVertex(container.c) ){
            outside[out++] = container.c;
        }else{
            inside[in++] = container.c;
        }
        if ( out == 1 ){
            frameBuilder.append(next.getOppositeSize(outside[0]));
            //trianglePairs.remove(new Edge(outside[0], inside[0]));
            //trianglePairs.remove(new Edge(outside[0], inside[1]));
            //trianglePairs.get(new Edge(inside[0], inside[1])).removeTriangle(outside[0]);

            /*if ( a ) frameBuilder.append(next.getOppositeSize(container.a));
            if ( b ) frameBuilder.append(next.getOppositeSize(container.b));
            if ( c ) frameBuilder.append(next.getOppositeSize(container.c));*/

            return true;
        }else if ( out == 2 ){
            // 一番外側の三角形
            //trianglePairs.remove(new Edge(outside[1], inside[0]));
            //trianglePairs.remove(new Edge(outside[0], inside[0]));
            //trianglePairs.remove(new Edge(outside[0], outside[1]));
            return true;
        }else{
            return false;
        }
    }

    private void normalizeDirection(List<Point> list){
        final int length = list.size();
        double sum = 0.0;
        for ( int i=0 ; i<length ; i++ ){
            Point previous = list.get((i-1+length)%length);
            Point current = list.get(i);
            Point next = list.get((i+1)%length);
            double ax = current.getX() - previous.getX();
            double ay = current.getY() - previous.getY();
            double bx = next.getX() - current.getX();
            double by = next.getY() - current.getY();

            //only available in range (-π/2,π/2)
            //double cross = ax*by - ay*bx;
            //double argument = Math.atan( cross / (ax*bx + ay*by));

            double argument = measureArgument(ax, ay, bx, by);
            sum += argument;
        }
        // sum = ±2π
        if ( sum < 0 ){
            Collections.reverse(list);
        }
    }

    /**
     * 2ベクトルA(ax,ay),B(bx,by)のなす角をラジアンで返す.
     * AからBへ右手系の向きで見た符号つき
     * @return {@code (-π,+π)}
     */
    private double measureArgument(double ax, double ay, double bx, double by){
        double v1 = ax * by - ay * bx;
        double v2 = ax * bx + ay * by;
        if ( v2 == 0 ){
            return v1 > 0 ? Math.PI/2 : -Math.PI/2;
        }else if ( v2 > 0 ){
            return Math.atan(v1/v2);
        }else{
            double v3 = Math.atan(v1/v2);
            return v3 > 0 ? v3 - Math.PI : v3 + Math.PI;
        }
    }

    private Triangle getContainer(Point point){
        for ( Triangle item : triangles ){
            if ( item.containsPoint(point) ){
                return item;
            }
        }
        return null;
    }

    private void addPointOutside(Point a, Point b, Point c){
        edges.clear();

        Edge ab = new Edge(a, b);
        Edge bc = new Edge(b, c);
        Edge ac = new Edge(a, c);

        trianglePairs.get(ab).replace(c);
        trianglePairs.get(bc).replace(a);
        trianglePairs.put(ac, new TrianglePair(ac, b));
        triangles.add(new Triangle(a, b, c));
        edges.offer(ab);
        edges.offer(bc);

        resolveDelaunay();
    }

    private void resolveDelaunay(){
        while( !edges.isEmpty() ){
            Edge edge = edges.poll();
            TrianglePair pair = trianglePairs.get(edge);
            if ( pair.isFlip() ){
                trianglePairs.remove(edge);
                triangles.remove(pair.t1);
                triangles.remove(pair.t2);
                pair.flip();
                trianglePairs.put(pair.edge, pair);
                triangles.add(pair.t1);
                triangles.add(pair.t2);
                Edge a1 = new Edge(pair.point1, pair.edge.a);
                Edge b1 = new Edge(pair.point1, pair.edge.b);
                Edge a2 = new Edge(pair.point2, pair.edge.a);
                Edge b2 = new Edge(pair.point2, pair.edge.b);
                edges.offer(a1);
                edges.offer(b1);
                edges.offer(a2);
                edges.offer(b2);
                trianglePairs.get(a1).replace(pair.edge.b);
                trianglePairs.get(b1).replace(pair.edge.a);
                trianglePairs.get(a2).replace(pair.edge.b);
                trianglePairs.get(b2).replace(pair.edge.a);
            }
        }
    }

    private void addPoint(Point p, Triangle t){

        edges.clear();

        if ( t.isVertex(p) ){
            //頂点に一致する場合はnothing to do
            return;
        }else if ( Edge.onEdge(t.a, t.b, p) ){
            addOnEdge(t.a, t.b, t.c, p);
        }else if ( Edge.onEdge(t.b, t.c, p) ){
            addOnEdge(t.b, t.c, t.a, p);
        }else if ( Edge.onEdge(t.c, t.a, p) ){
            addOnEdge(t.c, t.a, t.b, p);
        }else{
            addInTriangle(p, t);
        }

        resolveDelaunay();

    }

    //△ABCの辺AB上に点P
    private void addOnEdge(Point a, Point b, Point c, Point p){
        Edge old = new Edge(a, b);
        TrianglePair pair = trianglePairs.remove(old);
        Point d = new Line(old).onSameSide(p, pair.point1) ? pair.point2 : pair.point1;

        triangles.remove(pair.t1);
        triangles.remove(pair.t2);
        Edge pc = new Edge(p, c);
        Edge pd = new Edge(p, d);
        Edge pa = new Edge(p, a);
        Edge pb = new Edge(p, b);
        TrianglePair innerPair = new TrianglePair(pc, a, b);
        TrianglePair outerPair = new TrianglePair(pd, a, b);
        triangles.add(innerPair.t1);
        triangles.add(innerPair.t2);
        triangles.add(outerPair.t1);
        triangles.add(outerPair.t2);
        trianglePairs.put(pc, innerPair);
        trianglePairs.put(pd, outerPair);
        trianglePairs.put(pa, new TrianglePair(pa, d, c));
        trianglePairs.put(pb, new TrianglePair(pb, d, c));

        Edge ac = new Edge(a, c);
        Edge bc = new Edge(b, c);
        Edge ad = new Edge(a, d);
        Edge bd = new Edge(b, d);
        trianglePairs.get(ac).replace(p);
        trianglePairs.get(bc).replace(p);
        trianglePairs.get(ad).replace(p);
        trianglePairs.get(bd).replace(p);
        edges.offer(ac);
        edges.offer(bc);
        edges.offer(ad);
        edges.offer(bd);
    }

    private void addInTriangle(Point p, Triangle t){

        triangles.remove(t);
        Edge ab = new Edge(t.a, t.b);
        Edge bc = new Edge(t.b, t.c);
        Edge ca = new Edge(t.c, t.a);

        trianglePairs.get(ab).replace(p);
        trianglePairs.get(bc).replace(p);
        trianglePairs.get(ca).replace(p);
        Edge pa = new Edge(p, t.a);
        Edge pb = new Edge(p, t.b);
        Edge pc = new Edge(p, t.c);
        TrianglePair ta = new TrianglePair(pa, t.c, t.b);
        TrianglePair tb = new TrianglePair(pb, t.a, t.c);
        TrianglePair tc = new TrianglePair(pc, t.b, t.a);
        trianglePairs.put(pa, ta);
        trianglePairs.put(pb, tb);
        trianglePairs.put(pc, tc);
        triangles.add(ta.t1);
        triangles.add(tb.t1);
        triangles.add(tc.t1);
        edges.offer(ab);
        edges.offer(bc);
        edges.offer(ca);
    }



}
