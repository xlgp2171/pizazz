package org.pizazz2.test;

import org.pizazz2.common.ArrayUtils;
import org.pizazz2.common.StringUtils;
import org.pizazz2.common.ValidateUtils;
import org.pizazz2.exception.ValidateException;

import java.nio.charset.Charset;

public class ParentObject {

    private String parentName;
    private double parentSize;
    private byte[] parentData;

    public ParentObject() {
    }

    public ParentObject(String parentName, double parentSize, byte[] parentData) {
        this.parentName = parentName;
        this.parentSize = parentSize;
        this.parentData = parentData;
    }

    public String ofCharset(Charset charset) throws ValidateException {
        ValidateUtils.notNull("ofCharset", charset);

        if (!ArrayUtils.isEmpty(parentData)) {
            return new String(parentData, charset);
        }
        return StringUtils.EMPTY;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public double getParentSize() {
        return parentSize;
    }

    public void setParentSize(double parentSize) {
        this.parentSize = parentSize;
    }

    public byte[] getParentData() {
        return parentData;
    }

    public void setParentData(byte[] parentData) {
        this.parentData = parentData;
    }
}
