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
package org.apache.geronimo.jmx;

import java.io.InputStream;
import java.net.URI;

import org.apache.commons.digester.Digester;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.xerces.parsers.SAXParser;


/**
 * Loads the GeronimoMBeanInfo from xml.
 *
 * @version $Revision: 1.1 $ $Date: 2003/09/05 02:38:32 $
 */
public class GeronimoMBeanInfoXMLLoader {
    private Digester digester;

    public GeronimoMBeanInfoXMLLoader() throws DeploymentException {
        SAXParser parser = new SAXParser();
        try {
            parser.setFeature("http://xml.org/sax/features/validation", true);
            parser.setFeature("http://apache.org/xml/features/validation/schema", true);
            parser.setFeature("http://apache.org/xml/features/validation/dynamic", true);
        } catch (Exception e) {
            throw new DeploymentException(e);
        }

        digester = new Digester(parser);

        // MBean
        digester.addObjectCreate("mbean", GeronimoMBeanInfo.class);
        digester.addCallMethod("mbean/target", "setTargetClass", 2);
        digester.addCallParam("mbean/target", 0, "name");
        digester.addCallParam("mbean/target", 1, "class");

        // Attribute
        digester.addObjectCreate("mbean/attribute", GeronimoAttributeInfo.class);
        digester.addSetProperties("mbean/attribute", "cache", "cachePolicy");
        digester.addSetNext("mbean/attribute", "addAttributeInfo");

        // Operation
        digester.addObjectCreate("mbean/operation", GeronimoOperationInfo.class);
        digester.addSetProperties("mbean/operation", "cache", "cachePolicy");
        digester.addObjectCreate("mbean/operation/parameter", GeronimoParameterInfo.class);
        digester.addSetProperties("mbean/operation/parameter");
        digester.addSetNext("mbean/operation/parameter", "addParameterInfo");
        digester.addSetNext("mbean/operation", "addOperationInfo");

        // Notification
        digester.addObjectCreate("mbean/notification", GeronimoNotificationInfo.class);
        digester.addSetProperties("mbean/notification", "class", "name");
        digester.addCallMethod("mbean/notification/type", "addNotificationType", 1);
        digester.addCallParam("mbean/notification/type", 0);
        digester.addSetNext("mbean/notification", "addNotificationInfo");
    }

    public GeronimoMBeanInfo loadXML(InputStream in) throws DeploymentException {
        try {
            return (GeronimoMBeanInfo) digester.parse(in);
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
    }

    public GeronimoMBeanInfo loadXML(URI uri) throws DeploymentException {
        try {
            return (GeronimoMBeanInfo) digester.parse(uri.toURL().openStream());
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
    }
}
