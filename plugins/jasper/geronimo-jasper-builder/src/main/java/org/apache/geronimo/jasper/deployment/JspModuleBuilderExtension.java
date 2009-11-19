/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.jasper.deployment;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.j2ee.annotation.Holder;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ModuleBuilderExtension;
import org.apache.geronimo.j2ee.deployment.NamingBuilder;
import org.apache.geronimo.j2ee.deployment.WebModule;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.jasper.JasperServletContextCustomizer;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.web25.deployment.AbstractWebModuleBuilder;
import org.apache.geronimo.xbeans.javaee.FullyQualifiedClassType;
import org.apache.geronimo.xbeans.javaee.JspConfigType;
import org.apache.geronimo.xbeans.javaee.ListenerType;
import org.apache.geronimo.xbeans.javaee.TagType;
import org.apache.geronimo.xbeans.javaee.TaglibDocument;
import org.apache.geronimo.xbeans.javaee.TaglibType;
import org.apache.geronimo.xbeans.javaee.TldTaglibType;
import org.apache.geronimo.xbeans.javaee.WebAppType;
import org.apache.xbean.finder.ClassFinder;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import org.osgi.framework.Bundle;

/**
 * This JSP module builder extension is meant to find all the TLD descriptor files associated with a
 * deployable artifact, search those TLD files for listeners, search those listeners for
 * annotations, and ultimately create a ClassFinder using those annoated classes (for later
 * processing by the various naming builders)
 *
 * @version $Rev $Date
 */
@GBean(j2eeType = NameFactory.MODULE_BUILDER)
public class JspModuleBuilderExtension implements ModuleBuilderExtension {

    private static final Logger log = LoggerFactory.getLogger(JspModuleBuilderExtension.class);

    private final Environment defaultEnvironment;
    private final NamingBuilder namingBuilders;

    private static final QName TLIB_VERSION = new QName(SchemaConversionUtils.JAVAEE_NAMESPACE, "tlib-version");
    private static final QName SHORT_NAME = new QName(SchemaConversionUtils.JAVAEE_NAMESPACE, "short-name");
    private static final QName TAG_CLASS = new QName(SchemaConversionUtils.JAVAEE_NAMESPACE, "tag-class");
    private static final QName TEI_CLASS = new QName(SchemaConversionUtils.JAVAEE_NAMESPACE, "tei-class");
    private static final QName BODY_CONTENT = new QName(SchemaConversionUtils.JAVAEE_NAMESPACE, "body-content");

    private static final String SCHEMA_LOCATION_URL = "http://java.sun.com/xml/ns/javaee/web-jsptaglibrary_2_1.xsd";
    private static final String VERSION = "2.1";

    public JspModuleBuilderExtension(@ParamAttribute(name="defaultEnvironment")Environment defaultEnvironment,
                                     @ParamReference(name="NamingBuilders", namingType = NameFactory.MODULE_BUILDER)NamingBuilder namingBuilders) {
        this.defaultEnvironment = defaultEnvironment;
        this.namingBuilders = namingBuilders;
    }

    public void createModule(Module module, Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, Environment environment, Object moduleContextInfo, AbstractName earName, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
    }

    public void installModule(JarFile earFile, EARContext earContext, Module module, Collection configurationStores, ConfigurationStore targetConfigurationStore, Collection repository) throws DeploymentException {
    }

    public void initContext(EARContext earContext, Module module, Bundle bundle) throws DeploymentException {
    }

