package org.pizazz2.extraction.parser.mail;

import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.codec.DecoderUtil;
import org.apache.poi.hsmf.MAPIMessage;
import org.apache.poi.hsmf.datatypes.*;
import org.apache.poi.hsmf.exceptions.ChunkNotFoundException;
import org.apache.poi.poifs.filesystem.*;
import org.apache.poi.util.CodePageUtil;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Message;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.html.HtmlEncodingDetector;
import org.apache.tika.parser.mbox.MboxParser;
import org.apache.tika.parser.microsoft.OfficeParser;
import org.apache.tika.parser.microsoft.OutlookExtractor;
import org.apache.tika.parser.pkg.ZipContainerDetector;
import org.apache.tika.utils.ExceptionUtils;
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
import org.pizazz2.extraction.exception.CodeEnum;
import org.pizazz2.extraction.exception.ParseException;
import org.pizazz2.extraction.parser.AbstractParser;
import org.pizazz2.extraction.support.ExtractHelper;
import org.pizazz2.extraction.support.ParseHelper;
import org.pizazz2.helper.TupleObjectHelper;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * outlook(application/vnd.ms-outlook)解析<br>
 * 解析属性Metadata包括：
 *
 * @author xlgp2171
 * @version 2.1.211103
 */
public class OutlookParser extends AbstractParser {

    static final Metadata EMPTY_METADATA = new Metadata();
    static final Pattern HEADER_KEY_PAT = Pattern.compile("\\A([\\x21-\\x39\\x3B-\\x7E]+):(.*?)\\Z");
    static final Pattern CHARSET_PAT = Pattern.compile("Content-Type:.*?charset=[\"']?([^;'\"]+)[\"']?",
            Pattern.CASE_INSENSITIVE);
    private final HtmlEncodingDetector detector = new HtmlEncodingDetector();

    @Override
    public IConfig toConfig(TupleObject config) {
        return new Config(config);
    }

    @Override
    protected void doParse(ExtractObject object, IConfig config, IExtractListener listener) throws ParseException,
            ValidateException, IllegalException, DetectionException {
        MAPIMessage message = null;

        try (InputStream in = new ByteArrayInputStream(object.getData())) {
            message = new MAPIMessage(in);
        } catch (IOException e) {
            super.throwException(object, config, e);
        }
        if (message != null) {
            doParse(object, message, config.getTarget(Config.class), listener);
            listener.parsed(object, config);
        }
    }

    private void doParse(ExtractObject object, DirectoryNode node, IConfig config, IExtractListener listener)
            throws ParseException, IllegalException, ValidateException {
        MAPIMessage message = null;
        try {
            message = new MAPIMessage(node);
        } catch (IOException e) {
            super.throwException(object, config, e);
        }
        if (message != null) {
            doParse(object, message, config.getTarget(Config.class), listener);
        }
    }

    private void doParse(ExtractObject object, MAPIMessage message, Config config, IExtractListener listener)
            throws ValidateException {
        // 解析不抛出异常
        message.setReturnNullOnMissingChunk(config.ignoreException());
        // 填充字符集
        fillEncoding(object.getMetadata(), message, config.detectLimit(), object.toString());
        // 填充邮件属性
        fillMetadata(object.getMetadata(), message, object.toString());
        // 填充邮件内容
        fillContent(object, message, config.htmlFormat(), config.cleanLine());
        // 填充邮件附件
        fillAttachment(object, message, config, listener);
    }

    @Override
    public String[] getType() {
        return new String[] { "application/vnd.ms-outlook" };
    }

    private void fillEncoding(Metadata metadata, MAPIMessage message, int limit, String id) {
        // 解析字符集
        if (message.has7BitEncodingStrings()) {
            guess7BitEncoding(metadata, message, limit, id);
        }
        if (metadata.get(Metadata.CONTENT_ENCODING) == null) {
            metadata.set(Metadata.CONTENT_ENCODING, UTF_8.name());
        }
    }

    private void fillMetadata(Metadata metadata, MAPIMessage message, String id) {
        // 邮件发送者
        fillFromRecipient(metadata, message);
        // 邮件接收者
        fillRecipients(metadata, message);
        // 邮件发送时间
        fillSentDate(metadata, message, id);
        // 邮件发送
        fillSubject(metadata, message, id);
    }

