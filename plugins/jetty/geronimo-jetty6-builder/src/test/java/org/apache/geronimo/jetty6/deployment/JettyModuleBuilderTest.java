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
package org.apache.geronimo.jetty6.deployment;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;

import org.apache.geronimo.testsupport.TestSupport;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTrackingCoordinatorGBean;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.deployment.service.GBeanBuilder;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.deployment.util.UnpackedJarFile;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.UnavailableWebServiceBuilder;
import org.apache.geronimo.j2ee.deployment.WebServiceBuilder;
import org.apache.geronimo.j2ee.deployment.NamingBuilderCollection;
import org.apache.geronimo.j2ee.deployment.ModuleBuilderExtension;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.management.impl.J2EEServerImpl;
import org.apache.geronimo.jetty6.JettyContainerImpl;
import org.apache.geronimo.jetty6.connector.HTTPSocketConnector;
import org.apache.geronimo.kernel.Jsr77Naming;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.mock.MockConfigStore;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.EditableKernelConfigurationManager;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.config.NullConfigurationStore;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactManager;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.DefaultArtifactManager;
import org.apache.geronimo.kernel.repository.DefaultArtifactResolver;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.ImportType;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.security.deployment.GeronimoSecurityBuilderImpl;
import org.apache.geronimo.system.serverinfo.BasicServerInfo;
import org.apache.geronimo.transaction.manager.GeronimoTransactionManagerGBean;
import org.apache.geronimo.xbeans.geronimo.j2ee.GerSecurityDocument;
import org.apache.xmlbeans.impl.schema.SchemaTypeImpl;

/**
 * @version $Rev:385232 $ $Date$
 */
public class JettyModuleBuilderTest extends TestSupport {

    private static Naming naming = new Jsr77Naming();
    private Artifact baseId = new Artifact("test", "base", "1", "car");
    private final AbstractName serverName = naming.createRootName(baseId, "Server", "J2EEServer");

    protected Kernel kernel;
    private AbstractName tmName;
    private AbstractName ctcName;
    private ClassLoader cl;
    private JettyModuleBuilder builder;
    private Artifact webModuleArtifact = new Artifact("foo", "bar", "1", "car");
    private Environment defaultEnvironment = new Environment();
    private ConfigurationManager configurationManager;
    private ConfigurationStore configStore;

