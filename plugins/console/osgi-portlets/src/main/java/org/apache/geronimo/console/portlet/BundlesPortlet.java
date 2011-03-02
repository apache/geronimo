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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.console.portlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.WindowState;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.portlet.PortletFileUpload;
import org.apache.geronimo.console.javabean.OSGiBundle;
import org.apache.geronimo.console.json.BundleGridJSONObject;
import org.apache.geronimo.console.json.ManifestGridJSONObject;
import org.apache.geronimo.console.json.PackageBundlePairGridJSONObject;
import org.apache.geronimo.console.json.PackageBundlePairJSONObject;
import org.apache.geronimo.console.json.WiredBundlesJSONObject;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.kernel.config.ConfigurationInfo;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.xbean.osgi.bundle.util.BundleDescription;
import org.apache.xbean.osgi.bundle.util.BundleUtils;
import org.apache.xbean.osgi.bundle.util.BundleDescription.ExportPackage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.service.startlevel.StartLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BundlesPortlet extends GenericPortlet {

    private static final Logger logger = LoggerFactory.getLogger(BundlesPortlet.class);
    
    private static final String NORMALVIEW_JSP = "/WEB-INF/view/BundlesView.jsp";
    private static final String MAXIMIZEDVIEW_JSP = "/WEB-INF/view/BundlesView.jsp";
    private static final String HELPVIEW_JSP = "/WEB-INF/view/BundlesView.jsp";
    private PortletRequestDispatcher normalView;
    private PortletRequestDispatcher maximizedView;
    private PortletRequestDispatcher helpView;

   
    public void init(PortletConfig portletConfig) throws PortletException {
        super.init(portletConfig);
        normalView = portletConfig.getPortletContext().getRequestDispatcher(NORMALVIEW_JSP);
        maximizedView = portletConfig.getPortletContext().getRequestDispatcher(MAXIMIZEDVIEW_JSP);
        helpView = portletConfig.getPortletContext().getRequestDispatcher(HELPVIEW_JSP);
    }
    
    public void processAction(ActionRequest actionRequest, ActionResponse actionResponse) throws PortletException,
    IOException {
        // this portlet use dojo/ajax to process action. so all the actions are in serverResource method.
    }
    
    protected void doHelp(RenderRequest renderRequest, RenderResponse renderResponse) throws PortletException,
    IOException {
        helpView.include(renderRequest, renderResponse);
    }
    
    protected void doView(RenderRequest renderRequest, RenderResponse renderResponse) throws IOException,
            PortletException {
        
        if (WindowState.MINIMIZED.equals(renderRequest.getWindowState())) {
            return;
        } else if (WindowState.NORMAL.equals(renderRequest.getWindowState())) {
            try {
                doBundlesView(renderRequest, renderResponse);
            } catch (Exception e) {
                e.printStackTrace();
                throw new PortletException(e);
            }
            normalView.include(renderRequest, renderResponse);
        } else {
            maximizedView.include(renderRequest, renderResponse);
        }
    }



    private void doBundlesView(RenderRequest request, RenderResponse response) {

        //get bundleContext
        BundleContext bundleContext = (BundleContext) request.getPortletSession().getPortletContext().getAttribute("osgi-bundlecontext");
        
        //get the StartLeval object
        ServiceReference startLevelRef = bundleContext.getServiceReference(StartLevel.class.getCanonicalName());
        StartLevel startLevelService = (StartLevel) bundleContext.getService(startLevelRef);
        
        //get all bundles
        Bundle[] bundles = bundleContext.getBundles();

        // list contains bundles converted from Bundle objects
        ArrayList<OSGiBundle> OSGiBundleList = new ArrayList<OSGiBundle>();

        Set<Long> configedBundleIds = getConfigedBundleIds();
        
        // convert Bundle to OSGiBundle
        for (Bundle bundle : bundles) {
            if (!checkSysBundle(bundle, startLevelService, configedBundleIds)){
                OSGiBundle osgiBundle = new OSGiBundle(bundle);
                OSGiBundleList.add(osgiBundle);
            }
        }        
        
        try {
            JSONObject grid = new BundleGridJSONObject(OSGiBundleList);
            request.setAttribute("GridJSONObject", grid);
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            bundleContext.ungetService(startLevelRef);
        }

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
    
    private boolean checkSysBundle(Bundle bundle, StartLevel startLevelService, Set<Long> configedBundleIds){
        
        // check start level
        if (startLevelService!=null && startLevelService.getBundleStartLevel(bundle) <= 50){ //config.properties set karaf.systemBundlesStartLevel=50
            return true;
        }
        
        // check configuration bundle
        if (configedBundleIds.contains(bundle.getBundleId())){
            return true;
        }
        
        // check symbolic name
        String symbolicName = bundle.getSymbolicName();
        if (symbolicName!=null){
            if (symbolicName.indexOf("sample") != -1 
                || symbolicName.indexOf("demo") != -1 
                || symbolicName.indexOf("test") != -1 
                || symbolicName.indexOf("example") != -1 
                ){
                return false;
            }
                
            if (symbolicName.indexOf("org.apache.geronimo.modules") != -1
                || symbolicName.indexOf("org.apache.geronimo.configs") != -1
                || symbolicName.indexOf("org.apache.geronimo.specs") != -1
                || symbolicName.indexOf("org.apache.geronimo.bundles") != -1
                || symbolicName.indexOf("org.apache.geronimo.ext") != -1
                || symbolicName.indexOf("org.apache.geronimo.plugins") != -1
                || symbolicName.indexOf("org.apache.geronimo.framework") != -1
                || symbolicName.indexOf("org.apache.geronimo.javamail") != -1
                || symbolicName.indexOf("org.apache.geronimo.components") != -1
                || symbolicName.indexOf("org.apache.xbean") != -1
                || symbolicName.indexOf("org.apache.portals") != -1
                || symbolicName.indexOf("org.apache.yoko") != -1
                || symbolicName.indexOf("org.apache.openejb") != -1
                || symbolicName.indexOf("org.apache.openjpa") != -1
                || symbolicName.indexOf("org.apache.bval") != -1
                || symbolicName.indexOf("org.apache.myfaces") != -1
                || symbolicName.indexOf("org.apache.wink") != -1
                || symbolicName.indexOf("org.apache.ws") != -1
                || symbolicName.indexOf("openwebbeans") != -1
                || symbolicName.indexOf("org.apache.aries") != -1
                || symbolicName.indexOf("org.tranql") != -1
                || symbolicName.indexOf("org.apache.commons") != -1

                ){
                return true;
            }
        }
        
        return false;
        
        
    }
    
    private boolean checkBundleState(BundleContext bundleContext, Long id, int targetState) throws InterruptedException{
        for (int j = 0; j<5; j++){ // give system 5 times to check the new status
            if (bundleContext.getBundle(id).getState() == targetState){
                return true;
            } else {
                Thread.sleep(500L);
            }
        }
        return false;
    }
    
    private boolean checkBundleUninstalled(BundleContext bundleContext, Long id) throws InterruptedException{
        for (int j = 0; j<5; j++){ // give system 5 times to check the new status
            if (bundleContext.getBundle(id) == null){
                return true;
            } else {
                Thread.sleep(500L);
            }
        }
        return false;
    }

    public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
        
        BundleContext bundleContext = (BundleContext) request.getPortletSession().getPortletContext().getAttribute("osgi-bundlecontext");
        
        String resourceId = request.getResourceID();
  
        if (resourceId.equals("bundlesAction")) {
            
            String jsonData = request.getParameter("bundlesActionParam");
            try {
                /*
                 * The format of a request json looks like { "action":"start", "id":25}
                 */
                JSONObject jo = new JSONObject(jsonData);
                String action = jo.getString("action");
                Long id = Long.valueOf(jo.getString("id"));

                /*
                 * The format of a response json looks like { "items":[ {"id":0, "state":"Active"} {"id":25, "state":"Resolved"} ] }
                 * PS:  {"id":0, "state":"err"} means the state maybe not change, need user refresh the portlet.
                 */
                JSONObject resultItem = new JSONObject();;
                
                if (action.equals("start")){
                    // start the bundle
                    bundleContext.getBundle(id).start();

                    // prepare response
                    resultItem.put("id", id);
                    resultItem.put("state", "err");
                    if (checkBundleState(bundleContext, id, Bundle.ACTIVE)) {
                        resultItem.put("state", OSGiBundle.getStateName(Bundle.ACTIVE));
                    }

                } else if (action.equals("stop")){
                    // stop the bundle
                    bundleContext.getBundle(id).stop();

                    // prepare response
                    resultItem.put("id", id);
                    resultItem.put("state", "err");
                    if (checkBundleState(bundleContext, id, Bundle.RESOLVED)) {
                        resultItem.put("state", OSGiBundle.getStateName(Bundle.RESOLVED));
                    }
     
                } else if (action.equals("uninstall")){
                    
                    // uninstall the bundle
                    bundleContext.getBundle(id).uninstall();

                    // prepare response
                    resultItem.put("id", id);
                    resultItem.put("state", "err");
                    if (checkBundleUninstalled(bundleContext, id)) {
                        resultItem.put("state", OSGiBundle.getStateName(Bundle.UNINSTALLED));
                    }

                } else if (action.equals("refresh")){
                    // prepare response
                    resultItem.put("id", id);
                    if (bundleContext.getBundle(id)!=null){
                        resultItem.put("state", OSGiBundle.getStateName(bundleContext.getBundle(id).getState()));
                    } else {
                        resultItem.put("state", OSGiBundle.getStateName(Bundle.UNINSTALLED));
                    }
                }   
                
                PrintWriter writer = response.getWriter();
                writer.print(resultItem);
                
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (BundleException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            
        } else if (resourceId.equals("showManifest")) {
            String str_id = request.getParameter("id");
            long id = Long.parseLong(str_id);

            try {
                PrintWriter writer = response.getWriter();
                writer.print(new ManifestGridJSONObject(bundleContext.getBundle(id).getHeaders()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            
        } else if (resourceId.equals("showWiredBundles")) {
            String str_id = request.getParameter("id");
            long id = Long.parseLong(str_id);
            Bundle bundle = bundleContext.getBundle(id);
            
            ServiceReference reference = bundle.getBundleContext().getServiceReference(PackageAdmin.class.getName());
            try {
                
                PackageAdmin packageAdmin = (PackageAdmin) bundle.getBundleContext().getService(reference);
                
                //importing
                Set<PackageBundlePair> importingPairs =  getImportingPairs(packageAdmin, bundle);

                //importingPairs may have same values, so we override the equals method in PackageBundlePairJSONObject
                Set<PackageBundlePairJSONObject> importingPairSet = new HashSet<PackageBundlePairJSONObject>();
                
                int i=0;
                for (PackageBundlePair pair : importingPairs) {
                    
                    String pname = pair.exportedPackage.getName() + " (Version="+ pair.exportedPackage.getVersion()+")";
                    String bname = pair.bundle.getSymbolicName() + " (id=" + pair.bundle.getBundleId() + ")";
                    
                    importingPairSet.add(new PackageBundlePairJSONObject(i,pname,bname));
                    
                    i++;
                }
                PackageBundlePairGridJSONObject importingPairGrid = new PackageBundlePairGridJSONObject(importingPairSet);
                
                
                //exporting
                Set<PackageBundlePair> exportingPairs = getExportingPairs(packageAdmin, bundle);
                
                Set<PackageBundlePairJSONObject> exportingPairSet = new HashSet<PackageBundlePairJSONObject>();
                
                int j=0;
                for (PackageBundlePair pair : exportingPairs) {
                    
                    String pname = pair.exportedPackage.getName() + " (Version="+ pair.exportedPackage.getVersion()+")";
                    String bname = pair.bundle.getSymbolicName() + " (id=" + pair.bundle.getBundleId() + ")";
                    
                    exportingPairSet.add(new PackageBundlePairJSONObject(j, pname,bname));
                    
                    j++;
                }
                PackageBundlePairGridJSONObject exportingPairGrid = new PackageBundlePairGridJSONObject(exportingPairSet);
                
                
                //construct result
                WiredBundlesJSONObject wb = new WiredBundlesJSONObject(importingPairGrid,exportingPairGrid);

                PrintWriter writer = response.getWriter();
                writer.print(wb);
                
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                bundle.getBundleContext().ungetService(reference);
            }
                
                
        } else if (resourceId.equals("installAction")) {
            String result = processInstallAction(new ActionResourceRequest(request), bundleContext);
            PrintWriter writer = response.getWriter();
            writer.print(result);
            
            
            
        } else if (resourceId.equals("showSysBundles")) {
            
            //get the StartLeval object
            ServiceReference startLevelRef = bundleContext.getServiceReference(StartLevel.class.getCanonicalName());
            StartLevel startLevelService = (StartLevel) bundleContext.getService(startLevelRef);
            
            Bundle[] bundles = bundleContext.getBundles();

            // list contains bundles converted from Bundle objects
            ArrayList<OSGiBundle> OSGiBundleList = new ArrayList<OSGiBundle>();

            Set<Long> configedBundleIds = getConfigedBundleIds();
            
            // convert Bundle to OSGiBundle
            for (Bundle bundle : bundles) {
                if (checkSysBundle(bundle, startLevelService, configedBundleIds)){
                    OSGiBundle osgiBundle = new OSGiBundle(bundle);
                    OSGiBundleList.add(osgiBundle);
                }
            }
            
            try {
                JSONObject grid = new BundleGridJSONObject(OSGiBundleList);
                PrintWriter writer = response.getWriter();
                writer.print(grid);
            } catch (JSONException e) {
                e.printStackTrace();
            } finally{
                bundleContext.ungetService(startLevelRef);
            }
            
            
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
    
    class PackageBundlePair {
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
    
    private String processInstallAction(ActionRequest request, BundleContext bundleContext) throws PortletException, IOException {
        if (!PortletFileUpload.isMultipartContent(request)) {
            throw new PortletException("Expected file upload");
        }

        // use commons-fileupload to process the request
        File rootDir = new File(System.getProperty("java.io.tmpdir"));
        PortletFileUpload uploader = new PortletFileUpload(new DiskFileItemFactory(10240, rootDir));
        File bundleFile = null;
        String startAfterInstalled = null;
        String str_startLevel = null;

        try {
            List<?> items = uploader.parseRequest(request);
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
                            throw new PortletException(e);
                        }
                    } else {
                        return null;
                    }
                } else {
                    if ("startAfterInstalled".equalsIgnoreCase(item.getFieldName())) {
                        startAfterInstalled = item.getString();
                    } else if ("startLevel".equalsIgnoreCase(item.getFieldName())) {
                        str_startLevel = item.getString();
                    }
                }
            }

        } catch (FileUploadException e) {
            throw new PortletException(e);
        }

        // install the uploaded bundle file
        String url = "file:///" + bundleFile.getCanonicalPath();
        try {
            Bundle installedBundle =  bundleContext.installBundle(url);
            
            //get the StartLeval object
            ServiceReference startLevelRef = bundleContext.getServiceReference(StartLevel.class.getCanonicalName());
            StartLevel startLevelService = (StartLevel) bundleContext.getService(startLevelRef);
            
            int defaultStartLevel = startLevelService.getInitialBundleStartLevel(); 
            int startLevel = -1;
            try {
                startLevel = Integer.parseInt(str_startLevel);
            } catch (NumberFormatException e) {
                // if can't generated, use the default initialBundleStartLevel
            }
            if (startLevel != defaultStartLevel && startLevel != -1) {
                startLevelService.setBundleStartLevel(installedBundle, startLevel);
            }

            // if check box "Start" checked, then start the bundle
            if ("yes".equals(startAfterInstalled)) {
                installedBundle.start();
            }

            // response the installed bundle's json data
            /*
             * The format of the json data looks like {"id":0, "symbolicName":"a.b.c", "version":"1.0.0", "state":"Active"}
             */
            String jsonStr = "<textarea>" + "{\"id\":" + installedBundle.getBundleId() + ", \"symbolicName\":\"" + installedBundle.getSymbolicName()
                    + "\", \"version\":\"" + installedBundle.getVersion().toString() + "\", \"state\":\"" + OSGiBundle.getStateName(installedBundle.getState()) + "\"}" + "</textarea>";

            return jsonStr;
        } catch (Exception e) {
            throw new PortletException(e);
        }
    }
}
