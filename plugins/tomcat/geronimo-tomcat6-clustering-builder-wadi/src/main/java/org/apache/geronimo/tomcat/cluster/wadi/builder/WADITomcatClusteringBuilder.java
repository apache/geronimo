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

package org.apache.geronimo.tomcat.cluster.wadi.builder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.geronimo.clustering.wadi.BasicWADISessionManager;
import org.apache.geronimo.clustering.wadi.WADISessionManagerConfigInfo;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.NamespaceDrivenBuilder;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Jsr77Naming;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Dependency;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.naming.deployment.ENCConfigBuilder;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.tomcat.TomcatWebAppContext;
import org.apache.geronimo.tomcat.cluster.ClusteredManagerRetriever;
import org.apache.geronimo.tomcat.cluster.wadi.WADIClusteredValveRetriever;
import org.apache.geronimo.xbeans.tomcat.cluster.wadi.GerTomcatClusteringWadiDocument;
import org.apache.geronimo.xbeans.tomcat.cluster.wadi.GerTomcatClusteringWadiType;
import org.apache.geronimo.xbeans.geronimo.j2ee.GerClusteringDocument;
import org.apache.geronimo.xbeans.geronimo.naming.GerPatternType;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlObject;

/**
 *
 * @version $Rev:$ $Date:$
 */
@GBean(name="WADITomcatClusteringBuilder", j2eeType=NameFactory.MODULE_BUILDER)
public class WADITomcatClusteringBuilder implements NamespaceDrivenBuilder {
    private static final QName BASE_CLUSTERING_QNAME = GerClusteringDocument.type.getDocumentElementName();
    private static final QName CLUSTERING_WADI_QNAME = GerTomcatClusteringWadiDocument.type.getDocumentElementName();
    private static final QNameSet CLUSTERING_WADI_QNAME_SET = QNameSet.singleton(CLUSTERING_WADI_QNAME);

    static {
        SchemaConversionUtils.registerNamespaceConversions(
            Collections.singletonMap(CLUSTERING_WADI_QNAME.getLocalPart(),
            new TomcatClusteringWADIConverter()));
    }

    private final int defaultSweepInterval;
    private final int defaultSessionTimeout;
    private final int defaultNumPartitions;
    private final AbstractNameQuery defaultBackingStrategyFactoryName;
    private final AbstractNameQuery defaultClusterName;
    private final Artifact artifactToRemoveFromEnvironment;
    private final Environment defaultEnvironment;

    public WADITomcatClusteringBuilder(@ParamAttribute(name=GBEAN_ATTR_DFT_SWEEP_INTERVAL) int defaultSweepInterval,
        @ParamAttribute(name=GBEAN_ATTR_DFT_SESSION_TIMEOUT) int defaultSessionTimeout,
        @ParamAttribute(name=GBEAN_ATTR_DFT_NUM_PARTITIONS) int defaultNumPartitions,
        @ParamAttribute(name=GBEAN_ATTR_DFT_BACKING_STRATEGY_FACTORY_NAME) AbstractNameQuery defaultBackingStrategyFactoryName,
        @ParamAttribute(name=GBEAN_ATTR_DFT_CLUSTER_NAME) AbstractNameQuery defaultClusterName,
        @ParamAttribute(name=GBEAN_ATTR_ARTIFACT_TO_REMOVE) Artifact artifactToRemoveFromEnvironment,
        @ParamAttribute(name=GBEAN_ATTR_DFT_ENVIRONMENT) Environment defaultEnvironment) {
        if (defaultSweepInterval < 1) {
            throw new IllegalArgumentException("defaultSweepInterval is lower than 1");
        } else if (defaultSessionTimeout < 1) {
            throw new IllegalArgumentException("defaultSessionTimeout is lower than 1");
        } else if (defaultNumPartitions < 1) {
            throw new IllegalArgumentException("defaultNumPartitions is lower than 1");
        } else if (null == defaultBackingStrategyFactoryName) {
            throw new IllegalArgumentException("defaultBackingStrategyFactoryName is required");
        } else if (null == defaultClusterName) {
            throw new IllegalArgumentException("defaultClusterName is required");
        } else if (null == artifactToRemoveFromEnvironment) {
            throw new IllegalArgumentException("artifactToRemoveFromEnvironment is required");
        } else if (null == defaultEnvironment) {
            throw new IllegalArgumentException("defaultEnvironment is required");
        }
        this.defaultSweepInterval = defaultSweepInterval;
        this.defaultSessionTimeout = defaultSessionTimeout;
        this.defaultNumPartitions = defaultNumPartitions;
        this.defaultBackingStrategyFactoryName = defaultBackingStrategyFactoryName;
        this.defaultClusterName = defaultClusterName;
        this.artifactToRemoveFromEnvironment = artifactToRemoveFromEnvironment;
        this.defaultEnvironment = defaultEnvironment;
    }

