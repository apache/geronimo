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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;
import org.apache.geronimo.clustering.wadi.BasicWADISessionManager;
import org.apache.geronimo.clustering.wadi.WADISessionManagerConfigInfo;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.NamespaceDrivenBuilder;
import org.apache.geronimo.deployment.NamespaceDrivenBuilderCollection;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.ReferencePatterns;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Dependency;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.ImportType;
import org.apache.geronimo.tomcat.TomcatWebAppContext;
import org.apache.geronimo.tomcat.cluster.ClusteredManagerRetriever;
import org.apache.geronimo.tomcat.cluster.wadi.WADIClusteredValveRetriever;
import org.apache.geronimo.web.deployment.GenericToSpecificPlanConverter;
import org.apache.geronimo.xbeans.geronimo.web.tomcat.TomcatWebAppDocument;
import org.apache.geronimo.xbeans.geronimo.web.tomcat.TomcatWebAppType;
import org.apache.geronimo.xbeans.geronimo.web.tomcat.config.GerTomcatDocument;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.codehaus.wadi.replication.strategy.BackingStrategyFactory;

/**
 *
 * @version $Rev:$ $Date:$
 */
public class WADITomcatClusteringBuilderTest extends TestCase {

    private AbstractNameQuery clusterNameQuery;
    private AbstractNameQuery backingStrategyFactoryQuery;
    private WADITomcatClusteringBuilder builder;
    private Artifact artifact;
    private Artifact artifactToRemove;
    private Map<AbstractName, GBeanData> addedGBeanData;
    private String contextPath;
    private Artifact dependencyToMerge;

    @Override
    protected void setUp() throws Exception {
        artifact = new Artifact("groupId", "artifactId", "2.0", "car");
        artifactToRemove = new Artifact("groupId", "artifactToRemoveId", (String) null, "car");
        clusterNameQuery = new AbstractNameQuery(artifact, Collections.singletonMap("name", "ClusterName"));
        backingStrategyFactoryQuery = new AbstractNameQuery(BackingStrategyFactory.class.getName());
        contextPath = "/test-path";
        addedGBeanData = new HashMap<AbstractName, GBeanData>();
        
        Environment defaultEnvironment = new Environment();
        dependencyToMerge = new Artifact("groupId", "artifactToMergeId", "2.0", "car");
        defaultEnvironment.addDependency(new Dependency(dependencyToMerge, ImportType.ALL));
        builder = new MockedEnvironment(1,
                2,
                3,
                backingStrategyFactoryQuery,
                clusterNameQuery,
                artifactToRemove,
                defaultEnvironment);
        
        new NamespaceDrivenBuilderCollection(Collections.<NamespaceDrivenBuilder>singleton(builder));
    }
    
    public void testBuiltEnvironmentDoesNotContainArtifactToRemove() throws Exception {
        XmlObject container = newContainer("testBuiltGBeans");

        Environment environment = new Environment();
        environment.addDependency(new Dependency(new Artifact(artifactToRemove.getGroupId(),
                artifactToRemove.getArtifactId(),
                "2.0",
                artifactToRemove.getType()), ImportType.ALL));
        builder.buildEnvironment(container, environment);
        
        List<Dependency> dependencies = environment.getDependencies();
        assertEquals(1, dependencies.size());
        assertEquals(dependencyToMerge, dependencies.get(0).getArtifact());
    }
    
    public void testBuiltGBeans() throws Exception {
        XmlObject container = newContainer("testBuiltGBeans");

        builder.build(container, null, null);
        
        GBeanData beanData = addedGBeanData.get(newGBeanNameFor("WADISessionManager"));
        assertSessionManagerWithDefault(beanData);
        
        beanData = addedGBeanData.get(newGBeanNameFor("ClusteredManagerRetriever"));
        assertClusteredManagerRetriever(beanData);
        
        beanData = addedGBeanData.get(newGBeanNameFor("WADIClusteredValveRetriever"));
        assertClusteredValveRetriever(beanData);
    }

    public void testOverrideDefaults() throws Exception {
        XmlObject container = newContainer("testOverrideDefaults");
        
        builder.build(container, null, null);
        
        GBeanData beanData = addedGBeanData.get(newGBeanNameFor("WADISessionManager"));
        assertPattern(beanData, BasicWADISessionManager.GBEAN_REF_BACKING_STRATEGY_FACTORY,
            new AbstractNameQuery(null, Collections.singletonMap("name", "SpecificFactoryName")));
        assertPattern(beanData, BasicWADISessionManager.GBEAN_REF_CLUSTER,
            new AbstractNameQuery(null, Collections.singletonMap("name", "SpecificClusterName")));
        
        WADISessionManagerConfigInfo configInfo = (WADISessionManagerConfigInfo)
            beanData.getAttribute(BasicWADISessionManager.GBEAN_ATTR_WADI_CONFIG_INFO);
        assertEquals(10, configInfo.getSweepInterval());
        assertEquals(2, configInfo.getSessionTimeoutSeconds());
        assertEquals(12, configInfo.getNumPartitions());
        assertTrue(configInfo.isDeltaReplication());
        assertTrue(configInfo.isDisableReplication());
    }
    
