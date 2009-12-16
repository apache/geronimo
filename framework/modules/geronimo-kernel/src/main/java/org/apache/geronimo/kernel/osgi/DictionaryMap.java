/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.geronimo.kernel.osgi;

import java.util.AbstractMap;
import java.util.Dictionary;
import java.util.Set;

/**
 * Simple wrapper that exposes {@link Dictionary} class as a {@link Map}. 
 * 
 * @version $Rev$, $Date$
 */
public class DictionaryMap extends AbstractMap {

    private final Dictionary dictionary;

    public DictionaryMap(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    @Override
    public Object get(Object key) {
        return dictionary.get(key);
    }

    @Override
    public Object put(Object key, Object value) {
        return dictionary.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return dictionary.remove(key);
    }

    @Override
    public int size() {
        return dictionary.size();
    }

    @Override
    public boolean isEmpty() {
        return dictionary.isEmpty();
    }

    @Override
    public Set entrySet() {
        // TODO: implement 
        throw new UnsupportedOperationException();
    }

    @Override
    public Set keySet() {
        // TODO: implement 
        throw new UnsupportedOperationException();
    }
}
