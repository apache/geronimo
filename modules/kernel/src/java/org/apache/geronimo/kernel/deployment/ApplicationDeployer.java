/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.kernel.deployment;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.URL;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.shared.ModuleType;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.kernel.service.GeronimoMBeanTarget;
import org.apache.geronimo.kernel.service.GeronimoMBeanContext;
import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;
import org.apache.geronimo.kernel.service.GeronimoOperationInfo;
import org.apache.geronimo.kernel.service.GeronimoParameterInfo;
import org.apache.geronimo.kernel.service.GeronimoMBeanEndpoint;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.deployment.scanner.URLInfo;
import org.apache.geronimo.kernel.deployment.scanner.URLType;
import org.apache.geronimo.kernel.deployment.goal.DeployURL;
import org.apache.geronimo.kernel.deployment.goal.RedeployURL;
import org.apache.geronimo.kernel.deployment.goal.UndeployURL;
import org.apache.geronimo.kernel.deployment.goal.DeploymentGoal;
import org.apache.geronimo.kernel.deployment.goal.DistributeURL;

/**
 * This is the server-side back end of the JSR-88 DeploymentManager.
 * Presumably, it will also be invoked by the local directory scanner
 * when a deployable J2EE module is encountered.
 *
 * @version $Revision: 1.3 $ $Date: 2003/12/09 04:23:33 $
 */
public class ApplicationDeployer implements GeronimoMBeanTarget {
    private final static Log log = LogFactory.getLog(ApplicationDeployer.class);
    private ServerTarget localServerTarget;
    private File saveDir;
    private GeronimoMBeanContext context;
    private DeploymentController deploymentController;

