/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.connector;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.management.ObjectName;

import junit.framework.TestCase;
import org.apache.geronimo.connector.mock.MockAdminObject;
import org.apache.geronimo.connector.mock.MockAdminObjectImpl;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContextImpl;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;

/**
 * @version $Rev$ $Date$
 */
public class AdminObjectWrapperTest extends TestCase {

    private Kernel kernel;
    private ObjectName selfName;
    private static final String TARGET_NAME = "testAOName";

    public void testProxy() throws Exception {
        Object proxy = kernel.invoke(selfName, "$getResource");
        assertNotNull(proxy);
        assertTrue(proxy instanceof MockAdminObject);
        MockAdminObject mockAdminObject = ((MockAdminObject) proxy).getSomething();
        assertNotNull(mockAdminObject);
//        kernel.stopGBean(selfName);
//        try {
//            ((MockAdminObject) proxy).getSomething();
//            fail();
//        } catch (IllegalStateException ise) {
//        }
//        kernel.startGBean(selfName);
//        ((MockAdminObject) proxy).getSomething();
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
//        try {
//            proxy3.getSomething();
//            fail();
//        } catch (IllegalStateException ise) {
//        }
        kernel.startGBean(selfName);
        proxy3.getSomething();

    }

//this should be in ENCConfigBuilder tests.
//    public void testLocalLookup() throws Exception {
//        ComponentContextBuilder builder = new ComponentContextBuilder();
//        ENCConfigBuilder.addResourceEnvRefs(earContext, uri, resEnvRefs, cl, refMap, builder);
//        GerLocalRefType localRef = GerLocalRefType.Factory.newInstance();
//        localRef.setRefName("resourceenvref");
//        localRef.setKernelName(KERNEL_NAME);
//        localRef.setTargetName(TARGET_NAME);
//        builder.
//                addResourceEnvRef("resourceenvref", MockAdminObject.class, localRef);
//        ReadOnlyContext roc = builder.getContext();
//        Object o = roc.lookup("env/resourceenvref");
//        assertNotNull(o);
//        assertTrue(o instanceof MockAdminObject);
//    }

    protected void setUp() throws Exception {
        J2eeContext j2eeContext = new J2eeContextImpl("test.domain", "geronimo.server", "testapp", "testmodule", TARGET_NAME, NameFactory.JMS_RESOURCE);
        kernel = new Kernel(j2eeContext.getJ2eeServerName(), j2eeContext.getJ2eeDomainName());
        kernel.boot();
        selfName = NameFactory.getResourceComponentName(null, null, null, null, null, null, j2eeContext);

        GBeanMBean aow = new GBeanMBean(AdminObjectWrapper.getGBeanInfo());
        aow.setAttribute("adminObjectInterface", MockAdminObject.class.getName());
        aow.setAttribute("adminObjectClass", MockAdminObjectImpl.class.getName());
        kernel.loadGBean(selfName, aow);

        kernel.startGBean(selfName);
    }

    protected void tearDown() throws Exception {
        kernel.stopGBean(selfName);
        kernel.shutdown();
    }
}
