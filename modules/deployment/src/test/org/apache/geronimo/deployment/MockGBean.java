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

package org.apache.geronimo.deployment;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;

/**
 *
 *
 * @version $Revision: 1.8 $ $Date: 2004/06/03 23:12:54 $
 */
public class MockGBean implements MockEndpoint {
    private final String name;
    private String value;
    private int intValue;

    private MockEndpoint endpoint;

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

    public String doSomething(String name) {
        return name;
    }

    public String checkEndpoint() {
        if (endpoint == null) {
            return "no endpoint";
        }
        return endpoint.doSomething("endpointCheck");
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory("MockGBean", MockGBean.class);

        infoFactory.addAttribute("Name", String.class, true);
        infoFactory.addAttribute("Value", String.class, true);
        infoFactory.addAttribute("IntValue", int.class, true);

        infoFactory.addOperation("checkEndpoint");
        infoFactory.addOperation("doSomething", new Class[]{String.class});

        infoFactory.addReference("MockEndpoint", MockEndpoint.class);

        infoFactory.setConstructor(new String[] {"Name"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
