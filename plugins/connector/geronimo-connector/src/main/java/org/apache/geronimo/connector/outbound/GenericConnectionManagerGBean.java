/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.connector.outbound;

import java.io.Externalizable;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;
import java.io.Serializable;

import javax.resource.spi.ConnectionManager;
import javax.security.auth.Subject;

import org.apache.geronimo.connector.outbound.connectionmanagerconfig.PoolingSupport;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.TransactionSupport;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTracker;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.kernel.proxy.ProxyManager;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.transaction.manager.RecoverableTransactionManager;

/**
 * @version $Revision$
 */
public class GenericConnectionManagerGBean extends GenericConnectionManager implements GBeanLifecycle, Serializable, Externalizable {
    private Kernel kernel;
    private AbstractName abstractName;
    //externalizable format version
    private static final int VERSION = 1;

    public GenericConnectionManagerGBean() {
        super();
        kernel = null;
        abstractName = null;
    }

    public GenericConnectionManagerGBean(TransactionSupport transactionSupport,
                                         PoolingSupport pooling,
                                         boolean containerManagedSecurity,
                                         ConnectionTracker connectionTracker,
                                         RecoverableTransactionManager transactionManager,
                                         String objectName,
                                         AbstractName abstractName,
                                         ClassLoader classLoader,
                                         Kernel kernel) {
        super(transactionSupport, pooling, getSubjectSource(containerManagedSecurity), connectionTracker, transactionManager, objectName, classLoader);
        this.kernel = kernel;
        this.abstractName = abstractName;
    }

    public ConnectionManager getConnectionManager() {
        ConnectionManager unproxied = super.getConnectionManager();
        ProxyManager pm = kernel.getProxyManager();
        if (pm.isProxy(unproxied)) {
            return unproxied;
        } else {
            return (ConnectionManager) pm.createProxy(kernel.getAbstractNameFor(unproxied), unproxied.getClass().getClassLoader());
        }
    }

    private static SubjectSource getSubjectSource(boolean containerManagedSecurity) {
        if (containerManagedSecurity) {
            return new SubjectSource() {
                public Subject getSubject() {
                    return ContextManager.getNextCaller();
                }
            };
        } else {
            return null;
        }
    }

    private Object readResolve() throws ObjectStreamException {
        try {
            return kernel.getGBean(abstractName);
        } catch (GBeanNotFoundException e) {
            throw (ObjectStreamException) new InvalidObjectException("Could not locate connection manager gbean").initCause(e);
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(VERSION);
        out.writeObject(kernel.getKernelName());
        out.writeObject(abstractName);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int version = in.readInt();
        if (version != VERSION) {
            throw new IOException("Wrong version, expected " + VERSION + ", got: " + version);
        }
        String kernelName = (String) in.readObject();
        kernel = KernelRegistry.getKernel(kernelName);
        if (kernel == null) {
            kernel = KernelRegistry.getSingleKernel();
        }
        if (kernel == null) {
            throw new IOException("No kernel named: '" + kernelName + "' found");
        }
        abstractName = (AbstractName) in.readObject();
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(GenericConnectionManagerGBean.class, AbstractConnectionManagerGBean.GBEAN_INFO);

        infoBuilder.addAttribute("transactionSupport", TransactionSupport.class, true);
        infoBuilder.addAttribute("pooling", PoolingSupport.class, true);
        infoBuilder.addAttribute("containerManagedSecurity", Boolean.TYPE, true);

        infoBuilder.addAttribute("objectName", String.class, false);
        infoBuilder.addAttribute("abstractName", AbstractName.class, false);
        infoBuilder.addAttribute("classLoader", ClassLoader.class, false);
        infoBuilder.addAttribute("kernel", Kernel.class, false);

        infoBuilder.addReference("ConnectionTracker", ConnectionTracker.class, NameFactory.JCA_CONNECTION_TRACKER);
        infoBuilder.addReference("TransactionManager", RecoverableTransactionManager.class, NameFactory.JTA_RESOURCE);


        infoBuilder.setConstructor(new String[]{
                "transactionSupport",
                "pooling",
                "containerManagedSecurity",
                "ConnectionTracker",
                "TransactionManager",
                "objectName",
                "abstractName",
                "classLoader",
                "kernel"
        });

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
