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

package org.apache.geronimo.console.jmsmanager.activemqCF;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.console.util.ObjectNameConstants;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ListableRepository;

import javax.portlet.PortletRequest;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

public class ActiveMQConnectorHelper {
    //todo: this class is horrible and needs to be burned!
    private static final Logger log = LoggerFactory.getLogger(ActiveMQConnectorHelper.class);

    private static String MODULE_FILE;

    private final static String ACTIVEMQ_RAR = "repository/activemq/rars/activemq-ra-3.2.1.rar";

    private static final String LINE_SEP = System.getProperty("line.separator");

    private static final String PLAN_TEMPLATE = getPlanTemplate();

    private static final String[] DEPLOYER_ARGS = { File.class.getName(),
            File.class.getName() };

    private static final String DEPLOY_METHOD = "deploy";

    private static String getPlanTemplate() {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\"?>\n");
        sb
                .append("<connector xmlns=\"http://geronimo.apache.org/xml/ns/j2ee/connector-1.0\"\n");
        sb.append("    configId=\"{0}\" parentId=\"{1}\">\n");
        sb.append("  <resourceadapter>\n");
        sb.append("    <resourceadapter-instance>\n");
        sb.append("      <resourceadapter-name>{2}</resourceadapter-name>\n");
        sb
                .append("      <config-property-setting name=\"ServerUrl\">{3}</config-property-setting>\n");
        sb
                .append("      <config-property-setting name=\"UserName\">{4}</config-property-setting>\n");
        sb
                .append("      <config-property-setting name=\"Password\">{5}</config-property-setting>\n");
        sb
                .append("      <workmanager><gbean-link>DefaultWorkManager</gbean-link></workmanager>\n");
        sb.append("    </resourceadapter-instance>\n");
        sb.append("    <outbound-resourceadapter>\n");
        sb.append("      <connection-definition>\n");
        sb
                .append("        <connectionfactory-interface>javax.jms.ConnectionFactory</connectionfactory-interface>\n");
        sb.append("        <connectiondefinition-instance>\n");
        sb.append("          <name>{6}</name>\n");
        sb
                .append("          <implemented-interface>javax.jms.QueueConnectionFactory</implemented-interface>\n");
        sb
                .append("          <implemented-interface>javax.jms.TopicConnectionFactory</implemented-interface>\n");
        sb.append("          <connectionmanager>\n");
        sb.append("            <xa-transaction>\n");
        sb.append("              <transaction-caching/>\n");
        sb.append("            </xa-transaction>\n");
        sb.append("            <single-pool>\n");
        sb.append("              <max-size>{7}</max-size>\n");
        sb
                .append("              <blocking-timeout-milliseconds>{8}</blocking-timeout-milliseconds>\n");
        sb.append("              <match-one/>\n");
        sb.append("            </single-pool>\n");
        sb.append("          </connectionmanager>\n");
        sb.append("        </connectiondefinition-instance>\n");
        sb.append("      </connection-definition>\n");
        sb.append("    </outbound-resourceadapter>\n");
        sb.append("  </resourceadapter>\n");
        sb.append("</connector>\n");

        return sb.toString();
    }

    private void savePlan(File f, Object[] args) {
        MessageFormat mf = new MessageFormat(PLAN_TEMPLATE);
        String plan = mf.format(args);

        try {
            f.createNewFile();
            FileOutputStream fos = new FileOutputStream(f);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            Writer out = new BufferedWriter(osw);
            out.write(plan);
            out.flush();
            out.close();
            osw.close();
            fos.close();
        } catch (Exception e) {
            log.error("Problem creating the plan file", e);
        }
    }

    public void deployPlan(PortletRequest request, Object[] args) {
        try {
            File file = File.createTempFile("console-jms-connector-plan-", ".xml");
            file.deleteOnExit();
            savePlan(file, args);
            if(MODULE_FILE == null) {
                MODULE_FILE = PortletManager.getCurrentServer(request).getServerInfo().resolvePath(ACTIVEMQ_RAR);
            }
            deployPlan(new File(MODULE_FILE), file);
        } catch (IOException e) {
            log.error("Unable to write deployment plan", e);
        }
    }

    public void deployPlan(File moduleFile, File planFile) {
        try {
            Kernel kernel = KernelRegistry.getSingleKernel();
            List list = (List) kernel.invoke(ObjectNameConstants.DEPLOYER_OBJECT_NAME, DEPLOY_METHOD,
                    new Object[] {moduleFile, planFile}, DEPLOYER_ARGS);
            ConfigurationManager configurationManager = ConfigurationUtil
                    .getConfigurationManager(kernel);
            for (Iterator iterator = list.iterator(); iterator.hasNext();) {
                Artifact configID = Artifact.create((String)iterator.next());
                if (!configurationManager.isLoaded(configID)) {
                    configurationManager.loadConfiguration(configID);
                }

                configurationManager.startConfiguration(configID);
            }
        } catch (DeploymentException e) {
            StringBuilder buf = new StringBuilder(256);
            Throwable cause = e;
            while (cause != null) {
                buf.append(cause.getMessage());
                buf.append(LINE_SEP);
                cause = cause.getCause();
            }
            log.error("Problem deploying the ActiveMQ connector: " + buf);
        } catch (URISyntaxException e) {
            log.error("Newly installed app has invalid config ID", e);
        } catch (Exception e) {
            log.error("Problem creating the datasource", e);
        }
    }

    public List getDependencies(PortletRequest request) {
        ListableRepository[] repo = PortletManager.getCurrentServer(request).getRepositories();
        List dependencies = new ArrayList();
        for (int i = 0; i < repo.length; i++) {
            ListableRepository repository = repo[i];
            SortedSet artifacts = repository.list();
            for (Iterator iterator = artifacts.iterator(); iterator.hasNext();) {
                Artifact artifact = (Artifact) iterator.next();
                dependencies.add(artifact.toString());
            }
        }

        return dependencies;
    }
}