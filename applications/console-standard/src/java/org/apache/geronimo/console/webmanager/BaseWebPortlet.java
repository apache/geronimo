/**
 *
 * Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable.
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
package org.apache.geronimo.console.webmanager;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.portlet.GenericPortlet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.console.BasePortlet;

/**
 * Superclass for web container related portlets
 *
 * @version $Rev: 46228 $ $Date: 2004-09-16 21:21:04 -0400 (Thu, 16 Sep 2004) $
 */
public class BaseWebPortlet extends BasePortlet {
    private final static Log log = LogFactory.getLog(BaseWebPortlet.class);
    protected final static String SERVER_JETTY = "jetty";
    protected final static String SERVER_TOMCAT = "tomcat";
    protected final static String SERVER_GENERIC = "generic";

    protected final static String getServerType(Class cls) {
        Class[] intfs = cls.getInterfaces();
        for (int i = 0; i < intfs.length; i++) {
            Class intf = intfs[i];
            if(intf.getName().indexOf("Jetty") > -1) {
                return SERVER_JETTY;
            } else if(intf.getName().indexOf("Tomcat") > -1) {
                return SERVER_TOMCAT;
            }
        }
        return SERVER_GENERIC;
    }

}
