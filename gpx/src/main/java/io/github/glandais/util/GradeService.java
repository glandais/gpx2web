package io.github.glandais.util;

import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.Point;
import org.springframework.stereotype.Component;

@Component
public class GradeService {

    public void computeGrade(GPXPath path, String attribute) {
        for (int i = 0; i < path.size(); i++) {
            Point point = path.getPoints().get(i);
            double g;
            if (i == 0) {
                g = 0.0;
            } else {
                double d = path.getDists()[i] - path.getDists()[i - 1];
                double dz = path.getZs()[i] - path.getZs()[i - 1];
                if (d > 0.002) {
                    g = 100 * dz / d;
                } else {
                    g = path.getPoints().get(i - 1).getData().get(attribute);
                }
            }
            point.getData().put(attribute, g);
        }
    }

}