    public static GeronimoMBeanInfo getGeronimoMBeanInfo() throws Exception {
        GeronimoMBeanInfo mbeanInfo = new GeronimoMBeanInfo();
        mbeanInfo.setAutostart(true);
        mbeanInfo.setTargetClass(ApplicationDeployer.class.getName());
        // Methods taken over from DeploymentController
        mbeanInfo.addOperationInfo(new GeronimoOperationInfo("planDeployment",
                new GeronimoParameterInfo[] {
                    new GeronimoParameterInfo("Source", ObjectName.class.getName(), "Good question!"),
                    new GeronimoParameterInfo("URLInfos", Set.class.getName(), "Set of URLs to plan deployments for")
                },
                0,
                "plan the set of deployments"));
        mbeanInfo.addOperationInfo(new GeronimoOperationInfo("isDeployed",
                new GeronimoParameterInfo[] {
                    new GeronimoParameterInfo("URL", URL.class.getName(), "URL to test")
                },
                0,
                "Determine if the supplied URL is deployed"));
        mbeanInfo.addOperationInfo(new GeronimoOperationInfo("deploy",
                new GeronimoParameterInfo[] {
                    new GeronimoParameterInfo("URL", URL.class.getName(), "URL to deploy")
                },
                0,
                "Deploy the URL"));
        mbeanInfo.addOperationInfo(new GeronimoOperationInfo("undeploy",
                new GeronimoParameterInfo[] {
                    new GeronimoParameterInfo("URL", URL.class.getName(), "URL to undeploy")
                },
                0,
                "Undeploy the URL"));
        //todo: add the rest of the operation methods to support the JSR-88 client
        // Methods supporting JSR-88 client
        mbeanInfo.addOperationInfo(new GeronimoOperationInfo("getTargets",
                new GeronimoParameterInfo[] {},
                0,
                "Gets a list of the targets available for deployment"));
        mbeanInfo.addOperationInfo(new GeronimoOperationInfo("getRunningModules",
                new GeronimoParameterInfo[] {
                    new GeronimoParameterInfo("moduleTypeCode", Integer.TYPE.getName(), "The module type to search for, from ModuleType"),
                    new GeronimoParameterInfo("targetList", "[L"+Target.class.getName()+";", "The list of targets to search"),
                },
                0,
                "Gets a list of the modules running in the server"));
        mbeanInfo.addOperationInfo(new GeronimoOperationInfo("getNonRunningModules",
                new GeronimoParameterInfo[] {
                    new GeronimoParameterInfo("moduleTypeCode", Integer.TYPE.getName(), "The module type to search for, from ModuleType"),
                    new GeronimoParameterInfo("targetList", "[L"+Target.class.getName()+";", "The list of targets to search"),
                },
                0,
                "Gets a list of the modules distributed but not running in the server"));
        mbeanInfo.addOperationInfo(new GeronimoOperationInfo("getAvailableModules",
                new GeronimoParameterInfo[] {
                    new GeronimoParameterInfo("moduleTypeCode", Integer.TYPE.getName(), "The module type to search for, from ModuleType"),
                    new GeronimoParameterInfo("targetList", "[L"+Target.class.getName()+";", "The list of targets to search"),
                },
                0,
                "Gets a list of the all the modules in the server, whether running or not"));
        mbeanInfo.addOperationInfo(new GeronimoOperationInfo("prepareDistribute",
                new GeronimoParameterInfo[] {
                    new GeronimoParameterInfo("targets", "[L"+Target.class.getName()+";", "The list of targets to distribute to"),
                    new GeronimoParameterInfo("moduleArchive", URL.class.getName(), "A URL to the module to distribute"),
                    new GeronimoParameterInfo("deploymentPlan", URL.class.getName(), "A URL to the deployment plan of the module in question"),
                },
                0,
                "Begins the process of distributing a new module.  You must start the deployment job with the returned ID."));
        mbeanInfo.addOperationInfo(new GeronimoOperationInfo("prepareDistribute",
                new GeronimoParameterInfo[] {
                    new GeronimoParameterInfo("targets", "[L"+Target.class.getName()+";", "The list of targets to distribute to"),
                    new GeronimoParameterInfo("name", String.class.getName(), "The name of the module"),
                    new GeronimoParameterInfo("moduleArchive", "[B", "The content of the module to distribute"),
                    new GeronimoParameterInfo("deploymentPlan", "[B", "The content of the deployment plan of the module in question"),
                },
                0,
                "Begins the process of distributing a new module.  You must start the deployment job with the returned ID."));
        mbeanInfo.addOperationInfo(new GeronimoOperationInfo("prepareStart",
                new GeronimoParameterInfo[] {
                    new GeronimoParameterInfo("modules", "[L"+TargetModuleID.class.getName()+";", "The list of modules to start"),
                },
                0,
                "Begins the process of starting one or more modules.  You must start the deployment job with the returned ID."));
        mbeanInfo.addOperationInfo(new GeronimoOperationInfo("prepareStop",
                new GeronimoParameterInfo[] {
                    new GeronimoParameterInfo("modules", "[L"+TargetModuleID.class.getName()+";", "The list of modules to stop"),
                },
                0,
                "Begins the process of stopping one or more modules.  You must start the deployment job with the returned ID."));
        mbeanInfo.addOperationInfo(new GeronimoOperationInfo("prepareUndeploy",
                new GeronimoParameterInfo[] {
                    new GeronimoParameterInfo("modules", "[L"+TargetModuleID.class.getName()+";", "The list of modules to undeploy"),
                },
                0,
                "Begins the process of undeploying one or more modules.  You must start the deployment job with the returned ID."));
        mbeanInfo.addOperationInfo(new GeronimoOperationInfo("prepareRedeploy",
                new GeronimoParameterInfo[] {
                    new GeronimoParameterInfo("moduleIDList", "[L"+TargetModuleID.class.getName()+";", "The list of modules to redeploy"),
                    new GeronimoParameterInfo("moduleArchive", URL.class.getName(), "A URL to the module to distribute"),
                    new GeronimoParameterInfo("deploymentPlan", URL.class.getName(), "A URL to the deployment plan of the module in question"),
                },
                0,
                "Begins the process of stopping one or more modules.  You must start the deployment job with the returned ID."));
        mbeanInfo.addOperationInfo(new GeronimoOperationInfo("prepareRedeploy",
                new GeronimoParameterInfo[] {
                    new GeronimoParameterInfo("moduleIDList", "[L"+TargetModuleID.class.getName()+";", "The list of modules to redeploy"),
                    new GeronimoParameterInfo("moduleArchive", "[B", "The content of the module to distribute"),
                    new GeronimoParameterInfo("deploymentPlan", "[B", "The content of the deployment plan of the module in question"),
                },
                0,
                "Begins the process of stopping one or more modules.  You must start the deployment job with the returned ID."));
        mbeanInfo.addEndpoint(new GeronimoMBeanEndpoint("DeploymentController",
                DeploymentController.class,
                ObjectName.getInstance("geronimo.deployment:role=DeploymentController"),
                true));
        return mbeanInfo;
    }

