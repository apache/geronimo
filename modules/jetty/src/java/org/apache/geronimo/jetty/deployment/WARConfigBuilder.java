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

package org.apache.geronimo.jetty.deployment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.transaction.UserTransaction;
import javax.naming.NamingException;

import org.apache.geronimo.deployment.ConfigurationBuilder;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.deployment.service.GBeanHelper;
import org.apache.geronimo.common.xml.XmlBeansUtil;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.jetty.JettyWebApplicationContext;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.naming.deployment.ENCConfigBuilder;
import org.apache.geronimo.naming.java.ComponentContextBuilder;
import org.apache.geronimo.naming.java.ReadOnlyContext;
import org.apache.geronimo.naming.jmx.JMXReferenceFactory;
import org.apache.geronimo.xbeans.geronimo.jetty.JettyGbeanType;
import org.apache.geronimo.xbeans.geronimo.jetty.JettyLocalRefType;
import org.apache.geronimo.xbeans.geronimo.jetty.JettyWebAppDocument;
import org.apache.geronimo.xbeans.geronimo.jetty.JettyWebAppType;
import org.apache.geronimo.xbeans.j2ee.EnvEntryType;
import org.apache.geronimo.xbeans.j2ee.WebAppDocument;
import org.apache.geronimo.xbeans.j2ee.WebAppType;
import org.apache.geronimo.common.xml.XmlBeansUtil;
import org.apache.geronimo.transaction.UserTransactionImpl;

import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

/**
 *
 *
 * @version $Revision: 1.17 $ $Date: 2004/04/07 19:22:15 $
 */
public class WARConfigBuilder implements ConfigurationBuilder {
    private final Repository repository;
    private final Kernel kernel;

    public WARConfigBuilder(Kernel kernel, Repository repository) {
        this.kernel = kernel;
        this.repository = repository;
    }

    public boolean canConfigure(XmlObject plan) {
        return plan instanceof JettyWebAppDocument || plan instanceof WebAppDocument;
    }

    public SchemaTypeLoader[] getTypeLoaders() {
        return new SchemaTypeLoader[]{XmlBeans.getContextTypeLoader()};
    }

    public XmlObject getDeploymentPlan(URL module) {
        try {
            URL moduleBase;
            if (module.toString().endsWith("/")) {
                moduleBase = module;
            } else {
                moduleBase = new URL("jar:" + module.toString() + "!/");
            }
            XmlObject plan = XmlBeansUtil.getXmlObject(new URL(moduleBase, "WEB-INF/geronimo-jetty.xml"), JettyWebAppDocument.type);
// todo needs generic web XMLBeans
//            if (plan == null) {
//                plan = getPlan(new URL(moduleBase, "WEB-INF/geronimo-web.xml"));
//            }
// todo should be able to deploy a naked WAR
//            if (plan == null) {
//                plan = getPlan(new URL(moduleBase, "WEB-INF/web.xml"), WebAppDocument.type);
//            }
            return plan;
        } catch (MalformedURLException e) {
            return null;
        }
    }


