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

package org.apache.geronimo.console.jmsmanager.handlers;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.List;

import javax.management.ObjectName;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.console.jmsmanager.AbstractJMSManager;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.kernel.DependencyManager;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;

public class RemoveDestinationHandler extends AbstractJMSManager implements
        PortletResponseHandler {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    public void processAction(ActionRequest request, ActionResponse response)
            throws IOException, PortletException {
        String destinationConfigURIName = request
                .getParameter(DESTINATION_CONFIG_URI);
        try {
            ConfigurationManager configurationManager = ConfigurationUtil
                    .getConfigurationManager(kernel);
            Artifact destinationConfigArtifact = Artifact.create(destinationConfigURIName);
            AbstractName configurationObjectName = Configuration.getConfigurationAbstractName(destinationConfigArtifact);

            List stores = configurationManager.listStores();
            if (stores.isEmpty()) {
                throw new PortletException("No configuration store");
            }
            ObjectName storeName = (ObjectName) stores.get(0);

            // Unsubscribe topicbrowser before uninstalling the configuration.
            DependencyManager dm = kernel.getDependencyManager();
            //GBeanData topicBrowser = (GBeanData) kernel.invoke(storeName,
            // "getConfiguration", new Object[]{destinationConfigURI}, new
            // String[]{URI.class.getName()});
            GBeanData topicBrowser = kernel.getGBeanData(configurationObjectName);
            java.util.Set children = dm.getChildren(topicBrowser.getAbstractName());
            for (Iterator i = children.iterator(); i.hasNext();) {
                ObjectName o = (ObjectName) i.next();
                if ("TopicBrowser".equals(o.getKeyProperty("j2eeType"))) {
                    kernel.invoke(o, "unsubscribe");
                    break;
                }
            }

            // Uninstall configuration
            //kernel.stopConfiguration(destinationConfigURI);
            kernel.stopGBean(configurationObjectName);
            kernel.invoke(storeName, "uninstall",
                    new Object[] {destinationConfigArtifact},
                    new String[] {URI.class.getName()});
        } catch (Exception e) {
            log.error("problem removing destination: "
                    + destinationConfigURIName, e);
        }
    }

}
