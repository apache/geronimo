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
package org.apache.geronimo.gbean.runtime;

import junit.framework.TestCase;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.ReferencePatterns;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.MockGBean;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.osgi.MockBundleContext;
import org.apache.geronimo.kernel.repository.Artifact;

/**
 * @version $Rev$ $Date$
 */
public class GBeanDependencyTest extends TestCase {
    private MockBundleContext bundleContext = new MockBundleContext(getClass().getClassLoader(), null, null, null);
    private Kernel kernel;

    public void testGBeanDependency() throws Exception {
        AbstractName parentName = kernel.getNaming().createRootName(new Artifact("test", "foo", "1", "car"), "parent", "parent");
        GBeanData gbeanDataParent = new GBeanData(parentName, MockGBean.getGBeanInfo());
        GBeanData gbeanDataChild = new GBeanData(kernel.getNaming().createChildName(parentName, "child", "child"), MockGBean.getGBeanInfo());
        gbeanDataChild.addDependency(new ReferencePatterns(parentName));
        kernel.loadGBean(gbeanDataChild, bundleContext);
        kernel.startGBean(gbeanDataChild.getAbstractName());
        assertEquals(State.STARTING_INDEX, kernel.getGBeanState(gbeanDataChild.getAbstractName()));
        kernel.loadGBean(gbeanDataParent, bundleContext);
        assertEquals(State.STARTING_INDEX, kernel.getGBeanState(gbeanDataChild.getAbstractName()));
        kernel.startGBean(parentName);
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(gbeanDataChild.getAbstractName()));
    }

    protected void setUp() throws Exception {
        super.setUp();
        kernel = KernelFactory.newInstance(bundleContext).createKernel("test");
        kernel.boot(bundleContext);
    }

    protected void tearDown() throws Exception {
        kernel.shutdown();
        super.tearDown();
    }
}
