package com.example.quakereport;
import java.time.LocalDateTime;
public class Earthquake {
    private double mMagnitude;
    private String mLocation;
    private long mDateTime;
    private String mUrl;

    public Earthquake(double magnitude, String location, long dateTime, String url){
        mMagnitude = magnitude;
        mLocation = location;
        mDateTime = dateTime;
        mUrl = url;
    }

    public double getMagnitude() {
        return mMagnitude;
    }

    public String getLocation() {
        return mLocation;
    }

    public long getTimeInMilliseconds() {
        return mDateTime;
    }

    public String getUrl() {
        return mUrl;
    }
}
