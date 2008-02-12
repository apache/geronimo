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
package org.apache.geronimo.deployment;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.jar.JarFile;

import javax.sql.DataSource;

import junit.framework.TestCase;
import net.sf.cglib.core.DefaultGeneratorStrategy;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import org.apache.geronimo.kernel.Jsr77Naming;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.SimpleConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.NoSuchStoreException;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.LifecycleResults;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.config.LifecycleException;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.LifecycleMonitor;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.ArtifactManager;
import org.apache.geronimo.kernel.repository.DefaultArtifactManager;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.DefaultArtifactResolver;
import org.apache.geronimo.kernel.repository.Version;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.common.DeploymentException;

/**
 * @version $Rev$ $Date$
 */
public class DeploymentContextTest extends TestCase {
    private byte[] classBytes;

    public void testAddClass() throws Exception {
        File basedir = File.createTempFile("car", "tmp");
        basedir.delete();
        basedir.mkdirs();
        try {
            basedir.deleteOnExit();
            Environment environment = new Environment();
            Artifact configId = new Artifact("foo", "artifact", "1", "car");
            environment.setConfigId(configId);
            ArtifactManager artifactManager = new DefaultArtifactManager();
            ArtifactResolver artifactResolver = new DefaultArtifactResolver(artifactManager, Collections.EMPTY_SET, null);
            SimpleConfigurationManager configurationManager = new SimpleConfigurationManager(Collections.EMPTY_SET, artifactResolver, Collections.EMPTY_SET);
            DeploymentContext context = new DeploymentContext(basedir, null, environment, null, ConfigurationModuleType.CAR, new Jsr77Naming(), configurationManager, Collections.EMPTY_SET);
            Enhancer enhancer = new Enhancer();
            enhancer.setInterfaces(new Class[]{DataSource.class});
            enhancer.setCallbackType(MethodInterceptor.class);
            enhancer.setStrategy(new DefaultGeneratorStrategy() {
                public byte[] transform(byte[] b) {
                    classBytes = b;
                    return b;
                }
            });
            enhancer.setClassLoader(new URLClassLoader(new URL[0], this.getClass().getClassLoader()));
            Class type = enhancer.createClass();
            URI location = new URI("cglib/");
            context.addClass(location, type.getName(), classBytes);
            ClassLoader cl = context.getClassLoader();
            Class loadedType = cl.loadClass(type.getName());
            assertTrue(DataSource.class.isAssignableFrom(loadedType));
            assertTrue(type != loadedType);
        } finally {
            recursiveDelete(basedir);
        }
    }

