/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
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

import javax.management.ObjectName;
import javax.naming.NamingException;
import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapterAssociation;
import javax.transaction.SystemException;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.connector.ConnectorMethodInterceptor;
import org.apache.geronimo.connector.ResourceAdapterWrapper;
import org.apache.geronimo.connector.outbound.security.ManagedConnectionFactoryListener;
import org.apache.geronimo.gbean.DynamicGBean;
import org.apache.geronimo.gbean.DynamicGBeanDelegate;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.naming.geronimo.GeronimoContextManager;
import org.apache.geronimo.transaction.manager.NamedXAResource;
import org.apache.geronimo.transaction.manager.ResourceManager;

/**
 * @version $Rev$ $Date$
 */
public class ManagedConnectionFactoryWrapper implements GBeanLifecycle, DynamicGBean, ResourceManager {

    private static final GBeanInfo GBEAN_INFO;
    private static final Log log = LogFactory.getLog(ManagedConnectionFactoryWrapper.class);

    private final Class managedConnectionFactoryClass;
    private final Class connectionFactoryInterface;
    private final Class[] implementedInterfaces;
    private final Class connectionFactoryImplClass;
    private final Class connectionInterface;
    private final Class connectionImplClass;

    private final Class[] allImplementedInterfaces;

    private String globalJNDIName;

    private ResourceAdapterWrapper resourceAdapterWrapper;
    private ConnectionManagerFactory connectionManagerFactory;
    private ManagedConnectionFactoryListener managedConnectionFactoryListener;

    private ManagedConnectionFactory managedConnectionFactory;

    private Object connectionFactory;

    private DynamicGBeanDelegate delegate;


    private boolean registered = false;
    private Object proxy;
    private ConnectorMethodInterceptor interceptor;
    private final Kernel kernel;
    private final String objectName;
    private final boolean isProxyable;

    //default constructor for enhancement proxy endpoint
    public ManagedConnectionFactoryWrapper() {
        managedConnectionFactoryClass = null;
        connectionFactoryInterface = null;
        implementedInterfaces = null;
        connectionFactoryImplClass = null;
        connectionInterface = null;
        connectionImplClass = null;
        kernel = null;
        objectName = null;
        allImplementedInterfaces = null;
        isProxyable = false;
    }

    public ManagedConnectionFactoryWrapper(Class managedConnectionFactoryClass,
            Class connectionFactoryInterface,
            Class[] implementedInterfaces,
            Class connectionFactoryImplClass,
            Class connectionInterface,
            Class connectionImplClass,
            String globalJNDIName,
            ResourceAdapterWrapper resourceAdapterWrapper,
            ConnectionManagerFactory connectionManagerFactory,
            ManagedConnectionFactoryListener managedConnectionFactoryListener,
            Kernel kernel,
            String objectName) throws InstantiationException, IllegalAccessException {
        this.managedConnectionFactoryClass = managedConnectionFactoryClass;
        this.connectionFactoryInterface = connectionFactoryInterface;
        this.implementedInterfaces = implementedInterfaces;
        this.connectionFactoryImplClass = connectionFactoryImplClass;
        this.connectionInterface = connectionInterface;
        this.connectionImplClass = connectionImplClass;

        allImplementedInterfaces = new Class[1 + implementedInterfaces.length];
        allImplementedInterfaces[0]= connectionFactoryInterface;
        System.arraycopy(implementedInterfaces, 0, allImplementedInterfaces, 1, implementedInterfaces.length);
        boolean mightBeProxyable = true;
        for (int i = 0; i < implementedInterfaces.length; i++) {
            Class implementedInterface = implementedInterfaces[i];
            if (!implementedInterface.isInterface()) {
                mightBeProxyable = false;
                break;
            }
        }
        isProxyable = mightBeProxyable;

        this.globalJNDIName = globalJNDIName;
        this.resourceAdapterWrapper = resourceAdapterWrapper;
        this.connectionManagerFactory = connectionManagerFactory;

        //set up that must be done before start
        managedConnectionFactory = (ManagedConnectionFactory) managedConnectionFactoryClass.newInstance();
        delegate = new DynamicGBeanDelegate();
        delegate.addAll(managedConnectionFactory);
        this.managedConnectionFactoryListener = managedConnectionFactoryListener;
        this.kernel = kernel;
        this.objectName = objectName;

    }

    public Class getManagedConnectionFactoryClass() {
        return managedConnectionFactoryClass;
    }

    public Class getConnectionFactoryInterface() {
        return connectionFactoryInterface;
    }

    public Class[] getImplementedInterfaces() {
        return implementedInterfaces;
    }

    public Class getConnectionFactoryImplClass() {
        return connectionFactoryImplClass;
    }

    public Class getConnectionInterface() {
        return connectionInterface;
    }

    public Class getConnectionImplClass() {
        return connectionImplClass;
    }

    public String getGlobalJNDIName() {
        return globalJNDIName;
    }

    public ResourceAdapterWrapper getResourceAdapterWrapper() {
        return resourceAdapterWrapper;
    }

    public void setResourceAdapterWrapper(ResourceAdapterWrapper resourceAdapterWrapper) {
        this.resourceAdapterWrapper = resourceAdapterWrapper;
    }

