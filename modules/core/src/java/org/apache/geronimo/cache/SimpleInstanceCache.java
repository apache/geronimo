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

import java.util.HashMap;

/**
 * This is a very simple implementation of InstanceCache designed for raw flat
 * out speed.  It does not directly support passivation or have any storage
 * limits.
 *
 *
 * @version $Revision: 1.1 $ $Date: 2003/08/10 20:43:58 $
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
            // assert (inactive.remove(key) == null);
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

