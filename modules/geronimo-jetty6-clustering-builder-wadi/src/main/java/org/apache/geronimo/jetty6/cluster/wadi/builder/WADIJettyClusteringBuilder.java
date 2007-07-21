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
package org.apache.geronimo.jetty6.cluster.wadi.builder;

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
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.jetty6.JettyWebAppContext;
import org.apache.geronimo.jetty6.cluster.ClusteredSessionHandlerFactory;
import org.apache.geronimo.jetty6.cluster.wadi.WADIClusteredPreHandlerFactory;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.naming.deployment.ENCConfigBuilder;
import org.apache.geronimo.schema.NamespaceElementConverter;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.xbeans.geronimo.GerClusteringWadiDocument;
import org.apache.geronimo.xbeans.geronimo.GerClusteringWadiType;
import org.apache.geronimo.xbeans.geronimo.naming.GerPatternType;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlObject;

/**
 *
 * @version $Rev$ $Date$
 */
public class WADIJettyClusteringBuilder implements NamespaceDrivenBuilder {
    private static final QName CLUSTERING_WADI_QNAME = GerClusteringWadiDocument.type.getDocumentElementName();
    private static final QNameSet CLUSTERING_WADI_QNAME_SET = QNameSet.singleton(CLUSTERING_WADI_QNAME);

    private final int defaultSweepInterval;
    private final int defaultNumPartitions;
    private final AbstractNameQuery defaultBackingStrategyFactoryName;
    private final AbstractNameQuery defaultClusterName;
    private final Environment defaultEnvironment;

