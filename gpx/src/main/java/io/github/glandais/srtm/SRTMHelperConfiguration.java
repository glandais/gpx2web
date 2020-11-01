package io.github.glandais.srtm;

import com.graphhopper.reader.dem.ElevationProvider;
import com.graphhopper.reader.dem.MultiSourceElevationProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration(proxyBeanMethods = false)
public class SRTMHelperConfiguration {

    @Value("${gpx.data.cache:cache}")
    private File cacheFolder = new File("cache");

    @Bean
    public ElevationProvider elevationProvider() {

        final ElevationProvider srtm = new MultiSourceElevationProvider(
                new File(cacheFolder, "srtm").getAbsolutePath()
        );
        srtm.setInterpolate(true);
        return srtm;
    }
}
