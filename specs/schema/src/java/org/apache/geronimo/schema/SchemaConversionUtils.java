/**
 *
 * Copyright 2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.schema;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.namespace.QName;

import org.apache.geronimo.xbeans.j2ee.ActivationConfigPropertyType;
import org.apache.geronimo.xbeans.j2ee.ActivationConfigType;
import org.apache.geronimo.xbeans.j2ee.EjbJarDocument;
import org.apache.geronimo.xbeans.j2ee.WebAppDocument;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/06/17 06:55:11 $
 *
 * */
public class SchemaConversionUtils {
    private static final String J2EE_NAMESPACE = "http://java.sun.com/xml/ns/j2ee";

    private SchemaConversionUtils() {
    }

    public static XmlObject parse(InputStream is) throws IOException, XmlException {
        ArrayList errors = new ArrayList();
        XmlOptions options = new XmlOptions();
        options.setLoadLineNumbers();
        options.setErrorListener(errors);
        XmlObject parsed = XmlObject.Factory.parse(is, options);
        if (errors.size() != 0) {
            throw new XmlException(errors.toArray().toString());
        }
        return parsed;
    }

    public static EjbJarDocument convertToEJBSchema(XmlObject xmlObject) {
        XmlCursor cursor = xmlObject.newCursor();
        String schemaLocationURL = "http://java.sun.com/xml/ns/j2ee/ejb-jar_2_1.xsd";
        String version = "2.1";
        try {
            SchemaConversionUtils.convertToSchema(cursor, schemaLocationURL, version);
            //play with message-driven
            cursor.toStartDoc();
            SchemaConversionUtils.convertToActivationConfig(cursor);
        } finally {
            cursor.dispose();
        }
        XmlObject result = xmlObject.changeType(EjbJarDocument.type);
        if (result != null) {
            return (EjbJarDocument)result;
        }
        return (EjbJarDocument)xmlObject;
    }

    public static WebAppDocument convertToServletSchema(XmlObject xmlObject) {
        XmlCursor cursor = xmlObject.newCursor();
        String schemaLocationURL = "http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd";
        String version = "2.4";
        try {
            SchemaConversionUtils.convertToSchema(cursor, schemaLocationURL, version);
        } finally {
            cursor.dispose();
        }
        XmlObject result = xmlObject.changeType(WebAppDocument.type);
        if (result != null) {
            return (WebAppDocument)result;
        }
        return (WebAppDocument)xmlObject;
    }

    public static boolean convertToSchema(XmlCursor cursor, String schemaLocationURL, String version) {
        //convert namespace
        boolean isFirstStart = true;
        while (cursor.hasNextToken()) {
            if (cursor.isStart()) {
                if (J2EE_NAMESPACE.equals(cursor.getName().getNamespaceURI())) {
                    //already has correct schema, exit
                    return false;
                }
                cursor.setName(new QName(J2EE_NAMESPACE, cursor.getName().getLocalPart()));
                cursor.toNextToken();
                if (isFirstStart) {
                    cursor.insertNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
                    cursor.insertAttributeWithValue(new QName("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation", "xsi"), "http://java.sun.com/xml/ns/j2ee " + schemaLocationURL);
                    cursor.insertAttributeWithValue(new QName("version"), version);
                    isFirstStart = false;
                }
            } else {
                cursor.toNextToken();
            }
        }
        return true;
    }

    public static void convertToActivationConfig(XmlCursor cursor) {
        cursor.toChild(J2EE_NAMESPACE, "ejb-jar");
        cursor.toChild(J2EE_NAMESPACE, "enterprise-beans");
        boolean onMessageDriven = cursor.toChild(J2EE_NAMESPACE, "message-driven");
        while (onMessageDriven) {
            cursor.toChild(J2EE_NAMESPACE, "transaction-type");
            //add messaging-type
            cursor.insertElementWithText("messaging-type", J2EE_NAMESPACE, "javax.jms.MessageListener");
            cursor.toNextSibling();
            //mark activation-config-properties location
            cursor.push();
            ActivationConfigType activationConfig = ActivationConfigType.Factory.newInstance();
            if (cursor.isStart() && cursor.getName().equals(new QName(J2EE_NAMESPACE, "message-selector"))) {
                addActivationConfigProperty(activationConfig, cursor, "messageSelector");
                cursor.removeXml();
            }
            toNextStartToken(cursor);
            if (cursor.isStart() && cursor.getName().equals(new QName(J2EE_NAMESPACE, "acknowledge-mode"))) {
                addActivationConfigProperty(activationConfig, cursor, "acknowledgeMode");
                cursor.removeXml();
            }
            toNextStartToken(cursor);
            if (cursor.isStart() && cursor.getName().equals(new QName(J2EE_NAMESPACE, "message-driven-destination"))) {
                cursor.push();
                if (cursor.toChild(J2EE_NAMESPACE, "destination-type")) {
                    addActivationConfigProperty(activationConfig, cursor, "destinationType");
                } else {
                    throw new IllegalStateException("no destination-type in message-driven-destination");
                }
                if (cursor.toNextSibling(J2EE_NAMESPACE, "subscription-durability")) {
                    addActivationConfigProperty(activationConfig, cursor, "subscriptionDurability");
                }
                cursor.pop();
                cursor.removeXml();
            }
            cursor.pop();
            cursor.insertElement(new QName(J2EE_NAMESPACE, "activation-config"));
            //back up into element we just inserted.
            while (!cursor.isEnd()) {
                cursor.toPrevToken();
            }
            XmlCursor activationConfigCursor = activationConfig.newCursor();
            //move past the STARTDOC token
            toNextStartToken(activationConfigCursor);
            //add all the activation-config-properties we defined
            while (true) {
                activationConfigCursor.copyXml(cursor);
                if (!activationConfigCursor.toNextSibling(J2EE_NAMESPACE, "activation-config-property")) {
                    break;
                }
            }
            //out of activation-config element
            cursor.toParent();
            //out of message-driven element
            cursor.toParent();
            //on to next message driven bean, if any.
            onMessageDriven = cursor.toNextSibling(J2EE_NAMESPACE, "message-driven");
        }
    }

    private static void toNextStartToken(XmlCursor cursor) {
        while (!cursor.isStart()) {
            cursor.toNextToken();
        }
    }

    private static void addActivationConfigProperty(ActivationConfigType activationConfig, XmlCursor cursor, String activationConfigPropertyName) {
        ActivationConfigPropertyType activationConfigProperty = activationConfig.addNewActivationConfigProperty();
        activationConfigProperty.addNewActivationConfigPropertyName().setStringValue(activationConfigPropertyName);
        activationConfigProperty.addNewActivationConfigPropertyValue().setStringValue(cursor.getTextValue());
    }

}
