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
 * This source code implements specifications defined by the Java
 * Community Process. In order to remain compliant with the specification
 * DO NOT add / change / or delete method signatures!
 *
 * ====================================================================
 */
package javax.enterprise.deploy.spi;

import javax.enterprise.deploy.spi.exceptions.DConfigBeanVersionUnsupportedException;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.exceptions.InvalidModuleException;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.enterprise.deploy.shared.DConfigBeanVersionType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.model.DeployableObject;
import java.io.File;
import java.io.InputStream;
import java.util.Locale;

/**
 * The DeploymentManager object provides the core set of functions a J2EE platform
 * must provide for J2EE application deployment. It provides server related
 * information, such as, a list of deployment targets, and vendor unique runtime
 * configuration information.
 *
 * @version $Revision: 1.2 $ $Date: 2003/08/30 02:16:58 $
 */
public interface DeploymentManager {
    /**
     * Retrieve the list of deployment targets supported by this DeploymentManager.
     *
     * @return A list of deployment Target designators the user may select for
     *         application deployment or <code>null</code> if there are none.
     *
     * @throws IllegalStateException is thrown when the method is called when
     *         running in disconnected mode.
     */
    public Target[] getTargets() throws IllegalStateException;

    /**
     * Retrieve the list of J2EE application modules distributed to the identified
     * targets and that are currently running on the associated server or servers.
     *
     * @param moduleType A predefined designator for a J2EE module type.
     * @param targetList A list of deployment Target designators the user wants
     *                   checked for module run status.
     *
     * @return An array of TargetModuleID objects representing the running modules
     *         or <code>null</code> if there are none.
     *
     * @throws TargetException occurs when an invalid Target was provided.
     * @throws IllegalStateException is thrown when the method is called when running
     *         in disconnected mode.
     */
    public TargetModuleID[] getRunningModules(ModuleType moduleType, Target[] targetList) throws TargetException, IllegalStateException;

    /**
     * Retrieve the list of J2EE application modules distributed to the identified
     * targets and that are currently not running on the associated server or servers.
     *
     * @param moduleType A predefined designator for a J2EE module type.
     * @param targetList A list of deployment Target designators the user wants checked
     *                   for module not running status.
     *
     * @return An array of TargetModuleID objects representing the non-running modules
     *         or <code>null</code> if there are none.
     *
     * @throws TargetException occurs when an invalid Target was provided.
     * @throws IllegalStateException is thrown when the method is called when running
     *         in disconnected mode.
     */
    public TargetModuleID[] getNonRunningModules(ModuleType moduleType, Target[] targetList) throws TargetException, IllegalStateException;

    /**
     * Retrieve the list of all J2EE application modules running or not running on the
     * identified targets.
     *
     * @param moduleType A predefined designator for a J2EE module type.
     * @param targetList A list of deployment Target designators the user wants checked
     *                   for module not running status.
     *
     * @return An array of TargetModuleID objects representing all deployed modules
     *         running or not or <code>null</code> if there are no deployed modules.
     *
     * @throws TargetException occurs when an invalid Target was provided.
     * @throws IllegalStateException is thrown when the method is called when running
     *         in disconnected mode.
     */
    public TargetModuleID[] getAvailableModules(ModuleType moduleType, Target[] targetList) throws TargetException, IllegalStateException;

    /**
     * Retrieve the object that provides server-specific deployment configuration
     * information for the J2EE deployable component.
     *
     * @param dObj An object representing a J2EE deployable component.
     *
     * @return An object used to configure server-specific deployment information
     *
     * @throws InvalidModuleException The DeployableObject is an unknown or unsupported
     *         component for this configuration tool.
     */
    public DeploymentConfiguration createConfiguration(DeployableObject dObj) throws InvalidModuleException;

    /**
     * The distribute method performs three tasks; it validates the deployment
     * configuration data, generates all container specific classes and interfaces,
     * and moves the fully baked archive to the designated deployment targets.
     *
     * @param targetList     A list of server targets the user is specifying this application
     *                       should be deployed to.
     * @param moduleArchive  The file name of the application archive to be distributed.
     * @param deploymentPlan The file containing the runtime configuration information
     *                       associated with this application archive.
     *
     * @return an object that tracks and reports the status of the distribution process.
     *
     * @throws IllegalStateException is thrown when the method is called when running in disconnected mode.
     */
    public ProgressObject distribute(Target[] targetList, File moduleArchive, File deploymentPlan) throws IllegalStateException;

