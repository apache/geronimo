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

package org.apache.geronimo.core.service;

import java.util.LinkedList;

/**
 *
 *
 *
 * @version $Revision: 1.3 $ $Date: 2004/03/10 09:58:42 $
 */
public class StackThreadLocal {
    private final ThreadLocal threadLocal = new ThreadLocal() {
        protected Object initialValue() {
            return new LinkedList();
        }
    };

    public void push(Object value) {
        if (value == null) {
            throw new IllegalArgumentException("Value is null");
        }
        ((LinkedList) threadLocal.get()).addFirst(value);
    }

    public Object pop() {
        LinkedList linkedList = ((LinkedList) threadLocal.get());
        if (linkedList.isEmpty()) {
            return null;
        }
        return linkedList.removeFirst();
    }

    public Object peek() {
        LinkedList linkedList = ((LinkedList) threadLocal.get());
        if (linkedList.isEmpty()) {
            return null;
        }
        return linkedList.getFirst();
    }
}
