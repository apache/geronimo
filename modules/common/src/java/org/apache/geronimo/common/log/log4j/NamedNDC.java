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
package org.apache.geronimo.common.log.log4j;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Provides named nested diagnotic contexts.
 *
 * @version $Revision: 1.1 $ $Date: 2003/08/27 10:08:45 $
 */
public final class NamedNDC
{
    /**
     * Mapping from names to NamedNDCs.
     * Currently there is no way to remove a NamedNCD once created, so be sure you really
     * want a new NDC before creating one.
     * @todo make this a weak-valued map
     */
    private static final Map contexts = new HashMap();

    /**
     * Gets the NamedNDC by name, or creates a new one of one does not already exist.
     * @param name the name of the desired NamedNDC
     * @return the existing NamedNDC or a new one
     */
    public static NamedNDC getNamedNDC(String name) {
        synchronized (contexts) {
            NamedNDC context = (NamedNDC) contexts.get(name);
            if (context == null) {
                context = new NamedNDC();
                contexts.put(name, context);
            }
            return context;
        }
    }

    private final ListThreadLocal listThreadLocal = new ListThreadLocal();

    private NamedNDC() {
    }

    public void push(Object value) {
        listThreadLocal.getList().addLast(value);
    }

    public Object get() {
        LinkedList list = listThreadLocal.getList();
        if (list.isEmpty()) {
            return null;
        }
        return list.getLast();
    }

    public Object pop() {
        LinkedList list = listThreadLocal.getList();
        if (list.isEmpty()) {
            return null;
        }
        return list.removeLast();
    }

    public void clear() {
        listThreadLocal.getList().clear();
    }

    private final static class ListThreadLocal extends ThreadLocal {
        public LinkedList getList() {
            return (LinkedList) get();
        }

        protected Object initialValue() {
            return new LinkedList();
        }
    }
}
