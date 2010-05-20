/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.openejb.deployment.cluster;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarFile;

import javax.xml.namespace.QName;

import org.apache.geronimo.clustering.wadi.BasicWADISessionManager;
import org.apache.geronimo.clustering.wadi.WADISessionManagerConfigInfo;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.deployment.NamespaceDrivenBuilder;
import org.apache.geronimo.deployment.NamespaceDrivenBuilderCollection;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ModuleBuilderExtension;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.naming.deployment.ENCConfigBuilder;
import org.apache.geronimo.openejb.cluster.infra.BasicNetworkConnectorTrackerServiceHolder;
import org.apache.geronimo.openejb.cluster.infra.NetworkConnectorMonitor;
import org.apache.geronimo.openejb.cluster.stateful.deployment.ClusteredStatefulDeployment;
import org.apache.geronimo.openejb.deployment.BasicEjbDeploymentGBeanNameBuilder;
import org.apache.geronimo.openejb.deployment.EjbDeploymentGBeanNameBuilder;
import org.apache.geronimo.openejb.deployment.EjbModule;
import org.apache.geronimo.openejb.deployment.XmlUtil;
import org.apache.geronimo.openejb.xbeans.ejbjar.OpenejbGeronimoEjbJarType;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.xbeans.openejb.cluster.wadi.GerOpenejbClusteringWadiDocument;
import org.apache.geronimo.xbeans.openejb.cluster.wadi.GerOpenejbClusteringWadiType;
import org.apache.geronimo.xbeans.geronimo.j2ee.GerClusteringDocument;
import org.apache.geronimo.xbeans.geronimo.naming.GerPatternType;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.SessionBean;
import org.apache.openejb.jee.oejb2.GeronimoEjbJarType;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlObject;

import org.osgi.framework.Bundle;

/**
 *
 * @version $Rev:$ $Date:$
 */
public class WADIOpenEJBClusteringBuilder implements ModuleBuilderExtension {
    private static final QName BASE_CLUSTERING_QNAME = GerClusteringDocument.type.getDocumentElementName();
    private static final QName CLUSTERING_WADI_QNAME = GerOpenejbClusteringWadiDocument.type.getDocumentElementName();
    private static final QNameSet CLUSTERING_WADI_QNAME_SET = QNameSet.singleton(CLUSTERING_WADI_QNAME);

    static {
        SchemaConversionUtils.registerNamespaceConversions(
            Collections.singletonMap(CLUSTERING_WADI_QNAME.getLocalPart(),
            new OpenEJBClusteringWADIConverter()));
    }

    private final String defaultClusteredStatefulContainerId;
    private final EjbDeploymentGBeanNameBuilder beanNameBuilder;
    private final int defaultSweepInterval;
    private final int defaultSessionTimeout;
    private final int defaultNumPartitions;
    private final AbstractNameQuery defaultBackingStrategyFactoryName;
    private final AbstractNameQuery defaultClusterName;
    private final AbstractNameQuery defaultNetworkConnectorName;
    private final Environment defaultEnvironment;

    public WADIOpenEJBClusteringBuilder(String defaultClusteredStatefulContainerId,
        int defaultSweepInterval,
        int defaultSessionTimeout,
        int defaultNumPartitions,
        AbstractNameQuery defaultBackingStrategyFactoryName,
        AbstractNameQuery defaultClusterName,
        AbstractNameQuery defaultNetworkConnectorName,
        Environment defaultEnvironment) {
        if (null == defaultClusteredStatefulContainerId) {
            throw new IllegalArgumentException("defaultClusteredStatefulContainerId is required");
        } else if (defaultSweepInterval < 1) {
            throw new IllegalArgumentException("defaultSweepInterval is lower than 1");
        } else if (defaultSessionTimeout < 1) {
            throw new IllegalArgumentException("defaultSessionTimeout is lower than 1");
        } else if (defaultNumPartitions < 1) {
            throw new IllegalArgumentException("defaultNumPartitions is lower than 1");
        } else if (null == defaultBackingStrategyFactoryName) {
            throw new IllegalArgumentException("defaultBackingStrategyFactoryName is required");
        } else if (null == defaultClusterName) {
            throw new IllegalArgumentException("defaultClusterName is required");
        } else if (null == defaultEnvironment) {
            throw new IllegalArgumentException("defaultEnvironment is required");
        } else if (null == defaultNetworkConnectorName) {
            throw new IllegalArgumentException("defaultNetworkConnectorName is required");
        }
        this.defaultClusteredStatefulContainerId = defaultClusteredStatefulContainerId;
        this.defaultSweepInterval = defaultSweepInterval;
        this.defaultSessionTimeout = defaultSessionTimeout;
        this.defaultNumPartitions = defaultNumPartitions;
        this.defaultBackingStrategyFactoryName = defaultBackingStrategyFactoryName;
        this.defaultClusterName = defaultClusterName;
        this.defaultNetworkConnectorName = defaultNetworkConnectorName;
        this.defaultEnvironment = defaultEnvironment;

        beanNameBuilder = new BasicEjbDeploymentGBeanNameBuilder();

        new NamespaceDrivenBuilderCollection(Collections.<NamespaceDrivenBuilder>singleton(new NamespaceDrivenBuilder() {
            public void build(XmlObject container, DeploymentContext applicationContext, DeploymentContext moduleContext)
                    throws DeploymentException {
            }

            public void buildEnvironment(XmlObject container, Environment environment) throws DeploymentException {
            }

            public QNameSet getPlanQNameSet() {
                return CLUSTERING_WADI_QNAME_SET;
            }

            public QNameSet getSpecQNameSet() {
                return QNameSet.EMPTY;
            }

            public QName getBaseQName() {
                return BASE_CLUSTERING_QNAME;
            }

         }));
    }

