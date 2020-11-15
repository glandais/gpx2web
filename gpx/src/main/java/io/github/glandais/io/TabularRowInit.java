package io.github.glandais.io;

import io.github.glandais.gpx.storage.Values;

@FunctionalInterface
public interface TabularRowInit {
    void accept(Integer row, Values values);
}
