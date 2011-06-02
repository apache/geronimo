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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.jar.JarFile;

import junit.framework.TestCase;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.kernel.Jsr77Naming;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.mock.MockConfigurationManager;
import org.apache.geronimo.kernel.osgi.MockBundleContext;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.util.JarUtils;
import org.osgi.framework.BundleContext;

/**
 * @version $Rev$ $Date$
 */
public class DeploymentContextTest extends TestCase {
    private byte[] classBytes;

    private BundleContext bundleContext = new MockBundleContext(getClass().getClassLoader(), "", new HashMap<Artifact, ConfigurationData>(), null);

//    public void testAddClass() throws Exception {
//        File basedir = File.createTempFile("car", "tmp");
//        basedir.delete();
//        basedir.mkdirs();
//        try {
//            basedir.deleteOnExit();
//            Environment environment = new Environment();
//            Artifact configId = new Artifact("foo", "artifact", "1", "car");
//            environment.setConfigId(configId);
//            ArtifactManager artifactManager = new DefaultArtifactManager();
//            ArtifactResolver artifactResolver = new DefaultArtifactResolver(artifactManager, Collections.EMPTY_SET, null);
//            SimpleConfigurationManager configurationManager = new SimpleConfigurationManager(Collections.EMPTY_SET, artifactResolver, Collections.EMPTY_SET);
//            DeploymentContext context = new DeploymentContext(basedir, null, environment, null, ConfigurationModuleType.CAR, new Jsr77Naming(), configurationManager, Collections.EMPTY_SET);
//            Enhancer enhancer = new Enhancer();
//            enhancer.setInterfaces(new Class[]{DataSource.class});
//            enhancer.setCallbackType(MethodInterceptor.class);
//            enhancer.setStrategy(new DefaultGeneratorStrategy() {
//                public byte[] transform(byte[] b) {
//                    classBytes = b;
//                    return b;
//                }
//            });
//            enhancer.setClassLoader(new URLClassLoader(new URL[0], this.getClass().getClassLoader()));
//            Class type = enhancer.createClass();
//            URI location = new URI("cglib/");
//            context.addClass(location, type.getName(), classBytes);
//            ClassLoader cl = context.getClassLoader();
//            Class loadedType = cl.loadClass(type.getName());
//            assertTrue(DataSource.class.isAssignableFrom(loadedType));
//            assertTrue(type != loadedType);
//        } finally {
//            recursiveDelete(basedir);
//        }
//    }

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
            super(JarUtils.DUMMY_JAR_FILE);
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

    static class MockJarFileFactory implements ClassPathUtils.JarFileFactory {

        private final Map<URI, String> data;

        private File[] filesInLib2 = {new File("../../libfolder/a.jar") ,new File( "../../libfolder/b.jar"),new File( "../../libfolder/c.txt"), new File("../../libfolder/subfolder")};

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

        public boolean isDirectory(URI relativeURI) throws IOException {
            return relativeURI.equals(URI.create("libfolder")) || relativeURI.equals(URI.create("libfolder/"))
                    || relativeURI.equals(URI.create("libfolder/subfolder"))
                    || relativeURI.equals(URI.create("libfolder/subfolder/"));
        }

        public File[] listFiles(URI relativeURI) throws IOException {
            if (relativeURI.equals(URI.create("libfolder/")) || relativeURI.equals(URI.create("libfolder"))) {
                return filesInLib2;
            } else {
                return new File[0];
            }
        }
    }

    public void testManifestClassPath1() throws Exception {
        MockJarFile start = new MockJarFile(URI.create("ejb.jar"), "lib1.jar");
        URI resolutionURI = URI.create(".");
        LinkedHashSet<String> exclusions = new LinkedHashSet<String>();
        Map<URI, String> data = new HashMap<URI, String>();
        data.put(URI.create("lib1.jar"), "lib1.jar lib2.jar");

        ClassPathUtils.JarFileFactory factory = new MockJarFileFactory(data);
        DeploymentContext context = new DeploymentContext(new File("."), null, new Environment(Artifact.create("test/foo/1/ear")), new AbstractName(URI.create("test/foo/1/ear?name=test")), ConfigurationModuleType.EAR, new Jsr77Naming(), new MockConfigurationManager(), bundleContext);
        ArrayList<String> classPathList = new ArrayList<String>();
        ClassPathUtils.getCompleteManifestClassPath(start, start.getRelativeURI(), resolutionURI, classPathList, exclusions, factory);
        assertEquals(2, classPathList.size());
    }

