package com.tnf.bis.common.properties;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tnf.bis.common.ClientConstants;

public class ListProperties implements ClientConstants {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListProperties.class);

    List<String> keyList = new ArrayList<String>();
    Map<String, String> map = new HashMap<String, String>();

    public List<String> keyList() {
        return keyList;
    }

    public String getProperty(String key) {
        return map.get(key);
    }

    void setProperty(String key, String value) {
        String valueFromMap = map.get(key);
        if (valueFromMap != null) {
            int index = keyList.indexOf(key);
            keyList.remove(index);
        }
        map.put(key, value);
        keyList.add(key);
    }

    public void load(BufferedReader reader) {
        load(reader, null);
    }

    public void load(BufferedReader reader, String keyPrefix) {
        try {
            String propertyKey = null;
            StringBuilder propertyValue = null;
            boolean multiLines = false;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                    if (propertyKey != null) {
                        setProperty(propertyKey, propertyValue.toString());
                        propertyKey = null;
                        propertyValue = null;
                    }
                    multiLines = false;
                    continue;
                }
                if (multiLines) {
                    if (line.trim().endsWith("\\")) {
                        propertyValue.append(line.substring(0, line.lastIndexOf("\\")));
                        propertyValue.append(LS);
                    } else {
                        propertyValue.append(line);
                        setProperty(propertyKey, propertyValue.toString());
                        propertyKey = null;
                        propertyValue = null;
                        multiLines = false;
                    }
                } else {
                    if (line.indexOf("=") > -1) {
                        String key = line.substring(0, line.indexOf("=")).trim();
                        if (keyPrefix != null) {
                            propertyKey = keyPrefix + "." + key;
                        } else {
                            propertyKey = key;
                        }
                        propertyValue = new StringBuilder();
                        String value = line.substring(line.indexOf("=") + 1, line.length());
                        if (value.trim().endsWith("\\")) {
                            propertyValue.append(value.substring(0, value.lastIndexOf("\\")));
                            propertyValue.append(LS);
                            multiLines = true;
                        } else {
                            propertyValue.append(value);
                            setProperty(propertyKey, propertyValue.toString());
                            propertyKey = null;
                            propertyValue = null;
                        }
                    }
                }
            }
            if (propertyKey != null) {
                setProperty(propertyKey, propertyValue.toString());
                // propertyKey = null;
                // propertyValue = null;
            }
        } catch (IOException ex) {
            LOGGER.error("", ex);
        }
    }

    public void writeProperty(String propertyKey, Writer writer) throws IOException {
        String propertyValue = getProperty(propertyKey);
        if (propertyValue != null) {
            StringBuilder line = new StringBuilder(propertyKey);
            line.append("=");
            line.append(propertyValue.replaceAll(LS, "\\\\" + LS));
            writer.write(line.toString());
            writer.write(LS);
        }
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(",", "[", "]");
        for (String key : keyList) {
            String value = map.get(key);
            StringJoiner buf = new StringJoiner("\":\"", "\"", "\"");
            buf.add(key);
            buf.add(value);
            joiner.add(buf.toString());
        }
        return joiner.toString();
    }

}
