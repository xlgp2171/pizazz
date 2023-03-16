package org.pizazz2.extraction.parser.mail;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.codec.DecoderUtil;
import org.apache.james.mime4j.dom.address.*;
import org.apache.james.mime4j.dom.field.*;
import org.apache.james.mime4j.field.LenientFieldParser;
import org.apache.james.mime4j.message.DefaultBodyDescriptorBuilder;
import org.apache.james.mime4j.message.MaximalBodyDescriptor;
import org.apache.james.mime4j.parser.ContentHandler;
import org.apache.james.mime4j.parser.MimeStreamParser;
import org.apache.james.mime4j.stream.BodyDescriptor;
import org.apache.james.mime4j.stream.Field;
import org.apache.james.mime4j.stream.MimeConfig;
import org.apache.tika.metadata.Message;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.mailcommons.MailDateParser;
import org.apache.tika.parser.mailcommons.MailUtil;
import org.pizazz2.PizContext;
import org.pizazz2.common.*;
import org.pizazz2.data.TupleObject;
import org.pizazz2.exception.IllegalException;
import org.pizazz2.exception.UtilityException;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.extraction.process.IExtractListener;
import org.pizazz2.extraction.config.IConfig;
import org.pizazz2.extraction.config.ParseConfig;
import org.pizazz2.extraction.data.ExtractObject;
import org.pizazz2.extraction.exception.DetectionException;
import org.pizazz2.extraction.exception.ParseException;
import org.pizazz2.extraction.parser.AbstractParser;
import org.pizazz2.extraction.support.ExtractHelper;
import org.pizazz2.helper.TupleObjectHelper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;
import java.util.function.BiConsumer;

/**
 * RFC822(message/rfc822)解析<br>
 * 解析属性Metadata包括：
 *
 * @author xlgp2171
 * @version 2.2.230310
 */
public class Rfc822Parser extends AbstractParser {
    static final String KEY_MULTIPART_ALTERNATIVE = "multipart/alternative";
    static final String KEY_APPLICATION_RTF = "application/rtf";

    public static final String KEY_ATTACHMENT = "attachment";
    public static final String KEY_FROM = "From";
    public static final String KEY_SUBJECT = "Subject";
    public static final String KEY_TO = "To";
    public static final String KEY_CC = "Cc";
    public static final String KEY_BCC = "Bcc";
    public static final String KEY_DATE = "Date";
    public static final String KEY_CONTENT_TYPE = "Content-Type";

    @Override
    public IConfig toConfig(TupleObject config) {
        return new Config(config);
    }

    @Override
    protected void doParse(ExtractObject object, IConfig config, IExtractListener listener) throws ParseException,
            ValidateException, IllegalException, DetectionException {
        Config tmp = config.getTarget(Config.class);
        MimeStreamParser parser = new MimeStreamParser(tmp.mineConfig(), null,
                new DefaultBodyDescriptorBuilder());
        parser.setContentHandler(new Handler(object, tmp));
        parser.setContentDecoding(true);
        parser.setNoRecurse();

        try (InputStream in = new ByteArrayInputStream(object.getData())) {
            parser.parse(in);
        } catch (IOException | MimeException e) {
            super.throwException(object, tmp, e);
        }
    }

    @Override
    public String[] getType() {
        return new String[] { "message/rfc822" };
    }

    public static class Config extends ParseConfig {
        /**
         * 是否转换内容未html格式
         */
        private final boolean htmlFormat;
        private final boolean extractAll;
        private final MimeConfig mimeConfig;

        public Config(TupleObject config) throws IllegalException {
            super(config);
            this.htmlFormat = "html".equals(TupleObjectHelper.getString(config, "textFormat", "text"));
            this.extractAll = TupleObjectHelper.getBoolean(config, "extractAll", false);
            this.mimeConfig = new MimeConfig.Builder()
                    .setMaxHeaderLen(TupleObjectHelper
                            .getInt(config, "maxHeaderLen", NumberUtils.NEGATIVE_ONE.intValue()))
                    .setMaxLineLen(TupleObjectHelper
                            .getInt(config, "maxLineLen", NumberUtils.NEGATIVE_ONE.intValue())).build();
        }

        public boolean htmlFormat() {
            return htmlFormat;
        }

        public boolean extractAll() {
            return extractAll;
        }

        public MimeConfig mineConfig() {
            return mimeConfig;
        }

    }

    /**
     * 参考{@link org.apache.tika.parser.mail}class MailContentHandler
     */
    private class Handler implements ContentHandler {

        private final Metadata metadata;
        private final boolean extractAllAlternatives;
        private final ExtractObject object;
        private final Config config;
        private final StringBuilder content = new StringBuilder();
        // this is used to buffer a multipart body that
        // keeps track of multipart/alternative and its children
        private final Stack<Part> alternativePartBuffer = new Stack<>();
        private final Stack<BodyDescriptor> parts = new Stack<>();

