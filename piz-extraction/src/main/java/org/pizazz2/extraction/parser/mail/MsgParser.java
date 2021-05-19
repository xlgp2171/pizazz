package org.pizazz2.extraction.parser.mail;

import org.pizazz2.exception.ValidateException;
import org.pizazz2.extraction.config.IConfig;
import org.pizazz2.extraction.data.ExtractObject;
import org.pizazz2.extraction.exception.DetectionException;
import org.pizazz2.extraction.exception.ParseException;
import org.pizazz2.extraction.parser.AbstractParser;
import org.pizazz2.extraction.process.IExtractListener;

/**
 * outlook(application/vnd.ms-outlook)解析<br>
 * 解析属性Metadata包括：
 * <li/>FROM 邮件来源
 * <li/>TO 邮件发送到
 * <li/>CC 邮件抄送到
 * <li/>BCC 邮件密送到
 * <li/>SENT_DATE 邮件发送时间
 * <li/>SUBJECT 邮件主题
 *
 * @author xlgp2171
 */
@Deprecated
public class MsgParser extends AbstractParser {

//    @Override
//    public IConfig toConfig(TupleObject config) {
//        return new MsgParser.Config(config);
//    }

    @Override
    protected void doParse(ExtractObject object, IConfig config, IExtractListener listener) throws ParseException,
            ValidateException, DetectionException {
//        OutlookMessageParser parser = new OutlookMessageParser();
//        OutlookMessage message = null;
//
//        try (InputStream in = new ByteArrayInputStream(object.getData())) {
//            message = parser.parseMsg(in);
//        } catch (Exception e) {
//            super.throwException(object, config, e);
//        }
//        if (message != null) {
//            doParse(object, message, config.getTarget(MsgParser.Config.class));
//        }
    }

