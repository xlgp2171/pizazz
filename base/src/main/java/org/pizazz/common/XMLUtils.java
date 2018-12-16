package org.pizazz.common;

import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.pizazz.exception.BaseException;
import org.pizazz.message.BasicCodeEnum;
import org.pizazz.message.TypeEnum;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * XML操作工具
 * 
 * @author xlgp2171
 * @version 1.0.181210
 */
public class XMLUtils {

	public static Document getDocument(String resource) throws BaseException {
		InputStream _in = IOUtils.getResourceAsStream(resource, XMLUtils.class, null);
		return getDocument(_in, true);
	}

	public static Document getDocument(InputStream in, boolean close) throws BaseException {
		// 从DOM工厂中获得DOM解析器
		DocumentBuilder _builder;
		try {
			_builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.W3C.PROCESS", e.getMessage());
			throw new BaseException(BasicCodeEnum.MSG_0018, _msg, e);
		}
		// 解析XML
		try {
			return _builder.parse(in);
		} catch (Exception e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.W3C.PROCESS", e.getMessage());
			throw new BaseException(BasicCodeEnum.MSG_0018, _msg, e);
		} finally {
			if (close) {
				IOUtils.close(in);
			}
		}
	}

	public static NodeList selectNodes(String expression, Object parent) throws BaseException {
		AssertUtils.assertNotNull("selectNodes", expression, parent);
		XPath _xpath = XPathFactory.newInstance().newXPath();
		try {
			Object _tmp = _xpath.evaluate(expression, parent, XPathConstants.NODESET);
			return ClassUtils.cast(_tmp, NodeList.class);
		} catch (XPathExpressionException e) {
			String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.W3C.PROCESS", e.getMessage());
			throw new BaseException(BasicCodeEnum.MSG_0018, _msg, e);
		}
	}

	public static Element withElement(Node node) throws BaseException {
		AssertUtils.assertNotNull("withElement", node);

		if (node instanceof Element) {
			return ClassUtils.cast(node, Element.class);
		}
		String _msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.W3C.UNKNOWN", node.getClass().getName());
		throw new BaseException(BasicCodeEnum.MSG_0005, _msg);
	}
}
