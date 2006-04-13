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

package org.apache.geronimo.console.jmsmanager.activemqCF;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.console.util.ObjectNameConstants;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.repository.ListableRepository;

import javax.portlet.PortletRequest;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ActiveMQConnectorHelper {
    //todo: this class is horrible and needs to be burned!
    private final static Log log = LogFactory.getLog(ActiveMQConnectorHelper.class);

    private static String MODULE_FILE;

    private final static String ACTIVEMQ_RAR = "repository/activemq/rars/activemq-ra-3.2.1.rar";

    private static final String LINE_SEP = System.getProperty("line.separator");

    private static final String PLAN_TEMPLATE = getPlanTemplate();

    private static final String[] DEPLOYER_ARGS = { File.class.getName(),
            File.class.getName() };

    private static final String DEPLOY_METHOD = "deploy";

    private static String getPlanTemplate() {
        StringBuffer sb = new StringBuffer();
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
            System.out.println("ERROR: Problem creating the plan file");
            e.printStackTrace();
        }
    }

    public void deployPlan(PortletRequest request, Object[] args) {
        try {
            File file = File.createTempFile("console-jms-connector-plan-", ".xml");
            file.deleteOnExit();
            savePlan(file, args);
            if(MODULE_FILE == null) {
                MODULE_FILE = PortletManager.getServerInfo(request).resolvePath(ACTIVEMQ_RAR);
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
                URI configID = URI.create((String)iterator.next());
                if (!configurationManager.isLoaded(configID)) {
                    configurationManager.load(configID);
                }

                configurationManager.loadGBeans(configID);
                configurationManager.start(configID);
            }
        } catch (DeploymentException e) {
            StringBuffer buf = new StringBuffer(256);
            Throwable cause = e;
            while (cause != null) {
                buf.append(cause.getMessage());
                buf.append(LINE_SEP);
                cause = cause.getCause();
            }
            System.out
                    .println("ERROR: Problem deploying the ActiveMQ connector: "
                            + buf.toString());
            e.printStackTrace();
        } catch (URISyntaxException e) {
            System.out
                    .println("ERROR: Newly installed app has invalid config ID");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("ERROR: Problem creating the datasource");
            e.printStackTrace();
        }
    }

    public List getDependencies(PortletRequest request) {
        ListableRepository[] repo = PortletManager.getListableRepositories(request);
        List dependencies = new ArrayList();
        for (int i = 0; i < repo.length; i++) {
            ListableRepository repository = repo[i];
            try {
                URI[] uris = repository.listURIs();
                for (int j = 0; j < uris.length; j++) {
                    URI uri = uris[j];
                    dependencies.add(uri.toString());
                }
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        return dependencies;
    }
}