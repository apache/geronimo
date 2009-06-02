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

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.MultiPageModel;

/**
 * A handler for ...
 * 
 * @version $Rev$ $Date$
 */
public class ReferencesHandler extends AbstractHandler {
    private static final Log log = LogFactory.getLog(ReferencesHandler.class);

    public ReferencesHandler(BasePortlet portlet) {
        super(REFERENCES_MODE, "/WEB-INF/view/configcreator/references.jsp", portlet);
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model)
            throws PortletException, IOException {
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model)
            throws PortletException, IOException {
        WARConfigData data = getSessionData(request);
        request.setAttribute(DATA_PARAMETER, data);
        request.setAttribute(DEPLOYED_EJBS_PARAMETER, JSR77_Util.getDeployedEJBs(request));
        request.setAttribute(DEPLOYED_JDBC_CONNECTION_POOLS_PARAMETER, JSR77_Util.getJDBCConnectionPools(request));
        request.setAttribute(DEPLOYED_JMS_CONNECTION_FACTORIES_PARAMETER, JSR77_Util.getJMSConnectionFactories(request));
        request.setAttribute(DEPLOYED_JMS_DESTINATIONS_PARAMETER, JSR77_Util.getJMSDestinations(request));
        request.setAttribute(DEPLOYED_JAVAMAIL_SESSIONS_PARAMETER, JSR77_Util.getJavaMailSessions(request));
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model)
            throws PortletException, IOException {
        WARConfigData data = getSessionData(request);
        data.readReferencesData(request);
        HashSet dependenciesSet = new HashSet();
        if (processRefs(data.getEjbRefs(), dependenciesSet)
                && processRefs(data.getEjbLocalRefs(), dependenciesSet)
                && processRefs(data.getJdbcPoolRefs(), dependenciesSet)
                && processRefs(data.getJavaMailSessionRefs(), dependenciesSet)
                && processRefs(data.getJmsConnectionFactoryRefs(), dependenciesSet)
                && processRefs(data.getJmsDestinationRefs(), dependenciesSet)) {
            data.getDependencies().clear();
            data.getDependencies().addAll(dependenciesSet);
            if (data.getSecurity() != null) {
                return SECURITY_MODE + "-before";
            }
            return DEPENDENCIES_MODE + "-before";
        }
        data.setReferenceNotResolved(true);
        portlet.addErrorMessage(request, portlet.getLocalizedString(request, "errorMsg03"));
        return getMode() + "-before";
    }

    private boolean processRefs(List refList, HashSet dependenciesSet) {
        for (int i = 0; i < refList.size(); i++) {
            ReferenceData referenceData = (ReferenceData) refList.get(i);
            String referenceLink = referenceData.getRefLink();
            if (referenceLink == null || referenceLink.length() <= 0) {
                return false;
            }
            dependenciesSet.add(JSR88_Util.getDependencyString(referenceLink));
        }
        return true;
    }
}
