package com.keypointforensics.videotriage.image.match;

public class OrbKeypoint {
    public int x;
    public int y;
    public double angle;
    public byte[] descriptor;

    public OrbKeypoint(int x, int y, double angle) {
        this.x = x;
        this.y = y;
        this.angle = angle;
    }
}