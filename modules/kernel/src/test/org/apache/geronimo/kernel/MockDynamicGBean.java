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
package org.apache.geronimo.kernel;

import org.apache.geronimo.gbean.DynamicGBean;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;

/**
 * @version $Revision: 1.1 $ $Date: 2004/03/18 10:04:50 $
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
        GBeanInfoFactory infoFactory = new GBeanInfoFactory("MockDynamicGBean", MockDynamicGBean.class.getName());
        infoFactory.addAttribute("MutableInt", false);
        GBEAN_INFO = infoFactory.getBeanInfo();
    }
}
