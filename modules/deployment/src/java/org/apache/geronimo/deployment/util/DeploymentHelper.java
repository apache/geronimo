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

package org.apache.geronimo.deployment.util;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.List;
import java.io.IOException;
import java.io.InputStream;

import javax.management.ObjectName;
import javax.xml.parsers.DocumentBuilder;

import org.apache.geronimo.kernel.deployment.service.ClassSpaceMetadata;
import org.apache.geronimo.kernel.deployment.scanner.URLType;
import org.apache.geronimo.kernel.deployment.scanner.URLInfo;
import org.apache.geronimo.kernel.deployment.DeploymentException;
import org.apache.geronimo.kernel.service.GeronimoMBeanInfoXMLLoader;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Helper class that handles locating files in META-INF directory, building
 * class space and dealing with deployments
 *
 * @version $Revision: 1.2 $ $Date: 2004/01/17 01:32:38 $
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