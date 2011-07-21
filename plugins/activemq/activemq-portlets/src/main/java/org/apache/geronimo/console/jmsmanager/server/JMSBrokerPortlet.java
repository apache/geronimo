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

package org.apache.geronimo.console.jmsmanager.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.apache.activemq.broker.BrokerService;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.proxy.GeronimoManagedBean;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic list of JMS brokers
 *
 * @version $Rev$ $Date$
 */
public class JMSBrokerPortlet extends BaseJMSPortlet {

    private static final Logger log = LoggerFactory.getLogger(JMSBrokerPortlet.class);

    private PortletRequestDispatcher editView;

    private PortletRequestDispatcher helpView;

    private PortletRequestDispatcher maximizedView;

    private PortletRequestDispatcher normalView;

    private boolean deleteFolder(File folder) {
        for (File file : folder.listFiles()) {
            if (file.isFile())
                file.delete();
            else
                deleteFolder(file);
        }
        return folder.delete();
    }

    public void destroy() {
        helpView = null;
        normalView = null;
        maximizedView = null;
        editView = null;
        super.destroy();
    }

    protected void doCreate(RenderRequest renderRequest, RenderResponse renderResponse) throws IOException,
            PortletException {
        renderRequest.setAttribute("mode", "create");
        String sBrokerTemplateConfigurationXML = loadConfiguratonFileAsString(resolveConfigurationFile(renderRequest,
                "var/activemq/template/activemq-template.xml"));
        renderRequest.setAttribute("configXML", sBrokerTemplateConfigurationXML);
        editView.include(renderRequest, renderResponse);
    }

    protected void doHelp(RenderRequest renderRequest, RenderResponse renderResponse) throws PortletException,
            IOException {
        helpView.include(renderRequest, renderResponse);
    }

    protected void doList(RenderRequest renderRequest, RenderResponse renderResponse) throws IOException,
            PortletException {
        Map<String, BrokerServiceWrapper> brokerServices = getBrokerServices();
        renderRequest.setAttribute("brokers", brokerServices.values());
        if (WindowState.NORMAL.equals(renderRequest.getWindowState())) {
            normalView.include(renderRequest, renderResponse);
        } else {
            maximizedView.include(renderRequest, renderResponse);
        }
    }
    
    /*
    protected void doUpdate(RenderRequest renderRequest, RenderResponse renderResponse) throws IOException,
            PortletException {
        String sBrokerURI = renderRequest.getParameter("brokerURI");
        String sBrokerName = renderRequest.getParameter("brokerName");
        String sBrokerConfigurationXML = null;
        try {
            Kernel kernel = PortletManager.getKernel();
            GBeanData brokerGBeanData = kernel.getGBeanData(new AbstractName(URI.create(sBrokerURI)));
            String sBrokerConfigurationXMLPath = brokerGBeanData.getAttribute("amqBaseDir").toString()
                    + brokerGBeanData.getAttribute("amqConfigFile").toString();
            sBrokerConfigurationXML = loadConfiguratonFileAsString(resolveConfigurationFile(renderRequest,
                    sBrokerConfigurationXMLPath));
        } catch (GBeanNotFoundException e) {
            e.printStackTrace();
        } catch (InternalKernelException e) {
            e.printStackTrace();
        }
        renderRequest.setAttribute("configXML", sBrokerConfigurationXML);
        renderRequest.setAttribute("mode", "update");
        renderRequest.setAttribute("brokerWrapper", getBrokerWrapper(renderRequest, new AbstractName(URI
                .create(sBrokerURI))));
        editView.include(renderRequest, renderResponse);
    }
    */
    
    protected void doView(RenderRequest renderRequest, RenderResponse renderResponse) throws IOException,
            PortletException {
        try {
            if (WindowState.MINIMIZED.equals(renderRequest.getWindowState())) {
                return;
            }
            String mode = renderRequest.getParameter("mode");
            if (mode == null)
                mode = "list";
            renderRequest.setAttribute("mode", mode);
            if (mode.equals("create")) {
                //doCreate(renderRequest, renderResponse);
            } else if (mode.equals("update")) {
                //doUpdate(renderRequest, renderResponse);
            } else {
                doList(renderRequest, renderResponse);
            }
        } catch (Throwable e) {
            addErrorMessage(renderRequest, e.getMessage());
            log.error(e.getMessage(), e);
        }
    }

    public void init(PortletConfig portletConfig) throws PortletException {
        super.init(portletConfig);
        normalView = portletConfig.getPortletContext().getRequestDispatcher(
                "/WEB-INF/view/jmsmanager/server/normal.jsp");
        maximizedView = portletConfig.getPortletContext().getRequestDispatcher(
                "/WEB-INF/view/jmsmanager/server/maximized.jsp");
        helpView = portletConfig.getPortletContext().getRequestDispatcher("/WEB-INF/view/jmsmanager/server/help.jsp");
        editView = portletConfig.getPortletContext().getRequestDispatcher("/WEB-INF/view/jmsmanager/server/edit.jsp");
    }

