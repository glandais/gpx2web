package io.github.glandais.gpx.virtual.maxspeed;

public class MaxSpeedComputerTest {
    /*
     * @SneakyThrows
     * @Test
     * @Disabled public void computeMaxSpeedsTest() { Constants.DEBUG = true; GPXPath gpxPath = new
     * GPXFileReader().parseGpx(MaxSpeedComputerTest.class.getResourceAsStream("/stelvio.gpx")).paths().get(0); Cyclist
     * cyclist = Cyclist.getDefault(); Bike bike = Bike.getDefault(); Course course = new Course(gpxPath, Instant.now(),
     * cyclist, bike, new PowerProviderConstant(), new WindProviderNone(), new AeroProviderConstant()); MaxSpeedComputer
     * maxSpeedComputer = new MaxSpeedComputer(); maxSpeedComputer.firstPass(course);
     * maxSpeedComputer.secondPass(course); for (Point point : course.getGpxPath().getPoints()) { String maxSpeed =
     * get(point, ValueKey.speed_max, Unit.SPEED_S_M); String vmaxIncline = get(point, ValueKey.speed_max_incline,
     * Unit.SPEED_S_M); if (maxSpeed.equalsIgnoreCase(vmaxIncline)) { System.out.print("*** "); }
     * System.out.println("dist : " + get(point, ValueKey.dist, Unit.METERS) + " - " + "speed_max : " + maxSpeed + " - "
     * + "radius : " + get(point, ValueKey.radius, Unit.METERS) + " - " + "speed_max_incline : " + vmaxIncline); }
     * Constants.DEBUG = false; } public <J> String get(Point point, ValueKey key, StorageUnit<J> unit) { J value =
     * point.get(key, unit); if (value != null) { return unit.formatHuman(value); } else { return "null"; } }
     */
}
