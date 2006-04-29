/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.deployment.service;

import junit.framework.TestCase;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.FooBarBean;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.deployment.xbeans.ConfigurationDocument;
import org.apache.geronimo.deployment.xbeans.ConfigurationType;
import org.apache.geronimo.deployment.xbeans.GbeanType;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.ReferenceCollection;
import org.apache.geronimo.gbean.ReferenceCollectionListener;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ListableRepository;
import org.apache.geronimo.kernel.repository.ArtifactManager;
import org.apache.geronimo.kernel.repository.DefaultArtifactManager;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.DefaultArtifactResolver;
import org.apache.geronimo.kernel.Jsr77Naming;
import org.apache.geronimo.kernel.Naming;

import javax.management.ObjectName;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Collections;
import java.util.jar.JarFile;

/**
 * @version $Rev$ $Date$
 */
public class ServiceConfigBuilderTest extends TestCase {

    private Environment parentEnvironment = new Environment();

    public void testNonService() throws Exception {
        URL url = getClass().getResource("/empty.jar");
        File file = new File(url.getPath());
        JarFile jar = new JarFile(file);
        assertTrue(file.exists());
        ServiceConfigBuilder builder = new ServiceConfigBuilder(parentEnvironment, null, null, null, new Jsr77Naming());
        assertNull(builder.getDeploymentPlan(null, jar, new ModuleIDBuilder()));
        jar.close();
    }

    public void testJavaBeanXmlAttribute() throws Exception {
        ReferenceCollection referenceCollection = new MockReferenceCollection();
        JavaBeanXmlAttributeBuilder javaBeanXmlAttributeBuilder = new JavaBeanXmlAttributeBuilder();
        //this is kind of cheating, we rely on the builder to iterate through existing members of the collection.
        referenceCollection.add(javaBeanXmlAttributeBuilder);
        Naming naming = new Jsr77Naming();
        new ServiceConfigBuilder(parentEnvironment, null, referenceCollection, null, naming);
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        final URL plan1 = cl.getResource("services/plan1.xml");
        ConfigurationDocument doc = ConfigurationDocument.Factory.parse(plan1);
        ConfigurationType plan = doc.getConfiguration();
        File outFile = File.createTempFile("foo", "bar");
        outFile.delete();
        if (!outFile.mkdirs()) {
            fail("could not create temp dir");
        }
        try {

            Environment environment = EnvironmentBuilder.buildEnvironment(plan.getEnvironment());
            MockRepository mockRepository = new MockRepository();
            ArtifactManager artifactManager = new DefaultArtifactManager();
            ArtifactResolver artifactResolver = new DefaultArtifactResolver(artifactManager, Collections.singleton(mockRepository), null);
            DeploymentContext context = new DeploymentContext(outFile, null, environment, ConfigurationModuleType.CAR, naming, Collections.singleton(mockRepository), Collections.EMPTY_SET, artifactResolver);
            AbstractName j2eeContext = naming.createRootName(environment.getConfigId(), environment.getConfigId().toString(), "Configuration");

            GbeanType[] gbeans = plan.getGbeanArray();
            ServiceConfigBuilder.addGBeans(gbeans, cl, j2eeContext, context);
            Set gbeanNames = context.getGBeanNames();
            assertEquals(1, gbeanNames.size());
            AbstractName beanName = (AbstractName) gbeanNames.iterator().next();
            GBeanData data = context.getGBeanInstance(beanName);
            FooBarBean fooBarBean = (FooBarBean) data.getAttribute("fooBarBean");
            assertNotNull(fooBarBean);
            assertEquals("foo", fooBarBean.getFoo());
            assertEquals(10, fooBarBean.getBar());
            FooBarBean inner = fooBarBean.getBean();
            assertNotNull(inner);
            assertEquals("foo2", inner.getFoo());
            assertEquals(100, inner.getBar());
            assertNull(inner.getBean());
        } finally {
            recursiveDelete(outFile);
        }
    }

    private void recursiveDelete(File file) {

        File[] list = file.listFiles();
        if (list != null) {
            for (int i = 0; i < list.length; i++) {
                File file1 = list[i];
                if (file1.isDirectory()) {
                    recursiveDelete(file1);
                }
            }
        }
        file.delete();
    }

    private static class MockRepository implements ListableRepository {
        public boolean contains(Artifact artifact) {
            return true;
        }

        public File getLocation(Artifact artifact) {
            return new File(".");
        }

        public LinkedHashSet getDependencies(Artifact artifact) {
            return new LinkedHashSet();
        }

        public SortedSet list() {
            return new TreeSet();
        }

        public SortedSet list(Artifact query) {
            System.out.println("LOOKING FOR "+query);
            return new TreeSet();
        }
    }
    private static class MockReferenceCollection extends ArrayList implements ReferenceCollection {

        public void addReferenceCollectionListener(ReferenceCollectionListener listener) {

        }

        public void removeReferenceCollectionListener(ReferenceCollectionListener listener) {

        }

        public ObjectName[] getMemberObjectNames() { return new ObjectName[0];}
    }
}
