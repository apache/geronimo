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
package org.apache.geronimo.deployment.plugin;

import java.io.File;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.shared.DConfigBeanVersionType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.DConfigBeanVersionUnsupportedException;
import javax.enterprise.deploy.spi.exceptions.InvalidModuleException;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.status.ProgressObject;

import org.apache.geronimo.deployment.plugin.factories.DeploymentConfigurationFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GEndpointInfo;
import org.apache.geronimo.gbean.GConstructorInfo;

/**
 *
 *
 * @version $Revision: 1.3 $ $Date: 2004/01/22 05:47:41 $
 */
public class DeploymentManagerImpl implements DeploymentManager {
    private final Map configurationFactories;

    private boolean connected;

    public DeploymentManagerImpl(
            DeploymentConfigurationFactory earFactory,
            DeploymentConfigurationFactory warFactory,
            DeploymentConfigurationFactory ejbFactory,
            DeploymentConfigurationFactory rarFactory,
            DeploymentConfigurationFactory carFactory
            ) {
        connected = false;
        configurationFactories = new HashMap(5);
        configurationFactories.put(ModuleType.EAR, earFactory);
        configurationFactories.put(ModuleType.WAR, warFactory);
        configurationFactories.put(ModuleType.EJB, ejbFactory);
        configurationFactories.put(ModuleType.RAR, rarFactory);
        configurationFactories.put(ModuleType.CAR, carFactory);
    }

    public DeploymentConfiguration createConfiguration(DeployableObject deployable) throws InvalidModuleException {
        ModuleType type = deployable.getType();
        DeploymentConfigurationFactory factory = (DeploymentConfigurationFactory) configurationFactories.get(type);
        if (factory == null) {
            throw new InvalidModuleException("Unable to load DeploymentConfigurationFactory");
        }
        return factory.createConfiguration(deployable);
    }

    public DConfigBeanVersionType getDConfigBeanVersion() {
        return DConfigBeanVersionType.V1_4;
    }

    public void setDConfigBeanVersion(DConfigBeanVersionType version) throws DConfigBeanVersionUnsupportedException {
        if (!isDConfigBeanVersionSupported(version)) {
            throw new DConfigBeanVersionUnsupportedException("Version not supported " + version);
        }
    }

    public boolean isDConfigBeanVersionSupported(DConfigBeanVersionType version) {
        return DConfigBeanVersionType.V1_4.equals(version);
    }

    public Locale getDefaultLocale() {
        return Locale.getDefault();
    }

    public Locale[] getSupportedLocales() {
        return new Locale[] { getDefaultLocale() };
    }

    public Locale getCurrentLocale() {
        return getDefaultLocale();
    }

    public void setLocale(Locale locale) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Cannot set Locale");
    }

    public boolean isLocaleSupported(Locale locale) {
        return getDefaultLocale().equals(locale);
    }

    public void release() {
    }

    public Target[] getTargets() throws IllegalStateException {
        if (!connected) {
            throw new IllegalStateException("Disconnected");
        }
        throw new UnsupportedOperationException();
    }

    public TargetModuleID[] getRunningModules(ModuleType moduleType, Target[] targetList) throws TargetException, IllegalStateException {
        if (!connected) {
            throw new IllegalStateException("Disconnected");
        }
        throw new UnsupportedOperationException();
    }

    public TargetModuleID[] getNonRunningModules(ModuleType moduleType, Target[] targetList) throws TargetException, IllegalStateException {
        if (!connected) {
            throw new IllegalStateException("Disconnected");
        }
        throw new UnsupportedOperationException();
    }

    public TargetModuleID[] getAvailableModules(ModuleType moduleType, Target[] targetList) throws TargetException, IllegalStateException {
        if (!connected) {
            throw new IllegalStateException("Disconnected");
        }
        throw new UnsupportedOperationException();
    }

    public ProgressObject distribute(Target[] targetList, File moduleArchive, File deploymentPlan) throws IllegalStateException {
        if (!connected) {
            throw new IllegalStateException("Disconnected");
        }
        throw new UnsupportedOperationException();
    }

    public ProgressObject distribute(Target[] targetList, InputStream moduleArchive, InputStream deploymentPlan) throws IllegalStateException {
        if (!connected) {
            throw new IllegalStateException("Disconnected");
        }
        throw new UnsupportedOperationException();
    }

    public ProgressObject start(TargetModuleID[] moduleIDList) throws IllegalStateException {
        if (!connected) {
            throw new IllegalStateException("Disconnected");
        }
        throw new UnsupportedOperationException();
    }

    public ProgressObject stop(TargetModuleID[] moduleIDList) throws IllegalStateException {
        if (!connected) {
            throw new IllegalStateException("Disconnected");
        }
        throw new UnsupportedOperationException();
    }

    public ProgressObject undeploy(TargetModuleID[] moduleIDList) throws IllegalStateException {
        if (!connected) {
            throw new IllegalStateException("Disconnected");
        }
        throw new UnsupportedOperationException();
    }

    public boolean isRedeploySupported() {
        return true;
    }

    public ProgressObject redeploy(TargetModuleID[] moduleIDList, File moduleArchive, File deploymentPlan) throws UnsupportedOperationException, IllegalStateException {
        if (!connected) {
            throw new IllegalStateException("Disconnected");
        }
        throw new UnsupportedOperationException();
    }

    public ProgressObject redeploy(TargetModuleID[] moduleIDList, InputStream moduleArchive, InputStream deploymentPlan) throws UnsupportedOperationException, IllegalStateException {
        if (!connected) {
            throw new IllegalStateException("Disconnected");
        }
        throw new UnsupportedOperationException();
    }

    public static final GBeanInfo GBEAN_INFO;
    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory("JSR88 Deployment Manager", DeploymentManagerImpl.class.getName());
        infoFactory.addEndpoint(new GEndpointInfo("EARFactory", DeploymentConfigurationFactory.class.getName()));
        infoFactory.addEndpoint(new GEndpointInfo("WARFactory", DeploymentConfigurationFactory.class.getName()));
        infoFactory.addEndpoint(new GEndpointInfo("EJBFactory", DeploymentConfigurationFactory.class.getName()));
        infoFactory.addEndpoint(new GEndpointInfo("RARFactory", DeploymentConfigurationFactory.class.getName()));
        infoFactory.addEndpoint(new GEndpointInfo("CARFactory", DeploymentConfigurationFactory.class.getName()));
        infoFactory.setConstructor(new GConstructorInfo(
                Arrays.asList(new Object[] {"EARFactory", "WARFactory", "EJBFactory", "RARFactory", "CARFactory"}),
                Arrays.asList(new Object[] {DeploymentConfigurationFactory.class, DeploymentConfigurationFactory.class, DeploymentConfigurationFactory.class, DeploymentConfigurationFactory.class, DeploymentConfigurationFactory.class})
        ));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }
}
