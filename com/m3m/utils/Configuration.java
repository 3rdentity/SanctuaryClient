/**
 * @author Thunder
 */
package com.m3m.utils;

import java.io.*;
import java.util.*;

public class Configuration {

    private String fileName;
    private Properties properties;

    public Configuration(String fileName) throws IOException {
        this.fileName = fileName;
        properties = new Properties();
        load(fileName);
    }

    public Configuration(String fileName, Properties properties) throws IOException {
        this.fileName = fileName;
        this.properties = properties;
        load(fileName);
    }

    private void load(String fileName) throws IOException {
        File file = new File(fileName);
        load(file);
    }

    private void load(File configFile) throws IOException {
        FileInputStream fis = new FileInputStream(configFile);
        try (InputStreamReader isr = new InputStreamReader(fis, "UTF8")) {
            properties.load(isr);
        }
    }

    public boolean containsParameter(String parameter) {
        return properties.containsKey(parameter);
    }

    public void add(String key, String value) {
        properties.setProperty(key, value);
    }

    public void update(String key, String value) {
        if (properties.containsKey(key)) {
            properties.remove(key);
        }
        properties.setProperty(key, value);
    }

    public int getInt(String parameter) {
        return Integer.parseInt(getTrim(parameter));
    }

    public long getLong(String parameter) {
        return Long.parseLong(getTrim(parameter));
    }

    public float getFloat(String parameter) {
        return Float.parseFloat(getTrim(parameter));
    }

    public double getDouble(String parameter) {
        return Double.parseDouble(getTrim(parameter));
    }

    public String get(String parameter) {
        if (!properties.containsKey(parameter)) {
            String errorMessage = String.format("No such parameter: \"%s\", check the (%s) File.", parameter, getFileName());
            throw new RuntimeException(errorMessage);
        }
        return properties.getProperty(parameter);
    }

    public String getTrim(String parameter) {
        return get(parameter).trim();
    }

    public String getFileName() {
        return fileName;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public List getKeysList(boolean sort) {
        List sortedKeys = new ArrayList(properties.keySet());
        if (sort) {
            Collections.sort(sortedKeys);
        }
        return sortedKeys;
    }
}