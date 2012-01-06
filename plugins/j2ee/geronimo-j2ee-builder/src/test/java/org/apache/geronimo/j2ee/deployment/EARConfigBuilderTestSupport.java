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
package org.apache.geronimo.j2ee.deployment;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.deployment.NamespaceDrivenBuilder;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.jndi.JndiKey;
import org.apache.geronimo.j2ee.management.impl.J2EEServerImpl;
import org.apache.geronimo.kernel.Jsr77Naming;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.mock.MockConfigStore;
import org.apache.geronimo.kernel.mock.MockConfigurationManager;
import org.apache.geronimo.kernel.mock.MockRepository;
import org.apache.geronimo.kernel.osgi.MockBundleContext;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactManager;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.DefaultArtifactManager;
import org.apache.geronimo.kernel.repository.DefaultArtifactResolver;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.ImportType;
import org.apache.geronimo.kernel.repository.ListableRepository;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.util.FileUtils;
import org.apache.geronimo.system.configuration.DependencyManager;
import org.apache.geronimo.system.configuration.OsgiMetaDataProvider;
import org.apache.geronimo.testsupport.TestSupport;
import org.apache.xbean.osgi.bundle.util.BundleDescription.ExportPackage;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * Provides support for EAR config builder tests.
 *
 * @version $Rev:386276 $ $Date$
 */