    /**
     * The distribute method performs three tasks; it validates the deployment
     * configuration data, generates all container specific classes and interfaces,
     * and moves the fully baked archive to the designated deployment targets.
     *
     * @param targetList     A list of server targets the user is specifying this application
     *                       should be deployed to.
     * @param moduleArchive  The stream containing the application archive to be distributed.
     * @param deploymentPlan The stream containing the runtime configuration information
     *                       associated with this application archive.
     *
     * @return an object that tracks and reports the status of the distribution process.
     *
     * @throws IllegalStateException is thrown when the method is called when running in disconnected mode.
     */
    public ProgressObject distribute(Target[] targetList, InputStream moduleArchive, InputStream deploymentPlan) throws IllegalStateException;

    /**
     * Start the application running.
     *
     * <p>Only the TargetModuleIDs which represent a root module are valid for being
     * started.  A root TargetModuleID has no parent.  A TargetModuleID with a parent
     * can not be individually started.  A root TargetModuleID module and all its
     * child modules will be started.</p>
     *
     * @param moduleIDList An array of TargetModuleID objects representing the modules to be started.
     *
     * @return An object that tracks and reports the status of the start operation.
     *
     * @throws IllegalStateException is thrown when the method is called when running in disconnected mode.
     */
    public ProgressObject start(TargetModuleID[] moduleIDList) throws IllegalStateException;

    /**
     * Stop the application running.
     *
     * <p>Only the TargetModuleIDs which represent a root module are valid for
     * being stopped.  A root TargetModuleID has no parent.  A TargetModuleID
     * with a parent can not be individually stopped.  A root TargetModuleID
     * module and all its child modules will be stopped.</p>
     *
     * @param moduleIDList An array of TargetModuleID objects representing the modules to be stopped.
     *
     * @return An object that tracks and reports the status of the stop operation.
     *
     * @throws IllegalStateException is thrown when the method is called when running in disconnected mode.
     */
    public ProgressObject stop(TargetModuleID[] moduleIDList) throws IllegalStateException;

    /**
     * Remove the application from the target server.
     *
     * <p>Only the TargetModuleIDs which represent a root module are valid for
     * undeployment.  A root TargetModuleID has no parent.  A TargetModuleID with
     * a parent can not be undeployed.  A root TargetModuleID module and all its
     * child modules will be undeployed.  The root TargetModuleID module and all
     * its child modules must stopped before they can be undeployed.
     *
     * @param moduleIDList An array of TargetModuleID objects representing the root
     *                     modules to be undeployed.
     *
     * @return An object that tracks and reports the status of the stop operation.
     *
     * @throws IllegalStateException is thrown when the method is called when running in disconnected mode.
     */
    public ProgressObject undeploy(TargetModuleID[] moduleIDList) throws IllegalStateException;

    /**
     * This method designates whether this platform vendor provides application
     * redeployment functionality.  A value of true means it is supported.  False
     * means it is not.
     *
     * @return A value of true means redeployment is supported by this vendor's
     *         DeploymentManager. False means it is not.
     */
    public boolean isRedeploySupported();

    /**
     * (optional) The redeploy method provides a means for updating currently
     * deployed J2EE applications.  This is an optional method for vendor
     * implementation.  Redeploy replaces a currently deployed application with an
     * updated version.  The runtime configuration information for the updated
     * application must remain identical to the application it is updating.  When
     * an application update is redeployed, all existing client connections to the
     * original running application must not be disrupted; new clients will connect
     * to the application update.  This operation is valid for TargetModuleIDs that
     * represent a root module.  A root TargetModuleID has no parent.  A root
     * TargetModuleID module and all its child modules will be redeployed.  A child
     * TargetModuleID module cannot be individually redeployed.  The redeploy
     * operation is complete only when this action for all the modules has completed.
     *
     * @param moduleIDList   An array of designators of the applications to be updated.
     * @param moduleArchive  The file name of the application archive to be redeployed.
     * @param deploymentPlan The deployment configuration information associated with
     *                       this application archive.
     *
     * @return An object that tracks and reports the status of the redeploy operation.
     *
     * @throws UnsupportedOperationException this optional command is not supported by
     *         this implementation.
     * @throws IllegalStateException is thrown when the method is called when running
     *         in disconnected mode.
     */
    public ProgressObject redeploy(TargetModuleID[] moduleIDList, File moduleArchive, File deploymentPlan) throws UnsupportedOperationException, IllegalStateException;

