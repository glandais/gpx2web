package io.github.glandais.virtual;

import io.github.glandais.util.CacheFolderProvider;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.File;

@Singleton
public class CacheFolderProviderImpl implements CacheFolderProvider {

    @ConfigProperty(name = "gpx.data.cache", defaultValue = "cache")
    protected File cacheFolder = new File("cache");

    @Override
    public File getCacheFolder() {
        return cacheFolder;
    }
}
