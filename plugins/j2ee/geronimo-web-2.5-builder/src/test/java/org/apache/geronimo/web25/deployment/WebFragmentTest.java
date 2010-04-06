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

package org.apache.geronimo.web25.deployment;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationResolver;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.LifecycleException;
import org.apache.geronimo.kernel.config.LifecycleMonitor;
import org.apache.geronimo.kernel.config.LifecycleResults;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.config.NoSuchStoreException;
import org.apache.geronimo.kernel.osgi.MockBundle;
import org.apache.geronimo.kernel.osgi.MockBundleContext;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.repository.Version;
import org.apache.geronimo.kernel.util.FileUtils;
import org.apache.geronimo.testsupport.XmlBeansTestSupport;
import org.apache.geronimo.web25.deployment.merge.MergeHelper;
import org.apache.geronimo.web25.deployment.merge.webfragment.WebFragmentEntry;
import org.apache.geronimo.xbeans.javaee6.WebAppDocument;
import org.apache.geronimo.xbeans.javaee6.WebAppType;
import org.apache.geronimo.xbeans.javaee6.WebFragmentDocument;
import org.apache.xmlbeans.XmlObject;
import org.osgi.framework.Bundle;

/**
 *
 *
 * @version $Rev$ $Date$
 */
public class WebFragmentTest extends XmlBeansTestSupport {

    private ClassLoader classLoader = WebFragmentTest.class.getClassLoader();

    /**
     * Test points :
     * a. All the ordering configuration in the web-fragments should be ignored
     * b. The name in the absolute ordering configuration might be not present in founded web-fragment.xml
     * c. If others is configured, all the web-fragment should be included,
     * @throws Exception
     */
    public void testAbsoluteSortWithOthers() throws Exception {
        Map<String, WebFragmentDocument> jarURLWebFragmentDocumentMap = new LinkedHashMap<String, WebFragmentDocument>();
        jarURLWebFragmentDocumentMap.put("WEB-INF/lib/testA.jar", (WebFragmentDocument) loadXmlObject("webfragments/absolute/webfragmentA.xml"));
        jarURLWebFragmentDocumentMap.put("WEB-INF/lib/testB.jar", (WebFragmentDocument) loadXmlObject("webfragments/absolute/webfragmentB.xml"));
        jarURLWebFragmentDocumentMap.put("WEB-INF/lib/testC.jar", (WebFragmentDocument) loadXmlObject("webfragments/absolute/webfragmentC.xml"));
        jarURLWebFragmentDocumentMap.put("WEB-INF/lib/testD.jar", (WebFragmentDocument) loadXmlObject("webfragments/absolute/webfragmentD.xml"));
        WebAppType webApp = ((WebAppDocument) loadXmlObject("webfragments/absolute/web-withothers.xml")).getWebApp();
        WebFragmentEntry[] webFragmentEntries = MergeHelper.sortWebFragments(new DummyEARContext(), null, null, webApp, jarURLWebFragmentDocumentMap);
        Assert.assertEquals(4, webFragmentEntries.length);
        Assert.assertEquals("webfragmentD", webFragmentEntries[0].getName());
        Assert.assertEquals("webfragmentB", webFragmentEntries[1].getName());
        Assert.assertEquals("webfragmentC", webFragmentEntries[2].getName());
        Assert.assertEquals("webfragmentA", webFragmentEntries[3].getName());
    }

    /**
     * Test points :
     * a. All the ordering configuration in the web-fragments should be ignored
     * b. The name in the absolute ordering configuration might be not present in founded web-fragment.xml
     * c. If others element is not configured, only those explicitly configured web fragments are included
     * @throws Exception
     */
    public void testAbsoluteSortWithoutOthers() throws Exception {
        Map<String, WebFragmentDocument> jarURLWebFragmentDocumentMap = new LinkedHashMap<String, WebFragmentDocument>();
        jarURLWebFragmentDocumentMap.put("WEB-INF/lib/testA.jar", (WebFragmentDocument) loadXmlObject("webfragments/absolute/webfragmentA.xml"));
        jarURLWebFragmentDocumentMap.put("WEB-INF/lib/testB.jar", (WebFragmentDocument) loadXmlObject("webfragments/absolute/webfragmentB.xml"));
        jarURLWebFragmentDocumentMap.put("WEB-INF/lib/testC.jar", (WebFragmentDocument) loadXmlObject("webfragments/absolute/webfragmentC.xml"));
        jarURLWebFragmentDocumentMap.put("WEB-INF/lib/testD.jar", (WebFragmentDocument) loadXmlObject("webfragments/absolute/webfragmentD.xml"));
        WebAppType webApp = ((WebAppDocument) loadXmlObject("webfragments/absolute/web-withoutothers.xml")).getWebApp();
        WebFragmentEntry[] webFragmentEntries = MergeHelper.sortWebFragments(new DummyEARContext(), null, null, webApp, jarURLWebFragmentDocumentMap);
        Assert.assertEquals(2, webFragmentEntries.length);
        Assert.assertEquals("webfragmentD", webFragmentEntries[0].getName());
        Assert.assertEquals("webfragmentA", webFragmentEntries[1].getName());
    }