    private void recursiveDelete(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File file1 : files) {
                recursiveDelete(file1);
            }
        }
        file.delete();
    }


    //test the getCompleteManifestClassPath method

    private static class MockJarFile extends JarFile {
        private final URI relativeURI;
        private final String manifestClasspath;

        /**
         * Constructor
         * @param relativeURI "path" of "jar" within "ear"
         * @param manifestClasspath manifest classpath of jar. Entries should be relative to root of "ear"
         * @throws IOException ain't gonna happen.
         */
        public MockJarFile(URI relativeURI, String manifestClasspath) throws IOException {
            super(DeploymentUtil.DUMMY_JAR_FILE);
            this.relativeURI = relativeURI;
            this.manifestClasspath = manifestClasspath;
        }

        public URI getRelativeURI() {
            return relativeURI;
        }

        public String getManifestClasspath() {
            return manifestClasspath;
        }
    }

    static class MockJarFileFactory implements DeploymentContext.JarFileFactory {

        private final Map<URI, String> data;

        public MockJarFileFactory(Map<URI, String> data) {
            this.data = data;
        }

        public JarFile newJarFile(URI relativeURI) throws IOException {
            String manifestcp = data.get(relativeURI);
            return new MockJarFile(relativeURI, manifestcp);
        }

        public String getManifestClassPath(JarFile jarFile) throws IOException {
            return ((MockJarFile)jarFile).getManifestClasspath();
        }
    }
    
    public void testManifestClassPath1() throws Exception {
        MockJarFile start = new MockJarFile(URI.create("ejb.jar"), "lib1.jar");
        URI resolutionURI = URI.create(".");
        ModuleList exclusions = new ModuleList();
        Map<URI, String> data = new HashMap<URI, String>();
        data.put(URI.create("lib1.jar"), "lib1.jar lib2.jar");

        DeploymentContext.JarFileFactory factory = new MockJarFileFactory(data);
        DeploymentContext context = new DeploymentContext(new File("."), null, new Environment(Artifact.create("test/foo/1/ear")), new AbstractName(URI.create("test/foo/1/ear?name=test")), ConfigurationModuleType.EAR, new Jsr77Naming(), new MockConfigurationManager());
        ClassPathList classPathList = new ClassPathList();
        context.getCompleteManifestClassPath(start, start.getRelativeURI(), resolutionURI, classPathList, exclusions, factory, new ArrayList<DeploymentException>());
        assertEquals(2, classPathList.size());
    }

    public void testManifestClassPath2() throws Exception {
        MockJarFile start = new MockJarFile(URI.create("ejb1/ejb1/ejb1.jar"), "../../lib1/lib1/lib1.jar");
        URI resolutionURI = URI.create(".");
        ModuleList exclusions = new ModuleList();
        Map<URI, String> data = new HashMap<URI, String>();
        data.put(URI.create("lib1/lib1/lib1.jar"), "../../lib2/lib2.jar");
        data.put(URI.create("lib2/lib2.jar"), "lib2a.jar");
        data.put(URI.create("lib2/lib2a.jar"), "../lib3.jar ../lib1/lib1/lib1.jar");

        DeploymentContext.JarFileFactory factory = new MockJarFileFactory(data);
        DeploymentContext context = new DeploymentContext(new File("."), null, new Environment(Artifact.create("test/foo/1/ear")), new AbstractName(URI.create("test/foo/1/ear?name=test")), ConfigurationModuleType.EAR, new Jsr77Naming(), new MockConfigurationManager());
        ClassPathList classPathList = new ClassPathList();
        context.getCompleteManifestClassPath(start, start.getRelativeURI(), resolutionURI, classPathList, exclusions, factory, new ArrayList<DeploymentException>());
        assertEquals(4, classPathList.size());
        assertEquals("lib1/lib1/lib1.jar", classPathList.get(0));
        assertEquals("lib2/lib2.jar", classPathList.get(1));
        assertEquals("lib2/lib2a.jar", classPathList.get(2));
        assertEquals("lib3.jar", classPathList.get(3));
    }

    public void testManifestClassPathWar1() throws Exception {
        MockJarFile start = new MockJarFile(URI.create("war1.war"), "lib1.jar");
        URI resolutionURI = URI.create("../");
        ModuleList exclusions = new ModuleList();
        Map<URI, String> data = new HashMap<URI, String>();
        data.put(URI.create("lib1.jar"), "lib1.jar lib2.jar");

        DeploymentContext.JarFileFactory factory = new MockJarFileFactory(data);
        DeploymentContext context = new DeploymentContext(new File("."), null, new Environment(Artifact.create("test/foo/1/ear")), new AbstractName(URI.create("test/foo/1/ear?name=test")), ConfigurationModuleType.EAR, new Jsr77Naming(), new MockConfigurationManager());
        ClassPathList classPathList = new ClassPathList();
        context.getCompleteManifestClassPath(start, start.getRelativeURI(), resolutionURI, classPathList, exclusions, factory, new ArrayList<DeploymentException>());
        assertEquals(2, classPathList.size());
        assertEquals("../lib1.jar", classPathList.get(0));
        assertEquals("../lib2.jar", classPathList.get(1));
        //should contain ../lib1.jar ../lib2.jar
    }

    public void testManifestClassPathWar2() throws Exception {
        MockJarFile start = new MockJarFile(URI.create("war1/war1/war1.war"), "../../lib1/lib1/lib1.jar");
        URI resolutionURI = URI.create("../../../");
        ModuleList exclusions = new ModuleList();
        Map<URI, String> data = new HashMap<URI, String>();
        data.put(URI.create("lib1/lib1/lib1.jar"), "../../lib2/lib2/lib2.jar");
        data.put(URI.create("lib2/lib2/lib2.jar"), "../lib2a/lib2a.jar");
        data.put(URI.create("lib2/lib2a/lib2a.jar"), "../../lib3/lib3/lib3.jar ../../lib1/lib1/lib1.jar");

        DeploymentContext.JarFileFactory factory = new MockJarFileFactory(data);
        DeploymentContext context = new DeploymentContext(new File("."), null, new Environment(Artifact.create("test/foo/1/ear")), new AbstractName(URI.create("test/foo/1/ear?name=test")), ConfigurationModuleType.EAR, new Jsr77Naming(), new MockConfigurationManager());
        ClassPathList classPathList = new ClassPathList();
        context.getCompleteManifestClassPath(start, start.getRelativeURI(), resolutionURI, classPathList, exclusions, factory, new ArrayList<DeploymentException>());
        assertEquals(4, classPathList.size());
        assertEquals("../../../lib1/lib1/lib1.jar", classPathList.get(0));
        assertEquals("../../../lib2/lib2/lib2.jar", classPathList.get(1));
        assertEquals("../../../lib2/lib2a/lib2a.jar", classPathList.get(2));
        assertEquals("../../../lib3/lib3/lib3.jar", classPathList.get(3));
    }

    public void testManifestClassPathWar3() throws Exception {
        MockJarFile start = new MockJarFile(URI.create("war1/war1/war1.war"), "../../lib1/lib1/lib1.jar");
        URI resolutionURI = URI.create("../../../");
        ModuleList exclusions = new ModuleList();
        Map<URI, String> data = new HashMap<URI, String>();
        data.put(URI.create("lib1/lib1/lib1.jar"), "../../lib2/lib2.jar");
        data.put(URI.create("lib2/lib2.jar"), "lib2a.jar");
        data.put(URI.create("lib2/lib2a.jar"), "../lib3.jar ../lib1/lib1/lib1.jar");

        DeploymentContext.JarFileFactory factory = new MockJarFileFactory(data);
        DeploymentContext context = new DeploymentContext(new File("."), null, new Environment(Artifact.create("test/foo/1/ear")), new AbstractName(URI.create("test/foo/1/ear?name=test")), ConfigurationModuleType.EAR, new Jsr77Naming(), new MockConfigurationManager());
        ClassPathList classPathList = new ClassPathList();
        context.getCompleteManifestClassPath(start, start.getRelativeURI(), resolutionURI, classPathList, exclusions, factory, new ArrayList<DeploymentException>());
        assertEquals(4, classPathList.size());
        assertEquals("../../../lib1/lib1/lib1.jar", classPathList.get(0));
        assertEquals("../../../lib2/lib2.jar", classPathList.get(1));
        assertEquals("../../../lib2/lib2a.jar", classPathList.get(2));
        assertEquals("../../../lib3.jar", classPathList.get(3));
    }

    public void testManifestClassPathExcludeModules1() throws Exception {
        MockJarFile start = new MockJarFile(URI.create("ejb1.jar"), "lib1.jar");
        URI resolutionURI = URI.create(".");
        ModuleList exclusions = new ModuleList();
        exclusions.add("ejb1.jar");
        exclusions.add("ejb2.jar");
        Map<URI, String> data = new HashMap<URI, String>();
        data.put(URI.create("lib1.jar"), "ejb2.jar lib2.jar");
        data.put(URI.create("lib2.jar"), "ejb2.jar lib1.jar");
        data.put(URI.create("ejb2.jar"), "lib3.jar lib4.jar");

        DeploymentContext.JarFileFactory factory = new MockJarFileFactory(data);
        DeploymentContext context = new DeploymentContext(new File("."), null, new Environment(Artifact.create("test/foo/1/ear")), new AbstractName(URI.create("test/foo/1/ear?name=test")), ConfigurationModuleType.EAR, new Jsr77Naming(), new MockConfigurationManager());
        ClassPathList classPathList = new ClassPathList();
        context.getCompleteManifestClassPath(start, start.getRelativeURI(), resolutionURI, classPathList, exclusions, factory, new ArrayList<DeploymentException>());
        assertEquals(2, classPathList.size());
    }


    static class MockConfigurationManager implements ConfigurationManager {

        public boolean isInstalled(Artifact configurationId) {
            return false;
        }

        public boolean isLoaded(Artifact configurationId) {
            return false;
        }

        public boolean isRunning(Artifact configurationId) {
            return false;
        }

        public Artifact[] getInstalled(Artifact query) {
            return new Artifact[0];
        }

        public Artifact[] getLoaded(Artifact query) {
            return new Artifact[0];
        }

        public Artifact[] getRunning(Artifact query) {
            return new Artifact[0];
        }

        public List listConfigurations() {
            return null;
        }

        public List listStores() {
            return null;
        }

        public ConfigurationStore[] getStores() {
            return new ConfigurationStore[0];
        }

        public ConfigurationStore getStoreForConfiguration(Artifact configuration) {
            return null;
        }

        public List listConfigurations(AbstractName store) throws NoSuchStoreException {
            return null;
        }

        public boolean isConfiguration(Artifact artifact) {
            return false;
        }

        public Configuration getConfiguration(Artifact configurationId) {
            return null;
        }

        public LifecycleResults loadConfiguration(Artifact configurationId) throws NoSuchConfigException, LifecycleException {
            return null;
        }

        public LifecycleResults loadConfiguration(ConfigurationData configurationData) throws NoSuchConfigException, LifecycleException {
            return null;
        }

        public LifecycleResults loadConfiguration(Artifact configurationId, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
            return null;
        }

        public LifecycleResults loadConfiguration(ConfigurationData configurationData, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
            return null;
        }

        public LifecycleResults unloadConfiguration(Artifact configurationId) throws NoSuchConfigException {
            return null;
        }

        public LifecycleResults unloadConfiguration(Artifact configurationId, LifecycleMonitor monitor) throws NoSuchConfigException {
            return null;
        }

        public LifecycleResults startConfiguration(Artifact configurationId) throws NoSuchConfigException, LifecycleException {
            return null;
        }

        public LifecycleResults startConfiguration(Artifact configurationId, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
            return null;
        }

        public LifecycleResults stopConfiguration(Artifact configurationId) throws NoSuchConfigException {
            return null;
        }

        public LifecycleResults stopConfiguration(Artifact configurationId, LifecycleMonitor monitor) throws NoSuchConfigException {
            return null;
        }

        public LifecycleResults restartConfiguration(Artifact configurationId) throws NoSuchConfigException, LifecycleException {
            return null;
        }

        public LifecycleResults restartConfiguration(Artifact configurationId, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
            return null;
        }

        public LifecycleResults reloadConfiguration(Artifact configurationId) throws NoSuchConfigException, LifecycleException {
            return null;
        }

        public LifecycleResults reloadConfiguration(Artifact configurationId, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
            return null;
        }

        public LifecycleResults reloadConfiguration(Artifact configurationId, Version version) throws NoSuchConfigException, LifecycleException {
            return null;
        }

        public LifecycleResults reloadConfiguration(Artifact configurationId, Version version, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
            return null;
        }

        public LifecycleResults reloadConfiguration(ConfigurationData configurationData) throws NoSuchConfigException, LifecycleException {
            return null;
        }

        public LifecycleResults reloadConfiguration(ConfigurationData configurationData, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
            return null;
        }

        public void uninstallConfiguration(Artifact configurationId) throws IOException, NoSuchConfigException {
        }

        public ArtifactResolver getArtifactResolver() {
            return null;
        }

        public boolean isOnline() {
            return false;
        }

        public void setOnline(boolean online) {
        }

        public Collection<? extends Repository> getRepositories() {
            return null;
        }

        public LinkedHashSet<Artifact> sort(List<Artifact> ids, LifecycleMonitor monitor) throws InvalidConfigException, IOException, NoSuchConfigException, MissingDependencyException {
            return null;
        }
    }


}