    protected String loadConfiguratonFileAsString(File brokerConfigFile) throws IOException {
        if (!brokerConfigFile.exists())
            throw new IOException("Can not load the ActiveMQ broker configuration file ["
                    + brokerConfigFile.getAbsolutePath() + "]");
        BufferedReader reader = null;
        StringBuilder configBuilder = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(brokerConfigFile), "iso-8859-1"));
            String sCurrentReadLine = null;
            while ((sCurrentReadLine = reader.readLine()) != null)
                configBuilder.append(sCurrentReadLine).append("\n");
            return configBuilder.toString();
        } finally {
            if (reader != null)
                try {
                    reader.close();
                } catch (Exception e) {
                }
        }
    }

    public void processAction(ActionRequest actionRequest, ActionResponse actionResponse) throws PortletException,
            IOException {
        try {
            String mode = actionRequest.getParameter("mode");
            if (mode == null)
                mode = "list";
            if (mode.equals("start")) {
                processStartAction(actionRequest, actionResponse);
            } else if (mode.equals("stop")) {
                processStopAction(actionRequest, actionResponse);
            } else if (mode.equals("delete")) {
                //processDeleteAction(actionRequest, actionResponse);
            } else if (mode.equals("create")) {
                //processCreateAction(actionRequest, actionResponse);
            } else if (mode.equals("update")) {
                processUpdateAction(actionRequest, actionResponse);
            } else
                actionResponse.setRenderParameter("mode", mode);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            addErrorMessage(actionRequest, e.getMessage());
        }
    }

    /**
     * 1. Save the configuration XML file to /var/activemq/conf with the name ${brokerName}.xml
     * 2. Start the broker GBean     
     * @param actionRequest
     * @param actionResponse
     * @throws PortletException
     * @throws IOException
     */
    /*
    protected void processCreateAction(ActionRequest actionRequest, ActionResponse actionResponse)
            throws PortletException, IOException {
        String sConfigurationXML = actionRequest.getParameter("configXML");
        validateConfigXML(sConfigurationXML, actionRequest);
        String sBrokerName = actionRequest.getParameter("brokerName");
        Kernel kernel = PortletManager.getKernel();
        AbstractName brokerAbstractName = kernel.getNaming().createSiblingName(
                PortletManager.getNameFor(actionRequest, getActiveMQManager(actionRequest)), sBrokerName, "JMSServer");
        validateBrokerName(brokerAbstractName, actionRequest);
        saveConfigurationFile(resolveConfigurationFile(actionRequest, "var/activemq/conf/" + sBrokerName + ".xml"),
                sConfigurationXML);
        GBeanData brokerGBeanData = new GBeanData(brokerAbstractName, BrokerServiceGBeanImpl.class);
        brokerGBeanData.setAttribute("brokerName", sBrokerName);
        brokerGBeanData.setAttribute("amqBaseDir", "var/activemq/");
        brokerGBeanData.setAttribute("amqDataDir", "data/" + sBrokerName);
        brokerGBeanData.setAttribute("amqConfigFile", "conf/" + sBrokerName + ".xml");
        brokerGBeanData.setAttribute("useShutdownHook", false);
        brokerGBeanData.setReferencePattern("ServerInfo", new AbstractNameQuery(null, Collections.EMPTY_MAP,
                ServerInfo.class.getName()));
        brokerGBeanData.setReferencePattern("MBeanServerReference", new AbstractNameQuery(null, Collections.EMPTY_MAP,
                MBeanServerReference.class.getName()));
        try {
            JMSBroker jmsBroker = getActiveMQManager(actionRequest).addBroker(sBrokerName, brokerGBeanData);
            ((GeronimoManagedBean) jmsBroker).startRecursive();
            addInfoMessage(actionRequest, getLocalizedString(actionRequest, "jmsmanager.broker.successAddBroker",
                    sBrokerName));
        } catch (PortletException e) {
            throw e;
        } catch (Exception e) {
            throw new PortletException(getLocalizedString(actionRequest, "jmsmanager.broker.failAddBroker",
                    sBrokerName, e.getMessage()), e);
        }
        actionResponse.setRenderParameter("mode", "list");
    }
    */
    
    /**
     * 1. Remove the configuration XML file
     * 2. Remove the broker GBean
     * @param actionRequest
     * @param actionResponse
     * @throws PortletException
     * @throws IOException
     */
    /*
    protected void processDeleteAction(ActionRequest actionRequest, ActionResponse actionResponse)
            throws PortletException, IOException {
        String sBrokerName = actionRequest.getParameter("brokerName");
        String sBrokerURI = actionRequest.getParameter("brokerURI");
        try {
            Kernel kernel = PortletManager.getKernel();
            AbstractName brokerAbstractName = new AbstractName(URI.create(sBrokerURI));
            GBeanData brokerGBeanData = kernel.getGBeanData(brokerAbstractName);
            String sBrokerConfigurationXMLPath = brokerGBeanData.getAttribute("amqBaseDir").toString()
                    + brokerGBeanData.getAttribute("amqConfigFile").toString();
            getActiveMQManager(actionRequest).removeBroker(brokerAbstractName);
            File brokerConfigFile = resolveConfigurationFile(actionRequest, sBrokerConfigurationXMLPath);
            if (!brokerConfigFile.delete()) {
                String sWarningMessage = getLocalizedString(actionRequest, "jmsmanager.broker.failDeleteBrokerConfig",
                        brokerConfigFile.getAbsolutePath());
                log.warn(sWarningMessage);
                addWarningMessage(actionRequest, sWarningMessage);
            } else {
                String sInfoMessage = getLocalizedString(actionRequest, "jmsmanager.broker.successDeleteBrokerConfig",
                        brokerConfigFile.getAbsolutePath());
                log.info(sInfoMessage);
                addInfoMessage(actionRequest, sInfoMessage);
            }
            String sBrokerDataDirectoryPath = brokerGBeanData.getAttribute("amqBaseDir").toString()
                    + brokerGBeanData.getAttribute("amqDataDir").toString();
            File brokerDataDirectory = resolveConfigurationFile(actionRequest, sBrokerDataDirectoryPath);
            if (!deleteFolder(brokerDataDirectory)) {
                String sWarningMessage = getLocalizedString(actionRequest, "jmsmanager.broker.failDeleteBrokerData",
                        brokerDataDirectory.getAbsolutePath());
                log.warn(sWarningMessage);
                addWarningMessage(actionRequest, sWarningMessage);
            } else {
                String sInfoMessage = getLocalizedString(actionRequest, "jmsmanager.broker.successDeleteBrokerData",
                        brokerDataDirectory.getAbsolutePath());
                log.info(sInfoMessage);
                addInfoMessage(actionRequest, sInfoMessage);
            }
            addInfoMessage(actionRequest, getLocalizedString(actionRequest, "jmsmanager.broker.successDeleteBroker",
                    sBrokerName));
        } catch (GBeanNotFoundException e) {
            throw new PortletException(getLocalizedString(actionRequest, "jmsmanager.broker.failFindBroker",
                    sBrokerName, e.getMessage()), e);
        } catch (Exception e) {
            throw new PortletException(getLocalizedString(actionRequest, "jmsmanager.broker.failDeleteBroker",
                    sBrokerName, e.getMessage()), e);
        }
        actionResponse.setRenderParameter("mode", "list");
    }
    */
    
    protected void processStartAction(ActionRequest actionRequest, ActionResponse actionResponse)
            throws PortletException, IOException {
        //String sBrokerURI = actionRequest.getParameter("brokerURI");
        String sBrokerName = actionRequest.getParameter("brokerName");
        try {
            Map<String, BrokerServiceWrapper> brokerServices = getBrokerServices();
            BrokerService brokerService = brokerServices.get(sBrokerName).getBrokerService();
            if (brokerService.isStarted()) {
                return;
            }
            brokerService.start(true);
            brokerService.waitUntilStarted();
            if (!brokerService.isStarted()) {
                throw new PortletException(getLocalizedString(actionRequest,
                        "jmsmanager.broker.failStartBrokerNoReason", sBrokerName));
            }
            addInfoMessage(actionRequest, getLocalizedString(actionRequest, "jmsmanager.broker.successStartBroker",
                    sBrokerName));
        } catch (PortletException e) {
            throw e;
        } catch (Exception e) {
            throw new PortletException(getLocalizedString(actionRequest, "jmsmanager.broker.failStartBroker",
                    sBrokerName, e.getMessage()));
        }
        actionResponse.setRenderParameter("mode", "list");
    }

    protected void processStopAction(ActionRequest actionRequest, ActionResponse actionResponse)
            throws PortletException, IOException {
        //String sBrokerURI = actionRequest.getParameter("brokerURI");
        String sBrokerName = actionRequest.getParameter("brokerName");
        try {
            Map<String, BrokerServiceWrapper> brokerServices = getBrokerServices();
            BrokerService brokerService = brokerServices.get(sBrokerName).getBrokerService();
            if (!brokerService.isStarted()) {
                return;
            }
            brokerService.stop();
            brokerService.waitUntilStopped();
            if (brokerService.isStarted()) {
                throw new PortletException(getLocalizedString(actionRequest,
                        "jmsmanager.broker.failStopBrokerNoReason", sBrokerName));
            }
            addInfoMessage(actionRequest, getLocalizedString(actionRequest, "jmsmanager.broker.successStopBroker",
                    sBrokerName));
        } catch (PortletException e) {
            throw e;
        } catch (Exception e) {
            throw new PortletException(getLocalizedString(actionRequest, "jmsmanager.broker.failStopBroker",
                    sBrokerName, e.getMessage()));
        }
        actionResponse.setRenderParameter("mode", "list");
    }

    /**
     * 1. Save the configuration XML file to to /var/activemq/conf with the name ${brokerName}.xml
     * 2. Restart the broker GBean
     * @param actionRequest
     * @param actionResponse
     * @throws PortletException
     * @throws IOException
     */
    protected void processUpdateAction(ActionRequest actionRequest, ActionResponse actionResponse)
            throws PortletException, IOException {
        String sConfigurationXML = actionRequest.getParameter("configXML");
        String sBrokerName = actionRequest.getParameter("brokerName");
        String sBrokerURI = actionRequest.getParameter("brokerURI");
        validateConfigXML(sConfigurationXML, actionRequest);
        Kernel kernel = PortletManager.getKernel();
        try {
            AbstractName brokerAbstractName = new AbstractName(URI.create(sBrokerURI));
            GBeanData brokerGBeanData = kernel.getGBeanData(new AbstractName(URI.create(sBrokerURI)));
            String sBrokerConfigurationXMLPath = brokerGBeanData.getAttribute("amqBaseDir").toString()
                    + brokerGBeanData.getAttribute("amqConfigFile").toString();
            saveConfigurationFile(resolveConfigurationFile(actionRequest, sBrokerConfigurationXMLPath),
                    sConfigurationXML);
            GeronimoManagedBean jmsBroker = PortletManager.getManagedBean(actionRequest, brokerAbstractName);
            if (kernel.isRunning(brokerAbstractName)) {
                jmsBroker.stop();
            }
            jmsBroker.startRecursive();
            if (!kernel.isRunning(brokerAbstractName)) {
                throw new PortletException(getLocalizedString(actionRequest,
                        "jmsmanager.broker.failUpdateBrokerNoReason", sBrokerName));
            }
            addInfoMessage(actionRequest, getLocalizedString(actionRequest, "jmsmanager.broker.successUpdateBroker",
                    sBrokerName));
        } catch (PortletException e) {
            throw e;
        } catch (GBeanNotFoundException e) {
            throw new PortletException(getLocalizedString(actionRequest, "jmsmanager.broker.failFindBroker",
                    sBrokerName, e.getMessage()));
        } catch (Exception e) {
            throw new PortletException(getLocalizedString(actionRequest, "jmsmanager.broker.failUpdateBroker",
                    sBrokerName, e.getMessage()));
        }
        actionResponse.setRenderParameter("mode", "list");
    }

    protected File resolveConfigurationFile(PortletRequest portletRequest, String filePath) {
        ServerInfo serverInfo = PortletManager.getCurrentServer(portletRequest).getServerInfo();
        return serverInfo.resolve(filePath);
    }

    protected void saveConfigurationFile(File brokerConfigFile, String configurationXML) throws IOException {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(brokerConfigFile), "iso-8859-1"));
            writer.write(configurationXML);
        } finally {
            if (writer != null)
                try {
                    writer.close();
                } catch (Exception e) {
                }
        }
    }

    private void validateBrokerName(AbstractName brokerAbName, ActionRequest actionRequest) throws PortletException {
        Configuration brokersConfiguration = PortletManager.getConfigurationManager().getConfiguration(
                brokerAbName.getArtifact());
        Map<AbstractName, GBeanData> abNameGBeanDataMap = (Map<AbstractName, GBeanData>) brokersConfiguration
                .getGBeans();
        String sNewBrokerName = brokerAbName.getNameProperty("name");
        for (AbstractName abName : abNameGBeanDataMap.keySet()) {
            String sGBeanName = abName.getNameProperty("name");
            if (sNewBrokerName.equals(sGBeanName)) {
                throw new PortletException(getLocalizedString(actionRequest, "jmsmanager.broker.invalidBrokerName",
                        sGBeanName));
            }
        }
    }

    private void validateConfigXML(String configXML, ActionRequest actionRequest) throws PortletException {
        if (configXML == null || configXML.trim().length() == 0)
            throw new PortletException(getLocalizedString(actionRequest, "jmsmanager.broker.invalidBrokerConfig"));
    }
}
