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

package org.apache.geronimo.console.bundlemanager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.portlet.PortletFileUpload;
import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.kernel.config.ConfigurationInfo;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.xbean.osgi.bundle.util.BundleDescription;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.service.startlevel.StartLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BundleManagerPortlet extends BasePortlet {

    private static final Logger logger = LoggerFactory.getLogger(BundleManagerPortlet.class);
    
    private static final String SERACH_ACTION = "search";
    
    private static final String LIST_ACTION = "list";
    
    private static final String INSTALL_ACTION = "install";
    
    private static final String BUNDLE_ACTION = "bundle";

    private static final String START_OPERATION = "start";

    private static final String STOP_OPERATION = "stop";

    private static final String UPDATE_OPERATION = "update";
    
    private static final String UNINSTALL_OPERATION = "uninstall";

    private PortletRequestDispatcher helpView;
    
    private PortletRequestDispatcher bundleManagerView;

    private PortletRequestDispatcher showManifestView;
    
    private PortletRequestDispatcher showWiredBundlesView;

    //private String moduleType;

    protected void doHelp(RenderRequest renderRequest, RenderResponse renderResponse) throws PortletException, IOException {
        helpView.include(renderRequest, renderResponse);
    }

    public void init(PortletConfig portletConfig) throws PortletException {
        super.init(portletConfig);
        helpView = portletConfig.getPortletContext().getRequestDispatcher("/WEB-INF/view/bundlemanager/BundleManager.jsp");
        bundleManagerView = portletConfig.getPortletContext().getRequestDispatcher("/WEB-INF/view/bundlemanager/BundleManager.jsp");
        showManifestView = portletConfig.getPortletContext().getRequestDispatcher("/WEB-INF/view/bundlemanager/ShowManifest.jsp");
        showWiredBundlesView = portletConfig.getPortletContext().getRequestDispatcher("/WEB-INF/view/bundlemanager/ShowWiredBundles.jsp");

    }

    public void destroy() {
        bundleManagerView = null;
        super.destroy();
    }
    
    public void processAction(ActionRequest actionRequest, ActionResponse actionResponse) throws PortletException, IOException {
        
        actionResponse.setRenderParameter("message", ""); // set to blank first
        
        String page = actionRequest.getParameter("page");
        
        if ("view_manifest".equals(page)){
            // no actions in this page
            actionResponse.setRenderParameter("page", "view_manifest");
            
        }else if ("view_wired_bundles".equals(page)){
            String id = actionRequest.getParameter("bundleId");;
            String perspectiveType = actionRequest.getParameter("perspectiveType");
            //set render params
            actionResponse.setRenderParameter("page", "view_wired_bundles");
            actionResponse.setRenderParameter("bundleId", id);
            actionResponse.setRenderParameter("perspectiveTypeValue", perspectiveType);
            
        }else { //main page
            
            //we use session to control the listType and searchString for filtering list so that
            //user can easily turn back to the page that he just jumped out.
            Object sessionListType = actionRequest.getPortletSession().getAttribute("listTypeValue");
            if (sessionListType == null || "".equals((String)sessionListType)){
                //default value
                actionRequest.getPortletSession().setAttribute("listTypeValue","all");
            }
            String listType = (String)actionRequest.getPortletSession().getAttribute("listTypeValue");
            
            Object sessionSearchString = actionRequest.getPortletSession().getAttribute("searchStringValue");
            if (sessionSearchString == null){
                //default value
                actionRequest.getPortletSession().setAttribute("searchStringValue","");
            }
            String searchString = (String)actionRequest.getPortletSession().getAttribute("searchStringValue");;
            
            
            // process action
            String action = actionRequest.getParameter("action");
            if (INSTALL_ACTION.equals(action)){
                BundleContext bundleContext = getBundleContext(actionRequest);
                
                //get the StartLeval object
                ServiceReference startLevelRef = bundleContext.getServiceReference(StartLevel.class.getCanonicalName());
                StartLevel startLevelService = (StartLevel) bundleContext.getService(startLevelRef);
                
                processInstallAction(actionRequest, bundleContext,startLevelService);
                
                listType = "all";
                searchString = "";
                
            }else if (SERACH_ACTION.equals(action)){
                searchString = actionRequest.getParameter("searchString");
                
            }else if (LIST_ACTION.equals(action)){
                listType = actionRequest.getParameter("listType");
                searchString = "";
                
            }else if (BUNDLE_ACTION.equals(action)){ //start/stop/restart/update/uninstall actions
                
                String id = actionRequest.getParameter("bundleId");;
                BundleContext bundleContext = getBundleContext(actionRequest);

                String operation = actionRequest.getParameter("operation");
                
                try {
                    Bundle bundle = bundleContext.getBundle(Long.parseLong(id));

                    if (START_OPERATION.equals(operation)) {
                        bundle.start();
                        addInfoMessage(actionRequest, "bundle started");
                    } else if (STOP_OPERATION.equals(operation)) {
                        bundle.stop();
                        addInfoMessage(actionRequest, getLocalizedString(actionRequest, "consolebase.infoMsg02"));
                    } else if (UNINSTALL_OPERATION.equals(operation)) {
                        bundle.uninstall();
                        addInfoMessage(actionRequest, getLocalizedString(actionRequest, "consolebase.infoMsg04") + "<br />" + BundleUtil.getSymbolicName(bundle));
                    } else if (UPDATE_OPERATION.equals(operation)) {
                        bundle.update();
                        addInfoMessage(actionRequest, getLocalizedString(actionRequest, "consolebase.infoMsg19"));
                    } else {
                        addWarningMessage(actionRequest, getLocalizedString(actionRequest, "consolebase.warnMsg01") + action + "<br />");
                        throw new PortletException("Invalid value for changeState: " + action);
                    }            
                } catch (Throwable e) {
                    addErrorMessage(actionRequest, getLocalizedString(actionRequest, "consolebase.errorMsg03"), e.getMessage());
                    logger.error("Exception", e);
                }
            }
            
            // set listType and searchString in session
            actionRequest.getPortletSession().setAttribute("listTypeValue", listType);
            actionRequest.getPortletSession().setAttribute("searchStringValue", searchString);
            
            // set the values, which come from session, to render parameter
            actionResponse.setRenderParameter("listTypeValue", listType);
            actionResponse.setRenderParameter("searchStringValue", searchString);
        }
            
    }


    
    protected void doView(RenderRequest renderRequest, RenderResponse renderResponse) throws IOException, PortletException {
        
        
        if (WindowState.MINIMIZED.equals(renderRequest.getWindowState())) { // minimal view
            return;
            
        } else { // normal and maximal view
            
            String page = renderRequest.getParameter("page");
            
            if ("view_manifest".equals(page)){
                BundleContext bundleContext = getBundleContext(renderRequest);
                
                long id = Long.valueOf(renderRequest.getParameter("bundleId"));
                Bundle bundle = bundleContext.getBundle(id);
                
                List<ManifestHeader> manifestHeaders = new ArrayList<ManifestHeader>();
                Dictionary<String,String> headers = bundle.getHeaders();
                Enumeration<String> keys = headers.keys();
                while(keys.hasMoreElements()){
                    String key = (String)keys.nextElement();
                    if (key.equals("Import-Package")||key.equals("Export-Package")||key.equals("Ignore-Package")||key.equals("Private-Package")||key.equals("Export-Service")){
                        manifestHeaders.add(new ManifestHeader(key, ManifestHeader.formatPackageHeader((String)headers.get(key))));
                    } else {
                        manifestHeaders.add(new ManifestHeader(key, (String)headers.get(key)));
                    }
                }
                
                SimpleBundleInfo bundleInfo = new SimpleBundleInfo(bundle);
                
                Collections.sort(manifestHeaders);
                renderRequest.setAttribute("manifestHeaders", manifestHeaders);
                renderRequest.setAttribute("bundleInfo", bundleInfo);
                showManifestView.include(renderRequest, renderResponse);
                
            }else if("view_wired_bundles".equals(page)) {
                
                BundleContext bundleContext = getBundleContext(renderRequest);
                
                long id = Long.valueOf(renderRequest.getParameter("bundleId"));
                Bundle bundle = bundleContext.getBundle(id);
                
                String perspectiveType = renderRequest.getParameter("perspectiveTypeValue");
                if (perspectiveType == null || perspectiveType == "") perspectiveType = "package"; //when we access this page with a renderURL, we need the default value
                
                ServiceReference reference = bundleContext.getServiceReference(PackageAdmin.class.getName());
                PackageAdmin packageAdmin = (PackageAdmin) bundle.getBundleContext().getService(reference);
                

                Set<PackageBundlePair> importingPairs = getImportingPairs(packageAdmin, bundle);
                Set<PackageBundlePair> exportingPairs = getExportingPairs(packageAdmin, bundle);
                
                if ("package".equals(perspectiveType)){
                    List<PackagePerspective> importingPackagePerspectives = getPackagePerspectives(importingPairs);
                    List<PackagePerspective> exportingPackagePerspectives = getPackagePerspectives(exportingPairs);
                    
                    Collections.sort(importingPackagePerspectives);
                    Collections.sort(exportingPackagePerspectives);
                    
                    renderRequest.setAttribute("importingPackagePerspectives", importingPackagePerspectives);
                    renderRequest.setAttribute("exportingPackagePerspectives", exportingPackagePerspectives);
                }else{  //"bundle".equals(perspectiveType)){
                    List<BundlePerspective> importingBundlePerspectives = getBundlePerspectives(importingPairs);
                    List<BundlePerspective> exportingBundlePerspectives = getBundlePerspectives(exportingPairs);
                    
                    Collections.sort(importingBundlePerspectives);
                    Collections.sort(exportingBundlePerspectives);
                    
                    renderRequest.setAttribute("importingBundlePerspectives", importingBundlePerspectives);
                    renderRequest.setAttribute("exportingBundlePerspectives", exportingBundlePerspectives);
                }
                                
                SimpleBundleInfo bundleInfo = new SimpleBundleInfo(bundle);
                
                renderRequest.setAttribute("bundleInfo", bundleInfo);
                renderRequest.setAttribute("perspectiveTypeValue", perspectiveType);
                
                showWiredBundlesView.include(renderRequest, renderResponse);
                
            }else{  // main page

                String listType = renderRequest.getParameter("listTypeValue");
                if (listType == null || listType == "") listType = "all"; //when we access this page with a renderURL, we need the default value
                String searchString = renderRequest.getParameter("searchStringValue");
                if (searchString == null) searchString = ""; //when we access this page with a renderURL, we need the default value
                
                BundleContext bundleContext = getBundleContext(renderRequest);
                
                // retrieve bundle infos
                List<ExtendedBundleInfo> bundleInfos = new ArrayList<ExtendedBundleInfo>();
                
                // get the StartLeval object
                ServiceReference startLevelRef = bundleContext.getServiceReference(StartLevel.class.getCanonicalName());
                StartLevel startLevelService = (StartLevel) bundleContext.getService(startLevelRef);
                
                // get configured bundle Ids
                Set<Long> configedBundleIds = getConfigedBundleIds();
                
                Bundle[] bundles = bundleContext.getBundles();
                for (Bundle bundle : bundles) {
                    
                    if (searchString != "" && !matchBundle(bundle, searchString)){
                        continue;
                    }
                    
                    // construct the result bundleInfos by listType
                    if ("wab".equals(listType)){
                        if (checkWABBundle(bundle)){
                            ExtendedBundleInfo info = createExtendedBundleInfo(bundle, startLevelService, configedBundleIds);
                            bundleInfos.add(info);
                        }
                    }else if ("blueprint".equals(listType)){
                        if (checkBlueprintBundle(bundle)){
                            ExtendedBundleInfo info = createExtendedBundleInfo(bundle, startLevelService, configedBundleIds);
                            bundleInfos.add(info);
                        }
                    }else if ("system".equals(listType)){
                        if (checkSysBundle(bundle,startLevelService)){
                            ExtendedBundleInfo info = createExtendedBundleInfo(bundle, startLevelService, configedBundleIds);
                            bundleInfos.add(info);
                        }
                    }else if ("configuration".equals(listType)){
                        if (checkConfigedBundle(bundle,configedBundleIds)){
                            ExtendedBundleInfo info = createExtendedBundleInfo(bundle, startLevelService, configedBundleIds);
                            bundleInfos.add(info);
                        }
                    }else{
                        ExtendedBundleInfo info = createExtendedBundleInfo(bundle, startLevelService, configedBundleIds);
                        bundleInfos.add(info);
                    }
                }
                
                Collections.sort(bundleInfos, new BundleIdDescComparator());
                renderRequest.setAttribute("extendedBundleInfos", bundleInfos);
                
                // set the values to render attribute
                renderRequest.setAttribute("listTypeValue", listType);
                renderRequest.setAttribute("searchStringValue", searchString);
                
                renderRequest.setAttribute("initStartLevel", startLevelService.getInitialBundleStartLevel());
                
                if (bundleInfos.size() == 0) {
                    addWarningMessage(renderRequest, getLocalizedString(renderRequest, "consolebase.warnMsg02"));
                }
                
                bundleManagerView.include(renderRequest, renderResponse);
                
            }
        }
    }
    
    private ExtendedBundleInfo createExtendedBundleInfo(Bundle bundle, StartLevel startLevelService,Set<Long> configedBundleIds){
        ExtendedBundleInfo info = new ExtendedBundleInfo(bundle);
        
        String contextPath = getContextPath(bundle);
        if (contextPath != null && contextPath!="") {
            info.addContextPath(contextPath);
            info.addType(BundleType.WAB);
        }
        
        if (checkBlueprintBundle(bundle)) {
            info.addType(BundleType.BLUEPRINT);
        }
        
        if (checkSysBundle(bundle, startLevelService)) {
            info.addType(BundleType.SYSTEM);
        }
        
        if (checkConfigedBundle(bundle,configedBundleIds)){
            info.addType(BundleType.CONFIGURATION);
        }
        
        return info;
    }
    
    private BundleContext getBundleContext(PortletRequest request) {
        return (BundleContext) request.getPortletSession().getPortletContext().getAttribute("osgi-bundlecontext");
    }
    
    private static boolean matchBundle(Bundle bundle, String searchString){
        if (bundle.getSymbolicName() == null || (bundle.getSymbolicName() != null && bundle.getSymbolicName().indexOf(searchString) == -1)){
            return false;
        }
        return true;
    }
    
    private static String getContextPath(Bundle bundle) {
        Object path = bundle.getHeaders().get(BundleUtil.WEB_CONTEXT_PATH_HEADER);
        if (path!=null) {
            return (String)path;
        }
        return null;
    }
    
    private static boolean checkWABBundle(Bundle bundle){
        String contextPath = getContextPath(bundle);
        if (contextPath != null && contextPath!="") {
            return true;
        }
        return false;
    }
    
    private static boolean checkBlueprintBundle(Bundle bundle){
        // TODO do nothing currently
        return false;
    }
    
    private static boolean checkSysBundle(Bundle bundle, StartLevel startLevelService){
        //config.properties set karaf.systemBundlesStartLevel=50
        if (startLevelService!=null && startLevelService.getBundleStartLevel(bundle) <= 50){ 
            return true;
        }
        return false;
    }
        
    private static boolean checkConfigedBundle(Bundle bundle, Set<Long> configedBundleIds){
        // check configuration bundle
        if (configedBundleIds.contains(bundle.getBundleId())){
            return true;
        }
        return false;
    }
    
    private Set<Long> getConfigedBundleIds(){
        Set<Long> configedBundleIds = new HashSet<Long> ();
        
        ConfigurationManager configManager = PortletManager.getConfigurationManager();
        List<ConfigurationInfo> infos = configManager.listConfigurations();
        
        for (ConfigurationInfo info : infos) {
            Bundle configedBundle = configManager.getBundle(info.getConfigID());
            if (configedBundle!=null){
                configedBundleIds.add(configedBundle.getBundleId());
            }else{
                logger.info("Can not find the bundle for configuration: " +info.getConfigID()+ " in configuration manager");
            }
        }
        
        return configedBundleIds;
    }
    
    
    /*******************************
     * Bundle comparators
     *******************************/
    private static class BundleIdComparator implements Comparator<BundleInfo>{
        @Override
        public int compare(BundleInfo infoA, BundleInfo infoB) {
            if (infoA == null && infoB ==null) return 0;
            if (infoA == null) return -1;
            if (infoB == null) return 1;
            return (int) (infoA.getBundleId() - infoB.getBundleId());
        }
    }
    
    private static class BundleIdDescComparator implements Comparator<BundleInfo>{
        @Override
        public int compare(BundleInfo infoA, BundleInfo infoB) {
            if (infoA == null && infoB ==null) return 0;
            if (infoA == null) return 1;
            if (infoB == null) return -1;
            return (int) (infoB.getBundleId() - infoA.getBundleId());
        }
    }
    
    private static class BundleSymbolicComparator implements Comparator<BundleInfo>{
        @Override
        public int compare(BundleInfo infoA, BundleInfo infoB) {
            if (infoA == null && infoB ==null) return 0;
            if (infoA == null) return -1;
            if (infoB == null) return 1;
            return infoA.getSymbolicName().compareTo(infoB.getSymbolicName());
        }
    }
    
    
    
    
    /*************************************************************
     * Perspective definitions for Wired bundles page
     *************************************************************/
    public static class PackagePerspective implements Comparable<PackagePerspective>{

        private PackageInfo packageInfo;
        private List<BundleInfo> bundleInfos = new ArrayList<BundleInfo>();
        
        public PackagePerspective(String packageName, String packageVersion) {
            this.packageInfo = new PackageInfo(packageName, packageVersion);
        }
        
        public PackagePerspective(PackageInfo packageInfo) {
            this.packageInfo = packageInfo;
        }
        
        public PackagePerspective(PackageInfo packageInfo, List<BundleInfo> bundleInfos) {
            this.packageInfo = packageInfo;
            this.bundleInfos = bundleInfos;
        }
        
        @Override
        public int compareTo(PackagePerspective another) {
            if (another != null) {
                return packageInfo.compareTo(another.packageInfo);
            } else {
                return -1;
            } 
        }
        
        public PackageInfo getPackageInfo() {
            return packageInfo;
        }

        public List<BundleInfo> getBundleInfos() {
            return bundleInfos;
        }
        
        public void addBundleInfo(BundleInfo info){
            this.bundleInfos.add(info);
        }
        
        public void sortBundleInfos(Comparator<BundleInfo> comparator){
            Collections.sort(bundleInfos, comparator);
        }
    }
    
    public static class BundlePerspective implements Comparable<BundlePerspective>{

        private BundleInfo bundleInfo;
        private List<PackageInfo> packageInfos  = new ArrayList<PackageInfo>();
        private final Comparator<BundleInfo> comparator = new BundleSymbolicComparator();
        
        public BundlePerspective(Bundle bundle) {
            this.bundleInfo = new SimpleBundleInfo(bundle);
        }
        
        public BundlePerspective(BundleInfo info) {
            this.bundleInfo = info;
        }
        
        public BundlePerspective(BundleInfo info, List<PackageInfo> packageInfos) {
            this.bundleInfo = info;
            this.packageInfos = packageInfos;
        }
        
        @Override
        public int compareTo(BundlePerspective another) {
            if (another != null) {
                return comparator.compare(bundleInfo, another.bundleInfo);
            } else {
                return -1;
            } 
        }
        
        public BundleInfo getBundleInfo() {
            return bundleInfo;
        }

        public List<PackageInfo> getPackageInfos() {
            return packageInfos;
        }
        
        public void addPackageInfo(PackageInfo info){
            packageInfos.add(info);
        }
        
        public void sortPackageInfos(){
            Collections.sort(packageInfos);
        }
    }
    
    private Set<PackageBundlePair> getImportingPairs(PackageAdmin packageAdmin, Bundle bundle) {
        BundleDescription description = new BundleDescription(bundle.getHeaders());
        
        Set<PackageBundlePair> importingPairs = new HashSet<PackageBundlePair>();

        // handle static wire via Import-Package
        List<BundleDescription.ImportPackage> imports = description.getExternalImports();
        for (BundleDescription.ImportPackage packageImport : imports) {
            ExportedPackage[] exportedPackages = packageAdmin.getExportedPackages(packageImport.getName());
            if (exportedPackages!=null){
                for (ExportedPackage exportedPackage : exportedPackages) {
                    Bundle[] importingBundles = exportedPackage.getImportingBundles();
                    if (importingBundles != null) {
                        for (Bundle importingBundle : importingBundles) {
                            if (importingBundle == bundle) {
                                importingPairs.add(new PackageBundlePair(exportedPackage, exportedPackage.getExportingBundle()));
                            }
                        }
                    }
                }
            }
        }
        
        //TODO 1. sometimes, there might be same pairs in the result set.
        //TODO 2. the result set may contains 2 items which have the same package name but different versions.
        
        // handle dynamic wire via DynamicImport-Package
//        if (!description.getDynamicImportPackage().isEmpty()) {
//            for (Bundle b : bundle.getBundleContext().getBundles()) {
//                ExportedPackage[] exports = packageAdmin.getExportedPackages(b);
//                Bundle wiredBundle = getWiredBundle(bundle, exports);
//                if (exports != null) {
//                    for (ExportedPackage exportedPackage : exports) {
//                        Bundle[] importingBundles = exportedPackage.getImportingBundles();
//                        if (importingBundles != null) {
//                            for (Bundle importingBundle : importingBundles) {
//                                if (importingBundle == bundle) {
//                                    pbPairs.put(exportedPackage, wiredBundle);
//                                }
//                            }
//                        }
//                    }
//                }
//              
//            }
//        }
        
        return importingPairs;
    }
    
    
    private Set<PackageBundlePair> getExportingPairs(PackageAdmin packageAdmin, Bundle bundle) {
        
        Set<PackageBundlePair> exportingPairs = new HashSet<PackageBundlePair> ();
        
        ExportedPackage[] exportedPackages = packageAdmin.getExportedPackages(bundle);
        
        if (exportedPackages != null){
            for (ExportedPackage exportedPackage : exportedPackages) {
                Bundle[] importingBundles = exportedPackage.getImportingBundles();
                if (importingBundles != null) {
                    for (Bundle importingBundle : importingBundles) {
                        exportingPairs.add(new PackageBundlePair(exportedPackage, importingBundle));
                        
                    }
                }
            }
        }
        
        return exportingPairs;

    }
    
    private static class PackageBundlePair {
        final ExportedPackage exportedPackage;
        final Bundle bundle;
        
        PackageBundlePair(ExportedPackage exportedPackage,Bundle bundle ){
            this.exportedPackage = exportedPackage;
            this.bundle = bundle;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }

            if (getClass() != obj.getClass()) {
                return false;
            }

            final PackageBundlePair other = (PackageBundlePair) obj;
            if (this.exportedPackage != other.exportedPackage && (this.exportedPackage == null || !this.exportedPackage.equals(other.exportedPackage))) {
                return false;
            }
            if (this.bundle != other.bundle && (this.bundle == null || !this.bundle.equals(other.bundle))) {
                return false;
            }

            return true;
            
        }
        
        @Override
        public int hashCode() {
            int hash = 11;
            hash = 17* hash + (exportedPackage != null ? exportedPackage.hashCode():0);
            hash = 17 * hash + (bundle != null ? bundle.hashCode():0);

            return hash;
        }
        
    }
    

    private List<PackagePerspective> getPackagePerspectives(Set<PackageBundlePair> pairs){
        Map<PackageInfo,List<BundleInfo>> pbs = new HashMap<PackageInfo,List<BundleInfo>>();
        
        for (PackageBundlePair pair : pairs) {
            
            PackageInfo packageInfo = new PackageInfo(pair.exportedPackage.getName(), pair.exportedPackage.getVersion().toString());
            BundleInfo bundleInfo = new SimpleBundleInfo(pair.bundle);
            
            if (pbs.keySet().contains(packageInfo)){
                if (!pbs.get(packageInfo).contains(bundleInfo)) {
                    pbs.get(packageInfo).add(bundleInfo);
                }
            } else {
                List<BundleInfo> bundleInfos = new ArrayList<BundleInfo>();
                bundleInfos.add(bundleInfo);
                pbs.put(packageInfo, bundleInfos);
            }
            
        }
        
        List<PackagePerspective>packagePerspectives = new ArrayList<PackagePerspective>();
        BundleSymbolicComparator bsc = new BundleSymbolicComparator();
        for(Entry<PackageInfo,List<BundleInfo>> entry : pbs.entrySet()){
            PackagePerspective pp = new PackagePerspective(entry.getKey(),entry.getValue());
            pp.sortBundleInfos(bsc);
            packagePerspectives.add(pp);
        }
        
        return packagePerspectives;
    }
    
    private List<BundlePerspective> getBundlePerspectives(Set<PackageBundlePair> pairs){
        Map<BundleInfo,List<PackageInfo>> pbs = new HashMap<BundleInfo,List<PackageInfo>>();
        
        for (PackageBundlePair pair : pairs) {
            
            BundleInfo bundleInfo = new SimpleBundleInfo(pair.bundle);
            PackageInfo packageInfo = new PackageInfo(pair.exportedPackage.getName(), pair.exportedPackage.getVersion().toString());
            
            if (pbs.keySet().contains(bundleInfo)){
                if (!pbs.get(bundleInfo).contains(packageInfo)) {
                    pbs.get(bundleInfo).add(packageInfo);
                }
            } else {
                List<PackageInfo> packageInfos = new ArrayList<PackageInfo>();
                packageInfos.add(packageInfo);
                pbs.put(bundleInfo, packageInfos);
            }
            
        }
        
        List<BundlePerspective> bundlePerspectives = new ArrayList<BundlePerspective>();
        for(Entry<BundleInfo,List<PackageInfo>> entry : pbs.entrySet()){
            BundlePerspective bp = new BundlePerspective(entry.getKey(),entry.getValue());
            bp.sortPackageInfos();
            bundlePerspectives.add(bp);
        }
        
        return bundlePerspectives;
    }
    
    /******************************
     * Install Action
     ******************************/
    private void processInstallAction(ActionRequest request, BundleContext bundleContext,StartLevel startLevelService ) throws PortletException, IOException {
        if (!PortletFileUpload.isMultipartContent(request)) {
            throw new PortletException("Expected file upload");
        }

        // use commons-fileupload to process the request
        File rootDir = new File(System.getProperty("java.io.tmpdir"));
        PortletFileUpload uploader = new PortletFileUpload(new DiskFileItemFactory(10240, rootDir));
        
        File bundleFile = null;
        String startAfterInstalled = null;
        String str_startLevel = null;
        
        List<?> items;
        try {
            items = uploader.parseRequest(request);
        } catch (FileUploadException e) {
            addErrorMessage(request, "file upload failed!");
            logger.error("file upload failed!",e);
            return;
        }

        // deal with the multipart form data items;
        for (Iterator<?> i = items.iterator(); i.hasNext();) {
            FileItem item = (FileItem) i.next();
            if (!item.isFormField()) {
                String fieldName = item.getFieldName();
                String fileName = item.getName().trim();
                if (fileName.length() != 0) {
                    int index = fileName.lastIndexOf('\\');
                    if (index != -1) {
                        fileName = fileName.substring(index + 1);
                    }
                    if ("bundleFile".equals(fieldName)) {
                        bundleFile = new File(rootDir, fileName);
                    }
                }
                if (bundleFile != null) {
                    try {
                        item.write(bundleFile);
                    } catch (Exception e) {
                        addErrorMessage(request, "write file failed!");
                        logger.error("write file failed!", e);
                        return;
                    }
                } else {
                    addErrorMessage(request, "the file is null!");
                    logger.error("the file is null!");
                    return;
                }
            } else {
                if ("startAfterInstalled".equalsIgnoreCase(item.getFieldName())) {
                    startAfterInstalled = item.getString();
                } else if ("startLevel".equalsIgnoreCase(item.getFieldName())) {
                    str_startLevel = item.getString();
                }
            }
        }

        // install the uploaded bundle file
        String url = "file:///" + bundleFile.getCanonicalPath();

        Bundle installedBundle;
        try {
            installedBundle = bundleContext.installBundle(url);
            addInfoMessage(request, "bundle installed");
        } catch (BundleException e) {
            addErrorMessage(request, "install bundle failed!");
            logger.error("install bundle failed!", e);
            return;
        }
        

        // set start level for the installed bundle
        int startLevel = -1;
        try {
            startLevel = Integer.parseInt(str_startLevel);
        } catch (NumberFormatException e) {
            // if can't generated, use the default initialBundleStartLevel
        }
        int defaultStartLevel = startLevelService.getInitialBundleStartLevel();
        if (startLevel != defaultStartLevel && startLevel >= 0) {
            startLevelService.setBundleStartLevel(installedBundle, startLevel);
        }

        // if check box "Start" checked, then start the bundle
        if ("yes".equals(startAfterInstalled)) {
            try {
                installedBundle.start();
                addInfoMessage(request, "bundle started");
            } catch (BundleException e) {
                addErrorMessage(request, "start bundle failed!");
                logger.error("start bundle failed!", e);
                return;
            }
            
        }
    }
}
