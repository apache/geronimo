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

package org.apache.geronimo.cache;

import java.util.HashMap;

/**
 * This is a very simple implementation of InstanceCache designed for raw flat
 * out speed.  It does not directly support passivation or have any storage
 * limits.
 *
 *
 * @version $Revision: 1.4 $ $Date: 2004/02/25 09:57:25 $
 */
public final class SimpleInstanceCache implements InstanceCache {
    private HashMap active = new HashMap();
    private HashMap inactive = new HashMap();

    public synchronized void putActive(Object key, Object value) {
        inactive.remove(key);
        active.put(key, value);
    }

    public synchronized void putInactive(Object key, Object value) {
        active.remove(key);
        inactive.put(key, value);
    }

    public synchronized Object get(Object key) {
        Object value = active.get(key);
        if (value != null) {
            return value;
        }

        // if it is in the inactive list remove it and add it to the active list
        value = inactive.remove(key);
        if (value != null) {
            active.put(key, value);
        }
        return value;
    }

    public synchronized Object remove(Object key) {
        // first check the active map
        Object value = active.remove(key);

        // also check for an entry in the inactive map
        if (value == null) {
            value = inactive.remove(key);
        } else {
            // this should never happen because we don't let a key be in both maps
            // assert inactive.remove(key) == null;
        }

        return value;
    }

    public synchronized Object peek(Object key) {
        Object value = active.get(key);
        if (value != null) {
            return value;
        }
        return inactive.get(key);
    }

    public synchronized boolean isActive(Object key) {
        return active.containsKey(key);
    }
}

