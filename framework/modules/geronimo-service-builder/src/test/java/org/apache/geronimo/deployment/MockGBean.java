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

package org.apache.geronimo.deployment;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;

/**
 *
 *
 * @version $Rev$ $Date$
 */
public class MockGBean implements MockEndpoint {
    private final String name;
    private String value;
    private int intValue;

    private FooBarBean fooBarBean;

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

    public FooBarBean getFooBarBean() {
        return fooBarBean;
    }

    public void setFooBarBean(FooBarBean fooBarBean) {
        this.fooBarBean = fooBarBean;
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
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(MockGBean.class);

        infoFactory.addAttribute("name", String.class, true);
        infoFactory.addAttribute("value", String.class, true);
        infoFactory.addAttribute("intValue", int.class, true);
        infoFactory.addAttribute("fooBarBean", FooBarBean.class, true);

        infoFactory.addReference("MockEndpoint", MockEndpoint.class, null);

        infoFactory.setConstructor(new String[] {"name"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