    public void testManifestClassPath2() throws Exception {
        MockJarFile start = new MockJarFile(URI.create("ejb1/ejb1/ejb1.jar"), "../../lib1/lib1/lib1.jar");
        URI resolutionURI = URI.create(".");
        LinkedHashSet<String> exclusions = new LinkedHashSet<String>();
        Map<URI, String> data = new HashMap<URI, String>();
        data.put(URI.create("lib1/lib1/lib1.jar"), "../../lib2/lib2.jar");
        data.put(URI.create("lib2/lib2.jar"), "lib2a.jar");
        data.put(URI.create("lib2/lib2a.jar"), "../lib3.jar ../lib1/lib1/lib1.jar");

        ClassPathUtils.JarFileFactory factory = new MockJarFileFactory(data);
        DeploymentContext context = new DeploymentContext(new File("."), null, new Environment(Artifact.create("test/foo/1/ear")), new AbstractName(URI.create("test/foo/1/ear?name=test")), ConfigurationModuleType.EAR, new Jsr77Naming(), new MockConfigurationManager(), bundleContext);
        ArrayList<String> classPathList = new ArrayList<String>();
        ClassPathUtils.getCompleteManifestClassPath(start, start.getRelativeURI(), resolutionURI, classPathList, exclusions, factory);
        assertEquals(4, classPathList.size());
        assertEquals("lib1/lib1/lib1.jar", classPathList.get(0));
        assertEquals("lib2/lib2.jar", classPathList.get(1));
        assertEquals("lib2/lib2a.jar", classPathList.get(2));
        assertEquals("lib3.jar", classPathList.get(3));
    }

    public void testMainfestClassPath3() throws Exception {
        MockJarFile start = new MockJarFile(URI.create("ejb1/ejb1/ejb1.jar"), "../../lib1/lib1/lib1.jar ../../libfolder");
        URI resolutionURI = URI.create(".");
        LinkedHashSet<String> exclusions = new LinkedHashSet<String>();
        Map<URI, String> data = new HashMap<URI, String>();
        data.put(URI.create("lib1/lib1/lib1.jar"), "../../lib2/lib2.jar");
        data.put(URI.create("lib2/lib2.jar"), "lib2a.jar");
        data.put(URI.create("lib2/lib2a.jar"), "../lib3.jar ../lib1/lib1/lib1.jar");
        data.put(URI.create("libfolder/a.jar"), "");
        data.put(URI.create("libfolder/b.jar"), "");

        ClassPathUtils.JarFileFactory factory = new MockJarFileFactory(data);
        DeploymentContext context = new DeploymentContext(new File("."), null, new Environment(Artifact.create("test/foo/1/ear")), new AbstractName(URI.create("test/foo/1/ear?name=test")), ConfigurationModuleType.EAR, new Jsr77Naming(), new MockConfigurationManager(), bundleContext);
        ArrayList<String> classPathList = new ArrayList<String>();
        ClassPathUtils.getCompleteManifestClassPath(start, start.getRelativeURI(), resolutionURI, classPathList, exclusions, factory);
        assertEquals(7, classPathList.size());
        assertEquals("lib1/lib1/lib1.jar", classPathList.get(0));
        assertEquals("lib2/lib2.jar", classPathList.get(1));
        assertEquals("lib2/lib2a.jar", classPathList.get(2));
        assertEquals("lib3.jar", classPathList.get(3));
        assertEquals("libfolder/", classPathList.get(4));
        assertEquals("libfolder/a.jar", classPathList.get(5));
        assertEquals("libfolder/b.jar", classPathList.get(6));

    }

    public void testManifestClassPathWar1() throws Exception {
        MockJarFile start = new MockJarFile(URI.create("war1.war"), "lib1.jar");
        URI resolutionURI = URI.create("../");
        LinkedHashSet<String> exclusions = new LinkedHashSet<String>();
        Map<URI, String> data = new HashMap<URI, String>();
        data.put(URI.create("lib1.jar"), "lib1.jar lib2.jar");

        ClassPathUtils.JarFileFactory factory = new MockJarFileFactory(data);
        DeploymentContext context = new DeploymentContext(new File("."), null, new Environment(Artifact.create("test/foo/1/ear")), new AbstractName(URI.create("test/foo/1/ear?name=test")), ConfigurationModuleType.EAR, new Jsr77Naming(), new MockConfigurationManager(), bundleContext);
        ArrayList<String> classPathList = new ArrayList<String>();
        ClassPathUtils.getCompleteManifestClassPath(start, start.getRelativeURI(), resolutionURI, classPathList, exclusions, factory);
        assertEquals(2, classPathList.size());
        assertEquals("../lib1.jar", classPathList.get(0));
        assertEquals("../lib2.jar", classPathList.get(1));
        //should contain ../lib1.jar ../lib2.jar
    }

