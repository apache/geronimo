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
package org.apache.geronimo.kernel.log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogConfigurationException;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.LogFactoryImpl;

/**
 * @version $Revision: 1.1 $ $Date: 2004/02/11 03:14:11 $
 */
public class GeronimoLogFactory extends LogFactory {
    private LogFactory logFactory = new LogFactoryImpl();
    private final HashMap instances = new HashMap();

    public synchronized LogFactory getLogFactory() {
        return logFactory;
    }

    public void setLogFactory(LogFactory logFactory) {
        Set logs;
        synchronized (this) {
            this.logFactory = logFactory;
            logs = new HashSet(instances.values());
        }

        for (Iterator iterator = logs.iterator(); iterator.hasNext();) {
            GeronimoLog log = (GeronimoLog) iterator.next();
            log.setLog(logFactory.getInstance(log.getName()));
        }
    }

    public synchronized Set getInstances() {
        return new HashSet(instances.values());
    }

    public synchronized Log getInstance(Class clazz) throws LogConfigurationException {
        return getInstance(clazz.getName());
    }

    public synchronized Log getInstance(String name) throws LogConfigurationException {
        ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            Log instance = (Log) instances.get(name);
            if (instance == null) {
                instance = new GeronimoLog(name, logFactory.getInstance(name));
                instances.put(name, instance);
            }
            return instance;
        } finally {
            Thread.currentThread().setContextClassLoader(oldCL);
        }
    }

    public synchronized void release() {
        for (Iterator iterator = instances.values().iterator(); iterator.hasNext();) {
            GeronimoLog log = (GeronimoLog) iterator.next();
            log.setLog(null);
        }
        instances.clear();
    }

    public synchronized Object getAttribute(String name) {
        return logFactory.getAttribute(name);
    }

    public synchronized String[] getAttributeNames() {
        return logFactory.getAttributeNames();
    }

    public synchronized void removeAttribute(String name) {
        logFactory.removeAttribute(name);
    }

    public synchronized void setAttribute(String name, Object value) {
        logFactory.setAttribute(name, value);
    }
}

