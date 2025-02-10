package io.github.glandais.util;

import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.data.PointField;
import io.github.glandais.gpx.data.values.Unit;
import io.github.glandais.gpx.data.values.ValueKind;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
            Double value = points.get(i).get(attribute, unit);
            data[i] = value == null ? 0 : value;
            if (over == PointField.time) {
                time[i] = points.get(i).getEpochSeconds();
            } else {
                time[i] = points.get(i).get(over, unit);
            }
        }
        for (int j = 0; j < data.length; j++) {
            double newValue = computeNewValue(j, dist, data, time);
            Point p = points.get(j);
            p.put(attribute, newValue, unit, ValueKind.smoothed);
        }
        log.debug("Smoothed {}", attribute);
    }

    private double computeNewValue(int i, double dist, double[] data, double[] dists) {
        // double dsample = 1;

        double ac = dists[i];

        int mini = i - 1;
        while (mini >= 0 && (ac - dists[mini]) <= dist) {
            mini--;
        }
        mini++;

        int maxi = i + 1;
        while (maxi < data.length && (dists[maxi] - ac) <= dist) {
            maxi++;
        }

        double totc = 0;
        double totv = 0;
        for (int j = mini; j < maxi; j++) {
            double c = 1 - (Math.abs(dists[j] - ac) / dist);
            totc = totc + c;
            totv = totv + data[j] * c;
        }

        if (totc == 0) {
            return data[i];
        } else {
            return totv / totc;
        }

    }

}
