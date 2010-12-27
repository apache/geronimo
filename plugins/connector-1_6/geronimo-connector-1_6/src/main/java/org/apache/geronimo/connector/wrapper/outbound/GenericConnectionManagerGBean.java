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
package org.apache.geronimo.connector.wrapper.outbound;

import java.io.Externalizable;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;
import java.io.Serializable;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.security.auth.Subject;

import org.apache.geronimo.connector.outbound.GenericConnectionManager;
import org.apache.geronimo.connector.outbound.SubjectSource;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.PoolingSupport;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.TransactionSupport;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTracker;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.OsgiService;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.kernel.proxy.ProxyManager;
import org.apache.geronimo.management.geronimo.JCAManagedConnectionFactory;
import org.apache.geronimo.naming.ResourceSource;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.transaction.manager.RecoverableTransactionManager;
import org.osgi.framework.BundleContext;

/**
 * @version $Revision$
 */
@GBean(j2eeType = NameFactory.JCA_CONNECTION_MANAGER)
@OsgiService
public class GenericConnectionManagerGBean extends GenericConnectionManager implements ResourceSource<ResourceException>, GBeanLifecycle, Serializable, Externalizable {
    private Kernel kernel;
    private AbstractName abstractName;
    //externalizable format version
    private static final int VERSION = 1;

    private ManagedConnectionFactoryWrapper managedConnectionFactoryWrapper;

    public GenericConnectionManagerGBean(@ParamAttribute(name="transactionSupport") TransactionSupport transactionSupport,
                                         @ParamAttribute(name="pooling")PoolingSupport pooling,
                                         @ParamAttribute(name="containerManagedSecurity")boolean containerManagedSecurity,
                                         @ParamReference(name="ConnectionTracker", namingType = NameFactory.JCA_CONNECTION_TRACKER) ConnectionTracker connectionTracker,
                                         @ParamReference(name="TransactionManager", namingType = NameFactory.JTA_RESOURCE) RecoverableTransactionManager transactionManager,
                                         @ParamReference(name="ManagedConnectionFactory", namingType = NameFactory.JCA_MANAGED_CONNECTION_FACTORY) ManagedConnectionFactoryWrapper managedConnectionFactoryWrapper,
                                         @ParamSpecial(type=SpecialAttributeType.objectName) String objectName,
                                         @ParamSpecial(type=SpecialAttributeType.abstractName) AbstractName abstractName,
                                         @ParamSpecial(type=SpecialAttributeType.classLoader) ClassLoader classLoader,
                                         @ParamSpecial(type=SpecialAttributeType.bundleContext) BundleContext bundleContext,
                                         @ParamSpecial(type=SpecialAttributeType.kernel) Kernel kernel) {
        super(transactionSupport, pooling, getSubjectSource(containerManagedSecurity), connectionTracker, transactionManager, managedConnectionFactoryWrapper.getManagedConnectionFactory(), objectName, classLoader);
        this.kernel = kernel;
        this.abstractName = abstractName;
        this.managedConnectionFactoryWrapper = managedConnectionFactoryWrapper;
        doRecovery();

    }

    public GenericConnectionManagerGBean() {
        super();
    }

    public JCAManagedConnectionFactory getJCAManagedConnectionFactory() {
        return managedConnectionFactoryWrapper;
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

    @Override
    public Object $getResource() throws ResourceException {
        return createConnectionFactory();
    }

}
