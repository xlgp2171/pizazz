package org.pizazz2.extraction.data;

public class ImageProperty extends FileProperty {
    private int width = -1;
    private int height = -1;
    private int bitDepth = -1;

    public ImageProperty(long length, String type, String suffix) {
        super(length, type, suffix);
    }

    public int getWidth() {
        return width;
    }

    public ImageProperty setWidth(int width) {
        this.width = width;
        return this;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getBitDepth() {
        return bitDepth;
    }

    public void setBitDepth(int bitDepth) {
        this.bitDepth = bitDepth;
    }
}