    public void addGBeans(EARContext earContext, Module module, Bundle bundle, Collection repository) throws DeploymentException {
        if (!(module instanceof WebModule)) {
            //not a web module, nothing to do
            return;
        }
        //TODO Only merge if we detect jsps???
        EnvironmentBuilder.mergeEnvironments(module.getEnvironment(), defaultEnvironment);

        WebModule webModule = (WebModule) module;
        WebAppType webApp = (WebAppType) webModule.getSpecDD();

        EARContext moduleContext = module.getEarContext();
        Map sharedContext = module.getSharedContext();
        GBeanData jspServletData = AbstractWebModuleBuilder.DEFAULT_JSP_SERVLET_KEY.get(sharedContext);
        if (jspServletData != null) {
            try {
                moduleContext.addGBean(jspServletData);
            } catch (GBeanAlreadyExistsException e) {
                throw new DeploymentException("jsp servlet already present", e);
            }
        }

        GBeanData webAppData = (GBeanData) sharedContext.get(WebModule.WEB_APP_DATA);

        AbstractName moduleName = moduleContext.getModuleName();
        Map<NamingBuilder.Key, Object> buildingContext = new HashMap<NamingBuilder.Key, Object>();
        buildingContext.put(NamingBuilder.GBEAN_NAME_KEY, moduleName);

        //use the same jndi context as the web app
        Map compContext = NamingBuilder.JNDI_KEY.get(sharedContext);
        buildingContext.put(NamingBuilder.JNDI_KEY, compContext);

        //use the same holder object as the web app.
        Holder holder = NamingBuilder.INJECTION_KEY.get(sharedContext);
        buildingContext.put(NamingBuilder.INJECTION_KEY, holder);

        XmlObject jettyWebApp = webModule.getVendorDD();

        Configuration earConfiguration = earContext.getConfiguration();

        Set<String> listenerNames = new HashSet<String>();

        ClassFinder classFinder = createJspClassFinder(webApp, webModule, listenerNames);
        webModule.setClassFinder(classFinder);

        namingBuilders.buildNaming(webApp, jettyWebApp, webModule, buildingContext);

        //only try to install it if reference will work.
        //Some users (tomcat?) may have back doors into jasper that make adding this gbean unnecessary.
        GBeanInfo webAppGBeanInfo = webAppData.getGBeanInfo();
        if (webAppGBeanInfo.getReference("ContextCustomizer") != null) {
            AbstractName jspLifecycleName = moduleContext.getNaming().createChildName(moduleName, "jspLifecycleProvider", GBeanInfoBuilder.DEFAULT_J2EE_TYPE);
            GBeanData gbeanData = new GBeanData(jspLifecycleName, JasperServletContextCustomizer.GBEAN_INFO);
            gbeanData.setAttribute("holder", holder);

            try {
                moduleContext.addGBean(gbeanData);
            } catch (GBeanAlreadyExistsException e) {
                throw new DeploymentException("Duplicate jspLifecycleProvider", e);
            }

            webAppData.setReferencePattern("ContextCustomizer", jspLifecycleName);
        }
        //add listeners if possible
        //we may need to add them in another way for tomcat
        Object value = webAppData.getAttribute("listenerClassNames");
        if (value instanceof Collection) {
            ((Collection<String>) value).addAll(listenerNames);
        }
    }

    protected ClassFinder createJspClassFinder(WebAppType webApp, WebModule webModule, Set<String> listenerNames) throws DeploymentException {
        List<URL> urls = getTldFiles(webApp, webModule);
        List<Class> classes = getListenerClasses(webApp, webModule, urls, listenerNames);
        return new ClassFinder(classes);
    }


