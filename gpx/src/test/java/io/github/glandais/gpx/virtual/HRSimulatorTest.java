package io.github.glandais.gpx.virtual;

import io.github.glandais.gpx.Context;
import io.github.glandais.gpx.data.GPX;
import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.io.read.GPXFileReader;
import io.github.glandais.gpx.virtual.heartrate.HRSimulator;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class HRSimulatorTest {

    @Test
    @Disabled
    @SneakyThrows
    void train() {
        GPXFileReader gpxFileReader = new GPXFileReader();

        List<GPXPath> hrPaths = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            hrPaths.add(getGpxPath(gpxFileReader, "/hr/hr" + i + ".gpx"));
        }
        HRSimulator hrSimulator = Context.INSTANCE.getHrSimulator();
        hrSimulator.train(hrPaths);
        testWithStatistics();
    }

    private static GPXPath getGpxPath(GPXFileReader gpxFileReader, String fileName) throws Exception {
        GPX gpxHr = gpxFileReader.parseGPX(GPXEnhancerTest.class.getResourceAsStream(fileName));
        return gpxHr.paths().get(0);
    }

    @Test
    @SneakyThrows
    void test() {
        HRSimulator hrSimulator = Context.INSTANCE.getHrSimulator();
        GPXFileReader gpxFileReader = new GPXFileReader();
        for (int i = 1; i <= 7; i++) {
            String fileName = "/hr/hr" + i + ".gpx";
            GPXPath path = getGpxPath(gpxFileReader, fileName);
            Context.INSTANCE.getGpxFileWriter().writeGPXPath(path, new File("target/hr-orig-" + i + ".gpx"), true);
            for (Point point : path.getPoints()) {
                point.setHeartRate(null);
            }
            hrSimulator.simulateHeartRate(path);
            Context.INSTANCE.getGpxFileWriter().writeGPXPath(path, new File("target/hr-guess-" + i + ".gpx"), true);
        }
    }

    static class Statistics {
        final String fileName;
        final int nSamples;
        final double mae;
        final double rmse;
        final double mape;
        final double r2;
        final double correlation;
        final double bias;

        Statistics(String fileName, List<Double> actual, List<Double> predicted) {
            this.fileName = fileName;
            this.nSamples = actual.size();

            // Calculate mean of actual values
            double meanActual =
                    actual.stream().mapToDouble(Double::doubleValue).average().orElse(0);

            // Calculate metrics
            double sumSquaredError = 0;
            double sumAbsoluteError = 0;
            double sumPercentageError = 0;
            double sumBias = 0;
            double ssTotal = 0;

            for (int i = 0; i < actual.size(); i++) {
                double actualVal = actual.get(i);
                double predictedVal = predicted.get(i);
                double error = actualVal - predictedVal;

                sumSquaredError += error * error;
                sumAbsoluteError += Math.abs(error);
                if (actualVal != 0) {
                    sumPercentageError += Math.abs(error / actualVal) * 100;
                }
                sumBias += error;
                ssTotal += Math.pow(actualVal - meanActual, 2);
            }

            this.mae = sumAbsoluteError / nSamples;
            this.rmse = Math.sqrt(sumSquaredError / nSamples);
            this.mape = sumPercentageError / nSamples;
            this.bias = sumBias / nSamples;

            // Calculate R²
            double ssResidual = sumSquaredError;
            this.r2 = 1 - (ssResidual / ssTotal);

            // Calculate correlation
            double meanPredicted = predicted.stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0);
            double covariance = 0;
            double varActual = 0;
            double varPredicted = 0;

            for (int i = 0; i < actual.size(); i++) {
                double diffActual = actual.get(i) - meanActual;
                double diffPredicted = predicted.get(i) - meanPredicted;
                covariance += diffActual * diffPredicted;
                varActual += diffActual * diffActual;
                varPredicted += diffPredicted * diffPredicted;
            }

            this.correlation = covariance / Math.sqrt(varActual * varPredicted);
        }

        @Override
        public String toString() {
            return String.format(
                    "%-10s | n=%5d | MAE=%6.2f | RMSE=%6.2f | MAPE=%6.2f%% | R²=%6.4f | Corr=%6.4f | Bias=%6.2f",
                    fileName, nSamples, mae, rmse, mape, r2, correlation, bias);
        }
    }

    @Test
    @SneakyThrows
    void testWithStatistics() {
        HRSimulator hrSimulator = Context.INSTANCE.getHrSimulator();
        GPXFileReader gpxFileReader = new GPXFileReader();

        List<Statistics> allStats = new ArrayList<>();
        List<Double> allActual = new ArrayList<>();
        List<Double> allPredicted = new ArrayList<>();

        System.out.println("\n=== Random Forest Heart Rate Prediction Performance ===\n");
        System.out.println(
                "File       | Samples | MAE (bpm) | RMSE (bpm) | MAPE (%) | R²     | Correlation | Bias (bpm)");
        System.out.println(
                "-----------|---------|-----------|------------|----------|--------|-------------|----------");

        for (int i = 1; i <= 7; i++) {
            String fileName = "hr" + i;
            GPXPath pathOriginal = getGpxPath(gpxFileReader, "/hr/" + fileName + ".gpx");

            // Store original HR values
            List<Double> actualHR = new ArrayList<>();
            for (Point point : pathOriginal.getPoints()) {
                if (point.getHeartRate() != null && point.getHeartRate() > 0) {
                    actualHR.add(point.getHeartRate());
                }
            }

            // Clear HR and simulate
            GPXPath pathSimulated = getGpxPath(gpxFileReader, "/hr/" + fileName + ".gpx");
            for (Point point : pathSimulated.getPoints()) {
                point.setHeartRate(null);
            }
            hrSimulator.simulateHeartRate(pathSimulated);

            // Collect predicted HR values
            List<Double> predictedHR = new ArrayList<>();
            int idx = 0;
            for (Point point : pathSimulated.getPoints()) {
                if (idx < actualHR.size() && point.getHeartRate() != null) {
                    predictedHR.add(point.getHeartRate());
                    idx++;
                }
            }

            // Ensure same size
            int minSize = Math.min(actualHR.size(), predictedHR.size());
            actualHR = actualHR.subList(0, minSize);
            predictedHR = predictedHR.subList(0, minSize);

            // Calculate statistics for this file
            Statistics stats = new Statistics(fileName, actualHR, predictedHR);
            allStats.add(stats);
            System.out.println(stats);

            // Add to overall dataset
            allActual.addAll(actualHR);
            allPredicted.addAll(predictedHR);
        }

        // Calculate overall statistics
        System.out.println(
                "-----------|---------|-----------|------------|----------|--------|-------------|----------");
        Statistics overallStats = new Statistics("OVERALL", allActual, allPredicted);
        System.out.println(overallStats);

        System.out.println("\n=== Metrics Explanation ===");
        System.out.println("MAE:  Mean Absolute Error - Average absolute difference between predicted and actual HR");
        System.out.println("RMSE: Root Mean Square Error - Square root of average squared differences");
        System.out.println("MAPE: Mean Absolute Percentage Error - Average percentage error");
        System.out.println("R²:   Coefficient of Determination - Proportion of variance explained (1.0 = perfect)");
        System.out.println("Corr: Pearson Correlation - Linear relationship strength (-1 to 1)");
        System.out.println("Bias: Mean prediction error (negative = underestimation)");

        System.out.println("\n=== Model Performance Summary ===");
        System.out.printf("The Random Forest model achieves:\n");
        System.out.printf("- Overall R² of %.4f (%.1f%% variance explained)\n", overallStats.r2, overallStats.r2 * 100);
        System.out.printf("- Mean Absolute Error of %.2f bpm\n", overallStats.mae);
        System.out.printf("- Correlation of %.4f with actual HR\n", overallStats.correlation);
        System.out.printf("- Average bias of %.2f bpm\n", overallStats.bias);
    }
}
