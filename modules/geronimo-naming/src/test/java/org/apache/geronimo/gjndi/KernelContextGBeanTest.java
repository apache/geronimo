/**
 *
 * Copyright 2006 The Apache Software Foundation
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
package org.apache.geronimo.gjndi;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.EditableConfigurationManager;
import org.apache.geronimo.kernel.config.EditableKernelConfigurationManager;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.DefaultArtifactManager;
import org.apache.geronimo.kernel.repository.DefaultArtifactResolver;
import org.apache.geronimo.naming.java.RootContext;
import org.apache.xbean.naming.context.ImmutableContext;
import org.apache.xbean.naming.global.GlobalContextManager;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Iterator;
import java.util.Collections;

/**
 * @version $Rev$ $Date$
 */
public class KernelContextGBeanTest extends AbstractContextTest {
    private Kernel kernel;
    private EditableConfigurationManager configurationManager;
    private ConfigurationData configurationData;
    private GBeanInfo immutableContextGBeanInfo;
    private Hashtable contextEnv;

    public void test() throws Exception {
        Map globalBindings = new HashMap();
        globalBindings.put("java:comp/string", "foo");
        globalBindings.put("java:comp/nested/context/string", "bar");
        globalBindings.put("java:comp/a/b/c/d/e/string", "beer");
        globalBindings.put("java:comp/a/b/c/d/e/one", new Integer(1));
        globalBindings.put("java:comp/a/b/c/d/e/two", new Integer(2));
        globalBindings.put("java:comp/a/b/c/d/e/three", new Integer(3));
        globalBindings.put("test/env/foo", new Integer(42));
        globalBindings.put("test/baz", "caz");

        Map javaCompBindings = getNestedBindings(globalBindings, "java:comp/");
        ImmutableContext javaCompContext = new ImmutableContext(javaCompBindings);
        RootContext.setComponentContext(javaCompContext);

        GBeanData javaComp = configurationData.addGBean("JavaComp", JavaCompContextGBean.GBEAN_INFO);
        AbstractName javaCompName = javaComp.getAbstractName();

        GBeanData test = configurationData.addGBean("Test", immutableContextGBeanInfo);
        AbstractName testName = test.getAbstractName();
        test.setAttribute("nameInNamespace", "test");
        Map testBindings = getNestedBindings(globalBindings, "test/");
        test.setAttribute("bindings", testBindings);

        configurationManager.loadConfiguration(configurationData);
        configurationManager.startConfiguration(configurationData.getId());

        InitialContext ctx = new InitialContext(contextEnv);
        assertEq(globalBindings, ctx);

        //
        // stop test context
        //
        kernel.stopGBean(testName);

        HashMap javaCompOnlyBindings = new HashMap(globalBindings);
        javaCompOnlyBindings.remove("test/env/foo");
        javaCompOnlyBindings.remove("test/baz");
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

        kernel = KernelFactory.newInstance().createKernel("test");
        kernel.boot();

        ConfigurationData bootstrap = new ConfigurationData(new Artifact("bootstrap", "bootstrap", "", "car"), kernel.getNaming());

        GBeanData artifactManagerData = bootstrap.addGBean("ArtifactManager", DefaultArtifactManager.GBEAN_INFO);

        GBeanData artifactResolverData = bootstrap.addGBean("ArtifactResolver", DefaultArtifactResolver.GBEAN_INFO);
        artifactResolverData.setReferencePattern("ArtifactManager", artifactManagerData.getAbstractName());

        GBeanData configurationManagerData = bootstrap.addGBean("ConfigurationManager", EditableKernelConfigurationManager.GBEAN_INFO);
        configurationManagerData.setReferencePattern("ArtifactManager", artifactManagerData.getAbstractName());
        configurationManagerData.setReferencePattern("ArtifactResolver", artifactResolverData.getAbstractName());

        ConfigurationUtil.loadBootstrapConfiguration(kernel, bootstrap, getClass().getClassLoader());

        configurationManager = ConfigurationUtil.getEditableConfigurationManager(kernel);

        configurationData = new ConfigurationData(new Artifact("test", "test", "", "car"), kernel.getNaming());
        configurationData.addGBean("GlobalContext", GlobalContextGBean.GBEAN_INFO);

        GBeanInfoBuilder builder = new GBeanInfoBuilder(ImmutableContext.class);
        builder.setConstructor(new String[] {"nameInNamespace", "bindings", "cacheReferences"});
        builder.addAttribute("nameInNamespace", String.class, true);
        builder.addAttribute("bindings", Map.class, true);
        builder.addAttribute("cacheReferences", boolean.class, true);
        immutableContextGBeanInfo = builder.getBeanInfo();

        contextEnv = new Hashtable();
        contextEnv.put(Context.INITIAL_CONTEXT_FACTORY, GlobalContextManager.class.getName());
    }

    protected void tearDown() throws Exception {
        kernel.shutdown();
        super.tearDown();
    }
}
