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
package org.apache.geronimo.gbean.jmx;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import junit.framework.TestCase;
import org.apache.geronimo.gbean.DynamicGAttributeInfo;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.InvalidConfigurationException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.MockDynamicGBean;
import org.apache.geronimo.kernel.MockGBean;

/**
 * @version $Revision: 1.5 $ $Date: 2004/06/04 22:31:56 $
 */
public class GBeanMBeanAttributeTest extends TestCase {

    private static final String attributeName = "Name";

    private static final String persistentPrimitiveAttributeName = "MutableInt";

    private static ObjectName name;

    static {
        try {
            name = new ObjectName("test:name=MyMockGBean");
        } catch (MalformedObjectNameException ignored) {
        }
    }

    /**
     * Wraps GBean
     */
    private GBeanMBean gmbean = null;

    /**
     * Wraps DynamicGBean
     */
    private GBeanMBean dynamicGmbean = null;

    private MethodInvoker getInvoker = null;

    private MethodInvoker setInvoker = null;

    private GAttributeInfo persistentPrimitiveAttributeInfo = null, attributeInfo = null,
    throwingExceptionAttributeInfo = null;

    public final void testGBeanMBeanAttributeGBeanMBeanStringClassMethodInvokerMethodInvoker() {
        try {
            new GBeanMBeanAttribute((GBeanMBean) null, null, null, null, null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
        try {
            new GBeanMBeanAttribute(gmbean, attributeName, String.class, null, null);
            fail("InvalidConfigurationException expected");
        } catch (InvalidConfigurationException expected) {
        }
        GBeanMBeanAttribute attribute;
        attribute = new GBeanMBeanAttribute(gmbean, attributeName, String.class, getInvoker, null);
        assertEquals(String.class, attribute.getType());
        assertEquals(attributeName, attribute.getName());
        assertTrue(attribute.isReadable());
        assertFalse(attribute.isWritable());
        assertFalse(attribute.isPersistent());
        attribute = new GBeanMBeanAttribute(gmbean, attributeName, String.class, null, setInvoker);
        assertEquals(String.class, attribute.getType());
        assertEquals(attributeName, attribute.getName());
        assertFalse(attribute.isReadable());
        assertTrue(attribute.isWritable());
        assertFalse(attribute.isPersistent());
        attribute = new GBeanMBeanAttribute(gmbean, attributeName, String.class, getInvoker, setInvoker);
        assertEquals(String.class, attribute.getType());
        assertEquals(attributeName, attribute.getName());
        assertTrue(attribute.isReadable());
        assertTrue(attribute.isWritable());
        assertFalse(attribute.isPersistent());
    }

    public final void testGBeanMBeanAttributeGBeanMBeanGAttributeInfoClass() {
        try {
            new GBeanMBeanAttribute(null, null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }

        // 2. @todo BUG An attribute must be readable, writable, or persistent
        // GBeanMBeanAttribute ctor doesn't check if readable/writable are
        // null's
        try {
            new GBeanMBeanAttribute(gmbean, attributeInfo);
            // till Dain sorts out the question of ctor
            // fail("InvalidConfigurationException expected");
        } catch (InvalidConfigurationException expected) {
        }

        try {
            GAttributeInfo invalidAttributeInfo = new GAttributeInfo(attributeName, String.class.getName(), false, null, null);

            new GBeanMBeanAttribute(gmbean, invalidAttributeInfo);
            fail("InvalidConfigurationException expected");
        } catch (InvalidConfigurationException expected) {
        }

        {
            final GAttributeInfo attributeInfo = new GAttributeInfo(attributeName, String.class.getName(), false, Boolean.TRUE, Boolean.FALSE, null, null);
            GBeanMBeanAttribute attribute = new GBeanMBeanAttribute(gmbean, attributeInfo);
            assertTrue(attribute.isReadable());
            assertFalse(attribute.isWritable());
        }

        {
            final GAttributeInfo attributeInfo = new GAttributeInfo(persistentPrimitiveAttributeName, int.class.getName(), false, Boolean.FALSE, Boolean.TRUE, null, null);
            GBeanMBeanAttribute attribute = new GBeanMBeanAttribute(gmbean, attributeInfo);
            assertFalse(attribute.isReadable());
            assertTrue(attribute.isWritable());
        }

        {
            final GAttributeInfo attributeInfo = new GAttributeInfo("AnotherFinalInt", int.class.getName(), false, Boolean.TRUE, Boolean.TRUE, null, null);
            try {
                new GBeanMBeanAttribute(gmbean, attributeInfo);
                fail("Getter and setter methods do not have the same types; InvalidConfigurationException expected");
            } catch (InvalidConfigurationException expected) {
            }
        }

        {
            // the attribute name and getter name are different, yet both
            // exist.
            // getYetAnotherFinalInt doesn't exist
            final GAttributeInfo attributeInfo = new GAttributeInfo("YetAnotherFinalInt", int.class.getName(), true, "getFinalInt", null);
            GBeanMBeanAttribute attribute = new GBeanMBeanAttribute(gmbean, attributeInfo);
            assertNotNull(attribute);
        }

        {
            final GAttributeInfo attributeInfo = new GAttributeInfo("YetAnotherFinalInt", int.class.getName(), true, null, "setCharAsYetAnotherFinalInt");
            try {
                new GBeanMBeanAttribute(gmbean, attributeInfo);
                fail("Expected InvalidConfigurationException due to invalid setter parameter type");
            } catch (InvalidConfigurationException expected) {
            }
        }

        {
            final GAttributeInfo attributeInfo = new GAttributeInfo("YetAnotherFinalInt", int.class.getName(), true, null, "setBooleanAsYetAnotherFinalInt");
            try {
                new GBeanMBeanAttribute(gmbean, attributeInfo);
                fail("Expected InvalidConfigurationException due to invalid setter parameter type");
            } catch (InvalidConfigurationException expected) {
            }
        }

        {
            final GAttributeInfo attributeInfo = new GAttributeInfo("YetAnotherFinalInt", int.class.getName(), true, null, "setByteAsYetAnotherFinalInt");
            try {
                new GBeanMBeanAttribute(gmbean, attributeInfo);
                fail("Expected InvalidConfigurationException due to invalid setter parameter type");
            } catch (InvalidConfigurationException expected) {
            }
        }

        {
            final GAttributeInfo attributeInfo = new GAttributeInfo("YetAnotherFinalInt", int.class.getName(), true, null, "setShortAsYetAnotherFinalInt");
            try {
                new GBeanMBeanAttribute(gmbean, attributeInfo);
                fail("Expected InvalidConfigurationException due to invalid setter parameter type");
            } catch (InvalidConfigurationException expected) {
            }
        }

        {
            final GAttributeInfo attributeInfo = new GAttributeInfo("YetAnotherFinalInt", int.class.getName(), true, null, "setLongAsYetAnotherFinalInt");
            try {
                new GBeanMBeanAttribute(gmbean, attributeInfo);
                fail("Expected InvalidConfigurationException due to invalid setter parameter type");
            } catch (InvalidConfigurationException expected) {
            }
        }

        {
            final GAttributeInfo attributeInfo = new GAttributeInfo("YetAnotherFinalInt", int.class.getName(), true, null, "setFloatAsYetAnotherFinalInt");
            try {
                new GBeanMBeanAttribute(gmbean, attributeInfo);
                fail("Expected InvalidConfigurationException due to invalid setter parameter type");
            } catch (InvalidConfigurationException expected) {
            }
        }

        {
            final GAttributeInfo attributeInfo = new GAttributeInfo("YetAnotherFinalInt", int.class.getName(), true, null, "setDoubleAsYetAnotherFinalInt");
            try {
                new GBeanMBeanAttribute(gmbean, attributeInfo);
                fail("Expected InvalidConfigurationException due to invalid setter parameter type");
            } catch (InvalidConfigurationException expected) {
            }
        }

        {
            final GAttributeInfo attributeInfo = new GAttributeInfo("YetAnotherFinalInt", int.class.getName(), true, "getVoidGetterOfFinalInt", null);
            try {
                new GBeanMBeanAttribute(gmbean, attributeInfo);
                fail("Getter method not found on target; InvalidConfigurationException expected");
            } catch (InvalidConfigurationException expected) {
            }
        }

        {
            final GAttributeInfo attributeInfo = new GAttributeInfo("YetAnotherFinalInt", int.class.getName(), true, null, "setThatDoesntExist");
            try {
                new GBeanMBeanAttribute(gmbean, attributeInfo);
                fail("Setter method not found on target; InvalidConfigurationException expected");
            } catch (InvalidConfigurationException expected) {
            }
        }

        {
            final DynamicGAttributeInfo dynamicAttributeInfo = new DynamicGAttributeInfo(attributeName);
            GBeanMBeanAttribute attribute = new GBeanMBeanAttribute(gmbean, dynamicAttributeInfo);
            assertFalse(attribute.isPersistent());
            assertEquals(dynamicAttributeInfo.isPersistent(), attribute.isPersistent());
            assertTrue(attribute.isReadable());
            assertEquals(dynamicAttributeInfo.isReadable().booleanValue(), attribute.isReadable());
            assertTrue(attribute.isWritable());
            assertEquals(dynamicAttributeInfo.isWritable().booleanValue(), attribute.isWritable());
            assertEquals(dynamicAttributeInfo.getName(), attribute.getName());
        }

        {
            final DynamicGAttributeInfo dynamicAttributeInfo = new DynamicGAttributeInfo(attributeName, true);
            GBeanMBeanAttribute attribute = new GBeanMBeanAttribute(gmbean, dynamicAttributeInfo);
            assertTrue(attribute.isPersistent());
            assertEquals(dynamicAttributeInfo.isPersistent(), attribute.isPersistent());
            assertTrue(attribute.isReadable());
            assertEquals(dynamicAttributeInfo.isReadable().booleanValue(), attribute.isReadable());
            assertTrue(attribute.isWritable());
            assertEquals(dynamicAttributeInfo.isWritable().booleanValue(), attribute.isWritable());
            assertEquals(dynamicAttributeInfo.getName(), attribute.getName());
        }

        {
            final DynamicGAttributeInfo dynamicAttributeInfo = new DynamicGAttributeInfo(attributeName, true, false,
                    true);
            GBeanMBeanAttribute attribute = new GBeanMBeanAttribute(gmbean, dynamicAttributeInfo);
            assertTrue(attribute.isPersistent());
            assertEquals(dynamicAttributeInfo.isPersistent(), attribute.isPersistent());
            assertFalse(attribute.isReadable());
            assertEquals(dynamicAttributeInfo.isReadable().booleanValue(), attribute.isReadable());
            assertTrue(attribute.isWritable());
            assertEquals(dynamicAttributeInfo.isWritable().booleanValue(), attribute.isWritable());
            assertEquals(dynamicAttributeInfo.getName(), attribute.getName());
        }

        {
            final DynamicGAttributeInfo dynamicAttributeInfo = new DynamicGAttributeInfo(attributeName, true, false,
                    false);
            GBeanMBeanAttribute attribute = new GBeanMBeanAttribute(gmbean, dynamicAttributeInfo);
            assertTrue(attribute.isPersistent());
            assertEquals(dynamicAttributeInfo.isPersistent(), attribute.isPersistent());
            assertFalse(attribute.isReadable());
            assertEquals(dynamicAttributeInfo.isReadable().booleanValue(), attribute.isReadable());
            assertFalse(attribute.isWritable());
            assertEquals(dynamicAttributeInfo.isWritable().booleanValue(), attribute.isWritable());
            assertEquals(dynamicAttributeInfo.getName(), attribute.getName());
        }
    }

    public final void testOnline() throws Exception {

        // 1. setValue throws Exception
        {
            final Integer valueThatCausesException = new Integer(-1);

            final GBeanMBeanAttribute attribute = new GBeanMBeanAttribute(gmbean, throwingExceptionAttributeInfo);
            attribute.setValue(valueThatCausesException);

            final Kernel kernel = new Kernel("test.kernel", "test");
            try {
                kernel.boot();
                kernel.loadGBean(name, gmbean);
                attribute.online();
                fail("Setter upon call with " + valueThatCausesException + " should have thrown exception");
            } catch (/* IllegalArgument */Exception expected) {
            } finally {
                // @todo possible BUG: gmbean holds information on being online
                // although kernel is shutdown
                // explicit unloading GBean
                kernel.unloadGBean(name);
                kernel.shutdown();
            }
        }

        // 2. setValue throws Error
        {
            final Integer valueThatCausesError = new Integer(-2);

            final GBeanMBeanAttribute attribute = new GBeanMBeanAttribute(gmbean, throwingExceptionAttributeInfo);
            attribute.setValue(valueThatCausesError);

            final Kernel kernel = new Kernel("test.kernel", "test");
            try {
                kernel.boot();
                kernel.loadGBean(name, gmbean);
                attribute.online();
                fail("Setter upon call with " + valueThatCausesError + " should have thrown error");
            } catch (Error expected) {
            } finally {
                // @todo possible BUG: see the above finally block
                kernel.unloadGBean(name);
                kernel.shutdown();
            }
        }

        // 3. setValue throws Throwable
        {
            final Integer valueThatCausesThrowable = new Integer(-3);

            final GBeanMBeanAttribute attribute = new GBeanMBeanAttribute(gmbean, throwingExceptionAttributeInfo);
            attribute.setValue(valueThatCausesThrowable);

            final Kernel kernel = new Kernel("test.kernel", "test");
            try {
                kernel.boot();
                kernel.loadGBean(name, gmbean);
                attribute.online();
                fail("Setter upon call with " + valueThatCausesThrowable + " should have thrown throwable");
            } catch (Throwable expected) {
            } finally {
                kernel.shutdown();
            }
        }

        {
            try {
                GBeanMBean gmbean2 = new GBeanMBean(MockGBean.getGBeanInfo());
                GBeanMBeanAttribute attribute2 = new GBeanMBeanAttribute(gmbean2, throwingExceptionAttributeInfo);
                attribute2.online();
                fail("AssertionError or NullPointerException expected");
            } catch (Exception expected) {
            } catch (AssertionError expected) {
            }
        }
    }

    public final void testOffline() {
        //TODO Implement offline().
    }

    public final void testGetValue() throws Exception {
        {
            // attribute that isn't readable and persistent
            final GBeanMBeanAttribute attribute = new GBeanMBeanAttribute(gmbean, attributeName, String.class, null,
                    setInvoker);
            try {
                attribute.getValue();
                fail("Only persistent attributes can be accessed while offline; exception expected");
            } catch (/* IllegalState */Exception expected) {
            }
        }

        {
            final GBeanMBeanAttribute attribute = new GBeanMBeanAttribute(gmbean, attributeName, String.class, null,
                    setInvoker);

            final ObjectName name = new ObjectName("test:name=MyMockGBean");

            final Kernel kernel = new Kernel("test.kernel", "test");
            try {
                kernel.boot();
                kernel.loadGBean(name, gmbean);
                kernel.startGBean(name);

                attribute.getValue();
                fail("This attribute is not readable; exception expected");
            } catch (/* IllegalArgument */Exception expected) {
            } finally {
                kernel.shutdown();
            }
        }

        {
            final DynamicGAttributeInfo dynamicAttributeInfo = new DynamicGAttributeInfo(MockDynamicGBean.MUTABLE_INT_ATTRIBUTE_NAME, true, true, true);
            GBeanMBeanAttribute attribute = new GBeanMBeanAttribute(dynamicGmbean, dynamicAttributeInfo);
            final ObjectName name = new ObjectName("test:name=MyMockDynamicGBean");

            final Kernel kernel = new Kernel("test.kernel", "test");
            try {
                kernel.boot();
                kernel.loadGBean(name, dynamicGmbean);
                kernel.startGBean(name);

                final Integer zero = new Integer(0);
                assertEquals(zero, attribute.getValue());

                final Integer one = new Integer(1);
                attribute.setValue(one);
                assertEquals(one, attribute.getValue());
            } finally {
                kernel.shutdown();
            }

        }
    }

    public final void testSetValue() throws Exception {

        // 1. (offline) attribute that isn't readable and persistent
        {
            final GBeanMBeanAttribute attribute = new GBeanMBeanAttribute(gmbean, attributeName, String.class, null,
                    setInvoker);
            try {
                attribute.setValue(null);
                fail("Only persistent attributes can be modified while offline; exception expected");
            } catch (/* IllegalState */Exception expected) {
            }
        }

        // 2. (offline) attribute that is of primitive type, writable and
        // persistent, but not readable
        {
            final GBeanMBeanAttribute persistentAttribute = new GBeanMBeanAttribute(gmbean, persistentPrimitiveAttributeInfo, true);
            try {
                persistentAttribute.setValue(null);
                fail("Cannot assign null to a primitive attribute; exception expected");
            } catch (/* IllegalArgument */Exception expected) {
            }
        }

        // 3. (online) attribute that is immutable and not persistent
        {
            final GBeanMBeanAttribute immutableAttribute = new GBeanMBeanAttribute(gmbean, attributeName, String.class, getInvoker, null);

            final Kernel kernel = new Kernel("test.kernel", "test");
            try {
                kernel.boot();
                kernel.loadGBean(name, gmbean);
                kernel.startGBean(name);

                immutableAttribute.setValue(null);
                fail("This attribute is not writable; exception expected");
            } catch (/* IllegalArgument */Exception expected) {
            } finally {
                kernel.shutdown();
            }
        }

        // 4. (online) attribute that is mutable and of primitive type
        {
            final GBeanMBeanAttribute mutablePersistentAttribute = new GBeanMBeanAttribute(gmbean, persistentPrimitiveAttributeInfo);

            final Kernel kernel = new Kernel("test.kernel", "test");
            try {
                kernel.boot();
                kernel.loadGBean(name, gmbean);
                kernel.startGBean(name);

                mutablePersistentAttribute.setValue(null);
                fail("Cannot assign null to a primitive attribute; exception expected");
            } catch (/* IllegalArgument */Exception expected) {
            } finally {
                kernel.shutdown();
            }
        }

        // 4a. @todo BUG: It's possible to set a value to a persistent
        // attribute while online; IllegalStateException expected
        {
            final GBeanMBeanAttribute mutablePersistentAttribute = new GBeanMBeanAttribute(gmbean, persistentPrimitiveAttributeInfo);

            final Kernel kernel = new Kernel("test.kernel", "test");
            try {
                kernel.boot();
                kernel.loadGBean(name, gmbean);
                kernel.startGBean(name);

                mutablePersistentAttribute.setValue(new Integer(4));
                //fail("Cannot assign a value to a persistent attribute while
                // online; exception expected");
            } catch (/* IllegalState */Exception expected) {
            } finally {
                kernel.shutdown();
            }
        }

        // 5. Invoke setValue so that exception is thrown
        {
            final GBeanMBeanAttribute attribute = new GBeanMBeanAttribute(gmbean, attributeName, int.class, null,
                    setInvoker);

            final Kernel kernel = new Kernel("test.kernel", "test");
            try {
                kernel.boot();
                kernel.loadGBean(name, gmbean);
                kernel.startGBean(name);

                attribute.setValue(new Integer(4));
                fail("Exception expected upon setValue's call");
            } catch (/* IllegalState */Exception expected) {
            } finally {
                kernel.shutdown();
            }
        }
    }

    protected void setUp() throws Exception {
        gmbean = new GBeanMBean(MockGBean.getGBeanInfo());
        dynamicGmbean = new GBeanMBean(MockDynamicGBean.getGBeanInfo());
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
        attributeInfo = new GAttributeInfo(attributeName, String.class.getName(), false);
        throwingExceptionAttributeInfo = new GAttributeInfo("ExceptionMutableInt", int.class.getName(), true);
        persistentPrimitiveAttributeInfo = new GAttributeInfo(persistentPrimitiveAttributeName, int.class.getName(), true);
    }

    protected void tearDown() throws Exception {
        gmbean = null;
    }
}
