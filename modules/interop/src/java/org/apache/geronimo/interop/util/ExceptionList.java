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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;


public class ExceptionList extends ArrayList {
    public ExceptionList() {
    }

    public ExceptionList(Constructor template) {
        Class[] types = template.getExceptionTypes();
        add(types);
    }

    public ExceptionList(Method template) {
        Class[] types = template.getExceptionTypes();
        add(types);
    }

    public ExceptionList(Class[] types) {
        add(types);
    }

    public void add(Class[] types) {
        int n = types.length;
        for (int i = 0; i < n; i++) {
            Class type = types[i];
            if (ExceptionUtil.isUserException(type)) {
                add(type);
            }
        }
    }

    public ExceptionList add(String type) {
        super.add(type);
        return this;
    }

    public ExceptionList add(Class type) {
        return add(JavaType.getName(type));
    }

    public String toString() {
        if (size() == 0) {
            return "";
        }
        StringBuffer sb = new StringBuffer(" throws ");
        int comma = 0;
        for (Iterator i = iterator(); i.hasNext(); comma++) {
            String type = (String) i.next();
            if (comma > 0) {
                sb.append(", ");
            }
            sb.append(type);
        }
        return sb.toString();
    }
}
