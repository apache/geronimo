/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.geronimo.deployment.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;

import org.apache.geronimo.deployment.DeploymentException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Helper class that handles locating DDs in directories (mainly WEB-INF, META-INF),
 * building class space and dealing with deployments
 *
 * @version $Rev$ $Date$
 */
public class DeploymentHelper {
    protected final URL url;
    protected final URLType urlType;
    protected URL j2eeURL;
    protected URL geronimoURL;

    /**
     * Create an helper related to the specified deployment URL with META-INF
     * as the directory with the given files
     *
     * @see #DeploymentHelper(URLInfo, String, String, String, String)
     */
    public DeploymentHelper(URLInfo urlInfo, String j2eeDDName,
            String geronimoDDName) throws DeploymentException {
        this(urlInfo, j2eeDDName, geronimoDDName, "META-INF");
    }

    /**
     * Creates an helper related to the specified deployment URL.
     *
     * @param urlInfo Deployment URLInfo.
     * @param j2eeDDName name of the J2EE deployment descriptor file
     * @param geronimoDDName name of the Geronimo deployment descriptor file
     * @param infDir the directory where deployment descriptors are to be looked up
     * @throws DeploymentException when the deployment doesn't exist
     */
    public DeploymentHelper(URLInfo urlInfo, String j2eeDDName,
            String geronimoDDName, String infDir) throws DeploymentException {
        this.url = urlInfo.getUrl();
        this.urlType = urlInfo.getType();
        try {
            if (URLType.RESOURCE == urlType) {
                j2eeURL = null;
                geronimoURL = url;
            } else if (URLType.PACKED_ARCHIVE == urlType) {
                j2eeURL = new URL("jar:" + this.url.toExternalForm() + "!/" + infDir + "/" + j2eeDDName);
                geronimoURL = new URL("jar:" + this.url.toExternalForm() + "!/" + infDir + "/" + geronimoDDName);
            } else if (URLType.UNPACKED_ARCHIVE == urlType) {
                j2eeURL = new URL(this.url, infDir + "/" + j2eeDDName);
                geronimoURL = new URL(this.url, infDir + "/" + geronimoDDName);
            } else {
                j2eeURL = null;
                geronimoURL = null;
                return;
            }
        } catch (MalformedURLException e1) {
            throw new DeploymentException("Should never occur", e1);
        }
    }
    /**
     * Locates J2EE deployment descriptor.
     *
     * @return URL referencing the J2EE DD or null if no deployment descriptor
     *         is found.
     */
    public URL locateJ2EEDD() {
        return j2eeURL;
    }

    public Document getJ2EEDoc(DocumentBuilder parser) {
        return getDoc(parser, j2eeURL);
    }

    public Document getGeronimoDoc(DocumentBuilder parser) {
        return getDoc(parser, geronimoURL);
    }

    private Document getDoc(DocumentBuilder parser, URL url) {
        if (url == null) {
            return null;
        }
        try {
            return parser.parse(url.openStream());
        } catch (SAXException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Locates Geronimo's deployment descriptor
     *
     * @return URL referencing Geronimo's DD or null if no deployment
     *         descriptor is found
     */
    public URL locateGeronimoDD() {
        return geronimoURL;
    }
}
