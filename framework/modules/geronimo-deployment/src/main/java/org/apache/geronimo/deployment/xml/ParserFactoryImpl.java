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

package org.apache.geronimo.deployment.xml;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.util.XmlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

/**
 * @version $Rev$ $Date$
 */
public class ParserFactoryImpl implements ParserFactory {
    private static final Logger log = LoggerFactory.getLogger(ParserFactoryImpl.class);

    private final DocumentBuilderFactory factory;
    private EntityResolver entityResolver;

    public ParserFactoryImpl(EntityResolver entityResolver) {
        this.entityResolver = entityResolver;
        factory = XmlUtil.newDocumentBuilderFactory();
        //sets "http://xml.org/sax/features/namespaces"
        factory.setNamespaceAware(true);
        //sets "http://xml.org/sax/features/validation"
        factory.setValidating(true);
        factory.setAttribute(
                "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                "http://www.w3.org/2001/XMLSchema");
        factory.setAttribute("http://apache.org/xml/features/validation/schema",
                Boolean.TRUE);
    }

    public DocumentBuilder getParser()
            throws ParserConfigurationException {
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setEntityResolver(entityResolver);
        builder.setErrorHandler(new ErrorHandler() {
            public void error(SAXParseException exception) {
                log.warn("SAX parse error (ignored)", exception);
                //throw exception;
            }

            public void fatalError(SAXParseException exception) {
                log.warn("Fatal SAX parse error (ignored)", exception);
                //throw exception;
            }

            public void warning(SAXParseException exception) {
                log.warn("SAX parse warning", exception);
            }
        });
        return builder;
    }

    public EntityResolver getEntityResolver() {
        return entityResolver;
    }

    public void setEntityResolver(EntityResolver entityResolver) {
        this.entityResolver = entityResolver;
    }

    public final static GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic("Factory for constructing suitable configured xml parsers", ParserFactoryImpl.class);

//        infoFactory.addOperation("getParser");

        infoFactory.addReference("EntityResolver", EntityResolver.class, "GBean");

        infoFactory.setConstructor(new String[]{"EntityResolver"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
