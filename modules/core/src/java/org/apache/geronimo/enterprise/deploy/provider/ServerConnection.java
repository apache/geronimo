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
package org.apache.geronimo.enterprise.deploy.provider;

import java.io.File;
import java.io.InputStream;
import java.rmi.RemoteException;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.shared.ModuleType;

/**
 * Encapsulates all the JSR-88 DeploymentManager methods that ordinarily
 * require a connection to the application server in order to execute.
 * Also includes some Geronimo-specific method to gather deployment
 * information from the server (such as getting a list of EJBs deployed
 * to provide as options when resolving an EJB reference).
 *
 * @version $Revision: 1.3 $
 */
public interface ServerConnection {
    // ---------------------- Methods required by DeploymentManager ----------------------

    /**
     * Releases any server resources and closes the connection to the server.
     */
    public void close();

    /**
     * @see javax.enterprise.deploy.spi.DeploymentManager#getTargets
     */
    public Target[] getTargets() throws IllegalStateException, RemoteException;

    /**
     * @see javax.enterprise.deploy.spi.DeploymentManager#getRunningModules
     */
    public TargetModuleID[] getRunningModules(ModuleType moduleType, Target[] targetList) throws TargetException, IllegalStateException, RemoteException;

    /**
     * @see javax.enterprise.deploy.spi.DeploymentManager#getNonRunningModules
     */
    public TargetModuleID[] getNonRunningModules(ModuleType moduleType, Target[] targetList) throws TargetException, IllegalStateException, RemoteException;

    /**
     * @see javax.enterprise.deploy.spi.DeploymentManager#getAvailableModules
     */
    public TargetModuleID[] getAvailableModules(ModuleType moduleType, Target[] targetList) throws TargetException, IllegalStateException, RemoteException;

    /**
     * @see javax.enterprise.deploy.spi.DeploymentManager#distribute(javax.enterprise.deploy.spi.Target[], java.io.File, java.io.File)
     */
    public ProgressObject distribute(Target[] targetList, File moduleArchive, File deploymentPlan) throws IllegalStateException, RemoteException;

    /**
     * @see javax.enterprise.deploy.spi.DeploymentManager#distribute(javax.enterprise.deploy.spi.Target[], java.io.InputStream, java.io.InputStream)
     */
    public ProgressObject distribute(Target[] targetList, InputStream moduleArchive, InputStream deploymentPlan) throws IllegalStateException, RemoteException;

    /**
     * @see javax.enterprise.deploy.spi.DeploymentManager#start
     */
    public ProgressObject start(TargetModuleID[] moduleIDList) throws IllegalStateException, RemoteException;

    /**
     * @see javax.enterprise.deploy.spi.DeploymentManager#stop
     */
    public ProgressObject stop(TargetModuleID[] moduleIDList) throws IllegalStateException, RemoteException;

    /**
     * @see javax.enterprise.deploy.spi.DeploymentManager#undeploy
     */
    public ProgressObject undeploy(TargetModuleID[] moduleIDList) throws IllegalStateException, RemoteException;

    /**
     * @see javax.enterprise.deploy.spi.DeploymentManager#isRedeploySupported
     */
    public boolean isRedeploySupported() throws RemoteException;

    /**
     * @see javax.enterprise.deploy.spi.DeploymentManager#redeploy(javax.enterprise.deploy.spi.TargetModuleID[], java.io.File, java.io.File)
     */
    public ProgressObject redeploy(TargetModuleID[] moduleIDList, File moduleArchive, File deploymentPlan) throws UnsupportedOperationException, IllegalStateException, RemoteException;

    /**
     * @see javax.enterprise.deploy.spi.DeploymentManager#redeploy(javax.enterprise.deploy.spi.TargetModuleID[], java.io.InputStream, java.io.InputStream)
     */
    public ProgressObject redeploy(TargetModuleID[] moduleIDList, InputStream moduleArchive, InputStream deploymentPlan) throws UnsupportedOperationException, IllegalStateException, RemoteException;

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
    public String[] getSecurityRoleOptions(String securityRealm) throws RemoteException;

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
    public String[] getResourceJndiNames(String resourceClassName) throws RemoteException;
}
