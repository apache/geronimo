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
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogConfigurationException;
import org.apache.commons.logging.LogFactory;

/**
 * @version $Revision: 1.2 $ $Date: 2004/02/13 07:22:22 $
 */
public class GeronimoLogFactory extends LogFactory {
    private final static Object factoryLock = new Object();
    // todo this should use weak references
    private static final HashMap instancesByClassLoader = new HashMap();

    private static LogFactory logFactory = new BootstrapLogFactory();

    public GeronimoLogFactory() {
        System.out.println("Created Geronimo log factory");
    }

    public LogFactory getLogFactory() {
        synchronized (factoryLock) {
            return logFactory;
        }
    }

    public void setLogFactory(LogFactory logFactory) {
        // change the log factory
        this.logFactory = logFactory;

        // update all known logs to use instances of the new factory
        Set logs = getInstances();
        for (Iterator iterator = logs.iterator(); iterator.hasNext();) {
            GeronimoLog log = (GeronimoLog) iterator.next();
            log.setLog(logFactory.getInstance(log.getName()));
        }
    }

    public Set getInstances() {
        synchronized (factoryLock) {
            Set logs = new HashSet();
            for (Iterator iterator = instancesByClassLoader.values().iterator(); iterator.hasNext();) {
                Map instanceMap = ((Map) iterator.next());
                logs.addAll(instanceMap.values());

            }
            return logs;
        }
    }

    public Log getInstance(Class clazz) throws LogConfigurationException {
        synchronized (factoryLock) {
            return getInstance(clazz.getName());
        }
    }

    public Log getInstance(String name) throws LogConfigurationException {
        synchronized (factoryLock) {
            // get the instances for the context classloader
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            Map instances = (Map) instancesByClassLoader.get(contextClassLoader);
            if(instances == null) {
                instances = new HashMap();
                instancesByClassLoader.put(contextClassLoader, instances);
            }

            // get the log
            Log log = (Log) instances.get(name);
            if (log == null) {
                log = new GeronimoLog(name, logFactory.getInstance(name));
                instances.put(name, log);
            }
            return log;
        }
    }

    public void release() {
        synchronized (factoryLock) {
            for (Iterator maps = instancesByClassLoader.values().iterator(); maps.hasNext();) {
                Map instances = (Map) maps.next();
                for (Iterator logs = instances.values().iterator(); logs.hasNext();) {
                    GeronimoLog log = (GeronimoLog) logs.next();
                    log.setLog(null);

                }
            }
            instancesByClassLoader.clear();
        }
    }

    public Object getAttribute(String name) {
        synchronized (factoryLock) {
            return logFactory.getAttribute(name);
        }
    }

    public String[] getAttributeNames() {
        synchronized (factoryLock) {
            return logFactory.getAttributeNames();
        }
    }

    public  void removeAttribute(String name) {
        synchronized (factoryLock) {
            logFactory.removeAttribute(name);
        }
    }

    public void setAttribute(String name, Object value) {
        synchronized (factoryLock) {
            logFactory.setAttribute(name, value);
        }
    }
}

