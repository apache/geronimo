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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.management.ObjectName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.console.util.ObjectNameConstants;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.jmx.JMXUtil;

public class ActiveMQConnectorHelper {

    private final static File REPO_FOLDER;

    private final static String PLAN_FILE;

    private final static String MODULE_FILE;

    private final static String ACTIVEMQ_RA = "/activemq/rars/activemq-ra-3.2.rar";

    private final static String PLAN_XML = "/activemq/rars/amqconnectorPlan.xml";

    private static final String LINE_SEP = System.getProperty("line.separator");

    private static final String PLAN_TEMPLATE = getPlanTemplate();

    // Deployer
    private static final ObjectName DEPLOYER_NAME = JMXUtil
            .getObjectName(ObjectNameConstants.DEPLOYER_OBJECT_NAME);

    private static final String[] DEPLOYER_ARGS = { File.class.getName(),
            File.class.getName() };

    private static final String DEPLOY_METHOD = "deploy";

    // Repository
    private static final ObjectName REPO_NAME = JMXUtil
            .getObjectName(ObjectNameConstants.REPO_OBJECT_NAME);

    private static final String[] REPO_ARGS = { URI.class.getName() };

    private static final String GETURL_METHOD = "getURL";

    static {
        // Initialize static vars
        REPO_FOLDER = getRepositoryFile();
        MODULE_FILE = REPO_FOLDER.getAbsolutePath() + ACTIVEMQ_RA;
        PLAN_FILE = REPO_FOLDER.getAbsolutePath() + PLAN_XML;
    }

    private static File getRepositoryFile() {
        File repoFile = null;

        try {
            Kernel kernel = KernelRegistry.getSingleKernel();
            URI uri = new URI(".");
            URL rootURL = (URL) kernel.invoke(REPO_NAME, GETURL_METHOD,
                    new Object[] {uri}, REPO_ARGS);
            uri = new URI(rootURL.toString());
            repoFile = new File(uri);
        } catch (URISyntaxException e) {
            System.out.println("ERROR: Invalid repository URL");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("ERROR: Problem getting repository location");
            e.printStackTrace();
        }

        return repoFile;
    }

    private static String getPlanTemplate() {
        StringBuffer sb = new StringBuffer();
        sb.append("<?xml version=\"1.0\"?>\n");
        sb
                .append("<connector xmlns=\"http://geronimo.apache.org/xml/ns/j2ee/connector-1.0\"\n");
        sb.append("    version=\"1.5\" configId=\"{0}\" parentId=\"{1}\">\n");
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
        sb.append("          <global-jndi-name>{9}</global-jndi-name>\n");
        sb
                .append("          <credential-interface>javax.resource.spi.security.PasswordCredential</credential-interface>\n");
        sb.append("        </connectiondefinition-instance>\n");
        sb.append("      </connection-definition>\n");
        sb.append("    </outbound-resourceadapter>\n");
        sb.append("  </resourceadapter>\n");
        sb.append("</connector>\n");

        return sb.toString();
    }

    private void savePlan(String filename, Object[] args) {
        MessageFormat mf = new MessageFormat(PLAN_TEMPLATE);
        String plan = mf.format(args);

        try {
            File f = new File(filename);
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

    public void deployPlan(Object[] args) {
        savePlan(PLAN_FILE, args);
        deployPlan(new File(MODULE_FILE), new File(PLAN_FILE));
    }

    public void deployPlan(File moduleFile, File planFile) {
        try {
            Kernel kernel = KernelRegistry.getSingleKernel();
            List list = (List) kernel.invoke(DEPLOYER_NAME, DEPLOY_METHOD,
                    new Object[] {moduleFile, planFile}, DEPLOYER_ARGS);
            ConfigurationManager configurationManager = ConfigurationUtil
                    .getConfigurationManager(kernel);
            for (Iterator iterator = list.iterator(); iterator.hasNext();) {
                URI configID = URI.create((String)iterator.next());
                if (!configurationManager.isLoaded(configID)) {
                    configurationManager.load(configID);
                }

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

    public List getDependencies() {
        List dependencies = null;

        try {
            dependencies = getListing(REPO_FOLDER, REPO_FOLDER
                    .getCanonicalPath());
            Collections.sort(dependencies);
        } catch (Exception e) {
            System.out.println("ERROR: Problem getting dependencies");
            e.printStackTrace();
        }

        return dependencies;
    }

    private List getListing(File dir, String basepath)
            throws java.io.IOException {
        if (dir == null) {
            throw new IllegalArgumentException("directory argument is null");
        }

        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("directory argument expected");
        }

        List listing = new ArrayList();

        List ls = Arrays.asList(dir.listFiles());
        Iterator iter = ls.iterator();

        while (iter.hasNext()) {
            File f = (File) iter.next();

            if (f.isDirectory()) {
                List listing1 = getListing(f, basepath);
                listing.addAll(listing1);
            } else {
                listing.add(f.getCanonicalPath().substring(
                        basepath.length() + 1).replace('\\', '/'));
            }
        }
        return listing;
    }

}