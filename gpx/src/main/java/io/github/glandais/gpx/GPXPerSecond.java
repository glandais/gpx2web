package io.github.glandais.gpx;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GPXPerSecond {

    public void computeOnePointPerSecond(GPXPath path) {
        log.info("A point per second for {}", path.getName());
        List<Point> points = path.getPoints();
        List<Point> newPoints = new ArrayList<>();

        long[] time = path.getTime();
        long s = 1000 * (long) Math.ceil(time[0] / 1000.0);
        long e = 1000 * (long) Math.floor(time[time.length - 1] / 1000.0);
        int i = 0;

        while (s <= e) {
            while ((i + 1) < time.length && time[i + 1] < s) {
                i++;
            }
            if ((i + 1) < time.length) {
                if (time[i] <= s && s <= time[i + 1]) {
                    Point p = points.get(i);
                    if (time[i + 1] - time[i] > 1) {
                        double c = (s - time[i]) / (1.0 * time[i + 1] - time[i]);
                        Point pp1 = points.get(i + 1);

                        Point point = getPoint(p, pp1, c, s);
                        newPoints.add(point);
                    } else {
                        newPoints.add(p);
                    }
                } else {
                    log.error("strange");
                }
            }
            s = s + 1000;
        }

        path.setPoints(newPoints);
        log.info("Done - a point per second for {}", path.getName());
    }

    public static Point getPoint(Point p, Point pp1, double coef, long epochMillis) {
        double lon = p.getLon() + coef * (pp1.getLon() - p.getLon());
        double lat = p.getLat() + coef * (pp1.getLat() - p.getLat());
        double z = p.getZ() + coef * (pp1.getZ() - p.getZ());

        Map<String, Double> data = new HashMap<>();
        for (String key : p.getData().keySet()) {
            Double v = p.getData().get(key);
            Double vp1 = pp1.getData().get(key);
            Double nv;
            if (v != null && vp1 != null) {
                nv = v + coef * (vp1 - v);
            } else if (v != null) {
                nv = v;
            } else {
                nv = vp1;
            }
            if (nv != null) {
                data.put(key, nv);
            }
        }

        Point point = Point.builder().lon(lon).lat(lat).z(z).data(data)
                .time(Instant.ofEpochMilli(epochMillis)).build();
        point.getData().put("coef", coef);
        return point;
    }

}
