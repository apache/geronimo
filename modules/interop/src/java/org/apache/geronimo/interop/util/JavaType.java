/**
 *
 *  Copyright 2004-2005 The Apache Software Foundation
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
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.interop.util;

import java.util.HashMap;


public abstract class JavaType {
    private static HashMap _wrapper = new HashMap();

    static {
        _wrapper.put("boolean", "java.lang.Boolean");
        _wrapper.put("byte", "java.lang.Byte");
        _wrapper.put("char", "java.lang.Character");
        _wrapper.put("double", "java.lang.Double");
        _wrapper.put("float", "java.lang.Float");
        _wrapper.put("int", "java.lang.Integer");
        _wrapper.put("long", "java.lang.Long");
        _wrapper.put("short", "java.lang.Short");
    }

    /**
     * * Return the name of a type as would be referenced in source code,
     * * e.g. "int", "byte[]", "java.lang.String", "java.lang.Object[][]".
     */
    public static String getName(Class t) {
        if (t.isArray()) {
            return getName(t.getComponentType()) + "[]";
        } else {
            return t.getName().replace('$', '.');
        }
    }

    public static String wrapper(String type) {
        return (String) _wrapper.get(type);
    }

    public static String wrapper(Class type) {
        return wrapper(getName(type));
    }

    public static String wrap(String type, String name) {
        String w = wrapper(type);
        return w != null ? ("new " + w + "(" + name + ")") : name;
    }

    public static String wrap(Class type, String name) {
        return wrap(getName(type), name);
    }

    public static String unwrap(String type, String name) {
        String w = wrapper(type);
        return w != null ? ("((" + w + ")" + name + ")." + type + "Value()") : name;
    }

    public static String unwrap(Class type, String name) {
        return unwrap(getName(type), name);
    }

    public static String unwrapObject(Class type, Object expression) {
        return unwrapObject(getName(type), expression);
    }

    public static String unwrapObject(String type, Object expression) {
        String w = wrapper(type);
        if (w == null) {
            if (type.equals("java.lang.Object")) {
                return expression.toString();
            } else {
                return "(" + type + ")" + expression;
            }
        } else {
            return "((" + w + ")" + expression + ")." + type + "Value()";
        }
    }
}
