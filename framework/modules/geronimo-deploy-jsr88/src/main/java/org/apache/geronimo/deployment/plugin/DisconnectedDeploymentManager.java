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

package org.apache.geronimo.deployment.plugin;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.Locale;

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

import org.apache.geronimo.deployment.spi.ModuleConfigurer;

/**
 * Implementation of a disconnected JSR88 DeploymentManager.
 *
 *
 * @version $Rev$ $Date$
 */
public class DisconnectedDeploymentManager implements DeploymentManager {

    private final Collection<ModuleConfigurer> moduleConfigurers;

    public DisconnectedDeploymentManager(Collection<ModuleConfigurer> moduleConfigurers) {
        if (null == moduleConfigurers) {
            throw new IllegalArgumentException("moduleConfigurers is required");
        }
        this.moduleConfigurers = moduleConfigurers;
    }

    public DeploymentConfiguration createConfiguration(DeployableObject dObj) throws InvalidModuleException {
        if (dObj == null) {
            throw new NullPointerException("No deployable object supplied to configure");
        }
        ModuleConfigurer configurer = null;
        for (ModuleConfigurer moduleConfigurer : moduleConfigurers) {
            if (moduleConfigurer.getModuleType() == dObj.getType()) {
                configurer = moduleConfigurer;
               break;
            }
        }
        if (configurer == null) {
            throw new InvalidModuleException("No configurer for module type: " + dObj.getType() + " registered");
        }
        return configurer.createConfiguration(dObj);
    }

    public Locale[] getSupportedLocales() {
        return new Locale[]{getDefaultLocale()};
    }

    public Locale getCurrentLocale() {
        return getDefaultLocale();
    }

    public Locale getDefaultLocale() {
        return Locale.getDefault();
    }

    public boolean isLocaleSupported(Locale locale) {
        return getDefaultLocale().equals(locale);
    }

    public void setLocale(Locale locale) {
        if (isLocaleSupported(locale)) {
            throw new UnsupportedOperationException("Unsupported Locale");
        }
    }

    public DConfigBeanVersionType getDConfigBeanVersion() {
        return DConfigBeanVersionType.V1_4;
    }

    public boolean isDConfigBeanVersionSupported(DConfigBeanVersionType version) {
        return DConfigBeanVersionType.V1_4.equals(version);
    }

    public void setDConfigBeanVersion(DConfigBeanVersionType version) throws DConfigBeanVersionUnsupportedException {
        if (!isDConfigBeanVersionSupported(version)) {
            throw new DConfigBeanVersionUnsupportedException("Version not supported " + version);
        }
    }

    public Target[] getTargets() throws IllegalStateException {
        throw new IllegalStateException("Disconnected");
    }

    public TargetModuleID[] getRunningModules(ModuleType moduleType, Target[] targets) throws TargetException, IllegalStateException {
        throw new IllegalStateException("Disconnected");
    }

    public TargetModuleID[] getNonRunningModules(ModuleType moduleType, Target[] targets) throws TargetException, IllegalStateException {
        throw new IllegalStateException("Disconnected");
    }

    public TargetModuleID[] getAvailableModules(ModuleType moduleType, Target[] targets) throws TargetException, IllegalStateException {
        throw new IllegalStateException("Disconnected");
    }

    public ProgressObject distribute(Target[] targets, File file, File file1) throws IllegalStateException {
        throw new IllegalStateException("Disconnected");
    }

    /**
     * @deprecated
     */
    public ProgressObject distribute(Target[] targets, InputStream inputStream, InputStream inputStream1) throws IllegalStateException {
        throw new IllegalStateException("Disconnected");
    }

    public ProgressObject distribute(Target[] targets, ModuleType moduleType, InputStream inputStream, InputStream inputStream1) throws IllegalStateException {
        throw new IllegalStateException("Disconnected");
    }

    public ProgressObject start(TargetModuleID[] targetModuleIDs) throws IllegalStateException {
        throw new IllegalStateException("Disconnected");
    }

    public ProgressObject stop(TargetModuleID[] targetModuleIDs) throws IllegalStateException {
        throw new IllegalStateException("Disconnected");
    }

    public ProgressObject undeploy(TargetModuleID[] targetModuleIDs) throws IllegalStateException {
        throw new IllegalStateException("Disconnected");
    }

    public boolean isRedeploySupported() {
        return false;
    }

    public ProgressObject redeploy(TargetModuleID[] targetModuleIDs, File file, File file1) throws UnsupportedOperationException, IllegalStateException {
        throw new IllegalStateException("Disconnected");
    }

    public ProgressObject redeploy(TargetModuleID[] targetModuleIDs, InputStream inputStream, InputStream inputStream1) throws UnsupportedOperationException, IllegalStateException {
        throw new IllegalStateException("Disconnected");
    }

    public void release() {
        throw new IllegalStateException("Disconnected");
    }
}
