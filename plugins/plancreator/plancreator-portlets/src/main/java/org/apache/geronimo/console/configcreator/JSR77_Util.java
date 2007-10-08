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
package org.apache.geronimo.console.configcreator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.portlet.PortletRequest;

import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.repository.ListableRepository;
import org.apache.geronimo.management.EJBModule;
import org.apache.geronimo.management.geronimo.JCAAdminObject;
import org.apache.geronimo.management.geronimo.JCAManagedConnectionFactory;
import org.apache.geronimo.management.geronimo.ResourceAdapterModule;
import org.apache.geronimo.management.geronimo.SecurityRealm;

/**
 * Util class for JSR-77 related functions
 * 
 * @version $Rev$ $Date$
 */
public class JSR77_Util {

    public static class ReferredData {
        private String displayName;

        private String patternName;

        public ReferredData(String displayName, String patternName) {
            this.displayName = displayName;
            this.patternName = patternName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getPatternName() {
            return patternName;
        }

        public void setPatternName(String patternName) {
            this.patternName = patternName;
        }
    }

    protected static List getDeployedEJBs(PortletRequest request) {
        List ejbList = new ArrayList();
        EJBModule[] ejbModules = PortletManager.getManagementHelper(request).getEJBModules(
                PortletManager.getCurrentServer(request));
        for (int i = 0; ejbModules != null && i < ejbModules.length; i++) {
            String[] ejbObjectNames = ejbModules[i].getEjbs();
            for (int j = 0; j < ejbObjectNames.length; j++) {
                try {
                    ObjectName objectName = ObjectName.getInstance(ejbObjectNames[j]);
                    String ejbName = objectName.getKeyProperty(NameFactory.J2EE_NAME);
                    String configurationName;
                    if ("null".equalsIgnoreCase(objectName.getKeyProperty(NameFactory.J2EE_APPLICATION))) {
                        configurationName = objectName.getKeyProperty(NameFactory.EJB_MODULE) + "/";
                    } else {
                        configurationName = objectName.getKeyProperty(NameFactory.J2EE_APPLICATION) + "/"
                                + objectName.getKeyProperty(NameFactory.EJB_MODULE);
                    }
                    ReferredData data = new ReferredData(ejbName + " (" + configurationName + ")",
                            configurationName + "/" + ejbName);
                    ejbList.add(data);
                } catch (MalformedObjectNameException e) {
                    // log.error(e.getMessage(), e);
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    // log.error(e.getMessage(), e);
                    e.printStackTrace();
                }
            }
        }
        return ejbList;
    }

    protected static List getJMSConnectionFactories(PortletRequest request) {
        // TODO this is a duplicate of the code from
        // org.apache.geronimo.console.jmsmanager.wizard.ListScreenHandler.populateExistingList()
        // TODO need to eliminate this duplicate code probably by putting it in a common place
        List connectionFactories = new ArrayList();

        // Get the list of connection factories
        ResourceAdapterModule[] modules = PortletManager.getOutboundRAModules(request, new String[] {
                "javax.jms.ConnectionFactory", "javax.jms.QueueConnectionFactory",
                "javax.jms.TopicConnectionFactory", });
        try {
            for (int i = 0; i < modules.length; i++) {
                ResourceAdapterModule module = modules[i];
                String configurationName = PortletManager.getConfigurationFor(request,
                        PortletManager.getNameFor(request, module)).toString()
                        + "/";

                JCAManagedConnectionFactory[] factories = PortletManager.getOutboundFactoriesForRA(request,
                        module, new String[] { "javax.jms.ConnectionFactory", "javax.jms.QueueConnectionFactory",
                                "javax.jms.TopicConnectionFactory", });
                for (int j = 0; j < factories.length; j++) {
                    JCAManagedConnectionFactory factory = factories[j];
                    String factoryName = ObjectName.getInstance(factory.getObjectName()).getKeyProperty(
                            NameFactory.J2EE_NAME);
                    ReferredData data = new ReferredData(factoryName + " (" + configurationName + ")",
                            configurationName + "/" + factoryName);
                    connectionFactories.add(data);
                }
            }
        } catch (MalformedObjectNameException e) {
            // log.error(e.getMessage(), e);
        }
        return connectionFactories;
    }

