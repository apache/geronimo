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

package org.apache.geronimo.kernel;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;

/**
 * @version $Rev$ $Date$
 */
public class MockGBean implements MockEndpoint, MockParentInterface1, MockParentInterface2, MockChildInterface1, MockChildInterface2 {

    private static final GBeanInfo GBEAN_INFO;

    private String objectName;

    private ClassLoader classLoader;

    private Kernel kernel;

    private final String name;

    private final int finalInt;

    private int mutableInt;

    private int exceptionMutableInt;

    private String value;

    private Object someObject;

    private MockEndpoint endpoint;

    private Collection endpointCollection = Collections.EMPTY_SET;

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(MockGBean.class);
        infoFactory.addAttribute("name", String.class, true);
        infoFactory.addAttribute("actualObjectName", String.class, false);
        infoFactory.addAttribute("objectName", String.class, false);
        infoFactory.addAttribute("actualClassLoader", ClassLoader.class, false);
        infoFactory.addAttribute("classLoader", ClassLoader.class, false);
        infoFactory.addAttribute("actualKernel", Kernel.class, false);
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addAttribute("value", String.class, true, true, true);
        infoFactory.addAttribute("myPassword", String.class, true, true, false);
        infoFactory.addAttribute("yourPassword", String.class, true);
        infoFactory.addAttribute("nonStringPassword", Object.class, true);
        infoFactory.addAttribute("finalInt", Integer.TYPE, true);
        infoFactory.addAttribute("mutableInt", Integer.TYPE, false);
        infoFactory.addAttribute("exceptionMutableInt", Integer.TYPE, true);
        infoFactory.addAttribute("endpointMutableInt", Integer.TYPE, false);
        infoFactory.addAttribute("someObject", Object.class, true);

//        infoFactory.addOperation("echo", new Class[]{String.class});
//        infoFactory.addOperation("checkEndpoint");
//        infoFactory.addOperation("checkEndpointCollection");
//        infoFactory.addOperation("doSomething", new Class[]{String.class});
//        infoFactory.addOperation("fetchValue");

        infoFactory.addInterface(MockEndpoint.class, new String[]{"mutableInt"});
        infoFactory.addInterface(MockParentInterface1.class, new String[]{"value"});
        infoFactory.addInterface(MockParentInterface2.class, new String[]{"value"});
        infoFactory.addInterface(MockChildInterface1.class, new String[]{"finalInt"});
        infoFactory.addInterface(MockChildInterface2.class, new String[]{});

        infoFactory.addReference("MockEndpoint", MockEndpoint.class, null);
        infoFactory.addReference("EndpointCollection", MockEndpoint.class, null);

