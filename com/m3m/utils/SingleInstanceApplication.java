package com.m3m.utils;

public class SingleInstanceApplication {

    private SingleInstanceApplication() {
    }

    public static boolean registerInstance() {
        return false;
    }
}