    public void testRelativeSort() throws Exception {
        Map<String, WebFragmentDocument> jarURLWebFragmentDocumentMap = new LinkedHashMap<String, WebFragmentDocument>();
        //A  -(after)-> B
        jarURLWebFragmentDocumentMap.put("WEB-INF/lib/testA.jar", (WebFragmentDocument) loadXmlObject("webfragments/relative/webfragmentA.xml"));
        //B
        jarURLWebFragmentDocumentMap.put("WEB-INF/lib/testB.jar", (WebFragmentDocument) loadXmlObject("webfragments/relative/webfragmentB.xml"));
        //C -(before) -> others
        jarURLWebFragmentDocumentMap.put("WEB-INF/lib/testC.jar", (WebFragmentDocument) loadXmlObject("webfragments/relative/webfragmentC.xml"));
        //D -(after) -> others
        jarURLWebFragmentDocumentMap.put("WEB-INF/lib/testD.jar", (WebFragmentDocument) loadXmlObject("webfragments/relative/webfragmentD.xml"));
        WebAppType webApp = ((WebAppDocument) loadXmlObject("webfragments/relative/web.xml")).getWebApp();
        WebFragmentEntry[] webFragmentEntries = MergeHelper.sortWebFragments(new DummyEARContext(), null, null, webApp, jarURLWebFragmentDocumentMap);
        Assert.assertEquals("webfragmentC", webFragmentEntries[0].getName());
        Assert.assertEquals("webfragmentB", webFragmentEntries[1].getName());
        Assert.assertEquals("webfragmentA", webFragmentEntries[2].getName());
        Assert.assertEquals("webfragmentD", webFragmentEntries[3].getName());
    }

    /**
     * Test Points :
     * a. A -> A
     * @throws Exception
     */
    public void testCircusDependencyA() throws Exception {
        try {
            Map<String, WebFragmentDocument> jarURLWebFragmentDocumentMap = new LinkedHashMap<String, WebFragmentDocument>();
            //A  -(before)-> A
            jarURLWebFragmentDocumentMap.put("WEB-INF/lib/testA.jar", (WebFragmentDocument) loadXmlObject("webfragments/circus/circusA/webfragmentA.xml"));
            jarURLWebFragmentDocumentMap.put("WEB-INF/lib/testB.jar", (WebFragmentDocument) loadXmlObject("webfragments/circus/circusA/webfragmentB.xml"));
            jarURLWebFragmentDocumentMap.put("WEB-INF/lib/testC.jar", (WebFragmentDocument) loadXmlObject("webfragments/circus/circusA/webfragmentC.xml"));
            jarURLWebFragmentDocumentMap.put("WEB-INF/lib/testD.jar", (WebFragmentDocument) loadXmlObject("webfragments/circus/circusA/webfragmentD.xml"));
            WebAppType webApp = ((WebAppDocument) loadXmlObject("webfragments/circus/circusA/web.xml")).getWebApp();
            MergeHelper.sortWebFragments(new DummyEARContext(), null, null, webApp, jarURLWebFragmentDocumentMap);
            fail("Circus Dependency should be found");
        } catch (DeploymentException e) {
            Assert.assertTrue(e.getMessage().indexOf("WEB-INF/lib/testA.jar") != -1);
        }
    }