    /**
     * getTldFiles(): Find all the TLD files in the web module being deployed
     *
     * <p>Locations to search for these TLD file(s) (matches the precedence search order for TLD
     * files per the JSP specs):
     * <ol>
     *      <li>web.xml <taglib> entries
     *      <li>TLD(s) in JAR files in WEB-INF/lib
     *      <li>TLD(s) under WEB-INF
     *      <li>All TLD files in all META-INF(s)
     * </ol>
     *
     * @param webApp    spec DD for module
     * @param webModule module being deployed
     * @return list of the URL(s) for the TLD files
     * @throws DeploymentException if there's a problem finding a tld file
     */
    private List<URL> getTldFiles(WebAppType webApp, WebModule webModule) throws DeploymentException {
        if (log.isDebugEnabled()) {
            log.debug("getTldFiles( " + webApp.toString() + "," + webModule.getName() + " ): Entry");
        }

        List<URL> tldURLs = new ArrayList<URL>();

        // 1. web.xml <taglib> entries
        JspConfigType[] jspConfigs = webApp.getJspConfigArray();
        for (JspConfigType jspConfig : jspConfigs) {
            TaglibType[] taglibs = jspConfig.getTaglibArray();
            for (TaglibType taglib : taglibs) {
                String uri = taglib.getTaglibUri().getStringValue().trim();
                String location = taglib.getTaglibLocation().getStringValue().trim();
                if (!location.equals("")) {
                    if (location.startsWith("/")) {
                        location = location.substring(1);
                    }
                    try {
                        File targetFile = webModule.getEarContext().getTargetFile(createURI(location));
                        if (targetFile!=null) {
                            tldURLs.add(targetFile.toURL());
                        }
                    }
                    catch (MalformedURLException mfe) {
                        throw new DeploymentException("Could not locate TLD file specified in <taglib>: URI: " + uri + " Location: " + location + " " + mfe.getMessage(), mfe);
                    }
                    catch (URISyntaxException use) {
                        throw new DeploymentException("Could not locate TLD file specified in <taglib>: URI: " + uri + " Location: " + location + " " + use.getMessage(), use);
                    }
                }
            }
        }

        // 2. TLD(s) in JAR files in WEB-INF/lib
        // 3. TLD(s) under WEB-INF
        List<URL> tempURLs = scanModule(webModule);
        for (URL webInfURL : tempURLs) {
            tldURLs.add(webInfURL);
        }

        // 4. All TLD files in all META-INF(s)
        tempURLs.clear();
        try {
            Enumeration<URL> enumURLs = webModule.getEarContext().getBundle().getResources("META-INF");
            if (enumURLs != null) {
                while (enumURLs.hasMoreElements()) {
                    URL enumURL = enumURLs.nextElement();
                    tempURLs = scanDirectory(enumURL);
                    for (URL metaInfURL : tempURLs) {
                        tldURLs.add(metaInfURL);
                    }
                    tempURLs.clear();
                }
            }
        }
        catch (IOException ioe) {
            throw new DeploymentException("Could not locate TLD files located in META-INF(s) " + ioe.getMessage(), ioe);
        }

        log.debug("getTldFiles() Exit: URL[" + tldURLs.size() + "]: " + tldURLs.toString());
        return tldURLs;
    }


    /**
     * scanModule(): Scan the module being deployed for JAR files or TLD files in the WEB-INF
     * directory
     *
     * @param webModule module being deployed
     * @return list of the URL(s) for the TLD files in the module
     * @throws DeploymentException if module cannot be scanned
     */
    private List<URL> scanModule(WebModule webModule) throws DeploymentException {
        log.debug("scanModule( " + webModule.getName() + " ): Entry");

        List<URL> modURLs = new ArrayList<URL>();
        try {
            Enumeration<JarEntry> entries = webModule.getModuleFile().entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                if (jarEntry.getName().startsWith("WEB-INF/") && jarEntry.getName().endsWith(".tld")) {
                    File targetFile = webModule.getEarContext().getTargetFile(createURI(jarEntry.getName()));
                    if (targetFile!=null) {
                        modURLs.add(targetFile.toURL());
                    }
                }
                if (jarEntry.getName().startsWith("WEB-INF/lib/") && jarEntry.getName().endsWith(".jar")) {
                    File targetFile = webModule.getEarContext().getTargetFile(createURI(jarEntry.getName()));
                    List<URL> jarUrls = scanJAR(new JarFile(targetFile), null);
                    for (URL jarURL : jarUrls) {
                        modURLs.add(jarURL);
                    }
                }
            }
        }
        catch (IOException ioe) {
            throw new DeploymentException("Could not scan module for TLD files: " + webModule.getName() + " " + ioe.getMessage(), ioe);
        }
        catch (Exception e) {
            throw new DeploymentException("Could not scan module for TLD files: " + webModule.getName() + " " + e.getMessage(), e);
        }

