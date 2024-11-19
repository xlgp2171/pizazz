package org.pizazz2.extraction.data;

public class DocumentProperty extends AbstractProperty {
    private int size;
    public DocumentProperty(long length, String type, String suffix) {
        super(length, type, suffix);
    }

    public int getSize() {
        return size;
    }

    public DocumentProperty setSize(int size) {
        this.size = size;
        return this;
    }

    @Override
    public String toString() {
        return "DocumentProperty{" +
                "size=" + size +
                '}';
    }
}
