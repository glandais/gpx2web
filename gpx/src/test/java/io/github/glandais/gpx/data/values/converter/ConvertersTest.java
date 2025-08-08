package io.github.glandais.gpx.data.values.converter;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import org.junit.jupiter.api.Test;

class ConvertersTest {

    @Test
    void testDegreesConverter() {
        DegreesConverter converter = Converters.DEGREES_CONVERTER;

        // Test conversion to storage (degrees to radians)
        Double radians = converter.convertToStorage(45.0);
        assertEquals(Math.toRadians(45.0), radians, 0.00001);

        // Test conversion from storage (radians to degrees)
        Double degrees = converter.convertFromStorage(Math.PI / 4);
        assertEquals(45.0, degrees, 0.00001);

        // Test null handling
        assertNull(converter.convertToStorage(null));
        assertNull(converter.convertFromStorage(null));

        // Test edge cases
        assertEquals(0.0, converter.convertToStorage(0.0));
        assertEquals(Math.PI, converter.convertToStorage(180.0), 0.00001);
        assertEquals(-Math.PI, converter.convertToStorage(-180.0), 0.00001);
        assertEquals(2 * Math.PI, converter.convertToStorage(360.0), 0.00001);

        // Test inverse operations
        double original = 123.456;
        double converted = converter.convertToStorage(original);
        double backConverted = converter.convertFromStorage(converted);
        assertEquals(original, backConverted, 0.00001);
    }

    @Test
    void testSemiCirclesConverter() {
        SemiCirclesConverter converter = Converters.SEMI_CIRCLES_CONVERTER;

        // Test conversion to storage (semicircles to radians)
        int semiCircles = 536870912; // 45 degrees in semicircles
        Double radians = converter.convertToStorage(semiCircles);
        assertEquals(Math.toRadians(45.0), radians, 0.00001);

        // Test conversion from storage (radians to semicircles)
        Integer semi = converter.convertFromStorage(Math.PI / 4);
        assertEquals(536870912, semi, 1000); // Allow small rounding error

        // Test null handling
        assertNull(converter.convertToStorage(null));
        assertNull(converter.convertFromStorage(null));

        // Test edge cases
        assertEquals(0.0, converter.convertToStorage(0), 0.00001);

        // Test 180 degrees
        int semi180 = (int) ((1L << 31) - 1); // Max positive value
        Double rad180 = converter.convertToStorage(semi180);
        assertTrue(Math.abs(rad180 - Math.PI) < 0.001);

        // Test -180 degrees
        int semiNeg180 = Integer.MIN_VALUE;
        Double radNeg180 = converter.convertToStorage(semiNeg180);
        assertTrue(Math.abs(radNeg180 + Math.PI) < 0.001);

        // Test round trip
        int originalSemi = 123456789;
        Double rad = converter.convertToStorage(originalSemi);
        Integer backSemi = converter.convertFromStorage(rad);
        assertEquals(originalSemi, backSemi, 1); // Allow rounding error of 1
    }

    @Test
    void testDurationSecondsConverter() {
        DurationSecondsConverter converter = Converters.DURATION_SECONDS_CONVERTER;

        // Test conversion to storage (seconds to Duration)
        Duration duration = converter.convertToStorage(30.5);
        assertEquals(Duration.ofSeconds(30, 500_000_000), duration);

        // Test conversion from storage (Duration to seconds)
        Double seconds = converter.convertFromStorage(Duration.ofSeconds(45, 678_000_000));
        assertEquals(45.678, seconds, 0.00001);

        // Test null handling
        assertNull(converter.convertToStorage(null));
        assertNull(converter.convertFromStorage(null));

        // Test edge cases
        assertEquals(Duration.ZERO, converter.convertToStorage(0.0));
        assertEquals(0.0, converter.convertFromStorage(Duration.ZERO));

        // Test negative values
        assertEquals(Duration.ofSeconds(-1), converter.convertToStorage(-1.0));
        assertEquals(-1.0, converter.convertFromStorage(Duration.ofSeconds(-1)));

        // Test precision
        Double precise = 123.456789;
        Duration durationP = converter.convertToStorage(precise);
        Double backP = converter.convertFromStorage(durationP);
        assertEquals(precise, backP, 0.000001);

        // Test large values
        Double large = 86400.0; // 1 day in seconds
        Duration largeDuration = converter.convertToStorage(large);
        assertEquals(Duration.ofDays(1), largeDuration);
        assertEquals(large, converter.convertFromStorage(largeDuration));
    }

