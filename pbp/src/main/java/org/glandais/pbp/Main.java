package org.glandais.pbp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.glandais.pbp.bean.Profile;
import org.glandais.pbp.bean.Profiles;
import org.glandais.pbp.bean.Split;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Main {

	private static final List<Step> STEPS = Steps.INSTANCE.getSteps();

	private static final ZoneId PARIS = ZoneId.of("Europe/Paris");

	private static ObjectMapper objectMapper = new ObjectMapper();

	public static void main(String[] args)
			throws JsonGenerationException, JsonMappingException, FileNotFoundException, IOException {
//		Profiles profiles = retrieveProfiles();
		Profiles profiles = readProfiles();

		List<Profile> profileList = new ArrayList<>(profiles.values());

		Map<String, List<Integer>> ranksAbs = getRanks(profileList, false);
		Map<String, List<Integer>> ranksRel = getRanks(profileList, true);

		write(profiles, "rankings.txt", ranksAbs, ranksRel);
	}

	private static void write(Profiles profiles, String name, Map<String, List<Integer>> ranksAbs,
			Map<String, List<Integer>> ranksRel) throws IOException {
		try (BufferedWriter writer = new BufferedWriter(
				new FileWriter(new File(name), StandardCharsets.UTF_8, false))) {
			writer.write("bib\tname");
			addSteps(writer);
			writer.newLine();
			for (String bib : profiles.keySet()) {
				Profile profile = profiles.get(bib);
				writer.write(bib + "\t" + profile.getName());
				List<Split> splits = profile.getSplits();
				for (int i = 0; i < STEPS.size(); i++) {
					if (i < splits.size()) {
						writer.write("\t");
						writeSplit(writer, splits.get(i));
						writer.write("\t");
						writer.write(String.valueOf(ranksAbs.get(bib).get(i)));
						if (i > 0) {
							writer.write("\t");
							double duration = splits.get(i).getEpochTime() - splits.get(0).getEpochTime();
							Duration d = Duration.ofMillis((long) (1000 * duration));
							writer.write(formatDuration(d));
							writer.write("\t");
							writer.write(String.valueOf(ranksRel.get(bib).get(i)));
						}
					}
				}
				writer.newLine();
			}
		}
	}

	private static void writeSplit(BufferedWriter writer, Split split) throws IOException {
		Instant instant = Instant.ofEpochMilli((long) (1000 * split.getEpochTime()));
		ZonedDateTime start = ZonedDateTime.ofInstant(instant, PARIS);
		writer.write(start.toLocalDateTime().toString());
	}

	private static void addSteps(BufferedWriter writer) throws IOException {
		for (int i = 0; i < STEPS.size(); i++) {
			if (i == 0) {
				writer.write("\t" + STEPS.get(i).getLabel() + "\tRank");
			} else {
				writer.write("\t" + STEPS.get(i).getLabel() + "\tRank\tDuration\tRank");
			}
		}
	}

	private static Map<String, List<Integer>> getRanks(List<Profile> profileList, boolean relative) {
		Map<String, List<Integer>> ranks = new TreeMap<>();
		for (int i = 0; i < STEPS.size(); i++) {
			profileList.sort(getComparator(relative, i));
			int r = 1;
			for (Profile profile : profileList) {
				ranks.computeIfAbsent(profile.getBib(), bib -> new ArrayList<>()).add(r++);
			}
		}
		return ranks;
	}

	private static Comparator<Profile> getComparator(boolean relative, int i) {
		return Comparator.comparing(p -> {
			Profile profile = (Profile) p;
			if (profile.getSplits().isEmpty()) {
				return Double.MAX_VALUE;
			}
			double epochTime = getSplit(profile, i);
			if (relative) {
				return epochTime - getSplit(profile, 0);
			} else {
				return epochTime;
			}
		});
	}

	private static double getSplit(Profile profile, int i) {
		if (i < profile.getSplits().size()) {
			return profile.getSplits().get(i).getEpochTime();
		} else {
			return Double.MAX_VALUE;
		}
	}

	private static Profiles readProfiles()
			throws JsonParseException, JsonMappingException, FileNotFoundException, IOException {
		return objectMapper.readValue(new FileInputStream(new File("profiles.json")), Profiles.class);
	}

	private static Profiles retrieveProfiles()
			throws IOException, JsonGenerationException, JsonMappingException, FileNotFoundException {
		Profiles profiles = new Profiles();
		int max = 7;
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		String profilesUrl = addAuth("https://api.rtrt.me/events/PBP-2019/profiles");

		for (int i = 0; i < max; i++) {
			String url = profilesUrl + "&start=" + (i * 1000) + "&max=1000";
			Map map = getMap(httpClient, objectMapper, url);
			List<Map> list = (List<Map>) map.get("list");
			for (Map prof : list) {
				Profile profile = new Profile();
				profile.setBib(String.valueOf(prof.get("bib")));
				profile.setName(String.valueOf(prof.get("name")));
				profile.setPid(String.valueOf(prof.get("pid")));
				profiles.put(profile.getBib(), profile);
			}
		}
		addSplits(profiles, httpClient, objectMapper);
		objectMapper.writeValue(new FileOutputStream(new File("profiles.json")), profiles);
		return profiles;
	}

	private static void addSplits(Profiles profiles, CloseableHttpClient httpClient, ObjectMapper objectMapper)
			throws IOException, ClientProtocolException, JsonParseException, JsonMappingException {
		for (Profile profile : profiles.values()) {
			String url = "https://api.rtrt.me/events/PBP-2019/profiles/" + profile.getPid() + "/splits";
			url = addAuth(url);
			Map map = getMap(httpClient, objectMapper, url);

			List<Split> splits = new ArrayList<Split>();

			List<Map> list = (List<Map>) map.get("list");
			if (list != null) {
				for (Map spl : list) {
					Split split = new Split();
					split.setEpochTime(Double.parseDouble(String.valueOf(spl.get("epochTime"))));
					splits.add(split);
				}
			}
			profile.setSplits(splits);
		}
	}

	private static Map getMap(CloseableHttpClient httpClient, ObjectMapper objectMapper, String url)
			throws IOException, ClientProtocolException, JsonParseException, JsonMappingException {
		System.out.println(url);
		HttpGet get = new HttpGet(url);
		CloseableHttpResponse response = httpClient.execute(get);
		String result = EntityUtils.toString(response.getEntity());
		Map map = objectMapper.readValue(result, Map.class);
		return map;
	}

	private static String addAuth(String url) {
		return url + "?source=webtracker&appid=592479603a4c925a288b4567&token=296AE62E520D6A291C62";
	}

	private static String formatDuration(Duration duration) {
		return String.format("%02d:%02d:%02d", duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart());
	}
}
