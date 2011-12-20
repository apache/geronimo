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
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.InvalidConfigurationException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.MockGBean;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.osgi.MockBundleContext;
import org.osgi.framework.BundleContext;

/**
 * @version $Rev$ $Date$
 */
public class GBeanAttributeTest extends TestCase {

    private static final String attributeName = "Name";

    private static final String persistentPrimitiveAttributeName = "MutableInt";
    private BundleContext bundleContext = new MockBundleContext(getClass().getClassLoader(), null, null, null);

    /**
     * Wraps GBean
     */
    private GBeanInstance gbeanInstance = null;

    private MethodInvoker getInvoker = null;

    private MethodInvoker setInvoker = null;

    private GAttributeInfo persistentPrimitiveAttributeInfo = null;
    private GAttributeInfo attributeInfo = null;
    private Kernel kernel;

    public final void testGBeanAttributeEncryption() {
        GBeanInfo ginfo = gbeanInstance.getGBeanInfo();
        assertTrue(ginfo.getAttribute("value").isEncrypted());
        assertTrue(ginfo.getAttribute("yourPassword").isEncrypted());
        assertFalse(ginfo.getAttribute("myPassword").isEncrypted());
        assertFalse(ginfo.getAttribute("nonStringPassword").isEncrypted());
        assertFalse(ginfo.getAttribute("finalInt").isEncrypted());
    }
    
