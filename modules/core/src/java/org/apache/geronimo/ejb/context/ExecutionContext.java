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
package org.apache.geronimo.ejb.context;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import javax.transaction.Synchronization;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.lock.LockContext;

/**
 *
 *
 * @version $Revision: 1.4 $ $Date: 2003/08/23 09:07:11 $
 */
public abstract class ExecutionContext {
    private static Log log = LogFactory.getLog(ExecutionContext.class);

    private static ThreadLocal context = new ThreadLocal() {
        protected Object initialValue() {
            return new LinkedList();
        }
    };

    private static LinkedList getList() {
        return (LinkedList) context.get();
    }

    public static ExecutionContext getContext() {
        LinkedList list = getList();
        return list.isEmpty() ? null : (ExecutionContext) list.getFirst();
    }

    public static void push(ExecutionContext newContext) {
        assert newContext != null;
        getList().addFirst(newContext);
    }

    public static void pop(ExecutionContext newContext) {
        assert newContext != null;
        assert getContext() == newContext;
        getList().removeFirst();
    }

    private final Map values = new IdentityHashMap();
    protected final LinkedList listeners = new LinkedList();
    private final LockContext lockContext;
    private boolean readOnly = false;

    protected ExecutionContext(LockContext lockContext) {
        this.lockContext = lockContext;
        register(lockContext);
    }

    public LockContext getLockContext() {
        return lockContext;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public synchronized void register(Synchronization listener) {
        listeners.add(listener);
    }

    protected void runBeforeCompletion() {
        // do beforeCompletion in order that they registered
        for (Iterator i = listeners.iterator(); i.hasNext();) {
            Synchronization synchronization = (Synchronization) i.next();
            synchronization.beforeCompletion();
        }
    }

    protected void runAfterCompletion(int status) {
        // do afterCompletion in reverse order
        while (!listeners.isEmpty()) {
            Synchronization synchronization = (Synchronization) listeners.removeLast();
            try {
                synchronization.afterCompletion(status);
            } catch (RuntimeException e) {
                log.error("Error in afterCompletion", e);
            } catch (Error e) {
                log.error("Error in afterCompletion", e);
            }
        }
    }

    public void normalTermination() {
    }

    public void abnormalTermination(Throwable t) {
    }

    public Object put(Object key, Object value) {
        return values.put(key, value);
    }

    public Object get(Object key) {
        return values.get(key);
    }

    public Object remove(Object key) {
        return values.remove(key);
    }
}