    /**
     * Creates a new deployer
     */
    public ApplicationDeployer() {
        try {
            localServerTarget = new ServerTarget(InetAddress.getLocalHost().getHostName());
            localServerTarget.setHomeDir(System.getProperty("geronimo.home"));
        } catch(UnknownHostException e) {
            throw new RuntimeException("Unable to look up local hostname", e);
        }
    }

    public DeploymentController getDeploymentController() {
        return deploymentController;
    }

    public void setDeploymentController(DeploymentController deploymentController) {
        this.deploymentController = deploymentController;
    }

    /**
     * Sets the GeronimoMBeanContext.  This is called before doStart and with a null context after stop.
     *
     * @param context the new context; will be null after stop
     */
    public void setMBeanContext(GeronimoMBeanContext context) {
        this.context = context;
    }

    /**
     * Checks if the target is ready to start.  A target can delay the start of the GeronimoBean by returning
     * false from this method.
     *
     * @return true if the target is ready to start; false otherwise
     */
    public boolean canStart() {
        return true;
    }

    /**
     * Starts the target.  This method is called by the GeronimoMBean to inform the target that the MBean is about to
     * start.  This is called immediately before moving to the running state.
     */
    public void doStart() {
    }

    /**
     * Checks if the target is ready to stop.  A target can delay the stopping of the GeronimoBean by returning
     * false from this method.
     *
     * @return true if the target is ready to stop; false otherwise
     */
    public boolean canStop() {
        return true;
    }

    /**
     * Stops the target.  This method is called by the GeronimoMBean to inform the target that the MBean is about to
     * stop.  This is called immediately before moving to the stopped state.
     */
    public void doStop() {
    }

    /**
     * Fails the MBean.  This method is called by the GeronimoMBean to inform the target that the MBean is about to
     * fail.  This is called immediately before moving to the failed state.
     */
    public void doFail() {
    }

    /**
     */
    public Target[] getTargets() { // this should logically be an operation, but it seems that operations must have arguments
        return new Target[]{localServerTarget};
    }

    /**
     */
    public TargetModuleID[] getRunningModules(int moduleTypeCode, Target[] targetList) {
        ModuleType moduleType = ModuleType.getModuleType(moduleTypeCode);
        return new TargetModuleID[0]; //todo: implement me
    }

    /**
     */
    public TargetModuleID[] getNonRunningModules(int moduleTypeCode, Target[] targetList) {
        ModuleType moduleType = ModuleType.getModuleType(moduleTypeCode);
        return new TargetModuleID[0]; //todo: implement me
    }

    /**
     */
    public TargetModuleID[] getAvailableModules(int moduleTypeCode, Target[] targetList) {
        ModuleType moduleType = ModuleType.getModuleType(moduleTypeCode);
        return new TargetModuleID[0]; //todo: implement me
    }

    /**
     */
    public int prepareDistribute(Target[] targets, URL moduleArchive, URL deploymentPlan) throws TargetException {
        if(targets.length != 1 || !targets[0].equals(localServerTarget)) {
            throw new TargetException("The deployer can only distribute to the local application server ("+localServerTarget+")");
        }
        //todo: implement me
        return -1;
    }