    public void testDeployWar4() throws Exception {
        File outputPath = new File(BASEDIR, "target/test-resources/deployables/war4");
        recursiveDelete(outputPath);
        outputPath.mkdirs();
        new File(outputPath, "war").mkdir();
        File path = new File(BASEDIR, "src/test/resources/deployables/war4");
        UnpackedJarFile jarFile = new UnpackedJarFile(path);
        Module module = builder.createModule(null, jarFile, kernel.getNaming(), new ModuleIDBuilder());
        Repository repository = null;

        AbstractName moduleName = module.getModuleName();
        EARContext earContext = createEARContext(outputPath, defaultEnvironment, repository, configStore, moduleName);
        module.setEarContext(earContext);
        module.setRootEarContext(earContext);
        builder.initContext(earContext, module, cl);
        builder.addGBeans(earContext, module, cl, Collections.EMPTY_SET);
        ConfigurationData configurationData = earContext.getConfigurationData();
        earContext.close();
        module.close();

        Artifact configurationId = configurationData.getId();
        configurationManager.loadConfiguration(configurationData);
        Configuration configuration = configurationManager.getConfiguration(configurationId);
        configurationManager.startConfiguration(configurationId);

        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(moduleName));
        Set names = configuration.findGBeans(new AbstractNameQuery(moduleName.getArtifact(), Collections.EMPTY_MAP));
        log.debug("names: " + names);
        for (Iterator iterator = names.iterator(); iterator.hasNext();) {
            AbstractName objectName = (AbstractName) iterator.next();
            assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(objectName));
        }

        configurationManager.stopConfiguration(configurationId);
        configurationManager.unloadConfiguration(configurationId);

    }

    public void testContextRootWithSpaces() throws Exception {
        File outputPath = new File(BASEDIR, "target/test-resources/deployables/war-spaces-in-context");
        recursiveDelete(outputPath);
        outputPath.mkdirs();
        new File(outputPath, "war").mkdir();
        File path = new File(BASEDIR, "src/test/resources/deployables/war-spaces-in-context");
        UnpackedJarFile jarFile = new UnpackedJarFile(path);
        Module module = builder.createModule(null, jarFile, kernel.getNaming(), new ModuleIDBuilder());
        Repository repository = null;

        AbstractName moduleName = module.getModuleName();
        EARContext earContext = createEARContext(outputPath, defaultEnvironment, repository, configStore, moduleName);
        module.setEarContext(earContext);
        module.setRootEarContext(earContext);
        builder.initContext(earContext, module, cl);
        builder.addGBeans(earContext, module, cl, Collections.EMPTY_SET);
        ConfigurationData configurationData = earContext.getConfigurationData();
        earContext.close();
        module.close();

        Artifact configurationId = configurationData.getId();
        configurationManager.loadConfiguration(configurationData);
        Configuration configuration = configurationManager.getConfiguration(configurationId);
        configurationManager.startConfiguration(configurationId);

        String contextRoot = (String) kernel.getAttribute(moduleName, "contextPath");
        assertNotNull(contextRoot);
        assertEquals(contextRoot, contextRoot.trim());

        configurationManager.stopConfiguration(configurationId);
        configurationManager.unloadConfiguration(configurationId);

    }

    private EARContext createEARContext(File outputPath, Environment environment, Repository repository, ConfigurationStore configStore, AbstractName moduleName) throws DeploymentException {
        Set repositories = repository == null ? Collections.EMPTY_SET : Collections.singleton(repository);
        ArtifactManager artifactManager = new DefaultArtifactManager();
        return new EARContext(outputPath,
                null,
                environment,
                ConfigurationModuleType.WAR,
                naming,
                configurationManager,
                repositories,
                new AbstractNameQuery(serverName),
                moduleName,
                new AbstractNameQuery(tmName),
                new AbstractNameQuery(ctcName),
                null
        );
    }

    private void recursiveDelete(File path) {
        //does not delete top level dir passed in
        File[] listing = path.listFiles();
        if (listing != null) {
            for (int i = 0; i < listing.length; i++) {
                File file = listing[i];
                if (file.isDirectory()) {
                    recursiveDelete(file);
                }
                file.delete();
            }
        }
    }

    protected void setUp() throws Exception {
        super.setUp();

        ((SchemaTypeImpl) GerSecurityDocument.type).addSubstitutionGroupMember(org.apache.geronimo.xbeans.geronimo.security.GerSecurityDocument.type.getDocumentElementName());

        cl = this.getClass().getClassLoader();
        kernel = KernelFactory.newInstance().createKernel("test");
        kernel.boot();

        ConfigurationData bootstrap = new ConfigurationData(baseId, naming);

        GBeanData serverInfo = bootstrap.addGBean("ServerInfo", BasicServerInfo.GBEAN_INFO);
        serverInfo.setAttribute("baseDirectory", ".");

        AbstractName configStoreName = bootstrap.addGBean("MockConfigurationStore", MockConfigStore.GBEAN_INFO).getAbstractName();

        GBeanData artifactManagerData = bootstrap.addGBean("ArtifactManager", DefaultArtifactManager.GBEAN_INFO);

        GBeanData artifactResolverData = bootstrap.addGBean("ArtifactResolver", DefaultArtifactResolver.GBEAN_INFO);
        artifactResolverData.setReferencePattern("ArtifactManager", artifactManagerData.getAbstractName());

        GBeanData configurationManagerData = bootstrap.addGBean("ConfigurationManager", EditableKernelConfigurationManager.GBEAN_INFO);
        configurationManagerData.setReferencePattern("ArtifactManager", artifactManagerData.getAbstractName());
        configurationManagerData.setReferencePattern("ArtifactResolver", artifactResolverData.getAbstractName());
        configurationManagerData.setReferencePattern("Stores", configStoreName);
        bootstrap.addGBean(configurationManagerData);

        GBeanData serverData = new GBeanData(serverName, J2EEServerImpl.GBEAN_INFO);
        bootstrap.addGBean(serverData);

        Collection defaultServlets = new HashSet();
        Collection defaultFilters = new HashSet();
        Collection defaultFilterMappings = new HashSet();
        Object pojoWebServiceTemplate = null;
        WebServiceBuilder webServiceBuilder = new UnavailableWebServiceBuilder();

        GBeanData containerData = bootstrap.addGBean("JettyContainer", JettyContainerImpl.GBEAN_INFO);
        AbstractName containerName = containerData.getAbstractName();
        containerData.setAttribute("jettyHome", new File(BASEDIR, "target/var/jetty").toString());
        containerData.setReferencePattern("ServerInfo", serverInfo.getAbstractName());

        GBeanData connector = bootstrap.addGBean("JettyConnector", HTTPSocketConnector.GBEAN_INFO);
        connector.setAttribute("port", new Integer(5678));
        connector.setAttribute("maxThreads", new Integer(50));
//        connector.setAttribute("minThreads", new Integer(10));
        connector.setReferencePattern("JettyContainer", containerName);

        GBeanData tm = bootstrap.addGBean("TransactionManager", GeronimoTransactionManagerGBean.GBEAN_INFO);
        tmName = tm.getAbstractName();
        tm.setAttribute("defaultTransactionTimeoutSeconds", new Integer(10));

        GBeanData ctc = bootstrap.addGBean("ConnectionTrackingCoordinator", ConnectionTrackingCoordinatorGBean.GBEAN_INFO);
        ctcName = ctc.getAbstractName();
        ctc.setReferencePattern("TransactionManager", tmName);

        ConfigurationUtil.loadBootstrapConfiguration(kernel, bootstrap, getClass().getClassLoader());

        configurationManager = ConfigurationUtil.getEditableConfigurationManager(kernel);
        configStore = (ConfigurationStore) kernel.getGBean(configStoreName);
        configStore.install(bootstrap);

        defaultEnvironment.addDependency(baseId, ImportType.ALL);
        defaultEnvironment.setConfigId(webModuleArtifact);
        Collection<ModuleBuilderExtension> moduleBuilderExtensions = new ArrayList<ModuleBuilderExtension>();
        builder = new JettyModuleBuilder(defaultEnvironment,
                new Integer(1800),
                Collections.EMPTY_LIST,
                new AbstractNameQuery(containerName),
                //new AbstractNameQuery(containerName),
                null, defaultServlets,
                defaultFilters,
                defaultFilterMappings,
                null,
                null,
                pojoWebServiceTemplate,
                Collections.singleton(webServiceBuilder),
                null,
                Collections.singleton(new GeronimoSecurityBuilderImpl(null)),
                Collections.singleton(new GBeanBuilder(null, null)),
                new NamingBuilderCollection(null, null),
                moduleBuilderExtensions,
                new MockResourceEnvironmentSetter(),
                kernel);
        builder.doStart();
    }

    protected void tearDown() throws Exception {
        builder.doStop();
        kernel.shutdown();
        super.tearDown();
    }

}
