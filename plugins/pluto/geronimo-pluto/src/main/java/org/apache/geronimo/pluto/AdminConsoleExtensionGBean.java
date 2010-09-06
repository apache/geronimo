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
package org.apache.geronimo.pluto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.pluto.impl.PageConfig;
import org.apache.pluto.driver.services.portal.PortletWindowConfig;
import org.apache.pluto.driver.services.portal.RenderConfigService;
import org.apache.pluto.driver.services.portal.admin.RenderConfigAdminService;

/* GBean for adding an item to the Administration Console's Navigator.  Web apps
 * that contain portlets can add those portlets to the navigator by including
 * a GBean in their deployment plans like this:
 * 
 *  <gbean name="MyAdminConsoleExtension" class="org.apache.geronimo.pluto.AdminConsoleExtensionGBean">
 *    <attribute name="pageTitle">Hello</attribute>
 *    <attribute name="portletContext">/HelloWorldPortlet</attribute>
 *    <attribute name="portletList">[portlet_id_1, portlet_id_2]</attribute>
 *  </gbean>
 */
public class AdminConsoleExtensionGBean implements GBeanLifecycle {
    private static final Logger log = LoggerFactory.getLogger(AdminConsoleExtensionGBean.class);
    public static final GBeanInfo GBEAN_INFO;
    private final String pageTitle;
    private final String portletContext;
    private final ArrayList<String> portletList;
    private final String icon;
    private final PortalContainerServices portletContainerServices;
    private final String mode;
    private final static Set modes=new HashSet();
    static{
        modes.add("advanced");
        modes.add("basic");
    }
    /* Constructor for GBean */
    public AdminConsoleExtensionGBean(String pageTitle, String portletContext, ArrayList<String> portletList, String icon, PortalContainerServices portalContainerServices,String mode) {
        super();
        this.pageTitle = pageTitle;
        this.portletContext = portletContext;
        this.portletList = portletList;
        this.icon = icon;
        this.portletContainerServices = portalContainerServices;
        if(mode==null){
            log.debug("you did not specifiy a mode for GBean "+pageTitle+",  the system will use advanced as the default mode ");
            this.mode="advanced";
        }else if(modes.contains(mode)){
            this.mode=mode;
        }else{
            log.warn("when create GBean "+pageTitle+" error :your mode " + mode +" is illegal, the system will use advanced as the default mode ");
            this.mode="advanced";
        }
    }

    /*
     * Add the PageConfig to pluto.  This will overwrite any existing pages with the same page name
     * @see org.apache.geronimo.pluto.PlutoAccessInterface#addPage(org.apache.pluto.driver.services.portal.PageConfig)
     */
    private void addPage(PageConfig pageConfig) {
        //Add the new PageConfig to Pluto
        RenderConfigAdminService renderConfig = portletContainerServices.getAdminConfiguration().getRenderConfigAdminService();
        renderConfig.addPage(pageConfig);
    }

    /*
    * This will add the portlets to the PageConfig in Pluto.
    * @see org.apache.geronimo.pluto.PlutoAccessInterface#addPortlets(java.lang.String, java.lang.String, java.util.ArrayList)
    */
    private void addPortlets() {
        if (pageExists()) {
            PageConfig pageConfig = getPageConfigFromPluto();
            int portletCount = portletList.size();
            for (int i = 0; i < portletCount; i++) {
                pageConfig.addPortlet(portletContext, portletList.get(i));
            }
        } else {
            log.warn("Cannot add portlets to non-existent page " + pageTitle);
        }
    }

    /*
     * Removes a PageConfig object in Pluto with the pageTitle
     * @see org.apache.geronimo.pluto.PlutoAccessInterface#removePage(java.lang.String)
     */
    private void removePage() {
        //all we really need is a PageConfig with the page name
        PageConfig pageConfig = createPageConfig();

        RenderConfigAdminService renderConfig = portletContainerServices.getAdminConfiguration().getRenderConfigAdminService();

        //This removePage method was added into Pluto as a patch (PLUTO-387). addPage functionality
        //was available, but removePage functionality was unavailable.
        //NOTE: getList returns a copy of the Map that stores page data in List form. Since it's a copy,
        //it does not serve our purpose, and since no simple workaround was available, the simpler
        //solution was just to try to patch the code.
        renderConfig.removePage(pageConfig);
    }

