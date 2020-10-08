package io.github.glandais.guesser;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.Point;
import io.github.glandais.srtm.GPXElevationFixer;
import io.github.glandais.util.GradeService;
import io.github.glandais.util.SpeedService;
import io.github.glandais.virtual.CourseWithPower;
import io.github.glandais.virtual.Cyclist;
import io.github.glandais.virtual.MaxSpeedComputer;
import io.github.glandais.virtual.PowerComputer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.function.Predicate;

@Service
@Slf4j
public class ConstantsGuesser {

    private final ObjectMapper objectMapper;

    private final PowerComputer powerComputer;

    private final GPXElevationFixer gpxElevationFixer;

    private final MaxSpeedComputer maxSpeedComputer;

    private final GradeService gradeService;

    private final SpeedService speedService;

    public ConstantsGuesser(final ObjectMapper objectMapper,
                            final PowerComputer powerComputer,
                            final GPXElevationFixer gpxElevationFixer,
                            final MaxSpeedComputer maxSpeedComputer,
                            final GradeService gradeService,
                            final SpeedService speedService) {

        this.objectMapper = objectMapper;
        this.powerComputer = powerComputer;
        this.gpxElevationFixer = gpxElevationFixer;
        this.maxSpeedComputer = maxSpeedComputer;
        this.gradeService = gradeService;
        this.speedService = speedService;
    }

    public Cyclist guessWithPathWithPower(GPXPath original) throws IOException {
        gpxElevationFixer.smoothZ(original, 0.1);
        gradeService.computeGrade(original, "grade");
        speedService.computeSpeed(original, "originalSpeed");

        CyclistWithScore cyclist = new CyclistWithScore(55, 0, 15, 90, 0.3, 0.16, 0.01);
        CourseWithPower course = new CourseWithPower(original, cyclist, ZonedDateTime.now());
        maxSpeedComputer.computeMaxSpeeds(course);

        String originalJson = objectMapper.writeValueAsString(original);

        GPXPath simulated = objectMapper.readValue(originalJson, GPXPath.class);
        setScore(original, simulated, cyclist);
        log.info("{}", cyclist);

        ConstantRange cxRange = new ConstantRange(0.05, 0.35);
        ConstantRange fRange = new ConstantRange(0.005, 0.015);
        ConstantRange mRange = new ConstantRange(50.0, 100.0);

        printRanges(cxRange, fRange, mRange);

        CyclistWithScore minCyclist = null;
        int nSteps = 5;

        for (int l = 0; l < 4; l++) {
            minCyclist = null;
            log.info("******************** {}", l);
            for (int i = 0; i < nSteps; i++) {
                double cx = cxRange.getValue(i, nSteps);
                for (int j = 0; j < nSteps; j++) {
                    double f = fRange.getValue(j, nSteps);
                    for (int k = 0; k < nSteps; k++) {
                        double m = mRange.getValue(k, nSteps);
                        CyclistWithScore c = new CyclistWithScore(m, 0, 15, 90, 0.3, cx, f);
                        simulated = objectMapper.readValue(originalJson, GPXPath.class);

                        setScore(original, simulated, c);

                        log.debug("{}", c);
                        if (minCyclist == null || c.getScore() < minCyclist.getScore()) {
                            minCyclist = c;
                        }
                    }
                }
            }
            cxRange = new ConstantRange(minCyclist.getCx() - cxRange.getStep(nSteps),
                    minCyclist.getCx() + cxRange.getStep(nSteps));
            fRange = new ConstantRange(minCyclist.getF() - fRange.getStep(nSteps),
                    minCyclist.getF() + fRange.getStep(nSteps));
            mRange = new ConstantRange(minCyclist.getMKg() - mRange.getStep(nSteps),
                    minCyclist.getMKg() + mRange.getStep(nSteps));
            printRanges(cxRange, fRange, mRange);
        }

        return minCyclist;
    }

    private void printRanges(ConstantRange cxRange, ConstantRange fRange, ConstantRange mRange) {
        log.info("cx {}", cxRange);
        log.info("f {}", fRange);
        log.info("m {}", mRange);
    }

    private void setScore(GPXPath original, GPXPath simulated, CyclistWithScore c) {
        CourseWithPower course;
        course = new CourseWithPower(simulated, c, ZonedDateTime.now());
        powerComputer.computeTrack(course);
        speedService.computeSpeed(simulated, "speed");
        double scoreCx = getScore(original, simulated, g -> Math.abs(g) < 0.5);
        double scoreM = getScore(original, simulated, g -> Math.abs(g) > 2.5);
        double scoreF = getScore(original, simulated, g -> true);
        c.setScore(scoreCx * scoreM * scoreF);
    }

    private double getScore(GPXPath original, GPXPath simulated, Predicate<Double> predicate) {
        double s = 0;
        for (int i = 0; i < original.size(); i++) {
            Point originalPoint = original.getPoints().get(i);
            if (i > 0 && predicate.test(originalPoint.getData().get("grade"))) {
                Point simulatedPoint = simulated.getPoints().get(i);
                double dv = Math
                        .abs(originalPoint.getData().get("originalSpeed") - simulatedPoint.getData().get("speed"));
                double dt = original.getTime()[i] - original.getTime()[i - 1];
                s = s + (dv * dt / 1000.0);
            }
        }
        return s;
    }
}
