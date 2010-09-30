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
package org.apache.geronimo.jetty8.cluster.wadi.builder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
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
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.jetty8.WebAppContextWrapper;
import org.apache.geronimo.jetty8.cluster.ClusteredSessionHandlerFactory;
import org.apache.geronimo.jetty8.cluster.wadi.WADIClusteredPreHandlerFactory;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Jsr77Naming;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.naming.deployment.ENCConfigBuilder;
import org.apache.geronimo.schema.NamespaceElementConverter;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.xbeans.jetty8.cluster.wadi.GerClusteringWadiDocument;
import org.apache.geronimo.xbeans.jetty8.cluster.wadi.GerClusteringWadiType;
import org.apache.geronimo.xbeans.geronimo.j2ee.GerClusteringDocument;
import org.apache.geronimo.xbeans.geronimo.naming.GerPatternType;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlObject;

/**
 *
 * @version $Rev$ $Date$
 */
@GBean(name="WADIJettyClusteringBuilder", j2eeType=NameFactory.MODULE_BUILDER)
public class WADIJettyClusteringBuilder implements NamespaceDrivenBuilder {
    private static final QName BASE_CLUSTERING_QNAME = GerClusteringDocument.type.getDocumentElementName();
    private static final QName CLUSTERING_WADI_QNAME = GerClusteringWadiDocument.type.getDocumentElementName();
    private static final QNameSet CLUSTERING_WADI_QNAME_SET = QNameSet.singleton(CLUSTERING_WADI_QNAME);

    static  {
        SchemaConversionUtils.registerNamespaceConversions(
            Collections.singletonMap(CLUSTERING_WADI_QNAME.getLocalPart(),
            new NamespaceElementConverter(CLUSTERING_WADI_QNAME.getNamespaceURI())));
    }

    private final int defaultSweepInterval;
    private final int defaultNumPartitions;
    private final AbstractNameQuery defaultBackingStrategyFactoryName;
    private final AbstractNameQuery defaultClusterName;
    private final Environment defaultEnvironment;

    public WADIJettyClusteringBuilder(@ParamAttribute(name=GBEAN_ATTR_DFT_SWEEP_INTERVAL) int defaultSweepInterval,
        @ParamAttribute(name=GBEAN_ATTR_DFT_NUM_PARTITIONS) int defaultNumPartitions,
        @ParamAttribute(name=GBEAN_ATTR_DFT_BACKING_STRATEGY_FACTORY_NAME) AbstractNameQuery defaultBackingStrategyFactoryName,
        @ParamAttribute(name=GBEAN_ATTR_DFT_CLUSTER_NAME) AbstractNameQuery defaultClusterName,
        @ParamAttribute(name=GBEAN_ATTR_DFT_ENVIRONMENT) Environment defaultEnvironment) {
        if (defaultSweepInterval < 1) {
            throw new IllegalArgumentException("defaultSweepInterval is lower than 1");
        } else if (defaultNumPartitions < 1) {
            throw new IllegalArgumentException("defaultNumPartitions is lower than 1");
        } else if (null == defaultBackingStrategyFactoryName) {
            throw new IllegalArgumentException("defaultBackingStrategyFactoryName is required");
        } else if (null == defaultClusterName) {
            throw new IllegalArgumentException("defaultClusterName is required");
        } else if (null == defaultEnvironment) {
            throw new IllegalArgumentException("defaultEnvironment is required");
        }
        this.defaultSweepInterval = defaultSweepInterval;
        this.defaultNumPartitions = defaultNumPartitions;
        this.defaultBackingStrategyFactoryName = defaultBackingStrategyFactoryName;
        this.defaultClusterName = defaultClusterName;
        this.defaultEnvironment = defaultEnvironment;
    }

    public void buildEnvironment(XmlObject container, Environment environment) throws DeploymentException {
        if (getWadiClusterConfig(container) != null) {
            EnvironmentBuilder.mergeEnvironments(environment, defaultEnvironment);
        }
    }