        infoFactory.setConstructor(new String[]{"name", "finalInt", "objectName", "classLoader", "kernel"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public MockGBean() { // present so we can test creating a class-based proxy
        name = null;
        finalInt = 0;
    }

    public MockGBean(String name, int finalInt) {
        this.name = name;
        this.finalInt = finalInt;
    }

    public MockGBean(String name, int finalInt, String objectName, ClassLoader classLoader, Kernel kernel) {
        this.name = name;
        this.finalInt = finalInt;
        this.objectName = objectName;
        this.classLoader = classLoader;
        this.kernel = kernel;
    }

    public Object getSomeObject() {
        return someObject;
    }

    public void setSomeObject(Object someObject) {
        this.someObject = someObject;
    }

    public String getActualObjectName() {
        return objectName;
    }

    public String getObjectName() {
        return objectName;
    }

    public ClassLoader getActualClassLoader() {
        return classLoader;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public Kernel getActualKernel() {
        return kernel;
    }

    public Kernel getKernel() {
        return kernel;
    }

    public void setKernel(Kernel kernel) {
        this.kernel = kernel;
    }

    public String getName() {
        return name;
    }

    public void doNothing() {
    }

    public String echo(String msg) {
        return msg;
    }

    public int getFinalInt() {
        return finalInt;
    }

    /**
     * Parameter ignored
     */
    public void setAnotherFinalInt(int ignored) {
    }

    /**
     * Only setter for YetAnotherFinalInt
     */
    public void setYetAnotherFinalInt(int ignored) {
    }

    /**
     * @see #setYetAnotherFinalInt(int)
     */
    public void setCharAsYetAnotherFinalInt(char yetAnotherFinalInt) {
        setYetAnotherFinalInt((int) yetAnotherFinalInt);
    }

    /**
     * @see #setYetAnotherFinalInt(int)
     */
    public void setBooleanAsYetAnotherFinalInt(boolean yetAnotherFinalInt) {
        setYetAnotherFinalInt((yetAnotherFinalInt ? 1 : 0));
    }

    /**
     * @see #setYetAnotherFinalInt(int)
     */
    public void setByteAsYetAnotherFinalInt(byte yetAnotherFinalInt) {
        setYetAnotherFinalInt(yetAnotherFinalInt);
    }

    /**
     * @see #setYetAnotherFinalInt(int)
     */
    public void setShortAsYetAnotherFinalInt(short yetAnotherFinalInt) {
        setYetAnotherFinalInt(yetAnotherFinalInt);
    }

    /**
     * @see #setYetAnotherFinalInt(int)
     */
    public void setLongAsYetAnotherFinalInt(long yetAnotherFinalInt) {
        setYetAnotherFinalInt((int) yetAnotherFinalInt);
    }

    /**
     * @see #setYetAnotherFinalInt(int)
     */
    public void setFloatAsYetAnotherFinalInt(float yetAnotherFinalInt) {
        setYetAnotherFinalInt((int) yetAnotherFinalInt);
    }

    /**
     * @see #setYetAnotherFinalInt(int)
     */
    public void setDoubleAsYetAnotherFinalInt(double yetAnotherFinalInt) {
        setYetAnotherFinalInt((int) yetAnotherFinalInt);
    }

    /**
     * Getter that returns nothing
     */
    public void getVoidGetterOfFinalInt() {
    }

    public int getMutableInt() {
        return mutableInt;
    }

    public void doSetMutableInt(int mutableInt) {
        setMutableInt(mutableInt);
    }

    public void setMutableInt(int mutableInt) {
        this.mutableInt = mutableInt;
    }

    public String getValue() {
        return value;
    }

    public String fetchValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setExceptionMutableInt(int exceptionMutableInt) throws InvocationTargetException {
        this.exceptionMutableInt = exceptionMutableInt;
        if (exceptionMutableInt == -1) {
            throw new InvocationTargetException(new Exception("Thrown when -1"));
        }
        if (exceptionMutableInt == -2) {
            throw new InvocationTargetException(new Error("Thrown when -2"));
        }
        if (exceptionMutableInt == -3) {
            throw new InvocationTargetException(new Throwable("Thrown when -3"));
        }
    }

    public int getExceptionMutableInt() throws InvocationTargetException {
        if (this.exceptionMutableInt == -1) {
            throw new InvocationTargetException(new Exception("Thrown when -1"));
        }
        if (this.exceptionMutableInt == -2) {
            throw new InvocationTargetException(new Error("Thrown when -2"));
        }
        if (exceptionMutableInt == -3) {
            throw new InvocationTargetException(new Throwable("Thrown when -3"));
        }
        return this.exceptionMutableInt;
    }

    public MockEndpoint getMockEndpoint() {
        return endpoint;
    }

    public void setMockEndpoint(MockEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    public Collection getEndpointCollection() {
        return endpointCollection;
    }

    public void setEndpointCollection(Collection endpointCollection) {
        this.endpointCollection = endpointCollection;
    }

    public String doSomething(String name) {
        return name;
    }

    public String endpointDoSomething(String name) {
        return name;
    }

    public String checkEndpoint() {
        if (endpoint == null) {
            return "no endpoint";
        }
        return endpoint.endpointDoSomething("endpointCheck");
    }

    public int checkEndpointCollection() {
        int successCount = 0;
        for (Iterator iterator = endpointCollection.iterator(); iterator.hasNext();) {
            MockEndpoint mockEndpoint = (MockEndpoint) iterator.next();
            String result = mockEndpoint.endpointDoSomething("endpointCheck");
            if ("endpointCheck".equals(result)) {
                successCount++;
            }
        }
        return successCount;
    }

    public int getEndpointMutableInt() {
        return endpoint.getMutableInt();
    }

    public void setEndpointMutableInt(int mutableInt) {
        endpoint.setMutableInt(mutableInt);
    }
}
