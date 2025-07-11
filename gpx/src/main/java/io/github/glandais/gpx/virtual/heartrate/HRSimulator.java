package io.github.glandais.gpx.virtual.heartrate;

import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.util.SmoothService;
import jakarta.inject.Singleton;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import smile.data.DataFrame;
import smile.data.formula.Formula;
import smile.regression.LinearModel;
import smile.regression.OLS;

@Slf4j
@Service
@Singleton
public class HRSimulator {

    private final SmoothService smoothService;

    private LinearModel linearModel;

    public HRSimulator(SmoothService smoothService) {
        this.smoothService = smoothService;

        try {
            try (ObjectInputStream objectInputStream =
                    new ObjectInputStream(HRSimulator.class.getResourceAsStream("/hrmodel"))) {
                this.linearModel = (LinearModel) objectInputStream.readObject();
            }
        } catch (Exception e) {
            log.error("Failed to load linearModel");
        }
    }

    @SneakyThrows
    public void train(final List<GPXPath> samples) {
        List<DataPoint> dataPoints = new ArrayList<>();
        for (GPXPath sample : samples) {
            getDataPoints(dataPoints, sample);
        }
        this.linearModel = OLS.fit(Formula.lhs("hr"), DataFrame.of(dataPoints));
        System.out.println(linearModel);

        FileOutputStream fileOutputStream = new FileOutputStream("src/main/resources/hrmodel");
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(linearModel);
        objectOutputStream.flush();
        objectOutputStream.close();
    }

    public void simulateHeartRate(final GPXPath gpxPath) {
        List<Point> points = gpxPath.getPoints();
        for (int i = 0; i < points.size() - 1; i++) {
            double t = points.get(i).getElapsedSeconds();
            if (i == 0) {
                points.get(i).setHeartRate(100.0);
            }
            DataPoint dataPoint = getDataPoint(gpxPath, t);
            double hr = linearModel.predict(dataPoint);
            points.get(i).setHeartRate(hr);
        }
        smoothService.smoothHr(gpxPath);
    }

    private void getDataPoints(List<DataPoint> dataPoints, GPXPath sample) {
        if (sample.getPoints().isEmpty()) {
            return;
        }
        double duration = sample.getElapsedSeconds()[sample.getPoints().size() - 1];
        int n = (int) duration / 5;
        for (int i = 0; i < n; i++) {
            int t = i * 5;
            dataPoints.add(getDataPoint(sample, t));
        }
    }

    private DataPoint getDataPoint(GPXPath gpxPath, double t) {
        return new DataPoint(gpxPath, t);
    }
}
