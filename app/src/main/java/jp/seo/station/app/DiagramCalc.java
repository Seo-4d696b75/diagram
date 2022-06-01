package jp.seo.station.app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import jp.seo.diagram.core.*;
import jp.seo.diagram.core.Point;
import jp.seo.diagram.core.Rectangle;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DiagramCalc {

    public static void main(String[] args) {
        new DiagramCalc(args[0]);
    }

    private DiagramCalc(String srcFile) {
        try {
            List<DiagramCalc.Station> list = new GsonBuilder()
                    .serializeNulls()
                    .create()
                    .fromJson(
                            this.read(new File(srcFile)),
                            (new TypeToken<List<Station>>() {
                            }).getType()
                    );
            System.out.println("station size:" + list.size());
            VoronoiDiagram diagram = new VoronoiDiagram(list);
            diagram.split(new Rectangle(112.0D, 60.0D, 160.0D, 20.0D));
            System.out.println("adding edges. size : " + diagram.getEdges().size());

            JFrame window = new JFrame();
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setSize(1024, 1024);
            window.getContentPane().add(new MyCanvas(diagram));
            window.setVisible(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class MyCanvas extends JComponent {
        MyCanvas(VoronoiDiagram diagram) {
            this.diagram = diagram;
        }

        private final VoronoiDiagram diagram;

        @Override
        public void paint(Graphics g) {
            g.setColor(Color.BLACK);
            for (Edge e : diagram.getEdges()) {
                drawLine(g, e.a, e.b);
            }
        }

        final static int WIDTH = 1024;
        final static int HEIGHT = 1024;

        final static double EAST = 163.0;
        final static double WEST = 110.0;
        final static double SOUTH = 10.0;
        final static double NORTH = 63.0;

        private void drawLine(Graphics g, Point p1, Point p2) {
            int x1 = (int) ((p1.getX() - WEST) / (EAST - WEST) * WIDTH);
            int x2 = (int) ((p2.getX() - WEST) / (EAST - WEST) * WIDTH);
            int y1 = (int) ((NORTH - p1.getY()) / (NORTH - SOUTH) * HEIGHT);
            int y2 = (int) ((NORTH - p2.getY()) / (NORTH - SOUTH) * HEIGHT);
            g.drawLine(x1, y1, x2, y2);
        }
    }

    private void traverseTree(KdTree.Node<Station> node) {
        KdTree.Node<Station> left = node.getLeftChild();
        KdTree.Node<Station> right = node.getRightChild();
        if (left != null) {
            node.point.left = left.point.code;
            this.traverseTree(left);
        }

        if (right != null) {
            node.point.right = right.point.code;
            this.traverseTree(right);
        }

    }

    private String read(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        StringBuilder builder = new StringBuilder();
        String line = reader.readLine();

        do {
            builder.append(line);
            line = reader.readLine();
            if (line != null) {
                builder.append("\n");
            }
        } while (line != null);

        reader.close();
        return builder.toString();
    }

    private void save(String data, File file) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
        writer.write(data);
        writer.close();
    }

    private static class Station extends Point {
        @Expose
        private double lat;
        @Expose
        private double lng;
        @Expose
        private int code;
        @Expose
        private String name;
        @Expose(
                deserialize = false
        )
        private VoronoiDiagram.VoronoiArea voronoi;
        @Expose(
                deserialize = false
        )
        private Integer right;
        @Expose(
                deserialize = false
        )
        private Integer left;
        @Expose(
                deserialize = false
        )
        private List<Integer> next;

        Station(double lat, double lng, int code, String name) {
            this.lat = lat;
            this.lng = lng;
            this.code = code;
            this.name = name;
        }

        public double getX() {
            return this.lng;
        }

        public double getY() {
            return this.lat;
        }

        public String toString() {
            return String.format("%s(%d)", this.name, this.code);
        }
    }

    private static class StationTree {
        @SerializedName("root")
        int root;
        @SerializedName("node_list")
        List<DiagramCalc.Station> list;

        StationTree(int root, List<DiagramCalc.Station> list) {
            this.root = root;
            this.list = list;
        }

        String toJson() {
            StringBuilder builder = new StringBuilder();
            Gson gson = (new GsonBuilder()).registerTypeAdapter(VoronoiDiagram.VoronoiArea.class, new VoronoiConverter()).create();
            builder.append("{\n");
            builder.append("  \"root\":");
            builder.append(this.root);
            builder.append(",\n  \"node_list\":[\n    ");
            Stream<Station> stream = this.list.stream();
            Objects.requireNonNull(gson);
            builder.append(stream.map(gson::toJson).collect(Collectors.joining(",\n    ")));
            builder.append("\n  ]\n}");
            return builder.toString();
        }
    }
}