        Handler(ExtractObject object, Config config) {
            this.object = object;
            this.config = config;
            this.extractAllAlternatives = config.extractAll();
            this.metadata = object.getMetadata();
        }

        @Override
        public void startMessage() throws MimeException {
            // 开始解析邮件
        }

        @Override
        public void startHeader() throws MimeException {
            // 邮件header开始处理时，内容header开始处理时
        }

        /**
         * Header for the whole message or its parts
         *
         * @see <a href="http://james.apache.org/mime4j/apidocs/org/apache/james/mime4j/parser/">
         * http://james.apache.org/mime4j/apidocs/org/apache/james/mime4j/parser/</a>
         * Field.html
         */
        @Override
        public void field(Field field) throws MimeException {
            // if we're in a part, skip.
            // We want to gather only the metadata for the whole msg.
            if (parts.size() <= 0) {
                try {
                    field0(field);
                } catch (Exception e) {
                    LOGGER.warn("PARSE MAIL FILED:" + e.getMessage() + ",field=" + field);
                }
            }
        }

        private void field0(Field field) throws Exception {
            String fieldName = field.getName();
            ParsedField parsedField = LenientFieldParser.getParser().parse(field, DecodeMonitor.SILENT);

            if (KEY_FROM.equalsIgnoreCase(fieldName)) {
                MailboxListField fromField = (MailboxListField) parsedField;
                MailboxList mailboxList = fromField.getMailboxList();

                if (fromField.isValidField() && !CollectionUtils.isEmpty(mailboxList)) {
                    for (Address item : mailboxList) {
                        // 和源文件不一致，未使用MailUtil.setPersonAndEmail方法
                        fillDisplayString(item, (name, address) -> {
                            metadata.add(Metadata.MESSAGE_FROM_NAME, StringUtils.nullToEmpty(name));
                            metadata.add(Metadata.MESSAGE_FROM_EMAIL, StringUtils.nullToEmpty(address));
                        });
                    }
                } else {
                    String from = stripOutFieldPrefix(field, KEY_FROM + ":");
                    MailUtil.setPersonAndEmail(from, Message.MESSAGE_FROM_NAME, Message.MESSAGE_FROM_EMAIL, metadata);
                }
            } else if (KEY_SUBJECT.equalsIgnoreCase(fieldName)) {
                metadata.set(TikaCoreProperties.TITLE, ((UnstructuredField) parsedField).getValue());
                metadata.set(TikaCoreProperties.SUBJECT, ((UnstructuredField) parsedField).getValue());
            } else if (KEY_TO.equalsIgnoreCase(fieldName)) {
                processAddressList(parsedField, KEY_TO + ":", (name, address) -> {
                    metadata.add(Metadata.MESSAGE_TO_NAME, StringUtils.nullToEmpty(name));
                    metadata.add(Metadata.MESSAGE_TO_EMAIL, StringUtils.nullToEmpty(address));
                });
            } else if (KEY_CC.equalsIgnoreCase(fieldName)) {
                processAddressList(parsedField, KEY_CC + ":", (name, address) -> {
                    metadata.add(Metadata.MESSAGE_CC_NAME, StringUtils.nullToEmpty(name));
                    metadata.add(Metadata.MESSAGE_CC_EMAIL, StringUtils.nullToEmpty(address));
                });
            } else if (KEY_BCC.equalsIgnoreCase(fieldName)) {
                processAddressList(parsedField, KEY_BCC + ":", (name, address) -> {
                    metadata.add(Metadata.MESSAGE_BCC_NAME, StringUtils.nullToEmpty(name));
                    metadata.add(Metadata.MESSAGE_BCC_EMAIL, StringUtils.nullToEmpty(address));
                });
            } else if (KEY_DATE.equalsIgnoreCase(fieldName)) {
                String dateBody = parsedField.getBody();
                Date date = null;
                try {
                    date = MailDateParser.parseDateLenient(dateBody);
                } catch (Exception e) {
                    LOGGER.warn("DATE VALIDATE:" + e.getMessage() + ",date=" + dateBody);
                }
                if (date != null) {
                    metadata.set(TikaCoreProperties.CREATED, DateUtils.format(date, DateUtils.DEFAULT_FORMAT));
                }
            } else if (KEY_CONTENT_TYPE.equalsIgnoreCase(fieldName)) {
                final MediaType contentType = MediaType.parse(parsedField.getBody());

                if (contentType.getType().equalsIgnoreCase("multipart")) {
                    metadata.set(Message.MULTIPART_SUBTYPE, contentType.getSubtype());
                    metadata.set(Message.MULTIPART_BOUNDARY, contentType.getParameters().get("boundary"));
                } else {
                    metadata.add(Metadata.MESSAGE_RAW_HEADER_PREFIX + parsedField.getName(), field.getBody());
                }
            } else {
                // metadata.add(Metadata.MESSAGE_RAW_HEADER_PREFIX + parsedField.getName(), field.getBody());
            }
        }

