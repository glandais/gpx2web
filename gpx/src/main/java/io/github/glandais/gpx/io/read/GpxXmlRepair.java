package io.github.glandais.gpx.io.read;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Repairs the two ways real GPX files are malformed that can be fixed without guessing: text
 * encoded in ISO-8859-1 but declared (or assumed) UTF-8, and astral characters written as a pair of
 * numeric references to UTF-16 surrogate code points, which XML 1.0 forbids.
 *
 * <p>Never touches the DOCTYPE, so a repaired document is no more trusted than the original.
 */
final class GpxXmlRepair {

    private GpxXmlRepair() {}

    /** A single numeric character reference, decimal (group 1) or hexadecimal (group 2). */
    private static final Pattern NUMERIC_REFERENCE = Pattern.compile("&#(?:([0-9]+)|[xX]([0-9a-fA-F]+));");

    private static final Pattern DECLARED_ENCODING =
            Pattern.compile("(<\\?xml[^>]*?encoding\\s*=\\s*[\"'])([^\"']*)([\"'])");

    /**
     * @return repaired bytes, or null when there was nothing to repair
     */
    static byte[] repair(byte[] raw) {
        boolean reEncoded = false;
        String text;
        if (isValidUtf8(raw)) {
            text = new String(raw, StandardCharsets.UTF_8);
        } else {
            text = forceUtf8Declaration(new String(raw, StandardCharsets.ISO_8859_1));
            reEncoded = true;
        }

        String recombined = recombineSurrogatePairs(text);
        if (!reEncoded && recombined.equals(text)) {
            return null;
        }
        return recombined.getBytes(StandardCharsets.UTF_8);
    }

    private static boolean isValidUtf8(byte[] raw) {
        CharsetDecoder decoder = StandardCharsets.UTF_8
                .newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);
        try {
            decoder.decode(ByteBuffer.wrap(raw));
            return true;
        } catch (CharacterCodingException e) {
            return false;
        }
    }

    /** After re-encoding to UTF-8, a stale encoding declaration would make the parser misread us. */
    private static String forceUtf8Declaration(String xml) {
        if (!xml.startsWith("<?xml")) {
            return xml;
        }
        Matcher matcher = DECLARED_ENCODING.matcher(xml);
        if (!matcher.find()) {
            return xml;
        }
        return xml.substring(0, matcher.start())
                + matcher.group(1)
                + "UTF-8"
                + matcher.group(3)
                + xml.substring(matcher.end());
    }

    /**
     * Scans references one by one rather than matching pairs directly: a pattern consuming two
     * references at once would swallow the high surrogate of the real pair in
     * {@code &#65;&#55358;&#56600;} and miss it.
     */
    private static String recombineSurrogatePairs(String xml) {
        List<int[]> references = new ArrayList<>();
        Matcher matcher = NUMERIC_REFERENCE.matcher(xml);
        while (matcher.find()) {
            references.add(new int[] {matcher.start(), matcher.end(), referenceValue(matcher)});
        }

        StringBuilder out = new StringBuilder(xml.length());
        int copiedUpTo = 0;
        for (int i = 0; i + 1 < references.size(); i++) {
            int[] high = references.get(i);
            int[] low = references.get(i + 1);
            boolean adjacent = high[1] == low[0];
            if (!adjacent || !isHighSurrogate(high[2]) || !isLowSurrogate(low[2])) {
                continue;
            }
            out.append(xml, copiedUpTo, high[0]);
            out.append("&#")
                    .append(Character.toCodePoint((char) high[2], (char) low[2]))
                    .append(';');
            copiedUpTo = low[1];
            i++;
        }
        out.append(xml, copiedUpTo, xml.length());
        return out.toString();
    }

    /**
     * @return the referenced code point, or -1 when it cannot be one
     */
    private static int referenceValue(Matcher matcher) {
        String decimal = matcher.group(1);
        try {
            long value = decimal != null ? Long.parseLong(decimal) : Long.parseLong(matcher.group(2), 16);
            return value > Character.MAX_CODE_POINT ? -1 : (int) value;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static boolean isHighSurrogate(int cp) {
        return cp >= Character.MIN_HIGH_SURROGATE && cp <= Character.MAX_HIGH_SURROGATE;
    }

    private static boolean isLowSurrogate(int cp) {
        return cp >= Character.MIN_LOW_SURROGATE && cp <= Character.MAX_LOW_SURROGATE;
    }
}
