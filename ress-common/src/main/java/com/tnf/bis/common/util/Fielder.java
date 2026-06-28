package com.tnf.bis.common.util;

import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Fielder implements AutoCloseable {
    public static final char LF = '\n';
    public static final String LF_PATTERN = ( //
    Pattern.quote(Character.toString(LF)) //
    );
    public static final char PIPE = '|';
    public static final String PIPE_PATTERN = ( //
    Pattern.quote(Character.toString(PIPE)) //
    );
    public static final char COMMA = ',';
    public static final String COMMA_PATTERN = ( //
    Pattern.quote(Character.toString(COMMA))//
    );
    Writer mWriter;
    private char mSeparator;
    private String mSeparatorPattern;

    public Fielder(Writer writer) {
        this(writer, PIPE);
    }

    public Fielder(Writer sWriter, char separator) {
        mWriter = sWriter;
        mSeparator = separator;
        mSeparatorPattern = Pattern.quote(Character.toString(separator));
    }

    public static String[] parse(String line, char separator) {
        String[] result = new String[] {};
        if (line != null) {
            result = line.split(//
                    Pattern.quote(Character.toString(separator)), -1);
        }
        return result;
    }

    public static String[] parse(String line) {
        String[] result = new String[] {};
        if (line != null) {
            result = line.split(PIPE_PATTERN, -1);
        }
        return result;
    }

    public static Map<Integer, Integer> parseHeader(String[] headers,
            List<String> fields) {
        Map<Integer, Integer> result = new HashMap<Integer, Integer>();
        for (int h = 0; h < headers.length; h++) {
            if (h == 0 && headers.length > 0 && headers[0].startsWith("#")) {
                headers[0] = headers[0].substring(1);
            }
            String header = headers[h];
            for (int f = 0; f < fields.size(); f++) {
                String field = fields.get(f);
                if (header.equalsIgnoreCase(field)) {
                    result.put(f, h);
                    continue;
                }
            }
        }
        return result;
    }

    public void writeNext(String[] fields) {
        boolean first = true;
        try {
            for (String field : fields) {
                if (first) {
                    first = false;
                } else {
                    mWriter.append(mSeparator);
                }
                field = field.replaceAll(mSeparatorPattern, "") //
                        .replaceAll(LF_PATTERN, "");
                mWriter.append(field);
            }
            mWriter.append('\n');
        } catch (Exception e) {
            // TODO dz ???
        }
    }

    @Override
    public void close() throws Exception {
        mWriter.close();
    }

    public static String getStrValue(//
            String[] sValues, int i, Map<Integer, Integer> fldMap//
    ) {
        String value = null;
        if (fldMap == null) {
            if (i < sValues.length && sValues[i] != null
                    && sValues[i].length() > 0) {
                value = sValues[i];
            }
        } else {
            Integer idx = fldMap.get(i);
            if (idx != null) {
                if (idx < sValues.length //
                        && sValues[idx] != null //
                        && sValues[idx].length() > 0//
                ) {
                    value = sValues[idx];
                }
            }
        }
        return value;
    }
}