    public void build(XmlObject container, DeploymentContext applicationContext, DeploymentContext moduleContext) throws DeploymentException {
        GerClusteringWadiType clusteringWadiType = getWadiClusterConfig(container);
        if (clusteringWadiType != null) {
            GBeanData webModuleData = extractWebModule(moduleContext);
            try {
                AbstractName sessionManagerName = addSessionManager(clusteringWadiType, webModuleData, moduleContext);
                addSessionHandlerFactory(moduleContext, webModuleData, sessionManagerName);
                addPreHandlerFactory(moduleContext, webModuleData, sessionManagerName);
            } catch (GBeanAlreadyExistsException e) {
                throw new DeploymentException("Duplicate GBean", e);
            }
        }
    }

    protected GBeanData extractWebModule(DeploymentContext moduleContext) throws DeploymentException {
        Configuration configuration = moduleContext.getConfiguration();
        AbstractNameQuery webModuleQuery = createJettyWebAppContextNameQuery(moduleContext);
        try {
            return configuration.findGBeanData(webModuleQuery);
        } catch (GBeanNotFoundException e) {
            throw new DeploymentException("Could not locate web module gbean in web app configuration", e);
        }
    }

    protected AbstractNameQuery createJettyWebAppContextNameQuery(DeploymentContext moduleContext) {
        String name = moduleContext.getModuleName().getNameProperty(Jsr77Naming.J2EE_NAME);
        return new AbstractNameQuery(null,
            Collections.singletonMap(Jsr77Naming.J2EE_NAME, name),
            Collections.singleton(WebAppContextWrapper.class.getName()));
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

    protected GerClusteringWadiType getWadiClusterConfig(XmlObject container) throws DeploymentException {
        XmlObject[] items = container.selectChildren(CLUSTERING_WADI_QNAME_SET);
        if (items.length > 1) {
            throw new DeploymentException("Unexpected count of clustering elements in geronimo plan " + items.length + " qnameset: " + CLUSTERING_WADI_QNAME_SET);
        }
        if (items.length == 1) {
            return (GerClusteringWadiType) items[0].copy().changeType(GerClusteringWadiType.type);
        }
        return null;
    }

    protected AbstractName addSessionManager(GerClusteringWadiType clustering,
            GBeanData webModuleData,
            DeploymentContext moduleContext) throws GBeanAlreadyExistsException {
        AbstractName name = moduleContext.getNaming().createChildName(moduleContext.getModuleName(),
                "WADISessionManager", GBeanInfoBuilder.DEFAULT_J2EE_TYPE);

        GBeanData beanData = new GBeanData(name, BasicWADISessionManager.class);

        setConfigInfo(clustering, webModuleData, beanData);
        setCluster(clustering, beanData);
        setBackingStrategyFactory(clustering, beanData);

        moduleContext.addGBean(beanData);

        return name;
    }

    protected void setConfigInfo(GerClusteringWadiType clustering, GBeanData webModuleData, GBeanData beanData) {
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
            throw (AssertionError) new AssertionError("contextPath [" + contextPath + "] cannot be parsed as an URI.").initCause(e);
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
        //TODO this will no longer work, use WebAppInfo
        Integer sessionTimeout = (Integer) webModuleData.getAttribute(WebAppContextWrapper.GBEAN_ATTR_SESSION_TIMEOUT);
        if (null == sessionTimeout) {
            throw new AssertionError();
        }
        return sessionTimeout;
    }

    protected boolean isDeltaReplication(GerClusteringWadiType clustering) {
        if (clustering.isSetDeltaReplication()) {
            return clustering.getDeltaReplication();
        }
        return false;
    }

    protected boolean isDisableReplication(GerClusteringWadiType clustering) {
        if (clustering.isSetDisableReplication()) {
            return clustering.getDisableReplication();
        }
        return false;
    }

    protected int getNumberOfPartitions(GerClusteringWadiType clustering) {
        if (clustering.isSetNumPartitions()) {
            return clustering.getNumPartitions().intValue();
        }
        return defaultNumPartitions;
    }

    protected int getSweepInterval(GerClusteringWadiType clustering) {
        if (clustering.isSetSweepInterval()) {
            return clustering.getSweepInterval().intValue();
        }
        return defaultSweepInterval;
    }

    protected void setCluster(GerClusteringWadiType clustering, GBeanData beanData) {
        Set patterns = new HashSet();
        if (clustering.isSetCluster()) {
            addAbstractNameQueries(patterns, clustering.getCluster().getPatternArray());
        } else {
            patterns.add(defaultClusterName);
        }
        beanData.setReferencePatterns(BasicWADISessionManager.GBEAN_REF_CLUSTER, patterns);
    }

    protected void setBackingStrategyFactory(GerClusteringWadiType clustering, GBeanData beanData) {
        Set patterns = new HashSet();
        if (clustering.isSetBackingStrategyFactory()) {
            addAbstractNameQueries(patterns, clustering.getBackingStrategyFactory().getPatternArray());
        } else {
            patterns.add(defaultBackingStrategyFactoryName);
        }
        beanData.setReferencePatterns(BasicWADISessionManager.GBEAN_REF_BACKING_STRATEGY_FACTORY, patterns);
    }

    protected AbstractName addPreHandlerFactory(DeploymentContext moduleContext,
            GBeanData webModuleData, AbstractName sessionManagerName) throws GBeanAlreadyExistsException {
        AbstractName name = moduleContext.getNaming().createChildName(moduleContext.getModuleName(),
                "WADIClusteredPreHandlerFactory", GBeanInfoBuilder.DEFAULT_J2EE_TYPE);

        GBeanData beanData = new GBeanData(name, WADIClusteredPreHandlerFactory.class);
        beanData.setReferencePattern(WADIClusteredPreHandlerFactory.GBEAN_REF_WADI_SESSION_MANAGER, sessionManagerName);

        webModuleData.setReferencePattern(WebAppContextWrapper.GBEAN_REF_PRE_HANDLER_FACTORY, name);

        moduleContext.addGBean(beanData);

        return name;
    }

    protected AbstractName addSessionHandlerFactory(DeploymentContext moduleContext,
            GBeanData webModuleData, AbstractName sessionManagerName) throws GBeanAlreadyExistsException {
        AbstractName name = moduleContext.getNaming().createChildName(moduleContext.getModuleName(),
                "ClusteredSessionHandlerFactory", GBeanInfoBuilder.DEFAULT_J2EE_TYPE);

        GBeanData beanData = new GBeanData(name, ClusteredSessionHandlerFactory.class);
        beanData.setReferencePattern(ClusteredSessionHandlerFactory.GBEAN_REF_SESSION_MANAGER, sessionManagerName);

        webModuleData.setReferencePattern(WebAppContextWrapper.GBEAN_REF_SESSION_HANDLER_FACTORY, name);

        moduleContext.addGBean(beanData);

        return name;
    }

    protected void addAbstractNameQueries(Set patterns, GerPatternType[] patternTypes) {
        for (int i = 0; i < patternTypes.length; i++) {
            AbstractNameQuery query = ENCConfigBuilder.buildAbstractNameQuery(patternTypes[i], null, null, null);
            patterns.add(query);
        }
    }

    public static final String GBEAN_ATTR_DFT_SWEEP_INTERVAL = "defaultSweepInterval";
    public static final String GBEAN_ATTR_DFT_NUM_PARTITIONS = "defaultNumPartitions";
    public static final String GBEAN_ATTR_DFT_BACKING_STRATEGY_FACTORY_NAME = "defaultBackingStrategyFactoryName";
    public static final String GBEAN_ATTR_DFT_CLUSTER_NAME = "defaultClusterName";
    public static final String GBEAN_ATTR_DFT_ENVIRONMENT = "defaultEnvironment";
}
