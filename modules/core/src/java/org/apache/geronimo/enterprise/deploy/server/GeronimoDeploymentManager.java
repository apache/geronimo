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
package org.apache.geronimo.enterprise.deploy.server;

import java.io.File;
import java.io.InputStream;
import java.util.Locale;
import java.rmi.RemoteException;
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

import org.apache.geronimo.enterprise.deploy.server.ejb.EjbJarRoot;
import org.apache.geronimo.enterprise.deploy.server.ejb.EjbJarDeploymentConfiguration;
import org.apache.geronimo.enterprise.deploy.server.web.WebAppDeploymentConfiguration;
import org.apache.geronimo.enterprise.deploy.server.web.WebAppRoot;
import org.apache.geronimo.deployment.xml.ParserFactory;

/**
 * The Geronimo implementation of the JSR-88 DeploymentManager interface.
 * This same class is used for both connected mode and disconnected mode.
 * It uses a plugin to manage that.  Currently only J2EE 1.4 is supported.
 *
 * @version $Revision: 1.3 $ $Date: 2004/01/22 08:47:26 $
 */
public class GeronimoDeploymentManager implements DeploymentManager, DConfigBeanLookup {
    private ServerConnection server; // a connection to an application server
    private final ParserFactory parserFactory;

    GeronimoDeploymentManager(ServerConnection server, ParserFactory parserFactory) {
        this.server = server;
        this.parserFactory = parserFactory;
    }

    public DeploymentConfiguration createConfiguration(DeployableObject dObj) throws InvalidModuleException {
        if(dObj.getType().getValue() == ModuleType.EJB.getValue()) {
            return new EjbJarDeploymentConfiguration(dObj, new EjbJarRoot(dObj.getDDBeanRoot(), this), this, parserFactory);
        } else if(dObj.getType().getValue() == ModuleType.WAR.getValue()) {
            return new WebAppDeploymentConfiguration(dObj, new WebAppRoot(dObj.getDDBeanRoot(), this), this, parserFactory);
        } else {
            throw new InvalidModuleException("Can't handle modules of type " + dObj.getType());
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

    private void handleRemoteException(RemoteException e) {
        if(e.getCause() != null) {
            e.getCause().printStackTrace();
        } else {
            e.printStackTrace();
        }
        release();
    }

    // ---- All of the methods below are handled by the ServerConnection -----

    public Target[] getTargets() throws IllegalStateException {
        try {
            return server.getTargets();
        } catch(RemoteException e) {
            handleRemoteException(e);
            throw new IllegalStateException("Connection to server lost");
        }
    }

    public TargetModuleID[] getRunningModules(ModuleType moduleType, Target[] targetList) throws TargetException, IllegalStateException {
        try {
            return server.getRunningModules(moduleType, targetList);
        } catch(RemoteException e) {
            handleRemoteException(e);
            throw new IllegalStateException("Connection to server lost");
        }
    }

    public TargetModuleID[] getNonRunningModules(ModuleType moduleType, Target[] targetList) throws TargetException, IllegalStateException {
        try {
            return server.getNonRunningModules(moduleType, targetList);
        } catch(RemoteException e) {
            handleRemoteException(e);
            throw new IllegalStateException("Connection to server lost");
        }
    }

    public TargetModuleID[] getAvailableModules(ModuleType moduleType, Target[] targetList) throws TargetException, IllegalStateException {
        try {
            return server.getAvailableModules(moduleType, targetList);
        } catch(RemoteException e) {
            handleRemoteException(e);
            throw new IllegalStateException("Connection to server lost");
        }
    }

    public ProgressObject distribute(Target[] targetList, File moduleArchive, File deploymentPlan) throws IllegalStateException {
        try {
            return server.distribute(targetList, moduleArchive, deploymentPlan);
        } catch(RemoteException e) {
            handleRemoteException(e);
            throw new IllegalStateException("Connection to server lost");
        }
    }

    public ProgressObject distribute(Target[] targetList, InputStream moduleArchive, InputStream deploymentPlan) throws IllegalStateException {
        try {
            return server.distribute(targetList, moduleArchive, deploymentPlan);
        } catch(RemoteException e) {
            handleRemoteException(e);
            throw new IllegalStateException("Connection to server lost");
        }
    }

    public ProgressObject start(TargetModuleID[] moduleIDList) throws IllegalStateException {
        try {
            return server.start(moduleIDList);
        } catch(RemoteException e) {
            handleRemoteException(e);
            throw new IllegalStateException("Connection to server lost");
        }
    }

    public ProgressObject stop(TargetModuleID[] moduleIDList) throws IllegalStateException {
        try {
            return server.stop(moduleIDList);
        } catch(RemoteException e) {
            handleRemoteException(e);
            throw new IllegalStateException("Connection to server lost");
        }
    }

    public ProgressObject undeploy(TargetModuleID[] moduleIDList) throws IllegalStateException {
        try {
            return server.undeploy(moduleIDList);
        } catch(RemoteException e) {
            handleRemoteException(e);
            throw new IllegalStateException("Connection to server lost");
        }
    }

    public boolean isRedeploySupported() {
        try {
            return server.isRedeploySupported();
        } catch(RemoteException e) {
            handleRemoteException(e);
            return false;
        }
    }

    public ProgressObject redeploy(TargetModuleID[] moduleIDList, File moduleArchive, File deploymentPlan) throws UnsupportedOperationException, IllegalStateException {
        try {
            return server.redeploy(moduleIDList, moduleArchive, deploymentPlan);
        } catch(RemoteException e) {
            handleRemoteException(e);
            throw new IllegalStateException("Connection to server lost");
        }
    }

    public ProgressObject redeploy(TargetModuleID[] moduleIDList, InputStream moduleArchive, InputStream deploymentPlan) throws UnsupportedOperationException, IllegalStateException {
        try {
            return server.redeploy(moduleIDList, moduleArchive, deploymentPlan);
        } catch(RemoteException e) {
            handleRemoteException(e);
            throw new IllegalStateException("Connection to server lost");
        }
    }

    public String[] getSecurityRoleOptions(String securityRealm) {
        return server.getSecurityRoleOptions(securityRealm);
    }

    public String[] getResourceJndiNames(String resourceClassName) {
        return server.getResourceJndiNames(resourceClassName);
    }
}
