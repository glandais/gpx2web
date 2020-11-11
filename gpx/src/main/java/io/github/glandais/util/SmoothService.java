package io.github.glandais.util;

import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.Point;
import io.github.glandais.gpx.storage.Unit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class SmoothService {

    public void smoothPower(GPXPath path) {
        smooth(path, "power", 5000);
    }

    public void smoothCx(GPXPath path) {
        smooth(path, "cx", 30000);
    }

    public void smoothSpeed(GPXPath path) {
        smooth(path, "speed", 10000);
    }

    private void smooth(GPXPath path, String attribute, double dist) {
        log.info("Smoothing {}", attribute);
        List<Point> points = path.getPoints();
        double[] data = new double[points.size()];
        double[] time = new double[points.size()];
        for (int i = 0; i < points.size(); i++) {
            Double value = points.get(i).getData().get(attribute, Unit.DOUBLE_ANY);
            data[i] = value == null ? 0 : value;
            time[i] = points.get(i).getEpochMilli();
        }
        for (int j = 0; j < data.length; j++) {
            double newValue = SmootherService.computeNewValue(j, dist, data, time);
            Point p = points.get(j);
            p.put(attribute, newValue, Unit.DOUBLE_ANY);
        }
        path.computeArrays();
        log.info("Smoothed {}", attribute);
    }

}
