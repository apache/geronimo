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

import java.io.InputStream;
import java.net.URI;

import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.exceptions.InvalidModuleException;
import javax.management.ObjectName;

import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.deployment.DeploymentModule;
import org.apache.geronimo.deployment.plugin.factories.DeploymentConfigurationFactory;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GConstructorInfo;
import org.apache.geronimo.xbeans.geronimo.GerConnectorDocument;
import org.apache.geronimo.xbeans.geronimo.GerVersionType;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlObject;

/**
 *
 *
 * @version $Revision: 1.3 $ $Date: 2004/02/08 20:21:57 $
 *
 * */
public class RARConfigurationFactory implements DeploymentConfigurationFactory {
    private final ObjectName connectionTrackerNamePattern;
    public static final GBeanInfo GBEAN_INFO;
    private final static SchemaTypeLoader SCHEMA_TYPE_LOADER = XmlBeans.getContextTypeLoader();

    public RARConfigurationFactory(ObjectName connectionTrackerNamePattern) {
        this.connectionTrackerNamePattern = connectionTrackerNamePattern;
    }

    public ObjectName getConnectionTrackerNamePattern() {
        return connectionTrackerNamePattern;
    }

    public DeploymentConfiguration createConfiguration(DeployableObject deployable) throws InvalidModuleException {
        if (!ModuleType.RAR.equals(deployable.getType())) {
            throw new InvalidModuleException("DeployableObject must be a RAR");
        }
        return new RARConfiguration(deployable);
    }

    public DeploymentModule createModule(InputStream moduleArchive, XmlObject geronimoDD, URI configID, boolean isLocal) throws DeploymentException {
        GerConnectorDocument geronimoConnectorDocument = (GerConnectorDocument) geronimoDD;
        GerVersionType.Enum version = geronimoConnectorDocument.getConnector().getVersion();
        if (version.equals(GerVersionType.X_1_0)) {
        return new Connector_1_0Module(configID, moduleArchive, geronimoConnectorDocument, getConnectionTrackerNamePattern());
        } else if (version.equals(GerVersionType.X_1_5)) {
            return new Connector_1_5Module(configID, moduleArchive, geronimoConnectorDocument, getConnectionTrackerNamePattern());
        } else {
            throw new DeploymentException("Invalid plan version: " + version);
        }
    }

    public SchemaTypeLoader getSchemaTypeLoader() {
        return SCHEMA_TYPE_LOADER;
    }

    //these might be temporary
    public SchemaType getSchemaType() {
        return GerConnectorDocument.type;
    }

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory("Geronimo RAR Configuration Factory", RARConfigurationFactory.class.getName());
        infoFactory.addInterface(DeploymentConfigurationFactory.class);
        infoFactory.addAttribute(new GAttributeInfo("ConnectionTrackerNamePattern", true));
        infoFactory.setConstructor(new GConstructorInfo(
                new String[]{"ConnectionTrackerNamePattern"},
                new Class[]{ObjectName.class}));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return RARConfigurationFactory.GBEAN_INFO;
    }

}
