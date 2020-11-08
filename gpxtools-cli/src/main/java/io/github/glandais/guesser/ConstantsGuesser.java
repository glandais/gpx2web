package io.github.glandais.guesser;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.Point;
import io.github.glandais.srtm.GPXElevationFixer;
import io.github.glandais.util.GradeService;
import io.github.glandais.util.SpeedService;
import io.github.glandais.virtual.Course;
import io.github.glandais.virtual.Cyclist;
import io.github.glandais.virtual.MaxSpeedComputer;
import io.github.glandais.virtual.PowerComputer;
import io.github.glandais.virtual.cx.CxProviderConstant;
import io.github.glandais.virtual.cyclist.PowerProviderFromData;
import io.github.glandais.virtual.wind.WindProviderNone;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
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

    public Course guessWithPathWithPower(GPXPath original, Cyclist cyclist) throws IOException {
        gpxElevationFixer.smoothZ(original, 100);
        gradeService.computeGrade(original);
        speedService.computeSpeed(original, "originalSpeed");

        CourseWithScore course = new CourseWithScore(original, Instant.now(), cyclist, new PowerProviderFromData(), new WindProviderNone(), new CxProviderConstant());

        String originalJson = objectMapper.writeValueAsString(original);
        GPXPath simulated = objectMapper.readValue(originalJson, GPXPath.class);

        setScore(original, simulated, course);
        log.info("{}", cyclist);

        ConstantRange cxRange = new ConstantRange(0.05, 0.35);
        ConstantRange fRange = new ConstantRange(0.0002, 0.015);
        ConstantRange mRange = new ConstantRange(50.0, 100.0);

        printRanges(cxRange, fRange, mRange);

        CourseWithScore minCourse = null;
        int nSteps = 5;

        for (int l = 0; l < 4; l++) {
            minCourse = null;
            log.info("******************** {}", l);
            for (int i = 0; i < nSteps; i++) {
                double cx = cxRange.getValue(i, nSteps);
                for (int j = 0; j < nSteps; j++) {
                    double f = fRange.getValue(j, nSteps);
                    for (int k = 0; k < nSteps; k++) {
                        double m = mRange.getValue(k, nSteps);

                        Cyclist curCyclist = new Cyclist(m, 15, 90, 0.3, f);
                        CourseWithScore current = new CourseWithScore(original, Instant.now(), curCyclist,
                                new PowerProviderFromData(),
                                new WindProviderNone(),
                                new CxProviderConstant(cx));
                        simulated = objectMapper.readValue(originalJson, GPXPath.class);

                        setScore(original, simulated, current);

                        log.debug("{}", current);
                        if (minCourse == null || current.getScore() < minCourse.getScore()) {
                            minCourse = current;
                        }
                    }
                }
            }
            double cx = ((CxProviderConstant) minCourse.getCxProvider()).getCx();
            double f = minCourse.getCyclist().getF();
            double mKg = minCourse.getCyclist().getMKg();
            cxRange = new ConstantRange(cx - cxRange.getStep(nSteps),
                    cx + cxRange.getStep(nSteps));
            fRange = new ConstantRange(f - fRange.getStep(nSteps),
                    f + fRange.getStep(nSteps));
            mRange = new ConstantRange(mKg - mRange.getStep(nSteps),
                    mKg + mRange.getStep(nSteps));
            printRanges(cxRange, fRange, mRange);
        }

        return minCourse;
    }

    private void printRanges(ConstantRange cxRange, ConstantRange fRange, ConstantRange mRange) {
        log.info("cx {}", cxRange);
        log.info("f {}", fRange);
        log.info("m {}", mRange);
    }

    private void setScore(GPXPath original, GPXPath simulated, CourseWithScore course) {
        maxSpeedComputer.computeMaxSpeeds(course);
        powerComputer.computeTrack(course);
        speedService.computeSpeed(simulated, "speed");
        double scoreCx = getScore(original, simulated, g -> Math.abs(g) < 0.5);
        double scoreM = getScore(original, simulated, g -> Math.abs(g) > 2.5);
        double scoreF = getScore(original, simulated, g -> true);
        course.setScore(scoreCx * scoreM * scoreF);
    }

    private double getScore(GPXPath original, GPXPath simulated, Predicate<Double> predicate) {
        double s = 0;
        for (int i = 0; i < original.size(); i++) {
            Point originalPoint = original.getPoints().get(i);
            if (i > 0 && predicate.test(originalPoint.getGrade())) {
                Point simulatedPoint = simulated.getPoints().get(i);
                double dv = Math
                        .abs(originalPoint.getSpeed() - simulatedPoint.getSpeed());
                double dt = original.getTime()[i] - original.getTime()[i - 1];
                s = s + (dv * dt / 1000.0);
            }
        }
        return s;
    }
}
