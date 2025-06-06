package io.github.glandais.gpx.web.model;

public record VirtualizationResponse(String gpxContent, String jsonData, VirtualizationSummary summary) {

    public record VirtualizationSummary(double totalDistanceKm, double totalTimeSeconds, double averageSpeedKmH) {}
}
