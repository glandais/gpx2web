package io.github.glandais.braquet.db;

import java.util.Arrays;

public enum Pedalier {

	TRIPLE_VTT_1(22, 32, 44),

	TRIPLE_VTT_2(26, 36, 48),

	TRIPLE_ROUTE_1(24, 36, 48),

	TRIPLE_ROUTE_2(28, 36, 48),

	TRIPLE_ROUTE_3(30, 38, 48),

	TRIPLE_ROUTE_4(30, 39, 52), // Shimano

	DOUBLE_VTT_1(26, 39),

	DOUBLE_VTT_2(28, 42),

	DOUBLE_VTT_3(22, 36),

	DOUBLE_VTT_4(24, 38),

	DOUBLE_VTT_5(26, 38),

	DOUBLE_VTT_6(28, 40),

	DOUBLE_ROUTE_C1(34, 46),

	DOUBLE_ROUTE_C2(34, 48),

	DOUBLE_ROUTE_C3(34, 50),

	DOUBLE_ROUTE_1(39, 53),

	DOUBLE_ROUTE_1b(38, 53),

	DOUBLE_ROUTE_2(42, 52),

	;

	public int[] plateaux;

	Pedalier(int... plateaux) {
		this.plateaux = plateaux;
	}

	@Override
	public String toString() {
		return name() + " (" + Arrays.toString(plateaux) + ")";
	}

}
