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

package org.apache.geronimo.connector;

import javax.management.ObjectName;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import org.apache.geronimo.gbean.DynamicGBean;
import org.apache.geronimo.gbean.DynamicGBeanDelegate;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.kernel.Kernel;

/**
 * Wrapper around AdminObject that exposes its config-properties as GBeanAttributes and
 * supplies a disconnectable proxy to bind in jndi.
 *
 * @version $Revision: 1.10 $ $Date: 2004/06/12 18:43:31 $
 */
public class AdminObjectWrapper implements GBeanLifecycle, DynamicGBean {

    private final Class adminObjectInterface;
    private final Class adminObjectClass;

    private final DynamicGBeanDelegate delegate;
    private final Object adminObject;
    private final Kernel kernel;
    private final String objectName;

    private ConnectorMethodInterceptor interceptor;
    private Object proxy;

    /**
     * Default constructor required when a class is used as a GBean Endpoint.
     */
    public AdminObjectWrapper() {
        adminObjectInterface = null;
        adminObjectClass = null;
        adminObject = null;
        delegate = null;
        kernel = null;
        objectName = null;
    }

    /**
     * Normal managed constructor.
     *
     * @param adminObjectInterface Interface the proxy will implement.
     * @param adminObjectClass Class of admin object to be wrapped.
     * @param kernel name is used so proxy can find correct kernel.
     * @param objectName is used by proxy to find this gbean to reconnect to.
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public AdminObjectWrapper(final Class adminObjectInterface,
                              final Class adminObjectClass,
                              final Kernel kernel,
                              final String objectName) throws IllegalAccessException, InstantiationException {
        this.adminObjectInterface = adminObjectInterface;
        this.adminObjectClass = adminObjectClass;
        adminObject = adminObjectClass.newInstance();
        delegate = new DynamicGBeanDelegate();
        delegate.addAll(adminObject);
        this.kernel = kernel;
        this.objectName = objectName;
    }

    /**
     * Returns class of wrapped AdminObject.
     * @return class of wrapped AdminObject
     */
    public Class getAdminObjectClass() {
        return adminObjectClass;
    }

    /**
     * Returns disconnectable proxy for binding in jndi.
     * @return proxy implementing adminObjectInterface.
     */
    public Object getProxy() {
        return proxy;
    }

    /**
     * Returns the MethodInterceptor the proxy communicates with when connected.
     * @return MethodInterceptor the proxy calls.
     */
    public Object getMethodInterceptor() {
        return interceptor;
    }

    /**
     * GBean start method.
     * @throws WaitingException
     * @throws Exception
     */
    public void doStart() throws WaitingException, Exception {
        if (proxy == null) {
            //build proxy
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(adminObjectInterface);
            enhancer.setCallbackType(MethodInterceptor.class);
            //TODO is this correct?
            enhancer.setUseFactory(false);
            interceptor = new ConnectorMethodInterceptor(kernel.getKernelName(), ObjectName.getInstance(objectName));
            enhancer.setCallbacks(new Callback[]{interceptor});
            proxy = enhancer.create(new Class[0], new Object[0]);
        }
        //connect proxy
        interceptor.setInternalProxy(adminObject);
    }

    /**
     * GBean stop method.
     * @throws WaitingException
     * @throws Exception
     */
    public void doStop() throws WaitingException, Exception {
        //disconnect proxy
        interceptor.setInternalProxy(null);
    }

    /**
     * GBean fail method.
     */
    public void doFail() {
    }

    //DynamicGBean implementation

    /**
     * Delegating DynamicGBean getAttribute method.
     * @param name of attribute.
     * @return attribute value.
     * @throws Exception
     */
    public Object getAttribute(final String name) throws Exception {
        return delegate.getAttribute(name);
    }

    /**
     * Delegating DynamicGBean setAttribute method.
     * @param name of attribute.
     * @param value of attribute to be set.
     * @throws Exception
     */
    public void setAttribute(final String name, final Object value) throws Exception {
        delegate.setAttribute(name, value);
    }

    /**
     * no-op DynamicGBean method
     * @param name
     * @param arguments
     * @param types
     * @return nothing, there are no operations.
     * @throws Exception
     */
    public Object invoke(final String name, final Object[] arguments, final String[] types) throws Exception {
        //we have no dynamic operations.
        return null;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(AdminObjectWrapper.class);
        infoFactory.addAttribute("AdminObjectInterface", Class.class, true);
        infoFactory.addAttribute("AdminObjectClass", Class.class, true);
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addAttribute("objectName", String.class, false);

        infoFactory.addOperation("getProxy");
        infoFactory.addOperation("getMethodInterceptor");


        infoFactory.setConstructor(new String[]{
            "AdminObjectInterface",
            "AdminObjectClass",
            "kernel",
            "objectName"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