        log.debug("scanModule() Exit: URL[" + modURLs.size() + "]: " + modURLs.toString());
        return modURLs;
    }


    /**
     * scanJAR(): Scan a JAR files looking for all TLD
     *
     * @param jarFile jar file to scan
     * @param prefix  Optional prefix to limit the search to a specific subdirectory in the JAR file
     * @return list of the URL(s) for the TLD files in the JAR file
     * @throws DeploymentException if jar file cannot be scanned
     */
    private List<URL> scanJAR(JarFile jarFile, String prefix) throws DeploymentException {
        log.debug("scanJAR( " + jarFile.getName() + " ): Entry");

        List<URL> jarURLs = new ArrayList<URL>();
        try {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                URL tempURL = null;
                if (prefix != null) {
                    if (jarEntry.getName().endsWith(".tld") && jarEntry.getName().startsWith(prefix)) {
                        tempURL = new URL("jar:file:" + jarFile.getName() + "!/" + jarEntry.getName());
                    }
                } else {
                    if (jarEntry.getName().endsWith(".tld")) {
                        tempURL = new URL("jar:file:" + jarFile.getName() + "!/" + jarEntry.getName());
                    }
                }
                if (tempURL != null) {
                    jarURLs.add(tempURL);
                }
            }
        }
        catch (MalformedURLException mfe) {
            throw new DeploymentException("Could not scan JAR file for TLD files: " + jarFile.getName() + " " + mfe.getMessage(), mfe);
        }
        catch (Exception e) {
            throw new DeploymentException("Could not scan JAR file for TLD files: " + jarFile.getName() + " " + e.getMessage(), e);
        }

        log.debug("scanJAR() Exit: URL[" + jarURLs.size() + "]: " + jarURLs.toString());
        return jarURLs;
    }


    /**
     * scanDirectory(): Scan a directory for all TLD files
     *
     * @param url URL for the directory to be scanned
     * @return list of the URL(s) for the TLD files in the directory
     * @throws DeploymentException if directory cannot be scanned
     */
    private List<URL> scanDirectory(URL url) throws DeploymentException {
        log.debug("scanDirectory( " + url.toString() + " ): Entry");

        List<URL> dirURLs = new ArrayList<URL>();
        File directory;
        if (url != null) {
            if (url.toString().startsWith("jar:file:")) {
                try {
                    JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
                    URL urlJC = jarConnection.getJarFileURL();
                    URI baseURI = createURI(urlJC.toString());
                    directory = new File(baseURI);
                    if (directory.isDirectory()) {
                        if (directory.canRead()) {
                            JarFile temp = new JarFile(directory);
                            List<URL> tempURLs = scanJAR(temp, "META-INF");
                            for (URL jarURL : tempURLs) {
                                dirURLs.add(jarURL);
                            }
                        } else {
                            log.warn("Cannot read JAR file: " + url.toString());
                        }
                    }
                }
                catch (Exception e) {
                    throw new DeploymentException("Could not scan directory for TLD files: " + url.toString() + " " + e.getMessage(), e);
                }
            } else if (url.toString().startsWith("file:")) {
                try {
                    URI baseURI = createURI(url.toString());
                    directory = new File(baseURI);
                    if (directory.isDirectory() && directory.canRead()) {
                        File[] children = directory.listFiles();
                        for (File child : children) {
                            if (child.getName().endsWith(".tld")) {
                                dirURLs.add(child.toURL());
                            }
                        }
                    } else {
                        log.warn("Cannot read directory: " + url.toString());
                    }
                }
                catch (Exception e) {
                    throw new DeploymentException("Could not scan directory for TLD files: " + url.toString() + " " + e.getMessage(), e);
                }
            } else if (url.toString().startsWith("jar:")) {
                log.warn("URL type not accounted for: " + url.toString());
            }
        }

        log.debug("scanDirectory() Exit: URL[" + dirURLs.size() + "]: " + dirURLs.toString());
        return dirURLs;
    }


    private List<Class> getListenerClasses(WebAppType webApp, WebModule webModule, List<URL> urls, Set<String> listenerNames) throws DeploymentException {
        if (log.isDebugEnabled()) {
            log.debug("getListenerClasses( " + webApp.toString() + "," + '\n' +
                      webModule.getName() + " ): Entry");
        }

        // Get the classloader from the module's EARContext
        Bundle bundle = webModule.getEarContext().getBundle();
        List<Class> classes = new ArrayList<Class>();

        for (URL url : urls) {
            parseTldFile(url, bundle, classes, listenerNames);
        }

        if (log.isDebugEnabled()) {
            log.debug("getListenerClasses() Exit: Classes[" + classes.size() + "]: " + classes.toString());
        }
        return classes;
    }


    private void parseTldFile(URL url, Bundle bundle, List<Class> classes, Set<String> listenerNames) throws DeploymentException {
        log.debug("parseTLDFile( " + url.toString() + " ): Entry");

        try {
            XmlObject xml = XmlBeansUtil.parse(url, null);
            TaglibDocument tld = convertToTaglibSchema(xml);
            TldTaglibType tl = tld.getTaglib();

            // Get all the listeners from the TLD file
            ListenerType[] listeners = tl.getListenerArray();
            for (ListenerType listener : listeners) {
                FullyQualifiedClassType cls = listener.getListenerClass();
                String className = cls.getStringValue().trim();
                listenerNames.add(className);
                try {
                    Class clas = bundle.loadClass(className);
                    classes.add(clas);
                }
                catch (ClassNotFoundException e) {
                    log.warn("JspModuleBuilderExtension: Could not load listener class: " + className + " mentioned in TLD file at " + url.toString());
                }
            }

            // Get all the tags from the TLD file
            TagType[] tags = tl.getTagArray();
            for (TagType tag : tags) {
                FullyQualifiedClassType cls = tag.getTagClass();
                String className = cls.getStringValue().trim();
                try {
                    Class clas = bundle.loadClass(className);
                    classes.add(clas);
                }
                catch (ClassNotFoundException e) {
                    log.warn("JspModuleBuilderExtension: Could not load tag class: " + className + " mentioned in TLD file at " + url.toString());
                }
            }
        }
        catch (XmlException xmle) {
            throw new DeploymentException("Could not parse TLD file at " + url.toString(), xmle);
        }
        catch (IOException ioe) {
            throw new DeploymentException("Could not find TLD file at " + url.toString(), ioe);
        }

        log.debug("parseTLDFile(): Exit");
    }


    /**
     * convertToTaglibSchema(): Convert older TLD files based on the 1.1 and 1.2 DTD or the 2.0 XSD
     * schemas
     *
     * <p><strong>Note(s):</strong>
     * <ul>
     *      <li>Those tags from the 1.1 and 1.2 DTD that are no longer valid (e.g., jsp-version) are
     *      removed
     *      <li>Valid  tags from the 1.1 and 1.2 DTD are converted (e.g., tlibversion to
     *      tlib-version)
     *      <li>The <taglib> root and the <tag> root elements are reordered as necessary (i.e.,
     *      description, display-name)
     *      <li>The <rtexprvalue> tag is inserted in the &lt;attribute> tag if necessary since it was
     *      not required to preceed <type> in 2.0 schema. Default value of false is used.
     * </ul>
     *
     * @param xmlObject possibly old-style tag lib document
     * @return converted TagLibDocument in the new shiny schema
     * @throws XmlException if something goes horribly wrong
     */
    protected static TaglibDocument convertToTaglibSchema(XmlObject xmlObject) throws XmlException {
        if (log.isDebugEnabled()) {
            log.debug("convertToTaglibSchema( " + xmlObject.toString() + " ): Entry");
        }

        XmlCursor cursor = xmlObject.newCursor();
        XmlCursor moveable = xmlObject.newCursor();
        try {
            cursor.toStartDoc();
            cursor.toFirstChild();
            if (SchemaConversionUtils.JAVAEE_NAMESPACE.equals(cursor.getName().getNamespaceURI())) {
                log.debug("Nothing to do");
            }
            else if (SchemaConversionUtils.J2EE_NAMESPACE.equals(cursor.getName().getNamespaceURI())) {
                log.debug("Converting XSD 2.0 to 2.1 schema");
                SchemaConversionUtils.convertSchemaVersion(cursor, SchemaConversionUtils.JAVAEE_NAMESPACE, SCHEMA_LOCATION_URL, VERSION);
                cursor.toStartDoc();
                cursor.toChild(SchemaConversionUtils.JAVAEE_NAMESPACE, "taglib");
                cursor.toFirstChild();
                do {
                    String name = cursor.getName().getLocalPart();
                    if ("tag".equals(name)) {
                        cursor.push();
                        cursor.toFirstChild();
                        SchemaConversionUtils.convertToDescriptionGroup(SchemaConversionUtils.JAVAEE_NAMESPACE, cursor, moveable);
                        do {
                            name = cursor.getName().getLocalPart();
                            boolean rtexprvalueFound = false;
                            boolean typeFound = false;
                            if ("attribute".equals(name)) {
                                cursor.push();
                                cursor.toFirstChild();
                                do {
                                    name = cursor.getName().getLocalPart();
                                    if ("rtexprvalue".equals(name)) {
                                        rtexprvalueFound = true;
                                    }
                                    if ("type".equals(name)) {
                                        typeFound = true;
                                    }
                                } while (cursor.toNextSibling());
                                cursor.pop();
                                if (typeFound && !rtexprvalueFound) {
                                    //--------------------------------------------------------------
                                    // Handle the case where the <type> tag must now be preceded by
                                    // the <rtexprvalue> tag in the 2.1 schema. Cases are:
                                    // 1: Only type found:
                                    //      We are currently positioned directly after the attribute
                                    //      tag (via the pop) so just insert the rtexprvalue tag
                                    //      with the default value. The tags will be properly
                                    //      ordered below.
                                    // 2: Both type and rtexprvalue found:
                                    //      The tags will be properly ordered below with the
                                    //      convertToAttributeGroup() call, so nothing to do
                                    // 3: Only rtexprvalue found:
                                    //      Nothing to do
                                    // 4: Neither found:
                                    //      Nothing to do
                                    //--------------------------------------------------------------
                                    cursor.push();
                                    cursor.toFirstChild();
                                    cursor.insertElementWithText("rtexprvalue", SchemaConversionUtils.JAVAEE_NAMESPACE, "false");
                                    cursor.pop();
                                }
                                cursor.push();
                                cursor.toFirstChild();
                                SchemaConversionUtils.convertToTldAttribute(SchemaConversionUtils.JAVAEE_NAMESPACE, cursor, moveable);
                                cursor.pop();
                            }
                        } while (cursor.toNextSibling());
                        cursor.pop();
                        // Do this conversion last after the other tags have been converted
                        SchemaConversionUtils.convertToTldTag(SchemaConversionUtils.JAVAEE_NAMESPACE, cursor, moveable);
                    }
                } while (cursor.toNextSibling());
            }
            else {
                log.debug("Converting DTD to 2.1 schema");
                SchemaConversionUtils.convertToSchema(cursor, SchemaConversionUtils.JAVAEE_NAMESPACE, SCHEMA_LOCATION_URL, VERSION);
                cursor.toStartDoc();
                cursor.toChild(SchemaConversionUtils.JAVAEE_NAMESPACE, "taglib");
                cursor.toFirstChild();
                SchemaConversionUtils.convertToDescriptionGroup(SchemaConversionUtils.JAVAEE_NAMESPACE, cursor, moveable);
                do {
                    String name = cursor.getName().getLocalPart();
                    if ("jsp-version".equals(name) ||
                        "jspversion".equals(name) ||
                        "info".equals(name)) {
                        cursor.removeXmlContents();
                        cursor.removeXml();
                    }
                    if ("tlibversion".equals(name)) {
                        cursor.setName(TLIB_VERSION);
                    }
                    if ("tlibversion".equals(name)) {
                        cursor.setName(TLIB_VERSION);
                    }
                    if ("shortname".equals(name)) {
                        cursor.setName(SHORT_NAME);
                    }
                    if ("tag".equals(name)) {
                        cursor.push();
                        cursor.toFirstChild();
                        SchemaConversionUtils.convertToDescriptionGroup(SchemaConversionUtils.JAVAEE_NAMESPACE, cursor, moveable);
                        boolean bodyContentFound = false;
                        do {
                            name = cursor.getName().getLocalPart();
                            if ("tagclass".equals(name)) {
                                cursor.setName(TAG_CLASS);
                            }
                            if ("teiclass".equals(name)) {
                                cursor.setName(TEI_CLASS);
                            }
                            if ("bodycontent".equals(name)) {
                                cursor.setName(BODY_CONTENT);
                                bodyContentFound = true;
                            }
                            if ("body-content".equals(name)) {
                                bodyContentFound = true;
                            }
                            if ("attribute".equals(name)) {
                                cursor.push();
                                cursor.toFirstChild();
                                SchemaConversionUtils.convertToTldAttribute(SchemaConversionUtils.JAVAEE_NAMESPACE, cursor, moveable);
                                cursor.pop();
                            }
                            if ("variable".equals(name)) {
                                cursor.push();
                                cursor.toFirstChild();
                                SchemaConversionUtils.convertToTldVariable(SchemaConversionUtils.JAVAEE_NAMESPACE, cursor, moveable);
                                cursor.pop();
                            }
                            if ("info".equals(name)) {
                                cursor.removeXmlContents();
                                cursor.removeXml();
                            }
                        } while (cursor.toNextSibling());
                        cursor.pop();
                        if (!bodyContentFound) {
                            //--------------------------------------------------------------
                            // Handle the case where the <body-content> tag is missing. We
                            // are currently positioned directly after the <tag> attribute
                            // (via the pop) so just insert the <body-content> tag with the
                            // default value. The tags will be properly ordered below.
                            //--------------------------------------------------------------
                            cursor.push();
                            cursor.toFirstChild();
                            cursor.insertElementWithText("body-content", SchemaConversionUtils.JAVAEE_NAMESPACE, "scriptless");
                            cursor.pop();
                        }
                        // Do this conversion last after the other tags have been converted
                        cursor.push();
                        cursor.toFirstChild();
                        SchemaConversionUtils.convertToTldTag(SchemaConversionUtils.JAVAEE_NAMESPACE, cursor, moveable);
                        cursor.pop();
                    }
                    if ("validator".equals(name)) {
                        cursor.push();
                        cursor.toFirstChild();
                        SchemaConversionUtils.convertToTldValidator(SchemaConversionUtils.JAVAEE_NAMESPACE, cursor, moveable);
                        cursor.pop();
                    }
                } while (cursor.toNextSibling());
            }
        }
        finally {
            cursor.dispose();
            moveable.dispose();
        }
        XmlObject result = xmlObject.changeType(TaglibDocument.type);
        if (result != null) {
            try {
                XmlBeansUtil.validateDD(result);
            } catch (XmlException e) {
                log.warn("Invalid transformed taglib", e);
            }
            if (log.isDebugEnabled()) {
                log.debug("convertToTaglibSchema( " + result.toString() + " ): Exit 1");
            }
            return (TaglibDocument) result;
        }
        try {
            XmlBeansUtil.validateDD(xmlObject);
        } catch (XmlException e) {
            log.warn("Invalid transformed taglib", e);
        }
        if (log.isDebugEnabled()) {
            log.debug("convertToTaglibSchema( " + xmlObject.toString() + " ): Exit 2");
        }
        return (TaglibDocument) xmlObject;
    }


    private URI createURI(String path) throws URISyntaxException {
        path = path.replaceAll(" ", "%20");
        return new URI(path);
    }

}
