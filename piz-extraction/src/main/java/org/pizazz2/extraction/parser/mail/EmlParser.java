package org.pizazz2.extraction.parser.mail;

import org.pizazz2.exception.ValidateException;
import org.pizazz2.extraction.config.IConfig;
import org.pizazz2.extraction.data.ExtractObject;
import org.pizazz2.extraction.exception.DetectionException;
import org.pizazz2.extraction.exception.ParseException;
import org.pizazz2.extraction.parser.AbstractParser;
import org.pizazz2.extraction.process.IExtractListener;

/**
 * EML(message/rfc822)解析<br>
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
public class EmlParser extends AbstractParser {

//    @Override
//    public IConfig toConfig(TupleObject config) {
//        return new EmlParser.Config(config);
//    }

    @Override
    protected void doParse(ExtractObject object, IConfig config, IExtractListener listener) throws ParseException,
            ValidateException, DetectionException {
//        Email message = null;
//
//        try (InputStream in = new ByteArrayInputStream(object.getData())) {
//            message = EmailConverter.emlToEmail(in);
//        } catch (Exception e) {
//            super.throwException(object, config, e);
//        }
//        if (message != null) {
//            doParse(object, message, config.getTarget(EmlParser.Config.class));
//        }
    }

//    @SuppressWarnings("DuplicatedCode")
//    private void doParse(LinkedObject object, Email message, EmlParser.Config config)
//            throws ParseException, ValidateException, DetectionException {
//        Charset charset = !config.detectCharset() ? null : super.detect(null,
//                StringUtils.isEmpty(message.getPlainText()) ? message.getSubject() : message.getPlainText());
//        // 填充邮件属性
//        fillMetadata(object.getMetadata(), message, charset);
//        // 填充邮件内容
//        fillContent(object, message, charset, config.htmlFormat());
//        // 填充邮件附件
//        fillAttachment(object, message, config, charset);
//    }

    @Override
    public String[] getType() {
        return new String[] { "message/rfc822" };
    }

//    private void fillMetadata(Metadata metadata, Email message, Charset charset) {
//        if (!StringUtils.isEmpty(message.getId())) {
//            // 邮件ID
//            metadata.add("ID", message.getId());
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
//    private void fillAttachment(LinkedObject parent, Email message, EmlParser.Config config, Charset charset)
//            throws ParseException {
//        Path tmp = StringUtils.isTrimEmpty(parent.getSource()) ? null : Paths.get(parent.getSource());
//
//        for (AttachmentResource item :message.getAttachments()) {
//            DataSource source = item.getDataSource();
//            byte[] data;
//            try {
//                data = IOUtils.toByteArray(source.getInputStream());
//            } catch (UtilityException | IOException e) {
//                parent.setStatus(LinkedObject.StatusEnum.BROKEN);
//
//                if (!config.ignoreException()) {
//                    throw new ParseException(ExtractCodeEnum.ETT_04, e.getMessage(), e);
//                }
//                data = ArrayUtils.EMPTY_BYTE;
//            }
//            String fileName = ExtractHelper.convert(source.getName(), charset);
//            super.addAttachment(parent, fileName, tmp != null ? tmp.resolve(parent.getId()).toString() :
//                    StringUtils.EMPTY).setData(data);
//        }
//    }
//
//    private void fillFromRecipient(Metadata metadata, Email message) {
//        Recipient recipient = message.getFromRecipient();
//
//        if (recipient != null) {
//            metadata.add("FROM", StringUtils.nullToEmpty(recipient.getName()) + ";" +
//                    StringUtils.nullToEmpty(recipient.getAddress()));
//        }
//    }
//
//    /**
//     * 填充邮件接收者
//     * @param metadata 属性容器
//     * @param message eml实例
//     */
//    private void fillRecipients(Metadata metadata, Email message) {
//        for (Recipient item : message.getRecipients()) {
//            if (item.getType() != null) {
//                metadata.add(item.getType().toString().toUpperCase(), StringUtils.nullToEmpty(item.getName()) + ";" +
//                        StringUtils.nullToEmpty(item.getAddress()));
//            }
//        }
//    }
//
//    /**
//     * 填充邮件发送时间
//     * @param metadata 属性容器
//     * @param message eml实例
//     */
//    private void fillSentDate(Metadata metadata, Email message) {
//        String dateStr = null;
//        Date date = message.getSentDate();
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
//     * @param message eml实例
//     * @param charset 邮件字符编码
//     */
//    private void fillSubject(Metadata metadata, Email message, Charset charset) {
//        if (!StringUtils.isEmpty(message.getSubject())) {
//            metadata.add("SUBJECT", ExtractHelper.convert(message.getSubject(), charset));
//        }
//    }
//
//    /**
//     * 填充邮件内容
//     * @param object 详情对象
//     * @param message eml实例
//     * @param charset 邮件字符编码
//     * @param htmlFormat 是否显示HTML格式
//     */
//    private void fillContent(LinkedObject object, Email message, Charset charset, boolean htmlFormat) {
//        String tmp = htmlFormat ? message.getHTMLText() : message.getPlainText();
//
//        if (charset != null) {
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
//
//        public Config(TupleObject config) {
//            super(config);
//            this.htmlFormat = "html".equals(TupleObjectHelper.getString(config, "textFormat", "text"));
//        }
//
//        public boolean htmlFormat() {
//            return htmlFormat;
//        }
//    }
}
