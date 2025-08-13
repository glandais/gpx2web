package io.github.glandais.gpx.virtual.heartrate;

import com.thoughtworks.xstream.XStream;
import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.util.SmoothService;
import jakarta.inject.Singleton;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import smile.data.DataFrame;
import smile.data.formula.Formula;
import smile.regression.RandomForest;

@Slf4j
@Service
@Singleton
public class HRSimulator {

    private final XStream xstream = new XStream();

    private final SmoothService smoothService;

    private RandomForest randomForest;

    public HRSimulator(SmoothService smoothService) {
        this.smoothService = smoothService;

        xstream.setMode(XStream.ID_REFERENCES);
        xstream.allowTypesByWildcard(new String[] {"smile.**"});
        try {
            InputStream resourceAsStream = HRSimulator.class.getResourceAsStream("/hrmodel");
            GZIPInputStream gzipInputStream = new GZIPInputStream(resourceAsStream);
            try (ObjectInputStream objectInputStream = xstream.createObjectInputStream(gzipInputStream)) {
                this.randomForest = (RandomForest) objectInputStream.readObject();
            }
        } catch (Exception e) {
            log.error("Failed to load linearModel", e);
        }
    }

    @SneakyThrows
    public void train(final List<GPXPath> samples) {
        List<DataPoint> dataPoints = new ArrayList<>();
        for (GPXPath sample : samples) {
            getDataPoints(dataPoints, sample);
        }
        int ntrees = 200;
        int mtry = 0;
        int maxDepth = 20;
        int maxNodes = dataPoints.size() / 5;
        int nodeSize = 5;
        double subsample = 1.0;
        this.randomForest = RandomForest.fit(
                Formula.lhs("heartRate"),
                DataFrame.of(dataPoints),
                ntrees,
                mtry,
                maxDepth,
                maxNodes,
                nodeSize,
                subsample);
        System.out.println(randomForest);

        FileOutputStream fileOutputStream = new FileOutputStream("src/main/resources/hrmodel");
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(fileOutputStream);
        ObjectOutputStream objectOutputStream = xstream.createObjectOutputStream(gzipOutputStream);
        objectOutputStream.writeObject(randomForest);
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
            double hr = randomForest.predict(dataPoint);
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
