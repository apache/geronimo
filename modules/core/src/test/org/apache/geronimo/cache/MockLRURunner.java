/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.cache;

import java.util.Set;
import java.util.HashSet;

/**
 * A mock object to test {@link LRUInstanceCache#run(LRURunner)} method
 * @version $Revision: 1.2 $ $Date: 2003/08/17 10:32:10 $
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
