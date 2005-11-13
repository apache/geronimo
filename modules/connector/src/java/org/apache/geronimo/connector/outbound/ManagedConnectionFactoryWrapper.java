/**
 *
 * Copyright 2003-2005 The Apache Software Foundation
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

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
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
import org.apache.geronimo.gbean.DynamicGBean;
import org.apache.geronimo.gbean.DynamicGBeanDelegate;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.transaction.manager.NamedXAResource;
import org.apache.geronimo.transaction.manager.ResourceManager;
import org.apache.geronimo.management.geronimo.JCAManagedConnectionFactory;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;

/**
 * @version $Rev$ $Date$
 */
public class ManagedConnectionFactoryWrapper implements GBeanLifecycle, DynamicGBean, ResourceManager, JCAManagedConnectionFactory {

    private static final Log log = LogFactory.getLog(ManagedConnectionFactoryWrapper.class);

    private final String managedConnectionFactoryClass;
    private final String connectionFactoryInterface;
    private final String[] implementedInterfaces;
    private final String connectionFactoryImplClass;
    private final String connectionInterface;
    private final String connectionImplClass;

    private final Class[] allImplementedInterfaces;

    private final ResourceAdapterWrapper resourceAdapterWrapper;
    private final ConnectionManagerContainer connectionManagerContainer;

    private ManagedConnectionFactory managedConnectionFactory;

    private Object connectionFactory;

    private DynamicGBeanDelegate delegate;


