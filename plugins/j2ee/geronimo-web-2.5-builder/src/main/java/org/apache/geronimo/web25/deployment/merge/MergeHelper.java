/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.web25.deployment.merge;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;

import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebListener;
import javax.servlet.annotation.WebServlet;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.kernel.osgi.BundleAnnotationFinder;
import org.apache.geronimo.kernel.osgi.BundleResourceFinder;
import org.apache.geronimo.kernel.osgi.DiscoveryFilter;
import org.apache.geronimo.kernel.osgi.DiscoveryRange;
import org.apache.geronimo.kernel.osgi.BundleResourceFinder.ResourceFinderCallback;
import org.apache.geronimo.web25.deployment.AbstractWebModuleBuilder;
import org.apache.geronimo.web25.deployment.merge.annotation.AnnotationMergeHandler;
import org.apache.geronimo.web25.deployment.merge.annotation.WebFilterAnnotationMergeHandler;
import org.apache.geronimo.web25.deployment.merge.annotation.WebListenerAnnotationMergeHandler;
import org.apache.geronimo.web25.deployment.merge.annotation.WebServletAnnotationMergeHandler;
import org.apache.geronimo.web25.deployment.merge.webfragment.DataSourceMergeHandler;
import org.apache.geronimo.web25.deployment.merge.webfragment.EjbLocalRefMergeHandler;
import org.apache.geronimo.web25.deployment.merge.webfragment.EjbRefMergeHandler;
import org.apache.geronimo.web25.deployment.merge.webfragment.EnvEntryMergeHandler;
import org.apache.geronimo.web25.deployment.merge.webfragment.ErrorPageMergeHandler;
import org.apache.geronimo.web25.deployment.merge.webfragment.FilterMappingMergeHandler;
import org.apache.geronimo.web25.deployment.merge.webfragment.FilterMergeHandler;
import org.apache.geronimo.web25.deployment.merge.webfragment.JspConfigMergeHandler;
import org.apache.geronimo.web25.deployment.merge.webfragment.ListenerMergeHandler;
import org.apache.geronimo.web25.deployment.merge.webfragment.LocaleEncodingMappingListMergeHandler;
import org.apache.geronimo.web25.deployment.merge.webfragment.LoginConfigMergeHandler;
import org.apache.geronimo.web25.deployment.merge.webfragment.MessageDestinationMergeHandler;
import org.apache.geronimo.web25.deployment.merge.webfragment.MessageDestinationRefMergeHandler;
import org.apache.geronimo.web25.deployment.merge.webfragment.MimeMappingMergeHandler;
import org.apache.geronimo.web25.deployment.merge.webfragment.PersistenceContextRefMergeHandler;
import org.apache.geronimo.web25.deployment.merge.webfragment.PersistenceUnitRefMergeHandler;
import org.apache.geronimo.web25.deployment.merge.webfragment.PostConstructMergeHandler;
import org.apache.geronimo.web25.deployment.merge.webfragment.PreDestroyMergeHandler;
import org.apache.geronimo.web25.deployment.merge.webfragment.ResourceEnvRefMergeHandler;
import org.apache.geronimo.web25.deployment.merge.webfragment.ResourceRefMergeHandler;
import org.apache.geronimo.web25.deployment.merge.webfragment.SecurityConstraintMergeHandler;
import org.apache.geronimo.web25.deployment.merge.webfragment.SecurityRoleMergeHandler;
import org.apache.geronimo.web25.deployment.merge.webfragment.ServiceRefMergeHandler;
import org.apache.geronimo.web25.deployment.merge.webfragment.ServletMappingMergeHandler;
import org.apache.geronimo.web25.deployment.merge.webfragment.ServletMergeHandler;
import org.apache.geronimo.web25.deployment.merge.webfragment.SessionConfigMergeHandler;
import org.apache.geronimo.web25.deployment.merge.webfragment.WebFragmentEntry;
import org.apache.geronimo.web25.deployment.merge.webfragment.WebFragmentMergeHandler;
import org.apache.geronimo.web25.deployment.merge.webfragment.WelcomeFileListMergeHandler;
import org.apache.geronimo.web25.deployment.utils.WebDeploymentMessageUtils;
import org.apache.geronimo.xbeans.javaee6.AbsoluteOrderingType;
import org.apache.geronimo.xbeans.javaee6.JavaIdentifierType;
import org.apache.geronimo.xbeans.javaee6.OrderingOrderingType;
import org.apache.geronimo.xbeans.javaee6.OrderingType;
import org.apache.geronimo.xbeans.javaee6.WebAppType;
import org.apache.geronimo.xbeans.javaee6.WebFragmentDocument;
import org.apache.geronimo.xbeans.javaee6.WebFragmentType;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @version $Rev$ $Date$
 */
