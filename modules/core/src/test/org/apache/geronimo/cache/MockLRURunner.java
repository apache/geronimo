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

package org.apache.geronimo.cache;

import java.util.Set;
import java.util.HashSet;

/**
 * A mock object to test {@link LRUInstanceCache#run(LRURunner)} method
 * @version $Rev$ $Date$
 */
public class MockLRURunner implements LRURunner {
    private int maxInvocations = 0;
    private int invoked = 0;
    private Set elementsToRemove = new HashSet();
    private int elementsRemoved = 0;

    /**
     * Checks if the runner should continue to traverse the list.
     * According to the parameter of maximum interations set with {@link MockLRURunner#init(int)} a behaviour
     * of interrupted process can be imitated.
     * @return <code>true</code> if the runner should continue to traverse the list
     */
    public boolean shouldContinue() {
        return invoked < maxInvocations;
    }

    /**
     * Checks if the element was scheduled to be removed.
     * The number of this method invocations is counted and can be retreived later through {@link MockLRURunner#getInvoked()}
     * @param key the key of the element
     * @param value the value of the element
     * @return <code>true</code> if the element should be removed from the cache
     */
    public boolean shouldRemove(Object key, Object value) {
        invoked++;
        return elementsToRemove.contains(key);
    }

    /**
     * Removes the element from the internal list.
     * The number of this method invocations is counted and can be retreived later through {@link MockLRURunner#getElementsRemoved()}
     * @param key the key of the element
     * @param value the value of the element
     */
    public void remove(Object key, Object value) {
        elementsToRemove.remove(key);
        elementsRemoved++;
    }

    /**
     * Initializes the internal state in order for the object to be reused
     * @param maxInvocations the parameter of how many invocations to handle
     */
    public void init(int maxInvocations) {
        this.maxInvocations = maxInvocations;
        this.invoked = 0;
        this.elementsRemoved = 0;
    }

    /**
     * Schedules element to be removed
     * @param key the key of the element
     */
    public void addElementToRemove(Object key) {
        elementsToRemove.add(key);
    }

    /**
     * Returns the count of the elements it was asked to actually remove
     * @return the count of the elements removed
     */
    public int getElementsRemoved() {
        return elementsRemoved;
    }

    /**
     * Returns the count of how many times it was invoked to test if the element has to be removed
     * @return the count of how many times it was invoked to test if the element has to be removed
     */
    public int getInvoked() {
        return invoked;
    }
}
