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

package org.apache.geronimo.system.logging.log4j;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Provides named nested diagnotic contexts.
 *
 * @version $Rev$ $Date$
 */
public final class NamedNDC {
    /**
     * Mapping from names to NamedNDCs.
     * Currently there is no way to remove a NamedNCD once created, so be sure you really
     * want a new NDC before creating one.
     * @todo make this a weak-valued map
     */
    private static final Map contexts = new HashMap();

    /**
     * Gets the NamedNDC by name, or creates a new one of one does not already exist.
     * @param name the name of the desired NamedNDC
     * @return the existing NamedNDC or a new one
     */
    public static NamedNDC getNamedNDC(String name) {
        synchronized (contexts) {
            NamedNDC context = (NamedNDC) contexts.get(name);
            if (context == null) {
                context = new NamedNDC();
                contexts.put(name, context);
            }
            return context;
        }
    }

    private final ListThreadLocal listThreadLocal = new ListThreadLocal();

    private NamedNDC() {
    }

    public void push(Object value) {
        listThreadLocal.getList().addLast(value);
    }

    public Object get() {
        LinkedList list = listThreadLocal.getList();
        if (list.isEmpty()) {
            return null;
        }
        return list.getLast();
    }

    public Object pop() {
        LinkedList list = listThreadLocal.getList();
        if (list.isEmpty()) {
            return null;
        }
        return list.removeLast();
    }

    public void clear() {
        listThreadLocal.getList().clear();
    }

    private final static class ListThreadLocal extends ThreadLocal {
        public LinkedList getList() {
            return (LinkedList) get();
        }

        protected Object initialValue() {
            return new LinkedList();
        }
    }
}
