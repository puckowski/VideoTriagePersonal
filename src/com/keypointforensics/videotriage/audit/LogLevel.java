package com.keypointforensics.videotriage.audit;

public enum LogLevel {
    INFO("INFO"),

    WARN("WARN"),

    ERROR("ERROR");

    private String mName;

    private LogLevel(final String name) {
        mName = name;
    }

    @Override
    public String toString() {
        return mName;
    }
}
