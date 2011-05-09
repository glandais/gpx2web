import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Tile {

	private String url;
	private String realUrl;
	private String name;
	private int x;
	private int y;
	private GPXPoint bg = new GPXPoint(0, 0);
	private GPXPoint bd = new GPXPoint(0, 0);
	private GPXPoint hd = new GPXPoint(0, 0);
	private GPXPoint hg = new GPXPoint(0, 0);
	private int width = -1;
	private int height = -1;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public GPXPoint getBg() {
		return bg;
	}

	public void setBg(GPXPoint bg) {
		this.bg = bg;
	}

	public GPXPoint getBd() {
		return bd;
	}

	public void setBd(GPXPoint bd) {
		this.bd = bd;
	}

	public GPXPoint getHd() {
		return hd;
	}

	public void setHd(GPXPoint hd) {
		this.hd = hd;
	}

	public GPXPoint getHg() {
		return hg;
	}

	public void setHg(GPXPoint hg) {
		this.hg = hg;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getWidth() {
		if (width == -1) {
			loadWidthAndHeight();
		}
		return width;
	}

	private void loadWidthAndHeight() {
		try {
			BufferedImage image = ImageIO.read(new File(MagicPopup.cacheFolder,
					getUrl()));
			width = image.getWidth();
			height = image.getHeight();
		} catch (IOException e) {
			width = 256;
			height = 256;
			e.printStackTrace();
		}
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		if (height == -1) {
			loadWidthAndHeight();
		}
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void setRealUrl(String realUrl) {
		this.realUrl = realUrl;
	}

	public String getRealUrl() {
		return realUrl;
	}

}
