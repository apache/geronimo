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
package org.apache.geronimo.gbean;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class ReferenceMap<T, U> implements Map<T, U>, ReferenceCollectionListener {
    private final Map<T, U> map;
    private final Key<T, U> key;

    /**
     * Constructs the ReferenceMap using a new instance of
     * HashMap as the internal map.
     *
     * @param collection Must be an instance of ReferenceCollection
     * @param map The map instance to which references will be added/removed
     * @param key
     */
    public ReferenceMap(Collection<U> collection, Map<T, U> map, Key<T, U> key) {
        this.map = map;
        this.key = key;
        for (U object : collection) {
            map.put(key.getKey(object), object);
        }
        if (collection instanceof ReferenceCollection) {
            ((ReferenceCollection)collection).addReferenceCollectionListener(this);
        }
    }

    /**
     * Constructs the ReferenceMap using a new instance of
     * HashMap as the internal map.
     *
     * @param collection Must be an instance of ReferenceCollection
     * @param key
     */
    public ReferenceMap(Collection<U> collection, Key<T, U> key) {
        this(collection, new HashMap<T, U>(), key);
    }

    public void memberAdded(ReferenceCollectionEvent event) {
        map.put(key.getKey((U)event.getMember()), (U)event.getMember());
    }

    public void memberRemoved(ReferenceCollectionEvent event) {
        map.remove(key.getKey((U)event.getMember()));
    }

    public interface Key<T, V> {
        public T getKey(V object);
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    public U get(Object key) {
        return map.get(key);
    }

    public U put(T key, U value) {
        return map.put(key, value);
    }

    public U remove(Object key) {
        return map.remove(key);
    }

    public void putAll(Map<? extends T,? extends U> t) {
        map.putAll(t);
    }

    public void clear() {
        map.clear();
    }

    public Set<T> keySet() {
        return map.keySet();
    }

    public Collection<U> values() {
        return map.values();
    }

    public Set<Entry<T, U>> entrySet() {
        return map.entrySet();
    }

    public boolean equals(Object o) {
        return map.equals(o);
    }

    public int hashCode() {
        return map.hashCode();
    }
}