    public void buildEnvironment(XmlObject container, Environment environment) throws DeploymentException {
        if (getWadiClusterConfig(container) != null) {
            filterDependencies(environment);
            EnvironmentBuilder.mergeEnvironments(environment, defaultEnvironment);
        }
    }

    public void build(XmlObject container, DeploymentContext applicationContext, DeploymentContext moduleContext)
            throws DeploymentException {
        GerTomcatClusteringWadiType clusteringWadiType = getWadiClusterConfig(container);
        if (clusteringWadiType != null) {
            GBeanData webModuleData = extractWebModule(moduleContext);
            try {
                AbstractName sessionManagerName = addSessionManager(clusteringWadiType, webModuleData, moduleContext);
                addClusteredManagerRetriever(moduleContext, webModuleData, sessionManagerName);
                addClusteredValveRetriever(moduleContext, webModuleData, sessionManagerName);
            } catch (GBeanAlreadyExistsException e) {
                throw new DeploymentException("Duplicate GBean", e);
            }
        }
    }

    protected void filterDependencies(Environment environment) {
        List<Dependency> dependencies = environment.getDependencies();
        dependencies = new ArrayList<Dependency>(dependencies);
        for (Iterator<Dependency> iterator = dependencies.iterator(); iterator.hasNext();) {
            Dependency dependency = iterator.next();
            Artifact dependencyArtifact = dependency.getArtifact();
            if (artifactToRemoveFromEnvironment.matches(dependencyArtifact)) {
                iterator.remove();
            }
        }
        environment.setDependencies(dependencies);
    }

    protected GBeanData extractWebModule(DeploymentContext moduleContext) throws DeploymentException {
        AbstractNameQuery webModuleQuery = createTomcatWebAppContextNameQuery(moduleContext);
        Configuration configuration = moduleContext.getConfiguration();
        try {
            return configuration.findGBeanData(webModuleQuery);
        } catch (GBeanNotFoundException e) {
            throw new DeploymentException("Could not locate web module gbean in web app configuration", e);
        }
    }

    protected AbstractNameQuery createTomcatWebAppContextNameQuery(DeploymentContext moduleContext) {
        String name = moduleContext.getModuleName().getNameProperty(Jsr77Naming.J2EE_NAME);
        return new AbstractNameQuery(null,
            Collections.singletonMap(Jsr77Naming.J2EE_NAME, name),
            Collections.singleton(TomcatWebAppContext.class.getName()));
    }

    public QNameSet getSpecQNameSet() {
        return QNameSet.EMPTY;
    }

    public QNameSet getPlanQNameSet() {
        return CLUSTERING_WADI_QNAME_SET;
    }

    public QName getBaseQName() {
        return BASE_CLUSTERING_QNAME;
    }

    protected GerTomcatClusteringWadiType getWadiClusterConfig(XmlObject container) throws DeploymentException {
        XmlObject[] items = container.selectChildren(CLUSTERING_WADI_QNAME_SET);
        if (items.length > 1) {
            throw new DeploymentException("Unexpected count of clustering elements in geronimo plan " + items.length
                    + " qnameset: " + CLUSTERING_WADI_QNAME_SET);
        }
        if (items.length == 1) {
            return (GerTomcatClusteringWadiType) items[0].copy().changeType(GerTomcatClusteringWadiType.type);
        }
        return null;
    }

    protected AbstractName addSessionManager(GerTomcatClusteringWadiType clustering,
            GBeanData webModuleData,
            DeploymentContext moduleContext) throws GBeanAlreadyExistsException {
        AbstractName name = newGBeanName(moduleContext, "WADISessionManager");

        GBeanData beanData = new GBeanData(name, BasicWADISessionManager.class);

        setConfigInfo(clustering, webModuleData, beanData);
        setCluster(clustering, beanData);
        setBackingStrategyFactory(clustering, beanData);

        addGBean(moduleContext, beanData);

        return name;
    }

    protected void setConfigInfo(GerTomcatClusteringWadiType clustering, GBeanData webModuleData, GBeanData beanData) {
        int sweepInterval = getSweepInterval(clustering);
        int numPartitions = getNumberOfPartitions(clustering);
        Integer sessionTimeout = getSessionTimeout(webModuleData);
        boolean disableReplication = isDisableReplication(clustering);
        boolean deltaReplication = isDeltaReplication(clustering);

        String contextPath = (String) webModuleData.getAttribute("contextPath");
        URI serviceSpaceName;
        try {
            serviceSpaceName = new URI(contextPath);
        } catch (URISyntaxException e) {
            AssertionError error = new AssertionError("contextPath [" + contextPath + "] cannot be parsed as an URI.");
            throw (AssertionError) error.initCause(e);
        }

        WADISessionManagerConfigInfo configInfo = new WADISessionManagerConfigInfo(serviceSpaceName,
                sweepInterval,
                numPartitions,
                sessionTimeout.intValue(),
                disableReplication,
                deltaReplication);
        beanData.setAttribute(BasicWADISessionManager.GBEAN_ATTR_WADI_CONFIG_INFO, configInfo);
    }