    @Override
    public String[] getType() {
        return new String[] { "application/vnd.ms-outlook" };
    }

//    @SuppressWarnings("DuplicatedCode")
//    private void doParse(LinkedObject object, OutlookMessage message, MsgParser.Config config)
//            throws ParseException, ValidateException, DetectionException {
//        Charset charset = !config.detectCharset() ? null : super.detect(message.getBodyHTML(),
//                StringUtils.isEmpty(message.getBodyText()) ? message.getSubject() : message.getBodyText());
//        // 填充邮件属性
//        fillMetadata(object.getMetadata(), message, charset);
//        // 填充邮件内容
//        fillContent(object, message, charset, config.htmlFormat());
//        // 填充邮件附件
//        fillAttachment(object, message, config, charset);
//    }
//
//    private void fillMetadata(Metadata metadata, OutlookMessage message, Charset charset) {
//        if (!StringUtils.isEmpty(message.getMessageId())) {
//            // 邮件ID
//            metadata.add("ID", message.getMessageId());
//        }
//        // 邮件发送者
//        fillFromRecipient(metadata, message);
//        // 邮件接收者
//        fillRecipients(metadata, message);
//        // 邮件发送时间
//        fillSentDate(metadata, message);
//        // 邮件发送
//        fillSubject(metadata, message, charset);
//    }
//
//    private void fillAttachment(LinkedObject parent, OutlookMessage message, MsgParser.Config config,
//                                Charset charset) throws ParseException, DetectionException {
//        Path tmp = StringUtils.isTrimEmpty(parent.getSource()) ? null : Paths.get(parent.getSource());
//
//        for (OutlookAttachment item : message.getOutlookAttachments()) {
//            String source = tmp != null ? tmp.resolve(parent.getId()).toString() : StringUtils.EMPTY;
//            // 文件形式
//            if (item instanceof OutlookFileAttachment) {
//                OutlookFileAttachment file = (OutlookFileAttachment) item;
//                String fileName = ExtractHelper.convert(file.getLongFilename(), charset);
//                // 增加附件文件(未解析)
//                super.addAttachment(parent, fileName, source).setData(file.getData());
//            } else if (item instanceof OutlookMsgAttachment) {
//                OutlookMsgAttachment attachment = (OutlookMsgAttachment) item;
//                // 解析后的outlook使用邮件主题作为名称
//                String fileName = ExtractHelper.convert(attachment.getOutlookMessage().getSubject(), charset);
//                LinkedObject current = new LinkedObject(super.generateId(), fileName, source)
//                        .setTypeString(getType()[0]);
//                try {
//                    doParse(current, attachment.getOutlookMessage(), config);
//                } finally {
//                    current.archive(config.clearData());
//                }
//                // 增加附件文件(已解析)
//                parent.addAttachment(current);
//            } else {
//                parent.setStatus(LinkedObject.StatusEnum.BROKEN);
//            }
//        }
//    }
//
//    private void fillFromRecipient(Metadata metadata, OutlookMessage message) {
//        if (!StringUtils.isEmpty(message.getFromName()) || !StringUtils.isEmpty(message.getFromEmail())) {
//            metadata.add("FROM", StringUtils.nullToEmpty(message.getFromName()) + ";" +
//                    StringUtils.nullToEmpty(message.getFromEmail()));
//        }
//    }
//
//    /**
//     * 填充邮件接收者
//     * @param metadata 属性容器
//     * @param message outlook实例
//     */
//    private void fillRecipients(Metadata metadata, OutlookMessage message) {
//        for (OutlookRecipient to : message.getToRecipients()) {
//            metadata.add("TO", StringUtils.nullToEmpty(to.getName()) + ";" +
//                    StringUtils.nullToEmpty(to.getAddress()));
//        }
//        for (OutlookRecipient cc : message.getCcRecipients()) {
//            metadata.add("CC", StringUtils.nullToEmpty(cc.getName()) + ";" +
//                    StringUtils.nullToEmpty(cc.getAddress()));
//        }
//        for (OutlookRecipient bcc : message.getBccRecipients()) {
//            metadata.add("BCC", StringUtils.nullToEmpty(bcc.getName()) + ";" +
//                    StringUtils.nullToEmpty(bcc.getAddress()));
//        }
//    }
//
//    /**
//     * 填充邮件发送时间
//     * @param metadata 属性容器
//     * @param message outlook实例
//     */
//    private void fillSentDate(Metadata metadata, OutlookMessage message) {
//        String dateStr = null;
//        Date date = Optional.ofNullable(message.getClientSubmitTime()).orElse(message.getDate());
//        try {
//            dateStr = DateUtils.format(date, "yyyy-MM-dd HH:mm:ss");
//        } catch (ValidateException e) {
//            // do nothing
//        }
//        if (dateStr != null) {
//            // 邮件发送时间
//            metadata.add("SENT_DATE", dateStr);
//        }
//    }
//
//    /**
//     * 填充邮件主题
//     * @param metadata 属性容器
//     * @param message outlook实例
//     * @param charset 邮件字符编码
//     */
//    private void fillSubject(Metadata metadata, OutlookMessage message, Charset charset) {
//        if (!StringUtils.isEmpty(message.getSubject())) {
//            metadata.add("SUBJECT", ExtractHelper.convert(message.getSubject(), charset));
//        }
//    }
//
//    /**
//     * 填充邮件内容
//     * @param object 详情对象
//     * @param message outlook实例
//     * @param charset 邮件字符编码
//     * @param htmlFormat 是否显示HTML格式
//     */
//    private void fillContent(LinkedObject object, OutlookMessage message, Charset charset, boolean htmlFormat) {
//        String tmp;
//
//        if (charset == null) {
//            tmp = htmlFormat ? message.getBodyHTML() : message.getConvertedBodyHTML();
//        } else {
//            tmp = htmlFormat ? message.getBodyHTML() : message.getBodyText();
//            // 若提取的Charset不同于系统设置的Charset
//            tmp = ExtractHelper.convert(tmp, charset);
//        }
//        object.setContent(tmp);
//    }
//
//    public static class Config extends ParseConfig {
//        /**
//         * 是否转换内容未html格式
//         */
//        private final boolean htmlFormat;
//        private final boolean detectCharset;
//
//        public Config(TupleObject config) {
//            super(config);
//            this.htmlFormat = "html".equals(TupleObjectHelper.getString(config, "textFormat", "text"));
//            // outlook默认检查字符集
//            this.detectCharset = TupleObjectHelper.getBoolean(config, "detectCharset", true);
//        }
//
//        @Override
//        public boolean detectCharset() {
//            return detectCharset;
//        }
//
//        public boolean htmlFormat() {
//            return htmlFormat;
//        }
//    }
}
