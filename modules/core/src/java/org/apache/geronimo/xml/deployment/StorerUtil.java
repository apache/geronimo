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

import java.io.Writer;
import java.util.Properties;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;

/**
 * Holds utility methods for writing to a DOM tree
 *
 * @version $Revision: 1.2 $ $Date: 2004/01/02 23:32:38 $
 */
public class StorerUtil {
    private static EntityResolver entityResolver;

    /**
     * Creates a new child of the specified element, adds it as a child, and
     * returns it.
     *
     * @param parent The parent element for the new child
     * @param name The name of the new child element
     *
     * @return The created and added child element
     */
    public static Element createChild(Element parent, String name) {
        Element elem = parent.getOwnerDocument().createElement(name);
        parent.appendChild(elem);
        return elem;
    }

    /**
     * Creates a new child of the specified element, adds it as a child, and
     * sets the specified value to be its text content.
     *
     * @param parent  The parent element for the new child
     * @param name  The name of the new child element
     * @param value The text to set on the new element
     */
    public static void createChildText(Element parent, String name, String value) {
        Element child = parent.getOwnerDocument().createElement(name);
        parent.appendChild(child);
        setText(child, value);
    }

    /**
     * Creates a new child of the specified element if the specified value is
     * not empty, adds it as a child, and sets the value to be its text
     * content.  If the value is null or an empty String, nothing is done.
     *
     * @param parent  The parent element for the new child
     * @param name  The name of the new child element
     * @param value The text to set on the new element
     */
    public static void createOptionalChildText(Element parent, String name, String value) {
        if (value == null || value.equals(""))
            return;
        Element child = parent.getOwnerDocument().createElement(name);
        parent.appendChild(child);
        setText(child, value);
    }

    /**
     * Utility method to store the contents of a DOM Document to a stream.
     *
     * @param doc  The document to store
     * @param out  The stream to write to
     *
     * @throws javax.xml.transform.TransformerException Occurs when the writing did not complete
     *                              successfully
     */
    public static void writeXML(Document doc, Writer out) throws TransformerException {
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();
        Properties props = new Properties();
        props.put(OutputKeys.INDENT, "yes");
        props.put(OutputKeys.METHOD, "xml");
        props.put(OutputKeys.OMIT_XML_DECLARATION, "no");
        props.put(OutputKeys.VERSION, "1.0");
        transformer.setOutputProperties(props);
        transformer.transform(new DOMSource(doc), new StreamResult(out));
    }

    /**
     * Utility method to create an empty Document to work with.
     */
    public static Document createDocument() throws ParserConfigurationException {
        DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
        fac.setValidating(true);
        fac.setNamespaceAware(true);
        fac.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
        DocumentBuilder builder = fac.newDocumentBuilder();
        builder.setEntityResolver(entityResolver);
        return builder.newDocument();
    }

    /**
     * Sets the text for an element, removing any text that used to be there.
     */
    public static void setText(Element e, String s) {
        boolean cdata = s != null && (s.indexOf('>') > -1 ||
                s.indexOf('<') > -1 ||
                s.indexOf('&') > -1);
        NodeList nl = e.getChildNodes();
        boolean found = false;
        int max = nl.getLength();
        if (cdata) {
            for (int i = 0; i < max; i++) {
                Node n = nl.item(i);
                if (n.getNodeType() == Node.TEXT_NODE) {
                    e.removeChild(n);
                } else if (n.getNodeType() == Node.CDATA_SECTION_NODE) {
                    if (!found) {
                        n.setNodeValue(s);
                        found = true;
                    } else {
                        e.removeChild(n);
                    }
                }
            }
            if (!found) {
                e.appendChild(e.getOwnerDocument().createCDATASection(s));
            }
        } else {
            for (int i = 0; i < max; i++) {
                Node n = nl.item(i);
                if (n.getNodeType() == Node.TEXT_NODE) {
                    if (!found) {
                        n.setNodeValue(s);
                        found = true;
                    } else {
                        e.removeChild(n);
                    }
                } else if (n.getNodeType() == Node.CDATA_SECTION_NODE) {
                    e.removeChild(n);
                }
            }
            if (!found) {
                e.appendChild(e.getOwnerDocument().createTextNode(s == null ? "" : s));
            }
        }
    }

    /**
     * Creates a child element in the specified namespace with the specified
     * value as its text contents.
     *
     * @param elem          The parent element for the new child
     * @param qualifiedName The namespace-qualified element name (i.e. <tt>foo:someElement</tt>)
     * @param namespace     The namespace URI that the prefix on the qualified name should
     *                      map to (i.e. <tt>http://somewhere</tt> with the qualifiedName
     *                      <tt>foo:someElement</tt> means <tt>foo</tt> = <tt>http://somewhere</tt>)
     * @param value         The text value to use as the content of the new element
     */
    public static void createChildTextWithNS(Element elem, String qualifiedName, String namespace, String value) {
        Element child = elem.getOwnerDocument().createElementNS(namespace, qualifiedName);
        elem.appendChild(child);
        setText(child, value);
    }

    /**
     * If the value is not empty/null, creates a child element in the specified
     * namespace with the specified value as its text contents.
     *
     * @param elem          The parent element for the new child
     * @param qualifiedName The namespace-qualified element name (i.e. <tt>foo:someElement</tt>)
     * @param namespace     The namespace URI that the prefix on the qualified name should
     *                      map to (i.e. <tt>http://somewhere</tt> with the qualifiedName
     *                      <tt>foo:someElement</tt> means <tt>foo</tt> = <tt>http://somewhere</tt>)
     * @param value         The text value to use as the content of the new element
     */
    public static void createOptionalChildTextWithNS(Element elem, String qualifiedName, String namespace, String value) {
        if (value == null || value.equals(""))
            return;
        Element child = elem.getOwnerDocument().createElementNS(namespace, qualifiedName);
        elem.appendChild(child);
        setText(child, value);
    }

    public static void setEntityResolver(LocalEntityResolver entityResolver) {
        StorerUtil.entityResolver = entityResolver;
    }
}