    public final void testGBeanAttributStringClassMethodInvokerMethodInvoker() {
        try {
            GBeanAttribute.createFrameworkAttribute(null, null, null, null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }

        GBeanAttribute attribute;
        attribute = GBeanAttribute.createFrameworkAttribute(gbeanInstance, attributeName, String.class, getInvoker);
        assertEquals(String.class, attribute.getType());
        assertEquals(attributeName, attribute.getName());
        assertTrue(attribute.isReadable());
        assertFalse(attribute.isWritable());
        assertFalse(attribute.isPersistent());
        assertFalse(attribute.isEncrypted());
        attribute = GBeanAttribute.createFrameworkAttribute(gbeanInstance, attributeName, String.class, null, setInvoker, false, null, false);
        assertEquals(String.class, attribute.getType());
        assertEquals(attributeName, attribute.getName());
        assertFalse(attribute.isReadable());
        assertTrue(attribute.isWritable());
        assertFalse(attribute.isPersistent());
        assertFalse(attribute.isEncrypted());
        attribute = GBeanAttribute.createFrameworkAttribute(gbeanInstance, attributeName, String.class, getInvoker, setInvoker, false, null, false);
        assertEquals(String.class, attribute.getType());
        assertEquals(attributeName, attribute.getName());
        assertTrue(attribute.isReadable());
        assertTrue(attribute.isWritable());
        assertFalse(attribute.isPersistent());
        assertFalse(attribute.isEncrypted());
    }

    public final void testGBeanAttributeInfoClass() {
        try {
            new GBeanAttribute(null, null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }

        // 2. @todo BUG An attribute must be readable, writable, or persistent
        // GBeanAttribute ctor doesn't check if readable/writable are
        // null's
        try {
            new GBeanAttribute(gbeanInstance, attributeInfo);
            // till Dain sorts out the question of ctor
            // fail("InvalidConfigurationException expected");
        } catch (InvalidConfigurationException expected) {
        }

        {
            final GAttributeInfo attributeInfo = new GAttributeInfo(attributeName, String.class.getName(), false, false, true, false, null, null);
            GBeanAttribute attribute = new GBeanAttribute(gbeanInstance, attributeInfo);
            assertTrue(attribute.isReadable());
            assertFalse(attribute.isWritable());
        }

        {
            final GAttributeInfo attributeInfo = new GAttributeInfo(persistentPrimitiveAttributeName, int.class.getName(), false, false, false, true, null, null);
            GBeanAttribute attribute = new GBeanAttribute(gbeanInstance, attributeInfo);
            assertFalse(attribute.isReadable());
            assertTrue(attribute.isWritable());
        }

        {
            // the attribute name and getter name are different, yet both
            // exist.
            // getYetAnotherFinalInt doesn't exist
            final GAttributeInfo attributeInfo = new GAttributeInfo("YetAnotherFinalInt", int.class.getName(), true, false, "getFinalInt", null);
            GBeanAttribute attribute = new GBeanAttribute(gbeanInstance, attributeInfo);
            assertNotNull(attribute);
        }

        {
            final GAttributeInfo attributeInfo = new GAttributeInfo("YetAnotherFinalInt", int.class.getName(), true, false, null, "setCharAsYetAnotherFinalInt");
            try {
                new GBeanAttribute(gbeanInstance, attributeInfo);
                fail("Expected InvalidConfigurationException due to invalid setter parameter type");
            } catch (InvalidConfigurationException expected) {
            }
        }

        {
            final GAttributeInfo attributeInfo = new GAttributeInfo("YetAnotherFinalInt", int.class.getName(), true, false, null, "setBooleanAsYetAnotherFinalInt");
            try {
                new GBeanAttribute(gbeanInstance, attributeInfo);
                fail("Expected InvalidConfigurationException due to invalid setter parameter type");
            } catch (InvalidConfigurationException expected) {
            }
        }

        {
            final GAttributeInfo attributeInfo = new GAttributeInfo("YetAnotherFinalInt", int.class.getName(), true, false, null, "setByteAsYetAnotherFinalInt");
            try {
                new GBeanAttribute(gbeanInstance, attributeInfo);
                fail("Expected InvalidConfigurationException due to invalid setter parameter type");
            } catch (InvalidConfigurationException expected) {
            }
        }

        {
            final GAttributeInfo attributeInfo = new GAttributeInfo("YetAnotherFinalInt", int.class.getName(), true, false, null, "setShortAsYetAnotherFinalInt");
            try {
                new GBeanAttribute(gbeanInstance, attributeInfo);
                fail("Expected InvalidConfigurationException due to invalid setter parameter type");
            } catch (InvalidConfigurationException expected) {
            }
        }

        {
            final GAttributeInfo attributeInfo = new GAttributeInfo("YetAnotherFinalInt", int.class.getName(), true, false, null, "setLongAsYetAnotherFinalInt");
            try {
                new GBeanAttribute(gbeanInstance, attributeInfo);
                fail("Expected InvalidConfigurationException due to invalid setter parameter type");
            } catch (InvalidConfigurationException expected) {
            }
        }

        {
            final GAttributeInfo attributeInfo = new GAttributeInfo("YetAnotherFinalInt", int.class.getName(), true, false, null, "setFloatAsYetAnotherFinalInt");
            try {
                new GBeanAttribute(gbeanInstance, attributeInfo);
                fail("Expected InvalidConfigurationException due to invalid setter parameter type");
            } catch (InvalidConfigurationException expected) {
            }
        }

        {
            final GAttributeInfo attributeInfo = new GAttributeInfo("YetAnotherFinalInt", int.class.getName(), true, false, null, "setDoubleAsYetAnotherFinalInt");
            try {
                new GBeanAttribute(gbeanInstance, attributeInfo);
                fail("Expected InvalidConfigurationException due to invalid setter parameter type");
            } catch (InvalidConfigurationException expected) {
            }
        }

        {
            final GAttributeInfo attributeInfo = new GAttributeInfo("YetAnotherFinalInt", int.class.getName(), true, false, "getVoidGetterOfFinalInt", null);
            try {
                new GBeanAttribute(gbeanInstance, attributeInfo);
                fail("Getter method not found on target; InvalidConfigurationException expected");
            } catch (InvalidConfigurationException expected) {
            }
        }

        {
            final GAttributeInfo attributeInfo = new GAttributeInfo("YetAnotherFinalInt", int.class.getName(), true, false, null, "setThatDoesntExist");
            try {
                new GBeanAttribute(gbeanInstance, attributeInfo);
                fail("Setter method not found on target; InvalidConfigurationException expected");
            } catch (InvalidConfigurationException expected) {
            }
        }
    }

    public final void testGetValue() throws Exception {
        {
            // attribute that isn't readable and persistent
            final GBeanAttribute attribute = GBeanAttribute.createFrameworkAttribute(gbeanInstance, attributeName, String.class, null, setInvoker, false, null, false);
            try {
                attribute.getValue(gbeanInstance);
                fail("Only persistent attributes can be accessed while offline; exception expected");
            } catch (/* IllegalState */Exception expected) {
            }
        }

        {
            final GBeanAttribute attribute = GBeanAttribute.createFrameworkAttribute(gbeanInstance, attributeName, String.class, null, setInvoker, false, null, false);

            try {
                gbeanInstance.start();

                attribute.getValue(gbeanInstance);
                fail("This attribute is not readable; exception expected");
            } catch (/* IllegalArgument */Throwable expected) {
            } finally {
                gbeanInstance.stop();
            }
        }
    }

    public final void testSetValue() throws Exception {

        // 1. (offline) attribute that isn't readable and persistent
        {
            final GBeanAttribute attribute = GBeanAttribute.createFrameworkAttribute(gbeanInstance, attributeName, String.class, null, setInvoker, false, null, false);
            try {
                attribute.setValue(gbeanInstance, null);
                fail("Only persistent attributes can be modified while offline; exception expected");
            } catch (/* IllegalState */Exception expected) {
            }
        }

        // 2. (offline) attribute that is of primitive type, writable and
        // persistent, but not readable
        {
            final GBeanAttribute persistentAttribute = new GBeanAttribute(gbeanInstance, persistentPrimitiveAttributeInfo);
            try {
                persistentAttribute.setValue(gbeanInstance, null);
                fail("Cannot assign null to a primitive attribute; exception expected");
            } catch (/* IllegalArgument */Exception expected) {
            }
        }

        // 3. (online) attribute that is immutable and not persistent
        {
            final GBeanAttribute immutableAttribute = GBeanAttribute.createFrameworkAttribute(gbeanInstance, attributeName, String.class, getInvoker);

            try {
                gbeanInstance.start();

                immutableAttribute.setValue(gbeanInstance, null);
                fail("This attribute is not writable; exception expected");
            } catch (/* IllegalArgument */Exception expected) {
            } finally {
                gbeanInstance.stop();
            }
        }

        // 4. (online) attribute that is mutable and of primitive type
        {
            final GBeanAttribute mutablePersistentAttribute = new GBeanAttribute(gbeanInstance, persistentPrimitiveAttributeInfo);

            try {
                gbeanInstance.start();

                mutablePersistentAttribute.setValue(gbeanInstance, null);
                fail("Cannot assign null to a primitive attribute; exception expected");
            } catch (/* IllegalArgument */Exception expected) {
            } finally {
                gbeanInstance.stop();
            }
        }

        // 4a. @todo BUG: It's possible to set a value to a persistent
        // attribute while online; IllegalStateException expected
        {
            final GBeanAttribute mutablePersistentAttribute = new GBeanAttribute(gbeanInstance, persistentPrimitiveAttributeInfo);

            try {
                gbeanInstance.start();

                mutablePersistentAttribute.setValue(gbeanInstance, new Integer(4));
                //fail("Cannot assign a value to a persistent attribute while
                // online; exception expected");
            } catch (/* IllegalState */Exception expected) {
            } finally {
                gbeanInstance.stop();
            }
        }

        // 5. Invoke setValue so that exception is thrown
        {
            final GBeanAttribute attribute = GBeanAttribute.createFrameworkAttribute(gbeanInstance,
                    attributeName,
                    int.class,
                    null,
                    setInvoker,
                    false,
                    null,
                    false);

            try {
                gbeanInstance.start();

                attribute.setValue(gbeanInstance, new Integer(4));
                fail("Exception expected upon setValue's call");
            } catch (/* IllegalState */Exception expected) {
            } finally {
                gbeanInstance.stop();
            }
        }
    }

    protected void setUp() throws Exception {
        super.setUp();
        kernel = KernelFactory.newInstance(bundleContext).createKernel("test");
        kernel.boot(bundleContext);

        AbstractName name = kernel.getNaming().createRootName(new Artifact("test", "foo", "1", "car"), "test", "test");
        gbeanInstance = new GBeanInstance(new GBeanData(name, MockGBean.getGBeanInfo()),
                kernel,
                kernel.getDependencyManager(),
                new MyLifecycleBroadcaster(),
                bundleContext);

        getInvoker = new MethodInvoker() {
            public Object invoke(Object target, Object[] arguments) throws Exception {
                throw new UnsupportedOperationException("Throws exception to rise test coverage");
            }
        };

        setInvoker = new MethodInvoker() {
            public Object invoke(Object target, Object[] arguments) throws Exception {
                throw new UnsupportedOperationException("Throws exception to rise test coverage");
            }
        };

        attributeInfo = new GAttributeInfo(attributeName, String.class.getName(), false, false, "getName", "setName");
        persistentPrimitiveAttributeInfo = new GAttributeInfo(persistentPrimitiveAttributeName, int.class.getName(), true, false, "getMutableInt", "setMutableInt");
    }

    protected void tearDown() throws Exception {
        kernel.shutdown();
        gbeanInstance = null;
        super.tearDown();
    }

    private static class MyLifecycleBroadcaster implements LifecycleBroadcaster {
        public void fireLoadedEvent() {
        }

        public void fireStartingEvent() {
        }

        public void fireRunningEvent() {
        }

        public void fireStoppingEvent() {
        }

        public void fireStoppedEvent() {
        }

        public void fireFailedEvent() {
        }

        public void fireUnloadedEvent() {
        }
    }
}