    private boolean registered = false;
    private Object proxy;
    private ConnectorMethodInterceptor interceptor;
    private final Kernel kernel;
    private final String objectName;
    private final boolean isProxyable;
    private final ClassLoader classLoader;

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
        classLoader = null;
        resourceAdapterWrapper = null;
        connectionManagerContainer = null;
    }

    public ManagedConnectionFactoryWrapper(String managedConnectionFactoryClass,
                                           String connectionFactoryInterface,
                                           String[] implementedInterfaces,
                                           String connectionFactoryImplClass,
                                           String connectionInterface,
                                           String connectionImplClass,
                                           ResourceAdapterWrapper resourceAdapterWrapper,
                                           ConnectionManagerContainer connectionManagerContainer,
                                           Kernel kernel,
                                           String objectName,
                                           ClassLoader cl) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        this.managedConnectionFactoryClass = managedConnectionFactoryClass;
        this.connectionFactoryInterface = connectionFactoryInterface;
        this.implementedInterfaces = implementedInterfaces;
        this.connectionFactoryImplClass = connectionFactoryImplClass;
        this.connectionInterface = connectionInterface;
        this.connectionImplClass = connectionImplClass;

        allImplementedInterfaces = new Class[1 + implementedInterfaces.length];
        allImplementedInterfaces[0] = cl.loadClass(connectionFactoryInterface);
        for (int i = 0; i < implementedInterfaces.length; i++) {
            allImplementedInterfaces[i + 1] = cl.loadClass(implementedInterfaces[i]);

        }
        boolean mightBeProxyable = true;
        for (int i = 0; i < allImplementedInterfaces.length; i++) {
            Class implementedInterface = allImplementedInterfaces[i];
            if (!implementedInterface.isInterface()) {
                mightBeProxyable = false;
                break;
            }
        }
        isProxyable = mightBeProxyable;

        this.resourceAdapterWrapper = resourceAdapterWrapper;
        this.connectionManagerContainer = connectionManagerContainer;

        //set up that must be done before start
        classLoader = cl;
        Class clazz = cl.loadClass(managedConnectionFactoryClass);
        managedConnectionFactory = (ManagedConnectionFactory) clazz.newInstance();
        delegate = new DynamicGBeanDelegate();
        delegate.addAll(managedConnectionFactory);
        this.kernel = kernel;
        this.objectName = objectName;

    }

    public String getManagedConnectionFactoryClass() {
        return managedConnectionFactoryClass;
    }

    public String getConnectionFactoryInterface() {
        return connectionFactoryInterface;
    }

    public String[] getImplementedInterfaces() {
        return implementedInterfaces;
    }

    public String getConnectionFactoryImplClass() {
        return connectionFactoryImplClass;
    }

    public String getConnectionInterface() {
        return connectionInterface;
    }

    public String getConnectionImplClass() {
        return connectionImplClass;
    }

    public ResourceAdapterWrapper getResourceAdapterWrapper() {
        return resourceAdapterWrapper;
    }

    public ConnectionManagerContainer getConnectionManagerFactory() {
        return connectionManagerContainer;
    }

    public String getConnectionManager() {
        try {
            ObjectName mine = ObjectName.getInstance(objectName);
            Properties other = new Properties();
            other.setProperty(NameFactory.J2EE_APPLICATION, mine.getKeyProperty(NameFactory.J2EE_APPLICATION));
            other.setProperty(NameFactory.J2EE_SERVER, mine.getKeyProperty(NameFactory.J2EE_SERVER));
            other.setProperty(NameFactory.JCA_RESOURCE, mine.getKeyProperty(NameFactory.JCA_RESOURCE));
            other.setProperty(NameFactory.J2EE_TYPE, NameFactory.JCA_CONNECTION_MANAGER);
            other.setProperty(NameFactory.J2EE_NAME, mine.getKeyProperty(NameFactory.J2EE_NAME));
            return new ObjectName(mine.getDomain(), other).getCanonicalName();
        } catch (MalformedObjectNameException e) {
            log.error("Unable to construct ObjectName", e);
            return null;
        }
    }

    public void doStart() throws Exception {
        //register with resource adapter if not yet done
        if (!registered && (managedConnectionFactory instanceof ResourceAdapterAssociation)) {
            if (resourceAdapterWrapper == null) {
                throw new IllegalStateException("Managed connection factory expects to be registered with a ResourceAdapter, but there is no ResourceAdapter");
            }
            resourceAdapterWrapper.registerResourceAdapterAssociation((ResourceAdapterAssociation) managedConnectionFactory);
            registered = true;
            log.debug("Registered managedConnectionFactory with ResourceAdapter " + resourceAdapterWrapper.toString());
        }

        //create a new ConnectionFactory
        connectionFactory = connectionManagerContainer.createConnectionFactory(managedConnectionFactory);

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
    }

    public void doStop() {
        if (interceptor != null) {
            interceptor.setInternalProxy(null);
        }
        connectionFactory = null;
    }

    public void doFail() {
        doStop();
    }

    //DynamicGBean implementation
    public Object getAttribute(String name) throws Exception {
        Thread thread = Thread.currentThread();
        ClassLoader oldTCL = thread.getContextClassLoader();
        thread.setContextClassLoader(classLoader);
        try {
            return delegate.getAttribute(name);
        } finally {
            thread.setContextClassLoader(oldTCL);
        }
    }

    public void setAttribute(String name, Object value) throws Exception {
        Thread thread = Thread.currentThread();
        ClassLoader oldTCL = thread.getContextClassLoader();
        thread.setContextClassLoader(classLoader);
        try {
            delegate.setAttribute(name, value);
        } finally {
            thread.setContextClassLoader(oldTCL);
        }
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

    public ManagedConnectionFactory $getManagedConnectionFactory() {
        return managedConnectionFactory;
    }

    /**
     * Gets the config properties in the form of a map where the key is the
     * property name and the value is property type (as a String not a Class).
     */
    public Map getConfigProperties() {
        String[] props = delegate.getProperties();
        Map map = new HashMap();
        for (int i = 0; i < props.length; i++) {
            String prop = props[i];
            if(prop.equals("logWriter")) {
                continue;
            }
            map.put(prop, delegate.getPropertyType(prop));
        }
        return map;
    }

    public void setConfigProperty(String property, Object value) throws Exception {
        Class cls = delegate.getPropertyType(property);
        if(value != null && value instanceof String && !cls.getName().equals("java.lang.String")) {
            if(cls.isPrimitive()) {
                if(cls.equals(int.class)) {
                    cls = Integer.class;
                } else if(cls.equals(boolean.class)) {
                    cls = Boolean.class;
                } else if(cls.equals(float.class)) {
                    cls = Float.class;
                } else if(cls.equals(double.class)) {
                    cls = Double.class;
                } else if(cls.equals(long.class)) {
                    cls = Long.class;
                } else if(cls.equals(short.class)) {
                    cls = Short.class;
                } else if(cls.equals(byte.class)) {
                    cls = Byte.class;
                } else if(cls.equals(char.class)) {
                    cls = Character.class;
                }
            }
            //todo: what about value is null but type is primitive?
            Constructor con = cls.getConstructor(new Class[]{String.class});
            value = con.newInstance(new Object[]{value});
        }
        delegate.setAttribute(property, value);
    }

    public Object getConfigProperty(String property) throws Exception {
        return delegate.getAttribute(property);
    }

    //ResourceManager implementation
    public NamedXAResource getRecoveryXAResources() throws SystemException {
        try {
            return connectionManagerContainer.getRecoveryXAResource(managedConnectionFactory);
        } catch (ResourceException e) {
            throw (SystemException) new SystemException("Could not obtain recovery XAResource for managedConnectionFactory " + objectName).initCause(e);
        }
    }

    public void returnResource(NamedXAResource xaResource) {
        ((ConnectionManagerContainer.ReturnableXAResource) xaResource).returnConnection();
    }

    public String getObjectName() {
        return objectName;
    }

    public boolean isStateManageable() {
        return false;
    }

    public boolean isStatisticsProvider() {
        return false;
    }

    public boolean isEventProvider() {
        return false;
    }
}
