package io.github.glandais;

import io.github.glandais.gpx.util.Vector;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GPXInfo {

    private float distance;

    private int positiveElevation;

    private int negativeElevation;

    private Vector wind;

    private boolean crossing;

}
