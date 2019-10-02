package org.glandais.pbp;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.glandais.pbp.bean.Profile;
import org.glandais.pbp.bean.Profiles;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.glandais.gpx.GPXPath;
import lombok.extern.slf4j.Slf4j;
import net.e175.klaus.solarpositioning.AzimuthZenithAngle;
import net.e175.klaus.solarpositioning.DeltaT;
import net.e175.klaus.solarpositioning.Grena3;

@Slf4j
public class Movie {

	private static final Font FONT1 = new Font("Monospaced", Font.BOLD, 50);

	private static final Font FONT2 = new Font("Monospaced", Font.PLAIN, 20);

//	private static final double STEP = 2 * 3600 * 1000.0;// 5 * 60 * 1000.0;
	private static final double STEP = 5 * 60 * 1000.0;

	private static final List<Step> STEPS = Steps.INSTANCE.getSteps();

	private final static float DASH1[] = { 10.0f };
	private final static BasicStroke FULL = new BasicStroke(1.0f);
	private final static BasicStroke DASHED = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f,
			DASH1, 0.0f);

	private ObjectMapper objectMapper = new ObjectMapper();

	private Profiles profiles;

	private Map<String, Color> colors;

	private double totKm;
	private double elevationToPixelRatio = 0.3;
	private int[] elevation_x;
	private int[] elevation_y;

	private boolean absolute = true;

	public static void main(String[] args) throws IOException {
		Movie movie;

		movie = new Movie();
		movie.setAbsolute(false);
		movie.exportPictures(new File("output/rel"));

		movie = new Movie();
		movie.setAbsolute(true);
		movie.exportPictures(new File("output/abs"));
	}

	public Movie() throws IOException {
		super();
		Step lastStep = STEPS.get(STEPS.size() - 1);
		totKm = lastStep.getTotalDistanceEnd();

		computeElevation();

		this.profiles = objectMapper.readValue(new FileInputStream(new File("profiles.json")), Profiles.class);

		Set<String> bibs = new HashSet<>(this.profiles.keySet());
		for (String bib : bibs) {
			Profile profile = profiles.get(bib);
			if (profile.getSplits().isEmpty()) {
				profiles.remove(bib);
			} else {
				for (int i = 1; i < profile.getSplits().size(); i++) {
					double tim1 = getEpochMillis(profile, i - 1);
					double ti = getEpochMillis(profile, i);
					if (tim1 >= ti) {
						profiles.remove(bib);
						break;
					}
				}
			}
		}

		colors = new HashMap<>();
		for (Profile profile : profiles.values()) {
			colors.put(profile.getBib(), getColor(profile));
		}

	}

	private void computeElevation() {
		elevation_x = new int[1920];
		elevation_y = new int[1920];
		double d = 0.0;
		double s = totKm / 1920;
		int i = 0;
		for (Step step : STEPS) {
			d = step.getTotalDistanceStart();
			for (int j = 0; j < step.getPaths().length; j++) {
				GPXPath gpxPath = step.getPaths()[j];
				log.info("{} {}", gpxPath.getName(), d);
				double[] dists = gpxPath.getDists();
				double[] z = gpxPath.getZs();
				for (int k = 0; k < dists.length; k++) {
					double dist = d + dists[k];
					if (i == 0 || dist > s * i) {
						setElevation(i, z, k);
						i++;
					}
				}
				d = d + gpxPath.getDist();
			}
		}
	}

	private void setElevation(int i, double[] z, int k) {
		elevation_x[i] = i;
		elevation_y[i] = (int) (1079.0 - z[k] * elevationToPixelRatio);
	}

	public void setAbsolute(boolean absolute) {
		this.absolute = absolute;
	}

	public void exportPictures(File path) throws IOException {
		double minStart = Double.MAX_VALUE;
		double maxEnd = Double.MIN_VALUE;
		if (absolute) {
			for (Profile profile : profiles.values()) {
				double start = getEpochMillis(profile, 0);
				double dt = start / 1000.0;
				profile.getSplits().forEach(s -> s.setEpochTime(s.getEpochTime() - dt));
			}
		}
		for (Profile profile : profiles.values()) {
			minStart = Math.min(minStart, getEpochMillis(profile, 0));
			maxEnd = Math.max(maxEnd, getEpochMillis(profile, profile.getSplits().size() - 1));
		}
		log.info("{} {}", new Date((long) (minStart)), new Date((long) (maxEnd)));

		minStart = Math.floor(minStart / STEP) * STEP;
		long count = Math.round(1 + Math.ceil((maxEnd - minStart) / STEP));

		log.info("{} {}", new Date((long) (minStart)), count);

		for (int i = 0; i < count; i++) {
			double t = minStart + STEP * i;
			log.info("{} {}/{}", new Date((long) (t)), i, count);
			exportPicture(path, i, t);
		}
	}

	private void exportPicture(File path, int i, double t) throws IOException {

		BufferedImage image = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = image.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		Color sky = getSkyColor(t);

		graphics.setPaint(sky);
		graphics.fillRect(0, 0, image.getWidth(), image.getHeight());

		graphics.setColor(Color.WHITE);

		graphics.setStroke(DASHED);
		graphics.setFont(FONT2);

		int k = 0;
		for (Step step : STEPS) {
			int x = (int) ((1920) * (step.getTotalDistanceEnd() / totKm));
			graphics.drawLine(x, 1080, x, 150);
			graphics.drawString(step.getLabel(), x, 150 + 20 * (k % 2));
			k++;
		}

		int[] count = fillPicture(image, graphics, t);
		int total = count[0];
		int finish = count[1];

		graphics.setStroke(FULL);
		graphics.drawPolyline(elevation_x, elevation_y, elevation_x.length);

		graphics.setColor(Color.WHITE);

		graphics.setFont(FONT1);
		String text;
		if (absolute) {
			Duration duration = Duration.ofMillis((long) t);
			text = String.format("%02d:%02d:%02d", duration.toHours(), duration.toMinutesPart(),
					duration.toSecondsPart());
		} else {
			Date d = new Date((long) t);
			text = d.toString();
		}
		graphics.drawString(text, 10, 50);
		graphics.drawString(total + " on road", 1400, 50);
		graphics.drawString(finish + " finished", 1400, 100);

		String fileName = String.format("%04d.png", i);
		ImageIO.write(image, "png", new File(path, fileName));
	}

	private Color getSkyColor(double t) {
		Color sky;
		if (!absolute) {
			GregorianCalendar dateTime = new GregorianCalendar();
			dateTime.setTimeInMillis((long) t);
			AzimuthZenithAngle position = Grena3.calculateSolarPosition(dateTime, 48.353, 1.201,
					DeltaT.estimate(dateTime));
			log.info("{} {}", new Date((long) t), position);
			double zenithAngle = position.getZenithAngle();
			double zenithDay = 60;
			double zenithNight = 110;
			float minL = 0.1f;
			float maxL = 0.7f;
			float l;
			if (zenithAngle < zenithDay) {
				l = maxL;
			} else if (zenithAngle > zenithNight) {
				l = minL;
			} else {
				double c = (zenithNight - zenithAngle) / (zenithNight - zenithDay);
				l = (float) (minL + c * (maxL - minL));
			}
			int rgb = Color.HSBtoRGB(197.0f / 360.0f, 0.71f, l);
			sky = new Color(rgb);
		} else {
			sky = new Color(Color.HSBtoRGB(197.0f / 360.0f, 0.71f, 0.7f));
		}
		return sky;
	}

	private int[] fillPicture(BufferedImage image, Graphics2D graphics, double t) {
		int r = 8;
		int coefMul = 30;
		int[] count = new int[1920 / r];
		int total = 0;
		int finished = 0;
		for (Profile profile : profiles.values()) {
			RiderState state = getState(profile, t);
			if (state.getStatus() == RiderStatus.IN) {
				double km = state.getKm();
				int rgb = colors.get(profile.getBib()).getRGB();
				int x = (int) ((1920 / r) * (km / totKm));
				int y = count[x];
				count[x] = count[x] + 1;
				total++;
				for (int j = 0; j < r; j++) {
					for (int i = 0; i < (coefMul / r); i++) {
						int realx = x * r + j;
						int ry = 1080 - ((coefMul / r) * y + i);
						if (0 <= ry && ry < 1080) {
							image.setRGB(realx, ry, rgb);
						}
					}
				}
			} else if (state.getStatus() == RiderStatus.FINISHED) {
				finished++;
			}
		}
		return new int[] { total, finished };
	}

	private RiderState getState(Profile profile, double t) {
		for (int i = 1; i < profile.getSplits().size(); i++) {
			double tim1 = getEpochMillis(profile, i - 1);
			double ti = getEpochMillis(profile, i);
			if (tim1 <= t && t <= ti && (ti - tim1 > 0.0)) {
				double c = (t - tim1) / (ti - tim1);
				Step step = STEPS.get(i);
				double km = step.getTotalDistanceStart()
						+ c * (step.getTotalDistanceEnd() - step.getTotalDistanceStart());
				return new RiderState(RiderStatus.IN, step, km);
			}
		}
		if (profile.getSplits().size() == STEPS.size() && t > getEpochMillis(profile, STEPS.size() - 1)) {
			return new RiderState(RiderStatus.FINISHED, null, 0);
		}
		return new RiderState(RiderStatus.OUT, null, 0);
	}

	private double getEpochMillis(Profile profile, int i) {
		return 1000 * profile.getSplits().get(i).getEpochTime();
	}

	private Color getColor(Profile profile) {
		char first = 'A';
		int group = (short) profile.getBib().charAt(0) - ((short) first);
		float hue = group / 28.0f;

		int rgb = Color.HSBtoRGB(hue, 1.0f, 0.5f);
		Color color = new Color(rgb);
		return color;
	}

}