    protected static List getJMSDestinations(PortletRequest request) {
        // TODO this is a duplicate of the code from
        // org.apache.geronimo.console.jmsmanager.wizard.ListScreenHandler.populateExistingList()
        // TODO need to eliminate this duplicate code probably by putting it in a common place
        List jmsDestinations = new ArrayList();

        // Get the list of connection factories
        ResourceAdapterModule[] modules = PortletManager.getOutboundRAModules(request, new String[] {
                "javax.jms.ConnectionFactory", "javax.jms.QueueConnectionFactory",
                "javax.jms.TopicConnectionFactory", });
        try {
            for (int i = 0; i < modules.length; i++) {
                ResourceAdapterModule module = modules[i];
                String configurationName = PortletManager.getConfigurationFor(request,
                        PortletManager.getNameFor(request, module)).toString()
                        + "/";

                JCAAdminObject[] admins = PortletManager.getAdminObjectsForRA(request, module, new String[] {
                        "javax.jms.Queue", "javax.jms.Topic" });
                for (int j = 0; j < admins.length; j++) {
                    JCAAdminObject admin = admins[j];
                    String destinationName = ObjectName.getInstance(admin.getObjectName()).getKeyProperty(
                            NameFactory.J2EE_NAME);
                    ReferredData data = new ReferredData(destinationName + " (" + configurationName + ")",
                            configurationName + "/" + destinationName);
                    jmsDestinations.add(data);
                }
            }
        } catch (MalformedObjectNameException e) {
            // log.error(e.getMessage(), e);
        }
        return jmsDestinations;
    }

    protected static List getJDBCConnectionPools(PortletRequest request) {
        // TODO this is a duplicate of the code from
        // org.apache.geronimo.console.databasemanager.wizard.DatabasePoolPortlet.populatePoolList()
        // TODO need to eliminate this duplicate code probably by putting it in a common place
        List list = new ArrayList();
        ResourceAdapterModule[] modules = PortletManager.getOutboundRAModules(request, "javax.sql.DataSource");
        for (int i = 0; i < modules.length; i++) {
            ResourceAdapterModule module = modules[i];
            JCAManagedConnectionFactory[] databases = PortletManager.getOutboundFactoriesForRA(request, module,
                    "javax.sql.DataSource");
            for (int j = 0; j < databases.length; j++) {
                JCAManagedConnectionFactory db = databases[j];
                AbstractName dbName = PortletManager.getManagementHelper(request).getNameFor(db);
                String poolName = (String) dbName.getName().get(NameFactory.J2EE_NAME);
                String configurationName = dbName.getArtifact().toString() + "/";
                ReferredData data = new ReferredData(poolName + " (" + configurationName + ")", configurationName
                        + "/" + poolName);
                list.add(data);
            }
        }
        return list;
    }

    protected static List getCommonLibs(PortletRequest request) {
        // TODO this is a duplicate of the code from
        // org.apache.geronimo.console.repository.RepositoryViewPortlet.doView()
        // TODO need to eliminate this duplicate code probably by putting it in a common place
        List list = new ArrayList();
        ListableRepository[] repos = PortletManager.getCurrentServer(request).getRepositories();
        for (int i = 0; i < repos.length; i++) {
            ListableRepository repo = repos[i];
            final SortedSet artifacts = repo.list();
            for (Iterator iterator = artifacts.iterator(); iterator.hasNext();) {
                String fileName = iterator.next().toString();
                list.add(fileName);
            }
        }
        return list;
    }

    public static SecurityRealm[] getDeployedSecurityRealms(PortletRequest request) {
        return PortletManager.getCurrentServer(request).getSecurityRealms();
    }
}
