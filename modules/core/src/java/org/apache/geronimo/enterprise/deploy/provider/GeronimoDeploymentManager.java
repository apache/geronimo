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
import java.util.Locale;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.exceptions.InvalidModuleException;
import javax.enterprise.deploy.spi.exceptions.DConfigBeanVersionUnsupportedException;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.shared.DConfigBeanVersionType;
import javax.enterprise.deploy.model.DeployableObject;
import org.apache.geronimo.enterprise.deploy.provider.jar.EjbJarRoot;

/**
 * The Geronimo implementation of the JSR-88 DeploymentManager interface.
 * This same class is used for both connected mode and disconnected mode.
 * It uses a plugin to manage that.  Currently only J2EE 1.4 is supported.
 *
 * @version $Revision: 1.2 $ $Date: 2003/08/22 19:03:37 $
 */
public class GeronimoDeploymentManager implements DeploymentManager {
    private ServerConnection server; // a connection to an application server

    GeronimoDeploymentManager(ServerConnection server) {
        this.server = server;
    }

    public DeploymentConfiguration createConfiguration(DeployableObject dObj) throws InvalidModuleException {
        if(dObj.getType().getValue() == ModuleType.EJB.getValue()) {
            return new EjbJarDeploymentConfiguration(dObj, new EjbJarRoot(dObj.getDDBeanRoot()));
        } else {
            throw new InvalidModuleException("Can't handle modules of type "+dObj.getType());
        }
    }

    /**
     * Closes the current server connection, and replaces it with one that
     * behaves in disconnected mode.
     */
    public void release() {
        server.close();
        server = new NoServerConnection();
    }

    /**
     * Currently only the default locale is supported.
     */
    public Locale getDefaultLocale() {
        return Locale.getDefault();
    }

    /**
     * Currently only the default locale is supported.
     */
    public Locale getCurrentLocale() {
        return Locale.getDefault();
    }

    /**
     * Currently only the default locale is supported.
     *
     * @throws UnsupportedOperationException Thrown if the argument locale is
     *         not the default locale, as changing Locales is not supported.
     */
    public void setLocale(Locale locale) throws UnsupportedOperationException {
        if(!locale.equals(Locale.getDefault())) {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Currently only the default locale is supported.
     */
    public Locale[] getSupportedLocales() {
        return new Locale[]{Locale.getDefault()};
    }

    /**
     * Currently only the default locale is supported.
     */
    public boolean isLocaleSupported(Locale locale) {
        return locale.equals(Locale.getDefault());
    }

    /**
     * Currently only J2EE 1.4 is supported.
     */
    public DConfigBeanVersionType getDConfigBeanVersion() {
        return DConfigBeanVersionType.V1_4;
    }

    /**
     * Currently only J2EE 1.4 is supported.
     */
    public boolean isDConfigBeanVersionSupported(DConfigBeanVersionType version) {
        return version.getValue() == DConfigBeanVersionType.V1_4.getValue();
    }

    /**
     * Currently only J2EE 1.4 is supported.
     *
     * @throws DConfigBeanVersionUnsupportedException Occurs when the argument
     *         version is not 1.4.
     */
    public void setDConfigBeanVersion(DConfigBeanVersionType version) throws DConfigBeanVersionUnsupportedException {
        if(version.getValue() != DConfigBeanVersionType.V1_4.getValue()) {
            throw new DConfigBeanVersionUnsupportedException("This implementation only supports J2EE 1.4");
        }
    }

    // ---- All of the methods below are handled by the ServerConnection -----

    public Target[] getTargets() throws IllegalStateException {
        return server.getTargets();
    }

    public TargetModuleID[] getRunningModules(ModuleType moduleType, Target[] targetList) throws TargetException, IllegalStateException {
        return server.getRunningModules(moduleType, targetList);
    }

    public TargetModuleID[] getNonRunningModules(ModuleType moduleType, Target[] targetList) throws TargetException, IllegalStateException {
        return server.getNonRunningModules(moduleType, targetList);
    }

    public TargetModuleID[] getAvailableModules(ModuleType moduleType, Target[] targetList) throws TargetException, IllegalStateException {
        return server.getAvailableModules(moduleType, targetList);
    }

    public ProgressObject distribute(Target[] targetList, File moduleArchive, File deploymentPlan) throws IllegalStateException {
        return server.distribute(targetList, moduleArchive, deploymentPlan);
    }

    public ProgressObject distribute(Target[] targetList, InputStream moduleArchive, InputStream deploymentPlan) throws IllegalStateException {
        return server.distribute(targetList, moduleArchive, deploymentPlan);
    }

    public ProgressObject start(TargetModuleID[] moduleIDList) throws IllegalStateException {
        return server.start(moduleIDList);
    }

    public ProgressObject stop(TargetModuleID[] moduleIDList) throws IllegalStateException {
        return server.stop(moduleIDList);
    }

    public ProgressObject undeploy(TargetModuleID[] moduleIDList) throws IllegalStateException {
        return server.undeploy(moduleIDList);
    }

    public boolean isRedeploySupported() {
        return server.isRedeploySupported();
    }

    public ProgressObject redeploy(TargetModuleID[] moduleIDList, File moduleArchive, File deploymentPlan) throws UnsupportedOperationException, IllegalStateException {
        return server.redeploy(moduleIDList, moduleArchive, deploymentPlan);
    }

    public ProgressObject redeploy(TargetModuleID[] moduleIDList, InputStream moduleArchive, InputStream deploymentPlan) throws UnsupportedOperationException, IllegalStateException {
        return server.redeploy(moduleIDList, moduleArchive, deploymentPlan);
    }
}
