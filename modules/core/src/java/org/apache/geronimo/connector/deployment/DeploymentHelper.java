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

package org.apache.geronimo.connector.deployment;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.management.MBeanException;
import javax.management.ObjectName;
import javax.management.RuntimeOperationsException;

import org.apache.geronimo.common.Classes;
import org.apache.geronimo.deployment.model.connector.ConfigProperty;
import org.apache.geronimo.deployment.model.geronimo.connector.GeronimoConnectionDefinition;
import org.apache.geronimo.deployment.model.geronimo.connector.GeronimoResourceAdapter;
import org.apache.geronimo.kernel.deployment.DeploymentException;
import org.apache.geronimo.kernel.deployment.scanner.URLType;
import org.apache.geronimo.kernel.deployment.service.ClassSpaceMetadata;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.service.GeronimoAttributeInfo;
import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;
import org.apache.geronimo.kernel.service.GeronimoMBeanInfoXMLLoader;

/**
 * Connector deployment helper. It allows to compute various information of
 * a URL to be deployed.
 *
 * @version $Revision: 1.1 $ $Date: 2003/11/11 21:11:56 $
 */
public final class DeploymentHelper
{


    private final URL url;
    private final URLType type;
    private final URL raURL;
    private final URL graURL;

    /**
     * Creates an helper related to the specified deployment URL.
     *
     * @param aURL Deployment URL.
     * @param aType Type of the URL.
     * @todo use ConnectorDocument and GeronimoConnectorDocument to get "ra.xml" etc
     */
    public DeploymentHelper(URL aURL, URLType aType) throws DeploymentException {
        url = aURL;
        type = aType;
		try {
			if ( URLType.PACKED_ARCHIVE == type ) {
				if (!url.getPath().endsWith(".rar")) {
				    raURL = null;
				    graURL = null;
					return;
				}
				raURL = new URL("jar:"+url.toExternalForm()+"!/META-INF/ra.xml");
				graURL = new URL("jar:"+url.toExternalForm()+"!/META-INF/geronimo-ra.xml");
			} else if ( URLType.UNPACKED_ARCHIVE == type ) {
				raURL = new URL(url, "META-INF/ra.xml");
				graURL = new URL(url, "META-INF/geronimo-ra.xml");
			} else {
				raURL = null;
				graURL = null;
				return;
			}
		} catch (MalformedURLException e1) {
			throw new DeploymentException("Should never occurs", e1);
		}
    }

    /**
     * Locates the URL referencing the ra.xml deployment descriptor.
     *
     * @return URL referening ra.xml or null if no deployment descriptor is
     * found.
     *
     * @throws DeploymentException
     */
	public URL locateDD() throws DeploymentException {
		return raURL;
	}

	public URL locateGeronimoDD() throws DeploymentException {
		return graURL;
	}

    /**
     * Build a ClassSpaceMetadata abstracting the connector archives.
     * <P>
     * The connector archives are all the .jar, .so or .dll files contained
     * by the deployment unit.
     *
     * @return ClassSpaceMetadata referencing the connector archives.
     *
     * @throws DeploymentException
     */
    public ClassSpaceMetadata buildClassSpace() throws DeploymentException {
        ClassSpaceMetadata raCS = new ClassSpaceMetadata();
        raCS.setName(buildClassSpaceName());
        raCS.setGeronimoMBeanInfo(GeronimoMBeanInfoXMLLoader.loadMBean(
                ClassLoader.getSystemResource("org/apache/geronimo/kernel/classspace/classspace-mbean.xml")
        ));
        raCS.setParent(JMXUtil.getObjectName("geronimo.system:role=ClassSpace,name=System"));

        List raArchives = raCS.getUrls();

        if ( URLType.PACKED_ARCHIVE == type ) {
            String rootJar = "jar:"+url.toExternalForm();
            try {
                JarFile jFile = new JarFile(url.getFile());
                Enumeration entries = jFile.entries();
                while ( entries.hasMoreElements() ) {
                    JarEntry jEntry = (JarEntry) entries.nextElement();
                    if ( jEntry.isDirectory() ) {
                        continue;
                    }
                    if ( jEntry.getName().endsWith(".jar") ) {
                        raArchives.add(
                            new URL(rootJar + "!/" + jEntry.getName()));
                    }
                    // TODO handle the .so and .dll entries.
                }
            } catch (IOException e) {
                throw new DeploymentException("Should never occurs", e);
            }
        } else if ( URLType.UNPACKED_ARCHIVE == type ) {
            File rootDeploy = new File(url.getFile());
            File[] jarFiles = rootDeploy.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    return pathname.getName().endsWith(".jar");
                }
            });
            for (int i = 0; i < jarFiles.length; i++) {
                try {
                    raArchives.add(jarFiles[i].toURL());
                } catch (MalformedURLException e) {
                    throw new DeploymentException("Should never occurs", e);
                }
            }
            // TODO handle the .so and .dll entries.
        } else {
            throw new DeploymentException("Should never occurs");
        }
        return raCS;
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
                    + ",type=Connector");
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
                    + ",type=Connector");
    }

    /**
     * Build the name of the Connector deployment related to this URL.
     *
     * @return Connector deployment name.
     */

	public ObjectName buildResourceAdapterDeploymentName(GeronimoResourceAdapter gra) {
		return JMXUtil.getObjectName(
				"geronimo.management:j2eeType=JCAResourceAdapter,name="
					+ gra.getName());
	}

	public ObjectName buildManagedConnectionFactoryDeploymentName(GeronimoConnectionDefinition gcd) {
		return JMXUtil.getObjectName(
				"geronimo.management:j2eeType=JCAManagedConnectionFactory,name="
					+ gcd.getName());
	}


	public ObjectName buildConnectionManagerFactoryDeploymentName(GeronimoConnectionDefinition gcd) {
	return JMXUtil.getObjectName(
			"geronimo.management:j2eeType=ConnectionManager,name="
				+ gcd.getName());
	}


	public ObjectName buildMCFHelperDeploymentName(GeronimoConnectionDefinition gcd) {
	return JMXUtil.getObjectName(
			"geronimo.management:j2eeType=MCFHelper,name="
				+ gcd.getName());
	}

    /**
     * @param gra
     * @return
     */
    public ObjectName buildBootstrapContextName(GeronimoResourceAdapter gra) {
		return JMXUtil.getObjectName(
				gra.getBootstrapContext());
    }


}
