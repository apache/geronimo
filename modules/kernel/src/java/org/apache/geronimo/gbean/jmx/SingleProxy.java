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
package org.apache.geronimo.gbean.jmx;

import java.util.HashSet;
import java.util.Set;
import javax.management.ObjectName;

import org.apache.geronimo.kernel.jmx.InterfaceCallbackFilter;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.gbean.jmx.Proxy;
import org.apache.geronimo.gbean.jmx.ProxyMethodInterceptor;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.SimpleCallbacks;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/01/14 22:16:38 $
 */
public class SingleProxy implements Proxy {
    /**
     * The GBeanMBean to which this proxy belongs.
     */
    private final GBeanMBean gmbean;

    /**
     * Name of this proxy.
     */
    private final String name;

    /**
     * A set of all targets matching the
     */
    private final Set targets = new HashSet();

    /**
     * Proxy implementation held by the component
     */
    private final Object proxy;

    /**
     * The interceptor for the proxy instance
     */
    private ProxyMethodInterceptor methodInterceptor;

    public SingleProxy(GBeanMBean gmbean, String name, Class type) {
        this.gmbean = gmbean;
        this.name = name;
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(Object.class);
        enhancer.setInterfaces(new Class[]{type});
        enhancer.setCallbackFilter(new InterfaceCallbackFilter(type));
        enhancer.setCallbacks(new SimpleCallbacks());
        enhancer.setClassLoader(type.getClassLoader());
        Factory factory = enhancer.create();
        methodInterceptor = new ProxyMethodInterceptor(type);
        proxy = factory.newInstance(methodInterceptor);
    }

    public Object getProxy() {
        return proxy;
    }

    public Set getTargets() {
        return targets;
    }

    public synchronized void addTarget(ObjectName target) {
        // if this is a new target...
        if (!targets.contains(target)) {
            targets.add(target);

            // if we are running, we now have two valid targets, which is an illegal state so we need to fail
            if (gmbean.getStateInstance() == State.RUNNING) {
                gmbean.fail();
            }
        }
    }

    public synchronized void removeTarget(ObjectName target) {
        // if this is a new target...
        if (!targets.contains(target)) {
            targets.remove(target);

            // if we are running, we will no longer have a valid target, which is an illegal state so we need to fail
            if (gmbean.getStateInstance() == State.RUNNING) {
                gmbean.fail();
            }
        }
    }

    public synchronized void start() throws WaitingException {
        //
        // We must have exactally one running target
        //
        if (targets.size() == 0) {
            throw new WaitingException("No targets are running: endpointName=" + name);
        } else if (targets.size() > 1) {
            throw new WaitingException("More then one targets are running: endpointName=" + name);
        }

        methodInterceptor.connect(gmbean.getServer(), (ObjectName) targets.iterator().next());
    }

    public synchronized void stop() {
        methodInterceptor.disconnect();
    }
}
