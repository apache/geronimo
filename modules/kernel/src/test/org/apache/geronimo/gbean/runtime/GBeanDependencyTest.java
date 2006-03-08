/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.gbean.runtime;

import junit.framework.TestCase;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.ReferencePatterns;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.MockGBean;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.repository.Artifact;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.HashMap;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class GBeanDependencyTest extends TestCase {

    private Kernel kernel;

    public void testGBeanDependency() throws Exception {
        AbstractName parentName = buildAbstractName("parent", MockGBean.getGBeanInfo());
        GBeanData gbeanDataParent = new GBeanData(parentName, MockGBean.getGBeanInfo());
        GBeanData gbeanDataChild = new GBeanData(buildAbstractName("child", MockGBean.getGBeanInfo()), MockGBean.getGBeanInfo());
        gbeanDataChild.addDependency(new ReferencePatterns(parentName));
        kernel.loadGBean(gbeanDataChild, MockGBean.class.getClassLoader());
        kernel.startGBean(gbeanDataChild.getName());
        assertEquals(State.STARTING_INDEX, kernel.getGBeanState(gbeanDataChild.getName()));
        kernel.loadGBean(gbeanDataParent, MockGBean.class.getClassLoader());
        assertEquals(State.STARTING_INDEX, kernel.getGBeanState(gbeanDataChild.getName()));
        kernel.startGBean(parentName);
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(gbeanDataChild.getName()));
    }

    private AbstractName buildAbstractName(String name, GBeanInfo info) throws MalformedObjectNameException {
        Map names = new HashMap();
        names.put("name", name);
        return new AbstractName(new Artifact("test", "foo", "1", "car"), names, info.getInterfaces(), new ObjectName("test:MockGBean=" + name));
    }
    protected void setUp() throws Exception {
        kernel = KernelFactory.newInstance().createKernel("test");
        kernel.boot();
    }

    protected void tearDown() throws Exception {
        kernel.shutdown();
    }

}