public class MergeHelper {

    private static final Logger logger = LoggerFactory.getLogger(MergeHelper.class);

    private static final AnnotationMergeHandler WEB_FILTER_ANNOTATION_MERGE_HANDLER = new WebFilterAnnotationMergeHandler();

    private static final WebFragmentMergeHandler[] WEB_FRAGMENT_MERGE_HANDLERS = { new FilterMergeHandler(), new FilterMappingMergeHandler(), new ListenerMergeHandler(), new ServletMergeHandler(),
            new ServletMappingMergeHandler(), new SessionConfigMergeHandler(), new MimeMappingMergeHandler(), new WelcomeFileListMergeHandler(), new ErrorPageMergeHandler(),
            new JspConfigMergeHandler(), new SecurityConstraintMergeHandler(), new LoginConfigMergeHandler(), new SecurityRoleMergeHandler(), new EnvEntryMergeHandler(), new EjbRefMergeHandler(),
            new EjbLocalRefMergeHandler(), new ResourceRefMergeHandler(), new ResourceEnvRefMergeHandler(), new MessageDestinationRefMergeHandler(), new PersistenceContextRefMergeHandler(),
            new PersistenceUnitRefMergeHandler(), new PostConstructMergeHandler(), new PreDestroyMergeHandler(), new DataSourceMergeHandler(), new ServiceRefMergeHandler(),
            new MessageDestinationMergeHandler(), new LocaleEncodingMappingListMergeHandler() };

    private static final AnnotationMergeHandler WEB_LISTENER_ANNOTATION_MERGE_HANDLER = new WebListenerAnnotationMergeHandler();

    private static final AnnotationMergeHandler WEB_SERVLET_ANNOTATION_MERGE_HANDLER = new WebServletAnnotationMergeHandler();

