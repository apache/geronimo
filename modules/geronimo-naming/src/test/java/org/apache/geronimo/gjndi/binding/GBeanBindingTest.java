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
package org.apache.geronimo.gjndi.binding;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gjndi.AbstractContextTest;
import org.apache.geronimo.gjndi.GlobalContextGBean;
import org.apache.geronimo.gjndi.WritableContextGBean;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.EditableConfigurationManager;
import org.apache.geronimo.kernel.config.EditableKernelConfigurationManager;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.DefaultArtifactManager;
import org.apache.geronimo.kernel.repository.DefaultArtifactResolver;
import org.apache.xbean.naming.global.GlobalContextManager;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class GBeanBindingTest extends AbstractContextTest {
    private Kernel kernel;

    private Hashtable contextEnv;
    private Map globalBindings;
    private AbstractName ds1Name;
    private AbstractName ds2Name;

    public void testBasics() throws Exception {
        InitialContext ctx = new InitialContext(contextEnv);
        assertEq(globalBindings, ctx);

        //
        // stop ds1
        //
        kernel.stopGBean(ds1Name);
        DataSource ds2 = (DataSource) kernel.getGBean(ds2Name);
        globalBindings.put("writable/ds", ds2);
        globalBindings.remove("writable/ds1");
        assertEq(globalBindings, ctx);

        //
        // restart ds1
        //
        kernel.startGBean(ds1Name);
        DataSource ds1 = (DataSource) kernel.getGBean(ds1Name);
        globalBindings.put("writable/ds1", ds1);
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

        EditableConfigurationManager configurationManager = ConfigurationUtil.getEditableConfigurationManager(kernel);

        ConfigurationData configurationData = new ConfigurationData(new Artifact("test", "test", "", "car"), kernel.getNaming());
        configurationData.addGBean("GlobalContext", GlobalContextGBean.GBEAN_INFO);

        contextEnv = new Hashtable();
        contextEnv.put(Context.INITIAL_CONTEXT_FACTORY, GlobalContextManager.class.getName());

        // dataSources
        GBeanData ds1GBean = configurationData.addGBean("ds1", MockDataSource.GBEAN_INFO);
        ds1Name = ds1GBean.getAbstractName();

        GBeanData ds2GBean = configurationData.addGBean("ds2", MockDataSource.GBEAN_INFO);
        ds2Name = ds2GBean.getAbstractName();

        // bindings
        GBeanData writableGBean = configurationData.addGBean("writable", WritableContextGBean.GBEAN_INFO);
        AbstractName writableName = writableGBean.getAbstractName();
        writableGBean.setAttribute("nameInNamespace", "writable");

        GBeanData dsBinding = configurationData.addGBean("dsBinding", GBeanBinding.GBEAN_INFO);
        dsBinding.setReferencePattern("Context", writableName);
        dsBinding.setAttribute("name", "ds");
        dsBinding.setAttribute("abstractNameQuery", new AbstractNameQuery(null, Collections.EMPTY_MAP, DataSource.class.getName()));

        GBeanData ds1Binding = configurationData.addGBean("ds1Binding", GBeanBinding.GBEAN_INFO);
        ds1Binding.setReferencePattern("Context", writableName);
        ds1Binding.setAttribute("name", "ds1");
        ds1Binding.setAttribute("abstractNameQuery", new AbstractNameQuery(null,
                Collections.singletonMap("name", "ds1"),
                DataSource.class.getName()));

        GBeanData ds2Binding = configurationData.addGBean("ds2Binding", GBeanBinding.GBEAN_INFO);
        ds2Binding.setReferencePattern("Context", writableName);
        ds2Binding.setAttribute("name", "ds2");
        ds2Binding.setAttribute("abstractNameQuery", new AbstractNameQuery(null,
                Collections.singletonMap("name", "ds2"),
                DataSource.class.getName()));

        configurationManager.loadConfiguration(configurationData);
        configurationManager.startConfiguration(configurationData.getId());

        DataSource ds1 = (DataSource) kernel.getGBean(ds1Name);
        DataSource ds2 = (DataSource) kernel.getGBean(ds2Name);

        // global bindings
        globalBindings = new HashMap();
        globalBindings.put("writable/ds", ds1);
        globalBindings.put("writable/ds1", ds1);
        globalBindings.put("writable/ds2", ds2);
    }

    protected void tearDown() throws Exception {
        kernel.shutdown();
        super.tearDown();
    }
}