    public void addGBeans(EARContext earContext, Module module, Bundle bundle, Collection repository)
            throws DeploymentException {
        EjbModule ejbModule = (EjbModule) module;

        OpenejbGeronimoEjbJarType geronimoEjbJarType = ejbModule.getVendorDD();
        GerOpenejbClusteringWadiType clusteringWadiType = getWadiClusterConfig(geronimoEjbJarType);
        if (clusteringWadiType != null) {
            AbstractName sessionManagerName = addSessionManager(clusteringWadiType, ejbModule, earContext);

            addNetworkConnectorMonitor(earContext, sessionManagerName);

            EjbJar ejbJar = ejbModule.getEjbJar();
            for (EnterpriseBean enterpriseBean : ejbJar.getEnterpriseBeans()) {
                if (enterpriseBean instanceof SessionBean) {
                    SessionBean sessionBean = (SessionBean) enterpriseBean;
                    switch (sessionBean.getSessionType()) {
                        case STATEFUL:
                            replaceByClusteredDeploymentGBean(earContext,
                                ejbModule,
                                sessionManagerName,
                                enterpriseBean);
                    }
                }
            }
        }
    }

    protected void replaceByClusteredDeploymentGBean(EARContext earContext,
        EjbModule ejbModule,
        AbstractName sessionManagerName,
        EnterpriseBean enterpriseBean) throws DeploymentException {
        AbstractName name = beanNameBuilder.createEjbName(earContext, ejbModule, enterpriseBean);
        GBeanData beanInstance;
        try {
            beanInstance = earContext.getGBeanInstance(name);
            earContext.removeGBean(name);
        } catch (GBeanNotFoundException e) {
            throw new DeploymentException("No GBean [" + name + "]", e);
        }
        GBeanData clusteredDeploymentGBean = new GBeanData(beanInstance);
        clusteredDeploymentGBean.setGBeanInfo(new GBeanData(ClusteredStatefulDeployment.class).getGBeanInfo());
        clusteredDeploymentGBean.setReferencePattern(ClusteredStatefulDeployment.GBEAN_REF_SESSION_MANAGER, sessionManagerName);
        try {
            earContext.addGBean(clusteredDeploymentGBean);
        } catch (GBeanAlreadyExistsException e) {
            throw new DeploymentException("See nested", e);
        }
    }

    public void createModule(Module module, Bundle bundle, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
    }

    public void createModule(Module module,
        Object plan,
        JarFile moduleFile,
        String targetPath,
        URL specDDUrl,
        Environment environment,
        Object moduleContextInfo,
        AbstractName earName,
        Naming naming,
        ModuleIDBuilder idBuilder) throws DeploymentException {
        EjbModule ejbModule = (EjbModule) module;
        GeronimoEjbJarType tmpGeronimoEjbJarType = (GeronimoEjbJarType) ejbModule.getEjbModule().getAltDDs().get("geronimo-openejb.xml");
        OpenejbGeronimoEjbJarType geronimoEjbJarType = XmlUtil.convertToXmlbeans(tmpGeronimoEjbJarType);
        GerOpenejbClusteringWadiType clusteringWadiType = getWadiClusterConfig(geronimoEjbJarType);
        if (null == clusteringWadiType) {
            return;
        }

        EnvironmentBuilder.mergeEnvironments(environment, defaultEnvironment);

        ejbModule.getPreAutoConfigDeployer().add(new MapSFSBToContainerIDDeployer(defaultClusteredStatefulContainerId));
    }

    public void initContext(EARContext earContext, Module module, Bundle bundle) throws DeploymentException {
    }

    public void installModule(JarFile earFile,
        EARContext earContext,
        Module module,
        Collection configurationStores,
        ConfigurationStore targetConfigurationStore,
        Collection repository) throws DeploymentException {
    }

