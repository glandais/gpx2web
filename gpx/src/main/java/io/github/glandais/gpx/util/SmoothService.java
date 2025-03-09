package io.github.glandais.gpx.util;

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
        smooth(path, PointField.power, Unit.WATTS, PointField.time, null, 5);
        path.computeArrays(ValueKind.computed);
    }

    public void smoothAeroCoef(GPXPath path) {
        smooth(path, PointField.aeroCoef, Unit.AERO_COEF, PointField.dist, Unit.METERS, 100);
        path.computeArrays(ValueKind.computed);
    }

    public void smoothSpeed(GPXPath path) {
        smooth(path, PointField.speed, Unit.SPEED_S_M, PointField.time, null, 10);
        path.computeArrays(ValueKind.computed);
    }

    public void smoothEle(GPXPath path, double buffer) {
        smooth(path, PointField.ele, Unit.METERS, PointField.dist, Unit.METERS, buffer);
        path.computeArrays(ValueKind.computed);
    }

    private void smooth(GPXPath path, PointField attribute, Unit<Double> attributeUnit, PointField bufferOver, Unit<Double> bufferUnit, double dist) {
        log.debug("Smoothing {}", attribute);
        List<Point> points = path.getPoints();
        double[] data = new double[points.size()];
        double[] time = new double[points.size()];
        for (int i = 0; i < points.size(); i++) {
            Double value = points.get(i).get(attribute, attributeUnit);
            data[i] = value == null ? 0 : value;
            if (bufferOver == PointField.time) {
                time[i] = points.get(i).getEpochSeconds();
            } else {
                time[i] = points.get(i).get(bufferOver, bufferUnit);
            }
        }
        for (int j = 0; j < data.length; j++) {
            double newValue = computeNewValue(j, dist, data, time);
            Point p = points.get(j);
            p.put(attribute, newValue, attributeUnit, ValueKind.smoothed);
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
