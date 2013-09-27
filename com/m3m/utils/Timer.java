/**
 *
 * @author thunder
 */
package com.m3m.utils;

public class Timer {

    private static long startTime, stopTime, elapsed;
    private static boolean running;

    public static void start() {
        startTime = System.currentTimeMillis();
        stopTime = -1;
        elapsed = -1;
        running = true;
    }

    public static long stop() {
        stopTime = System.currentTimeMillis();
        running = false;
        elapsed = getElapsedTime();
        return elapsed;
    }

    public static void stopAndPrint() {
        stop();
        System.out.println("Elapsed Time: " + elapsed + " ms.");
        System.out.flush();
    }

    public static void stopAndReport() {
        stop();
        report();
    }

    // Elaspsed time in milliseconds
    public static long getElapsedTime() {
        long temp = (running) ? System.currentTimeMillis() : stopTime;
        return (temp - startTime);
    }

    // Elaspsed time in seconds
    public static long getElapsedTimeSecs() {
        return getElapsedTime() / 1000;
    }

    // Elaspsed time in minutes
    public static long getElapsedTimeMinutes() {
        return getElapsedTimeSecs() / 60;
    }

    public static void report() {
        System.out.println("-------------------------------------");
        System.out.println("Started: " + startTime);
        System.out.println("Stopped: " + stopTime);
        System.out.println("Elapsed (ms): " + getElapsedTime());
        System.out.println("Elapsed (sec): " + getElapsedTimeSecs());
        System.out.println("Elapsed (minutes): " + getElapsedTimeMinutes());
        System.out.println("-------------------------------------");
        System.out.flush();
    }
}
