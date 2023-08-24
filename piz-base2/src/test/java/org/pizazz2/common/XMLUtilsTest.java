package org.pizazz2.common;

import org.junit.Assert;
import org.junit.Test;
import org.pizazz2.exception.BaseException;
import org.pizazz2.exception.UtilityException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.List;

/**
 * XMLUtils测试
 *
 * @author xlgp2171
 * @version 2.0.210201
 */
public class XMLUtilsTest {

    @Test
    public void testGetDocumentAndGetRootElementAndGetChildElements() throws BaseException {
        Document doc = XMLUtils.getDocument("message.xml");
        Element root = XMLUtils.getRootElement(doc);
        String result = StringUtils.EMPTY;

        if (root != null) {
            NodeList nodes = root.getChildNodes();
            List<Element> elements = XMLUtils.getChildElements(nodes);

            for (Element item : elements) {
                if (item.getAttribute("name").equals("param1")) {
                    result = item.getTextContent();
                }
            }
        }
        Assert.assertEquals(result, "true");
    }

    @Test
    public void testSelectNodesAndWithElement() throws UtilityException {
        Document doc = XMLUtils.getDocument("message.xml");
        NodeList nodes = XMLUtils.selectNodes("//message[@name='param3']", doc);
        Element element = XMLUtils.withElement(nodes.item(0));
        Assert.assertEquals(element.getTextContent(), "23.4");
    }

    @Test
    public void testSampleXml() throws UtilityException {
        Document doc = XMLUtils.getDocument("sample.xml");
        Element root = XMLUtils.getRootElement(doc);

        if (root != null) {
            NodeList recordNode = XMLUtils.selectNodes("//record", doc);
            System.out.println(recordNode.getLength());
            List<Element> recordElements = XMLUtils.getChildElements(recordNode);

            for (Element record : recordElements) {
                NodeList fieldNode = record.getChildNodes();
                List<Element> fieldElements = XMLUtils.getChildElements(fieldNode);

                for (Element field : fieldElements) {
                    String name = field.getAttribute("name");

                    if (name.equals("srcip") || name.equals("dstip")) {
                        System.out.println(field.getAttribute("value"));
                    }
                }
            }
        }
    }
}
