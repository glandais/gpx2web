package io.github.glandais.srtm;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.graphhopper.reader.dem.ElevationProvider;
import com.graphhopper.reader.dem.SRTMGL1Provider;

@Configuration
public class SRTMHelperConfiguration {

	@Value("${gpx.data.cache:cache}")
	private File cacheFolder = new File("cache");

	@Bean
	public ElevationProvider elevationProvider() {

		final SRTMGL1Provider srtm = new SRTMGL1Provider(new File(cacheFolder, "srtm").getAbsolutePath());
		srtm.setInterpolate(true);
		return srtm;
	}
}