    private void fillAttachment(ExtractObject parent, MAPIMessage message, Config config, IExtractListener listener) {
        Path tmp = StringUtils.isTrimEmpty(parent.getSource()) ? null : Paths.get(parent.getSource());
        String source = tmp != null ? tmp.resolve(parent.getId()).toString() : StringUtils.EMPTY;

        for (AttachmentChunks item : message.getAttachmentFiles()) {
            String fileName = null;

            if (item.getAttachLongFileName() != null) {
                fileName = item.getAttachLongFileName().getValue();
            } else if (item.getAttachFileName() != null) {
                fileName = item.getAttachFileName().getValue();
            }
            if (item.getAttachData() != null) {
                // 修正文件名称编码
                if (!StringUtils.isEmpty(fileName)) {
                    fileName = ParseHelper.convert(fileName, parent.getMetadata().get(Metadata.CONTENT_ENCODING));
                }
                // 增加附件文件(未解析)
                super.addAttachment(parent, fileName, source).setData(item.getAttachData().getValue());
            }
            if (item.getAttachmentDirectory() != null) {
                try {
                    handleEmbeddedOfficeDoc(parent, source, item.getAttachmentDirectory().getDirectory(), config,
                           listener);
                } catch (IOException | DetectionException | ParseException | ValidateException | IllegalException e) {
                    LOGGER.error(e.getMessage());
                    parent.setStatus(ExtractObject.StatusEnum.BROKEN);
                }
            }
        }
    }

    private String nullToEmpty(Chunk target) {
        return target == null ? StringUtils.EMPTY : target.toString();
    }

    /**
     * 填充邮件发送者
     *
     * @param metadata 属性容器
     * @param message outlook实例
     */
    private void fillFromRecipient(Metadata metadata, MAPIMessage message) {
        Chunks chunks = message.getMainChunks();

        if (chunks != null) {
            metadata.add(Metadata.MESSAGE_FROM_NAME, nullToEmpty(chunks.getDisplayFromChunk()));
            metadata.add(Metadata.MESSAGE_FROM_EMAIL, nullToEmpty(chunks.getEmailFromChunk()));
        }
    }