    protected Integer getSessionTimeout(GBeanData webModuleData) throws AssertionError {
        return defaultSessionTimeout;
    }

    protected boolean isDeltaReplication(GerTomcatClusteringWadiType clustering) {
        if (clustering.isSetDeltaReplication()) {
            return clustering.getDeltaReplication();
        }
        return false;
    }

    protected boolean isDisableReplication(GerTomcatClusteringWadiType clustering) {
        if (clustering.isSetDisableReplication()) {
            return clustering.getDisableReplication();
        }
        return false;
    }

    protected int getNumberOfPartitions(GerTomcatClusteringWadiType clustering) {
        if (clustering.isSetNumPartitions()) {
            return clustering.getNumPartitions().intValue();
        }
        return defaultNumPartitions;
    }

    protected int getSweepInterval(GerTomcatClusteringWadiType clustering) {
        if (clustering.isSetSweepInterval()) {
            return clustering.getSweepInterval().intValue();
        }
        return defaultSweepInterval;
    }

    protected void setCluster(GerTomcatClusteringWadiType clustering, GBeanData beanData) {
        Set patterns = new HashSet();
        if (clustering.isSetCluster()) {
            addAbstractNameQueries(patterns, clustering.getCluster());
        } else {
            patterns.add(defaultClusterName);
        }
        beanData.setReferencePatterns(BasicWADISessionManager.GBEAN_REF_CLUSTER, patterns);
    }

    protected void setBackingStrategyFactory(GerTomcatClusteringWadiType clustering, GBeanData beanData) {
        Set patterns = new HashSet();
        if (clustering.isSetBackingStrategyFactory()) {
            addAbstractNameQueries(patterns, clustering.getBackingStrategyFactory());
        } else {
            patterns.add(defaultBackingStrategyFactoryName);
        }
        beanData.setReferencePatterns(BasicWADISessionManager.GBEAN_REF_BACKING_STRATEGY_FACTORY, patterns);
    }

    protected AbstractName addClusteredValveRetriever(DeploymentContext moduleContext,
            GBeanData webModuleData,
            AbstractName sessionManagerName) throws GBeanAlreadyExistsException {
        AbstractName name = newGBeanName(moduleContext, "WADIClusteredValveRetriever");

        GBeanData beanData = new GBeanData(name, WADIClusteredValveRetriever.class);
        beanData.setReferencePattern(WADIClusteredValveRetriever.GBEAN_REF_WADI_SESSION_MANAGER, sessionManagerName);

        webModuleData.setReferencePattern(TomcatWebAppContext.GBEAN_REF_CLUSTERED_VALVE_RETRIEVER, name);

        addGBean(moduleContext, beanData);

        return name;
    }

    protected AbstractName addClusteredManagerRetriever(DeploymentContext moduleContext,
            GBeanData webModuleData,
            AbstractName sessionManagerName) throws GBeanAlreadyExistsException {
        AbstractName name = newGBeanName(moduleContext, "ClusteredManagerRetriever");

        GBeanData beanData = new GBeanData(name, ClusteredManagerRetriever.class);
        beanData.setReferencePattern(ClusteredManagerRetriever.GBEAN_REF_SESSION_MANAGER, sessionManagerName);

        webModuleData.setReferencePattern(TomcatWebAppContext.GBEAN_REF_MANAGER_RETRIEVER, name);

        addGBean(moduleContext, beanData);

        return name;
    }

    protected void addGBean(DeploymentContext moduleContext, GBeanData beanData) throws GBeanAlreadyExistsException {
        moduleContext.addGBean(beanData);
    }

    protected AbstractName newGBeanName(DeploymentContext moduleContext, String name) {
        return moduleContext.getNaming().createChildName(moduleContext.getModuleName(),
                name,
                GBeanInfoBuilder.DEFAULT_J2EE_TYPE);
    }

    protected void addAbstractNameQueries(Set patterns, GerPatternType patternType) {
        AbstractNameQuery query = ENCConfigBuilder.buildAbstractNameQuery(patternType, null, null, null);
        patterns.add(query);
    }

    public static final String GBEAN_ATTR_DFT_SWEEP_INTERVAL = "defaultSweepInterval";
    public static final String GBEAN_ATTR_DFT_SESSION_TIMEOUT = "defaultSessionTimeout";
    public static final String GBEAN_ATTR_DFT_NUM_PARTITIONS = "defaultNumPartitions";
    public static final String GBEAN_ATTR_DFT_BACKING_STRATEGY_FACTORY_NAME = "defaultBackingStrategyFactoryName";
    public static final String GBEAN_ATTR_DFT_CLUSTER_NAME = "defaultClusterName";
    public static final String GBEAN_ATTR_ARTIFACT_TO_REMOVE = "artifactToRemoveFromEnvironment";
    public static final String GBEAN_ATTR_DFT_ENVIRONMENT = "defaultEnvironment";
}