    protected AbstractName addSessionManager(GerOpenejbClusteringWadiType clustering,
        EjbModule ejbModule,
        DeploymentContext moduleContext) throws DeploymentException {
        AbstractName name = newGBeanName(moduleContext, "WADISessionManager");

        GBeanData beanData = new GBeanData(name, BasicWADISessionManager.class);

        setConfigInfo(clustering, ejbModule, beanData);
        setCluster(clustering, beanData);
        setBackingStrategyFactory(clustering, beanData);
        setClusteredServiceHolders(moduleContext, beanData);

        addGBean(moduleContext, beanData);

        return name;
    }

    protected void setClusteredServiceHolders(DeploymentContext moduleContext, GBeanData beanData)
            throws DeploymentException {
        AbstractName name = newGBeanName(moduleContext, "NetworkConnectorTrackerHolder");

        GBeanData serviceHolder = new GBeanData(name, BasicNetworkConnectorTrackerServiceHolder.GBEAN_INFO);
        addGBean(moduleContext, serviceHolder);

        beanData.setReferencePattern(BasicWADISessionManager.GBEAN_REF_SERVICE_HOLDERS, name);
    }

    protected void addNetworkConnectorMonitor(DeploymentContext moduleContext, AbstractName sessionManagerName)
            throws DeploymentException {
        AbstractName name = newGBeanName(moduleContext, "NetworkConnectorMonitor");

        GBeanData networkConnectorMonitor = new GBeanData(name, NetworkConnectorMonitor.GBEAN_INFO);
        networkConnectorMonitor.setReferencePattern(NetworkConnectorMonitor.GBEAN_REF_NETWORK_CONNECTORS,
            defaultNetworkConnectorName);
        networkConnectorMonitor.setReferencePattern(NetworkConnectorMonitor.GBEAN_REF_EJB_DEP_ID_ACCESSOR,
            new AbstractNameQuery(ClusteredStatefulDeployment.class.getName()));
        networkConnectorMonitor.setReferencePattern(NetworkConnectorMonitor.GBEAN_REF_WADI_SESSION_MANAGER,
            sessionManagerName);

        addGBean(moduleContext, networkConnectorMonitor);
    }

    protected AbstractName newGBeanName(DeploymentContext moduleContext, String name) {
        return moduleContext.getNaming().createChildName(moduleContext.getModuleName(),
                name,
                GBeanInfoBuilder.DEFAULT_J2EE_TYPE);
    }

    protected void addGBean(DeploymentContext moduleContext, GBeanData beanData) throws DeploymentException {
        try {
            moduleContext.addGBean(beanData);
        } catch (GBeanAlreadyExistsException e) {
            throw new DeploymentException(e);
        }
    }

    protected void setCluster(GerOpenejbClusteringWadiType clustering, GBeanData beanData) {
        Set<AbstractNameQuery> patterns = new HashSet<AbstractNameQuery>();
        if (clustering.isSetCluster()) {
            addAbstractNameQueries(patterns, clustering.getCluster());
        } else {
            patterns.add(defaultClusterName);
        }
        beanData.setReferencePatterns(BasicWADISessionManager.GBEAN_REF_CLUSTER, patterns);
    }

    protected void setBackingStrategyFactory(GerOpenejbClusteringWadiType clustering, GBeanData beanData) {
        Set<AbstractNameQuery> patterns = new HashSet<AbstractNameQuery>();
        if (clustering.isSetBackingStrategyFactory()) {
            addAbstractNameQueries(patterns, clustering.getBackingStrategyFactory());
        } else {
            patterns.add(defaultBackingStrategyFactoryName);
        }
        beanData.setReferencePatterns(BasicWADISessionManager.GBEAN_REF_BACKING_STRATEGY_FACTORY, patterns);
    }

    protected void addAbstractNameQueries(Set<AbstractNameQuery> patterns, GerPatternType patternType) {
        AbstractNameQuery query = ENCConfigBuilder.buildAbstractNameQuery(patternType, null, null, null);
        patterns.add(query);
    }

    protected void setConfigInfo(GerOpenejbClusteringWadiType clustering, EjbModule ejbModule, GBeanData beanData) {
        int sweepInterval = getSweepInterval(clustering);
        int numPartitions = getNumberOfPartitions(clustering);
        Integer sessionTimeout = getSessionTimeout();
        boolean disableReplication = isDisableReplication(clustering);
        boolean deltaReplication = isDeltaReplication(clustering);

        String ejbModuleName = ejbModule.getName();
        URI serviceSpaceName;
        try {
            serviceSpaceName = new URI(ejbModuleName);
        } catch (URISyntaxException e) {
            AssertionError error = new AssertionError("contextPath [" + ejbModuleName + "] cannot be parsed as an URI.");
            throw (AssertionError) error.initCause(e);
        }

        WADISessionManagerConfigInfo configInfo = new WADISessionManagerConfigInfo(serviceSpaceName,
                sweepInterval,
                numPartitions,
                sessionTimeout,
                disableReplication,
                deltaReplication);
        beanData.setAttribute(BasicWADISessionManager.GBEAN_ATTR_WADI_CONFIG_INFO, configInfo);
    }

