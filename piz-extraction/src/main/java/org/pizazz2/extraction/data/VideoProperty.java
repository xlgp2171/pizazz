package org.pizazz2.extraction.data;

public class VideoProperty extends AbstractProperty{
    private String duration;
    /** 分辨率 */
    private String resolution;
    public VideoProperty(long length, String type, String suffix) {
        super(length, type, suffix);
    }

    public String getDuration() {
        return duration;
    }

    public VideoProperty setDuration(String duration) {
        this.duration = duration;
        return this;
    }

    public String getResolution() {
        return resolution;
    }

    public VideoProperty setResolution(String resolution) {
        this.resolution = resolution;
        return this;
    }
}
