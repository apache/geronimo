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
import java.util.Collections;
import java.util.Iterator;

/**
 * A wrapper around a reference collection to simulate a 0..1 reference
 */
public class SingleElementCollection<T> {

    private final Collection<T> collection;

    public SingleElementCollection(T element) {
        if (element == null) {
            collection = Collections.emptySet();
        } else {
            collection = Collections.singleton(element);
        }
    }

    public SingleElementCollection(Collection<T> collection) {
        if (collection == null) {
            collection = Collections.emptySet();
        }

        this.collection = collection;
    }

    public T getElement() {
        if (collection.isEmpty()) {
            return null;
        }
        if (collection.size() > 1) {
            throw new IllegalStateException("More than one element: " + collection);
        }
        Iterator<T> it = collection.iterator();
        return it.next();
    }

}
