package org.pizazz2.extraction.data;


public class FileProperty {
    private final long length;
    private final String type;
    private final String suffix;

    public FileProperty(long length, String type, String suffix) {
        this.length = length;
        this.type = type;
        this.suffix = suffix;
    }

    public long getLength() {
        return length;
    }

    public String getType() {
        return type;
    }

    public String getSuffix() {
        return suffix;
    }
}
