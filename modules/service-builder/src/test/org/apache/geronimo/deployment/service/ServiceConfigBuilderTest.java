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
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.deployment.FooBarBean;
import org.apache.geronimo.deployment.xbeans.ConfigurationDocument;
import org.apache.geronimo.deployment.xbeans.ConfigurationType;
import org.apache.geronimo.deployment.xbeans.GbeanType;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.ReferenceCollection;
import org.apache.geronimo.gbean.ReferenceCollectionListener;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContextImpl;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;

import javax.management.ObjectName;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;

/**
 * @version $Rev$ $Date$
 */
public class ServiceConfigBuilderTest extends TestCase {

    private Environment parentEnvironment = new Environment();

    public void testJavaBeanXmlAttribute() throws Exception {
        ReferenceCollection referenceCollection = new MockReferenceCollection();
        JavaBeanXmlAttributeBuilder javaBeanXmlAttributeBuilder = new JavaBeanXmlAttributeBuilder();
        //this is kind of cheating, we rely on the builder to iterate through existing members of the collection.
        referenceCollection.add(javaBeanXmlAttributeBuilder);
        new ServiceConfigBuilder(parentEnvironment, null, referenceCollection, null, null);
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
            DeploymentContext context = new DeploymentContext(outFile, environment, ConfigurationModuleType.SERVICE, null);
            J2eeContext j2eeContext = new J2eeContextImpl("domain", "server", "null", "test", "configtest", "foo", NameFactory.J2EE_MODULE);
//            ServiceConfigBuilder.addDependencies(context, plan.getDependencyArray(), new Repository() {
//
//                public boolean hasURI(URI uri) {
//                    return true;
//                }
//
//                public URL getURL(URI uri) throws MalformedURLException {
//                    return plan1;
//                }
//            });
            GbeanType[] gbeans = plan.getGbeanArray();
            ServiceConfigBuilder.addGBeans(gbeans, cl, j2eeContext, context);
            Set beanDatas = context.listGBeans(new ObjectName("*:*"));
            assertEquals(1, beanDatas.size());
            ObjectName beanName = (ObjectName) beanDatas.iterator().next();
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

    private static class MockReferenceCollection extends ArrayList implements ReferenceCollection {

        public void addReferenceCollectionListener(ReferenceCollectionListener listener) {

        }

        public void removeReferenceCollectionListener(ReferenceCollectionListener listener) {

        }

        public ObjectName[] getMemberObjectNames() { return new ObjectName[0];}
    }
}