        @Override
        public void endHeader() throws MimeException {
            // header结束时触发
        }

        @Override
        public void startMultipart(BodyDescriptor descriptor) throws MimeException {
            parts.push(descriptor);

            if (!extractAllAlternatives) {
                if (alternativePartBuffer.isEmpty() &&
                        KEY_MULTIPART_ALTERNATIVE.equalsIgnoreCase(descriptor.getMimeType())) {
                    alternativePartBuffer.push(new Part(descriptor));
                } else if (alternativePartBuffer.size() > 0) {
                    //add the part to the stack
                    Part parent = alternativePartBuffer.peek();
                    Part part = new Part(descriptor);
                    alternativePartBuffer.push(part);

                    if (parent != null) {
                        parent.children.add(part);
                    }
                }
            }
        }

        @Override
        public void preamble(InputStream is) throws MimeException, IOException {
            // 序言处理时触发
        }

        @Override
        public void startBodyPart() throws MimeException {
            // 内容开始处理时
        }

        @Override
        public void body(BodyDescriptor body, InputStream is) throws MimeException, IOException {
            // use a different metadata object
            // in order to specify the mime type of the
            // sub part without damaging the main metadata
            Metadata subMetadata = new Metadata();
            subMetadata.set(Metadata.CONTENT_TYPE, body.getMimeType());
            subMetadata.set(Metadata.CONTENT_ENCODING, body.getCharset());
            // TIKA-2455: flag the containing type.
            if (parts.size() > 0) {
                subMetadata.set(Message.MULTIPART_SUBTYPE, parts.peek().getSubType());
                subMetadata.set(Message.MULTIPART_BOUNDARY, parts.peek().getBoundary());
            }
            byte[] data;
            try {
                data = IOUtils.toByteArray(is);
            } catch (UtilityException e) {
                LOGGER.warn(e.getMessage() + ",id=" + object);
                return;
            }
            if (body instanceof MaximalBodyDescriptor) {
                MaximalBodyDescriptor maximalBody = (MaximalBodyDescriptor) body;
                String contentDispositionType = maximalBody.getContentDispositionType();

                if (KEY_ATTACHMENT.equalsIgnoreCase(contentDispositionType)) {
                    String fileName = maximalBody.getContentDispositionFilename();

                    if (!StringUtils.isEmpty(fileName)) {
                        fileName = DecoderUtil.decodeEncodedWords(fileName, DecodeMonitor.SILENT);
                    }
                    Path tmp = StringUtils.isTrimEmpty(object.getSource()) ? null : Paths.get(object.getSource());
                    String source = tmp != null ? tmp.resolve(object.getId()).toString() : StringUtils.EMPTY;
                    ExtractHelper.addAttachment(object, fileName, source, new Metadata()).setData(data);
                    // 附件录入到此结束
                    return;
                } else if (!StringUtils.isTrimEmpty(contentDispositionType)) {
                    LOGGER.warn("UNSUPPORTED CONTENT_DISPOSITION_TYPE:" + contentDispositionType + ",id=" +
                            object.toString());
                }
            }
            //if we're in a multipart/alternative or any one of its children
            //add the bodypart to the latest that was added
            if (!extractAllAlternatives && alternativePartBuffer.size() > 0) {
                alternativePartBuffer.peek().children.add(new BodyContents(subMetadata, data));
            } else if (!extractAllAlternatives && parts.size() < 2) {
                // if you're at the first level of embedding
                // and you're not in an alternative part block
                // and you're text/html, put that in the body of the email
                // otherwise treat as a regular attachment
                handleBodyPart(new BodyContents(subMetadata, data));
            } else {
                // else handle as you would any other embedded content
                handleBodyPart(new BodyContents(subMetadata, data));
            }
        }

        @Override
        public void endBodyPart() throws MimeException {
            // 内容结束处理时
        }

        @Override
        public void epilogue(InputStream is) throws MimeException, IOException {
            // 后记处理时
        }

        @Override
        public void endMultipart() throws MimeException {
            if (alternativePartBuffer.size() == 1) {
                Part alternativeRoot = alternativePartBuffer.pop();
                try {
                    handleBestParts(alternativeRoot);
                } catch (IOException e) {
                    throw new MimeException(e);
                }
            } else if (alternativePartBuffer.size() > 1) {
                alternativePartBuffer.pop();
            }
            // test that parts has something
            // if it doesn't, there's a problem with the file
            // e.g. more endMultiPart than startMultipart
            // we're currently silently swallowing this
            if (parts.size() > 0) {
                parts.pop();
            }
        }

