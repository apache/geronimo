/**
 *
 * Copyright 2004 The Apache Software Foundation
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

//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//

package javax.enterprise.deploy.spi;

import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.shared.DConfigBeanVersionType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.exceptions.DConfigBeanVersionUnsupportedException;
import javax.enterprise.deploy.spi.exceptions.InvalidModuleException;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.status.ProgressObject;
import java.io.File;
import java.io.InputStream;
import java.util.Locale;

public class MockDeploymentManager implements DeploymentManager {
    public Target[] getTargets() throws IllegalStateException {
        return new Target[0];
    }

    public TargetModuleID[] getRunningModules(ModuleType moduleType, Target[] targetList) throws TargetException, IllegalStateException {
        return new TargetModuleID[0];
    }

    public TargetModuleID[] getNonRunningModules(ModuleType moduleType, Target[] targetList) throws TargetException, IllegalStateException {
        return new TargetModuleID[0];
    }

    public TargetModuleID[] getAvailableModules(ModuleType moduleType, Target[] targetList) throws TargetException, IllegalStateException {
        return new TargetModuleID[0];
    }

    public DeploymentConfiguration createConfiguration(DeployableObject dObj) throws InvalidModuleException {
        return null;
    }

    public ProgressObject distribute(Target[] targetList, File moduleArchive, File deploymentPlan) throws IllegalStateException {
        return null;
    }

    public ProgressObject distribute(Target[] targetList, InputStream moduleArchive, InputStream deploymentPlan) throws IllegalStateException {
        return null;
    }

    public ProgressObject start(TargetModuleID[] moduleIDList) throws IllegalStateException {
        return null;
    }

    public ProgressObject stop(TargetModuleID[] moduleIDList) throws IllegalStateException {
        return null;
    }

    public ProgressObject undeploy(TargetModuleID[] moduleIDList) throws IllegalStateException {
        return null;
    }

    public boolean isRedeploySupported() {
        return false;
    }

    public ProgressObject redeploy(TargetModuleID[] moduleIDList, File moduleArchive, File deploymentPlan) throws UnsupportedOperationException, IllegalStateException {
        return null;
    }

    public ProgressObject redeploy(TargetModuleID[] moduleIDList, InputStream moduleArchive, InputStream deploymentPlan) throws UnsupportedOperationException, IllegalStateException {
        return null;
    }

    public void release() {
    }

    public Locale getDefaultLocale() {
        return null;
    }

    public Locale getCurrentLocale() {
        return null;
    }

    public void setLocale(Locale locale) throws UnsupportedOperationException {
    }

    public Locale[] getSupportedLocales() {
        return new Locale[0];
    }

    public boolean isLocaleSupported(Locale locale) {
        return false;
    }

    public DConfigBeanVersionType getDConfigBeanVersion() {
        return null;
    }

    public boolean isDConfigBeanVersionSupported(DConfigBeanVersionType version) {
        return false;
    }

    public void setDConfigBeanVersion(DConfigBeanVersionType version) throws DConfigBeanVersionUnsupportedException {
    }
}
