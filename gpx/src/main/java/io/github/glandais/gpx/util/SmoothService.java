package io.github.glandais.gpx.util;

import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.data.values.PropertyKey;
import io.github.glandais.gpx.data.values.PropertyKeys;
import io.github.glandais.gpx.data.values.ValueKind;
import io.github.glandais.gpx.data.values.converter.Converter;
import io.github.glandais.gpx.data.values.converter.Converters;
import io.github.glandais.gpx.data.values.converter.NoopConverter;
import io.github.glandais.gpx.data.values.unit.Unit;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Singleton
@Slf4j
public class SmoothService {

    public void smoothPower(GPXPath path) {
        smoothTime(path, PropertyKeys.power, 5);
        path.computeArrays(ValueKind.computed);
    }

    public void smoothAeroCoef(GPXPath path) {
        smoothDist(path, PropertyKeys.aeroCoef, 100);
        path.computeArrays(ValueKind.computed);
    }

    public void smoothSpeed(GPXPath path) {
        smoothTime(path, PropertyKeys.speed, 10);
        path.computeArrays(ValueKind.computed);
    }

    public void smoothEle(GPXPath path, double buffer) {
        smoothDist(path, PropertyKeys.ele, buffer);
        path.computeArrays(ValueKind.computed);
    }

    private void smoothDist(GPXPath path, PropertyKey<Double, ?> attribute, double dist) {
        smooth(path, attribute, PropertyKeys.dist, new NoopConverter<>(), dist);
    }

    private void smoothTime(GPXPath path, PropertyKey<Double, ?> attribute, double dist) {
        smooth(path, attribute, PropertyKeys.time, Converters.EPOCH_SECONDS_CONVERTER, dist);
    }

    private <S, U extends Unit<S>> void smooth(GPXPath path, PropertyKey<Double, ?> attribute, PropertyKey<S, U> bufferOver, Converter<S, U, Double> converter, double dist) {
        log.debug("Smoothing {}", attribute);
        List<Point> points = path.getPoints();
        double[] data = new double[points.size()];
        double[] time = new double[points.size()];
        for (int i = 0; i < points.size(); i++) {
            Double value = points.get(i).get(attribute);
            data[i] = value == null ? 0 : value;
            time[i] = points.get(i).get(bufferOver, converter);
        }
        for (int j = 0; j < data.length; j++) {
            double newValue = computeNewValue(j, dist, data, time);
            Point p = points.get(j);
            p.put(attribute, ValueKind.smoothed, newValue);
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
