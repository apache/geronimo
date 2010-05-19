/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.openejb;

import junit.framework.TestCase;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.kernel.osgi.MockBundle;

/**
 * @version $Rev$ $Date$
 */
public class GBeanTest extends TestCase {
    public void testOpenEjbSystemGBean() {
        GBeanInfo.getGBeanInfo(OpenEjbSystemGBean.class.getName(), new MockBundle(getClass().getClassLoader(), "test", 0L));
    }

    public void testEjbModuleImplGBean() {
        GBeanInfo.getGBeanInfo(EjbModuleImpl.class.getName(), new MockBundle(getClass().getClassLoader(), "test", 0L));
    }

    public void testEntityDeploymentGBean() {
        GBeanInfo.getGBeanInfo(EntityDeploymentGBean.class.getName(), new MockBundle(getClass().getClassLoader(), "test", 0L));
    }

    public void testMessageDrivenDeploymentGBean() {
        GBeanInfo.getGBeanInfo(MessageDrivenDeploymentGBean.class.getName(), new MockBundle(getClass().getClassLoader(), "test", 0L));
    }

    public void testStatefulDeploymentGBean() {
        GBeanInfo.getGBeanInfo(StatefulDeploymentGBean.class.getName(), new MockBundle(getClass().getClassLoader(), "test", 0L));
    }

    public void testStatelessDeploymentGBean() {
        GBeanInfo.getGBeanInfo(StatelessDeploymentGBean.class.getName(), new MockBundle(getClass().getClassLoader(), "test", 0L));
    }
}
