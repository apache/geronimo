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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;

/**
 * @version $Revision: 1.7 $ $Date: 2004/09/08 12:26:50 $
 */
public class SubsystemRouter extends AbstractRouterRouter {
    Log log = LogFactory.getLog(SubsystemRouter.class);
    Map currentRoutingMap = new HashMap();
    Collection childRouters;

    protected Router lookupRouterFrom(URI to) {
        String subsystem = to.getPath();
        return (Router) currentRoutingMap.get(subsystem);
    }

    synchronized public void addRoute(String path, Router router) {
        Map temp = new HashMap(currentRoutingMap);
        temp.put(path, router);
        currentRoutingMap = temp;
    }

    synchronized public void removeRoute(String path) {
        Map temp = new HashMap(currentRoutingMap);
        temp.remove(path);
        currentRoutingMap = temp;
    }

    public Router getRouter() {
        return this;
    }

    public void doStart() {
        super.doStart();
        log.info("Started subsystem router");
    }

    public void doStop() {
        super.doStop();
        log.info("Stopped subsystem router");
    }

    public void doFail() {
        super.doFail();
        log.info("Failed subsystem router");
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(SubsystemRouter.class, AbstractInterceptorRouter.GBEAN_INFO);
        infoFactory.addOperation("addRoute", new Class[]{String.class, Router.class});
        infoFactory.addOperation("removeRoute", new Class[]{String.class});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
