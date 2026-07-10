package io.github.glandais.gpx.io.write;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class GPXFileWriterEscapeTest {

    /** U+1F918 takes two UTF-16 code units: the old code emitted &#55358;&#56600;, which XML forbids. */
    @Test
    void escape_shouldEmitOneReferencePerCodePoint() {
        assertEquals("&#129304;", GPXFileWriter.escape("🤘"));
    }

    @Test
    void escape_shouldEscapeNonAsciiAndXmlSpecials() {
        assertEquals("&#232;", GPXFileWriter.escape("è"));
        assertEquals("a&#38;b", GPXFileWriter.escape("a&b"));
        assertEquals("&#60;&#62;&#34;&#39;", GPXFileWriter.escape("<>\"'"));
    }

    @Test
    void escape_shouldKeepPlainAsciiAndAllowedWhitespace() {
        assertEquals("Col du Galibier", GPXFileWriter.escape("Col du Galibier"));
        assertEquals("a\tb\nc", GPXFileWriter.escape("a\tb\nc"));
    }

    /** Forbidden by the XML 1.0 Char production: drop them instead of emitting &#1;. */
    @Test
    void escape_shouldDropCharactersForbiddenByXml() {
        assertEquals("ab", GPXFileWriter.escape("a\u0001b"));
        assertEquals("ab", GPXFileWriter.escape("a\u000Bb"));
        assertEquals("ab", GPXFileWriter.escape("a\uFFFEb"));
    }

    /** A lone surrogate forms no character at all, so it must produce nothing. */
    @Test
    void escape_shouldDropLoneSurrogate() {
        assertEquals("ab", GPXFileWriter.escape("a\uD83Eb"));
    }
}
