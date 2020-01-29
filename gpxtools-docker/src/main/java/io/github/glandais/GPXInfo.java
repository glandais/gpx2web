package io.github.glandais;

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

}
