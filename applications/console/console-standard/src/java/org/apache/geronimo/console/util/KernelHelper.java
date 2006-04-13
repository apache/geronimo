/**
 *
 * Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable.
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

package org.apache.geronimo.console.util;

import javax.management.ObjectName;

import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;

public class KernelHelper {

    protected static Kernel kernel = KernelRegistry.getSingleKernel();

    protected static Object invoke(ObjectName mBeanName, String function)
            throws Exception {
        Object[] NO_ARGS = new Object[0];
        String[] NO_PARAMS = new String[0];
        return invoke(mBeanName, function, NO_ARGS, NO_PARAMS);
    }

    protected static Object invoke(ObjectName mBeanName, String function,
            Object[] args, String[] types) throws Exception {
        Object ret = null;
        ret = kernel.invoke(mBeanName, function, args, types);
        return ret;
    }

    protected static Object get(ObjectName mBeanName, String attributeName) {
        Object ret = null;
        try {
            ret = kernel.getAttribute(mBeanName, attributeName);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        return ret;
    }

    protected static void set(ObjectName mBeanName, String attributeName,
            Object value) {
        Object ret = null;
        try {
            kernel.setAttribute(mBeanName, attributeName, value);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

}
