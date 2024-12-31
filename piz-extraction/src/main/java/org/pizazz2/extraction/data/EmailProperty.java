package org.pizazz2.extraction.data;

public class EmailProperty extends FileProperty {
    private String subject;
    private String[] toEmail;
    private String[] toName;
    private String[] fromEmail;
    private String[] fromName;
    private String[] ccEmail;
    private String[] ccName;
    private String[] bccEmail;
    private String[] bccName;
    private String created;

    public EmailProperty(long length, String type, String suffix) {
        super(length, type, suffix);
    }

    public String getSubject() {
        return subject;
    }

    public EmailProperty setSubject(String subject) {
        this.subject = subject;
        return this;
    }

    public String[] getToEmail() {
        return toEmail;
    }

    public void setToEmail(String[] toEmail) {
        this.toEmail = toEmail;
    }

    public String[] getToName() {
        return toName;
    }

    public void setToName(String[] toName) {
        this.toName = toName;
    }

    public String[] getFromEmail() {
        return fromEmail;
    }

    public void setFromEmail(String[] fromEmail) {
        this.fromEmail = fromEmail;
    }

    public String[] getFromName() {
        return fromName;
    }

    public void setFromName(String[] fromName) {
        this.fromName = fromName;
    }

    public String[] getCcEmail() {
        return ccEmail;
    }

    public void setCcEmail(String[] ccEmail) {
        this.ccEmail = ccEmail;
    }

    public String[] getCcName() {
        return ccName;
    }

    public void setCcName(String[] ccName) {
        this.ccName = ccName;
    }

    public String[] getBccEmail() {
        return bccEmail;
    }

    public void setBccEmail(String[] bccEmail) {
        this.bccEmail = bccEmail;
    }

    public String[] getBccName() {
        return bccName;
    }

    public void setBccName(String[] bccName) {
        this.bccName = bccName;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }
}
