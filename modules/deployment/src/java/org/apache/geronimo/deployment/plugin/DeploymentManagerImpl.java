/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.geronimo.deployment.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.jar.JarInputStream;

import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.shared.CommandType;
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

import org.apache.geronimo.deployment.ConfigurationBuilder;
import org.apache.geronimo.deployment.ModuleConfigurer;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GConstructorInfo;
import org.apache.geronimo.gbean.GOperationInfo;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

/**
 *
 *
 * @version $Revision: 1.15 $ $Date: 2004/03/10 09:58:48 $
 */
public class DeploymentManagerImpl implements DeploymentManager {
    private final DeploymentServer server;
    private final Collection typeLoaders = new ArrayList();
    private final SchemaTypeLoader thisTypeLoader = XmlBeans.getContextTypeLoader();
    private SchemaTypeLoader schemaTypeLoader = thisTypeLoader;
    private final Collection configurers;
    private final Collection builders = new ArrayList();

    public DeploymentManagerImpl(
            DeploymentServer server,
            Collection configurers) {
        this.server = server;
        this.configurers = configurers;
        //make sure context loader is always present
        typeLoaders.add(thisTypeLoader);
    }

    public synchronized void addConfigurationBuilder(ConfigurationBuilder builder) {
        builders.add(builder);
        SchemaTypeLoader[] loaders = builder.getTypeLoaders();
        for (int i = 0; i < loaders.length; i++) {
            typeLoaders.add(loaders[i]);

        }
        rebuildSchemaTypeLoader();
    }

    public synchronized void removeConfigurationBuilder(ConfigurationBuilder builder) {
        builders.remove(builder);
        SchemaTypeLoader[] loaders = builder.getTypeLoaders();
        for (int i = 0; i < loaders.length; i++) {
            typeLoaders.remove(loaders[i]);

        }
        rebuildSchemaTypeLoader();
    }

    private void rebuildSchemaTypeLoader() {
        SchemaTypeLoader[] loaders = (SchemaTypeLoader[]) typeLoaders.toArray(new SchemaTypeLoader[typeLoaders.size()]);
        schemaTypeLoader = XmlBeans.typeLoaderUnion(loaders);
    }

    public DeploymentConfiguration createConfiguration(DeployableObject deployable) throws InvalidModuleException {
        for (Iterator i = configurers.iterator(); i.hasNext();) {
            ModuleConfigurer configurer = (ModuleConfigurer) i.next();
            DeploymentConfiguration config = configurer.createConfiguration(deployable);
            if (config != null) {
                return config;
            }
        }
        throw new InvalidModuleException("Unable to locate a DeploymentConfigurationFactory for supplied DeployableObject");
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
        return new Locale[]{getDefaultLocale()};
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
        InputStream moduleArchiveStream = null;
        try {
            moduleArchiveStream = new FileInputStream(moduleArchive);
        } catch (FileNotFoundException e) {
            return new FailedProgressObject(CommandType.DISTRIBUTE, "Could not find module archive: " + moduleArchive);
        }
        InputStream deploymentPlanStream = null;
        try {
            deploymentPlanStream = new FileInputStream(deploymentPlan);
        } catch (FileNotFoundException e) {
            return new FailedProgressObject(CommandType.DISTRIBUTE, "Could not find deployment plan: " + deploymentPlan);
        }
        return distribute(targetList, moduleArchiveStream, deploymentPlanStream);
    }

    public ProgressObject distribute(Target[] targetList, InputStream moduleArchive, InputStream deploymentPlan) throws IllegalStateException {
        XmlObject plan;
        try {
            plan = schemaTypeLoader.parse(deploymentPlan, null, null);
            //validate
            XmlOptions xmlOptions = new XmlOptions();
            xmlOptions.setLoadLineNumbers();
            Collection errors = new ArrayList();
            xmlOptions.setErrorListener(errors);
            if (!plan.validate(xmlOptions)) {
                return new FailedProgressObject(CommandType.DISTRIBUTE, "Invalid deployment plan: errors: " + errors);
            }
        } catch (org.apache.xmlbeans.XmlException e) {
            return new FailedProgressObject(CommandType.DISTRIBUTE, "Could not parse deployment plan");
        } catch (java.io.IOException e) {
            return new FailedProgressObject(CommandType.DISTRIBUTE, "Could not read deployment plan");
        }
        for (Iterator iterator = builders.iterator(); iterator.hasNext();) {
            ConfigurationBuilder configurationBuilder = (ConfigurationBuilder) iterator.next();
            if (configurationBuilder.canConfigure(plan)) {
                try {
                    return server.distribute(targetList, configurationBuilder, new JarInputStream(moduleArchive), plan);
                } catch (IOException e) {
                    return new FailedProgressObject(CommandType.DISTRIBUTE, e.getMessage());
                }
            }
        }
        return new FailedProgressObject(CommandType.DISTRIBUTE, "No configuration builder found for module");
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
        try {
            return redeploy(moduleIDList, new FileInputStream(moduleArchive), new FileInputStream(deploymentPlan));
        } catch (FileNotFoundException e) {
            // @todo should this return a "failed" progress object?
            throw new IllegalStateException();
        }
    }

    public ProgressObject redeploy(TargetModuleID[] moduleIDList, InputStream moduleArchive, InputStream deploymentPlan) throws UnsupportedOperationException, IllegalStateException {
        return server.redeploy(moduleIDList, moduleArchive, deploymentPlan);
    }

    public void release() {
        server.release();
        // @todo shut down the deployment kernel
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory("JSR88 Deployment Manager", DeploymentManagerImpl.class.getName());
        infoFactory.addOperation(new GOperationInfo("addConfigurationBuilder", new String[]{ConfigurationBuilder.class.getName()}));
        infoFactory.addOperation(new GOperationInfo("removeConfigurationBuilder", new String[]{ConfigurationBuilder.class.getName()}));
        infoFactory.addReference(new GReferenceInfo("Server", DeploymentServer.class.getName()));
        infoFactory.addReference(new GReferenceInfo("Configurers", ModuleConfigurer.class));
        infoFactory.setConstructor(new GConstructorInfo(
                Arrays.asList(new Object[]{"Server", "Configurers"}),
                Arrays.asList(new Object[]{DeploymentServer.class, Collection.class})
        ));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }
}
