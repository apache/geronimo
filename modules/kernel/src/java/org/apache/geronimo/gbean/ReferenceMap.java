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
package org.apache.geronimo.gbean;

import java.util.*;

public class ReferenceMap implements Map, ReferenceCollectionListener {
    private final ReferenceCollection collection;
    private final Map map;
    private final Key key;

    /**
     * Constructs the ReferenceMap using a new instance of
     * HashMap as the internal map.
     *
     * @param collection Must be an instance of ReferenceCollection
     * @param map The map instance to which references will be added/removed
     * @param key
     */
    public ReferenceMap(Collection collection, Map map, Key key) {
        this.collection = (ReferenceCollection) collection;
        this.map = map;
        this.key = key;
        for (Iterator iterator = this.collection.iterator(); iterator.hasNext();) {
            Object object = iterator.next();
            map.put(key.getKey(object), object);
        }
        this.collection.addReferenceCollectionListener(this);
    }

    /**
     * Constructs the ReferenceMap using a new instance of
     * HashMap as the internal map.
     *
     * @param collection Must be an instance of ReferenceCollection
     * @param key
     */
    public ReferenceMap(Collection collection, Key key) {
        this(collection, new HashMap(), key);
    }

    public void memberAdded(ReferenceCollectionEvent event) {
        map.put(key.getKey(event.getMember()), event.getMember());
    }

    public void memberRemoved(ReferenceCollectionEvent event) {
        map.remove(key.getKey(event.getMember()));
    }

    public interface Key {
        public Object getKey(Object object);
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

    public Object get(Object key) {
        return map.get(key);
    }

    public Object put(Object key, Object value) {
        return map.put(key, value);
    }

    public Object remove(Object key) {
        return map.remove(key);
    }

    public void putAll(Map t) {
        map.putAll(t);
    }

    public void clear() {
        map.clear();
    }

    public Set keySet() {
        return map.keySet();
    }

    public Collection values() {
        return map.values();
    }

    public Set entrySet() {
        return map.entrySet();
    }

    public boolean equals(Object o) {
        return map.equals(o);
    }

    public int hashCode() {
        return map.hashCode();
    }
}
