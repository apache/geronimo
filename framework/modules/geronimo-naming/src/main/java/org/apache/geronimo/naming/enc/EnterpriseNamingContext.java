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
import java.util.Map;
import java.util.Set;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.transaction.UserTransaction;

import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.naming.reference.BundleAwareReference;
import org.apache.geronimo.naming.reference.ClassLoaderAwareReference;
import org.apache.geronimo.naming.reference.EntryFactory;
import org.apache.geronimo.naming.reference.KernelAwareReference;
import org.apache.xbean.naming.context.ImmutableContext;
import org.apache.xbean.naming.context.ImmutableFederatedContext;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
public final class EnterpriseNamingContext {

    public static Context createEnterpriseNamingContext(Set<Context> contexts) throws NamingException {
        if (contexts.contains(null)) {
            contexts.remove(null);
        }
        return new ImmutableFederatedContext("java:", contexts);
    }

    public static Context livenReferences(Map<String, Object> componentContext, UserTransaction userTransaction, Kernel kernel, ClassLoader classLoader, Bundle bundle, String prefix)
            throws NamingException {
        Map<String, Object> map = livenReferencesToMap(componentContext, userTransaction, kernel, classLoader, bundle, prefix);

        return new ImmutableContext(map, false);
    }

    public static Map<String, Object> livenReferencesToMap(Map<String, Object> componentContext, UserTransaction userTransaction, Kernel kernel, ClassLoader classLoader, Bundle bundle, String prefix) throws NamingException {
        Map<String, Object> map = new HashMap<String, Object>();
        boolean containsEnv = false;
        if (componentContext != null) {
            for (Map.Entry<String, Object> entry: componentContext.entrySet()) {
                String name = entry.getKey();
                Object value = entry.getValue();

                if (name.startsWith(prefix + "env/")) {
                    containsEnv = true;
                }
                if (value instanceof EntryFactory) {
                    value = ((EntryFactory)value).buildEntry(kernel, classLoader);
                }
                if (value instanceof KernelAwareReference) {
                    ((KernelAwareReference) value).setKernel(kernel);
                }
                if (value instanceof ClassLoaderAwareReference) {
                    ((ClassLoaderAwareReference) value).setClassLoader(classLoader);
                }
                if (value instanceof BundleAwareReference) {
                    ((BundleAwareReference) value).setBundle(bundle);
                }
                map.put(name, value);
            }
        }


        if (!containsEnv) {
            Context env = new ImmutableContext("java:" + prefix + "env", Collections.<String, Object>emptyMap(), false);
            map.put(prefix + "env", env);
        }

        if (userTransaction != null) {
            map.put(prefix + "UserTransaction", userTransaction);
        }
        return map;
    }

}
