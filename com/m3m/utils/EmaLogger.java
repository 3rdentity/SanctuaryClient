/**
 *
 * @author thunder
 */
package com.m3m.utils;

import java.io.*;
import org.apache.log4j.*;

public final class EmaLogger {

    private Logger logger;
    private File fileName;
    private FileAppender fileappender;
    private boolean logging, tracing;
    private static String logFileStrWindows = "logs/";
    private static String logFileStrLinux = "./logs/";

    public EmaLogger(Class clazz, String processName) {
        this(clazz.getName(), processName);
    }

    public EmaLogger(String clazz, String processName) {
        logger = Logger.getLogger(clazz);
        String logFileStr = (Utils.isWindows()) ? logFileStrWindows : logFileStrLinux;

        try {
            // Creating Procees logs Folder:
            String logFolderStr = String.format(logFileStr);
            File logFolder = new File(logFolderStr);
            if (!logFolder.isDirectory()) {
                logFolder.mkdirs();
            }

            // Creating the new Log file:
            String logFileName = processName + "." + Utils.getDate("yyyyMMdd-HHmmss");

            fileName = new File(logFolder, logFileName);
            fileName.createNewFile();

            fileappender = new FileAppender(new PatternLayout(), fileName.getAbsolutePath());
            logger.addAppender(fileappender);

            // Enable logging and tracing:
            setLogging(true);
            setTracing(true);
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }

    public void log(Object object, boolean newLine) {
        if ((!logging) || (object == null)) {
            return;
        }

        if (tracing) {
            System.out.print(object);
            if (newLine) {
                System.out.println();
            }

            if (object instanceof Exception) {
                Exception ex = (Exception) object;
            }
        }

        if (logging) {
            logger.info(object);
            if (object instanceof Exception) {
                Exception ex = (Exception) object;
                logger.error(ex.getMessage(), ex);
            }
        }
    }

    public void logln(Object object) {
        log(object, true);
    }

    public void logNoLine(Object object) {
        log(object, false);
    }

    public void log(Object object) {
        log(object, true);
    }

    public static void Log(Object message) {
        System.out.println(message);
    }

    public static void Log(Exception ex) {
        System.out.println(ex);
    }

    public boolean isTracing() {
        return tracing;
    }

    public boolean isLogging() {
        return logging;
    }

    public void setTracing(boolean tracing) {
        this.tracing = tracing;
    }

    public void setLogging(boolean logging) {
        this.logging = logging;
    }
}
