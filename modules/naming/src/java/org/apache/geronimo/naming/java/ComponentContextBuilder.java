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

package org.apache.geronimo.naming.java;

import java.util.Map;
import java.util.HashMap;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.naming.NamingException;
import javax.transaction.UserTransaction;

import org.apache.geronimo.kernel.ClassLoading;
import org.apache.geronimo.naming.reference.GBeanProxyReference;
import org.apache.geronimo.naming.reference.KernelReference;

/**
 * TODO consider removing this class. The only purpose is to slightly hide the internalBind method.
 *
 * @version $Rev$ $Date$
 */
public class ComponentContextBuilder {
    private static final String ENV = "env/";
    private final Map context = new HashMap();

    public Map getContext() {
        return context;
    }

    public void addUserTransaction(UserTransaction userTransaction) throws NamingException {
        context.put("UserTransaction", userTransaction);
    }

    public void bind(String name, Object value) throws NamingException {
        context.put(ENV + name, value);
    }

    public void addEnvEntry(String name, String type, String text, ClassLoader classLoader) throws NamingException, NumberFormatException {
        Object value;
        if (text == null) {
            if ("org.apache.geronimo.kernel.Kernel".equals(type)) {
                value = new KernelReference();
            } else {
                value = null;
            }
        } else if ("java.lang.String".equals(type)) {
            value = text;
        } else if ("java.lang.Character".equals(type)) {
            value = new Character(text.charAt(0));
        } else if ("java.lang.Boolean".equals(type)) {
            value = Boolean.valueOf(text);
        } else if ("java.lang.Byte".equals(type)) {
            value = Byte.valueOf(text);
        } else if ("java.lang.Short".equals(type)) {
            value = Short.valueOf(text);
        } else if ("java.lang.Integer".equals(type)) {
            value = Integer.valueOf(text);
        } else if ("java.lang.Long".equals(type)) {
            value = Long.valueOf(text);
        } else if ("java.lang.Float".equals(type)) {
            value = Float.valueOf(text);
        } else if ("java.lang.Double".equals(type)) {
            value = Double.valueOf(text);
        } else {
            Class clazz = null;
            try {
                clazz = ClassLoading.loadClass(type, classLoader);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Could not load class for env-entry " + name + ", " + type);
            }
            ObjectName objectName = null;
            try {
                objectName = ObjectName.getInstance(text);
            } catch (MalformedObjectNameException e) {
                throw new IllegalArgumentException("If env-entry type is not String, Character, Byte, Short, Integer, Long, " +
                        "Boolean, Double, or Float, the text value must be a valid ObjectName for use in a GBeanProxy:" +
                        " name= " + name +
                        ", value=" + type +
                        ", text=" + text);
            }
            value = new GBeanProxyReference(objectName, clazz);

        }
        context.put(ENV + name, value);
    }
}
