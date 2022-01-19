package com.garmin.fit;

public class FitFieldBuilder {

    public static final Field BOUND_MAX_LAT = new Field("bound_max_position_lat",
            27,
            133,
            1.0D,
            0.0D,
            "semicircles",
            false,
            Profile.Type.SINT32);
    public static final Field BOUND_MAX_LON = new Field("bound_max_position_long",
            28,
            133,
            1.0D,
            0.0D,
            "semicircles",
            false,
            Profile.Type.SINT32);
    public static final Field BOUND_MIN_LAT = new Field("bound_min_position_lat",
            29,
            133,
            1.0D,
            0.0D,
            "semicircles",
            false,
            Profile.Type.SINT32);
    public static final Field BOUND_MIN_LON = new Field("bound_min_position_long",
            30,
            133,
            1.0D,
            0.0D,
            "semicircles",
            false,
            Profile.Type.SINT32);

}
