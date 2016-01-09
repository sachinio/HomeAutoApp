package com.sachinpatney.homeauto;

public class HomeTempStorage {
    private static String IP = "10.0.0.19";
    private static String FlaskPort = "5000";

    public static void setIP(String ip) {
        HomeTempStorage.IP = ip;
    }

    public static String getUrl() {
        return "http://"+HomeTempStorage.IP;
    }

    public static String getFlaskUrl() {
        return "http://" + HomeTempStorage.IP + ":" + FlaskPort;
    }
}