    public ConnectionManagerFactory getConnectionManagerFactory() {
        return connectionManagerFactory;
    }

    public void setConnectionManagerFactory(ConnectionManagerFactory connectionManagerFactory) {
        this.connectionManagerFactory = connectionManagerFactory;
    }

    public void doStart() throws WaitingException, Exception {
        //register with resource adapter if not yet done
        if (!registered && (managedConnectionFactory instanceof ResourceAdapterAssociation)) {
            if (resourceAdapterWrapper == null) {
                throw new IllegalStateException("Managed connection factory expects to be registered with a ResourceAdapter, but there is no ResourceAdapter");
            }
            resourceAdapterWrapper.registerResourceAdapterAssociation((ResourceAdapterAssociation) managedConnectionFactory);
            registered = true;
            log.debug("Registered managedConnectionFactory with ResourceAdapter " + resourceAdapterWrapper.toString());
        }
        //set up login if present
        if (managedConnectionFactoryListener != null) {
            managedConnectionFactoryListener.setManagedConnectionFactory(managedConnectionFactory);
        }

        //create a new ConnectionFactory
        connectionFactory = connectionManagerFactory.createConnectionFactory(managedConnectionFactory);

        //build proxy
        if (isProxyable) {
            Enhancer enhancer = new Enhancer();
            enhancer.setInterfaces(allImplementedInterfaces);
            enhancer.setCallbackType(net.sf.cglib.proxy.MethodInterceptor.class);
            enhancer.setUseFactory(false);//????
            interceptor = new ConnectorMethodInterceptor(kernel.getKernelName(), ObjectName.getInstance(objectName));
            enhancer.setCallbacks(new Callback[]{interceptor});
            proxy = enhancer.create(new Class[0], new Object[0]);
        } else {
            proxy = connectionFactory;
        }

        //connect proxy
        if (interceptor != null) {
            interceptor.setInternalProxy(connectionFactory);
        }
        //If a globalJNDIName is supplied, bind it.
        if (globalJNDIName != null) {
            GeronimoContextManager.bind(globalJNDIName, proxy);
            log.debug("Bound connection factory into global 'ger:' context at " + globalJNDIName);
        }

    }

    public void doStop() {
        if (interceptor != null) {
            interceptor.setInternalProxy(null);
        }
        //tear down login if present
        if (managedConnectionFactoryListener != null) {
            managedConnectionFactoryListener.setManagedConnectionFactory(null);
        }
        connectionFactory = null;
        if (globalJNDIName != null) {
            try {
                GeronimoContextManager.unbind(globalJNDIName);
            } catch (NamingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void doFail() {
        doStop();
    }

    //DynamicGBean implementation
    public Object getAttribute(String name) throws Exception {
        return delegate.getAttribute(name);
    }

    public void setAttribute(String name, Object value) throws Exception {
        delegate.setAttribute(name, value);
    }

    public Object invoke(String name, Object[] arguments, String[] types) throws Exception {
        //we have no dynamic operations.
        return null;
    }

    public Object $getResource() {
        return proxy;
    }

    public Object $getConnectionFactory() {
        return connectionFactory;
    }

    //ResourceManager implementation
    public NamedXAResource getRecoveryXAResources() throws SystemException {
        try {
            return connectionManagerFactory.getRecoveryXAResource(managedConnectionFactory);
        } catch (ResourceException e) {
            throw (SystemException)new SystemException("Could not obtain recovery XAResource for managedConnectionFactory " + objectName).initCause(e);
        }
    }

    public void returnResource(NamedXAResource xaResource) {
        ((ConnectionManagerFactory.ReturnableXAResource)xaResource).returnConnection();
    }


    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(ManagedConnectionFactoryWrapper.class);

        infoFactory.addAttribute("managedConnectionFactoryClass", Class.class, true);
        infoFactory.addAttribute("connectionFactoryInterface", Class.class, true);
        infoFactory.addAttribute("implementedInterfaces", Class[].class, true);
        infoFactory.addAttribute("connectionFactoryImplClass", Class.class, true);
        infoFactory.addAttribute("connectionInterface", Class.class, true);
        infoFactory.addAttribute("connectionImplClass", Class.class, true);
        infoFactory.addAttribute("globalJNDIName", String.class, true);
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addAttribute("objectName", String.class, false);

        infoFactory.addOperation("$getResource");
        infoFactory.addOperation("$getConnectionFactory");

        infoFactory.addInterface(ResourceManager.class);

        infoFactory.addReference("ResourceAdapterWrapper", ResourceAdapterWrapper.class);
        infoFactory.addReference("ConnectionManagerFactory", ConnectionManagerFactory.class);
        infoFactory.addReference("ManagedConnectionFactoryListener", ManagedConnectionFactoryListener.class);

        infoFactory.setConstructor(new String[]{
            "managedConnectionFactoryClass",
            "connectionFactoryInterface",
            "implementedInterfaces",
            "connectionFactoryImplClass",
            "connectionInterface",
            "connectionImplClass",
            "globalJNDIName",
            "ResourceAdapterWrapper",
            "ConnectionManagerFactory",
            "ManagedConnectionFactoryListener",
            "kernel",
            "objectName"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