    /*
    * Removes the portletList from the PageConfig in Pluto
    * @see org.apache.geronimo.pluto.PlutoAccessInterface#removePortlets(java.lang.String, java.util.ArrayList)
    */
    private void removePortlets() {
        if (pageExists()) {
            PageConfig pageConfig = getPageConfigFromPluto();
            int portletCount = portletList.size();
            Collection<String> list = pageConfig.getPortletIds();

            //run through the list of portlets to remove
            for (int i = 0; i < portletCount; i++) {
                String portletName = portletList.get(i);

                //run through the list of portlets on this page and see if we can find a match
                for (String pid : list) {
                    String pletContext = PortletWindowConfig.parseContextPath(pid);
                    String pletName = PortletWindowConfig.parsePortletName(pid);
                    if (portletContext.equals(pletContext) && portletName.equals(pletName)) {
                        pageConfig.removePortlet(pid);
                        break;
                    }
                }
            }
        } else {
            log.warn("can't remove portlets from non-existent page " + pageTitle);
        }
    }

    /*
     * Creates a new PageConfig object with this GBean's pageTitle
     */
    private PageConfig createPageConfig() {
        //Create a new PageConfig
        PageConfig pageConfig = new PageConfig();
        pageConfig.setName(pageTitle);
        pageConfig.setUri("/WEB-INF/themes/default-theme.jsp");
        pageConfig.setIcon(icon);
        pageConfig.setMode(mode);
        return pageConfig;
    }

    /*
    * Gets the PageConfig object from Pluto with that name
    */
    private PageConfig getPageConfigFromPluto() {
        RenderConfigService service = portletContainerServices.getRenderConfigService();

        return (PageConfig) service.getPage(pageTitle);
    }

    /*
    * return true if Pluto contains a PageConfig with that name, else false
    */
    private boolean pageExists() {
        if (pageTitle == null) return false;

        RenderConfigService service = portletContainerServices.getRenderConfigService();

        List<PageConfig> pageConfigs = service.getPages();
        for (PageConfig pageConfig : pageConfigs) {
            if (pageTitle.equals(pageConfig.getName())) {
                return true;
            }
        }

        return false;
    }

    /*
    * returns true if Pluto contains a PageConfig with that name and it has 0 or if there isn't a page with that name
    */
    private boolean pageIsEmpty() {
        if (pageExists()) {
            PageConfig pageConfig = getPageConfigFromPluto();
            return pageConfig.getPortletIds().size() == 0;
        } else {
            log.debug("no pageConfig found for " + pageTitle);
            return true;
        }
    }

    /*
    * Called when the GBean is started
    *   This adds/updates a Page in Pluto according to this GBean's specifications (ACE)
    * @see org.apache.geronimo.gbean.GBeanLifecycle#doStart()
    */
    public synchronized void doStart() throws Exception {
        portletContainerServices.waitForInitialization(60 * 1000);
        
        // check to make sure that a portal driver has registered with the container services
        if (portletContainerServices.getAdminConfiguration() == null) {
            throw new RuntimeException("No portal driver has been registered with the portal container services");
        }

        //add the page if it doesn't exist yet
        if (!pageExists()) {
            //create a PageConfig
            PageConfig newPageConfig = createPageConfig();
            addPage(newPageConfig);
        }

        //add portlets to this newly created page
        addPortlets();
        log.debug("Started AdminConsoleExtensionGBean for " + pageTitle);
    }

    /*
     * Called when the GBean is stopped
     *   This removes/updates a Page in Pluto according to this GBean's specifications
     * @see org.apache.geronimo.gbean.GBeanLifecycle#doStop()
     */
    public synchronized void doStop() throws Exception {
        try {
            //remove portlets from the page
            removePortlets();

            //if the page has 0 portlets on it, then go ahead and remove it
            if (pageIsEmpty()) {
                removePage();
            }
        } catch (NullPointerException e) {
            // during normal server shutdown the portal driver has been shut down before
            // the admin console extensions.  the way that pluto is currently implemented
            // is that when the portal driver shuts down it destroys all its services.
            // this leads to an NPE when you try to use them.  currently there is no
            // way to check to see if a service has been shut down.
            log.debug("could not remove portlets for " + pageTitle, e);
        }

        log.debug("Stopped AdminConsoleExtensionGBean for " + pageTitle);
    }

    /*
    * Called when the GBean fails
    * @see org.apache.geronimo.gbean.GBeanLifecycle#doFail()
    */
    public synchronized void doFail() {
        log.warn("AdminConsoleExtensionGBean for " + pageTitle + " failed.");
    }

    /*
    * Standard GBean information
    */
    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder("AdminConsoleExtensionGBean", AdminConsoleExtensionGBean.class);
        infoFactory.addAttribute("pageTitle", String.class, true, true);
        infoFactory.addAttribute("portletContext", String.class, true, true);
        infoFactory.addAttribute("portletList", ArrayList.class, true, true);
        infoFactory.addAttribute("icon", String.class, true, true);
        infoFactory.addAttribute("mode", String.class, true, true);
        infoFactory.addReference("PortalContainerServices", PortalContainerServices.class, "GBean");
        infoFactory.setConstructor(new String[]{
                "pageTitle",
                "portletContext",
                "portletList",
                "icon", 
                "PortalContainerServices","mode"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
