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

package org.apache.geronimo.deployment.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Collection;
import java.util.ArrayList;

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

import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.deployment.DeploymentModule;
import org.apache.geronimo.deployment.ModuleConfigurer;
import org.apache.geronimo.deployment.plugin.factories.DeploymentConfigurationFactory;
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanContext;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GConstructorInfo;
import org.apache.geronimo.gbean.GOperationInfo;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.Document;

/**
 *
 *
 * @version $Revision: 1.13 $ $Date: 2004/02/25 09:57:36 $
 */
public class DeploymentManagerImpl implements DeploymentManager, GBean {
    private final DeploymentServer server;
    private final Map schemaTypeToFactoryMap = new HashMap();
    private final Map schemaTypeToLoaderMap = new LinkedHashMap();
    private final SchemaTypeLoader thisTypeLoader = XmlBeans.getContextTypeLoader();
    private SchemaTypeLoader schemaTypeLoader = thisTypeLoader;
    private final Map configurationFactories;
    private final Collection configurers;

    public DeploymentManagerImpl(
            DeploymentServer server,
            Collection configurers,
            DeploymentConfigurationFactory earFactory,
            DeploymentConfigurationFactory warFactory,
            DeploymentConfigurationFactory ejbFactory,
            DeploymentConfigurationFactory rarFactory,
            DeploymentConfigurationFactory carFactory
            ) {
        this.server = server;
        this.configurers = configurers;
        //make sure context loader is always present
        //todo think if null is a plausible key here.
        schemaTypeToLoaderMap.put(null, thisTypeLoader);
        configurationFactories = new HashMap(5);
        addFactory(ModuleType.EAR, earFactory);
        addFactory(ModuleType.WAR, warFactory);
        addFactory(ModuleType.EJB, ejbFactory);
        addFactory(ModuleType.RAR, rarFactory);
        addFactory(ModuleType.CAR, carFactory);
    }

    public synchronized void addDeploymentConfigurationFactory(SchemaType schemaType, SchemaTypeLoader schemaTypeLoader, DeploymentConfigurationFactory factory) {
        schemaTypeToFactoryMap.put(schemaType, factory);
        schemaTypeToLoaderMap.put(schemaType, schemaTypeLoader);
        rebuildSchemaTypeLoader();
    }

    public synchronized void removeDeploymentConfigurationFactory(SchemaType schemaType) {
        schemaTypeToFactoryMap.remove(schemaType);
        schemaTypeToLoaderMap.remove(schemaType);
        rebuildSchemaTypeLoader();
    }

    private void rebuildSchemaTypeLoader() {
        SchemaTypeLoader[] loaders = (SchemaTypeLoader[]) schemaTypeToLoaderMap.values().toArray(new SchemaTypeLoader[schemaTypeToLoaderMap.size()]);
        schemaTypeLoader = XmlBeans.typeLoaderUnion(loaders);
    }

    private void addFactory(ModuleType type, DeploymentConfigurationFactory factory) {
        if (factory != null) {
            configurationFactories.put(type, factory);
        }
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
        URI configId;
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
            configId = getConfigID(null);
        } catch (org.apache.xmlbeans.XmlException e) {
            return new FailedProgressObject(CommandType.DISTRIBUTE, "Could not parse deployment plan");
        } catch (java.io.IOException e) {
            return new FailedProgressObject(CommandType.DISTRIBUTE, "Could not read deployment plan");
        } catch (URISyntaxException e) {
            return new FailedProgressObject(CommandType.DISTRIBUTE, "Could not read construct configId URI");
        }
        SchemaType planType = plan.schemaType();
        DeploymentConfigurationFactory factory = (DeploymentConfigurationFactory) schemaTypeToFactoryMap.get(planType);
        DeploymentModule module = null;
        try {
            module = factory.createModule(moduleArchive, plan, configId, server.isLocal());
        } catch (DeploymentException e) {
            return new FailedProgressObject(CommandType.DISTRIBUTE, e.getMessage());
        }
        if (module == null) {
            return new FailedProgressObject(CommandType.DISTRIBUTE, "No deployer found for supplied plan");
        }
        return server.distribute(targetList, module, configId);


