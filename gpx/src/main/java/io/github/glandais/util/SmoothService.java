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

    public void smoothCx(GPXPath path) {
        log.info("Smoothing cx");
        List<Point> points = path.getPoints();
        double[] cx = new double[points.size()];
        double[] time = new double[points.size()];
        for (int i = 0; i < points.size(); i++) {
            Double power1 = points.get(i).getData().get("cx", Unit.CX);
            cx[i] = power1 == null ? 0 : power1;
            time[i] = points.get(i).getEpochMilli();
        }
        for (int j = 0; j < cx.length; j++) {
            double newCx = SmootherService.computeNewValue(j, 30000, cx, time);
            Point p = points.get(j);
            p.put("cx", newCx, Unit.CX);
        }
        path.computeArrays();
        log.info("Smoothed cx");
    }
}
