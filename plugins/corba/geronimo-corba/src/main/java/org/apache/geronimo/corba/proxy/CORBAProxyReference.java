/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.corba.proxy;

import java.net.URI;
import javax.naming.NameNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.naming.reference.ConfigurationAwareReference;


/**
 * @version $Revision: 451417 $ $Date: 2006-09-29 13:13:22 -0700 (Fri, 29 Sep 2006) $
 */
public final class CORBAProxyReference extends ConfigurationAwareReference {

    private final static Logger log = LoggerFactory.getLogger(CORBAProxyReference.class);

    private final URI nsCorbaloc;
    private final String objectName;
    private final String home;

    public CORBAProxyReference(Artifact[] configId, AbstractNameQuery abstractNameQuery, URI nsCorbaloc, String objectName, String home) {
        super(configId, abstractNameQuery);
        this.nsCorbaloc = nsCorbaloc;
        this.objectName = objectName;
        this.home = home;
        if (log.isDebugEnabled()) {
            log.debug("<init> " + nsCorbaloc.toString() + ", " + objectName + ", " + abstractNameQuery + ", " + home);
        }
    }

    public String getClassName() {
        return home;
    }

    public Object getContent() throws NameNotFoundException {

        if (log.isDebugEnabled()) {
            log.debug("Obtaining home from " + nsCorbaloc.toString() + ", " + objectName + ", " + abstractNameQueries + ", " + home);
        }
        AbstractName containerName;
        try {
            containerName = resolveTargetName();
        } catch (GBeanNotFoundException e) {
            throw (NameNotFoundException) new NameNotFoundException("Could not resolve gbean from name query: " + abstractNameQueries).initCause(e);
        }
        Kernel kernel = getKernel();
        Object proxy;
        try {
            //TODO configid objectname might well be wrong kind of thing.
            proxy = kernel.invoke(containerName, "getHome", new Object[]{nsCorbaloc, objectName}, new String[]{URI.class.getName(), String.class.getName()});
        } catch (Exception e) {
            log.error("Could not get proxy from " + containerName, e);
            throw (IllegalStateException) new IllegalStateException("Could not get proxy").initCause(e);
        }
        if (proxy == null) {
            log.error("Proxy not returned from " + containerName);
            throw new IllegalStateException("Proxy not returned. Target " + containerName + " not started");
        }
        if (!org.omg.CORBA.Object.class.isAssignableFrom(proxy.getClass())) {
            log.error("Proxy not an instance of expected class org.omg.CORBA.Object from " + containerName);
            throw new ClassCastException("Proxy not an instance of expected class org.omg.CORBA.Object");
        }
        return proxy;
    }
}
