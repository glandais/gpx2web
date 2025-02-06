package io.github.glandais.gpx.data.values;

import lombok.Value;

@Value
public class ValueKey {
    String key;
    ValueKind kind;
}
