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

package org.apache.geronimo.connector.deployment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
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
import org.apache.geronimo.deployment.service.GBeanHelper;
import org.apache.geronimo.common.xml.XmlBeansUtil;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GConstructorInfo;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.xbeans.geronimo.GerConnectorDocument;
import org.apache.geronimo.xbeans.geronimo.GerConnectorType;
import org.apache.geronimo.xbeans.geronimo.GerGbeanType;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

/**
 *
 *
 * @version $Revision: 1.11 $ $Date: 2004/03/21 18:12:06 $
 *
 * */
public abstract class AbstractRARConfigBuilder implements ConfigurationBuilder {

    private static final SchemaTypeLoader[] SCHEMA_TYPE_LOADERS = new SchemaTypeLoader[]{XmlBeans.getContextTypeLoader()};
    protected static final SchemaType TYPE = GerConnectorDocument.type;

    public final static String BASE_RESOURCE_ADAPTER_NAME = "geronimo.management:J2eeType=ResourceAdapter,name=";
    protected final static String BASE_CONNECTION_MANAGER_FACTORY_NAME = "geronimo.management:J2eeType=ConnectionManager,name=";
    protected static final String BASE_REALM_BRIDGE_NAME = "geronimo.security:service=RealmBridge,name=";
    protected static final String BASE_PASSWORD_CREDENTIAL_LOGIN_MODULE_NAME = "geronimo.security:service=Realm,type=PasswordCredential,name=";

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
                    context.addStreamInclude(uri, jarInputStream);
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
         try {
            URL moduleBase = new URL("jar:" + module.toString() + "!/");
            XmlObject plan = XmlBeansUtil.getXmlObject(new URL(moduleBase, "META-INF/geronimo-ra.xml"), GerConnectorDocument.type);
             if (plan != null && canConfigure(plan)) {
                 return plan;
             } else {
                 return null;
             }
         } catch (MalformedURLException e) {
            return null;
        }
    }

    public void buildConfiguration(File outfile, File module, XmlObject plan) throws IOException, DeploymentException {
        if (module.isDirectory()) {
            throw new DeploymentException("Cannot deploy an unpacked RAR");
        }
        FileInputStream is = new FileInputStream(module);
        try {
            buildConfiguration(outfile, new JarInputStream(new BufferedInputStream(is)), plan);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                // ignore
            }
        }
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

            addConnectorGBeans(context, genericConnectorDocument, geronimoConnector, cl);

            context.close();
            os.flush();
        } finally {
            fos.close();

        }
    }

    protected void addGBeans(GerConnectorType geronimoConnector, ClassLoader cl, DeploymentContext context) throws DeploymentException {
        GerGbeanType[] gbeans = geronimoConnector.getGbeanArray();
        for (int i = 0; i < gbeans.length; i++) {
            GBeanHelper.addGbean(new RARGBeanAdapter(gbeans[i]), cl, context);
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
