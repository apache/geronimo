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

package org.apache.geronimo.kernel;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GConstructorInfo;
import org.apache.geronimo.gbean.GOperationInfo;
import org.apache.geronimo.gbean.GReferenceInfo;

/**
 *
 *
 * @version $Revision: 1.13 $ $Date: 2004/03/10 09:59:02 $
 */
public class MockGBean implements MockEndpoint {
    private static final GBeanInfo GBEAN_INFO;
    private final String name;
    private final int finalInt;
    private int mutableInt;
    private String value;

    private MockEndpoint endpoint;

    private Collection endpointCollection = Collections.EMPTY_SET;

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory("MockGBean", MockGBean.class.getName());
        infoFactory.addAttribute("Name", true);
        infoFactory.addAttribute("Value", true);
        infoFactory.addAttribute("FinalInt", true);
        infoFactory.addAttribute("EndpointMutableInt", false);
        infoFactory.addOperation("checkResource", new Class[] {String.class});
        infoFactory.addOperation("checkEndpoint");
        infoFactory.addOperation("checkEndpointCollection");
        infoFactory.addOperation("doSomething", new Class[] {String.class});
        infoFactory.addInterface(MockEndpoint.class, new String[] {"MutableInt"});
        infoFactory.addReference("MockEndpoint", MockEndpoint.class);
        infoFactory.addReference("EndpointCollection", MockEndpoint.class);
        infoFactory.setConstructor(
                new String[]{"Name", "FinalInt"},
                new Class[]{String.class, Integer.TYPE}
        );
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public MockGBean(String name, int finalInt) {
        this.name = name;
        this.finalInt = finalInt;
    }

    public String getName() {
        return name;
    }

    public int getFinalInt() {
        return finalInt;
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

    public void setValue(String value) {
        this.value = value;
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

    public boolean checkResource(String name) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return cl.getResource(name) != null;
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
