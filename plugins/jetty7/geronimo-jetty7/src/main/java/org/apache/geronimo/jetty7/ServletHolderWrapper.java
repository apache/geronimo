/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.jetty7;

import java.util.Map;
import java.util.Set;

import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.management.Servlet;
import org.eclipse.jetty.servlet.ServletHolder;


/**
 * This ServletHolder's sole purpose is to provide the thread's current
 * ServletHolder for realms that are interested in the current servlet, e.g.
 * current servlet name.
 * <p/>
 * It is also being our servlet gbean for now.  We could gbean-ize the superclass to avoid the thread local access.
 *
 * @version $Rev$ $Date$
 */

@GBean(j2eeType = NameFactory.SERVLET)
public class ServletHolderWrapper implements ServletNameSource, Servlet, GBeanLifecycle {


    private final JettyServletRegistration servletRegistration;
    private final ServletHolder servletHolder;
    private final String objectName;

    //todo consider interface instead of this constructor for endpoint use.
    public ServletHolderWrapper() {
        servletRegistration = null;
        servletHolder = null;
        objectName = null;
    }

    public ServletHolderWrapper(@ParamSpecial(type = SpecialAttributeType.objectName) String objectName,
                              @ParamAttribute(name = "servletName") String servletName,
                              @ParamAttribute(name = "servletClass") String servletClassName,
                              @ParamAttribute(name = "jspFile") String jspFile,
                              @ParamAttribute(name = "initParams") Map initParams,
                              @ParamAttribute(name = "loadOnStartup") Integer loadOnStartup,
                              @ParamAttribute(name = "servletMappings") Set<String> servletMappings,
                              @ParamAttribute(name = "runAsRole") String runAsRole,
                              @ParamReference(name = "JettyServletRegistration", namingType = NameFactory.WEB_MODULE) JettyServletRegistration context) throws Exception {
        servletRegistration = context;
        servletHolder = new GeronimoServletHolder(context == null ? null : context.getIntegrationContext(), servletRegistration);
        servletHolder.setName(servletName);
        servletHolder.setClassName(servletClassName);
        servletHolder.getRegistration().setRunAsRole(runAsRole);
        //context will be null only for use as "default servlet info holder" in deployer.

        if (context != null) {
            servletHolder.setInitParameters(initParams);
            servletHolder.setForcedPath(jspFile);
            if (loadOnStartup != null) {
                //This has no effect on the actual start order, the gbean references "previous" control that.
                servletHolder.setInitOrder(loadOnStartup);
            }
            //this now starts the servlet in the appropriate context
            context.registerServletHolder(servletHolder, servletName, servletMappings, objectName);
        }
        this.objectName = objectName;
    }

    public String getServletName() {
        return servletHolder.getName();
    }

    public String getServletClassName() {
        return servletHolder.getClassName();
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

    public void doStart() throws Exception {
        //start actually handled in constructor
//        servletHolder.start();
    }

    public void doStop() throws Exception {
        servletHolder.stop();
        if (servletRegistration != null) {
            servletRegistration.unregisterServletHolder(servletHolder, servletHolder.getName(), null, objectName);
        }
    }

    public void doFail() {
        try {
            doStop();
        } catch (Exception e) {
            //?? ignore
        }
    }

}
