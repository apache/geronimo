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

package org.apache.geronimo.remoting.router;

import javax.management.ObjectName;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GOperationInfo;
import org.apache.geronimo.gbean.GReferenceInfo;

/**
 * Uses JMX Object names to route the request to a JMX object that implements the
 * JMXTargetMBean interface.
 *
 * This allows you to route invocations to MBeans using URIs like:
 * async://localhost:3434/JMX#geronimo.jmx:target=MBeanServerStub
 *
 * The MBean that will receive invocations must implement the JMXTarget interface.
 *
 * @version $Revision: 1.10 $ $Date: 2004/03/10 09:59:19 $
 */
public class JMXRouter extends AbstractInterceptorRouter {
    private SubsystemRouter subsystemRouter;
    private Map registered = new HashMap();

    public SubsystemRouter getSubsystemRouter() {
        return subsystemRouter;
    }

    public void setSubsystemRouter(SubsystemRouter subsystemRouter) {
        this.subsystemRouter = subsystemRouter;
    }

    public void register(ObjectName objectName, JMXTarget target) {
        registered.put(objectName, target);
    }

    public void unRegister(ObjectName objectName) {
        registered.remove(objectName);
    }

    protected Interceptor lookupInterceptorFrom(URI to) throws Exception {
        ObjectName objectName = new ObjectName(to.getFragment());
        JMXTarget bean = (JMXTarget)registered.get(objectName);

        if (bean != null) return bean.getRemotingEndpointInterceptor();

        throw new IllegalArgumentException("No names mbeans registered that match object name pattern: " + objectName);
    }

    public void doStart() {
        subsystemRouter.addRoute("/JMX", this);
        super.doStart();
    }

    public void doStop() {
        super.doStop();
        subsystemRouter.removeRoute("/JMX");
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(JMXRouter.class.getName(), AbstractInterceptorRouter.GBEAN_INFO);
        infoFactory.addReference(new GReferenceInfo("SubsystemRouter", SubsystemRouter.class.getName()));
        infoFactory.addOperation(new GOperationInfo("register", new String[] {ObjectName.class.getName(), JMXTarget.class.getName()}));
        infoFactory.addOperation(new GOperationInfo("unRegister", new String[] {ObjectName.class.getName()}));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