    /**
     * If absolute-ordering is found in the web.xml file, our steps :
     *   1. Process those web-fragment.xml which are explicitly configured by its sub name elements
     *   2. Depending on whether <others/> is found
     *       2.1 Yes, process those left web-fragment.xml files
     *       2.2 No, Construct the EXCLUDED_JAR_URLS, which is required by many other components,
     *          even the Servlet Container for dynamic ServletRegistration/FilterRegistration
     */
    public static WebFragmentEntry[] absoluteOrderWebFragments(EARContext earContext, Module module, Bundle bundle, WebAppType webApp, Map<String, WebFragmentEntry> webFragmentEntryMap)
            throws DeploymentException {
        AbsoluteOrderingType absoluteOrdering = webApp.getAbsoluteOrderingArray()[0];
        Set<String> expliciteConfiguredWebFragmentNames = new LinkedHashSet<String>();
        List<WebFragmentEntry> orderedWebFragments = new LinkedList<WebFragmentEntry>();
        boolean othersConfigured = absoluteOrdering.getOthersArray().length != 0;
        if (othersConfigured) {
            /*
             * If the <others/> element appears directly within the <absolute-
                    ordering> element, the runtime must ensure that any web-fragments not
                    explicitly named in the <absolute-ordering> section are included at that
                    point in the processing order.
             *  Seems that in xmlbeans, there is no way to know the initial order of the elements
             *  So using native operation of Node to iterator all the sub elements of absolute-ording
             */
            NodeList absoluteOrderingChildren = absoluteOrdering.getDomNode().getChildNodes();
            int iOthersIndex = -1;
            for (int i = 0; i < absoluteOrderingChildren.getLength(); i++) {
                Node node = absoluteOrderingChildren.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    if (node.getNodeName().equals("name")) {
                        String webFragmentName = node.getChildNodes().item(0).getNodeValue();
                        if (webFragmentEntryMap.containsKey(webFragmentName) && !expliciteConfiguredWebFragmentNames.contains(webFragmentName)) {
                            expliciteConfiguredWebFragmentNames.add(webFragmentName);
                            orderedWebFragments.add(webFragmentEntryMap.get(webFragmentName));
                        }
                    } else if (node.getNodeName().equals("others")) {
                        iOthersIndex = expliciteConfiguredWebFragmentNames.size();
                    }
                }
            }
            //Process left named web-fragment.xml files
            for (String webFragmentName : webFragmentEntryMap.keySet()) {
                if (!expliciteConfiguredWebFragmentNames.contains(webFragmentName)) {
                    orderedWebFragments.add(iOthersIndex++, webFragmentEntryMap.get(webFragmentName));
                }
            }
        } else {
            for (JavaIdentifierType javaIdentifier : absoluteOrdering.getNameArray()) {
                String webFragmentName = javaIdentifier.getStringValue();
                // Only process the web-fragment.xml when it is present and it is not processed before
                if (webFragmentEntryMap.containsKey(webFragmentName) && !expliciteConfiguredWebFragmentNames.contains(webFragmentName)) {
                    expliciteConfiguredWebFragmentNames.add(webFragmentName);
                    orderedWebFragments.add(webFragmentEntryMap.get(webFragmentName));
                }
            }
            // EXCLUDED_JAR_URLS is required for TLD scanning, ServletContainerInitializer scanning and ServletContextListeners.
            // So does it mean that we always need to scan web-fragment.xml whatever meta-complete is set with true or false.
            List<String> excludedURLs = new ArrayList<String>();
            earContext.getGeneralData().put(AbstractWebModuleBuilder.EXCLUDED_JAR_URLS, excludedURLs);
            //Add left named web-fragment.xml file URLs to the EXCLUDED_JAR_URLS List
            for (String foundedWebFragementName : webFragmentEntryMap.keySet()) {
                if (!expliciteConfiguredWebFragmentNames.contains(foundedWebFragementName)) {
                    excludedURLs.add(webFragmentEntryMap.get(foundedWebFragementName).getJarURL());
                }
            }
        }
        return orderedWebFragments.toArray(new WebFragmentEntry[0]);
    }

    @SuppressWarnings("unchecked")
    public static void mergeAnnotations(Bundle bundle, WebAppType webApp, MergeContext mergeContext, final String prefix) throws DeploymentException {
        final boolean isJarFile = prefix.endsWith(".jar");
        try {
            BundleAnnotationFinder bundleAnnotationFinder = new BundleAnnotationFinder(null, bundle, new DiscoveryFilter() {

                @Override
                public boolean directoryDiscoveryRequired(String url) {
                    return !isJarFile;
                }

                @Override
                public boolean rangeDiscoveryRequired(DiscoveryRange discoveryRange) {
                    return discoveryRange.equals(DiscoveryRange.BUNDLE_CLASSPATH);
                }

                @Override
                public boolean zipFileDiscoveryRequired(String url) {
                    return isJarFile ? url.equals(prefix) : false;
                }
            });
            List<Class> webServlets = bundleAnnotationFinder.findAnnotatedClasses(WebServlet.class);
            WEB_SERVLET_ANNOTATION_MERGE_HANDLER.merge(webServlets.toArray(new Class<?>[0]), webApp, mergeContext);
            List<Class> webFilters = bundleAnnotationFinder.findAnnotatedClasses(WebFilter.class);
            WEB_FILTER_ANNOTATION_MERGE_HANDLER.merge(webFilters.toArray(new Class<?>[0]), webApp, mergeContext);
            List<Class> webListeners = bundleAnnotationFinder.findAnnotatedClasses(WebListener.class);
            WEB_LISTENER_ANNOTATION_MERGE_HANDLER.merge(webListeners.toArray(new Class<?>[0]), webApp, mergeContext);
        } catch (Exception e) {
            throw new DeploymentException("Fail to merge annotations in " + prefix, e);
        }
    }

    public static boolean mergeRequired(String attributeName, String parentElementName, String elementName, String value, MergeContext mergeContext) throws DeploymentException {
        MergeItem mergeItem = (MergeItem) mergeContext.getAttribute(attributeName);
        if (mergeItem == null) {
            mergeContext.setAttribute(attributeName, new MergeItem(value, mergeContext.getCurrentJarUrl(), ElementSource.WEB_FRAGMENT));
            return true;
        }
        if (mergeItem.isFromWebFragment() && !mergeItem.getValue().equals(value)) {
            throw new DeploymentException(WebDeploymentMessageUtils.createDuplicateValueMessage(parentElementName, elementName, (String) mergeItem.getValue(), mergeItem.getBelongedURL(), value,
                    mergeContext.getCurrentJarUrl()));
        }
        return false;
    }

    public static boolean mergeRequired(String attributeName, String parentElementName, String KeyElementName, String keyName, String valueElementName, String value, MergeContext mergeContext)
            throws DeploymentException {
        if (value == null) {
            return false;
        }
        MergeItem mergeItem = (MergeItem) mergeContext.getAttribute(attributeName);
        if (mergeItem == null) {
            mergeContext.setAttribute(attributeName, new MergeItem(value, mergeContext.getCurrentJarUrl(), ElementSource.WEB_FRAGMENT));
            return true;
        }
        if (mergeItem.isFromWebFragment() && !mergeItem.getValue().equals(value)) {
            throw new DeploymentException(WebDeploymentMessageUtils.createDuplicateKeyValueMessage(parentElementName, KeyElementName, keyName, valueElementName, (String) mergeItem.getValue(),
                    mergeItem.getBelongedURL(), value, mergeContext.getCurrentJarUrl()));
        }
        return false;
    }

    public static void processServletContainerInitializer(EARContext earContext, Module module, Bundle bundle) throws DeploymentException {
        //ServletContainerInitializer
        //TODO A possible solution might be create ServletContainerInitializer GBean for each found ServletContainerInitializer,
        //The GBean would contain all the matched classes according the configuration of HandlesTypes annotation
        //then check the specific API of Tomcat/Jetty, which could be used to regiester them to the servlet container
        //That is for web application scope, how about the server scope ?
        ServiceReference reference = bundle.getBundleContext().getServiceReference(PackageAdmin.class.getName());
        try {
            PackageAdmin packageAdmin = (PackageAdmin) bundle.getBundleContext().getService(reference);
            BundleResourceFinder resourceFinder = new BundleResourceFinder(packageAdmin, bundle, "META-INF/services", "javax.servlet.ServletContainerInitializer");
            resourceFinder.find(new ResourceFinderCallback() {

                public void foundInDirectory(Bundle bundle, String basePath, URL url) throws Exception {
                    //ignore
                }

                public void foundInJar(Bundle bundle, String jarName, ZipEntry entry, InputStream in) throws Exception {
                }
            });
        } catch (Exception e) {
            throw new DeploymentException("Fail to scan javax.servlet.ServletContainerInitializer", e);
        } finally {
            bundle.getBundleContext().ungetService(reference);
        }
    }

    public static void processWebFragmentsAndAnnotations(EARContext earContext, Module module, Bundle bundle, WebAppType webApp) throws DeploymentException {
        BundleResourceFinder bundleResourceFinder = new BundleResourceFinder(null, bundle, "META-INF/", "web-fragment.xml");
        final Map<String, WebFragmentDocument> jarUrlWebFragmentDocumentMap = new LinkedHashMap<String, WebFragmentDocument>();
        final String validJarNamePrefix = module.isStandAlone() ? "WEB-INF" : module.getName() + "/WEB-INF";
        try {
            bundleResourceFinder.find(new ResourceFinderCallback() {

                public void foundInDirectory(Bundle bundle, String basePath, URL url) throws Exception {
                }

                public void foundInJar(Bundle bundle, String jarName, ZipEntry entry, InputStream in) throws Exception {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Found web-fragment.xml in jarName = [" + jarName + "] jarURL = [" + bundle.getEntry(jarName) + "]");
                    }
                    if (jarName.startsWith(validJarNamePrefix) && jarName.endsWith(".jar")) {
                        WebFragmentDocument webFragmentDocument = (WebFragmentDocument) XmlBeansUtil.parse(in);
                        //Hopefully, XmlBeansUtil should help to check most of errors against the schema files, like none null servlet-name etc.
                        XmlBeansUtil.validateDD(webFragmentDocument);
                        jarUrlWebFragmentDocumentMap.put(jarName, webFragmentDocument);
                    }
                }
            });
        } catch (Exception e) {
            logger.error("Fail to scan web-fragment.xml files", e);
            throw new DeploymentException("Fail to scan web-fragment.xml files", e);
        }
        WebFragmentEntry[] webFragmentEntries = sortWebFragments(earContext, module, bundle, webApp, jarUrlWebFragmentDocumentMap);
        MergeContext mergeContext = new MergeContext();
        mergeContext.setEarContext(earContext);
        mergeContext.setBundle(bundle);
        //Pre-process for annotations
        WEB_SERVLET_ANNOTATION_MERGE_HANDLER.preProcessWebXmlElement(webApp, mergeContext);
        WEB_FILTER_ANNOTATION_MERGE_HANDLER.preProcessWebXmlElement(webApp, mergeContext);
        WEB_LISTENER_ANNOTATION_MERGE_HANDLER.preProcessWebXmlElement(webApp, mergeContext);
        //Pre-process each web fragment
        for (WebFragmentMergeHandler<WebFragmentType, WebAppType> webFragmentMergeHandler : WEB_FRAGMENT_MERGE_HANDLERS) {
            webFragmentMergeHandler.preProcessWebXmlElement(webApp, mergeContext);
        }
        //Merge the web fragment and annotations to web.xml
        for (WebFragmentEntry webFragmentEntry : webFragmentEntries) {
            mergeContext.setWebFragmentEntry(webFragmentEntry);
            WebFragmentType webFragment = webFragmentEntry.getWebFragment();
            for (WebFragmentMergeHandler<WebFragmentType, WebAppType> webFragmentMergeHandler : WEB_FRAGMENT_MERGE_HANDLERS) {
                webFragmentMergeHandler.merge(webFragment, webApp, mergeContext);
            }
            if (!webFragment.getMetadataComplete()) {
                mergeAnnotations(bundle, webApp, mergeContext, webFragmentEntry.getJarURL());
            }
        }
        mergeContext.setWebFragmentEntry(null);
        for (WebFragmentMergeHandler<WebFragmentType, WebAppType> webFragmentMergeHandler : WEB_FRAGMENT_MERGE_HANDLERS) {
            webFragmentMergeHandler.postProcessWebXmlElement(webApp, mergeContext);
        }
        //Post-process for annotations
        WEB_SERVLET_ANNOTATION_MERGE_HANDLER.postProcessWebXmlElement(webApp, mergeContext);
        WEB_FILTER_ANNOTATION_MERGE_HANDLER.postProcessWebXmlElement(webApp, mergeContext);
        WEB_LISTENER_ANNOTATION_MERGE_HANDLER.postProcessWebXmlElement(webApp, mergeContext);
        //Merge the annotations found in WEB-INF/classes folder
        mergeAnnotations(bundle, webApp, mergeContext, "/WEB-INF/classes");
        mergeContext.clearup();
    }

    public static WebFragmentEntry[] relativeOrderWebFragments(EARContext earContext, Module module, Bundle bundle, WebAppType webApp, Map<String, WebFragmentEntry> webFragmentEntryMap)
            throws DeploymentException {
        Map<String, WebFragmentOrderEntry> webFragmentOrderEntryMap = new LinkedHashMap<String, WebFragmentOrderEntry>();
        //Step 1 : Create WebFragmentOrderEntry for sorting web fragments easily
        for (String webFragmentName : webFragmentEntryMap.keySet()) {
            webFragmentOrderEntryMap.put(webFragmentName, WebFragmentOrderEntry.create(webFragmentEntryMap.get(webFragmentName)));
        }
        //Step 2 : Initialize the list by before/after others configurations, also, convert the before configurations to corresponding after configurations
        //TODO Is the reference like A before others and B before A allowed ?
        LinkedList<WebFragmentOrderEntry> webFragmentOrderEntryList = new LinkedList<WebFragmentOrderEntry>();
        for (WebFragmentOrderEntry webFragmentOrderEntry : webFragmentOrderEntryMap.values()) {
            for (String beforeEntryName : webFragmentOrderEntry.beforeEntryNames) {
                webFragmentOrderEntryMap.get(beforeEntryName).afterEntryNames.add(webFragmentOrderEntry.name);
            }
            if (webFragmentOrderEntry.afterDefined && webFragmentOrderEntry.afterOthers) {
                webFragmentOrderEntryList.addLast(webFragmentOrderEntry);
            } else {
                webFragmentOrderEntryList.addFirst(webFragmentOrderEntry);
            }
        }
        //Step 3: Detect Circus references
        // a. A -> A
        // b. A -> B -> A
        // c. A -> B ->  C -> A
        for (WebFragmentOrderEntry webFragmentOrderEntry : webFragmentOrderEntryList) {
            detectCircusAfterDependency(webFragmentOrderEntry, webFragmentOrderEntry, webFragmentOrderEntryMap, new HashSet<String>());
        }
        //Step 4: Sort the webFragment depending on the after configurations
        //TODO The Sort algorithm might need to improve.
        for (WebFragmentOrderEntry webFragmentOrderEntry : webFragmentOrderEntryMap.values()) {
            for (String afterEntryName : webFragmentOrderEntry.afterEntryNames) {
                swap(webFragmentOrderEntry.name, afterEntryName, webFragmentOrderEntryList);
            }
        }
        WebFragmentEntry[] webFragmentTypes = new WebFragmentEntry[webFragmentOrderEntryList.size()];
        int iIndex = 0;
        for (WebFragmentOrderEntry webFragmentOrderEntry : webFragmentOrderEntryList) {
            webFragmentTypes[iIndex++] = webFragmentOrderEntry.webFragmentEntry;
        }
        return webFragmentTypes;
    }

    public static WebFragmentEntry[] sortWebFragments(EARContext earContext, Module module, Bundle bundle, WebAppType webApp, Map<String, WebFragmentDocument> jarURLDocumentMap)
            throws DeploymentException {
        Map<String, WebFragmentEntry> webFragmentEntryMap = new HashMap<String, WebFragmentEntry>(jarURLDocumentMap.size());
        boolean absoluteOrderingConfigured = webApp.getAbsoluteOrderingArray().length != 0;
        Set<String> usedWebFragmentNames = new HashSet<String>();
        Map<String, WebFragmentType> unnamedWebFragmentMap = new HashMap<String, WebFragmentType>();
        for (String jarURL : jarURLDocumentMap.keySet()) {
            WebFragmentType webFragment = jarURLDocumentMap.get(jarURL).getWebFragment();
            JavaIdentifierType[] names = webFragment.getNameArray();
            String webFragmentName = names.length == 0 ? null : names[0].getStringValue();
            if (webFragmentName != null) {
                if (webFragmentEntryMap.containsKey(webFragmentName)) {
                    //TODO Please correct my understanding about how to handle the duplicate web-fragment name (spec 8.2.2)
                    //If absolute-ordering is configured, the web-fragments of the same name are allowed, but only the first occurrence is considered
                    //If relative-ordering is configured, Duplicate name exception: if, when traversing the web-fragments, multiple
                    //members with the same <name> element are encountered, the application must
                    //log an informative error message including information to help fix the
                    //problem, and must fail to deploy. For example, one way to fix this problem is
                    //for the user to use absolute ordering, in which case relative ordering is ignored.
                    //TODO If there is no configuration for absolute-ordering in web.xml and relative-ordering in webfragment.xml files, shall we allowed duplicate names ?
                    if (!absoluteOrderingConfigured) {
                        throw new DeploymentException("Mutiple web-fragments with the same name [" + webFragmentName + "] are found in " + jarURL + " and "
                                + webFragmentEntryMap.get(webFragmentName).getJarURL() + ", you might make the name unique or use absolute ordering in web.xml");
                    }
                } else {
                    webFragmentEntryMap.put(webFragmentName, new WebFragmentEntry(webFragmentName, webFragmentName, webFragment, jarURL));
                }
                usedWebFragmentNames.add(webFragmentName);
            } else {
                unnamedWebFragmentMap.put(jarURL, webFragment);
            }
            //Add names configurations in before/after, so that we would not add an existing name for those unamed web fragment by sudden.
            if (webFragment.getOrderingArray().length > 0) {
                OrderingType order = webFragment.getOrderingArray()[0];
                if (order.getBefore() != null) {
                    for (JavaIdentifierType name : order.getBefore().getNameArray()) {
                        usedWebFragmentNames.add(name.getStringValue());
                    }
                }
                if (order.getAfter() != null) {
                    for (JavaIdentifierType name : order.getAfter().getNameArray()) {
                        usedWebFragmentNames.add(name.getStringValue());
                    }
                }
            }
        }
        //Generate names for all the web fragments which are not explicitly configured
        String tempNamePrefix = "geronimo-deployment";
        int nameSubfix = 0;
        for (String webFragmentURL : unnamedWebFragmentMap.keySet()) {
            WebFragmentType webFragment = unnamedWebFragmentMap.get(webFragmentURL);
            String tempWebFragmentName = tempNamePrefix + nameSubfix++;
            while (usedWebFragmentNames.contains(tempWebFragmentName)) {
                tempWebFragmentName = tempNamePrefix + nameSubfix++;
            }
            webFragmentEntryMap.put(tempWebFragmentName, new WebFragmentEntry(tempNamePrefix, null, webFragment, webFragmentURL));
        }
        //Order the web fragments required (<name> element is specified)
        if (absoluteOrderingConfigured) {
            return absoluteOrderWebFragments(earContext, module, bundle, webApp, webFragmentEntryMap);
        } else {
            return relativeOrderWebFragments(earContext, module, bundle, webApp, webFragmentEntryMap);
        }
    }

    private static void detectCircusAfterDependency(WebFragmentOrderEntry rootWebFragmentOrderEntry, WebFragmentOrderEntry dependentWebFragmentOrderEntry,
            Map<String, WebFragmentOrderEntry> webFragmentOrderEntryMap, Set<String> visitedWebFragmentNames) throws DeploymentException {
        if (dependentWebFragmentOrderEntry.afterEntryNames.contains(rootWebFragmentOrderEntry.name)) {
            throw new DeploymentException("Circus references are founded for " + rootWebFragmentOrderEntry.webFragmentEntry.getWebFragmentName() + " in the jar "
                    + rootWebFragmentOrderEntry.webFragmentEntry.getJarURL() + ", it must be corrected or change to use absolute-ordering in web.xml file");
        }
        for (String subDependentWebFragmentName : dependentWebFragmentOrderEntry.afterEntryNames) {
            if (visitedWebFragmentNames.contains(subDependentWebFragmentName)) {
                continue;
            }
            visitedWebFragmentNames.add(subDependentWebFragmentName);
            detectCircusAfterDependency(rootWebFragmentOrderEntry, webFragmentOrderEntryMap.get(subDependentWebFragmentName), webFragmentOrderEntryMap, visitedWebFragmentNames);
        }
    }

    private static void swap(String shouldAfter, String shouldBefore, List<WebFragmentOrderEntry> webFragmentOrderEntries) {
        int iShouldAfterIndex = -1;
        int iShouldBeforeIndex = -1;
        int iIndex = 0;
        for (WebFragmentOrderEntry webFragmentOrderEntry : webFragmentOrderEntries) {
            if (iShouldAfterIndex == -1 || iShouldBeforeIndex == -1) {
                String currentWebFragmentName = webFragmentOrderEntry.name;
                if (shouldAfter.equals(currentWebFragmentName)) {
                    iShouldAfterIndex = iIndex;
                } else if (shouldBefore.equals(currentWebFragmentName)) {
                    iShouldBeforeIndex = iIndex;
                }
                iIndex++;
            } else {
                break;
            }
        }
        if (iShouldAfterIndex < iShouldBeforeIndex) {
            WebFragmentOrderEntry webFragmentOrderEntry = webFragmentOrderEntries.remove(iShouldAfterIndex);
            webFragmentOrderEntries.add(iShouldBeforeIndex + 1, webFragmentOrderEntry);
        }
    }

    private static class WebFragmentOrderEntry {

        public boolean afterDefined;

        public Set<String> afterEntryNames = new HashSet<String>();

        public boolean afterOthers;

        public boolean beforeDefined;

        public Set<String> beforeEntryNames = new HashSet<String>();

        public boolean beforeOthers;

        //Duplicate name variable is just for easily access
        public String name;

        public WebFragmentEntry webFragmentEntry;

        public static WebFragmentOrderEntry create(WebFragmentEntry webFragmentEntry) throws DeploymentException {
            WebFragmentOrderEntry webFragmentOrderEntry = new WebFragmentOrderEntry();
            WebFragmentType webFragment = webFragmentEntry.getWebFragment();
            if (webFragment.getOrderingArray().length > 0) {
                OrderingType ordering = webFragment.getOrderingArray()[0];
                OrderingOrderingType after = ordering.getAfter();
                if (after == null) {
                    webFragmentOrderEntry.afterDefined = false;
                } else {
                    webFragmentOrderEntry.afterDefined = true;
                    webFragmentOrderEntry.afterOthers = (after.getOthers() != null);
                    for (JavaIdentifierType afterEntryName : after.getNameArray()) {
                        if (afterEntryName.getStringValue().length() > 0) {
                            webFragmentOrderEntry.afterEntryNames.add(afterEntryName.getStringValue());
                        }
                    }
                }
                OrderingOrderingType before = ordering.getBefore();
                if (before == null) {
                    webFragmentOrderEntry.beforeDefined = false;
                } else {
                    webFragmentOrderEntry.beforeDefined = true;
                    webFragmentOrderEntry.beforeOthers = (before.getOthers() != null);
                    for (JavaIdentifierType beforeEntryName : before.getNameArray()) {
                        if (beforeEntryName.getStringValue().length() > 0) {
                            webFragmentOrderEntry.beforeEntryNames.add(beforeEntryName.getStringValue());
                        }
                    }
                }
            }
            if (webFragmentOrderEntry.beforeOthers && webFragmentOrderEntry.afterOthers) {
                throw new DeploymentException("It is not allowed to define before and after others at the same time in jar " + webFragmentEntry.getJarURL());
            }
            webFragmentOrderEntry.webFragmentEntry = webFragmentEntry;
            webFragmentOrderEntry.name = webFragmentEntry.getName();
            return webFragmentOrderEntry;
        }
    }
}
