/**
 *
 * Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.console.jmsmanager.AbstractJMSManager;
import org.apache.geronimo.console.util.ObjectNameConstants;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.DependencyManager;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.jmx.JMXUtil;

public class RemoveDestinationHandler extends AbstractJMSManager implements
        PortletResponseHandler {

    protected static Log log = LogFactory
            .getLog(RemoveDestinationHandler.class);

    public void processAction(ActionRequest request, ActionResponse response)
            throws IOException, PortletException {
        String destinationConfigURIName = request
                .getParameter(DESTINATION_CONFIG_URI);
        try {
            ConfigurationManager configurationManager = ConfigurationUtil
                    .getConfigurationManager(kernel);
            URI destinationConfigURI = new URI(destinationConfigURIName);

            List stores = configurationManager.listStores();
            assert stores.size() == 1 :"Piling one hack on another, this code only works with exactly one store";
            ObjectName storeName = (ObjectName) stores.iterator().next();

            // Unsubscribe topicbrowser before uninstalling the configuration.
            DependencyManager dm = kernel.getDependencyManager();
            //GBeanData topicBrowser = (GBeanData) kernel.invoke(storeName,
            // "getConfiguration", new Object[]{destinationConfigURI}, new
            // String[]{URI.class.getName()});
            GBeanData topicBrowser = kernel.getGBeanData(JMXUtil
                    .getObjectName(ObjectNameConstants.CONFIG_GBEAN_PREFIX
                            + "\"" + destinationConfigURI + "\""));
            java.util.Set children = dm.getChildren(topicBrowser.getName());
            for (Iterator i = children.iterator(); i.hasNext();) {
                ObjectName o = (ObjectName) i.next();
                if ("TopicBrowser".equals(o.getKeyProperty("j2eeType"))) {
                    kernel.invoke(o, "unsubscribe");
                    break;
                }
            }

            // Uninstall configuration
            //kernel.stopConfiguration(destinationConfigURI);
            kernel.stopGBean(JMXUtil
                    .getObjectName(ObjectNameConstants.CONFIG_GBEAN_PREFIX
                            + "\"" + destinationConfigURIName + "\""));
            kernel.invoke(storeName, "uninstall",
                    new Object[] {destinationConfigURI},
                    new String[] {URI.class.getName()});
        } catch (Exception e) {
            log.error("problem removing destination: "
                    + destinationConfigURIName, e);
        }
    }

}
