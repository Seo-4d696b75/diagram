package jp.seo.station.app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import jp.seo.diagram.core.*;

import java.io.*;
import java.util.ArrayList;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DiagramCalc {

    public static void main(String[] args) {
        new DiagramCalc(args[0], args[1]);
    }

    private DiagramCalc(String srcFile, String dstFile) {
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
            for (Station s : list) {
                s.voronoi = diagram.getVoronoiArea(s);
                s.next = new ArrayList<>();
            }
            for (Edge edge : diagram.getDelaunayEdges()) {
                DiagramCalc.Station s1 = (DiagramCalc.Station) edge.a;
                DiagramCalc.Station s2 = (DiagramCalc.Station) edge.b;
                s1.next.add(s2.code);
                s2.next.add(s1.code);
            }

            System.out.println("Kd-tree");
            KdTree<DiagramCalc.Station> tree = new KdTree<>(list);
            this.traverseTree(tree.getRoot());
            String data = (new DiagramCalc.StationTree(tree.getRoot().point.code, list)).toJson();
            this.save(data, new File(dstFile));
        } catch (IOException e) {
            e.printStackTrace();
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
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
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
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
        writer.write(data);
        writer.close();
    }

    private static class Station extends Point {
        @Expose
        final private double lat;
        @Expose
        final private double lng;
        @Expose
        final private int code;
        @Expose
        final private String name;
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
