package org.glandais.gpx.braquet.db;

public enum Pedalier {

	VTT_SHIMANO(Compat.SHIMANO_9, 22, 32, 42),

	VTT_SRAM_XX_1(Compat.SRAM_10, 26, 39),

	VTT_SRAM_XX_2(Compat.SRAM_10, 28, 42),

//	VTT_SRAM_XX_3(Compat.SRAM_10, 30, 45), // ?

	VTT_SRAM_X0_1(Compat.SRAM_10, 22, 36),

	VTT_SRAM_X0_2(Compat.SRAM_10, 24, 38),

	VTT_SRAM_X7_3(Compat.SRAM_10, 22, 33, 44),

	VTT_SRAM_APEX_1(Compat.SRAM_10, 34, 48),

	VTT_SRAM_APEX_2(Compat.SRAM_10, 34, 50),

	VTT_SRAM_APEX_3(Compat.SRAM_10, 39, 53),

	;

	public int[] plateaux;

	// private Compat[] compat;
	public Compat compat;

	Pedalier(Compat compat, int... plateaux) {
		// this.compat = new Compat[] { compat };
		this.compat = compat;
		this.plateaux = plateaux;
	}

	// Pedalier(Compat[] compat, int... plateaux) {
	// this.compat = compat;
	// this.plateaux = plateaux;
	// }

}
