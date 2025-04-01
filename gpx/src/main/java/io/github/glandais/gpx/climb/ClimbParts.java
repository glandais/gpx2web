package io.github.glandais.gpx.climb;

import java.util.ArrayList;
import java.util.Collection;

public class ClimbParts extends ArrayList<ClimbPart> {
    public ClimbParts() {}

    public ClimbParts(Collection<? extends ClimbPart> c) {
        super(c);
    }
}
