package io.github.glandais.util;

import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.Point;
import io.github.glandais.gpx.PointField;
import io.github.glandais.gpx.storage.Unit;
import io.github.glandais.gpx.storage.ValueKind;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.inject.Singleton;
import java.util.List;

@Service
@Singleton
@Slf4j
public class SmoothService {

    public void smoothPower(GPXPath path) {
        smooth(path, PointField.power, PointField.time, 5, Unit.WATTS);
    }

    public void smoothCx(GPXPath path) {
        smooth(path, PointField.cx, PointField.dist, 100, Unit.CX);
    }

    public void smoothSpeed(GPXPath path) {
        smooth(path, PointField.speed, PointField.time, 10, Unit.SPEED_S_M);
    }

    public void smoothEle(GPXPath path, double buffer) {
        smooth(path, PointField.ele, PointField.dist, buffer, Unit.METERS);
        path.computeArrays(ValueKind.computed);
    }

    private void smooth(GPXPath path, PointField attribute, PointField over, double dist, Unit<Double> unit) {
        log.debug("Smoothing {}", attribute);
        List<Point> points = path.getPoints();
        double[] data = new double[points.size()];
        double[] time = new double[points.size()];
        for (int i = 0; i < points.size(); i++) {
            Double value = points.get(i).getCurrent(attribute, unit);
            data[i] = value == null ? 0 : value;
            if (over == PointField.time) {
                time[i] = points.get(i).getEpochSeconds();
            } else {
                time[i] = points.get(i).getCurrent(over, unit);
            }
        }
        for (int j = 0; j < data.length; j++) {
            double newValue = SmootherService.computeNewValue(j, dist, data, time);
            Point p = points.get(j);
            p.put(attribute, newValue, unit, ValueKind.smoothed);
        }
        log.debug("Smoothed {}", attribute);
    }

}
