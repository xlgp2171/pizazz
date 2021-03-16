package org.pizazz2.common;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.pizazz2.exception.ValidateException;
import org.pizazz2.exception.UtilityException;
import org.pizazz2.helper.LocaleHelper;
import org.pizazz2.message.BasicCodeEnum;
import org.pizazz2.message.TypeEnum;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * XML操作工具
 * 
 * @author xlgp2171
 * @version 2.0.210201
 */
public class XMLUtils {

	public static Document getDocument(String resource) throws ValidateException, UtilityException {
		InputStream in = IOUtils.getResourceAsStream(resource, XMLUtils.class, null);
		return XMLUtils.getDocument(in, true);
	}

	public static Document getDocument(InputStream in, boolean close) throws ValidateException, UtilityException {
		ValidateUtils.notNull("getDocument", in);
		// 从DOM工厂中获得DOM解析器
		DocumentBuilder builder;
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.W3C.PROCESS", e.getMessage());
			throw new UtilityException(BasicCodeEnum.MSG_0018, msg, e);
		}
		// 解析XML
		try {
			return builder.parse(in);
		} catch (Exception e) {
			String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.W3C.PROCESS", e.getMessage());
			throw new UtilityException(BasicCodeEnum.MSG_0018, msg, e);
		} finally {
			if (close) {
				SystemUtils.close(in);
			}
		}
	}

	public static NodeList selectNodes(String expression, Object parent) throws ValidateException, UtilityException {
		ValidateUtils.notNull("selectNodes", expression, parent);
		XPath xpath = XPathFactory.newInstance().newXPath();
		try {
			Object tmp = xpath.evaluate(expression, parent, XPathConstants.NODESET);
			return ClassUtils.cast(tmp, NodeList.class);
		} catch (XPathExpressionException e) {
			String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.W3C.PROCESS", e.getMessage());
			throw new UtilityException(BasicCodeEnum.MSG_0018, msg, e);
		}
	}

	public static boolean isElement(Node node) throws ValidateException {
		ValidateUtils.notNull("isElement", node);
		return node.getNodeType() == Node.ELEMENT_NODE;
	}

	public static Element getRootElement(Document doc) {
		ValidateUtils.notNull("getRootElement", doc);
		Node node = doc.getFirstChild();
		List<Element> elements = XMLUtils.getNextElements(node);
		return elements.isEmpty() ? null : elements.get(0);
	}

	public static List<Element> getNextElements(Node node) {
		ValidateUtils.notNull("getChildElements", node);
		List<Element> tmp = new LinkedList<>();

		while(node != null) {
			if(XMLUtils.isElement(node)) {
				tmp.add(XMLUtils.withElement(node));
			}
			node = node.getNextSibling();
		}
		return tmp;
	}

	public static List<Element> getChildElements(NodeList nodes) {
		ValidateUtils.notNull("getChildElements", nodes);
		List<Element> tmp = new LinkedList<>();

		for (int i = 0; i < nodes.getLength(); i ++) {
			Node node = nodes.item(i);

			if (XMLUtils.isElement(node)) {
				tmp.add(XMLUtils.withElement(node));
			}
		}
		return tmp;
	}

	public static Element withElement(Node node) throws ValidateException {
		ValidateUtils.notNull("withElement", node);

		if (node instanceof Element) {
			return ClassUtils.cast(node, Element.class);
		}
		String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.W3C.UNKNOWN", node.getClass().getName());
		throw new ValidateException(BasicCodeEnum.MSG_0005, msg);
	}
}
