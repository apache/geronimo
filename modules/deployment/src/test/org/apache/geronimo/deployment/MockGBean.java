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

package org.apache.geronimo.deployment;

import java.util.Collections;

import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GConstructorInfo;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.gbean.GOperationInfo;

/**
 *
 *
 * @version $Revision: 1.5 $ $Date: 2004/02/25 09:57:39 $
 */
public class MockGBean implements MockEndpoint {
    private static final GBeanInfo GBEAN_INFO;
    private final String name;
    private String value;
    private int intValue;

    private MockEndpoint endpoint;

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory("MockGBean", MockGBean.class.getName());
        infoFactory.addAttribute(new GAttributeInfo("Name", true));
        infoFactory.addAttribute(new GAttributeInfo("Value", true));
        infoFactory.addAttribute(new GAttributeInfo("IntValue", true));
        infoFactory.addOperation(new GOperationInfo("checkResource", new String[]{"java.lang.String"}));
        infoFactory.addOperation(new GOperationInfo("checkEndpoint"));
        infoFactory.addOperation(new GOperationInfo("doSomething", new String[]{"java.lang.String"}));
        infoFactory.addReference(new GReferenceInfo("MockEndpoint", MockEndpoint.class.getName()));
        infoFactory.setConstructor(new GConstructorInfo(Collections.singletonList("Name"), Collections.singletonList(String.class)));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public MockGBean(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getIntValue() {
        return intValue;
    }

    public void setIntValue(int intValue) {
        this.intValue = intValue;
    }

    public MockEndpoint getMockEndpoint() {
        return endpoint;
    }

    public void setMockEndpoint(MockEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    public boolean checkResource(String name) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return cl.getResource(name) != null;
    }

    public String doSomething(String name) {
        return name;
    }

    public String checkEndpoint() {
        if (endpoint == null) {
            return "no endpoint";
        }
        return endpoint.doSomething("endpointCheck");
    }
}