    /**
     * 填充邮件接收者
     *
     * @param metadata 属性容器
     * @param message outlook实例
     */
    private void fillRecipients(Metadata metadata, MAPIMessage message) {
        for (RecipientChunks item : message.getRecipientDetailsChunks()) {
            StringChunk chunk = Optional.ofNullable(item.getRecipientDisplayNameChunk())
                    .orElse(item.getRecipientNameChunk());
            String name = chunk != null ? chunk.getValue() : StringUtils.EMPTY;
            //
            List<PropertyValue> vals = item.getProperties().get(MAPIProperty.RECIPIENT_TYPE);
            OutlookExtractor.RECIPIENT_TYPE recipientType = OutlookExtractor.RECIPIENT_TYPE.UNSPECIFIED;

            if (vals != null && vals.size() > 0) {
                Object val = vals.get(0).getValue();

                if (val instanceof Integer) {
                    recipientType = OutlookExtractor.RECIPIENT_TYPE.getTypeFromVal((int) val);
                }
            }
            String email = StringUtils.nullToEmpty(item.getRecipientEmailAddress());

            switch (recipientType) {
                case TO:
                    metadata.add(Message.MESSAGE_TO_NAME, name);
                    metadata.add(Message.MESSAGE_TO_EMAIL, email);
                    break;
                case CC:
                    metadata.add(Message.MESSAGE_CC_NAME, name);
                    metadata.add(Message.MESSAGE_CC_EMAIL, email);
                    break;
                case BCC:
                    metadata.add(Message.MESSAGE_BCC_NAME, name);
                    metadata.add(Message.MESSAGE_BCC_EMAIL, email);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 填充邮件发送时间
     *
     * @param metadata 属性容器
     * @param message outlook实例
     * @param id 邮件标识
     */
    private void fillSentDate(Metadata metadata, MAPIMessage message, String id) {
        Calendar calendar = null;
        String dateString = null;
        try {
            calendar = message.getMessageDate();
        } catch (ChunkNotFoundException e) {
            LOGGER.warn("MAIL DATE NOT FOUND,id=" + id);
        }
        if (calendar != null) {
            dateString = DateUtils.format(calendar.getTime(), "yyyy-MM-dd HH:mm:ss");
        } else {
            Map<String, String[]> headers = null;
            try {
                headers = normalizeHeaders(message.getHeaders());
            } catch (ChunkNotFoundException e) {
                // do nothing
            }
            if (!CollectionUtils.isEmpty(headers)) {
                for (Map.Entry<String, String[]> header : headers.entrySet()) {
                    String headerKey = header.getKey();

                    if (headerKey.toLowerCase().startsWith("date:")) {
                        String date = headerKey.substring(headerKey.indexOf(':') + 1).trim();
                        // See if we can parse it as a normal mail date
                        try {
                            Date d = MboxParser.parseDate(date);
                            dateString = DateUtils.format(d, "yyyy-MM-dd HH:mm:ss");
                        } catch (java.text.ParseException e) {
                            LOGGER.warn("UNSUPPORTED MAIL DATE FORMAT:" + date + ",id=" + id);
                            dateString = date;
                        }
                        break;
                    }
                }
            }
        }
        if (dateString != null) {
            metadata.set(TikaCoreProperties.CREATED, dateString);
        }
    }

    /**
     * 填充邮件主题
     *
     * @param metadata 属性容器
     * @param message outlook实例
     * @param id 邮件标识
     */
    private void fillSubject(Metadata metadata, MAPIMessage message, String id) {
        try {
            if (!StringUtils.isEmpty(message.getSubject())) {
                metadata.add(TikaCoreProperties.TITLE, message.getSubject());
            }
        } catch (ChunkNotFoundException e) {
            LOGGER.warn("MAIL SUBJECT NOT FOUND,id=" + id);
        }
    }

    /**
     * 填充邮件内容
     *
     * @param object 详情对象
     * @param message outlook实例
     * @param htmlFormat 是否显示HTML格式
     * @param cleanLine 是否去除多余的换行
     */
    private void fillContent(ExtractObject object, MAPIMessage message, boolean htmlFormat, boolean cleanLine) {
        Map<MAPIProperty, List<Chunk>> mainChunks = message.getMainChunks().getAll();

        if (htmlFormat) {
            // TODO HTML格式 参考org.apache.tika.parser.microsoft.OutlookExtractor#handleBodyChunks
        } else if (mainChunks.containsKey(MAPIProperty.BODY)) {
            List<Chunk> chunkList = mainChunks.get(MAPIProperty.BODY);

            if (!CollectionUtils.isEmpty(chunkList)) {
                String content = nullToEmpty(chunkList.get(0));
                object.setContent(ExtractHelper.tryCleanBlankLine(content, cleanLine));
            }
        }
    }

    /**
     * 参考{@link org.apache.tika.parser.microsoft}抽象类AbstractPOIFSExtractor的保护方法handleEmbeddedOfficeDoc<br>
     * Handle an office document that's embedded at the POIFS level
     *
     * @param parent 邮件对象
     * @param source 邮件路径
     * @param dir 内嵌outlook数据
     * @param config 解析配置
     * @param listener 监听
     */
    protected void handleEmbeddedOfficeDoc(ExtractObject parent, String source, DirectoryNode dir, Config config,
                                           IExtractListener listener) throws IOException, DetectionException,
            ParseException, ValidateException, IllegalException {
        // Is it an embedded OLE2 document, or an embedded OOXML document?
        if (dir.hasEntry("Package")) {
            // It's OOXML (has a ZipFile):
            Entry ooxml = dir.getEntry("Package");

            try (TikaInputStream stream = TikaInputStream.get(new DocumentInputStream((DocumentEntry) ooxml))) {
                ZipContainerDetector detector = new ZipContainerDetector();
                MediaType type;
                try {
                    //if there's a stream error while detecting...
                    type = detector.detect(stream, new Metadata());
                } catch (Exception e) {
                    String msg = ExceptionUtils.getFilteredStackTrace(e);
                    throw new DetectionException(CodeEnum.ETT_04, msg, e);
                }
                byte[] data;
                try {
                    data = IOUtils.toByteArray(stream);
                } catch (UtilityException e) {
                    LOGGER.warn(e.getMessage() + ",name=" + dir.getName() + ",id=" + parent.getId());
                    data = ArrayUtils.EMPTY_BYTE;
                }
                // 增加附件文件(未解析)
                ExtractObject tmp = super.addAttachment(parent, dir.getName(), source);
                tmp.setType(type).setData(data);

                if (tmp.getType() != null) {
                    listener.detected(tmp);
                }
            }
        } else {
            handleEmbeddedOfficeDoc0(parent, source, dir, config, listener);
        }
    }

    private void handleEmbeddedOfficeDoc0(ExtractObject parent, String source, DirectoryNode dir, Config config,
                                          IExtractListener listener) throws ParseException, IllegalException,
            ValidateException {
        // It's regular OLE2:
        // What kind of document is it?
        Metadata metadata = new Metadata();
        metadata.set(Metadata.EMBEDDED_RELATIONSHIP_ID, dir.getName());
        if (dir.getStorageClsid() != null) {
            metadata.set(Metadata.EMBEDDED_STORAGE_CLASS_ID, dir.getStorageClsid().toString());
        }
        OfficeParser.POIFSDocumentType type = OfficeParser.POIFSDocumentType.detectType(dir);
        String rName = dir.getName();
        byte[] data = null;
        String contentType = StringUtils.EMPTY;

        if (type == OfficeParser.POIFSDocumentType.OLE10_NATIVE) {
            try {
                // Try to un-wrap the OLE10Native record:
                Ole10Native ole = Ole10Native.createFromEmbeddedOleObject(dir);

                if (ole.getLabel() != null) {
                    rName = rName + '/' + ole.getLabel();
                }
                data = ole.getDataBuffer();
            } catch (Ole10NativeException ex) {
                // Not a valid OLE10Native record, skip it
            } catch (Exception e) {
                String msg = ExceptionUtils.getFilteredStackTrace(e);
                throw new ParseException(CodeEnum.ETT_06, msg, e);
            }
        } else if (type == OfficeParser.POIFSDocumentType.COMP_OBJ) {
            byte[] contents;
            try {
                //TODO: figure out if the equivalent of OLE 1.0's
                //getCommand() and getFileName() exist for OLE 2.0 to populate
                //TikaCoreProperties.ORIGINAL_RESOURCE_NAME
                // Grab the contents and process
                DocumentEntry contentsEntry;
                try {
                    contentsEntry = (DocumentEntry) dir.getEntry("CONTENTS");
                } catch (FileNotFoundException ioe) {
                    contentsEntry = (DocumentEntry) dir.getEntry("Contents");
                }
                DocumentInputStream inp = new DocumentInputStream(contentsEntry);
                contents = new byte[contentsEntry.getSize()];
                inp.readFully(contents);
            } catch (Exception e) {
                String msg = ExceptionUtils.getFilteredStackTrace(e);
                throw new ParseException(CodeEnum.ETT_06, msg, e);
            }
            data = contents;
            rName = rName + type.getExtension();
        } else {
            contentType = StringUtils.of(type.getType().getBaseType());
            rName = rName + '.' + type.getExtension();
        }
        if (data == null) {
            ExtractObject current = new ExtractObject(ExtractHelper.generateId(), rName, source)
                    .setTypeString(getType()[0]);
            listener.detected(current);
            try {
                doParse(current, dir, config, listener);
                listener.parsed(current, config);
            } finally {
                current.archive(config.cleanData());
            }
            // 增加附件文件(已解析)
            parent.addAttachment(current);
        } else {
            // 增加附件文件(未解析)
            ExtractObject tmp = super.addAttachment(parent, rName, source);
            tmp.setTypeString(contentType).setData(data);

            if (tmp.getType() != null) {
                listener.detected(tmp);
            }
        }
    }

    /**
     * 参考{@link org.apache.tika.parser.microsoft.OutlookExtractor}的私有方法guess7BitEncoding
     *
     * @param metadata 属性容器
     * @param message outlook解析消息体
     * @param limit 识别数据限制
     * @param id 邮件标识
     */
    private void guess7BitEncoding(Metadata metadata, MAPIMessage message, int limit, String id) {
        Chunks mainChunks = message.getMainChunks();
        //sanity check
        if (mainChunks == null) {
            return;
        }
        Map<MAPIProperty, List<PropertyValue>> props = mainChunks.getProperties();

        if (props != null) {
            // First choice is a codepage property
            for (MAPIProperty prop : new MAPIProperty[] { MAPIProperty.MESSAGE_CODEPAGE, MAPIProperty.INTERNET_CPID }) {
                List<PropertyValue> val = props.get(prop);

                if (val != null && val.size() > 0) {
                    int codepage = ((PropertyValue.LongPropertyValue) val.get(0)).getValue();
                    String encoding = null;
                    try {
                        encoding = CodePageUtil.codepageToEncoding(codepage, true);
                    } catch (UnsupportedEncodingException e) {
                        LOGGER.warn("UNSUPPORTED MAIL ENCODING:" + e.getMessage() + ",id=" + id);
                    }
                    if (tryToSet7BitEncoding(metadata, message, encoding)) {
                        return;
                    }
                }
            }
        }
        // Second choice is a charset on a content type header
        try {
            String[] headers = message.getHeaders();

            if (headers != null && headers.length > 0) {
                // Look for a content type with a charset
                for (String header : headers) {
                    if (header.startsWith("Content-Type")) {
                        Matcher m = CHARSET_PAT.matcher(header);

                        if (m.matches()) {
                            // Found it! Tell all the string chunks
                            String charset = m.group(1);

                            if (tryToSet7BitEncoding(metadata, message, charset)) {
                                return;
                            }
                        }
                    }
                }
            }
        } catch (ChunkNotFoundException e) {
            // do nothing
        }
        // Nothing suitable in the headers, try HTML
        // TODO: do we need to replicate this in Tika? If we wind up
        // parsing the html version of the email, this is duplicative??
        // Or do we need to reset the header strings based on the html
        // meta header if there is no other information?
        try {
            String html = message.getHtmlBody();

            if (html != null && html.length() > 0) {
                Charset charset = null;
                try {
                    charset = detector.detect(new ByteArrayInputStream(html.getBytes(UTF_8)), EMPTY_METADATA);
                } catch (IOException e) {
                    //swallow
                }
                if (charset != null && tryToSet7BitEncoding(metadata, message, charset.name())) {
                    return;
                }
            }
        } catch (ChunkNotFoundException e) {
            // do nothing
        }
        //absolute last resort, try charset detector
        StringChunk text = mainChunks.getTextBodyChunk();

        if (text != null) {
            Charset charset = super.detect(text.getRawValue(), limit, null);

            if (charset != null) {
                tryToSet7BitEncoding(metadata, message, charset.name());
            }
        }
    }

    /**
     * 参考{@link org.apache.tika.parser.microsoft.OutlookExtractor}的私有方法tryToSet7BitEncoding
     *
     * @param metadata 属性容器
     * @param message outlook解析消息体
     * @param charsetName 字符集名称
     * @return 是否设置字符集到消息体
     */
    private boolean tryToSet7BitEncoding(Metadata metadata, MAPIMessage message, String charsetName) {
        if (charsetName == null) {
            return false;
        }
        if (charsetName.equalsIgnoreCase(UTF_8.name())) {
            return false;
        }
        try {
            if (Charset.isSupported(charsetName)) {
                metadata.set(Metadata.CONTENT_ENCODING, charsetName);
                message.set7BitEncoding(charsetName);
                return true;
            }
        } catch (IllegalCharsetNameException | UnsupportedCharsetException e) {
            // swallow
        }
        return false;
    }

    /**
     * 参考{@link org.apache.tika.parser.microsoft.OutlookExtractor}的私有方法normalizeHeaders<br>
     * As of 3.15, POI currently returns header[] by splitting on /\r?\n/<br>
     * this rebuilds headers that are broken up over several lines<br>
     * this also decodes encoded headers.
     *
     * @param rows 每行header
     * @return 解析后的header结果
     */
    private Map<String, String[]> normalizeHeaders(String[] rows) {
        Map<String, String[]> ret = new LinkedHashMap<>();

        if (rows == null) {
            return ret;
        }
        StringBuilder sb = new StringBuilder();
        Map<String, List<String>> headers = new LinkedHashMap<>();
        Matcher headerKeyMatcher = HEADER_KEY_PAT.matcher("");
        String lastKey = null;
        int consec = 0;

        for (String row : rows) {
            headerKeyMatcher.reset(row);

            if (headerKeyMatcher.find()) {
                if (lastKey != null) {
                    List<String> vals = headers.get(lastKey);
                    vals = (vals == null) ? new ArrayList<>() : vals;
                    vals.add(decodeHeader(sb.toString()));
                    headers.put(lastKey, vals);
                }
                //reset sb
                sb.setLength(0);
                lastKey = headerKeyMatcher.group(1).trim();
                sb.append(headerKeyMatcher.group(2).trim());
                consec = 0;
            } else {
                if (consec > 0) {
                    sb.append("\n");
                }
                sb.append(row);
            }
            consec++;
        }
        //make sure to add the last value
        if (sb.length() > 0 && lastKey != null) {
            List<String> vals = headers.get(lastKey);
            vals = (vals == null) ? new ArrayList<>() : vals;
            vals.add(decodeHeader(sb.toString()));
            headers.put(lastKey, vals);
        }
        //convert to array
        for (Map.Entry<String, List<String>> e : headers.entrySet()) {
            ret.put(e.getKey(), e.getValue().toArray(new String[0]));
        }
        return ret;

    }

    /**
     * 参考{@link org.apache.tika.parser.microsoft.OutlookExtractor}的私有方法decodeHeader
     *
     * @param header header
     * @return 解析后的header
     */
    private String decodeHeader(String header) {
        return DecoderUtil.decodeEncodedWords(header, DecodeMonitor.SILENT);
    }

    public static class Config extends ParseConfig {
        /**
         * 是否转换内容未html格式
         */
        private final boolean htmlFormat;

        public Config(TupleObject config) throws IllegalException {
            super(config);
            this.htmlFormat = "html".equals(TupleObjectHelper.getString(config, "textFormat", "text"));
        }

        public boolean htmlFormat() {
            return htmlFormat;
        }
    }
}
