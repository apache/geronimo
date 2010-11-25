package org.apache.geronimo.console.portlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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
import org.apache.geronimo.console.json.GridJSONObject;
import org.apache.geronimo.console.json.HeadersJSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.startlevel.StartLevel;

public class BundlesPortlet extends GenericPortlet {

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

    protected void doHelp(RenderRequest renderRequest, RenderResponse renderResponse) throws PortletException,
            IOException {
        helpView.include(renderRequest, renderResponse);
    }

    private void doBundlesView(RenderRequest request, RenderResponse response) {

        BundleContext bundleContext = (BundleContext) request.getPortletSession().getPortletContext().getAttribute(
                "osgi-bundlecontext");

        Bundle[] bundles = bundleContext.getBundles();

        // list contains bundles converted from Bundle objects
        ArrayList<OSGiBundle> OSGiBundleList = new ArrayList<OSGiBundle>();

        // convert Bundle to OSGiBundle
        for (Bundle bundle : bundles) {
            OSGiBundle osgiBundle = new OSGiBundle(bundle);
            OSGiBundleList.add(osgiBundle);
        }

        try {
            JSONObject grid = new GridJSONObject(OSGiBundleList);
            request.setAttribute("GridJSONObject", grid);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
        
        BundleContext bundleContext = (BundleContext) request.getPortletSession().getPortletContext().getAttribute(
                "osgi-bundlecontext");
        
        String resourceId = request.getResourceID();
  
        if (resourceId.equals("bundlesAction")) {
            
            String jsonData = request.getParameter("bundlesActionParam");
            try {
                /*
                 * The format of a request json looks like { "action":"start" "items":[ {"id":0} {"id":25} ] }
                 */
                JSONObject jo = new JSONObject(jsonData);
                String action = jo.getString("action");
                JSONArray items = jo.getJSONArray("items");

                /*
                 * The format of a response json looks like { "items":[ {"id":0, "state":"Active"} {"id":25, "state":"Resolved"} ] }
                 * PS:  {"id":0, "state":"err"} means the state maybe not change, need user refresh the portlet.
                 */
                LinkedList<JSONObject> resultItems = new LinkedList<JSONObject>();
                
                if (action.equals("start")){
                    for (int i = 0; i < items.length(); i++) {
                        Long id = items.getJSONObject(i).getLong("id");
                        
                        // start the bundle
                        bundleContext.getBundle(id).start();
                        
                        // prepare response
                        JSONObject resultItem = new JSONObject();
                        resultItem.put("id", id);
                        
                        resultItem.put("state", "err");
                        for (int j = 0; j<5; j++){ // give system 5 times to check the new status
                            if (bundleContext.getBundle(id).getState() == Bundle.ACTIVE){
                                resultItem.put("state", "Active");
                                break;
                            } else {
                                Thread.sleep(500L);
                            }
                        }
                        
                        resultItems.add(resultItem);
                    }
                } else if (action.equals("stop")){
                    for (int i = 0; i < items.length(); i++) {
                        Long id = items.getJSONObject(i).getLong("id");
                        
                        // stop the bundle
                        bundleContext.getBundle(id).stop();
                        
                        // prepare response
                        JSONObject resultItem = new JSONObject();
                        resultItem.put("id", id);
                        
                        resultItem.put("state", "err");
                        for (int j = 0; j<5; j++){ // give system 5 times to check the new status
                            if (bundleContext.getBundle(id).getState() == Bundle.RESOLVED){
                                resultItem.put("state", "Resolved");
                                break;
                            } else {
                                Thread.sleep(500L);
                            }
                        }
                        
                        resultItems.add(resultItem);
                    }
                                        
                } else if (action.equals("uninstall")){
                    for (int i = 0; i < items.length(); i++) {
                        Long id = items.getJSONObject(i).getLong("id");
                        
                        // uninstall the bundle
                        bundleContext.getBundle(id).uninstall();
                        
                        // prepare response
                        JSONObject resultItem = new JSONObject();
                        resultItem.put("id", id);
                        
                        resultItem.put("state", "err");
                        for (int j = 0; j<5; j++){ // give system 5 times to check the new status
                            if (bundleContext.getBundle(id) == null){
                                resultItem.put("state", "Uninstalled");
                                break;
                            } else {
                                Thread.sleep(500L);
                            }
                        }
                        
                        resultItems.add(resultItem);
                    }
                }

                JSONObject result = new JSONObject();
                result.put("items", resultItems);
                
                PrintWriter writer = response.getWriter();
                writer.print(result.toString());
                
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (BundleException e) {
                e.printStackTrace();
            }catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else if (resourceId.equals("showSingleBundle")) {
            String str_id = request.getParameter("id");
            long id = Long.parseLong(str_id);

            try {
                PrintWriter writer = response.getWriter();
                writer.print(new HeadersJSONObject(bundleContext.getBundle(id).getHeaders()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            
        } else if (resourceId.equals("installAction")) {
            String result = processInstallAction(new ActionResourceRequest(request), bundleContext);
            PrintWriter writer = response.getWriter();
            writer.print(result);
            
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
