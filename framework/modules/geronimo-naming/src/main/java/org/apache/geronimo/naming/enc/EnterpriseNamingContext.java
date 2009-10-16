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

package org.apache.geronimo.naming.enc;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.transaction.UserTransaction;

import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.naming.reference.ClassLoaderAwareReference;
import org.apache.geronimo.naming.reference.EntryFactory;
import org.apache.geronimo.naming.reference.KernelAwareReference;
import org.apache.xbean.naming.context.ImmutableContext;

/**
 * @version $Rev$ $Date$
 */
public final class EnterpriseNamingContext {

    public static Context createEnterpriseNamingContext(Map<String, Object> componentContext, UserTransaction userTransaction, Kernel kernel, ClassLoader classLoader) throws NamingException {
        Map<String, Object> map = new HashMap<String, Object>();
        if (componentContext != null) {
            map.putAll(componentContext);
        }

        boolean containsEnv = false;
        for (Map.Entry<String, Object> entry: map.entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();

            if (name.startsWith("env/")) {
                containsEnv = true;
            }
            if (value instanceof EntryFactory) {
                value = ((EntryFactory)value).buildEntry(kernel, classLoader);
                entry.setValue(value);
            }
            if (value instanceof KernelAwareReference) {
                ((KernelAwareReference) value).setKernel(kernel);
            }
            if (value instanceof ClassLoaderAwareReference) {
                ((ClassLoaderAwareReference) value).setClassLoader(classLoader);
            }
        }

        if (!containsEnv) {
            Context env = new ImmutableContext("java:comp/env", Collections.EMPTY_MAP, false);
            map.put("env", env);
        }

        if (userTransaction != null) {
            map.put("UserTransaction", userTransaction);
        }

        return createEnterpriseNamingContext(map);
    }

    public static Context createEnterpriseNamingContext(Map context) throws NamingException {
        return new ImmutableContext(context, false);
    }

}
