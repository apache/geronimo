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

package org.apache.geronimo.tomcat.deployment;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ModuleBuilder;
import org.apache.geronimo.j2ee.deployment.WebModule;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContextImpl;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.tomcat.TomcatWebAppContext;
import org.apache.geronimo.xbeans.geronimo.jetty.JettyWebAppDocument;
import org.apache.geronimo.xbeans.geronimo.jetty.JettyWebAppType;
import org.apache.geronimo.xbeans.j2ee.WebAppDocument;
import org.apache.geronimo.xbeans.j2ee.WebAppType;


/**
 * @version $Rev: 106522 $ $Date: 2004-11-25 01:28:57 +0100 (Thu, 25 Nov 2004) $
 */
public class TomcatModuleBuilder implements ModuleBuilder {

    private final URI defaultParentId;

    private static final Log log = LogFactory.getLog(TomcatModuleBuilder.class);

    public TomcatModuleBuilder(URI defaultParentId) {
        log.debug("TomcatModuleBuilder(" + defaultParentId + ")");

        this.defaultParentId = defaultParentId;
    }

    public String addGBeans(EARContext earContext, Module module, ClassLoader cl) throws DeploymentException {
        J2eeContext earJ2eeContext = earContext.getJ2eeContext();
        J2eeContext moduleJ2eeContext = new J2eeContextImpl(earJ2eeContext.getJ2eeDomainName(),
                                                            earJ2eeContext.getJ2eeServerName(),
                                                            earJ2eeContext.getJ2eeApplicationName(),
                                                            module.getName(), null, null);
        WebModule webModule = (WebModule) module;

        // construct the webClassLoader
        URI[] webClassPath = getWebClassPath(earContext, webModule);
        URI baseUri = earContext.getTargetFile(URI.create(webModule.getTargetPath() + "/")).toURI();
        URL[] webClassPathURLs = new URL[webClassPath.length];
        for (int i = 0; i < webClassPath.length; i++) {
            URI path = baseUri.resolve(webClassPath[i]);
            try {
                webClassPathURLs[i] = path.toURL();
            } catch (MalformedURLException e) {
                throw new DeploymentException("Invalid web class path element: path=" + path + ", baseUri=" + baseUri);
            }
        }

        ObjectName webModuleName = null;
        try {
            webModuleName = NameFactory.getModuleName(null, null, null, null, NameFactory.WEB_MODULE, moduleJ2eeContext);
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException("Could not construct module name", e);
        }

        GBeanData gbean;
        try {
            gbean = new GBeanData(TomcatWebAppContext.GBEAN_INFO);

            gbean.setName(webModuleName);
            gbean.setAttribute("webAppRoot", baseUri);
            gbean.setAttribute("webClassPath", webClassPath);

            gbean.setAttribute("path", webModule.getContextRoot());

            gbean.setReferencePattern("Container", new ObjectName("*:type=WebContainer,container=Tomcat"));
        } catch (Exception e) {
            throw new DeploymentException("Unable to initialize webapp GBean", e);
        }
        earContext.addGBean(gbean);

        return null;
    }

    public Module createModule(File plan, JarFile moduleFile) throws DeploymentException {
        log.debug("createModule: " + plan + "; " + moduleFile);

        // parse the spec dd
        String specDD;
        WebAppType webApp;
        try {
            specDD = DeploymentUtil.readAll(DeploymentUtil.createJarURL(moduleFile, "WEB-INF/web.xml"));

            // parse it
            XmlObject parsed = SchemaConversionUtils.parse(specDD);
            WebAppDocument webAppDoc = SchemaConversionUtils.convertToServletSchema(parsed);
            webApp = webAppDoc.getWebApp();
        } catch (XmlException xmle) {
            throw new DeploymentException("Error parsing web.xml", xmle);
        } catch (Exception e) {
            return null;
        }

        // parse vendor dd
        JettyWebAppType jettyWebApp = getJettyWebApp(plan, moduleFile, false, "war", webApp);

        // get the ids from either the application plan or for a stand alone
        // module from the specific deployer
        URI configId = null;
        try {
            configId = new URI(jettyWebApp.getConfigId());
        } catch (URISyntaxException e) {
            throw new DeploymentException("Invalid configId " + jettyWebApp.getConfigId(), e);
        }

        URI parentId = null;
        if (jettyWebApp.isSetParentId()) {
            try {
                parentId = new URI(jettyWebApp.getParentId());
            } catch (URISyntaxException e) {
                throw new DeploymentException("Invalid parentId " + jettyWebApp.getParentId(), e);
            }
        } else {
            parentId = defaultParentId;
        }

        // for now, every webapps will get defaultParentId which is
        // org/apache/geronimo/Tomcat
        // TODO: Create a general WebModuleBuilder and hand over deploying a
        // webapp to Jetty- or TomcatModuleBuilder based on parentId.
        log.warn("Assigning defaultParentId [" + defaultParentId + "] to webapp [" + configId + "]");
        parentId = defaultParentId;

        WebModule module = new WebModule(false, configId, parentId, moduleFile, "war", webApp, jettyWebApp, specDD);
        module.setContextRoot(jettyWebApp.getContextRoot());

        return module;
    }

