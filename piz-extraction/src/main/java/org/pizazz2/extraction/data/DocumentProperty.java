package org.pizazz2.extraction.data;

public class DocumentProperty extends FileProperty {
    private int page = -1;
    private String encoding;
    private String created;
    private String modified;
    private String creator;
    private String modifier;

    public DocumentProperty(long length, String type, String suffix) {
        super(length, type, suffix);
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public String getEncoding() {
        return encoding;
    }

    public DocumentProperty setEncoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getModified() {
        return modified;
    }

    public void setModified(String modified) {
        this.modified = modified;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }
}
