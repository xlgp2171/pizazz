package org.pizazz2.extraction.data;

public class VideoProperty extends FileProperty {
    private float duration = -1F;

    private int width = -1;
    private int height = -1;
    public VideoProperty(long length, String type, String suffix) {
        super(length, type, suffix);
    }

    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
