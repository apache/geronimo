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

package org.apache.geronimo.kernel.deployment;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.List;
import java.io.IOException;
import java.io.InputStream;

import javax.management.ObjectName;

import org.apache.geronimo.kernel.deployment.service.ClassSpaceMetadata;
import org.apache.geronimo.kernel.deployment.scanner.URLType;
import org.apache.geronimo.kernel.service.GeronimoMBeanInfoXMLLoader;
import org.apache.geronimo.kernel.jmx.JMXUtil;

/**
 *
 *
 * @version $Revision: 1.3 $ $Date: 2003/11/17 00:16:43 $
 *
 * */
public class DeploymentHelper {
    protected final URL url;
    protected final URLType urlType;
    protected URL j2eeURL;
    protected URL geronimoURL;
    private final String objectNameTypeName;

    /**
     * Creates an helper related to the specified deployment URL.
     *
     * @param url Deployment URL.
     * @param urlType Type of the URL.
     */
    public DeploymentHelper(URL url, URLType urlType, String objectNameTypeName, String j2eeDDName, String geronimoDDName) throws DeploymentException {
        this(url, urlType, objectNameTypeName, j2eeDDName, geronimoDDName, "META-INF");
    }

    public DeploymentHelper(URL url, URLType urlType, String objectNameTypeName, String j2eeDDName, String geronimoDDName, String infDir) throws DeploymentException {
        this.url = url;
        this.urlType = urlType;
        this.objectNameTypeName = objectNameTypeName;
        try {
            if (URLType.PACKED_ARCHIVE == urlType) {
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
        InputStream is = null;
        try {
            is = j2eeURL.openStream();
        } catch (IOException e) {
            //not there, not for us.
            j2eeURL = null;
            geronimoURL = null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) { //ignore
                }
            }
        }

    }

    /**
     * Locates the URL referencing the ra.xml deployment descriptor.
     *
     * @return URL referening ra.xml or null if no deployment descriptor is
     * found.
     *
     * @throws org.apache.geronimo.kernel.deployment.DeploymentException
     */
    public URL locateJ2eeDD() throws DeploymentException {
        return j2eeURL;
    }

    public URL locateGeronimoDD() throws DeploymentException {
        return geronimoURL;
    }

    /**
     * Build a ClassSpaceMetadata abstracting the connector archives.
     * <P>
     * The connector archives are all the .jar, .so or .dll files contained
     * by the deployment unit.
     *
     * @return ClassSpaceMetadata referencing the connector archives.
     *
     * @throws org.apache.geronimo.kernel.deployment.DeploymentException
     */
    public ClassSpaceMetadata buildClassSpace() throws DeploymentException {
        ClassSpaceMetadata classSpaceMetaData = new ClassSpaceMetadata();
        classSpaceMetaData.setName(buildClassSpaceName());
        classSpaceMetaData.setGeronimoMBeanInfo(GeronimoMBeanInfoXMLLoader.loadMBean(
                ClassLoader.getSystemResource("org/apache/geronimo/kernel/classspace/classspace-mbean.xml")
        ));
        classSpaceMetaData.setParent(JMXUtil.getObjectName("geronimo.system:role=ClassSpace,name=System"));

        List archives = classSpaceMetaData.getUrls();

        if (URLType.PACKED_ARCHIVE == urlType) {
            findPackedArchives(archives);
        } else if (URLType.UNPACKED_ARCHIVE == urlType) {
            findUnpackedArchives(archives);
        } else {
            throw new DeploymentException("Should never occurs");
        }
        return classSpaceMetaData;
    }

    /**
     * Intended to be overridden for all but the simplest packages
     * @param archives
     * @throws DeploymentException
     */
    protected void findUnpackedArchives(List archives) throws DeploymentException {
        archives.add(url);
    }

    /**
     * Intended to be overridden for all but the simplest packages
     * @param archives
     * @throws DeploymentException
     */
    protected void findPackedArchives(List archives) throws DeploymentException {
        archives.add(url);
    }

    /**
     * Build a connector deployment name.
     *
     * @return Connector deployment name.
     */
    public ObjectName buildDeploymentName() {
        return JMXUtil.getObjectName(
                "geronimo.deployment:role=DeploymentUnit,url="
                + ObjectName.quote(url.toString())
                + ",type=" + objectNameTypeName);
    }

    /**
     * Build the name of the ClassSpace which is used as the ClassLoader of the
     * URL to be deployed.
     *
     * @return ClassSpace name.
     */
    public ObjectName buildClassSpaceName() {
        return JMXUtil.getObjectName(
                "geronimo.deployment:role=DeploymentUnitClassSpace,url="
                + ObjectName.quote(url.toString())
                + ",type=" + objectNameTypeName);
    }


}
