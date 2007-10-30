/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.kernel.util;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerFactory;

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class XmlUtilTest extends TestCase {
    private String documentBuilderClassName;
    private String saxParserClassName;
    private String transformerClassName;

    public void testDocumentBuilderDefault() {
        System.getProperties().remove(XmlUtil.DOCUMENT_BUILDER_FACTORY);
        assertNull(System.getProperty(XmlUtil.DOCUMENT_BUILDER_FACTORY));

        DocumentBuilderFactory documentBuilderFactory = XmlUtil.newDocumentBuilderFactory();
        String className = documentBuilderFactory.getClass().getName();
        assertEquals(documentBuilderClassName, className);
    }

    public void testDocumentBuilderOverride() {
        System.setProperty(XmlUtil.DOCUMENT_BUILDER_FACTORY, documentBuilderClassName);
        assertEquals(documentBuilderClassName, System.getProperty(XmlUtil.DOCUMENT_BUILDER_FACTORY));

        DocumentBuilderFactory documentBuilderFactory = XmlUtil.newDocumentBuilderFactory();
        String className = documentBuilderFactory.getClass().getName();
        assertEquals(documentBuilderClassName, className);
    }

    public void testSaxparserDefault() {
        System.getProperties().remove(XmlUtil.SAX_PARSER_FACTORY);
        assertNull(System.getProperty(XmlUtil.SAX_PARSER_FACTORY));

        SAXParserFactory saxParserFactory = XmlUtil.newSAXParserFactory();
        String className = saxParserFactory.getClass().getName();
        assertEquals(saxParserClassName, className);
    }

    public void testSAXParserOverride() {
        System.setProperty(XmlUtil.SAX_PARSER_FACTORY, saxParserClassName);
        assertEquals(saxParserClassName, System.getProperty(XmlUtil.SAX_PARSER_FACTORY));

        SAXParserFactory saxParserFactory = XmlUtil.newSAXParserFactory();
        String className = saxParserFactory.getClass().getName();
        assertEquals(saxParserClassName, className);
    }

    public void testTransformerDefault() {
        System.getProperties().remove(XmlUtil.TRANSFORMER_FACTORY);
        assertNull(System.getProperty(XmlUtil.TRANSFORMER_FACTORY));

        TransformerFactory transformerFactory = XmlUtil.newTransformerFactory();
        String className = transformerFactory.getClass().getName();
        assertEquals(transformerClassName, className);
    }

    public void testTransformerOverride() {
        System.setProperty(XmlUtil.TRANSFORMER_FACTORY, transformerClassName);
        assertEquals(transformerClassName, System.getProperty(XmlUtil.TRANSFORMER_FACTORY));

        TransformerFactory transformerFactory = XmlUtil.newTransformerFactory();
        String className = transformerFactory.getClass().getName();
        assertEquals(transformerClassName, className);
    }

    protected void setUp() throws Exception {
        super.setUp();
        documentBuilderClassName = DocumentBuilderFactory.newInstance().getClass().getName();
        saxParserClassName = SAXParserFactory.newInstance().getClass().getName();
        transformerClassName = TransformerFactory.newInstance().getClass().getName();
    }
}
