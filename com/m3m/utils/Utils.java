/**
 *
 * @author thunder
 */
package com.m3m.utils;

import java.io.*;
import java.math.*;
import java.text.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class Utils {

    public static void main(String[] args) {
        double d = 10.546875;
        System.out.println(Utils.formatNumber(d, 2, 2));

        String s = "2011-01-28 23:59:59";
        System.out.println(s);
        System.out.println(Utils.formatDate("yyyy-MM-dd HH:mm:ss", s, "dd-MM-yyyy HH:mm"));
    }

    private Utils() {
    }

    public static String[] tokenize(String arguments, String delimiter) {
        return tokenize(arguments, delimiter, false);
    }

    public static String[] tokenize(String arguments, String delimiter, boolean trimToken) {
        StringTokenizer tocken = new StringTokenizer(arguments, delimiter);
        String[] args = new String[tocken.countTokens()];
        for (int i = 0; i < args.length; i++) {
            args[i] = (trimToken) ? tocken.nextToken().trim() : tocken.nextToken();
        }

        return args;
    }

    public static Object[] toArray(Object... elements) {
        return elements;
    }

    public static HashMap<Object, Object> toMap(Object... elements) {
        if (elements == null || elements.length < 2) {
            return null;
        }

        HashMap<Object, Object> map = new HashMap<>();
        for (int i = 0; i < elements.length; i = i + 2) {
            map.put(elements[i], elements[i + 1]);
        }

        return map;
    }

    public static boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        //windows
        return (os.indexOf("win") >= 0);
    }

    public static boolean isMac() {
        String os = System.getProperty("os.name").toLowerCase();
        //Mac
        return (os.indexOf("mac") >= 0);
    }

    public static boolean isUnix() {
        String os = System.getProperty("os.name").toLowerCase();
        //linux or unix
        return (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0);
    }

    public static String getCurrentDate(String dateFormat) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        return sdf.format(cal.getTime());
    }

    public static String getDate(String dateFormat) {
        return getCurrentDate(dateFormat);
    }

    public static Calendar getTime(String dateStr, String format) {
        Calendar calendar = null;
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(format);
            Date date = formatter.parse(dateStr);
            calendar = Calendar.getInstance();
            calendar.setTime(date);
        } catch (ParseException ex) {
            calendar = null;
        }
        return calendar;
    }

    public static long compareMillis(Calendar first, Calendar second) {
        return first.getTimeInMillis() - second.getTimeInMillis();
    }

    public static int compareDays(Calendar first, Calendar second) {
        // One day = 24 * 60 * 60 * 1000 = 86400000 Millis.
        return Math.round(compareMillis(first, second) / (86400000));
    }

    public static long millisToCurrentTime(String dateStr, String format) {
        Calendar calendar = getTime(dateStr, format);
        Calendar current = Calendar.getInstance();
        return compareMillis(current, calendar);
    }

    public static int daysToCurrentTime(String dateStr, String format) {
        // One day = 24 * 60 * 60 * 1000 = 86400000 Millis.
        return Math.round(millisToCurrentTime(dateStr, format) / 86400000);
    }

    public static void sleep(long sleepPeriod) {
        try {
            Thread.sleep(sleepPeriod);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    public static HashSet<String> toHashSet(String str, String delimiter) {
        String[] parts = tokenize(str, delimiter, true);
        HashSet<String> set = new HashSet<>(parts.length);
        List<String> lst = Arrays.asList(parts);
        set.addAll(lst);
        return set;
    }

    public static int indexOf(Object obj, Object[] array) {
        for (int i = 0; i < array.length; i++) {
            if (obj.equals(array[i])) {
                return i;
            }
        }

        return -1;
    }

    public static boolean contains(Object obj, Object[] array) {
        return indexOf(obj, array) > -1;
    }

    public static String formatNumber(double d, int integerDigits, int fractionDigits) {
        NumberFormat formatter = NumberFormat.getInstance();
        formatter.setMaximumIntegerDigits(integerDigits);
        formatter.setMaximumFractionDigits(fractionDigits);
        return formatter.format(d);
    }

    public static String formatNumber(double d, int integerDigits, int fractionDigits, RoundingMode roundingMode) {
        NumberFormat formatter = NumberFormat.getInstance();
        formatter.setMaximumIntegerDigits(integerDigits);
        formatter.setMaximumFractionDigits(fractionDigits);
        formatter.setRoundingMode(roundingMode);
        return formatter.format(d);
    }

    public static String formatDate(String dateStrFormat, String dateStr, String dateFormat) {
        SimpleDateFormat parser = new SimpleDateFormat(dateStrFormat);
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        try {
            Date date = parser.parse(dateStr);
            return formatter.format(date);
        } catch (ParseException ex) {
        }
        return null;
    }

    public static Object dynamicConvert(Object value) {
        if (value == null) {
            return null;
        }

        Object dValue = null;
        try {
            dValue = Integer.parseInt(String.valueOf(value));
        } catch (Exception ex) {
            try {
                dValue = Long.parseLong(String.valueOf(value));
            } catch (Exception ex1) {
                try {
                    dValue = Float.parseFloat(String.valueOf(value));
                } catch (Exception ex2) {
                    try {
                        dValue = Double.parseDouble(String.valueOf(value));
                    } catch (Exception ex4) {
                        dValue = value;
                    }
                }
            }
        }
        return dValue;
    }

    public static InputStream getAsInputStream(String str) {
        try {
            return FileManager.getAsInputStream(str);
        } catch (UnsupportedEncodingException ex) {
            return null;
        }
    }

    public static Document getDocumentFromFile(String fileName) {
        // Prepare XML document:
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(fileName);
            return doc;
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            return null;
        }
    }

    public static Document getDocumentFromData(String data) {
        InputStream inStream = getAsInputStream(data);
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(inStream);
            return doc;
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            return null;
        }
    }

    public static String trimTrailingZeros(String number) {
        if (!number.contains(".")) {
            return number;
        }

        number = number.replaceAll("0*$", "");
        if (number.charAt(number.length() - 1) == '.') {
            number = number.substring(0, number.length() - 1);
        }
        return number;
    }
}