    /**
     * Test Points :
     * a. A -> B -> A
     * @throws Exception
     */
    public void testCircusDependencyB() throws Exception {
        try {
            Map<String, WebFragmentDocument> jarURLWebFragmentDocumentMap = new LinkedHashMap<String, WebFragmentDocument>();
            //A  -(before)-> B
            jarURLWebFragmentDocumentMap.put("WEB-INF/lib/testA.jar", (WebFragmentDocument) loadXmlObject("webfragments/circus/circusB/webfragmentA.xml"));
            //B -(before) -> A
            jarURLWebFragmentDocumentMap.put("WEB-INF/lib/testB.jar", (WebFragmentDocument) loadXmlObject("webfragments/circus/circusB/webfragmentB.xml"));
            jarURLWebFragmentDocumentMap.put("WEB-INF/lib/testC.jar", (WebFragmentDocument) loadXmlObject("webfragments/circus/circusB/webfragmentC.xml"));
            jarURLWebFragmentDocumentMap.put("WEB-INF/lib/testD.jar", (WebFragmentDocument) loadXmlObject("webfragments/circus/circusB/webfragmentD.xml"));
            WebAppType webApp = ((WebAppDocument) loadXmlObject("webfragments/circus/circusB/web.xml")).getWebApp();
            MergeHelper.sortWebFragments(new DummyEARContext(), null, null, webApp, jarURLWebFragmentDocumentMap);
            fail("Circus Dependency should be found");
        } catch (DeploymentException e) {
            Assert.assertTrue(e.getMessage().indexOf("WEB-INF/lib/testA.jar") != -1 || e.getMessage().indexOf("WEB-INF/lib/testB.jar") != -1);
        }
    }

    /**
     * Test Points :
     * a. A -> B -> C -> A
     * @throws Exception
     */
    public void testCircusDependencyC() throws Exception {
        try {
            Map<String, WebFragmentDocument> jarURLWebFragmentDocumentMap = new LinkedHashMap<String, WebFragmentDocument>();
            //A  -(after)-> B
            jarURLWebFragmentDocumentMap.put("WEB-INF/lib/testA.jar", (WebFragmentDocument) loadXmlObject("webfragments/circus/circusC/webfragmentA.xml"));
            //B - (after) -> D
            jarURLWebFragmentDocumentMap.put("WEB-INF/lib/testB.jar", (WebFragmentDocument) loadXmlObject("webfragments/circus/circusC/webfragmentB.xml"));
            //C -(before) -> others
            jarURLWebFragmentDocumentMap.put("WEB-INF/lib/testC.jar", (WebFragmentDocument) loadXmlObject("webfragments/circus/circusC/webfragmentC.xml"));
            //D -(after) -> A
            jarURLWebFragmentDocumentMap.put("WEB-INF/lib/testD.jar", (WebFragmentDocument) loadXmlObject("webfragments/circus/circusC/webfragmentD.xml"));
            WebAppType webApp = ((WebAppDocument) loadXmlObject("webfragments/circus/circusC/web.xml")).getWebApp();
            MergeHelper.sortWebFragments(new DummyEARContext(), null, null, webApp, jarURLWebFragmentDocumentMap);
            fail("Circus Dependency should be found");
        } catch (DeploymentException e) {
            Assert.assertTrue(e.getMessage().indexOf("WEB-INF/lib/testA.jar") != -1 || e.getMessage().indexOf("WEB-INF/lib/testB.jar") != -1 || e.getMessage().indexOf("WEB-INF/lib/testD.jar") != -1);
        }
    }

    private XmlObject loadXmlObject(String url) throws Exception {
        URL srcXml = classLoader.getResource(url);
        return XmlBeansUtil.parse(srcXml, classLoader);
    }

    public static class DummyConfigurationManager implements ConfigurationManager {

        @Override
        public ArtifactResolver getArtifactResolver() {
            return null;
        }

        @Override
        public Bundle getBundle(Artifact id) {
            return null;
        }

        @Override
        public Configuration getConfiguration(Artifact configurationId) {
            return null;
        }

        @Override
        public Artifact[] getInstalled(Artifact query) {
            return null;
        }

        @Override
        public Artifact[] getLoaded(Artifact query) {
            return null;
        }

        @Override
        public Collection<? extends Repository> getRepositories() {
            return null;
        }

        @Override
        public Artifact[] getRunning(Artifact query) {
            return null;
        }

        @Override
        public ConfigurationStore getStoreForConfiguration(Artifact configuration) {
            return null;
        }

        @Override
        public ConfigurationStore[] getStores() {
            return null;
        }

        @Override
        public boolean isConfiguration(Artifact artifact) {
            return false;
        }

        @Override
        public boolean isInstalled(Artifact configurationId) {
            return false;
        }

        @Override
        public boolean isLoaded(Artifact configurationId) {
            return false;
        }

        @Override
        public boolean isOnline() {
            return false;
        }

        @Override
        public boolean isRunning(Artifact configurationId) {
            return false;
        }

        @Override
        public List listConfigurations() {
            return null;
        }

        @Override
        public List listConfigurations(AbstractName store) throws NoSuchStoreException {
            return null;
        }