        //this won't get called.
        /*
        Document doc;
        try {
            DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = parser.parse(deploymentPlan);
        } catch (Exception e) {
            return new FailedProgressObject(CommandType.DISTRIBUTE, e.getMessage());
        }
        URI configID;
        try {
            configID = getConfigID(doc);
        } catch (URISyntaxException e) {
            return new FailedProgressObject(CommandType.DISTRIBUTE, e.getMessage());
        }
        DeploymentModule module = null;
        for (Iterator i = configurationFactories.values().iterator(); i.hasNext();) {
            DeploymentConfigurationFactory factory = (DeploymentConfigurationFactory) i.next();
            try {
                module = factory.createModule(moduleArchive, doc, configID);
            } catch (DeploymentException e) {
                return new FailedProgressObject(CommandType.DISTRIBUTE, e.getMessage());
            }
        }
        if (module == null) {
            return new FailedProgressObject(CommandType.DISTRIBUTE, "No deployer found for supplied plan");
        }
        return server.distribute(targetList, module, configID);
        */
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

    //should we be using this or reading configID from deploymentplan?
    private URI getConfigID(Document doc) throws URISyntaxException {
        String id = Long.toString(System.currentTimeMillis()); // unique enough one hopes
        return new URI(id);
    }

    public static final GBeanInfo GBEAN_INFO;

    public void setGBeanContext(GBeanContext context) {
    }

    public void doStart() throws WaitingException, Exception {
        for (Iterator iterator = configurationFactories.values().iterator(); iterator.hasNext();) {
            DeploymentConfigurationFactory factory = (DeploymentConfigurationFactory) iterator.next();
            addDeploymentConfigurationFactory(factory.getSchemaType(), factory.getSchemaTypeLoader(), factory);
        }
    }

    public void doStop() throws WaitingException, Exception {
        for (Iterator iterator = configurationFactories.values().iterator(); iterator.hasNext();) {
            DeploymentConfigurationFactory factory = (DeploymentConfigurationFactory) iterator.next();
            removeDeploymentConfigurationFactory(factory.getSchemaType());
        }
    }

    public void doFail() {
    }

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory("JSR88 Deployment Manager", DeploymentManagerImpl.class.getName());
        infoFactory.addOperation(new GOperationInfo("addDeploymentConfigurationFactory", new String[]{SchemaType.class.getName(), SchemaTypeLoader.class.getName(), DeploymentConfigurationFactory.class.getName()}));
        infoFactory.addOperation(new GOperationInfo("removeDeploymentConfigurationFactory", new String[]{SchemaType.class.getName()}));
        infoFactory.addReference(new GReferenceInfo("Server", DeploymentServer.class.getName()));
        infoFactory.addReference(new GReferenceInfo("Configurers", ModuleConfigurer.class));
        infoFactory.addReference(new GReferenceInfo("EARFactory", DeploymentConfigurationFactory.class.getName()));
        infoFactory.addReference(new GReferenceInfo("WARFactory", DeploymentConfigurationFactory.class.getName()));
        infoFactory.addReference(new GReferenceInfo("EJBFactory", DeploymentConfigurationFactory.class.getName()));
        infoFactory.addReference(new GReferenceInfo("RARFactory", DeploymentConfigurationFactory.class.getName()));
        infoFactory.addReference(new GReferenceInfo("CARFactory", DeploymentConfigurationFactory.class.getName()));
        infoFactory.setConstructor(new GConstructorInfo(
                Arrays.asList(new Object[]{"Server", "Configurers", "EARFactory", "WARFactory", "EJBFactory", "RARFactory", "CARFactory"}),
                Arrays.asList(new Object[]{DeploymentServer.class, Collection.class, DeploymentConfigurationFactory.class, DeploymentConfigurationFactory.class, DeploymentConfigurationFactory.class, DeploymentConfigurationFactory.class, DeploymentConfigurationFactory.class})
        ));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }
}