    public void testManifestClassPathWar2() throws Exception {
        MockJarFile start = new MockJarFile(URI.create("war1/war1/war1.war"), "../../lib1/lib1/lib1.jar");
        URI resolutionURI = URI.create("../../../");
        LinkedHashSet<String> exclusions = new LinkedHashSet<String>();
        Map<URI, String> data = new HashMap<URI, String>();
        data.put(URI.create("lib1/lib1/lib1.jar"), "../../lib2/lib2/lib2.jar");
        data.put(URI.create("lib2/lib2/lib2.jar"), "../lib2a/lib2a.jar");
        data.put(URI.create("lib2/lib2a/lib2a.jar"), "../../lib3/lib3/lib3.jar ../../lib1/lib1/lib1.jar");

        ClassPathUtils.JarFileFactory factory = new MockJarFileFactory(data);
        DeploymentContext context = new DeploymentContext(new File("."), null, new Environment(Artifact.create("test/foo/1/ear")), new AbstractName(URI.create("test/foo/1/ear?name=test")), ConfigurationModuleType.EAR, new Jsr77Naming(), new MockConfigurationManager(), bundleContext);
        ArrayList<String> classPathList = new ArrayList<String>();
        ClassPathUtils.getCompleteManifestClassPath(start, start.getRelativeURI(), resolutionURI, classPathList, exclusions, factory);
        assertEquals(4, classPathList.size());
        assertEquals("../../../lib1/lib1/lib1.jar", classPathList.get(0));
        assertEquals("../../../lib2/lib2/lib2.jar", classPathList.get(1));
        assertEquals("../../../lib2/lib2a/lib2a.jar", classPathList.get(2));
        assertEquals("../../../lib3/lib3/lib3.jar", classPathList.get(3));
    }

    public void testManifestClassPathWar3() throws Exception {
        MockJarFile start = new MockJarFile(URI.create("war1/war1/war1.war"), "../../lib1/lib1/lib1.jar");
        URI resolutionURI = URI.create("../../../");
        LinkedHashSet<String> exclusions = new LinkedHashSet<String>();
        Map<URI, String> data = new HashMap<URI, String>();
        data.put(URI.create("lib1/lib1/lib1.jar"), "../../lib2/lib2.jar");
        data.put(URI.create("lib2/lib2.jar"), "lib2a.jar");
        data.put(URI.create("lib2/lib2a.jar"), "../lib3.jar ../lib1/lib1/lib1.jar");

        ClassPathUtils.JarFileFactory factory = new MockJarFileFactory(data);
        DeploymentContext context = new DeploymentContext(new File("."), null, new Environment(Artifact.create("test/foo/1/ear")), new AbstractName(URI.create("test/foo/1/ear?name=test")), ConfigurationModuleType.EAR, new Jsr77Naming(), new MockConfigurationManager(), bundleContext);
        ArrayList<String> classPathList = new ArrayList<String>();
        ClassPathUtils.getCompleteManifestClassPath(start, start.getRelativeURI(), resolutionURI, classPathList, exclusions, factory);
        assertEquals(4, classPathList.size());
        assertEquals("../../../lib1/lib1/lib1.jar", classPathList.get(0));
        assertEquals("../../../lib2/lib2.jar", classPathList.get(1));
        assertEquals("../../../lib2/lib2a.jar", classPathList.get(2));
        assertEquals("../../../lib3.jar", classPathList.get(3));
    }

    public void testManifestClassPathExcludeModules1() throws Exception {
        MockJarFile start = new MockJarFile(URI.create("ejb1.jar"), "lib1.jar");
        URI resolutionURI = URI.create(".");
        LinkedHashSet<String> exclusions = new LinkedHashSet<String>();
        exclusions.add("ejb1.jar");
        exclusions.add("ejb2.jar");
        Map<URI, String> data = new HashMap<URI, String>();
        data.put(URI.create("lib1.jar"), "ejb2.jar lib2.jar");
        data.put(URI.create("lib2.jar"), "ejb2.jar lib1.jar");
        data.put(URI.create("ejb2.jar"), "lib3.jar lib4.jar");

        ClassPathUtils.JarFileFactory factory = new MockJarFileFactory(data);
        DeploymentContext context = new DeploymentContext(new File("."), null, new Environment(Artifact.create("test/foo/1/ear")), new AbstractName(URI.create("test/foo/1/ear?name=test")), ConfigurationModuleType.EAR, new Jsr77Naming(), new MockConfigurationManager(), bundleContext);
        ArrayList<String> classPathList = new ArrayList<String>();
        ClassPathUtils.getCompleteManifestClassPath(start, start.getRelativeURI(), resolutionURI, classPathList, exclusions, factory);
        assertEquals(2, classPathList.size());
    }

}
