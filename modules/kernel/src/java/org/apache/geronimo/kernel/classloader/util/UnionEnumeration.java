/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.kernel.classloader.util;

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @version $Rev$ $Date$
 */
public final class UnionEnumeration implements Enumeration {
    private final LinkedList enumerations = new LinkedList();

    public UnionEnumeration(List enumerations) {
        this.enumerations.addAll(enumerations);
    }

    public UnionEnumeration(Enumeration first, Enumeration second) {
        if (first == null) throw new NullPointerException("first is null");
        if (second == null) throw new NullPointerException("second is null");

        enumerations.add(first);
        enumerations.add(second);
    }

    public boolean hasMoreElements() {
        while (!enumerations.isEmpty()) {
            Enumeration enumeration = (Enumeration) enumerations.getFirst();
            if (enumeration.hasMoreElements()) {
                return true;
            }
            enumerations.removeFirst();
        }
        return false;
    }

    public Object nextElement() {
        while (!enumerations.isEmpty()) {
            Enumeration enumeration = (Enumeration) enumerations.getFirst();
            if (enumeration.hasMoreElements()) {
                return enumeration.nextElement();
            }
            enumerations.removeFirst();
        }
        throw new NoSuchElementException();
    }
}