        @Override
        public List<AbstractName> listStores() {
            return null;
        }

        @Override
        public LifecycleResults loadConfiguration(Artifact configurationId) throws NoSuchConfigException, LifecycleException {
            return null;
        }

        @Override
        public LifecycleResults loadConfiguration(Artifact configurationId, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
            return null;
        }

        @Override
        public LifecycleResults loadConfiguration(ConfigurationData configurationData) throws NoSuchConfigException, LifecycleException {
            return null;
        }

        @Override
        public LifecycleResults loadConfiguration(ConfigurationData configurationData, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
            return null;
        }

        @Override
        public ConfigurationResolver newConfigurationResolver(ConfigurationData configurationData) {
            return null;
        }

        @Override
        public LifecycleResults reloadConfiguration(Artifact configurationId) throws NoSuchConfigException, LifecycleException {
            return null;
        }

        @Override
        public LifecycleResults reloadConfiguration(Artifact configurationId, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
            return null;
        }

        @Override
        public LifecycleResults reloadConfiguration(Artifact configurationId, Version version) throws NoSuchConfigException, LifecycleException {
            return null;
        }

        @Override
        public LifecycleResults reloadConfiguration(Artifact configurationId, Version version, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
            return null;
        }

        @Override
        public LifecycleResults reloadConfiguration(ConfigurationData configurationData) throws NoSuchConfigException, LifecycleException {
            return null;
        }

        @Override
        public LifecycleResults reloadConfiguration(ConfigurationData configurationData, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
            return null;
        }

        @Override
        public LinkedHashSet<Artifact> resolveParentIds(ConfigurationData configurationData) throws MissingDependencyException, InvalidConfigException {
            return null;
        }

        @Override
        public LifecycleResults restartConfiguration(Artifact configurationId) throws NoSuchConfigException, LifecycleException {
            return null;
        }

        @Override
        public LifecycleResults restartConfiguration(Artifact configurationId, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
            return null;
        }

        @Override
        public void setOnline(boolean online) {
        }

        @Override
        public LinkedHashSet<Artifact> sort(List<Artifact> ids, LifecycleMonitor monitor) throws InvalidConfigException, IOException, NoSuchConfigException, MissingDependencyException {
            return null;
        }

        @Override
        public LifecycleResults startConfiguration(Artifact configurationId) throws NoSuchConfigException, LifecycleException {
            return null;
        }

        @Override
        public LifecycleResults startConfiguration(Artifact configurationId, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
            return null;
        }

        @Override
        public LifecycleResults stopConfiguration(Artifact configurationId) throws NoSuchConfigException {
            return null;
        }

        @Override
        public LifecycleResults stopConfiguration(Artifact configurationId, LifecycleMonitor monitor) throws NoSuchConfigException {
            return null;
        }

        @Override
        public void uninstallConfiguration(Artifact configurationId) throws IOException, NoSuchConfigException {
        }

        @Override
        public LifecycleResults unloadConfiguration(Artifact configurationId) throws NoSuchConfigException {
            return null;
        }

        @Override
        public LifecycleResults unloadConfiguration(Artifact configurationId, LifecycleMonitor monitor) throws NoSuchConfigException {
            return null;
        }
    }

    private static class DummyEARContext extends EARContext {

        /**
         *  public EARContext(File baseDir,
                      File inPlaceConfigurationDir,
                      Environment environment,
                      ConfigurationModuleType moduleType,
                      Naming naming,
                      ConfigurationManager configurationManager,
                      BundleContext bundleContext,
                      AbstractNameQuery serverName,
                      AbstractName baseName,
                      AbstractNameQuery transactionManagerObjectName,
                      AbstractNameQuery connectionTrackerObjectName,
                      AbstractNameQuery corbaGBeanObjectName,
                      Map messageDestinations) throws DeploymentException {
        super(baseDir, inPlaceConfigurationDir, environment, baseName, moduleType, naming, configurationManager, bundleContext);

        this.serverName = serverName;
        this.transactionManagerObjectName = transactionManagerObjectName;
        this.connectionTrackerObjectName = connectionTrackerObjectName;
        this.corbaGBeanObjectName = corbaGBeanObjectName;
        this.messageDestinations = messageDestinations;
        }
         */
        public DummyEARContext() throws Exception {
            super(FileUtils.createTempDir(), null, new Environment(), ConfigurationModuleType.WAR, null, new DummyConfigurationManager(), new MockBundleContext(new MockBundle(WebFragmentTest.class
                    .getClassLoader(), "", 1L)), null, null, null, null, null, null);
        }
    }
}
