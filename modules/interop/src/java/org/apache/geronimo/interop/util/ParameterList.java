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

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedList;


public class ParameterList extends LinkedList {
    public ParameterList() {
    }

    public ParameterList(Class[] types) {
        init(types);
    }

    public ParameterList(Method template) {
        Class[] types = template.getParameterTypes();
        init(types);
    }

    private void init(Class[] types) {
        int n = types.length;
        for (int i = 0; i < n; i++) {
            Class type = types[i];
            add(type, "p" + (i + 1));
        }
    }

    public ParameterList add(String type, String name) {
        add(new MethodParameter(type, name));
        return this;
    }

    public ParameterList add(Class type, String name) {
        return add(JavaType.getName(type), name);
    }

    public ParameterList add(String name) {
        return add("?", name);
    }

    public MethodParameter find(String parameter) {
        for (Iterator i = iterator(); i.hasNext();) {
            MethodParameter mp = (MethodParameter) i.next();
            if (mp.name.equals(parameter)) {
                return mp;
            }
        }
        return null;
    }

    public MethodParameter getParameter(int index) {
        return (MethodParameter) get(index);
    }

    public boolean hasSameTypes(ParameterList that) {
        if (this.size() != that.size()) {
            return false;
        }
        Iterator i = this.iterator();
        Iterator j = that.iterator();
        for (; i.hasNext();) {
            MethodParameter mp1 = (MethodParameter) i.next();
            MethodParameter mp2 = (MethodParameter) j.next();
            if (!mp1.type.equals(mp2.type)) {
                return false;
            }
        }
        return true;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("(");
        int comma = 0;
        for (Iterator i = iterator(); i.hasNext(); comma++) {
            MethodParameter mp = (MethodParameter) i.next();
            if (comma > 0) {
                sb.append(", ");
            }
            sb.append(mp.name);
        }
        sb.append(")");
        return sb.toString();
    }

    public String toStringWithNoSpaces() {
        StringBuffer sb = new StringBuffer("(");
        int comma = 0;
        for (Iterator i = iterator(); i.hasNext(); comma++) {
            MethodParameter mp = (MethodParameter) i.next();
            if (comma > 0) {
                sb.append(",");
            }
            sb.append(mp.name);
        }
        sb.append(")");
        return sb.toString();
    }

    public String toStringWithTypesOnly() {
        StringBuffer sb = new StringBuffer("(");
        int comma = 0;
        for (Iterator i = iterator(); i.hasNext(); comma++) {
            MethodParameter mp = (MethodParameter) i.next();
            if (comma > 0) {
                sb.append(", ");
            }
            sb.append(mp.type);
        }
        sb.append(")");
        return sb.toString();
    }

    public String toStringWithTypes() {
        StringBuffer sb = new StringBuffer("(");
        int comma = 0;
        for (Iterator i = iterator(); i.hasNext(); comma++) {
            MethodParameter mp = (MethodParameter) i.next();
            if (comma > 0) {
                sb.append(", ");
            }
            sb.append(mp.type);
            sb.append(' ');
            sb.append(mp.name);
        }
        sb.append(")");
        return sb.toString();
    }

    public String toStringWithFinalTypes() {
        StringBuffer sb = new StringBuffer("(");
        int comma = 0;
        for (Iterator i = iterator(); i.hasNext(); comma++) {
            MethodParameter mp = (MethodParameter) i.next();
            if (comma > 0) {
                sb.append(", ");
            }
            sb.append("final ");
            sb.append(mp.type);
            sb.append(' ');
            sb.append(mp.name);
        }
        sb.append(")");
        return sb.toString();
    }

    public String getClassArray() {
        if (isEmpty()) {
            return "org.apache.geronimo.interop.util.ArrayUtil.EMPTY_CLASS_ARRAY";
        }
        StringBuffer sb = new StringBuffer("new java.lang.Class[] {");
        int comma = 0;
        for (Iterator i = iterator(); i.hasNext(); comma++) {
            MethodParameter mp = (MethodParameter) i.next();
            if (comma > 0) {
                sb.append(", ");
            }
            sb.append(mp.type + ".class");
        }
        sb.append("}");
        return sb.toString();
    }

    public String getObjectArray() {
        if (isEmpty()) {
            return "org.apache.geronimo.interop.util.ArrayUtil.EMPTY_OBJECT_ARRAY";
        }
        StringBuffer sb = new StringBuffer("new java.lang.Object[] {");
        int comma = 0;
        for (Iterator i = iterator(); i.hasNext(); comma++) {
            MethodParameter mp = (MethodParameter) i.next();
            if (comma > 0) {
                sb.append(", ");
            }
            sb.append(mp.getObject());
        }
        sb.append("}");
        return sb.toString();
    }

    public String wrapAll() {
        StringBuffer sb = new StringBuffer();
        int comma = 0;
        for (Iterator i = iterator(); i.hasNext(); comma++) {
            MethodParameter mp = (MethodParameter) i.next();
            if (comma > 0) {
                sb.append(", ");
            }
            sb.append(mp.getObject());
        }
        return sb.toString();
    }
}
