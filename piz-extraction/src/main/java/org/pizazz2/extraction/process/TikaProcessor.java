package org.pizazz2.extraction.process;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.ExpandedTitleContentHandler;
import org.pizazz2.PizContext;
import org.pizazz2.common.ValidateUtils;
import org.pizazz2.exception.IllegalException;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.extraction.exception.DetectionException;
import org.pizazz2.extraction.exception.CodeEnum;
import org.pizazz2.extraction.exception.ParseException;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.Charset;

/**
 * 执行器基类<br>
 * 可参考使用ForkParser
 *
 * @author xlgp2171
 * @version 2.1.211104
 */
public class TikaProcessor {
    private final TikaConfig config;
    private final Parser parser;
    private final ParseContext context;

    public TikaProcessor() {
        // 配置可由System.getProperty("tika.config")或System.getenv("TIKA_CONFIG")初始化
        config = TikaConfig.getDefaultConfig();
        //
        parser = new AutoDetectParser(config);
        context = new ParseContext();
        context.set(Parser.class, parser);
    }

    public MediaType detect(byte[] data, Metadata metadata) throws DetectionException, ValidateException {
        ValidateUtils.notEmpty("detect", data, 1);
        metadata = metadata == null ? new Metadata() : metadata;

        try (TikaInputStream input = TikaInputStream.get(data, metadata)) {
            // 简化类型识别
            return config.getDetector().detect(input, metadata);
        } catch (IOException e) {
            throw new DetectionException(CodeEnum.ETT_01, "DETECT:" + e.getMessage(), e);
        }
    }

    public String extract(byte[] data, Metadata metadata, Charset charset, HandlerEnum format)
            throws ParseException, IllegalException {
        // 默认初始化大小5MB
        ByteArrayOutputStream tmp = new ByteArrayOutputStream(5 * 1024 * 1024);
        ContentHandler handler = format.newHandler(tmp, charset,false);
        extract(data, metadata, handler);
        return tmp.toString();
    }


    private void extract(byte[] data, Metadata metadata, ContentHandler handler) throws ParseException {
        try (TikaInputStream stream = TikaInputStream.get(data, metadata)) {
            //             ContentHandler handler = new BodyContentHandler(-1);
            parser.parse(stream, handler, metadata, context);
        } catch (IOException | SAXException | TikaException e) {
            throw new ParseException(CodeEnum.ETT_05, "EXTRACT:" + e.getMessage(), e);
        }
    }

    public interface IHandler {
        /**
         * 新的内容处理器
         *
         * @param output 输出IO
         * @param charset 字符编码
         * @param prettyPrint 按格式输出
         * @return 内容处理器
         *
         * @throws IllegalException 内容处理器生成异常
         */
        ContentHandler newHandler(OutputStream output, Charset charset, boolean prettyPrint) throws IllegalException;
    }

    public enum HandlerEnum implements IHandler {
        /** 文本格式 */
        TEXT {
            @Override
            public ContentHandler newHandler(OutputStream output, Charset charset, boolean prettyPrint)
                    throws IllegalException {
                return new BodyContentHandler(HandlerEnum.getOutputWriter(output, charset));
            }
        },
        /** HTML格式 */
        HTML {
            @Override
            public ContentHandler newHandler(OutputStream output, Charset charset, boolean prettyPrint)
                    throws IllegalException {
                try {
                    return new ExpandedTitleContentHandler(
                            HandlerEnum.getTransformerHandler(output, "html", charset, prettyPrint));
                } catch (TransformerConfigurationException e) {
                    throw new IllegalException("NEW HTML HANDLER:" + e.getMessage(), e);
                }
            }
        },
        /** XML格式 */
        XML {
            @Override
            public ContentHandler newHandler(OutputStream output, Charset charset, boolean prettyPrint)
                    throws IllegalException {
                try {
                    return HandlerEnum.getTransformerHandler(output, "xml", charset, prettyPrint);
                } catch (TransformerConfigurationException e) {
                    throw new IllegalException("NEW XML HANDLER:" + e.getMessage(), e);
                }
            }
        };

        static Writer getOutputWriter(OutputStream output, Charset charset) {
            return new OutputStreamWriter(output, charset != null ? charset : PizContext.LOCAL_ENCODING);
        }

        static TransformerHandler getTransformerHandler(OutputStream output, String method, Charset charset,
                                                        boolean prettyPrint) throws TransformerConfigurationException {
            SAXTransformerFactory factory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
            TransformerHandler handler = factory.newTransformerHandler();
            handler.getTransformer().setOutputProperty(OutputKeys.METHOD, method);
            handler.getTransformer().setOutputProperty(OutputKeys.INDENT, prettyPrint ? "yes" : "no");

            if (charset != null) {
                handler.getTransformer().setOutputProperty(OutputKeys.ENCODING, charset.name());
            }
            handler.setResult(new StreamResult(output));
            return handler;
        }

        public static HandlerEnum from(String format) {
            for (HandlerEnum item : values()) {
                if (item.name().equalsIgnoreCase(format)) {
                    return item;
                }
            }
            return null;
        }
    }
}