    public void buildConfiguration(File outfile, File module, XmlObject plan) throws IOException, DeploymentException {
        if (!module.isDirectory()) {
            FileInputStream in = new FileInputStream(module);
            try {
                buildConfiguration(outfile, in, plan);
                return;
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        WebAppType webApp = getDD(module);
        JettyWebAppType jettyWebApp = ((JettyWebAppDocument) plan).getWebApp();
        URI configID = getConfigID(jettyWebApp);
        URI parentID = getParentID(jettyWebApp);

        FileOutputStream fos = new FileOutputStream(outfile);
        try {
            JarOutputStream os = new JarOutputStream(new BufferedOutputStream(fos));
            DeploymentContext context = null;
            try {
                context = new DeploymentContext(os, configID, parentID, kernel);
            } catch (MalformedObjectNameException e) {
                throw new DeploymentException(e);
            }

            buildGBeanConfiguration(context, jettyWebApp, webApp, module.getAbsoluteFile().toURI());

            context.close();
            os.flush();
        } finally {
            fos.close();
        }
    }

    private WebAppType getDD(File module) throws IOException, DeploymentException {
        File dd = new File(module, "WEB-INF/web.xml");
        if (!(dd.exists() && dd.canRead())) {
            throw new DeploymentException("Cannot read WEB-INF/web.xml from module directory");
        }
        FileInputStream is = new FileInputStream(dd);
        try {
            try {
                WebAppDocument doc = (WebAppDocument) XmlBeansUtil.parse(new BufferedInputStream(is), WebAppDocument.type);
                return doc.getWebApp();
            } catch (XmlException e) {
                throw new DeploymentException("Unable to parse web.xml", e);
            }
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    public void buildConfiguration(File outfile, InputStream in, XmlObject plan) throws IOException, DeploymentException {
        WebAppType webApp = null;
        JettyWebAppType jettyWebApp = ((JettyWebAppDocument) plan).getWebApp();
        URI configID = getConfigID(jettyWebApp);
        URI parentID = getParentID(jettyWebApp);

        FileOutputStream fos = new FileOutputStream(outfile);
        JarInputStream module = null;
        try {
            module = new JarInputStream(new BufferedInputStream(in));
            JarOutputStream os = new JarOutputStream(new BufferedOutputStream(fos));
            DeploymentContext context = null;
            try {
                context = new DeploymentContext(os, configID, parentID, kernel);
            } catch (MalformedObjectNameException e) {
                throw new DeploymentException(e);
            }

            // add the warfile's content to the configuration
            URI warRoot = URI.create("war/");
            ZipEntry src;
            while ((src = module.getNextEntry()) != null) {
                URI target = warRoot.resolve(src.getName());
                if ("WEB-INF/web.xml".equals(src.getName())) {
                    byte[] buffer = getBytes(module);
                    context.addFile(target, new ByteArrayInputStream(buffer));
                    try {
                        WebAppDocument doc = (WebAppDocument) XmlBeansUtil.parse(new ByteArrayInputStream(buffer), WebAppDocument.type);
                        webApp = doc.getWebApp();
                    } catch (XmlException e) {
                        throw new DeploymentException("Unable to parse web.xml");
                    }
                } else {
                    context.addFile(target, module);
                }
            }

            if (webApp == null) {
                throw new DeploymentException("Did not find WEB-INF/web.xml in module");
            }

            buildGBeanConfiguration(context, jettyWebApp, webApp, warRoot);


            context.close();
            os.flush();
        } finally {
            if (module != null) {
                try {
                    module.close();
                } catch (IOException e) {
                    // ignore
                }
            }
            fos.close();
        }
    }

    private void buildGBeanConfiguration(DeploymentContext context, JettyWebAppType jettyWebApp, WebAppType webApp, URI warRoot) throws DeploymentException {
        // todo do we need to support include and dependency or can we rely on the parent?
        // add low-level GBean definitions to the config
//            addIncludes(context, configType);
//            addDependencies(context, configType.getDependencyArray());
        ClassLoader cl = context.getClassLoader(repository);

        JettyGbeanType[] gbeans = jettyWebApp.getGbeanArray();
        for (int i = 0; i < gbeans.length; i++) {
            GBeanHelper.addGbean(new JettyGBeanAdapter(gbeans[i]), cl, context);
        }


        // add the GBean for the web application
        addWebAppGBean(context, webApp, jettyWebApp, warRoot, cl);

        // todo do we need to add GBeans to make the servlets JSR77 ManagedObjects?
    }

    private void addWebAppGBean(DeploymentContext context, WebAppType webApp, JettyWebAppType jettyWebApp, URI warRoot, ClassLoader cl) throws DeploymentException {
        String contextRoot = jettyWebApp.getContextRoot().trim();
        if (contextRoot.length() == 0) {
            throw new DeploymentException("Missing value for context-root");
        }
        URI configID = context.getConfigID();

        Properties nameProps = new Properties();
        nameProps.put("J2EEServer", "null");
        nameProps.put("J2EEApplication", "null");
        nameProps.put("J2EEType", "WebModule");
        nameProps.put("ContextRoot", contextRoot);
        nameProps.put("Config", configID.toString());
        ObjectName name;
        try {
            name = new ObjectName("geronimo.jetty", nameProps);
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException("Unable to construct ObjectName", e);
        }

        UserTransaction userTransaction = new UserTransactionImpl();
        ReadOnlyContext compContext = buildComponentContext(webApp, jettyWebApp, userTransaction, cl);

        GBeanMBean gbean = new GBeanMBean(JettyWebApplicationContext.GBEAN_INFO);
        try {
            gbean.setAttribute("URI", warRoot);
            gbean.setAttribute("ContextPath", contextRoot);
            gbean.setAttribute("ContextPriorityClassLoader", Boolean.valueOf(jettyWebApp.getContextPriorityClassloader()));
            gbean.setAttribute("PolicyContextID", null);
            gbean.setAttribute("ComponentContext", compContext);
            gbean.setAttribute("UserTransaction", userTransaction);
            gbean.setReferencePatterns("Configuration", Collections.singleton(ConfigurationManager.getConfigObjectName(configID)));
            gbean.setReferencePatterns("JettyContainer", Collections.singleton(new ObjectName("*:type=WebContainer,container=Jetty"))); // @todo configurable
            gbean.setReferencePatterns("TransactionManager", Collections.singleton(new ObjectName("*:type=TransactionManager,*")));
            gbean.setReferencePatterns("TrackedConnectionAssociator", Collections.singleton(new ObjectName("*:type=ConnectionTracker,*")));
        } catch (Exception e) {
            throw new DeploymentException("Unable to initialize webapp GBean", e);
        }
        context.addGBean(name, gbean);
    }

    private ReadOnlyContext buildComponentContext(WebAppType webApp, JettyWebAppType jettyWebApp, UserTransaction userTransaction, ClassLoader cl) throws DeploymentException {
        ComponentContextBuilder builder = new ComponentContextBuilder(new JMXReferenceFactory());
        if (userTransaction != null) {
            try {
                builder.addUserTransaction(userTransaction);
            } catch (NamingException e) {
                throw new DeploymentException("Unable to bind UserTransaction into ENC", e);
            }
        }

        EnvEntryType[] envEntries = webApp.getEnvEntryArray();
        ENCConfigBuilder.addEnvEntries(envEntries, builder);
        // todo ejb-ref
        // todo ejb-local-ref
        // todo resource-ref
        Map resourceRefMap = new HashMap();
        JettyLocalRefType[] jettyResourceRefs = jettyWebApp.getResourceRefArray();
        for (int i = 0; i < jettyResourceRefs.length; i++) {
            JettyLocalRefType jettyResourceRef = jettyResourceRefs[i];
            resourceRefMap.put(jettyResourceRef.getRefName(), new JettyRefAdapter(jettyResourceRef));
        }
        ENCConfigBuilder.addResourceRefs(webApp.getResourceRefArray(), cl, resourceRefMap, builder);
        // todo resource-env-ref
        Map resourceEnvRefMap = new HashMap();
        JettyLocalRefType[] jettyResourceEnvRefs = jettyWebApp.getResourceEnvRefArray();
        for (int i = 0; i < jettyResourceEnvRefs.length; i++) {
            JettyLocalRefType jettyResourceEnvRef = jettyResourceEnvRefs[i];
            resourceEnvRefMap.put(jettyResourceEnvRef.getRefName(), new JettyRefAdapter(jettyResourceEnvRef));
        }
        ENCConfigBuilder.addResourceEnvRefs(webApp.getResourceEnvRefArray(), cl, resourceEnvRefMap, builder);
        // todo message-destination-ref
        return builder.getContext();
    }


    private URI getParentID(JettyWebAppType jettyWebApp) throws DeploymentException {
        URI parentID;
        if (jettyWebApp.isSetParentId()) {
            try {
                parentID = new URI(jettyWebApp.getParentId());
            } catch (URISyntaxException e) {
                throw new DeploymentException("Invalid parentId " + jettyWebApp.getParentId(), e);
            }
        } else {
            parentID = null;
        }
        return parentID;
    }

    private URI getConfigID(JettyWebAppType jettyWebApp) throws DeploymentException {
        URI configID;
        try {
            configID = new URI(jettyWebApp.getConfigId());
        } catch (URISyntaxException e) {
            throw new DeploymentException("Invalid configId " + jettyWebApp.getConfigId(), e);
        }
        return configID;
    }

    private byte[] getBytes(InputStream is) throws IOException {
        byte[] buffer = new byte[4096];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int count;
        while ((count = is.read(buffer)) > 0) {
            baos.write(buffer, 0, count);
        }
        return baos.toByteArray();
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(WARConfigBuilder.class);
        infoFactory.addInterface(ConfigurationBuilder.class);
        infoFactory.addReference("Repository", Repository.class);
        infoFactory.addReference("Kernel", Kernel.class);
        infoFactory.setConstructor(
                new String[]{"Kernel", "Repository"},
                new Class[]{Kernel.class, Repository.class}
        );
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
