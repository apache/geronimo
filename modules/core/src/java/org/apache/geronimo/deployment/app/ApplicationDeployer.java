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
package org.apache.geronimo.deployment.app;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.URL;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.shared.ModuleType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is the server-side back end of the JSR-88 DeploymentManager.
 * Presumably, it will also be invoked by the local directory scanner
 * when a deployable J2EE module is encountered.
 *
 * @jmx:mbean
 *
 * @version $Revision: 1.3 $ $Date: 2003/10/20 02:46:36 $
 */
public class ApplicationDeployer implements ApplicationDeployerMBean,MBeanRegistration {
    private final static Log log = LogFactory.getLog(ApplicationDeployer.class);
    private MBeanServer server;
    private ServerTarget localServerTarget;
    private File saveDir;
    private List deployments = new ArrayList();

    /**
     * Creates a new deployer
     *
     * @jmx:managed-constructor
     */
    public ApplicationDeployer() {
        try {
            localServerTarget = new ServerTarget(InetAddress.getLocalHost().getHostName());
            localServerTarget.setHomeDir(System.getProperty("geronimo.home"));
        } catch(UnknownHostException e) {
            throw new RuntimeException("Unable to look up local hostname", e);
        }
    }

    public ObjectName preRegister(MBeanServer mBeanServer, ObjectName objectName) throws Exception {
        this.server = mBeanServer; // we will need this to invoke other JMX components (the web server, etc.)
        return objectName;
    }

    public void postRegister(Boolean aBoolean) {
    }

    public void preDeregister() throws Exception {
    }

    public void postDeregister() {
    }
    
    /**
     * @jmx:managed-attribute
     */
    public Target[] getTargets() { // this should logically be an operation, but it seems that operations must have arguments
        return new Target[]{localServerTarget};
    }

    /**
     * @jmx:managed-operation
     */
    public TargetModuleID[] getRunningModules(int moduleTypeCode, Target[] targetList) {
        ModuleType moduleType = ModuleType.getModuleType(moduleTypeCode);
        return new TargetModuleID[0]; //todo: implement me
    }

    /**
     * @jmx:managed-operation
     */
    public TargetModuleID[] getNonRunningModules(int moduleTypeCode, Target[] targetList) {
        ModuleType moduleType = ModuleType.getModuleType(moduleTypeCode);
        return getAvailableModules(moduleTypeCode, targetList); // currently, nothing can be started; everything is non-running
        //todo: implement me
    }

    /**
     * @jmx:managed-operation
     */
    public TargetModuleID[] getAvailableModules(int moduleTypeCode, Target[] targetList) {
        ModuleType moduleType = ModuleType.getModuleType(moduleTypeCode);
        List list = new ArrayList();
        for(Iterator iterator = deployments.iterator(); iterator.hasNext();) {
            ServerTargetModule module = (ServerTargetModule)iterator.next();
            if(module.getType().getValue() == moduleTypeCode) {
                list.add(module);
            }
        }
        return (TargetModuleID[])list.toArray(new TargetModuleID[list.size()]);
    }

    /**
     * @jmx:managed-operation
     */
    public void distribute(Target[] targets, URL moduleArchive, URL deploymentPlan) throws TargetException {
        //todo: what should this return?  Some sort of ID that the ProgressObject can poll?  Perhaps it should use notifications instead?
        if(targets.length != 1 || !targets[0].equals(localServerTarget)) {
            throw new TargetException("The deployer can only distribute to the local application server ("+localServerTarget+")");
        }
        //todo: implement me
    }

    /**
     * @jmx:managed-operation
     */
    public void distribute(Target[] targets, String name, byte[] moduleArchive, byte[] deploymentPlan) throws TargetException {
        //todo: what should this return?  Some sort of ID that the ProgressObject can poll?  Perhaps it should use notifications instead?
        if(targets.length != 1 || !targets[0].equals(localServerTarget)) {
            throw new TargetException("The deployer can only distribute to the local application server ("+localServerTarget+")");
        }
        File module = saveFile(name, moduleArchive);
        File dd = saveFile("geronimo-deployment-"+name, deploymentPlan);
        ModuleType type = verifyDeployment(module, dd);
        //todo: Create and start an MBean for the deployment, use that later to check status of the deployment
        ServerTargetModule tm = new ServerTargetModule(type, localServerTarget, name); //todo: specify URL for web apps
        if(!deployments.contains(tm)) {
            deployments.add(tm);
        }
    }

    /**
     * @jmx:managed-operation
     */
    public void start(TargetModuleID[] modules) {
        //todo: what should this return?  Some sort of ID that the ProgressObject can poll?  Perhaps it should use notifications instead?
        validateModules(modules, false);
        //todo: implement me
    }

    /**
     * @jmx:managed-operation
     */
    public void stop(TargetModuleID[] modules) {
        //todo: what should this return?  Some sort of ID that the ProgressObject can poll?  Perhaps it should use notifications instead?
        validateModules(modules, true);
        //todo: implement me
    }

    /**
     * @jmx:managed-operation
     */
    public void undeploy(TargetModuleID[] modules) {
        //todo: what should this return?  Some sort of ID that the ProgressObject can poll?  Perhaps it should use notifications instead?
        validateModules(modules, false);
        //todo: implement me
    }

    /**
     * @jmx:managed-operation
     */
    public void redeploy(TargetModuleID[] moduleIDList, URL moduleArchive, URL deploymentPlan) {
        //todo: what should this return?  Some sort of ID that the ProgressObject can poll?  Perhaps it should use notifications instead?
        //todo: implement me
    }

    /**
     * @jmx:managed-operation
     */
    public void redeploy(TargetModuleID[] moduleIDList, byte[] moduleArchive, byte[] deploymentPlan) {
        //todo: what should this return?  Some sort of ID that the ProgressObject can poll?  Perhaps it should use notifications instead?
        //todo: implement me
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
     *
     * @jmx:managed-operation
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
     *
     * @jmx:managed-operation
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
}
