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
package org.apache.geronimo.gbean.runtime;

import javax.management.ObjectName;

import junit.framework.TestCase;
import org.apache.geronimo.gbean.DynamicGAttributeInfo;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.InvalidConfigurationException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.LifecycleAdapter;
import org.apache.geronimo.kernel.MockDynamicGBean;
import org.apache.geronimo.kernel.MockGBean;

/**
 * @version $Rev$ $Date$
 */
public class GBeanAttributeTest extends TestCase {

    private static final String attributeName = "Name";

    private static final String persistentPrimitiveAttributeName = "MutableInt";

    /**
     * Wraps GBean
     */
    private GBeanInstance gbeanInstance = null;

    /**
     * Wraps DynamicGBean
     */
    private GBeanInstance dynamicGBeanInstance = null;

    private MethodInvoker getInvoker = null;

    private MethodInvoker setInvoker = null;

    private GAttributeInfo persistentPrimitiveAttributeInfo = null;
    private GAttributeInfo attributeInfo = null;
    private Kernel kernel;
//    private GAttributeInfo throwingExceptionAttributeInfo = null;

    public final void testGBeanMBeanAttributeGBeanMBeanStringClassMethodInvokerMethodInvoker() {
        try {
            GBeanAttribute.createFrameworkAttribute(null, null, null, null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
//        try {
//            GBeanAttribute.createFrameworkAttribute(gbeanInstance, attributeName, String.class, null);
//            fail("InvalidConfigurationException expected");
//        } catch (InvalidConfigurationException expected) {
//        }
        GBeanAttribute attribute;
        attribute = GBeanAttribute.createFrameworkAttribute(gbeanInstance, attributeName, String.class, getInvoker);
        assertEquals(String.class, attribute.getType());
        assertEquals(attributeName, attribute.getName());
        assertTrue(attribute.isReadable());
        assertFalse(attribute.isWritable());
        assertFalse(attribute.isPersistent());
        attribute = GBeanAttribute.createFrameworkAttribute(gbeanInstance, attributeName, String.class, null, setInvoker, false, null);
        assertEquals(String.class, attribute.getType());
        assertEquals(attributeName, attribute.getName());
        assertFalse(attribute.isReadable());
        assertTrue(attribute.isWritable());
        assertFalse(attribute.isPersistent());
        attribute = GBeanAttribute.createFrameworkAttribute(gbeanInstance, attributeName, String.class, getInvoker, setInvoker, false, null);
        assertEquals(String.class, attribute.getType());
        assertEquals(attributeName, attribute.getName());
        assertTrue(attribute.isReadable());
        assertTrue(attribute.isWritable());
        assertFalse(attribute.isPersistent());
    }

    public final void testGBeanAttributeInfoClass() {
        try {
            new GBeanAttribute(null, null, false);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }

        // 2. @todo BUG An attribute must be readable, writable, or persistent
        // GBeanAttribute ctor doesn't check if readable/writable are
        // null's
        try {
            new GBeanAttribute(gbeanInstance, attributeInfo, false);
            // till Dain sorts out the question of ctor
            // fail("InvalidConfigurationException expected");
        } catch (InvalidConfigurationException expected) {
        }

        try {
            GAttributeInfo invalidAttributeInfo = new GAttributeInfo(attributeName, String.class.getName(), false, null, null);

            new GBeanAttribute(gbeanInstance, invalidAttributeInfo, false);
            fail("InvalidConfigurationException expected");
        } catch (InvalidConfigurationException expected) {
        }

        {
            final GAttributeInfo attributeInfo = new GAttributeInfo(attributeName, String.class.getName(), false, Boolean.TRUE, Boolean.FALSE, null, null);
            GBeanAttribute attribute = new GBeanAttribute(gbeanInstance, attributeInfo, false);
            assertTrue(attribute.isReadable());
            assertFalse(attribute.isWritable());
        }

        {
            final GAttributeInfo attributeInfo = new GAttributeInfo(persistentPrimitiveAttributeName, int.class.getName(), false, Boolean.FALSE, Boolean.TRUE, null, null);
            GBeanAttribute attribute = new GBeanAttribute(gbeanInstance, attributeInfo, false);
            assertFalse(attribute.isReadable());
            assertTrue(attribute.isWritable());
        }

        {
            final GAttributeInfo attributeInfo = new GAttributeInfo("AnotherFinalInt", int.class.getName(), false, Boolean.TRUE, Boolean.TRUE, null, null);
            try {
                new GBeanAttribute(gbeanInstance, attributeInfo, false);
                fail("Getter and setter methods do not have the same types; InvalidConfigurationException expected");
            } catch (InvalidConfigurationException expected) {
            }
        }

        {
            // the attribute name and getter name are different, yet both
            // exist.
            // getYetAnotherFinalInt doesn't exist
            final GAttributeInfo attributeInfo = new GAttributeInfo("YetAnotherFinalInt", int.class.getName(), true, "getFinalInt", null);
            GBeanAttribute attribute = new GBeanAttribute(gbeanInstance, attributeInfo, false);
            assertNotNull(attribute);
        }

        {
            final GAttributeInfo attributeInfo = new GAttributeInfo("YetAnotherFinalInt", int.class.getName(), true, null, "setCharAsYetAnotherFinalInt");
            try {
                new GBeanAttribute(gbeanInstance, attributeInfo, false);
                fail("Expected InvalidConfigurationException due to invalid setter parameter type");
            } catch (InvalidConfigurationException expected) {
            }
        }

        {
            final GAttributeInfo attributeInfo = new GAttributeInfo("YetAnotherFinalInt", int.class.getName(), true, null, "setBooleanAsYetAnotherFinalInt");
            try {
                new GBeanAttribute(gbeanInstance, attributeInfo, false);
                fail("Expected InvalidConfigurationException due to invalid setter parameter type");
            } catch (InvalidConfigurationException expected) {
            }
        }

        {
            final GAttributeInfo attributeInfo = new GAttributeInfo("YetAnotherFinalInt", int.class.getName(), true, null, "setByteAsYetAnotherFinalInt");
            try {
                new GBeanAttribute(gbeanInstance, attributeInfo, false);
                fail("Expected InvalidConfigurationException due to invalid setter parameter type");
            } catch (InvalidConfigurationException expected) {
            }
        }

        {
            final GAttributeInfo attributeInfo = new GAttributeInfo("YetAnotherFinalInt", int.class.getName(), true, null, "setShortAsYetAnotherFinalInt");
            try {
                new GBeanAttribute(gbeanInstance, attributeInfo, false);
                fail("Expected InvalidConfigurationException due to invalid setter parameter type");
            } catch (InvalidConfigurationException expected) {
            }
        }

        {
            final GAttributeInfo attributeInfo = new GAttributeInfo("YetAnotherFinalInt", int.class.getName(), true, null, "setLongAsYetAnotherFinalInt");
            try {
                new GBeanAttribute(gbeanInstance, attributeInfo, false);
                fail("Expected InvalidConfigurationException due to invalid setter parameter type");
            } catch (InvalidConfigurationException expected) {
            }
        }

        {
            final GAttributeInfo attributeInfo = new GAttributeInfo("YetAnotherFinalInt", int.class.getName(), true, null, "setFloatAsYetAnotherFinalInt");
            try {
                new GBeanAttribute(gbeanInstance, attributeInfo, false);
                fail("Expected InvalidConfigurationException due to invalid setter parameter type");
            } catch (InvalidConfigurationException expected) {
            }
        }

        {
            final GAttributeInfo attributeInfo = new GAttributeInfo("YetAnotherFinalInt", int.class.getName(), true, null, "setDoubleAsYetAnotherFinalInt");
            try {
                new GBeanAttribute(gbeanInstance, attributeInfo, false);
                fail("Expected InvalidConfigurationException due to invalid setter parameter type");
            } catch (InvalidConfigurationException expected) {
            }
        }

        {
            final GAttributeInfo attributeInfo = new GAttributeInfo("YetAnotherFinalInt", int.class.getName(), true, "getVoidGetterOfFinalInt", null);
            try {
                new GBeanAttribute(gbeanInstance, attributeInfo, false);
                fail("Getter method not found on target; InvalidConfigurationException expected");
            } catch (InvalidConfigurationException expected) {
            }
        }

        {
            final GAttributeInfo attributeInfo = new GAttributeInfo("YetAnotherFinalInt", int.class.getName(), true, null, "setThatDoesntExist");
            try {
                new GBeanAttribute(gbeanInstance, attributeInfo, false);
                fail("Setter method not found on target; InvalidConfigurationException expected");
            } catch (InvalidConfigurationException expected) {
            }
        }

        {
            final DynamicGAttributeInfo dynamicAttributeInfo = new DynamicGAttributeInfo(attributeName);
            GBeanAttribute attribute = new GBeanAttribute(gbeanInstance, dynamicAttributeInfo, false);
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
            GBeanAttribute attribute = new GBeanAttribute(gbeanInstance, dynamicAttributeInfo, false);
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
            GBeanAttribute attribute = new GBeanAttribute(gbeanInstance, dynamicAttributeInfo, false);
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
            GBeanAttribute attribute = new GBeanAttribute(gbeanInstance, dynamicAttributeInfo, false);
            assertTrue(attribute.isPersistent());
            assertEquals(dynamicAttributeInfo.isPersistent(), attribute.isPersistent());
            assertFalse(attribute.isReadable());
            assertEquals(dynamicAttributeInfo.isReadable().booleanValue(), attribute.isReadable());
            assertFalse(attribute.isWritable());
            assertEquals(dynamicAttributeInfo.isWritable().booleanValue(), attribute.isWritable());
            assertEquals(dynamicAttributeInfo.getName(), attribute.getName());
        }
    }

//    public final void testOnline() throws Exception {
//
//        // 1. setValue throws Exception
//        {
//            final Integer valueThatCausesException = new Integer(-1);
//
//            final GBeanAttribute attribute = new GBeanAttribute(gbeanInstance, throwingExceptionAttributeInfo);
//            attribute.setValue(valueThatCausesException);
//
//            final Kernel kernel = new Kernel("test.kernel");
//            try {
//                kernel.boot();
//                kernel.loadGBean(name, gbeanInstance);
//                attribute.start();
//                fail("Setter upon call with " + valueThatCausesException + " should have thrown exception");
//            } catch (/* IllegalArgument */Exception expected) {
//            } finally {
//                // @todo possible BUG: gbeanInstance holds information on being online
//                // although kernel is shutdown
//                // explicit unloading GBean
//                kernel.unloadGBean(name);
//                kernel.shutdown();
//            }
//        }
//
//        // 2. setValue throws Error
//        {
//            final Integer valueThatCausesError = new Integer(-2);
//
//            final GBeanAttribute attribute = new GBeanAttribute(gbeanInstance, throwingExceptionAttributeInfo);
//            attribute.setValue(valueThatCausesError);
//
//            final Kernel kernel = new Kernel("test.kernel");
//            try {
//                kernel.boot();
//                kernel.loadGBean(name, gbeanInstance);
//                attribute.start();
//                fail("Setter upon call with " + valueThatCausesError + " should have thrown error");
//            } catch (Error expected) {
//            } finally {
//                // @todo possible BUG: see the above finally block
//                kernel.unloadGBean(name);
//                kernel.shutdown();
//            }
//        }
//
//        // 3. setValue throws Throwable
//        {
//            final Integer valueThatCausesThrowable = new Integer(-3);
//
//            final GBeanAttribute attribute = new GBeanAttribute(gbeanInstance, throwingExceptionAttributeInfo);
//            attribute.setValue(valueThatCausesThrowable);
//
//            final Kernel kernel = new Kernel("test.kernel");
//            try {
//                kernel.boot();
//                kernel.loadGBean(name, gbeanInstance);
//                attribute.start();
//                fail("Setter upon call with " + valueThatCausesThrowable + " should have thrown throwable");
//            } catch (Throwable expected) {
//            } finally {
//                kernel.shutdown();
//            }
//        }
//
//        {
//            try {
//                GBeanMBean gmbean2 = new GBeanMBean(MockGBean.getGBeanInfo());
//                GBeanAttribute attribute2 = new GBeanAttribute(gmbean2, throwingExceptionAttributeInfo);
//                attribute2.start();
//                fail("AssertionError or NullPointerException expected");
//            } catch (Exception expected) {
//            } catch (AssertionError expected) {
//            }
//        }
//    }
//
//    public final void testOffline() {
//        //TODO Implement offline().
//    }
//
    public final void testGetValue() throws Exception {
        {
            // attribute that isn't readable and persistent
            final GBeanAttribute attribute = GBeanAttribute.createFrameworkAttribute(gbeanInstance, attributeName, String.class, null, setInvoker, false, null);
            try {
                attribute.getValue();
                fail("Only persistent attributes can be accessed while offline; exception expected");
            } catch (/* IllegalState */Exception expected) {
            }
        }

        {
            final GBeanAttribute attribute = GBeanAttribute.createFrameworkAttribute(gbeanInstance, attributeName, String.class, null, setInvoker, false, null);

            try {
                gbeanInstance.start();

                attribute.getValue();
                fail("This attribute is not readable; exception expected");
            } catch (/* IllegalArgument */Throwable expected) {
            } finally {
                gbeanInstance.stop();
            }
        }

        {
            final DynamicGAttributeInfo dynamicAttributeInfo = new DynamicGAttributeInfo(MockDynamicGBean.MUTABLE_INT_ATTRIBUTE_NAME, true, true, true);
            GBeanAttribute attribute = new GBeanAttribute(dynamicGBeanInstance, dynamicAttributeInfo, false);

            try {
                dynamicGBeanInstance.start();

                final Integer zero = new Integer(0);
                assertEquals(zero, attribute.getValue());

                final Integer one = new Integer(1);
                attribute.setValue(one);
                assertEquals(one, attribute.getValue());
            } finally {
                dynamicGBeanInstance.stop();
            }

        }
    }

    public final void testSetValue() throws Exception {

        // 1. (offline) attribute that isn't readable and persistent
        {
            final GBeanAttribute attribute = GBeanAttribute.createFrameworkAttribute(gbeanInstance, attributeName, String.class, null, setInvoker, false, null);
            try {
                attribute.setValue(null);
                fail("Only persistent attributes can be modified while offline; exception expected");
            } catch (/* IllegalState */Exception expected) {
            }
        }

        // 2. (offline) attribute that is of primitive type, writable and
        // persistent, but not readable
        {
            final GBeanAttribute persistentAttribute = new GBeanAttribute(gbeanInstance, persistentPrimitiveAttributeInfo, false);
            try {
                persistentAttribute.setValue(null);
                fail("Cannot assign null to a primitive attribute; exception expected");
            } catch (/* IllegalArgument */Exception expected) {
            }
        }

        // 3. (online) attribute that is immutable and not persistent
        {
            final GBeanAttribute immutableAttribute = GBeanAttribute.createFrameworkAttribute(gbeanInstance, attributeName, String.class, getInvoker);

            try {
                gbeanInstance.start();

                immutableAttribute.setValue(null);
                fail("This attribute is not writable; exception expected");
            } catch (/* IllegalArgument */Exception expected) {
            } finally {
                gbeanInstance.stop();
            }
        }

        // 4. (online) attribute that is mutable and of primitive type
        {
            final GBeanAttribute mutablePersistentAttribute = new GBeanAttribute(gbeanInstance, persistentPrimitiveAttributeInfo, false);

            try {
                gbeanInstance.start();

                mutablePersistentAttribute.setValue(null);
                fail("Cannot assign null to a primitive attribute; exception expected");
            } catch (/* IllegalArgument */Exception expected) {
            } finally {
                gbeanInstance.stop();
            }
        }

        // 4a. @todo BUG: It's possible to set a value to a persistent
        // attribute while online; IllegalStateException expected
        {
            final GBeanAttribute mutablePersistentAttribute = new GBeanAttribute(gbeanInstance, persistentPrimitiveAttributeInfo, false);

            try {
                gbeanInstance.start();

                mutablePersistentAttribute.setValue(new Integer(4));
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
                    null);

            try {
                gbeanInstance.start();

                attribute.setValue(new Integer(4));
                fail("Exception expected upon setValue's call");
            } catch (/* IllegalState */Exception expected) {
            } finally {
                gbeanInstance.stop();
            }
        }
    }

    protected void setUp() throws Exception {
        kernel = new Kernel("test");
        kernel.boot();

        gbeanInstance = new GBeanInstance(kernel,
                new GBeanData(new ObjectName("test:MockGBean=normal"), MockGBean.getGBeanInfo()),
                new LifecycleAdapter(),
                MockGBean.class.getClassLoader());
        dynamicGBeanInstance = new GBeanInstance(kernel,
                new GBeanData(new ObjectName("test:MockGBean=dynamic"), MockDynamicGBean.getGBeanInfo()),
                new LifecycleAdapter(),
                MockGBean.class.getClassLoader());
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
//        throwingExceptionAttributeInfo = new GAttributeInfo("ExceptionMutableInt", int.class.getName(), true);
        persistentPrimitiveAttributeInfo = new GAttributeInfo(persistentPrimitiveAttributeName, int.class.getName(), true);
    }

    protected void tearDown() throws Exception {
        kernel.shutdown();
        gbeanInstance = null;
    }
}
