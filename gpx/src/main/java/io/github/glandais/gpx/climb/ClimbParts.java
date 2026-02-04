package io.github.glandais.gpx.climb;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public class ClimbParts extends ArrayList<ClimbPart> implements Serializable {
    public ClimbParts() {}

    public ClimbParts(Collection<? extends ClimbPart> c) {
        super(c);
    }
}
