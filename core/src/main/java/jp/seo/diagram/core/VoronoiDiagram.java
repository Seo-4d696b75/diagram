package jp.seo.diagram.core;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author Seo-4d696b75
 * @version 2018/05/14
 */
public class VoronoiDiagram extends DelaunayDiagram {

    //https://qiita.com/yellow_73/items/abfca17297124785fe28


    public VoronoiDiagram(Collection<? extends Point> list) {
        super(list);
    }

    private Set<Edge> solvedEdge;
    private Map<Point, VoronoiArea> areaMap;

    public Collection<VoronoiArea> getVoronoiAreas() {
        return areaMap.values();
    }

    public VoronoiArea getVoronoiArea(Point key) {
        return areaMap.get(key);
    }

    @Override
    public Set<Edge> getEdges() {
        return this.solvedEdge;
    }

    public Set<Edge> getDelaunayEdges() {
        return super.getEdges();
    }

    @Override
    public void split(Rectangle border) {
        super.split(border);
        Map<Edge, TrianglePair> pairMap = getEdgeTriangleMap();
        Set<Edge> edges = super.getEdges();
        areaMap = new HashMap<>();
        int size = edges.size();
        int cnt = 0;
        System.out.println("calculating voronoi diagram...");
        solvedEdge = new HashSet<>();
        for (Edge edge : edges) {
            TrianglePair pair = pairMap.get(edge);
            Triangle t1 = pair.getTriangle1();
            Triangle t2 = pair.getTriangle2();
            if (t2 != null) {
                Point p1 = t1.getCircumscribed().center;
                Point p2 = t2.getCircumscribed().center;
                Edge boundary = new Edge(p1, p2);
                if (isInsideRect(border, boundary)) {
                    // nothing
                } else if (isInsideRect(border, p1)) {
                    Point p = getBorderIntersection(border, boundary);
                    boundary = new Edge(p, p1);
                } else if (isInsideRect(border, p2)) {
                    Point p = getBorderIntersection(border, boundary);
                    boundary = new Edge(p, p2);
                } else {
                    boundary = null;
                }
                if (boundary != null) {
                    solvedEdge.add(boundary);
                    addEdge(boundary, edge.a);
                    addEdge(boundary, edge.b);
                }
            } else {
                // ドロネー図の一番外側の辺
                // 本当はこの三角形の外心を端点にもつ長さ無限の半直線が生えているのだが、
                // とりあえず適当な長さの線分を生やしておく
                Point center = t1.getCircumscribed().center;
                Point opposite = pair.getPoint1();
                Line bisector = Line.getPerpendicularBisector(edge);
                Point vector = bisector.getUnitDirectionVector();
                Point intersection = edge.getIntersection(bisector);
                int direction = edge.onSameSide(center, opposite) ?
                        Point.compare(intersection, center) : Point.compare(center, intersection);
                Point outer = new BasePoint(
                        center.getX() + vector.getX() * direction * 100,
                        center.getY() + vector.getY() * direction * 100
                );
                Edge line = new Edge(center, outer);
                if (!isOutsideRect(border, line)) {
                    Point p = getBorderIntersection(border, line);
                    Edge boundary = new Edge(center, p);
                    solvedEdge.add(boundary);
                    addEdge(boundary, edge.a);
                    addEdge(boundary, edge.b);
                }
            }
            System.out.printf(Locale.US, "\r%.2f%% complete  ", (double) cnt++ * 100 / size);
        }
        System.out.println("\ncheck...");
        for (VoronoiArea area : areaMap.values()) {
            area.onEnclosed();
        }
        System.out.println("done.");
    }

    private void addEdge(Edge boundary, Point p) {
        areaMap.computeIfAbsent(p, (VoronoiArea::new));
        VoronoiArea area = areaMap.get(p);
        area.addEdge(boundary);
    }

    @NotNull
    private Point getBorderIntersection(Rectangle b, Edge line) {
        Point p;
        Edge top = new Edge(new BasePoint(b.left, b.top), new BasePoint(b.right, b.top));
        p = top.getIntersection(line);
        if (p != null) return p;
        Edge right = new Edge(new BasePoint(b.right, b.top), new BasePoint(b.right, b.bottom));
        p = right.getIntersection(line);
        if (p != null) return p;
        Edge bottom = new Edge(new BasePoint(b.left, b.bottom), new BasePoint(b.right, b.bottom));
        p = bottom.getIntersection(line);
        if (p != null) return p;
        Edge left = new Edge(new BasePoint(b.left, b.top), new BasePoint(b.left, b.bottom));
        p = left.getIntersection(line);
        if (p != null) return p;
        throw new RuntimeException("no intersection");
    }

    private boolean isInsideRect(Rectangle rect, Edge e) {
        return isInsideRect(rect, e.a) && isInsideRect(rect, e.b);
    }

    private boolean isOutsideRect(Rectangle rect, Edge e) {
        return !isInsideRect(rect, e.a) && !isInsideRect(rect, e.b);
    }

    private boolean isInsideRect(Rectangle rect, Point p) {
        return rect.left <= p.getX() && p.getX() <= rect.right &&
                rect.bottom <= p.getY() && p.getY() <= rect.top;
    }

    public static class VoronoiArea {

        private VoronoiArea(Point center) {
            this.center = center;
        }

        private void addEdge(Edge edge) {
            if (pool == null) {
                pool = new LinkedList<>();
                list = new LinkedList<>();
                list.add(edge.a);
                list.add(edge.b);
                start = edge.a;
                end = edge.b;
            } else {
                if (merge(edge)) {
                    while (true) {
                        if (!pool.removeIf(this::merge) || pool.isEmpty()) break;
                    }
                } else {
                    pool.add(edge);
                }
            }
        }

        private boolean merge(Edge edge) {
            if (edge.a.equals(start)) {
                list.add(0, edge.b);
                start = edge.b;
            } else if (edge.a.equals(end)) {
                list.add(edge.b);
                end = edge.b;
            } else if (edge.b.equals(start)) {
                list.add(0, edge.a);
                start = edge.a;
            } else if (edge.b.equals(end)) {
                list.add(edge.a);
                end = edge.a;
            } else {
                return false;
            }
            return true;
        }

        private void onEnclosed() {
            if (!pool.isEmpty()) {
                throw new RuntimeException("fail to solve voronoi area at " + center.toString());
            }
            if (!start.equals(end)) {
                //throw new RuntimeException("voronoi area not enclosed at " + center.toString());
                System.out.println("voronoi area not enclosed at " + center.toString());
                enclosed = false;
            } else {
                enclosed = true;
                list.remove(0);
            }
            points = new Point[list.size()];
            list.toArray(points);
            list = null;
            pool = null;
        }

        public final Point center;
        public Point[] points;
        public boolean enclosed;

        private Point start, end;
        private List<Point> list;
        private List<Edge> pool;

    }

}
