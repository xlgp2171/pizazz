package org.pizazz2.test;

public class SubObject extends ParentObject {
    private String subName;
    private double subSize;
    private byte[] subData;

    public String getSubName() {
        return subName;
    }

    public void setSubName(String subName) {
        this.subName = subName;
    }

    public double getSubSize() {
        return subSize;
    }

    public void setSubSize(double subSize) {
        this.subSize = subSize;
    }

    public byte[] getSubData() {
        return subData;
    }

    public void setSubData(byte[] subData) {
        this.subData = subData;
    }
}