    public WADIJettyClusteringBuilder(int defaultSweepInterval,
            int defaultNumPartitions,
            AbstractNameQuery defaultBackingStrategyFactoryName,
            AbstractNameQuery defaultClusterName,
            Environment defaultEnvironment) {
        this.defaultSweepInterval = defaultSweepInterval;
        this.defaultNumPartitions = defaultNumPartitions;
        this.defaultBackingStrategyFactoryName = defaultBackingStrategyFactoryName;
        this.defaultClusterName = defaultClusterName;
        this.defaultEnvironment = defaultEnvironment;
        
        SchemaConversionUtils.registerNamespaceConversions(
            Collections.singletonMap(CLUSTERING_WADI_QNAME.getLocalPart(),
            new NamespaceElementConverter(CLUSTERING_WADI_QNAME.getNamespaceURI())));
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

    private GBeanData extractWebModule(DeploymentContext moduleContext) throws DeploymentException {
        Configuration configuration = moduleContext.getConfiguration();
        AbstractNameQuery webModuleQuery = new AbstractNameQuery(configuration.getId(), Collections.EMPTY_MAP, Collections.singleton(JettyWebAppContext.class.getName()));
        try {
            return configuration.findGBeanData(webModuleQuery);
        } catch (GBeanNotFoundException e) {
            throw new DeploymentException("Could not locate web module gbean in web app configuration", e);
        }
    }

    public QNameSet getSpecQNameSet() {
        return QNameSet.EMPTY;
    }

    public QNameSet getPlanQNameSet() {
        return CLUSTERING_WADI_QNAME_SET;
    }

    private GerClusteringWadiType getWadiClusterConfig(XmlObject container) throws DeploymentException {
        XmlObject[] items = container.selectChildren(CLUSTERING_WADI_QNAME_SET);
        if (items.length > 1) {
            throw new DeploymentException("Unexpected count of clustering elements in geronimo plan " + items.length + " qnameset: " + CLUSTERING_WADI_QNAME_SET);
        }
        if (items.length == 1) {
            return (GerClusteringWadiType) items[0].copy().changeType(GerClusteringWadiType.type);
        }
        return null;
    }

    private AbstractName addSessionManager(GerClusteringWadiType clustering,
            GBeanData webModuleData,
            DeploymentContext moduleContext) throws GBeanAlreadyExistsException {
        AbstractName name = moduleContext.getNaming().createChildName(moduleContext.getModuleName(),
                "WADISessionManager", NameFactory.GERONIMO_SERVICE);

        GBeanData beanData = new GBeanData(name, BasicWADISessionManager.GBEAN_INFO);

        setConfigInfo(clustering, webModuleData, beanData);
        setCluster(clustering, beanData);
        setBackingStrategyFactory(clustering, beanData);

        moduleContext.addGBean(beanData);

        return name;
    }
    
    private void setConfigInfo(GerClusteringWadiType clustering, GBeanData webModuleData, GBeanData beanData) {
        int sweepInterval = defaultSweepInterval;
        if (clustering.isSetSweepInterval()) {
            sweepInterval = clustering.getSweepInterval().intValue();
        }
        int numPartitions = defaultNumPartitions;
        if (clustering.isSetNumPartitions()) {
            numPartitions = clustering.getNumPartitions().intValue();
        }
        Integer sessionTimeout = (Integer) webModuleData.getAttribute(JettyWebAppContext.GBEAN_ATTR_SESSION_TIMEOUT);
        if (null == sessionTimeout) {
            throw new AssertionError();
        }
        
        WADISessionManagerConfigInfo configInfo = new WADISessionManagerConfigInfo(
                beanData.getAbstractName().toURI(),
                sweepInterval,
                numPartitions,
                sessionTimeout.intValue());
        beanData.setAttribute(BasicWADISessionManager.GBEAN_ATTR_WADI_CONFIG_INFO, configInfo);
    }

    private void setCluster(GerClusteringWadiType clustering, GBeanData beanData) {
        Set patterns = new HashSet();
        if (clustering.isSetCluster()) {
            addAbstractNameQueries(patterns, clustering.getCluster().getPatternArray());
        } else {
            patterns.add(defaultClusterName);
        }
        beanData.setReferencePatterns(BasicWADISessionManager.GBEAN_REF_CLUSTER, patterns);
    }

    private void setBackingStrategyFactory(GerClusteringWadiType clustering, GBeanData beanData) {
        Set patterns = new HashSet();
        if (clustering.isSetBackingStrategyFactory()) {
            addAbstractNameQueries(patterns, clustering.getBackingStrategyFactory().getPatternArray());
        } else {
            patterns.add(defaultBackingStrategyFactoryName);
        }
        beanData.setReferencePatterns(BasicWADISessionManager.GBEAN_REF_BACKING_STRATEGY_FACTORY, patterns);
    }

    private AbstractName addPreHandlerFactory(DeploymentContext moduleContext,
            GBeanData webModuleData, AbstractName sessionManagerName) throws GBeanAlreadyExistsException {
        AbstractName name = moduleContext.getNaming().createChildName(moduleContext.getModuleName(),
                "WADIClusteredPreHandlerFactory", NameFactory.GERONIMO_SERVICE);

        GBeanData beanData = new GBeanData(name, WADIClusteredPreHandlerFactory.GBEAN_INFO);
        beanData.setReferencePattern(WADIClusteredPreHandlerFactory.GBEAN_REF_WADI_SESSION_MANAGER, sessionManagerName);

        webModuleData.setReferencePattern(JettyWebAppContext.GBEAN_REF_PRE_HANDLER_FACTORY, name);

        moduleContext.addGBean(beanData);

        return name;
    }

    private AbstractName addSessionHandlerFactory(DeploymentContext moduleContext,
            GBeanData webModuleData, AbstractName sessionManagerName) throws GBeanAlreadyExistsException {
        AbstractName name = moduleContext.getNaming().createChildName(moduleContext.getModuleName(),
                "ClusteredSessionHandlerFactory", NameFactory.GERONIMO_SERVICE);

        GBeanData beanData = new GBeanData(name, ClusteredSessionHandlerFactory.GBEAN_INFO);
        beanData.setReferencePattern(ClusteredSessionHandlerFactory.GBEAN_REF_SESSION_MANAGER, sessionManagerName);

        webModuleData.setReferencePattern(JettyWebAppContext.GBEAN_REF_SESSION_HANDLER_FACTORY, name);

        moduleContext.addGBean(beanData);

        return name;
    }

    private void addAbstractNameQueries(Set patterns, GerPatternType[] patternTypes) {
        for (int i = 0; i < patternTypes.length; i++) {
            AbstractNameQuery query = ENCConfigBuilder.buildAbstractNameQuery(patternTypes[i], null, null, null);
            patterns.add(query);
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    public static final String GBEAN_ATTR_DFT_SWEEP_INTERVAL = "defaultSweepInterval";
    public static final String GBEAN_ATTR_DFT_NUM_PARTITIONS = "defaultNumPartitions";
    public static final String GBEAN_ATTR_DFT_BACKING_STRATEGY_FACTORY_NAME = "defaultBackingStrategyFactoryName";
    public static final String GBEAN_ATTR_DFT_CLUSTER_NAME = "defaultClusterName";
    public static final String GBEAN_ATTR_DFT_ENVIRONMENT = "defaultEnvironment";

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic("WADI Session Manager",
                WADIJettyClusteringBuilder.class,
                NameFactory.MODULE_BUILDER);

        infoBuilder.addAttribute(GBEAN_ATTR_DFT_SWEEP_INTERVAL, int.class, true);
        infoBuilder.addAttribute(GBEAN_ATTR_DFT_NUM_PARTITIONS, int.class, true);
        infoBuilder.addAttribute(GBEAN_ATTR_DFT_BACKING_STRATEGY_FACTORY_NAME, AbstractNameQuery.class, true);
        infoBuilder.addAttribute(GBEAN_ATTR_DFT_CLUSTER_NAME, AbstractNameQuery.class, true);
        infoBuilder.addAttribute(GBEAN_ATTR_DFT_ENVIRONMENT, Environment.class, true);

        infoBuilder.setConstructor(new String[]{GBEAN_ATTR_DFT_SWEEP_INTERVAL,
                GBEAN_ATTR_DFT_NUM_PARTITIONS,
                GBEAN_ATTR_DFT_BACKING_STRATEGY_FACTORY_NAME,
                GBEAN_ATTR_DFT_CLUSTER_NAME,
                GBEAN_ATTR_DFT_ENVIRONMENT});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
