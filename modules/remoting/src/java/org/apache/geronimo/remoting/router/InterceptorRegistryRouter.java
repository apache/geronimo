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

import java.net.URI;

import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.remoting.InterceptorRegistry;

/**
 * @version $Rev$ $Date$
 */
public class InterceptorRegistryRouter extends AbstractInterceptorRouter implements Router {
    private SubsystemRouter subsystemRouter;

    public SubsystemRouter getSubsystemRouter() {
        return subsystemRouter;
    }

    public void setSubsystemRouter(SubsystemRouter subsystemRouter) {
        this.subsystemRouter = subsystemRouter;
    }

    protected Interceptor lookupInterceptorFrom(URI to) throws Throwable {
        Long identifier = new Long(to.getFragment());
        return InterceptorRegistry.instance.lookup(identifier);
    }

    public void doStart() {
        if (subsystemRouter != null) {
            subsystemRouter.addRoute("/Remoting", this);
        }
        super.doStart();
    }

    public void doStop() {
        super.doStop();
        if (subsystemRouter != null) {
            subsystemRouter.removeRoute("/Remoting");
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(InterceptorRegistryRouter.class, AbstractInterceptorRouter.GBEAN_INFO);
        infoFactory.addReference("SubsystemRouter", SubsystemRouter.class);
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
