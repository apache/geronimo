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
package org.apache.geronimo.jetty.deployment;

import javax.xml.namespace.QName;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.geronimo.xbeans.geronimo.web.GerWebAppDocument;
import org.apache.geronimo.xbeans.geronimo.web.GerWebAppType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * STRICTLY TEMPORARY!  Support a previous version of the Jetty deployment
 * plan syntax by converting it to the new unified web deployment plan
 * syntax.  This should be removed as soon as possible.
 *
 * @version $Rev: 159325 $ $Date: 2005-03-28 17:53:03 -0500 (Mon, 28 Mar 2005) $
 */
public class TemporaryPlanAdapter {
    private final static Log log = LogFactory.getLog(TemporaryPlanAdapter.class);
    private final static String CORRECT_NAMESPACE = "http://geronimo.apache.org/xml/ns/web";
    private final static String WRONG_NAMESPACE = "http://geronimo.apache.org/xml/ns/web/jetty";

    /**
     * Convert a Jetty document to a web document.
     */
    public static GerWebAppDocument convertJettyDocumentToWeb(XmlObject source) {
        XmlCursor cursor = source.newCursor();
        while(!cursor.isStart()) {
            cursor.toNextToken();
        }

        if(WRONG_NAMESPACE.equals(cursor.getName().getNamespaceURI())) {
            log.error("WAR includes a file using the old geronimo-jetty.xml format "+
                "(including namespace http://geronimo.apache.org/xml/ns/web/jetty). "+
                "While we're still using your file for now, the next release will not, "+
                "and you should change to the new geronimo-web.xml format immediately. "+
                "The main difference is that it uses the namespace http://geronimo.apache.org/xml/ns/web");
            swapNamespace(cursor, CORRECT_NAMESPACE, WRONG_NAMESPACE);
        }

        XmlObject result = source.changeType(GerWebAppDocument.type);
        if (result != null) {
            return (GerWebAppDocument) result;
        }
        return (GerWebAppDocument) source;
    }

    /**
     * Convert a (presumably nested) Jetty element to a web element.
     */
    public static GerWebAppType convertJettyElementToWeb(XmlObject source) {
        XmlCursor cursor = source.newCursor();
        while(!cursor.isStart()) {
            cursor.toNextToken();
        }

        if(WRONG_NAMESPACE.equals(cursor.getName().getNamespaceURI())) {
            log.error("EAR includes WAR deployment content using the old geronimo-jetty.xml format "+
                "(including namespace http://geronimo.apache.org/xml/ns/web/jetty). "+
                "While we're still using your WAR deployment content for now, the next release will not, "+
                "and you should change to the new geronimo-web.xml format immediately. "+
                "The main difference is that it uses the namespace http://geronimo.apache.org/xml/ns/web");
            swapNamespace(cursor, CORRECT_NAMESPACE, WRONG_NAMESPACE);
        }

        XmlObject result = source.changeType(GerWebAppType.type);
        if (result != null) {
            return (GerWebAppType) result;
        }
        return (GerWebAppType) source;
    }

    /**
     * @return true if the schema was correct to begin with
     */
    public static boolean swapNamespace(XmlCursor cursor, String correct, String wrong) {
        while (cursor.hasNextToken()) {
            if (cursor.isStart()) {
                String current = cursor.getName().getNamespaceURI();
                if (correct.equals(current)) {
                    //already has correct schema, exit
                    return true;
                } else if(wrong.equals(current)) {
                    cursor.setName(new QName(correct, cursor.getName().getLocalPart()));
                }
            }
            cursor.toNextToken();
        }
        return false;
    }
}