        @Override
        public void endMessage() throws MimeException {
            object.setContent(ExtractHelper.tryCleanBlankLine(content.toString(), config.cleanLine()));
        }

        @Override
        public void raw(InputStream is) throws MimeException, IOException {
        }

        private void processAddressList(ParsedField field, String addressListType, BiConsumer<String, String> consumer) {
            AddressListField toField = (AddressListField) field;

            if (toField.isValidField()) {
                AddressList addressList = toField.getAddressList();

                for (Address address : addressList) {
                    fillDisplayString(address, consumer);
                }
            } else {
                String to = stripOutFieldPrefix(field, addressListType);

                for (String eachTo : to.split(",")) {
                    consumer.accept(eachTo, StringUtils.EMPTY);
                }
            }
        }

        private void fillDisplayString(Address address, BiConsumer<String, String> consumer) {
            String name = StringUtils.EMPTY;

            if (address instanceof Mailbox) {
                Mailbox mailbox = (Mailbox) address;
                name = mailbox.getName();

                if (!StringUtils.isEmpty(name)) {
                    name = DecoderUtil.decodeEncodedWords(name, DecodeMonitor.SILENT);
                }
                consumer.accept(name, mailbox.getAddress());
            } else if (address instanceof Group) {
                Group group = (Group) address;

                for (Mailbox item : group.getMailboxes()) {
                    fillDisplayString(item, consumer);
                }
            } else {
                consumer.accept(name, address.toString());
            }
        }

        private String stripOutFieldPrefix(Field field, String fieldName) {
            String temp = field.getRaw().toString();
            int loc = fieldName.length();

            while (temp.charAt(loc) == ' ') {
                loc++;
            }
            return temp.substring(loc);
        }

        private void handleBestParts(Part part) throws IOException {
            if (part == null) {
                return;
            }
            if (part instanceof BodyContents) {
                handleBodyPart((BodyContents) part);
                return;
            }
            if (KEY_MULTIPART_ALTERNATIVE.equalsIgnoreCase(part.descriptor.getMimeType())) {
                int bestPartScore = -1;
                Part bestPart = null;

                for (Part alternative : part.children) {
                    int score = score(alternative);

                    if (score > bestPartScore) {
                        bestPart = alternative;
                        bestPartScore = score;
                    }
                }
                handleBestParts(bestPart);
            } else {
                for (Part child : part.children) {
                    handleBestParts(child);
                }
            }
        }
        /**
         * 提取内容并追加
         *
         * @param part 内容
         */
        private void handleBodyPart(BodyContents part) {
            String contentType = part.metadata.get(Metadata.CONTENT_TYPE);
            String tmp = StringUtils.EMPTY;
            ExtractObject object = ExtractHelper.newTempObject().setTypeString(contentType).setData(part.bytes);
            try {
                tmp = Rfc822Parser.super.extract(object, TupleObjectHelper.emptyObject());
            } catch (DetectionException | ParseException | ValidateException | IllegalException e) {
                // do nothing
            }
            if (!StringUtils.isTrimEmpty(tmp)) {
                content.append(tmp).append(PizContext.LINE_SEPARATOR);
            }
        }

        private int score(Part part) {
            if (part == null) {
                return 0;
            }
            if (part instanceof BodyContents) {
                String contentType = ((BodyContents) part).metadata.get(Metadata.CONTENT_TYPE);
                if (contentType == null) {
                    return 0;
                } else if (contentType.equalsIgnoreCase(MediaType.TEXT_PLAIN.toString())) {
                    return 1;
                } else if (contentType.equalsIgnoreCase(KEY_APPLICATION_RTF)) {
                    // TODO -- is this the right definition in rfc822 for rich text?!
                    return 2;
                } else if (contentType.equalsIgnoreCase(MediaType.TEXT_HTML.toString())) {
                    return 3;
                }
            }
            return 4;
        }
    }

    private static class Part {
        private final BodyDescriptor descriptor;
        private final List<Part> children = new ArrayList<>();

        public Part(BodyDescriptor descriptor) {
            this.descriptor = descriptor;
        }

        @Override
        public String toString() {
            return "Part{" + "bodyDescriptor=" + descriptor + ", children=" + children + '}';
        }
    }

    private static class BodyContents extends Part {
        private final Metadata metadata;
        private final byte[] bytes;

        private BodyContents(Metadata metadata, byte[] bytes) {
            super(null);
            this.metadata = metadata;
            this.bytes = bytes;
        }
    }
}
