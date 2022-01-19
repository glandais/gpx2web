package io.github.glandais.srtm;

import com.graphhopper.reader.dem.ElevationProvider;
import com.graphhopper.reader.dem.MultiSourceElevationProvider;
import com.graphhopper.reader.dem.SkadiProvider;
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

        return new SkadiProvider(new File(cacheFolder, "skadi").getAbsolutePath());
    }
}
