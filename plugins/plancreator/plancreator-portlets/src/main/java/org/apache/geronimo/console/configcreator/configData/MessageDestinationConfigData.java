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
package org.apache.geronimo.console.configcreator.configData;

import java.util.HashSet;
import java.util.Map;
import java.util.List;

import javax.portlet.PortletRequest;

import org.apache.openejb.jee.JndiConsumer;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.MessageDestination;

import org.apache.geronimo.xbeans.geronimo.naming.GerMessageDestinationType;
import org.apache.geronimo.xbeans.geronimo.web.GerWebAppType;

/**
 * 
 * @version $Rev$ $Date$
 */
public class MessageDestinationConfigData {
    private HashSet<String> dependenciesSet = new HashSet<String>();

    private boolean referenceNotResolved = false;

    public void parseWebDD(JndiConsumer annotatedWebAppDD, GerWebAppType webApp) {
        if (annotatedWebAppDD instanceof WebApp) {
            WebApp webAppDD = (WebApp) annotatedWebAppDD;
            List<MessageDestination> messageDestinations = webAppDD.getMessageDestination();
            for (MessageDestination messageDestination: messageDestinations) {
                String messageDestinationName = messageDestination.getMessageDestinationName();
                GerMessageDestinationType gerMessageDestination = webApp.addNewMessageDestination();
                messageDestination.setMessageDestinationName(messageDestinationName);
                // messageDestination.setAdminObjectLink(messageDestinationName);
            }
        }
    }

    public void readReferencesData(PortletRequest request, GerWebAppType webApp) {
        dependenciesSet.clear();
        Map map = request.getParameterMap();
        int index = 0;
        while (true) {
            String prefix = "messageDestination" + "." + (index) + ".";
            if (!map.containsKey(prefix + JndiRefsConfigData.REF_NAME)) {
                break;
            }
            String referenceLink = request.getParameter(prefix + JndiRefsConfigData.REF_LINK);
            if (isEmpty(referenceLink)) {
                referenceNotResolved = true;
            }
            dependenciesSet.add(JndiRefsConfigData.getDependencyString(referenceLink));
            // TODO setting pattern as below isn't working! why??
            // webApp.getMessageDestinationArray(index).setPattern(createPattern(referenceLink));
            webApp.getMessageDestinationArray(index).setAdminObjectLink(JndiRefsConfigData.createPattern(referenceLink).getName());
            index++;
        }
    }

    public HashSet<String> getDependenciesSet() {
        return dependenciesSet;
    }

    public boolean isReferenceNotResolved() {
        return referenceNotResolved;
    }

    private static boolean isEmpty(String s) {
        return s == null || s.trim().equals("");
    }
}
