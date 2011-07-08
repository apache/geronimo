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
import java.net.URL;
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
import org.apache.xbean.osgi.bundle.util.VersionRange;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.blueprint.container.BlueprintContainer;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.service.startlevel.StartLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BundleManagerPortlet extends BasePortlet {

    private static final Logger logger = LoggerFactory.getLogger(BundleManagerPortlet.class);
    
    

    private PortletRequestDispatcher helpView;
    
    private PortletRequestDispatcher bundleManagerView;

    private PortletRequestDispatcher showManifestView;
    
    private PortletRequestDispatcher showWiredBundlesView;
    
    private PortletRequestDispatcher showServicesView;
    
    private PortletRequestDispatcher findPackagesView;
    
    private static final String VIEW_MANIFEST_PAGE = "view_manifest";
    
    private static final String VIEW_WIRED_BUNDLES_PAGE = "view_wired_bundles";
    
    private static final String VIEW_SERVICES_PAGE = "view_services";
    
    private static final String FIND_PACKAGES_PAGE = "find_packages";
    
    private static final String SERACH_ACTION = "search";
    
    private static final String LIST_ACTION = "list";
    
    private static final String INSTALL_ACTION = "install";
    
    private static final String BUNDLE_ACTION = "bundle";

    private static final String START_OPERATION = "start";

    private static final String STOP_OPERATION = "stop";

    private static final String UPDATE_OPERATION = "update";
    
    private static final String REFRESH_OPERATION = "refresh";
    
    private static final String UNINSTALL_OPERATION = "uninstall";
    
    protected void doHelp(RenderRequest renderRequest, RenderResponse renderResponse) throws PortletException, IOException {
        helpView.include(renderRequest, renderResponse);
    }

    public void init(PortletConfig portletConfig) throws PortletException {
        super.init(portletConfig);
        helpView = portletConfig.getPortletContext().getRequestDispatcher("/WEB-INF/view/bundlemanager/BundleManager.jsp");
        bundleManagerView = portletConfig.getPortletContext().getRequestDispatcher("/WEB-INF/view/bundlemanager/BundleManager.jsp");
        showManifestView = portletConfig.getPortletContext().getRequestDispatcher("/WEB-INF/view/bundlemanager/ShowManifest.jsp");
        showWiredBundlesView = portletConfig.getPortletContext().getRequestDispatcher("/WEB-INF/view/bundlemanager/ShowWiredBundles.jsp");
        showServicesView = portletConfig.getPortletContext().getRequestDispatcher("/WEB-INF/view/bundlemanager/ShowServices.jsp");
        findPackagesView = portletConfig.getPortletContext().getRequestDispatcher("/WEB-INF/view/bundlemanager/FindPackages.jsp");
    }

    public void destroy() {
        bundleManagerView = null;
        super.destroy();
    }
    
    public void processAction(ActionRequest actionRequest, ActionResponse actionResponse) throws PortletException, IOException {
        
        actionResponse.setRenderParameter("message", ""); // set to blank first
        
        String page = actionRequest.getParameter("page");
        
        if (VIEW_MANIFEST_PAGE.equals(page)){
            // no actions in this page
            actionResponse.setRenderParameter("page", VIEW_MANIFEST_PAGE);
            
        } else if (VIEW_WIRED_BUNDLES_PAGE.equals(page)){
            String id = actionRequest.getParameter("bundleId");
            String perspectiveType = actionRequest.getParameter("perspectiveType");
            //set render params
            actionResponse.setRenderParameter("page", VIEW_WIRED_BUNDLES_PAGE);
            actionResponse.setRenderParameter("bundleId", id);
            actionResponse.setRenderParameter("perspectiveTypeValue", perspectiveType);
            
        } else if (FIND_PACKAGES_PAGE.equals(page)) {
            String packageString = actionRequest.getParameter("packageString");
            //set render params
            actionResponse.setRenderParameter("page", FIND_PACKAGES_PAGE);
            actionResponse.setRenderParameter("packageStringValue", packageString);
            
        } else { //main page
            
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
                    String symbolicName = bundle.getSymbolicName();
                                        
                    if (START_OPERATION.equals(operation)) {
                        bundle.start();
                        addInfoMessage(actionRequest, getLocalizedString(actionRequest, "consolebase.bundlemanager.info.start", symbolicName, id));
                    } else if (STOP_OPERATION.equals(operation)) {
                        bundle.stop();
                        addInfoMessage(actionRequest, getLocalizedString(actionRequest, "consolebase.bundlemanager.info.stop", symbolicName, id));
                    } else if (UNINSTALL_OPERATION.equals(operation)) {
                        bundle.uninstall();
                        addInfoMessage(actionRequest, getLocalizedString(actionRequest, "consolebase.bundlemanager.info.uninstall", symbolicName, id));
                    } else if (UPDATE_OPERATION.equals(operation)) {
                        bundle.update();
                        addInfoMessage(actionRequest, getLocalizedString(actionRequest, "consolebase.bundlemanager.info.update", symbolicName, id));
                    } else if (REFRESH_OPERATION.equals(operation)) {
                        ServiceReference reference = bundleContext.getServiceReference(PackageAdmin.class.getName());
                        PackageAdmin packageAdmin = (PackageAdmin) bundleContext.getService(reference);
                        packageAdmin.refreshPackages(new Bundle[]{bundle});
                        addInfoMessage(actionRequest, getLocalizedString(actionRequest, "consolebase.bundlemanager.info.refresh", symbolicName, id));
                    } else {
                        // should never happen
                        addWarningMessage(actionRequest, getLocalizedString(actionRequest, "consolebase.bundlemanager.warn.invalidAction") + action);
                    }
                } catch (Throwable e) {
                    addErrorMessage(actionRequest, getLocalizedString(actionRequest, "consolebase.bundlemanager.err.actionError") + action, e.getMessage());
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
            
            if (FIND_PACKAGES_PAGE.equals(page)){
                BundleContext bundleContext = getBundleContext(renderRequest);
                
                ServiceReference reference = bundleContext.getServiceReference(PackageAdmin.class.getName());
                PackageAdmin packageAdmin = (PackageAdmin) bundleContext.getService(reference);
                
                
                String packageString = renderRequest.getParameter("packageStringValue");
                 
               
                Map<PackageInfo,List<BundleInfo>> packageExportBundles = new HashMap<PackageInfo,List<BundleInfo>>();
                Map<PackageInfo,List<BundleInfo>> packageImportBundles = new HashMap<PackageInfo,List<BundleInfo>>();
                
                
                for (Bundle bundle : bundleContext.getBundles()) {
                    ExportedPackage[] exportedPackages = packageAdmin.getExportedPackages(bundle);
                    if (exportedPackages != null) {
                        
                        // construct the export bundle info
                        BundleInfo exportBundleInfo = new SimpleBundleInfo(bundle);
                        
                        for (ExportedPackage exportedPackage : exportedPackages) {
                            // filter by keyword and ignore case. if the keyword is null, then return all the packages
                            if (packageString==null || exportedPackage.getName().toLowerCase().indexOf(packageString.trim().toLowerCase())!= -1) {
                                // construct the package info
                                // fill in its export bundle
                                PackageInfo packageInfo = new PackageInfo(exportedPackage.getName(), exportedPackage.getVersion().toString());
                                fillPackageBundlesMap(packageExportBundles, packageInfo, exportBundleInfo);
                                
                                Bundle[] importingBundles = exportedPackage.getImportingBundles();
                                if (importingBundles != null) {                                    
                                    for (Bundle importingBundle : importingBundles) {
                                        
                                        // construct the import bundle info
                                        // fill in its import bundle
                                        BundleInfo importBundleInfo = new SimpleBundleInfo(importingBundle);
                                        fillPackageBundlesMap(packageImportBundles, packageInfo, importBundleInfo);
                                        
                                    } 
                                    
                                }
                            }
                        }
                    }
                }
                
                
                List<PackageWiredBundles> packageWiredBundlesList = new ArrayList<PackageWiredBundles>();
                BundleSymbolicComparator bsc = new BundleSymbolicComparator();
                for(Entry<PackageInfo,List<BundleInfo>> entry : packageExportBundles.entrySet()){
                    PackageInfo pkg = entry.getKey();
                    List<BundleInfo> exportBundles = entry.getValue();
                    List<BundleInfo> importBundles = packageImportBundles.get(pkg) == null? new ArrayList<BundleInfo>():packageImportBundles.get(pkg);
                    
                    PackageWiredBundles pwb = new PackageWiredBundles(pkg, exportBundles, importBundles);
                    pwb.sortBundleInfos(bsc);
                    packageWiredBundlesList.add(pwb);
                }
                
                Collections.sort(packageWiredBundlesList);
                
                renderRequest.setAttribute("packageWiredBundlesList", packageWiredBundlesList);
                renderRequest.setAttribute("packageStringValue", packageString);
                findPackagesView.include(renderRequest, renderResponse);
                
            } else if (VIEW_MANIFEST_PAGE.equals(page)){
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
                
            }else if(VIEW_SERVICES_PAGE.equals(page)) {
                
                BundleContext bundleContext = getBundleContext(renderRequest);
                
                long id = Long.valueOf(renderRequest.getParameter("bundleId"));
                Bundle bundle = bundleContext.getBundle(id);
                
                // because this page should not be very complex ,so we only have a Service Perspective
                // if user wants 2 perspective like wired bundle page, we can extend this page to add a new Bundle Perspective.
                List<ServicePerspective> usingServicePerspectives = getUsingServicePerspectives(bundle);
                List<ServicePerspective> registeredServicePerspectives = getRegisteredServicePerspectives(bundle);
                
                Collections.sort(usingServicePerspectives);
                Collections.sort(registeredServicePerspectives);
                
                renderRequest.setAttribute("usingServicePerspectives", usingServicePerspectives);
                renderRequest.setAttribute("registeredServicePerspectives", registeredServicePerspectives);
                
                SimpleBundleInfo bundleInfo = new SimpleBundleInfo(bundle);
                
                renderRequest.setAttribute("bundleInfo", bundleInfo);
                
                showServicesView.include(renderRequest, renderResponse);
                
            }else if(VIEW_WIRED_BUNDLES_PAGE.equals(page)) {
                
                BundleContext bundleContext = getBundleContext(renderRequest);
                
                long id = Long.valueOf(renderRequest.getParameter("bundleId"));
                Bundle bundle = bundleContext.getBundle(id);
                
                String perspectiveType = renderRequest.getParameter("perspectiveTypeValue");
                if (perspectiveType == null || perspectiveType == "") perspectiveType = "bundle"; //when we access this page with a renderURL, we need the default value
                
                ServiceReference reference = bundleContext.getServiceReference(PackageAdmin.class.getName());
                PackageAdmin packageAdmin = (PackageAdmin) bundle.getBundleContext().getService(reference);
                

                Set<PackageBundlePair> importingPairs = getImportingPairs(packageAdmin, bundle);
                Set<PackageBundlePair> dynamicImportingPairs = getDynamicImportingPairs(packageAdmin, bundle, importingPairs);
                Set<PackageBundlePair> requireBundlesImportingPairs = getRequireBundlesImportingPairs(packageAdmin, bundle);
                Set<PackageBundlePair> exportingPairs = getExportingPairs(packageAdmin, bundle);
                
                
                if ("package".equals(perspectiveType)){
                    List<PackagePerspective> importingPackagePerspectives = getPackagePerspectives(importingPairs);
                    List<PackagePerspective> dynamicImportingPackagePerspectives = getPackagePerspectives(dynamicImportingPairs);
                    List<PackagePerspective> requireBundlesImportingPackagePerspectives = getPackagePerspectives(requireBundlesImportingPairs);
                    List<PackagePerspective> exportingPackagePerspectives = getPackagePerspectives(exportingPairs);
                    
                    Collections.sort(importingPackagePerspectives);
                    Collections.sort(dynamicImportingPackagePerspectives);
                    Collections.sort(requireBundlesImportingPackagePerspectives);
                    Collections.sort(exportingPackagePerspectives);
                    
                    renderRequest.setAttribute("importingPackagePerspectives", importingPackagePerspectives);
                    renderRequest.setAttribute("dynamicImportingPackagePerspectives", dynamicImportingPackagePerspectives);
                    renderRequest.setAttribute("requireBundlesImportingPackagePerspectives", requireBundlesImportingPackagePerspectives);
                    renderRequest.setAttribute("exportingPackagePerspectives", exportingPackagePerspectives);
                    
                }else{  //"bundle".equals(perspectiveType)){
                    List<BundlePerspective> importingBundlePerspectives = getBundlePerspectives(importingPairs);
                    List<BundlePerspective> dynamicImportingBundlePerspectives = getBundlePerspectives(dynamicImportingPairs);
                    List<BundlePerspective> requireBundlesImportingBundlePerspectives = getBundlePerspectives(requireBundlesImportingPairs);
                    List<BundlePerspective> exportingBundlePerspectives = getBundlePerspectives(exportingPairs);
                    
                    Collections.sort(importingBundlePerspectives);
                    Collections.sort(dynamicImportingBundlePerspectives);
                    Collections.sort(requireBundlesImportingBundlePerspectives);
                    Collections.sort(exportingBundlePerspectives);
                    
                    renderRequest.setAttribute("importingBundlePerspectives", importingBundlePerspectives);
                    renderRequest.setAttribute("dynamicImportingBundlePerspectives", dynamicImportingBundlePerspectives);
                    renderRequest.setAttribute("requireBundlesImportingBundlePerspectives", requireBundlesImportingBundlePerspectives);
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
                Set<Long> configurationBundleIds = getConfigurationBundleIds();
                
                Bundle[] bundles = bundleContext.getBundles();
                for (Bundle bundle : bundles) {
                    
                    if (searchString != "" && !matchBundle(bundle, searchString)){
                        continue;
                    }
                    
                    // construct the result bundleInfos by listType
                    if ("wab".equals(listType)){
                        if (checkWABBundle(bundle)){
                            ExtendedBundleInfo info = createExtendedBundleInfo(bundle, startLevelService, configurationBundleIds);
                            info.addContextPath(getContextPath(bundle));
                            bundleInfos.add(info);
                        }
                    }else if ("blueprint".equals(listType)){
                        if (checkBlueprintBundle(bundle)){
                            ExtendedBundleInfo info = createExtendedBundleInfo(bundle, startLevelService, configurationBundleIds);
                            
                            // currently, we try get the the blueprintContainer service to determine if a blueprint bundle is created
                            // TODO A better way is using a BlueprintListener to track all blueprint bundle events
                            String filter = "(&(osgi.blueprint.container.symbolicname=" + bundle.getSymbolicName()
                                            + ")(osgi.blueprint.container.version=" + bundle.getVersion() + "))";
                            ServiceReference[] serviceReferences = null;
                            try {
                                serviceReferences = bundleContext.getServiceReferences(BlueprintContainer.class.getName(), filter);
                            } catch (InvalidSyntaxException e) {
                                throw new RuntimeException(e);
                            }
                            if (serviceReferences != null && serviceReferences.length > 0){
                                info.setBlueprintState(BlueprintState.CREATED);
                            }
                            
                            bundleInfos.add(info);
                        }
                    }else if ("system".equals(listType)){
                        if (checkSysBundle(bundle,startLevelService)){
                            ExtendedBundleInfo info = createExtendedBundleInfo(bundle, startLevelService, configurationBundleIds);
                            bundleInfos.add(info);
                        }
                    }else if ("configuration".equals(listType)){
                        if (checkConfigurationBundle(bundle,configurationBundleIds)){
                            ExtendedBundleInfo info = createExtendedBundleInfo(bundle, startLevelService, configurationBundleIds);
                            bundleInfos.add(info);
                        }
                    }else{
                        ExtendedBundleInfo info = createExtendedBundleInfo(bundle, startLevelService, configurationBundleIds);
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
                    addWarningMessage(renderRequest, getLocalizedString(renderRequest, "consolebase.bundlemanager.warn.nobundlesfound"));
                }
                
                bundleManagerView.include(renderRequest, renderResponse);
                
            }
        }
    }
    
    private ExtendedBundleInfo createExtendedBundleInfo(Bundle bundle, StartLevel startLevelService, Set<Long> configurationBundleIds){
        ExtendedBundleInfo info = new ExtendedBundleInfo(bundle);
      
        if (checkWABBundle(bundle)){
            info.addType(BundleType.WAB);
        }
        
        if (checkBlueprintBundle(bundle)) {
            info.addType(BundleType.BLUEPRINT);
        }
        
        if (checkSysBundle(bundle, startLevelService)) {
            info.addType(BundleType.SYSTEM);
        }
        
        if (checkConfigurationBundle(bundle,configurationBundleIds)){
            info.addType(BundleType.CONFIGURATION);
        }
        
        return info;
    }
    
    private BundleContext getBundleContext(PortletRequest request) {
        return (BundleContext) request.getPortletSession().getPortletContext().getAttribute("osgi-bundlecontext");
    }
    
    private static boolean matchBundle(Bundle bundle, String searchString){
        if (bundle.getSymbolicName() == null 
            || (bundle.getSymbolicName() != null && bundle.getSymbolicName().toLowerCase().indexOf(searchString.trim().toLowerCase()) == -1)){ // match ignore case
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
        // OSGi enterprise spec(r4.2) 121.3.4 (Page 206)
        // check blueprint header
        Object bpHeader = bundle.getHeaders().get(BundleUtil.BLUEPRINT_HEADER);
        if (bpHeader!=null && (String)bpHeader!="") return true;
        
        // check blueprint definitions
        Enumeration<URL> enu = bundle.findEntries("OSGI-INF/blueprint/", "*.xml", false);
        if (enu!=null && enu.hasMoreElements()) return true;

        return false;
    }
    
    private static boolean checkSysBundle(Bundle bundle, StartLevel startLevelService){
        //config.properties set karaf.systemBundlesStartLevel=50
        if (startLevelService!=null && startLevelService.getBundleStartLevel(bundle) <= 50){ 
            return true;
        }
        return false;
    }
        
    private static boolean checkConfigurationBundle(Bundle bundle, Set<Long> configurationBundleIds){
        // check configuration bundle
        if (configurationBundleIds.contains(bundle.getBundleId())){
            return true;
        }
        return false;
    }
    
    private Set<Long> getConfigurationBundleIds(){
        Set<Long> configurationBundleIds = new HashSet<Long> ();
        
        ConfigurationManager configManager = PortletManager.getConfigurationManager();
        List<ConfigurationInfo> infos = configManager.listConfigurations();
        
        for (ConfigurationInfo info : infos) {
            Bundle configurationBundle = configManager.getBundle(info.getConfigID());
            if (configurationBundle!=null){
                configurationBundleIds.add(configurationBundle.getBundleId());
            }else{
                logger.info("Can not find the bundle for configuration: " +info.getConfigID()+ " in configuration manager");
            }
        }
        
        return configurationBundleIds;
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
    
    private static class ServiceObjectClassComparator implements Comparator<ServiceInfo>{
        @Override
        public int compare(ServiceInfo infoA, ServiceInfo infoB) {
            if (infoA == null && infoB ==null) return 0;
            if (infoA == null) return -1;
            if (infoB == null) return 1;
            
            String objectClassA = "";
            for ( String str : infoA.getObjectClass()){
                objectClassA += str+",";
            }
            String objectClassB = "";
            for ( String str : infoB.getObjectClass()){
                objectClassB += str+",";
            }
            return objectClassA.compareTo(objectClassB);
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


        // description.getExternalImports() only shows the packages from other bundles. This will exclude the packages
        // which are exported by itself, but may actually import from others during resolve.
        List<BundleDescription.ImportPackage> imports = description.getImportPackage();
        for (BundleDescription.ImportPackage packageImport : imports) {
            //find the packages that we are importing
            ExportedPackage[] exportedPackages = packageAdmin.getExportedPackages(packageImport.getName());
            if (exportedPackages!=null){
                for (ExportedPackage exportedPackage : exportedPackages) {
                    Bundle exportingBundle = exportedPackage.getExportingBundle();
                    Bundle[] importingBundles = exportedPackage.getImportingBundles();
                    if (importingBundles != null) {
                        for (Bundle importingBundle : importingBundles) {
                            if (exportingBundle != bundle && importingBundle == bundle) {
                                importingPairs.add(new PackageBundlePair(exportedPackage, exportedPackage.getExportingBundle()));
                            }
                        }
                    }
                }
            }
            
        }
        //TODO  the result set may contains 2 items which have the same package name but different versions.
        return importingPairs;
    }

    private Set<PackageBundlePair> getRequireBundlesImportingPairs(PackageAdmin packageAdmin, Bundle bundle) {
        
        Set<PackageBundlePair> requireBundlesImportingPairs = new HashSet<PackageBundlePair>();
        
        BundleDescription description = new BundleDescription(bundle.getHeaders());
        
        List<BundleDescription.RequireBundle> requireBundles = description.getRequireBundle();
        if (!requireBundles.isEmpty()) {

            Map<String, VersionRange> requireBundlesMap = new HashMap<String, VersionRange>();

            for (BundleDescription.RequireBundle requireBundle : requireBundles) {
                requireBundlesMap.put(requireBundle.getName(), requireBundle.getVersionRange());
            }

            Set<String> requireBundleNames = requireBundlesMap.keySet();
            for (Bundle b : bundle.getBundleContext().getBundles()) {
                if (requireBundleNames.contains(b.getSymbolicName())
                        && requireBundlesMap.get(b.getSymbolicName()).isInRange(b.getVersion())) {

                    // find the packages that importing from the require bundle
                    ExportedPackage[] exportedPackages = packageAdmin.getExportedPackages(b);
                    if (exportedPackages != null) {
                        for (ExportedPackage exportedPackage : exportedPackages) {
                            Bundle[] importingBundles = exportedPackage.getImportingBundles();
                            if (importingBundles != null) {
                                for (Bundle importingBundle : importingBundles) {
                                    if (importingBundle == bundle) {
                                        requireBundlesImportingPairs.add(new PackageBundlePair(exportedPackage, b));
                                    }
                                }
                            }
                        }
                    }

                }

            }
        }
        
        return requireBundlesImportingPairs;
    
    }
    
    
    private Set<PackageBundlePair> getDynamicImportingPairs(PackageAdmin packageAdmin, Bundle bundle, Set<PackageBundlePair> explicitImportingPairs) {
        BundleDescription description = new BundleDescription(bundle.getHeaders());
        
        Set<PackageBundlePair> dynamicImportingPairs = new HashSet<PackageBundlePair>();
        
        if (!description.getDynamicImportPackage().isEmpty()) {
            for (Bundle b : bundle.getBundleContext().getBundles()) {
                
                // find the packages that importing from the bundle
                ExportedPackage[] exportedPackages = packageAdmin.getExportedPackages(b);
                if (exportedPackages != null) {
                    for (ExportedPackage exportedPackage : exportedPackages) {
                        Bundle[] importingBundles = exportedPackage.getImportingBundles();
                        if (importingBundles != null) {
                            for (Bundle importingBundle : importingBundles) {
                                if (importingBundle == bundle) {
                                    PackageBundlePair pair = new PackageBundlePair(exportedPackage, b);
                                    if (!explicitImportingPairs.contains(pair)){
                                        dynamicImportingPairs.add(pair);
                                    }
                                }
                            }
                        }
                    }
                }
                
            }
        }
        
        return dynamicImportingPairs;
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
// when using the following, there still be same pairs in the set. Maybe the equal method is not re-written.
//            if (this.exportedPackage != other.exportedPackage && (this.exportedPackage == null || !this.exportedPackage.equals(other.exportedPackage))) {
//                return false;
//            }
//            if (this.bundle != other.bundle && (this.bundle == null || !this.bundle.equals(other.bundle))) {
//                return false;
//            }
            
            if (this.exportedPackage != null && other.exportedPackage == null) return false;
            if (this.exportedPackage == null && other.exportedPackage != null) return false;
            if (this.exportedPackage != null && other.exportedPackage != null) {
                if (this.exportedPackage.getName()!= other.exportedPackage.getName() || this.exportedPackage.getVersion() != other.exportedPackage.getVersion())
                    return false;
            }
            
            if (this.bundle != null && other.bundle == null) return false;
            if (this.bundle == null && other.bundle != null) return false;
            if (this.bundle != null && other.bundle != null) {
                if (this.bundle.getSymbolicName()!= other.bundle.getSymbolicName() || this.bundle.getVersion() != other.bundle.getVersion())
                    return false;
            }
            
            return true;
            
        }
        
        @Override
        public int hashCode() {
            int hash = 11;
// because we used exportedPackage.getName(); exportedPackage.getVersion();  bundle.getSymbolicName();bundle.getVersion()
// to calculate equals(), we must use them to calculate hashCode()
//            hash = 17 * hash + (exportedPackage != null ? exportedPackage.hashCode():0);
//            hash = 17 * hash + (bundle != null ? bundle.hashCode():0);
            hash = 17 * hash + (exportedPackage != null ? exportedPackage.getName().hashCode():0);
            hash = 17 * hash + (exportedPackage != null ? exportedPackage.getVersion().hashCode():0);
            hash = 17 * hash + (bundle != null ? bundle.getSymbolicName().hashCode():0);
            hash = 17 * hash + (bundle != null ? bundle.getVersion().hashCode():0);

            return hash;
        }
        
    }
    

    private List<PackagePerspective> getPackagePerspectives(Set<PackageBundlePair> pairs){
        Map<PackageInfo,List<BundleInfo>> pbs = new HashMap<PackageInfo,List<BundleInfo>>();
        
        for (PackageBundlePair pair : pairs) {
            
            PackageInfo packageInfo = new PackageInfo(pair.exportedPackage.getName(), pair.exportedPackage.getVersion().toString());
            BundleInfo bundleInfo = new SimpleBundleInfo(pair.bundle);
            
            fillPackageBundlesMap(pbs, packageInfo, bundleInfo);
            
        }
        
        List<PackagePerspective> packagePerspectives = new ArrayList<PackagePerspective>();
        BundleSymbolicComparator bsc = new BundleSymbolicComparator();
        for(Entry<PackageInfo,List<BundleInfo>> entry : pbs.entrySet()){
            PackagePerspective pp = new PackagePerspective(entry.getKey(),entry.getValue());
            pp.sortBundleInfos(bsc);
            packagePerspectives.add(pp);
        }
        
        return packagePerspectives;
    }
    
    private List<BundlePerspective> getBundlePerspectives(Set<PackageBundlePair> pairs){
        Map<BundleInfo,List<PackageInfo>> bps = new HashMap<BundleInfo,List<PackageInfo>>();
        
        for (PackageBundlePair pair : pairs) {
            
            BundleInfo bundleInfo = new SimpleBundleInfo(pair.bundle);
            PackageInfo packageInfo = new PackageInfo(pair.exportedPackage.getName(), pair.exportedPackage.getVersion().toString());
            
            fillBundlePackagesMap(bps, bundleInfo, packageInfo);
            
        }
        
        List<BundlePerspective> bundlePerspectives = new ArrayList<BundlePerspective>();
        for(Entry<BundleInfo,List<PackageInfo>> entry : bps.entrySet()){
            BundlePerspective bp = new BundlePerspective(entry.getKey(),entry.getValue());
            bp.sortPackageInfos();
            bundlePerspectives.add(bp);
        }
        
        return bundlePerspectives;
    }
    
    private void fillPackageBundlesMap(Map<PackageInfo,List<BundleInfo>> pbmap, PackageInfo packageInfo, BundleInfo bundleInfo){
        if (pbmap.keySet().contains(packageInfo)){
            if (!pbmap.get(packageInfo).contains(bundleInfo)) {
                pbmap.get(packageInfo).add(bundleInfo);
            }
        } else {
            List<BundleInfo> bundleInfos = new ArrayList<BundleInfo>();
            bundleInfos.add(bundleInfo);
            pbmap.put(packageInfo, bundleInfos);
        }
    }
    
    private void fillBundlePackagesMap(Map<BundleInfo,List<PackageInfo>> bpmap, BundleInfo bundleInfo, PackageInfo packageInfo){
        if (bpmap.keySet().contains(bundleInfo)){
            if (!bpmap.get(bundleInfo).contains(packageInfo)) {
                bpmap.get(bundleInfo).add(packageInfo);
            }
        } else {
            List<PackageInfo> packageInfos = new ArrayList<PackageInfo>();
            packageInfos.add(packageInfo);
            bpmap.put(bundleInfo, packageInfos);
        }
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
            addErrorMessage(request, getLocalizedString(request, "consolebase.bundlemanager.err.file.uploadError"));
            logger.error("FileUploadException", e);
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
                        addErrorMessage(request, getLocalizedString(request,"consolebase.bundlemanager.err.file.writeError"));
                        logger.error("Exception", e);
                        return;
                    }
                } else {
                    //should never happen
                    addErrorMessage(request, getLocalizedString(request, "consolebase.bundlemanager.err.file.nullError"));
                    logger.error("The uploaded file is null!");
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
            addInfoMessage(request, getLocalizedString(request, "consolebase.bundlemanager.info.install", installedBundle.getSymbolicName(), installedBundle.getBundleId()));
        } catch (BundleException e) {
            addErrorMessage(request, getLocalizedString(request, "consolebase.bundlemanager.err.actionError") + "install", e.getMessage());
            logger.error("BundleException", e);
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
                addInfoMessage(request, getLocalizedString(request, "consolebase.bundlemanager.info.start", installedBundle.getSymbolicName(), installedBundle.getBundleId()));
            } catch (BundleException e) {
                addErrorMessage(request, getLocalizedString(request, "consolebase.bundlemanager.err.actionError") + "start", e.getMessage());
                logger.error("BundleException", e);
                return;
            }
            
        }
    }
    
    
    /************************************************
     * Show services definitions
     ************************************************/
    public static class ServicePerspective implements Comparable<ServicePerspective>{

        private ServiceInfo serviceInfo;
        private List<BundleInfo> bundleInfos = new ArrayList<BundleInfo>();
        private final Comparator<ServiceInfo> comparator = new ServiceObjectClassComparator();
        
        
        public ServicePerspective(ServiceInfo serviceInfo) {
            this.serviceInfo = serviceInfo;
        }
        
        public ServicePerspective(ServiceInfo serviceInfo, List<BundleInfo> bundleInfos) {
            this.serviceInfo = serviceInfo;
            this.bundleInfos = bundleInfos;
        }
        
        @Override
        public int compareTo(ServicePerspective another) {
            if (another != null) {
                return comparator.compare(serviceInfo,another.serviceInfo);
            } else {
                return -1;
            } 
        }
        
        public ServiceInfo getServiceInfo() {
            return serviceInfo;
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
    
    private List<ServicePerspective> getUsingServicePerspectives(Bundle bundle){
        ServiceReference[] serviceRefs = bundle.getServicesInUse();
        
        List<ServicePerspective> usingServicePerspectives = new ArrayList<ServicePerspective>();
        
        if (serviceRefs != null && serviceRefs.length!=0){
            for (ServiceReference ref : serviceRefs){
                ServiceInfo info = new ServiceInfo(ref);
                ServicePerspective perspective = new ServicePerspective(info);
                perspective.addBundleInfo(new SimpleBundleInfo(ref.getBundle()));
                usingServicePerspectives.add(perspective);
            }
        }
        
        
        return usingServicePerspectives;
    }
    
    private List<ServicePerspective> getRegisteredServicePerspectives(Bundle bundle){
        ServiceReference[] serviceRefs = bundle.getRegisteredServices();
        
        List<ServicePerspective> registeredServicePerspectives = new ArrayList<ServicePerspective>();
        
        if (serviceRefs != null && serviceRefs.length!=0){
            for (ServiceReference ref : serviceRefs){
                ServiceInfo info = new ServiceInfo(ref);
                ServicePerspective perspective = new ServicePerspective(info);
                
                Bundle[] usingBundles = ref.getUsingBundles();
                if (usingBundles!=null && usingBundles.length!=0){
                    for (Bundle usingBundle : usingBundles){
                        perspective.addBundleInfo(new SimpleBundleInfo(usingBundle));
                    }
                }
                                
                registeredServicePerspectives.add(perspective);
            }
        }
        
        
        return registeredServicePerspectives;
    }
    
    
    /*************************************************************
     * definitions for find packages page
     *************************************************************/
    public static class PackageWiredBundles implements Comparable<PackageWiredBundles>{

        private PackageInfo packageInfo;
        private List<BundleInfo> importBundleInfos = new ArrayList<BundleInfo>();
        private List<BundleInfo> exportBundleInfos = new ArrayList<BundleInfo>();
                
        public PackageWiredBundles(String packageName, String packageVersion) {
            this.packageInfo = new PackageInfo(packageName, packageVersion);
        }
        
        public PackageWiredBundles(PackageInfo packageInfo) {
            this.packageInfo = packageInfo;
        }
        
        public PackageWiredBundles(PackageInfo packageInfo, List<BundleInfo> exportBundleInfos, List<BundleInfo> importBundleInfos) {
            this.packageInfo = packageInfo;
            this.importBundleInfos = importBundleInfos;
            this.exportBundleInfos = exportBundleInfos;
        }
        
        @Override
        public int compareTo(PackageWiredBundles another) {
            if (another != null) {
                return packageInfo.compareTo(another.packageInfo);
            } else {
                return -1;
            } 
        }
        
        public PackageInfo getPackageInfo() {
            return packageInfo;
        }

        public List<BundleInfo> getImportBundleInfos() {
            return importBundleInfos;
        }
        
        public void addImportBundleInfo(BundleInfo info){
            this.importBundleInfos.add(info);
        }
        
        public List<BundleInfo> getExportBundleInfos() {
            return exportBundleInfos;
        }
        
        public void addExportBundleInfo(BundleInfo info){
            this.exportBundleInfos.add(info);
        }
        
        public void sortBundleInfos(Comparator<BundleInfo> comparator){
            Collections.sort(importBundleInfos, comparator);
            Collections.sort(exportBundleInfos, comparator);
        }
    }
}
