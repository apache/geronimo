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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gjndi.binding.ResourceBinding;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.osgi.MockBundleContext;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.KernelConfigurationManager;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactManager;
import org.apache.geronimo.kernel.repository.DefaultArtifactManager;
import org.apache.geronimo.kernel.repository.DefaultArtifactResolver;
import org.apache.geronimo.naming.java.RootContext;
import org.apache.xbean.naming.context.ImmutableContext;

/**
 * @version $Rev$ $Date$
 */
public class KernelContextGBeanTest extends AbstractContextTest {
    private MockBundleContext bundleContext = new MockBundleContext(getClass().getClassLoader(), "", new HashMap<Artifact, ConfigurationData>(), null);
    private Kernel kernel;
    private ConfigurationManager configurationManager;
    private ConfigurationData configurationData;
    private GBeanInfo immutableContextGBeanInfo;

    public void xtest() throws Exception {
        HashMap javaCompOnlyBindings = new HashMap();
        javaCompOnlyBindings.put("java:comp/string", "foo");
        javaCompOnlyBindings.put("java:comp/nested/context/string", "bar");
        javaCompOnlyBindings.put("java:comp/a/b/c/d/e/string", "beer");
        javaCompOnlyBindings.put("java:comp/a/b/c/d/e/one", new Integer(1));
        javaCompOnlyBindings.put("java:comp/a/b/c/d/e/two", new Integer(2));
        javaCompOnlyBindings.put("java:comp/a/b/c/d/e/three", new Integer(3));

        Map globalBindings = new HashMap(javaCompOnlyBindings);
        globalBindings.put("test/env/foo", new Integer(42));
        globalBindings.put("test/baz", "caz");

        Map javaCompBindings = getNestedBindings(globalBindings, "java:");
        ImmutableContext javaCompContext = new ImmutableContext(javaCompBindings);
        RootContext.setComponentContext(javaCompContext);

        GBeanData javaComp = configurationData.addGBean("JavaComp", JavaCompContextGBean.class);
        AbstractName javaCompName = javaComp.getAbstractName();

        GBeanData test = configurationData.addGBean("Test", immutableContextGBeanInfo);
        AbstractName testName = test.getAbstractName();
        test.setAttribute("nameInNamespace", "test");
        Map testBindings = getNestedBindings(globalBindings, "test/");
        test.setAttribute("bindings", testBindings);

        configurationManager.loadConfiguration(configurationData);
        configurationManager.startConfiguration(configurationData.getId());

        InitialContext ctx = new InitialContext();
        assertEq(globalBindings, ctx);

        //
        // stop test context
        //
        kernel.stopGBean(testName);


        assertEq(javaCompOnlyBindings, ctx);

        //
        // stop java context
        //
        kernel.stopGBean(javaCompName);

        assertEq(Collections.EMPTY_MAP, ctx);

        //
        // restart java context
        //
        kernel.startGBean(javaCompName);

        assertEq(javaCompOnlyBindings, ctx);

        //
        // restart test context
        //
        kernel.startGBean(testName);

        assertEq(globalBindings, ctx);
    }

    public void testGBeanFormatBinding() throws Exception {
        setUpJcaContext();
        configurationManager.loadConfiguration(configurationData);
        configurationManager.startConfiguration(configurationData.getId());

        InitialContext ctx = new InitialContext();
        Context fooCtx = ctx.createSubcontext("jca:foo");
        fooCtx.createSubcontext("bar");
        ctx.bind("jca:foo/bar/baz", 1);
        assertEquals(ctx.lookup("jca:foo/bar/baz"), 1);
        ctx.rebind("jca:foo/bar/baz", 2);
        assertEquals(ctx.lookup("jca:foo/bar/baz"), 2);

    }

    /*Disable this one, have to update MockServiceRegistration, MockServiceReference in kernel module
     * public void testGBeanFormatReBinding() throws Exception {
        setUpJcaContext();
        configurationData.addGBean("resourceSource", new AnnotationGBeanInfoBuilder(MockResourceSource.class).buildGBeanInfo());

        configurationManager.loadConfiguration(configurationData);
        configurationManager.startConfiguration(configurationData.getId());

        InitialContext ctx = new InitialContext();
        assertTrue(ctx.lookup("jca:/test/test/GBean/resourceSource") instanceof DataSource);
        Context context = (Context)ctx.lookup("jca:/test/test");
        assertTrue(context.lookup("GBean/resourceSource") instanceof DataSource);

        ctx.rebind("jca:/test/test/GBean/resourceSource", 2);
        assertEquals(2, ctx.lookup("jca:/test/test/GBean/resourceSource"));

    }*/

    private void setUpJcaContext() {
        GBeanData jca = configurationData.addGBean("jca", ResourceBinding.class);
        jca.setAttribute("format","{groupId}/{artifactId}/{j2eeType}/{name}");
        jca.setAttribute("nameInNamespace", "jca:");
        jca.setAttribute("abstractNameQuery", new AbstractNameQuery("org.apache.geronimo.naming.ResourceSource"));
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

        kernel = KernelFactory.newInstance(bundleContext).createKernel("test");
        kernel.boot(bundleContext);

        ArtifactManager artifactManager = new DefaultArtifactManager();

        DefaultArtifactResolver artifactResolver = new DefaultArtifactResolver();
        artifactResolver.setArtifactManager(artifactManager);

        KernelConfigurationManager configurationManager = new KernelConfigurationManager();
        configurationManager.setArtifactManager(artifactManager);
        configurationManager.setArtifactResolver(artifactResolver);
        configurationManager.setKernel(kernel);
        configurationManager.activate(bundleContext);
        this.configurationManager = configurationManager;

        artifactResolver.setConfigurationManager(configurationManager);

        configurationData = new ConfigurationData(new Artifact("test", "test", "", "car"), kernel.getNaming());
        configurationData.setBundleContext(bundleContext);
        configurationData.addGBean("GlobalContext", GlobalContextGBean.class);

        GBeanInfoBuilder builder = new GBeanInfoBuilder(ImmutableContext.class);
        builder.setConstructor(new String[]{"nameInNamespace", "bindings", "cacheReferences"});
        builder.addAttribute("nameInNamespace", String.class, true);
        builder.addAttribute("bindings", Map.class, true);
        builder.addAttribute("cacheReferences", boolean.class, true);
        immutableContextGBeanInfo = builder.getBeanInfo();

    }

    protected void tearDown() throws Exception {
        kernel.shutdown();
        super.tearDown();
    }
}
