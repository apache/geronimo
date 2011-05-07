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
package org.apache.geronimo.gjndi;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NotContextException;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.basic.BasicKernel;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.osgi.MockBundleContext;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.DefaultArtifactManager;
import org.apache.geronimo.kernel.repository.DefaultArtifactResolver;
import org.apache.geronimo.naming.java.RootContext;
import org.apache.geronimo.naming.java.javaURLContextFactory;
import org.apache.xbean.naming.context.ImmutableContext;

/**
 * @version $Rev$ $Date$
 */
public class JavaCompGBeanTest extends AbstractContextTest {
    private MockBundleContext bundleContext = new MockBundleContext(getClass().getClassLoader(), "", new HashMap<Artifact, ConfigurationData>(), null);
    private BasicKernel kernel;

    public void testLookupEnv() throws Exception {
        Map javaCompBindings = new HashMap();
        javaCompBindings.put("foo", "bar");

        // a regular context doesn't contain env
        Thread.currentThread().setContextClassLoader(javaURLContextFactory.class.getClassLoader());
        RootContext.setComponentContext(new ImmutableContext(javaCompBindings));
        try {
            new InitialContext().lookup("java:comp/env");
            fail("Expected NameNotFoundException");
        } catch (NotContextException expected) {
            // expected
        } catch (NameNotFoundException expected) {
            // expected
        }

        // ENC adds env if not present
        javaCompBindings.put("comp/env/foo", "bar");
        RootContext.setComponentContext(new ImmutableContext(javaCompBindings));
        new InitialContext().lookup("java:comp/env");
    }

    protected Map getNestedBindings(Map globalBindings, String nestedPath) {
        HashMap nestedBindings = new HashMap();
        for (Iterator iterator = globalBindings.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String globalName = (String) entry.getKey();
            Object value = entry.getValue();

            if (globalName.startsWith(nestedPath)) {
                String nestedName = globalName.substring(nestedPath.length());
                nestedBindings.put(nestedName, value);
            }
        }
        return nestedBindings;
    }

    protected void setUp() throws Exception {
        super.setUp();

        kernel = new BasicKernel();
//        kernel = KernelFactory.newInstance(bundleContext).createKernel("test");
//        kernel.boot();

//        ConfigurationData bootstrap = new ConfigurationData(new Artifact("bootstrap", "bootstrap", "", "car"), kernel.getNaming());
//
//        GBeanData artifactManagerData = bootstrap.addGBean("ArtifactManager", DefaultArtifactManager.GBEAN_INFO);
//
//        GBeanData artifactResolverData = bootstrap.addGBean("ArtifactResolver", DefaultArtifactResolver.class);
//        artifactResolverData.setReferencePattern("ArtifactManager", artifactManagerData.getAbstractName());
//
//        GBeanData configurationManagerData = bootstrap.addGBean("ConfigurationManager", KernelConfigurationManager.class);
//        configurationManagerData.setReferencePattern("ArtifactManager", artifactManagerData.getAbstractName());
//        configurationManagerData.setReferencePattern("ArtifactResolver", artifactResolverData.getAbstractName());
//
//        ConfigurationUtil.loadBootstrapConfiguration(kernel, bootstrap, bundleContext);
//
//        ConfigurationManager configurationManager = ConfigurationUtil.getConfigurationManager(kernel);

        ConfigurationData configurationData = new ConfigurationData(new Artifact("test", "test", "", "car"), kernel.getNaming());
        configurationData.setBundle(bundleContext.getBundle());
        configurationData.addGBean("GlobalContext", GlobalContextGBean.class);
        configurationData.addGBean("JavaComp", JavaCompContextGBean.class);

        Configuration configuration = new Configuration(configurationData, null);
        ConfigurationUtil.loadConfigurationGBeans(configuration, kernel);
        ConfigurationUtil.startConfigurationGBeans(configuration, kernel);

    }

    protected void tearDown() throws Exception {
        kernel.shutdown();
        super.tearDown();
    }
}
