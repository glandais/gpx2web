package io.github.glandais.gpx.storage;

import lombok.Value;

@Value
public class ValueKey {
    String key;
    ValueKind kind;
}