    @Test
    void testDateConverter() {
        DateConverter converter = Converters.DATE_CONVERTER;

        // Test conversion to storage (Date to Instant)
        Date now = new Date();
        Instant stored = converter.convertToStorage(now);
        assertEquals(now.toInstant(), stored);

        // Test conversion from storage (Instant to Date)
        Instant instant = Instant.now();
        Date retrieved = converter.convertFromStorage(instant);
        assertEquals(Date.from(instant), retrieved);

        // Test null handling
        assertNull(converter.convertToStorage(null));
        assertNull(converter.convertFromStorage(null));

        // Test specific dates
        Date epochDate = new Date(0);
        Instant epochInstant = Instant.EPOCH;
        assertEquals(epochInstant, converter.convertToStorage(epochDate));
        assertEquals(epochDate, converter.convertFromStorage(epochInstant));

        Instant specific = Instant.parse("2025-01-01T12:00:00Z");
        Date specificDate = Date.from(specific);
        assertEquals(specific, converter.convertToStorage(specificDate));
        assertEquals(specificDate, converter.convertFromStorage(specific));
    }

    @Test
    void testNoopConverter() {
        NoopConverter<Double, ?> doubleConverter = Converters.NOOP_CONVERTER;

        // Test with Double
        Double num = 42.0;
        assertSame(num, doubleConverter.convertToStorage(num));
        assertSame(num, doubleConverter.convertFromStorage(num));

        // Test with null
        assertNull(doubleConverter.convertToStorage(null));
        assertNull(doubleConverter.convertFromStorage(null));

        // Test creating generic noop converter for other types
        NoopConverter<String, ?> stringConverter = new NoopConverter<>();
        String str = "test";
        assertSame(str, stringConverter.convertToStorage(str));
        assertSame(str, stringConverter.convertFromStorage(str));
    }

    @Test
    void testConvertersConstants() {
        // Verify that the constants are properly initialized
        assertNotNull(Converters.DEGREES_CONVERTER);
        assertNotNull(Converters.SEMI_CIRCLES_CONVERTER);
        assertNotNull(Converters.DURATION_SECONDS_CONVERTER);
        assertNotNull(Converters.DATE_CONVERTER);
        assertNotNull(Converters.NOOP_CONVERTER);

        // Verify they are the expected types
        assertTrue(Converters.DEGREES_CONVERTER instanceof DegreesConverter);
        assertTrue(Converters.SEMI_CIRCLES_CONVERTER instanceof SemiCirclesConverter);
        assertTrue(Converters.DURATION_SECONDS_CONVERTER instanceof DurationSecondsConverter);
        assertTrue(Converters.DATE_CONVERTER instanceof DateConverter);
        assertTrue(Converters.NOOP_CONVERTER instanceof NoopConverter);
    }

    @Test
    void testConverterInterfaces() {
        // Test that all converters implement the Converter interface properly
        Converter<Double, ?, Double> degrees = Converters.DEGREES_CONVERTER;
        Converter<Double, ?, Integer> semi = Converters.SEMI_CIRCLES_CONVERTER;
        Converter<Duration, ?, Double> duration = Converters.DURATION_SECONDS_CONVERTER;
        Converter<Instant, ?, Date> date = Converters.DATE_CONVERTER;
        Converter<Double, ?, Double> noop = Converters.NOOP_CONVERTER;

        // Verify interface methods exist and work
        assertDoesNotThrow(() -> degrees.convertToStorage(45.0));
        assertDoesNotThrow(() -> degrees.convertFromStorage(Math.PI / 4));

        assertDoesNotThrow(() -> semi.convertToStorage(1000000));
        assertDoesNotThrow(() -> semi.convertFromStorage(0.1));

        assertDoesNotThrow(() -> duration.convertToStorage(10.0));
        assertDoesNotThrow(() -> duration.convertFromStorage(Duration.ofSeconds(10)));

        assertDoesNotThrow(() -> date.convertToStorage(new Date()));
        assertDoesNotThrow(() -> date.convertFromStorage(Instant.now()));

        assertDoesNotThrow(() -> noop.convertToStorage(42.0));
        assertDoesNotThrow(() -> noop.convertFromStorage(42.0));
    }
}
