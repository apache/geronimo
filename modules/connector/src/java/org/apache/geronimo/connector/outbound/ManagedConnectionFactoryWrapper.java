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

import javax.naming.NamingException;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapterAssociation;
import javax.management.ObjectName;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.connector.ResourceAdapterWrapper;
import org.apache.geronimo.connector.ConnectorMethodInterceptor;
import org.apache.geronimo.connector.outbound.security.ManagedConnectionFactoryListener;
import org.apache.geronimo.gbean.DynamicGBean;
import org.apache.geronimo.gbean.DynamicGBeanDelegate;
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanContext;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GConstructorInfo;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.naming.geronimo.GeronimoContextManager;
import org.apache.geronimo.kernel.KernelMBean;

/**
 *
 *
 * @version $Revision: 1.8 $ $Date: 2004/04/07 06:54:57 $
 *
 * */
public class ManagedConnectionFactoryWrapper implements GBean, DynamicGBean {

    private static final GBeanInfo GBEAN_INFO;
    private static final Log log = LogFactory.getLog(ManagedConnectionFactoryWrapper.class);

    private final Class managedConnectionFactoryClass;
    private final Class connectionFactoryInterface;
    private final Class connectionFactoryImplClass;
    private final Class connectionInterface;
    private final Class connectionImplClass;


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
    private final KernelMBean kernel;
    private final ObjectName selfName;

    //default constructor for enhancement proxy endpoint
    public ManagedConnectionFactoryWrapper() {
        managedConnectionFactoryClass = null;
        connectionFactoryInterface = null;
        connectionFactoryImplClass = null;
        connectionInterface = null;
        connectionImplClass = null;
        kernel = null;
        selfName = null;
    }

    public ManagedConnectionFactoryWrapper(
            Class managedConnectionFactoryClass,
            Class connectionFactoryInterface,
            Class connectionFactoryImplClass,
            Class connectionInterface,
            Class connectionImplClass,
            String globalJNDIName,
            ResourceAdapterWrapper resourceAdapterWrapper,
            ConnectionManagerFactory connectionManagerFactory,
            ManagedConnectionFactoryListener managedConnectionFactoryListener,
            KernelMBean kernel,
            ObjectName selfName) throws InstantiationException, IllegalAccessException {
        this.managedConnectionFactoryClass = managedConnectionFactoryClass;
        this.connectionFactoryInterface = connectionFactoryInterface;
        this.connectionFactoryImplClass = connectionFactoryImplClass;
        this.connectionInterface = connectionInterface;
        this.connectionImplClass = connectionImplClass;

        this.globalJNDIName = globalJNDIName;
        this.resourceAdapterWrapper = resourceAdapterWrapper;
        this.connectionManagerFactory = connectionManagerFactory;

        //set up that must be done before start
        managedConnectionFactory = (ManagedConnectionFactory) managedConnectionFactoryClass.newInstance();
        delegate = new DynamicGBeanDelegate();
        delegate.addAll(managedConnectionFactory);
        this.managedConnectionFactoryListener = managedConnectionFactoryListener;
        this.kernel = kernel;
        this.selfName = selfName;

    }

    public Class getManagedConnectionFactoryClass() {
        return managedConnectionFactoryClass;
    }

    public Class getConnectionFactoryInterface() {
        return connectionFactoryInterface;
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

    public void setGBeanContext(GBeanContext context) {
    }

    public void doStart() throws WaitingException, Exception {
        //register with resource adapter if not yet done
        if (!registered && (managedConnectionFactory instanceof ResourceAdapterAssociation)) {
            if (resourceAdapterWrapper == null) {
                throw new IllegalStateException("Managed connection factory expects to be registered with a ResourceAdapter, but there is no ResourceAdapter");
            }
            resourceAdapterWrapper.registerManagedConnectionFactory((ResourceAdapterAssociation)managedConnectionFactory);
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
        if (proxy == null) {
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(connectionFactoryInterface);
            enhancer.setCallbackType(net.sf.cglib.proxy.MethodInterceptor.class);
            enhancer.setUseFactory(false);//????
            interceptor = new ConnectorMethodInterceptor(kernel.getKernelName(), selfName);
            enhancer.setCallbacks(new Callback[] {interceptor});
            proxy = enhancer.create(new Class[0], new Object[0]);
        }
        //connect proxy
        interceptor.setInternalProxy(connectionFactory);
        //If a globalJNDIName is supplied, bind it.
        if (globalJNDIName != null) {
            GeronimoContextManager.bind(globalJNDIName, proxy);
            log.debug("Bound connection factory into global 'ger:' context at " + globalJNDIName);
        }

    }

    public void doStop() throws WaitingException {
        interceptor.setInternalProxy(null);
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

    public Object getProxy() {
        return proxy;
    }

    public Object getMethodInterceptor() {
        return interceptor;
    }

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(ManagedConnectionFactoryWrapper.class.getName());

        infoFactory.addAttribute("ManagedConnectionFactoryClass", true);
        infoFactory.addAttribute("ConnectionFactoryInterface", true);
        infoFactory.addAttribute("ConnectionFactoryImplClass", true);
        infoFactory.addAttribute("ConnectionInterface", true);
        infoFactory.addAttribute("ConnectionImplClass", true);
        infoFactory.addAttribute("SelfName", true);

        infoFactory.addAttribute("GlobalJNDIName", true);

        infoFactory.addOperation("getProxy");
        infoFactory.addOperation("getMethodInterceptor");

        infoFactory.addReference("ResourceAdapterWrapper", ResourceAdapterWrapper.class);
        infoFactory.addReference("ConnectionManagerFactory", ConnectionManagerFactory.class);
        infoFactory.addReference("ManagedConnectionFactoryListener", ManagedConnectionFactoryListener.class);
        infoFactory.addReference("Kernel", KernelMBean.class);
        infoFactory.setConstructor(new GConstructorInfo(
                new String[]{"ManagedConnectionFactoryClass", "ConnectionFactoryInterface", "ConnectionFactoryImplClass", "ConnectionInterface", "ConnectionImplClass",
                             "GlobalJNDIName", "ResourceAdapterWrapper", "ConnectionManagerFactory", "ManagedConnectionFactoryListener", "Kernel", "SelfName"},
                new Class[]{Class.class, Class.class, Class.class, Class.class, Class.class,
                            String.class, ResourceAdapterWrapper.class, ConnectionManagerFactory.class, ManagedConnectionFactoryListener.class, KernelMBean.class, ObjectName.class}));

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }


}
