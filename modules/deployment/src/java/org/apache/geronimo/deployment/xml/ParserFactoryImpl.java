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

package org.apache.geronimo.deployment.xml;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GOperationInfo;
import org.apache.geronimo.gbean.GEndpointInfo;
import org.apache.geronimo.gbean.GConstructorInfo;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/01/19 06:40:07 $
 *
 * */
public class ParserFactoryImpl implements ParserFactory {

    private static final Log log = LogFactory.getLog(ParserFactoryImpl.class);

    private final static GBeanInfo GBEAN_INFO;

    private final DocumentBuilderFactory factory;
    private EntityResolver entityResolver;

    public ParserFactoryImpl(EntityResolver entityResolver) {
        this.entityResolver = entityResolver;
        factory = DocumentBuilderFactory.newInstance();
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
            public void error(SAXParseException exception)
                    throws SAXException {
                log.warn("SAX parse error (ignored)", exception);
                //throw exception;
            }

            public void fatalError(SAXParseException exception)
                    throws SAXException {
                log.warn("Fatal SAX parse error (ignored)", exception);
                //throw exception;
            }

            public void warning(SAXParseException exception)
                    throws SAXException {
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

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory("Factory for constructing suitable configured xml parsers", ParserFactoryImpl.class.getName());
        infoFactory.addOperation(new GOperationInfo("getParser"));
        infoFactory.addEndpoint(new GEndpointInfo("EntityResolver", EntityResolver.class.getName()));
        infoFactory.setConstructor(new GConstructorInfo(
                new String[] {"EntityResolver"},
                new Class[] {EntityResolver.class}));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
