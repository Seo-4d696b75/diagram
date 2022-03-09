package jp.seo.station.app;

import com.google.gson.*;
import jp.seo.diagram.core.Point;

import java.lang.reflect.Type;

public class PointConverter implements JsonSerializer<Point> {
    final double SCALE = 1000000.0D;

    public PointConverter() {
    }

    public JsonElement serialize(Point point, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonArray array = new JsonArray();
        if (point != null) {
            array.add((double)Math.round(point.getX() * SCALE) / SCALE);
            array.add((double)Math.round(point.getY() * SCALE) / SCALE);
            return array;
        } else {
            return JsonNull.INSTANCE;
        }
    }
}
