/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.xml.deployment;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Holds utility methods for parsing a DOM tree.
 *
 * @version $Revision: 1.3 $ $Date: 2003/09/05 20:18:03 $
 */
public final class LoaderUtil {
    private static final Log log = LogFactory.getLog(LoaderUtil.class);

    public static String getContent(Element element) {
        LinkedList nodes = new LinkedList();
        nodes.add(element);
        StringBuffer buf = new StringBuffer(100);
        while (!nodes.isEmpty()) {
            Node node = (Node) nodes.removeFirst();
            switch (node.getNodeType()) {
            case Node.ELEMENT_NODE:
                for (Node child = node.getLastChild(); child != null; child = child.getPreviousSibling()) {
                    nodes.addFirst(child);
                }
                break;
            case Node.TEXT_NODE:
            case Node.CDATA_SECTION_NODE:
                buf.append(node.getNodeValue());
                break;
            }
        }
        String content = buf.toString().trim();
        if (content.length() == 0) {
            return null;
        } else {
            return content;
        }
    }

    public static String getAttribute(Element element, String attribute) {
        if (element.hasAttribute(attribute)) {
            return element.getAttribute(attribute);
        } else {
            return null;
        }
    }

    public static Element getChild(Element element, String child) {
        for (Node node = element.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node instanceof Element == false) {
                continue;
            }
            Element e = (Element) node;
            if (child.equals(e.getLocalName())) {
                return e;
            }
        }
        return null;
    }

    public static Element[] getChildren(Element root, String childName) {
        NodeList nodes = root.getElementsByTagName(childName);
        if(nodes == null) {
            return new Element[0];
        }
        int length = nodes.getLength();
        Element[] result = new Element[length];
        for (int i = 0; i < length; i++) {
            result[i] = (Element) nodes.item(i);
        }
        return result;
    }

    public static String getChildContent(Element element, String child) {
        Element e = getChild(element, child);
        if (e != null) {
            return getContent(e);
        } else {
            return null;
        }
    }

    public static String[] getChildrenContent(Element element, String child) {
        NodeList nodes = element.getElementsByTagName(child);
        String[] result = new String[nodes.getLength()];
        for (int i=0; i < nodes.getLength(); i++) {
            Element e = (Element) nodes.item(i);
            result[i] = getContent(e);
        }
        return result;
    }

    /**
     * Utility method to parse the contents of a Reader into a DOM Document.
     * NOTE: closes the reader when finished!
     *
     * @param reader  The reader with the XML content
     * @param context A file name or similar context, included when any warnings
     *                or errors are logged to help the user identify the problem
     */
    public static Document parseXML(Reader reader) throws SAXException, IOException {
        DocumentBuilderFactory factory = new org.apache.xerces.jaxp.DocumentBuilderFactoryImpl();
        factory.setNamespaceAware(true);
        factory.setValidating(true);
        factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver(new LocalEntityResolver(new File("src/schema"))); // @todo get this from resource
            return builder.parse(new InputSource(new BufferedReader(reader)));
        } catch (ParserConfigurationException e) {
            throw new AssertionError("Unable to obtain suitable DocumentBuilder");
        }
    }
}
