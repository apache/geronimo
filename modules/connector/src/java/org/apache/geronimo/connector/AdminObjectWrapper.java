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
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanContext;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.kernel.Kernel;

/**
 * @version $Revision: 1.7 $ $Date: 2004/06/05 01:40:09 $
 */
public class AdminObjectWrapper implements GBean, DynamicGBean {

    public static final GBeanInfo GBEAN_INFO;

    private final Class adminObjectInterface;
    private final Class adminObjectClass;

    private final DynamicGBeanDelegate delegate;
    private final Object adminObject;
    private final Kernel kernel;
    private final ObjectName selfName;

    private ConnectorMethodInterceptor interceptor;
    private Object proxy;

    //for use as endpoint
    public AdminObjectWrapper() {
        adminObjectInterface = null;
        adminObjectClass = null;
        adminObject = null;
        delegate = null;
        kernel = null;
        selfName = null;
    }

    public AdminObjectWrapper(Class adminObjectInterface, Class adminObjectClass, Kernel kernel, ObjectName selfName) throws IllegalAccessException, InstantiationException {
        this.adminObjectInterface = adminObjectInterface;
        this.adminObjectClass = adminObjectClass;
        adminObject = adminObjectClass.newInstance();
        delegate = new DynamicGBeanDelegate();
        delegate.addAll(adminObject);
        this.kernel = kernel;
        this.selfName = selfName;
    }

    public Class getAdminObjectClass() {
        return adminObjectClass;
    }

    public Object getProxy() {
        return proxy;
    }

    public Object getMethodInterceptor() {
        return interceptor;
    }

    //gbean implementation
    public void setGBeanContext(GBeanContext context) {
    }

    public void doStart() throws WaitingException, Exception {
        if (proxy == null) {
            //build proxy
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(adminObjectInterface);
            enhancer.setCallbackType(MethodInterceptor.class);
            enhancer.setUseFactory(false);//????
            interceptor = new ConnectorMethodInterceptor(kernel.getKernelName(), selfName);
            enhancer.setCallbacks(new Callback[]{interceptor});
            proxy = enhancer.create(new Class[0], new Object[0]);
        }
        //connect proxy
        interceptor.setInternalProxy(adminObject);
    }

    public void doStop() throws WaitingException, Exception {
        //disconnect proxy
        interceptor.setInternalProxy(null);
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

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(AdminObjectWrapper.class);
        infoFactory.addAttribute("AdminObjectInterface", Class.class, true);
        infoFactory.addAttribute("AdminObjectClass", Class.class, true);
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addAttribute("SelfName", ObjectName.class, true);

        infoFactory.addOperation("getProxy");
        infoFactory.addOperation("getMethodInterceptor");


        infoFactory.setConstructor(new String[]{
            "AdminObjectInterface",
            "AdminObjectClass",
            "kernel",
            "SelfName"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