public abstract class EARConfigBuilderTestSupport
    extends TestSupport
{
    protected static String WEB_NAMESPACE = "foo";

    protected static JarFile earFile;

    protected static MockConfigStore configStore = new MockConfigStore();

    protected static ArtifactManager artifactManager = new DefaultArtifactManager();

    protected static MockEJBConfigBuilder ejbConfigBuilder = new MockEJBConfigBuilder();

    protected static MockWARConfigBuilder webConfigBuilder = new MockWARConfigBuilder();

    protected static MockConnectorConfigBuilder connectorConfigBuilder = new MockConnectorConfigBuilder();

    protected static ActivationSpecInfoLocator activationSpecInfoLocator = connectorConfigBuilder;

    protected static ModuleBuilder appClientConfigBuilder = null;

    protected final static ModuleIDBuilder idBuilder = new ModuleIDBuilder();

    protected static final NamespaceDrivenBuilder securityBuilder = null;

    protected static final NamespaceDrivenBuilder serviceBuilder = null;
    
    protected static final ModuleBuilderExtension BValModuleBuilder = null;

    protected static final ModuleBuilderExtension persistenceUnitBuilder = null;

    protected static final NamingBuilder namingBuilder = new NamingBuilderCollection(Collections.<NamingBuilder>emptySet());

    protected static final Naming naming = new Jsr77Naming();

    protected static final AbstractName rootConfig = naming.createRootName(new Artifact("test", "stuff", "", "car"), "test", "test") ;

    protected static final AbstractName transactionManagerObjectName = naming.createChildName(rootConfig, "TransactionManager", "TransactionManager");

    protected static final AbstractName connectionTrackerObjectName = naming.createChildName(rootConfig, "ConnectionTracker", "ConnectionTracker");

    protected static final AbstractName transactionalTimerObjectName = naming.createChildName(rootConfig, "TransactionalThreaPooledTimer", "ThreadPooledTimer");

    protected static final AbstractName nonTransactionalTimerObjectName = naming.createChildName(rootConfig, "NonTransactionalThreaPooledTimer", "ThreadPooledTimer");

    protected static final AbstractName globalContextAbstractName = naming.createChildName(rootConfig, "GlobalContext", "GlobalContext");

    protected static final AbstractName serverName = naming.createChildName(rootConfig, "J2EEServer", "Server");

    protected static final AbstractName earName = naming.createRootName(new Artifact("org.apache.geronimo.test.test-ear", "ear", "", "ear"), "test", NameFactory.J2EE_APPLICATION);

    protected static final AbstractName ejbModuleName = naming.createChildName(earName, "ejb-jar", NameFactory.EJB_MODULE);

    protected static final AbstractName webModuleName = naming.createChildName(earName, "war", NameFactory.WEB_MODULE);

    protected static final AbstractName raModuleName = naming.createChildName(earName, "rar", NameFactory.RESOURCE_ADAPTER_MODULE);


    protected Map<String, Artifact> locations = new HashMap<String, Artifact>();

    protected Environment defaultParentId;

    protected static String contextRoot = "test";

    protected static final Map portMap = null;

    protected final AbstractNameQuery transactionManagerAbstractNameQuery = new AbstractNameQuery(transactionManagerObjectName, null);

    protected final AbstractNameQuery connectionTrackerAbstractNameQuery = new AbstractNameQuery(connectionTrackerObjectName, null);

    protected final AbstractNameQuery globalContextAbstractNameQuery = new AbstractNameQuery(globalContextAbstractName, null);

    protected final AbstractNameQuery corbaGBeanAbstractNameQuery = new AbstractNameQuery(serverName, null);

    private ListableRepository repository;
    protected ArtifactResolver artifactResolver = new DefaultArtifactResolver(artifactManager, repository);
    protected Collection<? extends ArtifactResolver> artifactResolvers = Collections.singleton(new DefaultArtifactResolver(artifactManager, repository));
    protected Map<JndiKey, Map<String, Object>> jndiContext;
    protected Module parentModule;

    protected void setUp() throws Exception {
        super.setUp();
        bundleContext = new MockBundleContext(getClass().getClassLoader(), "", new HashMap<Artifact, ConfigurationData>(), locations);
        ((MockBundleContext)bundleContext).setConfigurationManager(new MockConfigurationManager());
        bundleContext.registerService(OsgiMetaDataProvider.class.getName(), new MockDependencyManager(bundleContext, Collections.<Repository> emptyList(), null), new Hashtable());
        Set<Artifact> repo = new HashSet<Artifact>();
        repo.add(Artifact.create("org.apache.geronimo.tests/test/1/car"));
        repository = new MockRepository(repo);
        defaultParentId = new Environment();
        defaultParentId.addDependency(new Artifact("org.apache.geronimo.tests", "test", "1", "car"), ImportType.ALL);
    }

    protected void tearDown() throws Exception {
        configStore.cleanup();
        super.tearDown();
    }

    public void testBuildConfiguration() throws Exception {
        ConfigurationData configurationData = null;
        DeploymentContext context = null;
        try {
            EARConfigBuilder configBuilder = new EARConfigBuilder(defaultParentId,
                    transactionManagerAbstractNameQuery,
                    connectionTrackerAbstractNameQuery,
                    corbaGBeanAbstractNameQuery,
                    null,
                    globalContextAbstractNameQuery,
                    Collections.singleton(repository),
                    ejbConfigBuilder,
                    webConfigBuilder,
                    connectorConfigBuilder,
                    activationSpecInfoLocator,
                    appClientConfigBuilder,
                    serviceBuilder,
                    BValModuleBuilder,
                    persistenceUnitBuilder,
                    namingBuilder,
                    naming,
                    artifactResolvers,
                    bundleContext);

            Object plan = configBuilder.getDeploymentPlan(null, earFile, idBuilder);
            context = configBuilder.buildConfiguration(false, configBuilder.getConfigurationID(plan, earFile, idBuilder), plan, earFile, Collections.singleton(configStore), artifactResolver, configStore);
            configurationData = getConfigurationData(context);
        } finally {
            if (context != null) {
                context.close();
            }
            if (configurationData != null) {
                FileUtils.recursiveDelete(configurationData.getConfigurationDir());
            }
        }
    }

    public void testBadEJBJARConfiguration() throws Exception {
        EARConfigBuilder configBuilder = new EARConfigBuilder(defaultParentId,
                transactionManagerAbstractNameQuery,
                connectionTrackerAbstractNameQuery,
                corbaGBeanAbstractNameQuery,
                null,
                globalContextAbstractNameQuery,
                null,
                ejbConfigBuilder,
                webConfigBuilder,
                connectorConfigBuilder,
                activationSpecInfoLocator,
                appClientConfigBuilder,
                serviceBuilder,
                BValModuleBuilder,
                persistenceUnitBuilder,
                namingBuilder,
                naming,
                artifactResolvers,
                bundleContext);

        ConfigurationData configurationData = null;
        DeploymentContext context = null;
        try {
            Object plan = configBuilder.getDeploymentPlan(resolveFile("src/test/resources/plans/test-bad-ejb-jar.xml"), earFile, idBuilder);
            context = configBuilder.buildConfiguration(false, configBuilder.getConfigurationID(plan, earFile, idBuilder), plan, earFile, Collections.singleton(configStore), artifactResolver, configStore);
            configurationData = getConfigurationData(context);
            fail("Should have thrown a DeploymentException");
        } catch (DeploymentException e) {
            //we now may get a FileNotFoundException for missing wars.
        } finally {
            if (context != null) {
                context.close();
            }
            if (configurationData != null) {
                FileUtils.recursiveDelete(configurationData.getConfigurationDir());
            }
        }
    }

    public void testBadWARConfiguration() throws Exception {
        EARConfigBuilder configBuilder = new EARConfigBuilder(defaultParentId,
                transactionManagerAbstractNameQuery,
                connectionTrackerAbstractNameQuery,
                corbaGBeanAbstractNameQuery,
                null,
                globalContextAbstractNameQuery,
                null,
                ejbConfigBuilder,
                webConfigBuilder,
                connectorConfigBuilder,
                activationSpecInfoLocator,
                appClientConfigBuilder,
                serviceBuilder,
                BValModuleBuilder,
                persistenceUnitBuilder,
                namingBuilder,
                naming,
                artifactResolvers,
                bundleContext);

        ConfigurationData configurationData = null;
        DeploymentContext context = null;
        try {
            Object plan = configBuilder.getDeploymentPlan(resolveFile("src/test/resources/plans/test-bad-war.xml"), earFile, idBuilder);
            context = configBuilder.buildConfiguration(false, configBuilder.getConfigurationID(plan, earFile, idBuilder), plan, earFile, Collections.singleton(configStore), artifactResolver, configStore);
            configurationData = getConfigurationData(context);
            fail("Should have thrown a DeploymentException");
        } catch (DeploymentException e) {
            //we now may get a FileNotFoundException for missing wars.
        } finally {
            if (context != null) {
                context.close();
            }
            if (configurationData != null) {
                FileUtils.recursiveDelete(configurationData.getConfigurationDir());
            }
        }
    }

    public void testBadRARConfiguration() throws Exception {
        EARConfigBuilder configBuilder = new EARConfigBuilder(defaultParentId,
                transactionManagerAbstractNameQuery,
                connectionTrackerAbstractNameQuery,
                corbaGBeanAbstractNameQuery,
                null,
                globalContextAbstractNameQuery,
                null,
                ejbConfigBuilder,
                webConfigBuilder,
                connectorConfigBuilder,
                activationSpecInfoLocator,
                appClientConfigBuilder,
                serviceBuilder,
                BValModuleBuilder,
                persistenceUnitBuilder,
                namingBuilder,
                naming,
                artifactResolvers,
                bundleContext);

        ConfigurationData configurationData = null;
        DeploymentContext context = null;
        try {
            Object plan = configBuilder.getDeploymentPlan(resolveFile("src/test/resources/plans/test-bad-rar.xml"), earFile, idBuilder);
            context = configBuilder.buildConfiguration(false, configBuilder.getConfigurationID(plan, earFile, idBuilder), plan, earFile, Collections.singleton(configStore), artifactResolver, configStore);
            configurationData = getConfigurationData(context);
            fail("Should have thrown a DeploymentException");
        } catch (DeploymentException e) {
            //we now may get a FileNotFoundException for missing wars.
        } finally {
            if (context != null) {
                context.close();
            }
            if (configurationData != null) {
                FileUtils.recursiveDelete(configurationData.getConfigurationDir());
            }
        }
    }

    public void testBadCARConfiguration() throws Exception {
        EARConfigBuilder configBuilder = new EARConfigBuilder(defaultParentId,
                transactionManagerAbstractNameQuery,
                connectionTrackerAbstractNameQuery,
                corbaGBeanAbstractNameQuery,
                null,
                globalContextAbstractNameQuery,
                null,
                ejbConfigBuilder,
                webConfigBuilder,
                connectorConfigBuilder,
                activationSpecInfoLocator,
                appClientConfigBuilder,
                serviceBuilder,
                BValModuleBuilder,
                persistenceUnitBuilder,
                namingBuilder,
                naming,
                artifactResolvers,
                bundleContext);

        ConfigurationData configurationData = null;
        DeploymentContext context = null;
        try {
            Object plan = configBuilder.getDeploymentPlan(resolveFile("src/test/resources/plans/test-bad-car.xml"), earFile, idBuilder);
            context = configBuilder.buildConfiguration(false, configBuilder.getConfigurationID(plan, earFile, idBuilder), plan, earFile, Collections.singleton(configStore), artifactResolver, configStore);
            configurationData = getConfigurationData(context);
            fail("Should have thrown a DeploymentException");
        } catch (DeploymentException e) {
            //we now may get a FileNotFoundException for missing wars.
        } finally {
            if (context != null) {
                context.close();
            }
            if (configurationData != null) {
                FileUtils.recursiveDelete(configurationData.getConfigurationDir());
            }
        }
    }

    public void testNoEJBDeployer() throws Exception {
        EARConfigBuilder configBuilder = new EARConfigBuilder(defaultParentId,
                transactionManagerAbstractNameQuery,
                connectionTrackerAbstractNameQuery,
                corbaGBeanAbstractNameQuery,
                null,
                globalContextAbstractNameQuery,
                null,
                null,
                webConfigBuilder,
                connectorConfigBuilder,
                activationSpecInfoLocator,
                appClientConfigBuilder,
                serviceBuilder,
                BValModuleBuilder,
                persistenceUnitBuilder,
                namingBuilder,
                naming,
                artifactResolvers,
                bundleContext);


        ConfigurationData configurationData = null;
        DeploymentContext context = null;
        try {
            Object plan = configBuilder.getDeploymentPlan(null, earFile, idBuilder);
            context = configBuilder.buildConfiguration(false, configBuilder.getConfigurationID(plan, earFile, idBuilder), plan, earFile, Collections.singleton(configStore), artifactResolver, configStore);
            configurationData = getConfigurationData(context);
            fail("Should have thrown a DeploymentException");
        } catch (DeploymentException e) {
            // expected
        } finally {
            if (context != null) {
                context.close();
            }
            if (configurationData != null) {
                FileUtils.recursiveDelete(configurationData.getConfigurationDir());
            }
        }
    }

    public void testNoWARDeployer() throws Exception {
        EARConfigBuilder configBuilder = new EARConfigBuilder(defaultParentId,
                transactionManagerAbstractNameQuery,
                connectionTrackerAbstractNameQuery,
                corbaGBeanAbstractNameQuery,
                null,
                globalContextAbstractNameQuery,
                null,
                ejbConfigBuilder,
                null,
                connectorConfigBuilder,
                activationSpecInfoLocator,
                appClientConfigBuilder,
                serviceBuilder,
                BValModuleBuilder,
                persistenceUnitBuilder,
                namingBuilder,
                naming,
                artifactResolvers,
                bundleContext);

        ConfigurationData configurationData = null;
        DeploymentContext context = null;
        try {
            Object plan = configBuilder.getDeploymentPlan(null, earFile, idBuilder);
            context = configBuilder.buildConfiguration(false, configBuilder.getConfigurationID(plan, earFile, idBuilder), plan, earFile, Collections.singleton(configStore), artifactResolver, configStore);
            configurationData = getConfigurationData(context);
            fail("Should have thrown a DeploymentException");
        } catch (DeploymentException e) {
            // expected
        } finally {
            if (context != null) {
                context.close();
            }
            if (configurationData != null) {
                FileUtils.recursiveDelete(configurationData.getConfigurationDir());
            }
        }
    }

    public void testNoConnectorDeployer() throws Exception {
        EARConfigBuilder configBuilder = new EARConfigBuilder(defaultParentId,
                transactionManagerAbstractNameQuery,
                connectionTrackerAbstractNameQuery,
                corbaGBeanAbstractNameQuery,
                null,
                globalContextAbstractNameQuery,
                null,
                ejbConfigBuilder,
                webConfigBuilder,
                null,
                activationSpecInfoLocator,
                appClientConfigBuilder,
                serviceBuilder,
                BValModuleBuilder,
                persistenceUnitBuilder,
                namingBuilder,
                naming,
                artifactResolvers,
                bundleContext);

        ConfigurationData configurationData = null;
        DeploymentContext context = null;
        try {
            Object plan = configBuilder.getDeploymentPlan(null, earFile, idBuilder);
            context = configBuilder.buildConfiguration(false, configBuilder.getConfigurationID(plan, earFile, idBuilder), plan, earFile, Collections.singleton(configStore), artifactResolver, configStore);
            configurationData = getConfigurationData(context);
            fail("Should have thrown a DeploymentException");
        } catch (DeploymentException e) {
            // expected
        } finally {
            if (context != null) {
                context.close();
            }
            if (configurationData != null) {
                FileUtils.recursiveDelete(configurationData.getConfigurationDir());
            }
        }
    }

    protected ConfigurationData getConfigurationData(DeploymentContext context) throws Exception {
        // add the a j2ee server so the application context reference can be resolved
        context.addGBean("geronimo", J2EEServerImpl.GBEAN_INFO);

        return context.getConfigurationData();
    }

    protected static void close(Module module) {
        if (module != null) {
            module.close();
        }
    }

    private class MockDependencyManager extends DependencyManager {

        public MockDependencyManager(BundleContext bundleContext, Collection<Repository> repositories, ArtifactResolver artifactResolver) {
            super(bundleContext, repositories, artifactResolver);
        }

        @Override
        public synchronized Set<ExportPackage> getExportedPackages(Bundle bundle) {
           return Collections.<ExportPackage>emptySet();
        }

        @Override
        public List<Bundle> getDependentBundles(Bundle bundle) {
            return Collections.<Bundle>emptyList();
        }

        @Override
        public Bundle getBundle(Artifact artifact) {
            return null;
        }

    }
}
