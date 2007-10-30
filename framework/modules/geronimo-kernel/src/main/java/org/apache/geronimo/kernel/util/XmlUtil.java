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

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.geronimo.kernel.ClassLoading;

/**
 * @version $Rev$ $Date$
 */
public final class XmlUtil {
    public static final String DOCUMENT_BUILDER_FACTORY = "geronimo.xml.parsers.DocumentBuilderFactory";
    public static final String SAX_PARSER_FACTORY = "geronimo.xml.parsers.SAXParserFactory";
    public static final String TRANSFORMER_FACTORY = "geronimo.xml.transform.TransformerFactory";

    private XmlUtil() {
    }

    public static DocumentBuilderFactory newDocumentBuilderFactory() {
        return newDocumentBuilderFactory(getClassLoader());
    }

    public static DocumentBuilderFactory newDocumentBuilderFactory(ClassLoader classLoader) {
        String documentBuilderName = getSystemProperty(DOCUMENT_BUILDER_FACTORY);
        if (documentBuilderName != null && documentBuilderName.length() != 0) {
            try {
                Class documentBuilderClass = ClassLoading.loadClass(documentBuilderName, classLoader);
                DocumentBuilderFactory documentBuilderFactory = (DocumentBuilderFactory) documentBuilderClass.newInstance();
                return documentBuilderFactory;
            } catch (Exception e) {
                throw new FactoryConfigurationError(e, "Unable to create DocumentBuilderFactory " +
                        documentBuilderName + ", which was specified in the " + DOCUMENT_BUILDER_FACTORY + " system property");
            }
        }

        return DocumentBuilderFactory.newInstance();
    }

    public static SAXParserFactory newSAXParserFactory() {
        return newSAXParserFactory(getClassLoader());
    }

    public static SAXParserFactory newSAXParserFactory(ClassLoader classLoader) {
        String saxParserName = getSystemProperty(SAX_PARSER_FACTORY);
        if (saxParserName != null && saxParserName.length() != 0) {
            try {
                Class saxParserClass = ClassLoading.loadClass(saxParserName, classLoader);
                SAXParserFactory saxParserFactory = (SAXParserFactory) saxParserClass.newInstance();
                return saxParserFactory;
            } catch (Exception e) {
                throw new FactoryConfigurationError(e, "Unable to create SAXParserFactory " +
                        saxParserName + ", which was specified in the " + SAX_PARSER_FACTORY + " system property");
            }
        }

        return SAXParserFactory.newInstance();
    }

    public static TransformerFactory newTransformerFactory() {
        return newTransformerFactory(getClassLoader());
    }

    public static TransformerFactory newTransformerFactory(ClassLoader classLoader) {
        String transformerName = getSystemProperty(TRANSFORMER_FACTORY);
        if (transformerName != null && transformerName.length() != 0) {
            try {
                Class transformerClass = ClassLoading.loadClass(transformerName, classLoader);
                TransformerFactory transformerFactory = (TransformerFactory) transformerClass.newInstance();
                return transformerFactory;
            } catch (Exception e) {
                throw new TransformerFactoryConfigurationError(e, "Unable to create TransformerFactory " +
                        transformerName + ", which was specified in the " + TRANSFORMER_FACTORY + " system property");
            }
        }

        return TransformerFactory.newInstance();
    }

    private static ClassLoader getClassLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader != null) {
            return classLoader;
        } else {
            return XmlUtil.class.getClassLoader();
        }
    }

    private static String getSystemProperty(String key) {
        String value = System.getProperty(key);
        if (value != null) value = value.trim();
        return value;
    }
}
