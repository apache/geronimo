/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.geronimo.itest.client;

import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.naming.InitialContext;

import junit.framework.TestCase;
import org.openejb.test.object.ObjectGraph;
import org.openejb.test.stateless.EncStatelessHome;
import org.openejb.test.stateless.EncStatelessObject;
import org.openejb.test.stateless.RmiIiopStatelessHome;
import org.openejb.test.stateless.RmiIiopStatelessObject;

public class EjbCommunicationTest extends TestCase {

    private RmiIiopStatelessHome ejbHome;
    private RmiIiopStatelessObject ejbObject;
    private InitialContext initialContext;


    protected void setUp() throws Exception {
        initialContext = new InitialContext();
        Object obj = initialContext.lookup("java:comp/env/ejb/rmiiiopbean");
        ejbHome = (RmiIiopStatelessHome) obj;
        ejbObject = ejbHome.create();
    }

/*-------------------------------------------------*/
/*  String                                         */
/*-------------------------------------------------*/

    public void testReturnStringObject() {
        try {
            String expected = new String("1");
            String actual = ejbObject.returnStringObject(expected);
            assertEquals(expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void testReturnStringObjectArray() {
        try {
            String[] expected = {"1", "2", "3"};
            String[] actual = ejbObject.returnStringObjectArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i = 0; i < actual.length; i++) {
                assertEquals("Array values are not equal at index " + i, expected[i], actual[i]);
            }
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }


/*-------------------------------------------------*/
/*  Character                                      */
/*-------------------------------------------------*/
    public void testReturnCharacterObject() {
        try {
            Character expected = new Character('1');
            Character actual = ejbObject.returnCharacterObject(expected);
            assertEquals(expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void testReturnCharacterPrimitive() {
        try {
            char expected = '1';
            char actual = ejbObject.returnCharacterPrimitive(expected);
            assertEquals(expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void testReturnCharacterObjectArray() {
        try {
            Character[] expected = {new Character('1'), new Character('2'), new Character('3')};
            Character[] actual = ejbObject.returnCharacterObjectArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i = 0; i < actual.length; i++) {
                assertEquals("Array values are not equal at index " + i, expected[i], actual[i]);
            }
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void testReturnCharacterPrimitiveArray() {
        try {
            char[] expected = {'1', '2', '3'};
            char[] actual = ejbObject.returnCharacterPrimitiveArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i = 0; i < actual.length; i++) {
                assertEquals("Array values are not equal at index " + i, expected[i], actual[i]);
            }
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }


/*-------------------------------------------------*/
/*  Boolean                                        */
/*-------------------------------------------------*/

    public void testReturnBooleanObject() {
        try {
            Boolean expected = new Boolean(true);
            Boolean actual = ejbObject.returnBooleanObject(expected);
            assertEquals(expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void testReturnBooleanPrimitive() {
        try {
            boolean expected = true;
            boolean actual = ejbObject.returnBooleanPrimitive(expected);
            assertEquals("" + expected, "" + actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void testReturnBooleanObjectArray() {
        try {
            Boolean[] expected = {new Boolean(true), new Boolean(false), new Boolean(true)};
            Boolean[] actual = ejbObject.returnBooleanObjectArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i = 0; i < actual.length; i++) {
                assertEquals("Array values are not equal at index " + i, expected[i], actual[i]);
            }
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void testReturnBooleanPrimitiveArray() {
        try {
            boolean[] expected = {false, true, true};
            boolean[] actual = ejbObject.returnBooleanPrimitiveArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i = 0; i < actual.length; i++) {
                assertEquals("Array values are not equal at index " + i, expected[i], actual[i]);
            }
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }


/*-------------------------------------------------*/
/*  Byte                                      */
/*-------------------------------------------------*/

    public void testReturnByteObject() {
        try {
            Byte expected = new Byte("1");
            Byte actual = ejbObject.returnByteObject(expected);
            assertEquals(expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void testReturnBytePrimitive() {
        try {
            byte expected = (byte) 1;
            byte actual = ejbObject.returnBytePrimitive(expected);
            assertEquals(expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void testReturnByteObjectArray() {
        try {
            Byte[] expected = {new Byte("1"), new Byte("2"), new Byte("3")};
            Byte[] actual = ejbObject.returnByteObjectArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i = 0; i < actual.length; i++) {
                assertEquals("Array values are not equal at index " + i, expected[i], actual[i]);
            }
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void testReturnBytePrimitiveArray() {
        try {
            byte[] expected = {(byte) 1, (byte) 2, (byte) 3};
            byte[] actual = ejbObject.returnBytePrimitiveArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i = 0; i < actual.length; i++) {
                assertEquals("Array values are not equal at index " + i, expected[i], actual[i]);
            }
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }


/*-------------------------------------------------*/
/*  Short                                      */
/*-------------------------------------------------*/

    public void testReturnShortObject() {
        try {
            Short expected = new Short("1");
            Short actual = ejbObject.returnShortObject(expected);
            assertEquals(expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void testReturnShortPrimitive() {
        try {
            short expected = (short) 1;
            short actual = ejbObject.returnShortPrimitive(expected);
            assertEquals(expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void testReturnShortObjectArray() {
        try {
            Short[] expected = {new Short("1"), new Short("2"), new Short("3")};
            Short[] actual = ejbObject.returnShortObjectArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i = 0; i < actual.length; i++) {
                assertEquals("Array values are not equal at index " + i, expected[i], actual[i]);
            }
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void testReturnShortPrimitiveArray() {
        try {
            short[] expected = {(short) 1, (short) 2, (short) 3};
            short[] actual = ejbObject.returnShortPrimitiveArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i = 0; i < actual.length; i++) {
                assertEquals("Array values are not equal at index " + i, expected[i], actual[i]);
            }
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }


/*-------------------------------------------------*/
/*  Integer                                      */
/*-------------------------------------------------*/

    public void testReturnIntegerObject() {
        try {
            Integer expected = new Integer(1);
            Integer actual = ejbObject.returnIntegerObject(expected);
            assertEquals(expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void testReturnIntegerPrimitive() {
        try {
            int expected = 1;
            int actual = ejbObject.returnIntegerPrimitive(expected);
            assertEquals(expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void testReturnIntegerObjectArray() {
        try {
            Integer[] expected = {new Integer(1), new Integer(2), new Integer(3)};
            Integer[] actual = ejbObject.returnIntegerObjectArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i = 0; i < actual.length; i++) {
                assertEquals("Array values are not equal at index " + i, expected[i], actual[i]);
            }
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void testReturnIntegerPrimitiveArray() {
        try {
            int[] expected = {1, 2, 3};
            int[] actual = ejbObject.returnIntegerPrimitiveArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i = 0; i < actual.length; i++) {
                assertEquals("Array values are not equal at index " + i, expected[i], actual[i]);
            }
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }


/*-------------------------------------------------*/
/*  Long                                           */
/*-------------------------------------------------*/

    public void testReturnLongObject() {
        try {
            Long expected = new Long("1");
            Long actual = ejbObject.returnLongObject(expected);
            assertEquals(expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void testReturnLongPrimitive() {
        try {
            long expected = 1;
            long actual = ejbObject.returnLongPrimitive(expected);
            assertEquals(expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void testReturnLongObjectArray() {
        try {
            Long[] expected = {new Long("1"), new Long("2"), new Long("3")};
            Long[] actual = ejbObject.returnLongObjectArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i = 0; i < actual.length; i++) {
                assertEquals("Array values are not equal at index " + i, expected[i], actual[i]);
            }
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void testReturnLongPrimitiveArray() {
        try {
            long[] expected = {1, 2, 3};
            long[] actual = ejbObject.returnLongPrimitiveArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i = 0; i < actual.length; i++) {
                assertEquals("Array values are not equal at index " + i, expected[i], actual[i]);
            }
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }


/*-------------------------------------------------*/
/*  Float                                      */
/*-------------------------------------------------*/

    public void testReturnFloatObject() {
        try {
            Float expected = new Float("1.3");
            Float actual = ejbObject.returnFloatObject(expected);
            assertEquals(expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void testReturnFloatPrimitive() {
        try {
            float expected = 1.2F;
            float actual = ejbObject.returnFloatPrimitive(expected);
            assertEquals(expected, actual, 0.00D);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void testReturnFloatObjectArray() {
        try {
            Float[] expected = {new Float("1.1"), new Float("2.2"), new Float("3.3")};
            Float[] actual = ejbObject.returnFloatObjectArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i = 0; i < actual.length; i++) {
                assertEquals("Array values are not equal at index " + i, expected[i], actual[i]);
            }
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void testReturnFloatPrimitiveArray() {
        try {
            float[] expected = {1.2F, 2.3F, 3.4F};
            float[] actual = ejbObject.returnFloatPrimitiveArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i = 0; i < actual.length; i++) {
                assertEquals("Array values are not equal at index " + i, expected[i], actual[i], 0.0D);
            }
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }


/*-------------------------------------------------*/
/*  Double                                      */
/*-------------------------------------------------*/

    public void testReturnDoubleObject() {
        try {
            Double expected = new Double("1.1");
            Double actual = ejbObject.returnDoubleObject(expected);
            assertEquals(expected, actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void testReturnDoublePrimitive() {
        try {
            double expected = 1.2;
            double actual = ejbObject.returnDoublePrimitive(expected);
            assertEquals(expected, actual, 0.0D);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void testReturnDoubleObjectArray() {
        try {
            Double[] expected = {new Double("1.3"), new Double("2.4"), new Double("3.5")};
            Double[] actual = ejbObject.returnDoubleObjectArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i = 0; i < actual.length; i++) {
                assertEquals("Array values are not equal at index " + i, expected[i], actual[i]);
            }
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void testReturnDoublePrimitiveArray() {
        try {
            double[] expected = {1.4, 2.5, 3.6};
            double[] actual = ejbObject.returnDoublePrimitiveArray(expected);

            assertNotNull("The array returned is null", actual);
            assertEquals(expected.length, actual.length);
            for (int i = 0; i < actual.length; i++) {
                assertEquals("Array values are not equal at index " + i, expected[i], actual[i], 0.0D);
            }
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }


/*-------------------------------------------------*/
/*  EJBHome                                        */
/*-------------------------------------------------*/

    public void testReturnEJBHome() {
        try {
            Object obj = initialContext.lookup("client/tests/stateless/EncBean");
            EncStatelessHome expected = (EncStatelessHome) obj;
            assertNotNull("The EJBHome returned from JNDI is null", expected);

            EncStatelessHome actual = (EncStatelessHome) ejbObject.returnEJBHome(expected);
            assertNotNull("The EJBHome returned is null", actual);

        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void testReturnEJBHome2() {
        try {
            EncStatelessHome actual = (EncStatelessHome) ejbObject.returnEJBHome();
            assertNotNull("The EJBHome returned is null", actual);

        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void testReturnNestedEJBHome() {
        try {
            Object obj = initialContext.lookup("client/tests/stateless/EncBean");
            EncStatelessHome expected = (EncStatelessHome) obj;
            assertNotNull("The EJBHome returned from JNDI is null", expected);

            ObjectGraph graph = ejbObject.returnObjectGraph(new ObjectGraph(expected));
            assertNotNull("The ObjectGraph is null", graph);

            EncStatelessHome actual = (EncStatelessHome) graph.getObject();
            assertNotNull("The EJBHome returned is null", actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void testReturnNestedEJBHome2() {
        try {
            ObjectGraph graph = ejbObject.returnNestedEJBHome();
            assertNotNull("The ObjectGraph is null", graph);

            EncStatelessHome actual = (EncStatelessHome) graph.getObject();
            assertNotNull("The EJBHome returned is null", actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void XtestReturnEJBHomeArray() {
        try {

            EncStatelessHome expected[] = new EncStatelessHome[3];
            for (int i = 0; i < expected.length; i++) {
                Object obj = initialContext.lookup("client/tests/stateless/EncBean");
                expected[i] = (EncStatelessHome) obj;
                assertNotNull("The EJBHome returned from JNDI is null", expected[i]);
            }

            EJBHome[] actual = ejbObject.returnEJBHomeArray(expected);
            assertNotNull("The EJBHome array returned is null", actual);
            assertEquals(expected.length, actual.length);

        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }


/*-------------------------------------------------*/
/*  EJBObject                                      */
/*-------------------------------------------------*/

    public void testReturnEJBObject() {
        try {
            Object obj = initialContext.lookup("client/tests/stateless/EncBean");
            EncStatelessHome home = (EncStatelessHome) obj;
            assertNotNull("The EJBHome returned from JNDI is null", home);

            EncStatelessObject expected = home.create();
            assertNotNull("The EJBObject created is null", expected);

            EncStatelessObject actual = (EncStatelessObject) ejbObject.returnEJBObject(expected);
            assertNotNull("The EJBObject returned is null", actual);

            assertTrue("The EJBObejcts are not identical", expected.isIdentical(actual));
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void testReturnEJBObject2() {
        try {
            EncStatelessObject actual = (EncStatelessObject) ejbObject.returnEJBObject();
            assertNotNull("The EJBObject returned is null", actual);

        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void testReturnNestedEJBObject() {
        try {
            Object obj = initialContext.lookup("client/tests/stateless/EncBean");
            EncStatelessHome home = (EncStatelessHome) obj;
            assertNotNull("The EJBHome returned from JNDI is null", home);

            EncStatelessObject expected = home.create();
            assertNotNull("The EJBObject created is null", expected);

            ObjectGraph graph = ejbObject.returnObjectGraph(new ObjectGraph(expected));
            assertNotNull("The ObjectGraph is null", graph);

            EncStatelessObject actual = (EncStatelessObject) graph.getObject();
            assertNotNull("The EJBObject returned is null", actual);

            assertTrue("The EJBObejcts are not identical", expected.isIdentical(actual));
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void testReturnNestedEJBObject2() {
        try {
            ObjectGraph graph = ejbObject.returnNestedEJBObject();
            assertNotNull("The ObjectGraph is null", graph);

            EncStatelessObject actual = (EncStatelessObject) graph.getObject();
            assertNotNull("The EJBHome returned is null", actual);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void XtestReturnEJBObjectArray() {
        try {
            Object obj = initialContext.lookup("client/tests/stateless/EncBean");
            EncStatelessHome home = (EncStatelessHome) obj;
            assertNotNull("The EJBHome returned from JNDI is null", home);

            EncStatelessObject expected[] = new EncStatelessObject[3];
            for (int i = 0; i < expected.length; i++) {
                expected[i] = home.create();
                assertNotNull("The EJBObject created is null", expected[i]);
            }

            EJBObject[] actual = ejbObject.returnEJBObjectArray(expected);
            assertNotNull("The EJBObject array returned is null", actual);
            assertEquals(expected.length, actual.length);

            for (int i = 0; i < actual.length; i++) {
                assertTrue("The EJBObejcts are not identical", expected[i].isIdentical(actual[i]));
            }

        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

/*-------------------------------------------------*/
/*  EJBMetaData                                    */
/*-------------------------------------------------*/

    public void testReturnEJBMetaData() {
        try {
            Object obj = initialContext.lookup("client/tests/stateless/EncBean");
            EncStatelessHome home = (EncStatelessHome) obj;
            assertNotNull("The EJBHome returned from JNDI is null", home);

            EJBMetaData expected = home.getEJBMetaData();
            assertNotNull("The EJBMetaData returned is null", expected);

            EJBMetaData actual = ejbObject.returnEJBMetaData(expected);
            assertNotNull("The EJBMetaData returned is null", actual);
            assertEquals(expected.getHomeInterfaceClass(), actual.getHomeInterfaceClass());
            assertEquals(expected.getRemoteInterfaceClass(), actual.getRemoteInterfaceClass());
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void testReturnEJBMetaData2() {
        try {
            EJBMetaData actual = ejbObject.returnEJBMetaData();
            assertNotNull("The EJBMetaData returned is null", actual);
            assertEquals(actual.getHomeInterfaceClass(), actual.getHomeInterfaceClass());
            assertEquals(actual.getRemoteInterfaceClass(), actual.getRemoteInterfaceClass());
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void testReturnNestedEJBMetaData() {
        try {
            Object obj = initialContext.lookup("client/tests/stateless/EncBean");
            EncStatelessHome home = (EncStatelessHome) obj;
            assertNotNull("The EJBHome returned from JNDI is null", home);

            EJBMetaData expected = home.getEJBMetaData();
            assertNotNull("The EJBMetaData returned is null", expected);

            ObjectGraph graph = ejbObject.returnObjectGraph(new ObjectGraph(expected));
            assertNotNull("The ObjectGraph is null", graph);

            EJBMetaData actual = (EJBMetaData) graph.getObject();
            assertNotNull("The EJBMetaData returned is null", actual);
            assertEquals(expected.getHomeInterfaceClass(), actual.getHomeInterfaceClass());
            assertEquals(expected.getRemoteInterfaceClass(), actual.getRemoteInterfaceClass());
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void testReturnNestedEJBMetaData2() {
        try {
            ObjectGraph graph = ejbObject.returnNestedEJBMetaData();
            assertNotNull("The ObjectGraph is null", graph);

            EJBMetaData actual = (EJBMetaData) graph.getObject();
            assertNotNull("The EJBMetaData returned is null", actual);
            assertNotNull("The home interface class of the EJBMetaData is null", actual.getHomeInterfaceClass());
            assertNotNull("The remote interface class of the EJBMetaData is null", actual.getRemoteInterfaceClass());
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void testReturnEJBMetaDataArray() {
        try {

            Object obj = initialContext.lookup("client/tests/stateless/EncBean");
            EncStatelessHome home = (EncStatelessHome) obj;
            assertNotNull("The EJBHome returned from JNDI is null", home);

            EJBMetaData expected[] = new EJBMetaData[3];
            for (int i = 0; i < expected.length; i++) {
                expected[i] = home.getEJBMetaData();
                assertNotNull("The EJBMetaData returned is null", expected[i]);
            }

            EJBMetaData[] actual = (EJBMetaData[]) ejbObject.returnEJBMetaDataArray(expected);
            assertNotNull("The EJBMetaData array returned is null", actual);
            assertEquals(expected.length, actual.length);

            for (int i = 0; i < actual.length; i++) {
                assertNotNull("The EJBMetaData returned is null", actual[i]);
                assertEquals(expected[i].getHomeInterfaceClass(), actual[i].getHomeInterfaceClass());
                assertEquals(expected[i].getRemoteInterfaceClass(), actual[i].getRemoteInterfaceClass());
            }
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }


/*-------------------------------------------------*/
/*  Handle                                         */
/*-------------------------------------------------*/

    public void testReturnHandle() {
        try {
            Object obj = initialContext.lookup("client/tests/stateless/EncBean");
            EncStatelessHome home = (EncStatelessHome) obj;
            assertNotNull("The EJBHome returned from JNDI is null", home);

            EncStatelessObject object = home.create();
            assertNotNull("The EJBObject created is null", object);

            Handle expected = object.getHandle();
            assertNotNull("The EJBObject Handle returned is null", expected);
            assertNotNull("The EJBObject in the Handle is null", expected.getEJBObject());

            Handle actual = ejbObject.returnHandle(expected);
            assertNotNull("The EJBObject Handle returned is null", actual);
            assertNotNull("The EJBObject in the Handle is null", actual.getEJBObject());

            EJBObject exp = expected.getEJBObject();
            EJBObject act = actual.getEJBObject();

            assertTrue("The EJBObjects in the Handles are not identical", exp.isIdentical(act));
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void testReturnHandle2() {
        try {
            Handle actual = ejbObject.returnHandle();
            assertNotNull("The EJBObject Handle returned is null", actual);
            assertNotNull("The EJBObject in the Handle is null", actual.getEJBObject());

        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void testReturnNestedHandle() {
        try {
            Object obj = initialContext.lookup("client/tests/stateless/EncBean");
            EncStatelessHome home = (EncStatelessHome) obj;
            assertNotNull("The EJBHome returned from JNDI is null", home);

            EncStatelessObject object = home.create();
            assertNotNull("The EJBObject created is null", object);

            Handle expected = object.getHandle();
            assertNotNull("The EJBObject Handle returned is null", expected);
            assertNotNull("The EJBObject in the Handle is null", expected.getEJBObject());

            ObjectGraph graph = ejbObject.returnObjectGraph(new ObjectGraph(expected));
            assertNotNull("The ObjectGraph is null", graph);

            Handle actual = (Handle) graph.getObject();
            assertNotNull("The EJBObject Handle returned is null", actual);
            assertNotNull("The EJBObject in the Handle is null", actual.getEJBObject());

            EJBObject exp = expected.getEJBObject();
            EJBObject act = actual.getEJBObject();

            assertTrue("The EJBObjects in the Handles are not identical", exp.isIdentical(act));

        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void testReturnNestedHandle2() {
        try {
            ObjectGraph graph = ejbObject.returnNestedHandle();
            assertNotNull("The ObjectGraph is null", graph);

            Handle actual = (Handle) graph.getObject();
            assertNotNull("The EJBObject Handle returned is null", actual);
            assertNotNull("The EJBObject in the Handle is null", actual.getEJBObject());
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    public void testReturnHandleArray() {
        try {
            Object obj = initialContext.lookup("client/tests/stateless/EncBean");
            EncStatelessHome home = (EncStatelessHome) obj;
            assertNotNull("The EJBHome returned from JNDI is null", home);

            EncStatelessObject object = home.create();
            assertNotNull("The EJBObject created is null", object);

            Handle expected[] = new Handle[3];
            for (int i = 0; i < expected.length; i++) {
                expected[i] = object.getHandle();
                assertNotNull("The EJBObject Handle returned is null", expected[i]);
            }

            Handle[] actual = (Handle[]) ejbObject.returnHandleArray(expected);
            assertNotNull("The Handle array returned is null", actual);
            assertEquals(expected.length, actual.length);

            for (int i = 0; i < expected.length; i++) {
                assertNotNull("The EJBObject Handle returned is null", actual[i]);
                assertNotNull("The EJBObject in the Handle is null", actual[i].getEJBObject());
                assertTrue("The EJBObjects in the Handles are not equal", expected[i].getEJBObject().isIdentical(actual[i].getEJBObject()));
            }

        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

}
