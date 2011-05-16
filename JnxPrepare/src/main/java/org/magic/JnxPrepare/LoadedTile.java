package org.magic.JnxPrepare;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Date;

import javax.imageio.ImageIO;

public class LoadedTile {

	private MagicTile tile;
	private Date lastAccess;
	private BufferedImage tileImage;

	public LoadedTile(MagicTile magicTile) throws IOException {
		super();
		this.tile = magicTile;
		this.lastAccess = new Date();

		if (!tile.getImageFile().exists()) {
			tile.downloadImageFile();
		}
		tileImage = ImageIO.read(tile.getImageFile());
	}

	public MagicTile getTile() {
		return tile;
	}

	public void setTile(MagicTile tile) {
		this.tile = tile;
	}

	public Date getLastAccess() {
		return lastAccess;
	}

	public void setLastAccess(Date lastAccess) {
		this.lastAccess = lastAccess;
	}

	public BufferedImage getTileImage() {
		return tileImage;
	}

}
