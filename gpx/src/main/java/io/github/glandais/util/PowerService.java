package io.github.glandais.util;

import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.Point;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class PowerService {

    public void smoothPower(GPXPath path) {
        log.info("Smoothing power");
        List<Point> points = path.getPoints();
        double[] power = new double[points.size()];
        double[] time = new double[points.size()];
        for (int i = 0; i < points.size(); i++) {
            Double power1 = points.get(i).getPower();
            power[i] = power1 == null ? 0 : power1;
            time[i] = points.get(i).getEpochMilli();
        }
        for (int j = 0; j < power.length; j++) {
            double newPower = SmootherService.computeNewValue(j, 5000, power, time);
            Point p = points.get(j);
            p.setPower(newPower);
        }
        path.computeArrays();
        log.info("Smoothed power");
    }

}