    private void assertClusteredValveRetriever(GBeanData beanData) {
        assertNotNull(beanData);
        assertName(beanData,
            WADIClusteredValveRetriever.GBEAN_REF_WADI_SESSION_MANAGER,
            newGBeanNameFor("WADISessionManager"));
    }

    private void assertClusteredManagerRetriever(GBeanData beanData) {
        assertNotNull(beanData);
        assertName(beanData,
            ClusteredManagerRetriever.GBEAN_REF_SESSION_MANAGER,
            newGBeanNameFor("WADISessionManager"));
    }

    private void assertSessionManagerWithDefault(GBeanData beanData) throws URISyntaxException {
        assertNotNull(beanData);
        assertPattern(beanData, BasicWADISessionManager.GBEAN_REF_BACKING_STRATEGY_FACTORY, backingStrategyFactoryQuery);
        assertPattern(beanData, BasicWADISessionManager.GBEAN_REF_CLUSTER, clusterNameQuery);
        
        WADISessionManagerConfigInfo configInfo = (WADISessionManagerConfigInfo)
            beanData.getAttribute(BasicWADISessionManager.GBEAN_ATTR_WADI_CONFIG_INFO);
        assertEquals(1, configInfo.getSweepInterval());
        assertEquals(2, configInfo.getSessionTimeoutSeconds());
        assertEquals(3, configInfo.getNumPartitions());
        assertEquals(new URI(contextPath), configInfo.getServiceSpaceURI());
        assertFalse(configInfo.isDeltaReplication());
        assertFalse(configInfo.isDisableReplication());
    }

    private void assertPattern(GBeanData beanData, String refName, AbstractNameQuery query) {
        ReferencePatterns refPatterns = beanData.getReferencePatterns(refName);
        Set<AbstractNameQuery> patterns = refPatterns.getPatterns();
        assertEquals(1, patterns.size());
        AbstractNameQuery pattern = patterns.iterator().next();
        assertEquals(query, pattern);
    }

    private void assertName(GBeanData beanData, String refName, AbstractName name) {
        ReferencePatterns refPatterns = beanData.getReferencePatterns(refName);
        assertEquals(name, refPatterns.getAbstractName());
    }
    
    private AbstractName newGBeanNameFor(String name) {
        return new AbstractName(artifact, Collections.singletonMap("name", name));
    }

    private XmlObject newContainer(String suffix) throws IOException, XmlException, DeploymentException {
        XmlObject rawPlan = XmlBeansUtil.parse(
            getClass().getResourceAsStream("WADITomcatClusteringBuilder_" + suffix + ".xml"));
        GenericToSpecificPlanConverter planConverter = new GenericToSpecificPlanConverter(
            GerTomcatDocument.type.getDocumentElementName().getNamespaceURI(),
            TomcatWebAppDocument.type.getDocumentElementName().getNamespaceURI(), "tomcat");
        XmlObject container = planConverter.convertToSpecificPlan(rawPlan);
        container = container.changeType(TomcatWebAppType.type);
        XmlBeansUtil.validateDD(container);
        return container;
    }

    private final class MockedEnvironment extends WADITomcatClusteringBuilder {

        private MockedEnvironment(int defaultSweepInterval,
                int defaultSessionTimeout,
                int defaultNumPartitions,
                AbstractNameQuery defaultBackingStrategyFactoryName,
                AbstractNameQuery defaultClusterName,
                Artifact artifactToRemoveFromEnvironment,
                Environment defaultEnvironment) {
            super(defaultSweepInterval,
                    defaultSessionTimeout,
                    defaultNumPartitions,
                    defaultBackingStrategyFactoryName,
                    defaultClusterName,
                    artifactToRemoveFromEnvironment,
                    defaultEnvironment);
        }

        @Override
        protected GBeanData extractWebModule(DeploymentContext moduleContext) throws DeploymentException {
            GBeanData gbeanData = new GBeanData(TomcatWebAppContext.class);
            gbeanData.setAttribute("contextPath", contextPath);
            return gbeanData;
        }

        @Override
        protected AbstractName newGBeanName(DeploymentContext moduleContext, String name) {
            return newGBeanNameFor(name);
        }

        @Override
        protected void addGBean(DeploymentContext moduleContext, GBeanData beanData) throws GBeanAlreadyExistsException {
            addedGBeanData.put(beanData.getAbstractName(), beanData);
        }
    }

}
