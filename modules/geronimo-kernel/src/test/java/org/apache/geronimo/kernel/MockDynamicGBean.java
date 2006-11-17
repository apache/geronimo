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

import org.apache.geronimo.gbean.DynamicGBean;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.DynamicGAttributeInfo;

/**
 * @version $Rev$ $Date$
 */
public class MockDynamicGBean implements DynamicGBean {

    private static final GBeanInfo GBEAN_INFO;

    public static final String MUTABLE_INT_ATTRIBUTE_NAME = "MutableInt";

    private int mutableInt;

    public Object getAttribute(String name) throws Exception {
        if (MUTABLE_INT_ATTRIBUTE_NAME.equals(name)) {
            return new Integer(mutableInt);
        }
        return null;
    }

    public void setAttribute(String name, Object value) throws Exception {
        if (MUTABLE_INT_ATTRIBUTE_NAME.equals(name)) {
            mutableInt = ((Integer) value).intValue();
            return;
        }
        throw new IllegalArgumentException(name + " attribute not supported");
    }

    public Object invoke(String name, Object[] arguments, String[] types) throws Exception {
        return null;
    }

    /**
     * @return GBeanInfo of this GBean
     */
    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(MockDynamicGBean.class);
        infoFactory.addAttribute(new DynamicGAttributeInfo("mutableInt", Integer.class.getName(), false, false, true, true));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }
}
