package org.glandais.gpx.braquet.db;

public enum Cassette {

	SHIMANO_11_32_9(Compat.SHIMANO_9, 11, 12, 14, 16, 18, 21, 24, 28, 32),

	SRAM_11_36_10(Compat.SRAM_10, 11, 12, 14, 16, 18, 21, 24, 28, 32, 36),

	SRAM_11_25_10(Compat.SRAM_10, 11, 12, 13, 14, 15, 17, 19, 21, 23, 25),

	SRAM_12_36_10(Compat.SRAM_10, 12, 13, 15, 17, 19, 22, 25, 28, 32, 36),

	SRAM_11_23_10(Compat.SRAM_10, 11, 12, 13, 14, 15, 16, 17, 19, 21, 23),

	SRAM_11_26_10(Compat.SRAM_10, 11, 12, 13, 14, 15, 17, 19, 21, 23, 26),

	SRAM_11_28_10(Compat.SRAM_10, 11, 12, 13, 14, 15, 17, 19, 22, 25, 28),

	SRAM_11_32_10(Compat.SRAM_10, 11, 12, 13, 15, 17, 19, 22, 25, 28, 32),

	SRAM_12_25_10(Compat.SRAM_10, 12, 13, 14, 15, 16, 17, 19, 21, 23, 25),

	SRAM_12_26_10(Compat.SRAM_10, 12, 13, 14, 15, 16, 17, 19, 21, 23, 26),

	SRAM_12_27_10(Compat.SRAM_10, 12, 13, 14, 15, 16, 17, 19, 21, 24, 27),

	SRAM_12_28_10(Compat.SRAM_10, 12, 13, 14, 15, 16, 17, 19, 22, 25, 28),

	SRAM_12_32_10(Compat.SRAM_10, 12, 13, 14, 15, 17, 19, 22, 25, 28, 32),

	;

	public int[] pignons;
	// private Compat[] compat;
	public Compat compat;

	Cassette(Compat compat, int... pignons) {
		// this.compat = new Compat[] { compat };
		this.compat = compat;
		this.pignons = pignons;
	}

	/*
		Cassette(Compat[] compat, int... pignons) {
			this.compat = compat;
			this.pignons = pignons;
		}
	*/
}
