/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.console.car;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.deployment.plugin.factories.DeploymentFactoryWithKernel;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.system.plugin.PluginInstaller;
import org.apache.geronimo.system.plugin.PluginRepositoryList;
import org.apache.geronimo.system.plugin.ServerArchiver;

/**
 * @version $Rev$ $Date$
 */
public class ManagementHelper {

    private final static String PLUGIN_HELPER_KEY = "org.apache.geronimo.console.PluginManagementHelper";
    private final Kernel kernel;
    private PluginInstaller pluginInstaller;
    private ServerArchiver archiver;
    private List<PluginRepositoryList> pluginRepositoryLists;

    public static ManagementHelper getManagementHelper(PortletRequest request) {
        ManagementHelper helper = (ManagementHelper) request.getPortletSession(true).getAttribute(PLUGIN_HELPER_KEY, PortletSession.APPLICATION_SCOPE);
        if (helper == null) {
            Kernel kernel = PortletManager.getKernel();
            helper = new ManagementHelper(kernel);
            request.getPortletSession().setAttribute(PLUGIN_HELPER_KEY, helper, PortletSession.APPLICATION_SCOPE);
        }
        return helper;
    }

    public ManagementHelper(Kernel kernel) {
        this.kernel = kernel;
    }

    public PluginInstaller getPluginInstaller() {
        if (pluginInstaller == null) {
            Set<AbstractName> pluginInstallers = kernel.listGBeans(new AbstractNameQuery(PluginInstaller.class.getName()));
            if (pluginInstallers.size() == 0) {
                throw new IllegalStateException("No plugin installer registered");
            }
            try {
                pluginInstaller = (PluginInstaller) kernel.getGBean(pluginInstallers.iterator().next());
            } catch (GBeanNotFoundException e) {
                throw new IllegalStateException("Plugin installer cannot be retrieved from kernel");
            }
        }
        return pluginInstaller;
    }

    public ServerArchiver getArchiver() {
        if (archiver == null) {
            Set<AbstractName> archivers = kernel.listGBeans(new AbstractNameQuery(ServerArchiver.class.getName()));
            if (archivers.size() == 0) {
                throw new IllegalStateException("No plugin installer registered");
            }
            try {
                archiver = (ServerArchiver) kernel.getGBean(archivers.iterator().next());
            } catch (GBeanNotFoundException e) {
                throw new IllegalStateException("Plugin installer cannot be retrieved from kernel");
            }
        }
        return archiver;
    }

    public List<PluginRepositoryList> getPluginRepositoryLists() {
        if (this.pluginRepositoryLists == null) {
            Set<AbstractName> names = kernel.listGBeans(new AbstractNameQuery(PluginRepositoryList.class.getName()));
            List<PluginRepositoryList> pluginRepositoryLists = new ArrayList<PluginRepositoryList>(names.size());
            for (AbstractName name : names) {
                try {
                    pluginRepositoryLists.add((PluginRepositoryList) kernel.getGBean(name));
                } catch (GBeanNotFoundException e) {
                    //ignore?
                }
            }
            this.pluginRepositoryLists = pluginRepositoryLists;
        }
        return this.pluginRepositoryLists;
    }

    public DeploymentManager getDeploymentManager() {
        DeploymentFactory factory = new DeploymentFactoryWithKernel(kernel);
        try {
            return factory.getDeploymentManager("deployer:geronimo:inVM", null, null);
        } catch (DeploymentManagerCreationException e) {
            //            log.error(e.getMessage(), e);
            return null;
        }
    }
    
    public List<String> getApplicationModuleLists() {
        List<String> apps = new ArrayList<String>();
        Set<AbstractName> gbeans = this.kernel.listGBeans((AbstractNameQuery) null);
        for (Iterator<AbstractName> it = gbeans.iterator(); it.hasNext();) {
            AbstractName name = (AbstractName) it.next();
            if (isApplicationModule(name)) {
                apps.add(name.getNameProperty("name"));
            }
        }
       
        return apps;
        
    }
    
    private static boolean isApplicationModule(AbstractName abstractName) {
        String type = abstractName.getNameProperty("j2eeType");
        String app = abstractName.getNameProperty("J2EEApplication");
        String name = abstractName.getNameProperty("name");
        if (type != null) {
            return (type.equals("WebModule") || type.equals("J2EEApplication") || type.equals("EJBModule") || type.equals("AppClientModule") || type.equals("ResourceAdapterModule")) && !name.startsWith("geronimo/system"); 
        }
        return false;
    }

}
