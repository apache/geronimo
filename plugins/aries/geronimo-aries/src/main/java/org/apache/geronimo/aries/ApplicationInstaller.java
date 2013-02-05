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
package org.apache.geronimo.aries;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.zip.ZipInputStream;

import org.apache.aries.application.ApplicationMetadata;
import org.apache.aries.application.DeploymentMetadata;
import org.apache.aries.application.management.AriesApplication;
import org.apache.aries.application.management.AriesApplicationContext.ApplicationState;
import org.apache.aries.application.management.AriesApplicationListener;
import org.apache.aries.application.management.BundleInfo;
import org.apache.aries.application.utils.AppConstants;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.ConfigurationAlreadyExistsException;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.repository.AbstractRepository;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.util.BundleUtil;
import org.apache.geronimo.kernel.util.FileUtils;
import org.apache.geronimo.kernel.util.IOUtils;
import org.apache.geronimo.kernel.util.JarUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev:385232 $ $Date$
 */
@GBean
public class ApplicationInstaller implements GBeanLifecycle {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationInstaller.class);

    private Kernel kernel;
    private BundleContext bundleContext;
    private AbstractName abstractName;
    private ServiceRegistration registration;
    private ConfigurationManager configurationManager;
    private WebApplicationTracker webApplicationTracker;
    private Collection<? extends Repository> repositories;
    private Collection<ConfigurationStore> configurationStores;
    private Environment defaultEnvironment;
    private GeronimoApplicationManager applicationManager;
    
    public ApplicationInstaller(@ParamReference(name = "Store", namingType = "ConfigurationStore") Collection<ConfigurationStore> configurationStores,
                                @ParamReference(name = "Repositories", namingType = "Repository") Collection<? extends Repository> repositories,
                                @ParamAttribute(name = "defaultEnvironment") Environment defaultEnvironment,
                                @ParamSpecial(type = SpecialAttributeType.kernel) Kernel kernel,
                                @ParamSpecial(type = SpecialAttributeType.bundleContext) BundleContext bundleContext,
                                @ParamSpecial(type = SpecialAttributeType.abstractName) AbstractName abstractName)
        throws GBeanNotFoundException {
        this.kernel = kernel;
        this.bundleContext = bundleContext;
        this.abstractName = abstractName;
        this.repositories = repositories;
        this.configurationStores = configurationStores;
        this.configurationManager = ConfigurationUtil.getConfigurationManager(kernel);
        this.defaultEnvironment = defaultEnvironment;
        this.webApplicationTracker = new WebApplicationTracker(bundleContext);
        this.applicationManager = new GeronimoApplicationManager(bundleContext);
    }

    public void doStart() throws Exception {
        registration = bundleContext.registerService(ApplicationInstaller.class.getName(), this, null);
        webApplicationTracker.start();
        applicationManager.doStart();
        
        if (getUnpackApplicationBundles()) {
            for (Repository repository : repositories) {
                if (repository instanceof AbstractRepository) {
                    ((AbstractRepository) repository).setTypeHandler("eba", new UnpackEBATypeHandler());
                }
            }
        }        
    }

    public void doStop() {
        if (registration != null) {
            registration.unregister();
        }
        webApplicationTracker.stop();
        applicationManager.doStop();
    }

    public void doFail() {
        doStop();
    }

    public GeronimoApplicationManager getGeronimoApplicationManager() {
        return applicationManager;
    }
    
    protected ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }
    
    protected WebApplicationTracker getWebApplicationTracker() {
        return webApplicationTracker;
    }
    
    public DeploymentContext startInstall(AriesApplication app, File inPlaceLocation, ConfigurationStore targetConfigurationStore)
        throws ConfigurationAlreadyExistsException, IOException, DeploymentException {

        Artifact configId = getConfigId(app.getApplicationMetadata());

        targetConfigurationStore.createNewConfigurationDir(configId);

        Environment environment = new Environment();
        environment.setConfigId(configId);

        EnvironmentBuilder.mergeEnvironments(environment, defaultEnvironment);

        Naming naming = kernel.getNaming();
        AbstractName moduleName = naming.createRootName(configId, configId.toString(), "AriesApplication");
        //Use a temporary folder to hold the extracted files for analysis use
        File tempDirectory = FileUtils.createTempDir();
        try {
            DeploymentContext context = new DeploymentContext(tempDirectory,
                            null,
                            environment,
                            moduleName,
                            ConfigurationModuleType.EBA,
                            naming,
                            configurationManager,
                            null,
                            bundleContext);

            context.flush();
            context.initializeConfiguration();

            if (inPlaceLocation == null) {
                // UnpackEBATypeHandler will unpack the application bundles if necessary at install time 
                storeApplication(app, tempDirectory, false);
            } else {                
                storeInPlaceApplication(app, inPlaceLocation);
            }
            
            AbstractName name = naming.createChildName(moduleName, "AriesApplication", "GBean");
            GBeanData data = new GBeanData(name, ApplicationGBean.class);
            data.setAttribute("configId", configId);
            data.setAttribute("location", inPlaceLocation);
            data.setReferencePattern("Installer", abstractName);

            context.addGBean(data);
            
            return context;
        } catch (DeploymentException e) {
            throw e;
        } catch (Exception e) {
            throw new DeploymentException("Error deploying application", e);
        }
    }
    
    private static boolean getUnpackApplicationBundles() {
        String property = System.getProperty("org.apache.geronimo.aries.unpackApplicationBundles", "true");
        return Boolean.parseBoolean(property);
    }
    
    private void storeApplication(AriesApplication app, File directory, boolean unpack) throws IOException {
        ApplicationMetadata appMetadata = app.getApplicationMetadata();
        appMetadata.store(new File(directory, AppConstants.APPLICATION_MF));

        DeploymentMetadata deploymentMetadata = app.getDeploymentMetadata();
        if (deploymentMetadata != null) {
            deploymentMetadata.store(new File(directory, AppConstants.DEPLOYMENT_MF));
        }
        
        for (BundleInfo bi : app.getBundleInfo()) { 
          // bi.getLocation() will return a URL to the source bundle. It may be of the form
          // file:/path/to/my/file.jar, or
          // jar:file:/my/path/to/eba.jar!/myBundle.jar
          String bundleLocation = bi.getLocation();
          String bundleFileName = bundleLocation.substring(bundleLocation.lastIndexOf('/') + 1);
          InputStream in = null;
          try { 
              URL bundleURL = new URL(bundleLocation);
              in = bundleURL.openStream();
              File target = new File(directory, bundleFileName);
              if (unpack) {
                  target.mkdirs();
                  ZipInputStream zipIn = new ZipInputStream(in);
                  try {
                      JarUtils.unzipToDirectory(zipIn, target);
                  } finally {
                      IOUtils.close(zipIn);
                  }
              } else {
                  FileOutputStream fileOut = new FileOutputStream(target);
                  try {
                      IOUtils.copy(in, fileOut);
                  } finally {
                      IOUtils.close(fileOut);
                  }
              }            
          } finally {
              IOUtils.close(in);
          }
        }
    }
    
    private void storeInPlaceApplication(AriesApplication app, File inPlaceLocation) throws IOException {
        // save DEPLOYMENT.MF if it was not there before and application is resolved
        File deploymentMF = new File(inPlaceLocation, AppConstants.DEPLOYMENT_MF);
        if (!deploymentMF.exists()) {
            DeploymentMetadata deploymentMetadata = app.getDeploymentMetadata();
            if (deploymentMetadata != null) {
                deploymentMetadata.store(deploymentMF);
            }
        }
    }

    public ConfigurationData finishInstall(DeploymentContext context, ConfigurationStore targetConfigurationStore)
        throws ConfigurationAlreadyExistsException, DeploymentException {
        try {
            ConfigurationData configurationData = context.getConfigurationData();
            targetConfigurationStore.install(configurationData);
            return configurationData;
        } catch (Exception e) {
            throw new DeploymentException("Error installing application", e);
        } finally {
            try { context.close(); } catch (IOException e) {}
        }
    }

    public void install(AriesApplication app) throws ConfigurationAlreadyExistsException, IOException, DeploymentException {
        ConfigurationStore store = configurationStores.iterator().next();
        if (store == null) {
            throw new DeploymentException("No ConfigurationStore");
        }
        DeploymentContext context = startInstall(app, null, store);
        ConfigurationData configurationData = finishInstall(context, store);

        try {
            configurationManager.loadConfiguration(configurationData.getId());
            configurationManager.startConfiguration(configurationData.getId());
        } catch (Exception e) {
            throw new DeploymentException("Error installing application", e);
        }
    }

    public static Artifact getConfigId(ApplicationMetadata metadata) {
        return BundleUtil.createArtifact(BundleUtil.EBA_GROUP_ID, metadata.getApplicationSymbolicName(), metadata.getApplicationVersion());
    }

    protected File getApplicationLocation(Artifact artifactId) {
        for (Repository repository : repositories) {
            if (repository.contains(artifactId)) {
                return repository.getLocation(artifactId);
            }
        }
        return null;
    }
    
    protected void fireApplicationEvent(AriesApplication application, ApplicationState state) {
        Collection<ServiceReference<AriesApplicationListener>> references = null;
        try {
            references = bundleContext.getServiceReferences(AriesApplicationListener.class, null);
        } catch (InvalidSyntaxException e) {
            // this can't happen
            throw new Error(e);
        }
        if (references != null && !references.isEmpty()) {
            GeronimoApplicationEvent event = new GeronimoApplicationEvent(application, state);
            for (ServiceReference<AriesApplicationListener> reference : references) {
                AriesApplicationListener listener = bundleContext.getService(reference);
                if (listener != null) {
                    try {
                        listener.applicationChanged(event);
                    } catch (Exception e) {
                        LOG.debug("Error calling AriesApplicationListener", e);
                    } finally {
                        bundleContext.ungetService(reference);
                    }
                }
            }
        }
    }

}
