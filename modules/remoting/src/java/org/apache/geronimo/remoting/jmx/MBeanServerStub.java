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

package org.apache.geronimo.remoting.jmx;

import javax.management.ObjectName;

import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanContext;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.proxy.ProxyContainer;
import org.apache.geronimo.proxy.ReflexiveInterceptor;
import org.apache.geronimo.remoting.DeMarshalingInterceptor;
import org.apache.geronimo.remoting.router.JMXRouter;
import org.apache.geronimo.remoting.router.JMXTarget;


/**
 * @version $Revision: 1.14 $ $Date: 2004/06/04 22:31:56 $
 */
public class MBeanServerStub implements GBean, JMXTarget {
    private final Kernel kernel;
    private final ObjectName objectName;
    private ProxyContainer serverContainer;
    private DeMarshalingInterceptor demarshaller;
    private JMXRouter router;

    public MBeanServerStub(Kernel kernel, String objectName) {
        this.kernel = kernel;
        this.objectName = JMXUtil.getObjectName(objectName);
    }

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
    }

    public void doStart() {
        router.register(objectName, this);

        // Setup the server side contianer..
        Interceptor firstInterceptor = new ReflexiveInterceptor(kernel.getMBeanServer());
        demarshaller = new DeMarshalingInterceptor(firstInterceptor, getClass().getClassLoader());
        serverContainer = new ProxyContainer(firstInterceptor);
    }

    public void doStop() {
        router.unregister(objectName);
        serverContainer = null;
        demarshaller = null;
    }

    public void doFail() {
        serverContainer = null;
        demarshaller = null;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(MBeanServerStub.class);
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addAttribute("objectName", String.class, false);
        infoFactory.addOperation("getRemotingEndpointInterceptor");
        infoFactory.addReference("Router", JMXRouter.class);
        infoFactory.setConstructor(new String[]{"kernel", "objectName"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
