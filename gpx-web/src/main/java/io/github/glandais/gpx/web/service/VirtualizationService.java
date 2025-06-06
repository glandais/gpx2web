package io.github.glandais.gpx.web.service;

import io.github.glandais.gpx.data.GPX;
import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.io.read.GPXFileReader;
import io.github.glandais.gpx.io.write.GPXFileWriter;
import io.github.glandais.gpx.io.write.JsonFileWriter;
import io.github.glandais.gpx.virtual.Bike;
import io.github.glandais.gpx.virtual.Course;
import io.github.glandais.gpx.virtual.Cyclist;
import io.github.glandais.gpx.virtual.GPXEnhancer;
import io.github.glandais.gpx.virtual.power.aero.aero.AeroProviderConstant;
import io.github.glandais.gpx.virtual.power.aero.wind.Wind;
import io.github.glandais.gpx.virtual.power.aero.wind.WindProviderConstant;
import io.github.glandais.gpx.virtual.power.cyclist.PowerProviderConstant;
import io.github.glandais.gpx.web.model.VirtualizationRequest;
import io.github.glandais.gpx.web.model.VirtualizationResponse;
import io.github.glandais.gpx.web.virtual.PowerCurvePowerProvider;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.File;
import java.io.StringWriter;
import java.nio.file.Files;
import lombok.RequiredArgsConstructor;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@ApplicationScoped
@RequiredArgsConstructor
public class VirtualizationService {

    private final GPXFileReader gpxFileReader;
    private final GPXEnhancer gpxEnhancer;
    private final JsonFileWriter jsonFileWriter;
    private final GPXFileWriter gpxFileWriter;

    public VirtualizationResponse virtualizeGpx(FileUpload fileUpload, VirtualizationRequest request) throws Exception {
        GPX gpx = gpxFileReader.parseGPX(fileUpload.uploadedFile().toFile());

        // Validate single track
        if (gpx.paths().size() != 1) {
            throw new IllegalArgumentException("GPX file must contain exactly one track, found: "
                    + gpx.paths().size());
        }

        GPXPath gpxPath = gpx.paths().get(0);
        Cyclist cyclist = createCyclist(request.cyclist());
        Bike bike = createBike(request.bike());
        Wind wind = createWind(request.wind());

        // Create power provider based on power curve or use constant
        var powerProvider =
                (request.powerCurve() != null && !request.powerCurve().isEmpty())
                        ? new PowerCurvePowerProvider(request.powerCurve())
                        : new PowerProviderConstant();

        Course course = new Course(
                gpxPath,
                request.startTime(),
                cyclist,
                bike,
                powerProvider,
                new WindProviderConstant(wind),
                new AeroProviderConstant());

        gpxEnhancer.virtualize(course, false);

        // Generate JSON data for visualization
        File tempFile = File.createTempFile("gpx", ".json");
        jsonFileWriter.writeGPXPath(gpxPath, tempFile);
        String jsonData = Files.readString(tempFile.toPath());
        tempFile.delete();

        // Generate GPX content
        StringWriter gpxWriter = new StringWriter();
        gpxFileWriter.writeGPX(gpx, gpxWriter, true);
        String gpxContent = gpxWriter.toString();

        // Calculate summary statistics
        VirtualizationResponse.VirtualizationSummary summary = calculateSummary(gpxPath);

        return new VirtualizationResponse(gpxContent, jsonData, summary);
    }

    private Cyclist createCyclist(VirtualizationRequest.CyclistParameters params) {
        return new Cyclist(
                params.weightKg(),
                params.powerWatts(),
                params.harmonics(),
                params.maxBrakeG(),
                params.dragCoefficient(),
                params.frontalAreaM2(),
                params.maxAngleDeg(),
                params.maxSpeedKmH());
    }

    private Bike createBike(VirtualizationRequest.BikeParameters params) {
        return new Bike(
                params.rollingResistance(),
                params.frontWheelInertia(),
                params.rearWheelInertia(),
                params.wheelRadiusM(),
                params.efficiency());
    }

    private Wind createWind(VirtualizationRequest.WindParameters params) {
        double windDirectionRad = Math.toRadians(params.directionDeg());
        return new Wind(params.speedMs(), windDirectionRad);
    }

    private VirtualizationResponse.VirtualizationSummary calculateSummary(GPXPath gpxPath) {
        double totalDistanceKm = gpxPath.getDist() / 1000.0;

        // Calculate total time from first to last point
        var points = gpxPath.getPoints();
        if (points.isEmpty()) {
            return new VirtualizationResponse.VirtualizationSummary(0, 0, 0);
        }

        long totalTimeNanos = points.get(points.size() - 1).getElapsed().toNanos();
        double totalTimeSeconds = totalTimeNanos / 1_000_000_000.0;

        double averageSpeedKmH = totalTimeSeconds > 0 ? (totalDistanceKm / totalTimeSeconds) * 3600 : 0;

        return new VirtualizationResponse.VirtualizationSummary(totalDistanceKm, totalTimeSeconds, averageSpeedKmH);
    }
}
