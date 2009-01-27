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

import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.MultiPageAbstractHandler;
import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.console.configcreator.configData.EARConfigData;
import org.apache.geronimo.console.configcreator.configData.EjbConfigData;
import org.apache.geronimo.console.configcreator.configData.WARConfigData;

/**
 * Base class for portlet helpers
 * 
 * @version $Rev$ $Date$
 */
public abstract class AbstractHandler extends MultiPageAbstractHandler {
    private static final Logger log = LoggerFactory.getLogger(AbstractHandler.class);

    protected final static String MODULE_URI_PARAMETER = "moduleURI";

    protected final static String UPLOADED_WAR_URI_PARAMETER = "uploadedWarUri";

    protected final static String DEPLOYMENT_PLAN_PARAMETER = "deploymentPlanXml";

    protected final static String GET_ARCHIVE_MODE = "index";

    // 'mode' of initial portlet must be one of "index" or "list"

    protected final static String ENVIRONMENT_MODE = "environment";

    protected final static String REFERENCES_MODE = "references";

    protected final static String SECURITY_MODE = "security";

    protected final static String DEPENDENCIES_MODE = "dependencies";

    protected final static String DISPLAY_PLAN_MODE = "displayPlan";

    protected final static String DEPLOY_STATUS_MODE = "deployStatus";

    protected final static String EJB_MODE = "ejbPage";

    protected final static String EAR_MODE = "enterpriseApp";

    protected final static String WAR_CONFIG_DATA_ID = "org.apache.geronimo.configcreator.warConfigData";

    protected final static String EJB_JAR_CONFIG_DATA_ID = "org.apache.geronimo.configcreator.ejbJarConfigData";

    public final static String EAR_CONFIG_DATA_ID = "org.apache.geronimo.configcreator.earConfigData";

    protected final static String DATA_PARAMETER = "data";

    protected final static String DEPLOYED_EJBS_PARAMETER = "deployedEjbs";

    protected final static String DEPLOYED_JDBC_CONNECTION_POOLS_PARAMETER = "deployedJdbcConnectionPools";

    protected final static String DEPLOYED_JMS_CONNECTION_FACTORIES_PARAMETER = "deployedJmsConnectionFactories";

    protected final static String DEPLOYED_JMS_DESTINATIONS_PARAMETER = "deployedJmsDestinations";

    protected final static String DEPLOYED_JAVAMAIL_SESSIONS_PARAMETER = "deployedJavaMailSessions";

    protected final static String DEPLOYED_SECURITY_REALMS_PARAMETER = "deployedSecurityRealms";

    protected final static String DEPLOYED_CREDENTIAL_STORES_PARAMETER = "deployedCredentialStores";

    protected final static String COMMON_LIBS_PARAMETER = "commonLibs";

    protected final static String SELECTED_LIBS_PARAMETER = "selectedLibs";

    public AbstractHandler(String mode, String viewName) {
        super(mode, viewName);
    }
    
    public AbstractHandler(String mode, String viewName, BasePortlet portlet) {
        super(mode, viewName, portlet);
    }

    public static class TempConfigModel implements MultiPageModel {
        public TempConfigModel(PortletRequest request) {
        }

        public void save(ActionResponse response, PortletSession session) {
        }
    }

    public WARConfigData setNewWARSessionData(PortletRequest request) {
        WARConfigData configData = new WARConfigData();
        request.getPortletSession().setAttribute(WAR_CONFIG_DATA_ID, configData);
        return configData;
    }

    public WARConfigData getWARSessionData(PortletRequest request) {
        return (WARConfigData) request.getPortletSession().getAttribute(WAR_CONFIG_DATA_ID);
    }

    public EjbConfigData setNewEjbJarSessionData(PortletRequest request) {
        EjbConfigData configData = new EjbConfigData();
        request.getPortletSession().setAttribute(EJB_JAR_CONFIG_DATA_ID, configData);
        return configData;
    }

    public EjbConfigData getEjbJarSessionData(PortletRequest request) {
        return (EjbConfigData) request.getPortletSession().getAttribute(EJB_JAR_CONFIG_DATA_ID);
    }

    public EARConfigData setNewEARSessionData(PortletRequest request) {
        EARConfigData configData = new EARConfigData();
        request.getPortletSession().setAttribute(EAR_CONFIG_DATA_ID, configData, PortletSession.APPLICATION_SCOPE);
        return configData;
    }

    public EARConfigData getEARSessionData(PortletRequest request) {
        return (EARConfigData) request.getPortletSession().getAttribute(EAR_CONFIG_DATA_ID, PortletSession.APPLICATION_SCOPE);
    }
}