    public Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, URI earConfigId)
            throws DeploymentException {
        log.debug("createModule: " + plan + "; " + moduleFile + "; " + targetPath + "; " + specDDUrl + "; "
                  + earConfigId);
        return null;
    }

    /**
     * What's the difference between this and createModule - the params are the
     * same
     */
    public void initContext(EARContext earContext, Module module, ClassLoader cl) throws DeploymentException {
        log.debug("initContext: " + earContext + "; " + module + "; " + cl);
    }

    public void installModule(JarFile earFile, EARContext earContext, Module module) throws DeploymentException {
        log.debug("installModule: " + earFile + "; " + earContext + "; " + module);
        try {
            URI baseDir = URI.create(module.getTargetPath() + "/");

            // add the warfile's content to the configuration
            JarFile warFile = module.getModuleFile();
            Enumeration entries = warFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                URI targetPath = baseDir.resolve(entry.getName());
                /*
                 * if (entry.getName().equals("WEB-INF/web.xml")) {
                 * earContext.addFile(targetPath, module.getOriginalSpecDD()); }
                 * else { earContext.addFile(targetPath, warFile, entry); }
                 */
                earContext.addFile(targetPath, warFile, entry);
            }

            // add the manifest classpath entries declared in the war to the
            // class loader
            // we have to explicitly add these since we are unpacking the web
            // module
            // and the url class loader will not pick up a manifiest from an
            // unpacked dir
            earContext.addManifestClassPath(warFile, URI.create(module.getTargetPath()));

        } catch (IOException e) {
            throw new DeploymentException("Problem deploying war", e);
        }
    }

    JettyWebAppType getJettyWebApp(Object plan, JarFile moduleFile, boolean standAlone, String targetPath,
                                   WebAppType webApp) throws DeploymentException {
        JettyWebAppType jettyWebApp = null;
        try {
            // load the geronimo-jetty.xml from either the supplied plan or from
            // the earFile
            try {
                if (plan instanceof XmlObject) {
                    jettyWebApp = (JettyWebAppType) SchemaConversionUtils.getNestedObjectAsType((XmlObject) plan,
                                                                                                "web-app", JettyWebAppType.type);
                } else {
                    JettyWebAppDocument jettyWebAppdoc = null;
                    if (plan != null) {
                        jettyWebAppdoc = JettyWebAppDocument.Factory.parse((File) plan);
                    } else {
                        URL path = DeploymentUtil.createJarURL(moduleFile, "WEB-INF/geronimo-jetty.xml");
                        jettyWebAppdoc = JettyWebAppDocument.Factory.parse(path);
                    }
                    if (jettyWebAppdoc != null) {
                        jettyWebApp = jettyWebAppdoc.getWebApp();
                    }
                }
            } catch (IOException e) {
            }

            // if we got one extract and validate it otherwise create a default
            // one
            if (jettyWebApp != null) {
                jettyWebApp = (JettyWebAppType) SchemaConversionUtils.convertToGeronimoNamingSchema(jettyWebApp);
                jettyWebApp = (JettyWebAppType) SchemaConversionUtils.convertToGeronimoSecuritySchema(jettyWebApp);
                SchemaConversionUtils.validateDD(jettyWebApp);
            } else {
                String path;
                if (standAlone) {
                    // default configId is based on the moduleFile name
                    path = new File(moduleFile.getName()).getName();
                } else {
                    // default configId is based on the module uri from the
                    // application.xml
                    path = targetPath;
                }
                jettyWebApp = createDefaultPlan(path, webApp);
            }
        } catch (XmlException e) {
            throw new DeploymentException("xml problem", e);
        }
        return jettyWebApp;
    }

    private JettyWebAppType createDefaultPlan(String path, WebAppType webApp) {
        String id = webApp.getId();
        if (id == null) {
            id = path;
            if (id.endsWith(".war")) {
                id = id.substring(0, id.length() - 4);
            }
            if (id.endsWith("/")) {
                id = id.substring(0, id.length() - 1);
            }
        }

        JettyWebAppType jettyWebApp = JettyWebAppType.Factory.newInstance();

        // set the parentId, configId and context root
        jettyWebApp.setParentId(defaultParentId.toString());
        if (null != webApp.getId()) {
            id = webApp.getId();
        }
        jettyWebApp.setConfigId(id);
        jettyWebApp.setContextRoot(id);
        return jettyWebApp;
    }

    private static URI[] getWebClassPath(EARContext earContext, WebModule webModule) {
        LinkedList webClassPath = new LinkedList();
        File baseDir = earContext.getTargetFile(URI.create(webModule.getTargetPath() + "/"));
        File webInfDir = new File(baseDir, "WEB-INF");

        // check for a classes dir
        File classesDir = new File(webInfDir, "classes");
        if (classesDir.isDirectory()) {
            webClassPath.add(URI.create("WEB-INF/classes/"));
        }

        // add all of the libs
        File libDir = new File(webInfDir, "lib");
        if (libDir.isDirectory()) {
            File[] libs = libDir.listFiles(new FileFilter() {
                public boolean accept(File file) {
                    return file.isFile() && file.getName().endsWith(".jar");
                }
            });

            if (libs != null) {
                for (int i = 0; i < libs.length; i++) {
                    File lib = libs[i];
                    webClassPath.add(URI.create("WEB-INF/lib/" + lib.getName()));
                }
            }
        }
        return (URI[]) webClassPath.toArray(new URI[webClassPath.size()]);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = new GBeanInfoBuilder(TomcatModuleBuilder.class);
        infoBuilder.addAttribute("defaultParentId", URI.class, true);
        infoBuilder.addInterface(ModuleBuilder.class);

        infoBuilder.setConstructor(new String[]{"defaultParentId"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