    protected Integer getSessionTimeout() throws AssertionError {
        return defaultSessionTimeout;
    }

    protected int getSweepInterval(GerOpenejbClusteringWadiType clustering) {
        if (clustering.isSetSweepInterval()) {
            return clustering.getSweepInterval().intValue();
        }
        return defaultSweepInterval;
    }

    protected boolean isDeltaReplication(GerOpenejbClusteringWadiType clustering) {
        if (clustering.isSetDeltaReplication()) {
            return clustering.getDeltaReplication();
        }
        return false;
    }

    protected boolean isDisableReplication(GerOpenejbClusteringWadiType clustering) {
        if (clustering.isSetDisableReplication()) {
            return clustering.getDisableReplication();
        }
        return false;
    }

    protected int getNumberOfPartitions(GerOpenejbClusteringWadiType clustering) {
        if (clustering.isSetNumPartitions()) {
            return clustering.getNumPartitions().intValue();
        }
        return defaultNumPartitions;
    }

    protected GerOpenejbClusteringWadiType getWadiClusterConfig(XmlObject container) throws DeploymentException {
        XmlObject[] items = container.selectChildren(CLUSTERING_WADI_QNAME_SET);
        if (items.length > 1) {
            throw new DeploymentException("Unexpected count of clustering elements in geronimo plan " + items.length
                    + " qnameset: " + CLUSTERING_WADI_QNAME_SET);
        }
        if (items.length == 1) {
            return (GerOpenejbClusteringWadiType) items[0].copy().changeType(GerOpenejbClusteringWadiType.type);
        }
        return null;
    }

    public static final GBeanInfo GBEAN_INFO;

    public static final String GBEAN_ATTR_DFT_CLUSTERED_SFSB_CONT_ID = "defaultClusteredStatefulContainerId";
    public static final String GBEAN_ATTR_DFT_SWEEP_INTERVAL = "defaultSweepInterval";
    public static final String GBEAN_ATTR_DFT_SESSION_TIMEOUT = "defaultSessionTimeout";
    public static final String GBEAN_ATTR_DFT_NUM_PARTITIONS = "defaultNumPartitions";
    public static final String GBEAN_ATTR_DFT_BACKING_STRATEGY_FACTORY_NAME = "defaultBackingStrategyFactoryName";
    public static final String GBEAN_ATTR_DFT_CLUSTER_NAME = "defaultClusterName";
    public static final String GBEAN_ATTR_DFT_NETWORK_CONNECTOR_NAME = "defaultNetworkConnectorName";
    public static final String GBEAN_ATTR_DFT_ENVIRONMENT = "defaultEnvironment";

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic("WADI OpenEJB Clusteting Builder",
                WADIOpenEJBClusteringBuilder.class,
                NameFactory.MODULE_BUILDER);

        infoBuilder.addAttribute(GBEAN_ATTR_DFT_CLUSTERED_SFSB_CONT_ID, String.class, true);
        infoBuilder.addAttribute(GBEAN_ATTR_DFT_SWEEP_INTERVAL, int.class, true);
        infoBuilder.addAttribute(GBEAN_ATTR_DFT_SESSION_TIMEOUT, int.class, true);
        infoBuilder.addAttribute(GBEAN_ATTR_DFT_NUM_PARTITIONS, int.class, true);
        infoBuilder.addAttribute(GBEAN_ATTR_DFT_BACKING_STRATEGY_FACTORY_NAME, AbstractNameQuery.class, true);
        infoBuilder.addAttribute(GBEAN_ATTR_DFT_CLUSTER_NAME, AbstractNameQuery.class, true);
        infoBuilder.addAttribute(GBEAN_ATTR_DFT_NETWORK_CONNECTOR_NAME, AbstractNameQuery.class, true);
        infoBuilder.addAttribute(GBEAN_ATTR_DFT_ENVIRONMENT, Environment.class, true);

        infoBuilder.setConstructor(new String[] { GBEAN_ATTR_DFT_CLUSTERED_SFSB_CONT_ID,
            GBEAN_ATTR_DFT_SWEEP_INTERVAL,
            GBEAN_ATTR_DFT_SESSION_TIMEOUT,
            GBEAN_ATTR_DFT_NUM_PARTITIONS,
            GBEAN_ATTR_DFT_BACKING_STRATEGY_FACTORY_NAME,
            GBEAN_ATTR_DFT_CLUSTER_NAME,
            GBEAN_ATTR_DFT_NETWORK_CONNECTOR_NAME,
            GBEAN_ATTR_DFT_ENVIRONMENT });

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
