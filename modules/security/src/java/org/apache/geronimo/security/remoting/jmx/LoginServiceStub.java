/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.security.remoting.jmx;

import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanContext;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.jmx.GBeanMBeanContext;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.jmx.MBeanProxyFactory;
import org.apache.geronimo.proxy.ProxyContainer;
import org.apache.geronimo.proxy.ReflexiveInterceptor;
import org.apache.geronimo.remoting.DeMarshalingInterceptor;
import org.apache.geronimo.remoting.router.JMXRouter;
import org.apache.geronimo.remoting.router.JMXTarget;
import org.apache.geronimo.security.jaas.LoginServiceMBean;


/**
 * @version $Revision: 1.3 $ $Date: 2004/06/02 05:33:04 $
 */
public class LoginServiceStub implements GBean, JMXTarget {
    private ProxyContainer serverContainer;
    private DeMarshalingInterceptor demarshaller;
    private GBeanMBeanContext context;
    private JMXRouter router;


    public Interceptor getRemotingEndpointInterceptor() {
        return demarshaller;
    }

    public JMXRouter getRouter() {
        return router;
    }

    public void setRouter(JMXRouter router) {
        this.router = router;
    }

    public void setGBeanContext(GBeanContext context) {
        this.context = (GBeanMBeanContext) context;
    }

    public void doStart() throws Exception {
        router.register(context.getObjectName(), this);

        // Setup the server side contianer..
        LoginServiceMBean loginService = (LoginServiceMBean) MBeanProxyFactory.getProxy(LoginServiceMBean.class,
                                                                                        context.getServer(),
                                                                                        JMXUtil.getObjectName("geronimo.security:type=LoginService"));
        Interceptor firstInterceptor = new ReflexiveInterceptor(loginService);
        demarshaller = new DeMarshalingInterceptor(firstInterceptor, getClass().getClassLoader());
        serverContainer = new ProxyContainer(firstInterceptor);
    }

    public void doStop() {
        router.unRegister(context.getObjectName());

        serverContainer = null;
        demarshaller = null;
    }

    public void doFail() {
        serverContainer = null;
        demarshaller = null;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(LoginServiceStub.class);
        infoFactory.addReference("Router", JMXRouter.class);
        infoFactory.addOperation("getRemotingEndpointInterceptor");
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
