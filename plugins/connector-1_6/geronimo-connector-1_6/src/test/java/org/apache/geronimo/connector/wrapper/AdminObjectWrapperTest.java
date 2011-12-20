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

package org.apache.geronimo.connector.wrapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.geronimo.connector.mock.MockAdminObject;
import org.apache.geronimo.connector.mock.MockAdminObjectImpl;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContextImpl;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.osgi.MockBundleContext;
import org.apache.geronimo.kernel.repository.Artifact;
import org.osgi.framework.BundleContext;

/**
 * @version $Rev$ $Date$
 */
public class AdminObjectWrapperTest extends TestCase {

    private Kernel kernel;
    private AbstractName selfName;
    private static final String TARGET_NAME = "testAOName";

    public void testProxy() throws Exception {
        Object proxy = kernel.invoke(selfName, "$getResource");
        assertNotNull(proxy);
        assertTrue(proxy instanceof MockAdminObject);
        MockAdminObject mockAdminObject = ((MockAdminObject) proxy).getSomething();
        assertNotNull(mockAdminObject);
    }

    public void testSerialization() throws Exception {
        MockAdminObject proxy = (MockAdminObject) kernel.invoke(selfName, "$getResource");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(proxy);
        oos.flush();
        byte[] bytes = baos.toByteArray();
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
        Object proxy2 = ois.readObject();
        assertNotNull(proxy2);
        assertTrue(proxy instanceof MockAdminObject);
        MockAdminObject mockAdminObject = proxy.getSomething();
        assertNotNull(mockAdminObject);
        kernel.stopGBean(selfName);
        ObjectInputStream ois2 = new ObjectInputStream(new ByteArrayInputStream(bytes));
        MockAdminObject proxy3 = (MockAdminObject) ois2.readObject();
        kernel.startGBean(selfName);
        proxy3.getSomething();

    }

    protected void setUp() throws Exception {
        super.setUp();
        J2eeContext j2eeContext = new J2eeContextImpl("test.domain", "geronimo.server", "testapp", NameFactory.RESOURCE_ADAPTER_MODULE, "testmodule", TARGET_NAME, NameFactory.JMS_RESOURCE);
        BundleContext bundleContext = new MockBundleContext(getClass().getClassLoader(), "", null, null);
        kernel = KernelFactory.newInstance(bundleContext).createKernel(j2eeContext.getJ2eeDomainName());
        kernel.boot(bundleContext);

        GBeanData aow = buildGBeanData("name", TARGET_NAME, AdminObjectWrapperGBean.class);
        selfName = aow.getAbstractName();
        aow.setAttribute("adminObjectInterface", MockAdminObject.class.getName());
        aow.setAttribute("adminObjectClass", MockAdminObjectImpl.class.getName());
        kernel.loadGBean(aow, bundleContext);

        kernel.startGBean(selfName);
    }

    private GBeanData buildGBeanData(String key, String value, Class info) {
        AbstractName abstractName = buildAbstractName(key, value);
        return new GBeanData(abstractName, info);
    }

    private AbstractName buildAbstractName(String key, String value) {
        Map names = new HashMap();
        names.put(key, value);
        return new AbstractName(new Artifact("test", "foo", "1", "car"), names);
    }

    protected void tearDown() throws Exception {
        kernel.stopGBean(selfName);
        kernel.shutdown();
        super.tearDown();
    }
}
