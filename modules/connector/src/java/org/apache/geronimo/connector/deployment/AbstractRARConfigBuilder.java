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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.geronimo.deployment.ConfigurationBuilder;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GConstructorInfo;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.xbeans.geronimo.GerConnectorDocument;
import org.apache.geronimo.xbeans.geronimo.GerConnectorType;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/21 01:10:49 $
 *
 * */
public abstract class AbstractRARConfigBuilder implements ConfigurationBuilder {

    private static final SchemaTypeLoader[] SCHEMA_TYPE_LOADERS = new SchemaTypeLoader[]{XmlBeans.getContextTypeLoader()};
    protected static final SchemaType TYPE = GerConnectorDocument.type;

    public final static String BASE_RESOURCE_ADAPTER_NAME = "geronimo.management:J2eeType=ResourceAdapter,name=";
    protected final static String BASE_CONNECTION_MANAGER_FACTORY_NAME = "geronimo.management:J2eeType=ConnectionManager,name=";
    protected static final String BASE_MANAGED_CONNECTION_FACTORY_NAME = "geronimo.management:J2eeType=ManagedConnectionFactory,name=";
    protected static final String BASE_REALM_BRIDGE_NAME = "geronimo.security:service=RealmBridge,name=";
    private static final String BASE_PASSWORD_CREDENTIAL_LOGIN_MODULE_NAME = "geronimo.security:service=Realm,type=PasswordCredential,name=";
    protected static final String BASE_ADMIN_OBJECT_NAME = "geronimo.management:service=AdminObject,name=";

    protected final Repository repository;
    protected final Kernel kernel;
    protected final ObjectName connectionTrackerNamePattern;

    public AbstractRARConfigBuilder(Kernel kernel, Repository repository, ObjectName connectionTrackerNamePattern) {
        this.kernel = kernel;
        this.repository = repository;
        this.connectionTrackerNamePattern = connectionTrackerNamePattern;
    }

    public SchemaTypeLoader[] getTypeLoaders() {
        return SCHEMA_TYPE_LOADERS;
    }

    protected XmlObject generateClassPath(URI configID, JarInputStream jarInputStream, DeploymentContext context) throws DeploymentException {
        URI moduleBase = URI.create(configID.toString() + "/");
        XmlObject j2eeDoc = null;
        try {
            for (JarEntry entry; (entry = jarInputStream.getNextJarEntry()) != null; jarInputStream.closeEntry()) {
                String name = entry.getName();
                if (name.endsWith("/")) {
                    continue;
                }
                if (name.equals("META-INF/ra.xml")) {
                    j2eeDoc = getConnectorDocument(jarInputStream);
                    continue;
                }
                if (name.endsWith(".jar")) {
                    URI uri = moduleBase.resolve(name);
                    context.addFile(uri, jarInputStream);
                    context.addToClassPath(uri);
                }
                //native libraries?
            }
        } catch (IOException e) {
            throw new DeploymentException(e);
        } catch (XmlException e) {
            throw new DeploymentException(e);
        }
        if (j2eeDoc == null) {
            throw new DeploymentException("Did not find required META-INF/ra.xml deployment descriptor");
        }
        return j2eeDoc;
    }

    protected abstract XmlObject getConnectorDocument(JarInputStream jarInputStream) throws XmlException, IOException, DeploymentException;

    public XmlObject getDeploymentPlan(URL module) {
        //for starters we require an external geronimo dd.
        return null;
    }

    public void buildConfiguration(File outfile, JarInputStream module, XmlObject plan) throws IOException, DeploymentException {
        GerConnectorType geronimoConnector = ((GerConnectorDocument) plan).getConnector();
        URI configID;
        try {
            configID = new URI(geronimoConnector.getConfigId());
        } catch (URISyntaxException e) {
            throw new DeploymentException("Invalid configId " + geronimoConnector.getConfigId(), e);
        }
        URI parentID;
        if (geronimoConnector.isSetParentId()) {
            try {
                parentID = new URI(geronimoConnector.getParentId());
            } catch (URISyntaxException e) {
                throw new DeploymentException("Invalid parentId " + geronimoConnector.getParentId(), e);
            }
        } else {
            parentID = null;
        }

        FileOutputStream fos = new FileOutputStream(outfile);
        try {
            JarOutputStream os = new JarOutputStream(new BufferedOutputStream(fos));
            DeploymentContext context = null;
            try {
                context = new DeploymentContext(os, configID, parentID, kernel);
            } catch (MalformedObjectNameException e) {
                throw new DeploymentException(e);
            }

            XmlObject genericConnectorDocument = generateClassPath(configID, module, context);
            ClassLoader cl = context.getClassLoader(repository);
            //addGBeans(context, geronimoConnector.getGbeanArray(), cl);

            addConnectorGBeans(context, genericConnectorDocument, geronimoConnector, cl);

            context.close();
            os.flush();
        } finally {
            fos.close();
        }
    }

    abstract void addConnectorGBeans(DeploymentContext context, XmlObject gerericConnectorDocument, GerConnectorType geronimoConnector, ClassLoader cl) throws DeploymentException;

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory("Geronimo RAR Configuration Factory", AbstractRARConfigBuilder.class.getName());
        infoFactory.addInterface(ConfigurationBuilder.class);
        infoFactory.addAttribute(new GAttributeInfo("ConnectionTrackerNamePattern", true));
        infoFactory.addReference(new GReferenceInfo("Repository", Repository.class));
        infoFactory.addReference(new GReferenceInfo("Kernel", Kernel.class));
        infoFactory.setConstructor(new GConstructorInfo(
                new String[]{"Kernel", "Repository", "ConnectionTrackerNamePattern"},
                new Class[]{Kernel.class, Repository.class, ObjectName.class}));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }


}