    /**
     * (optional) The redeploy method provides a means for updating currently
     * deployed J2EE applications.  This is an optional method for vendor
     * implementation.  Redeploy replaces a currently deployed application with an
     * updated version.  The runtime configuration information for the updated
     * application must remain identical to the application it is updating.  When
     * an application update is redeployed, all existing client connections to the
     * original running application must not be disrupted; new clients will connect
     * to the application update.  This operation is valid for TargetModuleIDs that
     * represent a root module.  A root TargetModuleID has no parent.  A root
     * TargetModuleID module and all its child modules will be redeployed.  A child
     * TargetModuleID module cannot be individually redeployed.  The redeploy
     * operation is complete only when this action for all the modules has completed.
     *
     * @param moduleIDList   An array of designators of the applications to be updated.
     * @param moduleArchive  The stream containing the application archive to be redeployed.
     * @param deploymentPlan The streeam containing the deployment configuration information
     *                       associated with this application archive.
     *
     * @return An object that tracks and reports the status of the redeploy operation.
     *
     * @throws UnsupportedOperationException this optional command is not supported by
     *         this implementation.
     * @throws IllegalStateException is thrown when the method is called when running
     *         in disconnected mode.
     */
    public ProgressObject redeploy(TargetModuleID[] moduleIDList, InputStream moduleArchive, InputStream deploymentPlan) throws UnsupportedOperationException, IllegalStateException;

    /**
     * The release method is the mechanism by which the tool signals to the
     * DeploymentManager that the tool does not need it to continue running
     * connected to the platform.  The tool may be signaling it wants to run in a
     * disconnected mode or it is planning to shutdown.  When release is called the
     * DeploymentManager may close any J2EE resource connections it had for
     * deployment configuration and perform other related resource cleanup.  It
     * should not accept any new operation requests (i.e., distribute, start, stop,
     * undeploy, redeploy.  It should finish any operations that are currently in
     * process.  Each ProgressObject associated with a running operation should be
     * marked as released (see the ProgressObject).
     */
    public void release();

    /**
     * Returns the default locale supported by this implementation of
     * javax.enterprise.deploy.spi subpackages.
     *
     * @return the default locale for this implementation.
     */
    public Locale getDefaultLocale();

    /**
     * Returns the active locale this implementation of
     * javax.enterprise.deploy.spi subpackages is running.
     *
     * @return the active locale of this implementation.
     */
    public Locale getCurrentLocale();

    /**
     * Set the active locale for this implementation of
     * javax.enterprise.deploy.spi subpackages to run.
     *
     * @param locale the locale to set
     *
     * @throws UnsupportedOperationException the provide locale is not supported.
     */
    public void setLocale(Locale locale) throws UnsupportedOperationException;

    /**
     * Returns an array of supported locales for this implementation.
     *
     * @return the list of supported locales.
     */
    public Locale[] getSupportedLocales();

    /**
     * Reports if this implementation supports the designated locale.
     *
     * @param locale  the locale to check
     *
     * @return A value of <code>true</code> means it is supported and <code>false</code> it is not.
     */
    public boolean isLocaleSupported(Locale locale);

    /**
     * Returns the J2EE platform version number for which the configuration
     * beans are provided.  The beans must have been compiled with the J2SE
     * version required by the J2EE platform.
     *
     * @return a DConfigBeanVersionType object representing the platform
     *         version number for which these beans are provided.
     */
    public DConfigBeanVersionType getDConfigBeanVersion();

    /**
     * Returns <code>true</code> if the configuration beans support the J2EE platform
     * version specified.  It returns <code>false</code> if the version is not supported.
     *
     * @param version a DConfigBeanVersionType object representing the J2EE
     *                platform version for which support is requested.
     *
     * @return <code>true</code> if the version is supported and 'false if not.
     */
    public boolean isDConfigBeanVersionSupported(DConfigBeanVersionType version);

    /**
     * Set the configuration beans to be used to the J2EE platform version specified.
     *
     * @param version a DConfigBeanVersionType object representing the J2EE
     *                platform version for which support is requested.
     *
     * @throws DConfigBeanVersionUnsupportedException when the requested bean
     *         version is not supported.
     */
    public void setDConfigBeanVersion(DConfigBeanVersionType version) throws DConfigBeanVersionUnsupportedException;
}