    /**
     */
    public int prepareDistribute(Target[] targets, String name, byte[] moduleArchive, byte[] deploymentPlan) throws TargetException {
        if(targets.length != 1 || !targets[0].equals(localServerTarget)) {
            throw new TargetException("The deployer can only distribute to the local application server ("+localServerTarget+")");
        }
        File module = saveFile(name, moduleArchive);
        File dd = saveFile("geronimo-deployment-"+name, deploymentPlan);
        ModuleType type = verifyDeployment(module, dd);
        //todo: Create and start an MBean for the deployment, use that later to check status of the deployment
        GeronimoTargetModule tm = new GeronimoTargetModule(localServerTarget, name); //todo: specify URL for web apps
        try {
            int id = deploymentController.prepareDeploymentJob(new DeploymentGoal[]{
                new DistributeURL(tm, module.toURL(), URLType.PACKED_ARCHIVE)});
            return id;
//            if(!deployments.contains(tm)) {
//                deployments.add(tm);
//            }
        } catch(Throwable e) {
            log.error("Unable to prepare a deployment job", e);
            while(e.getCause() != null) {
                e = e.getCause();
                log.error("Unable to prepare a deployment job", e);
            }
            return -1;
        }
    }

    /**
     */
    public int prepareStart(TargetModuleID[] modules) {
        validateModules(modules, false);
        //todo: implement me
        return -1;
    }

    /**
     */
    public int prepareStop(TargetModuleID[] modules) {
        validateModules(modules, true);
        //todo: implement me
        return -1;
    }

    /**
     */
    public int prepareUndeploy(TargetModuleID[] modules) {
        validateModules(modules, false);
        //todo: implement me
        return -1;
    }

    /**
     */
    public int prepareRedeploy(TargetModuleID[] moduleIDList, URL moduleArchive, URL deploymentPlan) {
        //todo: implement me
        return -1;
    }

    /**
     */
    public int prepareRedeploy(TargetModuleID[] moduleIDList, byte[] moduleArchive, byte[] deploymentPlan) {
        //todo: implement me
        return -1;
    }

    // ---------------------- Methods required to populate Property Editors ----------------------

    /**
     * Used to provide a list of security users/groups/roles that the deployer
     * can map a J2EE security role to.
     *
     * @param securityRealm The security realm in use by the application
     *
     * @return A list of security mapping options, or null if the current user
     *         is not authorized to retrieve that information, or the
     *         information is not available.
     */
    public String[] getSecurityRoleOptions(String securityRealm) {
        return new String[0]; //todo: implement me
    }

    /**
     * Gets a list of the JNDI names of global resources of a particular type
     * defined in the server.  For example, a list of all javax.sql.DataSource
     * resources.  Note that any resources tied to a particular application
     * will not be included.
     *
     * @param resourceClassName The name of the interface that the resource
     *                          should implement (e.g. javax.sql.DataSource).
     *
     * @return A list of the JNDI names of the available resources.  Returns
     *         null of no such resources are available, the current user is
     *         not authorized to retrieve the list, etc.
     */
    public String[] getResourceJndiNames(String resourceClassName) {
        return new String[0]; //todo: implement me
    }

    // ---------------------- Helper Methods ----------------------

    private File saveFile(String name, byte[] bytes) {
        if(saveDir == null) {
            String home = System.getProperty("geronimo.home");
            if(home.startsWith("file:")) {
                home = home.substring(5);
            }
            saveDir = new File(home, "working");
            if(!saveDir.exists()) {
                log.warn("Geronimo working directory ("+saveDir.getAbsolutePath()+") does not exist!");
                if(!saveDir.mkdir()) {
                    throw new RuntimeException("Unable to create working directory "+saveDir.getAbsolutePath());
                }
            }
        }
        try {
            File target = new File(saveDir, name);
            log.info("Preparing to save file to "+target.getAbsolutePath());
            OutputStream out = new FileOutputStream(target, false);
            out.write(bytes);
            out.flush();
            out.close();
            return target;
        } catch(IOException e) {
            throw new RuntimeException("Unable to save file locally");
        }
    }

