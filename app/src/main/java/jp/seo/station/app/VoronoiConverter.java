package jp.seo.station.app;

import com.google.gson.*;
import jp.seo.diagram.core.Point;
import jp.seo.diagram.core.VoronoiDiagram;

import java.lang.reflect.Type;

public class VoronoiConverter implements JsonSerializer<VoronoiDiagram.VoronoiArea> {
    public VoronoiConverter() {
    }

    public JsonElement serialize(VoronoiDiagram.VoronoiArea src, Type type, JsonSerializationContext jsonSerializationContext) {
        if (src == null) {
            return JsonNull.INSTANCE;
        } else {
            JsonObject obj = new JsonObject();
            obj.addProperty("type", "Feature");
            obj.add("geometry", this.serializeGeometry(src));
            obj.add("properties", new JsonObject());
            return obj;
        }
    }

    private JsonElement serializeGeometry(VoronoiDiagram.VoronoiArea src) {
        JsonObject obj = new JsonObject();
        JsonArray array;
        if (src.enclosed) {
            obj.addProperty("type", "Polygon");
            array = new JsonArray();
            JsonArray ring = new JsonArray();
            PointConverter converter = new PointConverter();
            Point[] points = src.points;
            for (Point p : points) {
                ring.add(converter.serialize(p, null, null));
            }

            ring.add(converter.serialize(src.points[0], null, null));
            array.add(ring);
            obj.add("coordinates", array);
        } else {
            obj.addProperty("type", "LineString");
            array = new JsonArray();
            PointConverter converter = new PointConverter();
            Point[] points = src.points;
            for (Point p : points) {
                array.add(converter.serialize(p, null, null));
            }

            obj.add("coordinates", array);
        }

        return obj;
    }

}
