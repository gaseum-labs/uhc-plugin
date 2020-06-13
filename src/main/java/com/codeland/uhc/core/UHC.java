package com.codeland.uhc.core;

public class UHC {

    private double startRadius;
    private double endRadius;
    private double graceTime;
    private double shrinkTime;

    public UHC(double startRadius, double endRadius, double graceTime, double shrinkTime) {
        setRadius(startRadius, endRadius);
        setGraceTime(graceTime);
        setShrinkTime(shrinkTime);
    }

    public void setRadius(double startRadius, double endRadius) {
        this.startRadius = startRadius;
        this.endRadius = endRadius;
    }

    public void setGraceTime(double graceTime) {
        this.graceTime = graceTime;
    }

    public void setShrinkTime(double shrinkTime) {
        this.shrinkTime = shrinkTime;
    }

}