    private ModuleType verifyDeployment(File module, File dd) {
        //todo: validation
        if(module.getName().toLowerCase().endsWith(".war")) {
            return ModuleType.WAR;
        } else if(module.getName().toLowerCase().endsWith(".jar")) {
            return ModuleType.EJB;
        } else {
            log.error("Validation Error: cannot determine the J2EE module type for "+module.getName());
            return null;
        }
    }

    private void validateModules(TargetModuleID[] modules, boolean running) {
        for(int i = 0; i < modules.length; i++) {
            TargetModuleID module = modules[i];
            if(!module.getTarget().equals(localServerTarget)) {
                throw new IllegalArgumentException("Cannot affect modules for target "+module.getTarget().getName());
            }
            //todo: validate whether module is running according to the running argument
        }
    }

    private void runDeployment(DeploymentGoal[] goals) {
        deploymentController.runDeploymentJob(goals);
   }

    // ------------------------ Moved from DeploymentController ------------------------------
    private final Map scanResults = new HashMap();

    public synchronized void planDeployment(ObjectName source, Set urlInfos) {
        Set lastScan = (Set) scanResults.get(source);
        List goals = new ArrayList();

        // find new and existing urlInfos
        for (Iterator i = urlInfos.iterator(); i.hasNext();) {
            URLInfo urlInfo = (URLInfo) i.next();
            URL url = urlInfo.getUrl();


            if (!isDeployed(url)) {
                //only add a new deployment goal if we don't already have one. One can already exist if
                //there was no deployer available when the url was scanned
                if ((lastScan == null) || ((lastScan != null) &&!lastScan.contains (urlInfo))){
//                    log.info("Adding url goal for " + url);
                    GeronimoTargetModule tm = new GeronimoTargetModule(localServerTarget, url.getPath()); //todo: specify URL for web apps
                    goals.add(new DeployURL(tm, url, urlInfo.getType()));
                }
            } else {
//                log.info("Redeploying url " + url);
                //todo: look up old TargetModuleID
                GeronimoTargetModule tm = new GeronimoTargetModule(localServerTarget, url.getPath()); //todo: specify URL for web apps
                goals.add(new RedeployURL(tm, url));
            }
        }

        // create remove goals for all urlInfos that were found last time but not now
        if (lastScan != null) {
            for (Iterator i = lastScan.iterator(); i.hasNext();) {
                URLInfo urlInfo = (URLInfo) i.next();
                URL url = urlInfo.getUrl();

                if (!urlInfos.contains(urlInfo) && isDeployed(url)) {
                    //todo: look up old TargetModuleID
                    GeronimoTargetModule tm = new GeronimoTargetModule(localServerTarget, url.getPath()); //todo: specify URL for web apps
                    goals.add(new UndeployURL(tm, url));
                }
            }
        }
        scanResults.put(source, urlInfos);
        runDeployment((DeploymentGoal[])goals.toArray(new DeploymentGoal[goals.size()]));
    }

    /**
     */
    public boolean isDeployed(URL url) {
        try {
            ObjectName pattern = new ObjectName("*:role=DeploymentUnit,url=" + ObjectName.quote(url.toString()) + ",*");
            return !context.getServer().queryNames(pattern, null).isEmpty();
        } catch (MalformedObjectNameException e) {
            throw new AssertionError();
        }
    }

    /**
     */
    public synchronized void deploy(URL url) throws DeploymentException {
        if (isDeployed(url)) {
            return;
        }

        URLType type = null;
        try {
            type = URLType.getType(url);
        } catch (IOException e) {
            throw new DeploymentException(e);
        }

        GeronimoTargetModule tm = new GeronimoTargetModule(localServerTarget, url.getPath()); //todo: specify URL for web apps
        runDeployment(new DeploymentGoal[]{new DeployURL(tm, url, type)});
    }

    /**
     */
    public synchronized void undeploy(URL url) {
        if (!isDeployed(url)) {
            return;
        }
        GeronimoTargetModule tm = new GeronimoTargetModule(localServerTarget, url.getPath()); //todo: specify URL for web apps
        //todo: look up old TargetModuleID
        runDeployment(new DeploymentGoal[]{new UndeployURL(tm, url)});
    }
}
