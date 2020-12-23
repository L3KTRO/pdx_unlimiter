package com.crschnick.pdx_unlimiter.core.parser;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TextFormatParser extends FormatParser {

    public static final byte[] EU4_MAGIC = new byte[]{0x45, 0x55, 0x34, 0x74, 0x78, 0x74};
    public static final byte[] HOI_MAGIC = new byte[]{0x48, 0x4F, 0x49, 0x34, 0x74, 0x78, 0x74};

    private TextFormatParser(byte[] magic) {
        super(magic);
    }

    public static TextFormatParser textFileParser() {
        return new TextFormatParser(new byte[0]);
    }

    public static TextFormatParser eu4SavegameParser() {
        return new TextFormatParser(EU4_MAGIC);
    }

    public static TextFormatParser stellarisSavegameParser() {
        return new TextFormatParser(new byte[0]);
    }

    public static TextFormatParser hoi4SavegameParser() {
        return new TextFormatParser(HOI_MAGIC);
    }

    private List<Token> tokenize(String s) {
        // Approx amount of needed tokens
        List<Token> tokens = new ArrayList<>(s.length() / 5);
        int prev = 0;
        boolean isInQuotes = false;
        boolean isInComment = false;

        var chars = s.toCharArray();
        for (int i = 0; i < s.length(); i++) {
            Token t = null;
            if (isInQuotes && chars[i] != '"') {
                continue;
            } else if (chars[i] == '"') {
                isInQuotes = !isInQuotes;
            } else if (chars[i] == '{') {
                t = new OpenGroupToken();
            } else if (chars[i] == '}') {
                t = new CloseGroupToken();
            } else if (chars[i] == '=') {
                t = new EqualsToken();
            }

            boolean isNewLine = chars[i] == '\n';
            if (isInComment) {
                if (isNewLine) {
                    isInComment = false;
                }
                prev = i + 1;
                continue;
            }

            boolean isWhitespace = !isInQuotes && (isNewLine || chars[i] == '\r' || chars[i] == ' ' || chars[i] == '\t');
            boolean marksEndOfPreviousToken =
                    (chars[i] == '\0' && prev < i)               // EOF
                            || (t != null && prev < i)           // New token finishes old token
                            || (isWhitespace && prev < i)        // Whitespace finishes old token
                            || (chars[i] == '#' && prev < i);    // New comment finishes old token
            if (marksEndOfPreviousToken) {
                String sub = s.substring(prev, i);
                if (sub.charAt(0) == '"' && sub.charAt(sub.length() - 1) == '"') {
                    tokens.add(new ValueToken(sub.substring(1, sub.length() - 1)));
                } else {
                    tokens.add(new ValueToken(sub));
                }
            }

            if (isWhitespace) {
                prev = i + 1;
            } else if (t != null) {
                tokens.add(t);
                prev = i + 1;
            } else if (chars[i] == '#') {
                isInComment = true;
            }
        }
        return tokens;
    }

    @Override
    public List<FormatParser.Token> tokenize(InputStream stream) throws IOException {
        String s = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        s += "\0";
        return tokenize(s);
    }
}
