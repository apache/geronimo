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
package org.apache.geronimo.console;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.management.geronimo.WebContainer;

/**
 * Superclass with some generic functionality for console portlets
 *
 * @version $Rev: 46228 $ $Date: 2004-09-16 21:21:04 -0400 (Thu, 16 Sep 2004) $
 */
public class BasePortlet extends GenericPortlet {
    private final static Log log = LogFactory.getLog(BasePortlet.class);
    protected final static String WEB_SERVER_JETTY = "jetty";
    protected final static String WEB_SERVER_TOMCAT = "tomcat";
    protected final static String WEB_SERVER_GENERIC = "generic";

    protected final static String getWebServerType(Class cls) {
        Class[] intfs = cls.getInterfaces();
        for (int i = 0; i < intfs.length; i++) {
            Class intf = intfs[i];
            if(intf.getName().indexOf("Jetty") > -1) {
                return WEB_SERVER_JETTY;
            } else if(intf.getName().indexOf("Tomcat") > -1) {
                return WEB_SERVER_TOMCAT;
            }
        }
        return WEB_SERVER_GENERIC;
    }

    public final static void setProperty(Object target, String name, Object value) {
        boolean found = false;
        Class cls = target.getClass();
        String setter = "set"+Character.toUpperCase(name.charAt(0))+name.substring(1);
        Method[] list = cls.getMethods();
        for (int i = 0; i < list.length; i++) {
            Method method = list[i];
            if(method.getName().equals(setter) && method.getParameterTypes().length == 1 && Modifier.isPublic(method.getModifiers()) &&
                    !Modifier.isStatic(method.getModifiers())) {
                found = true;
                try {
                    method.invoke(target, new Object[]{value});
                } catch (Exception e) {
                    log.error("Unable to set property "+name+" on "+target.getClass().getName());
                }
                break;
            }
        }
        if(!found) {
            throw new IllegalArgumentException("No such method found ("+setter+" on "+target.getClass().getName()+")");
        }
    }

    public final static Object getProperty(Object target, String name) {
        Class cls = target.getClass();
        String getter = "get"+Character.toUpperCase(name.charAt(0))+name.substring(1);
        Method[] list = cls.getMethods();
        for (int i = 0; i < list.length; i++) {
            Method method = list[i];
            if(method.getName().equals(getter) && method.getParameterTypes().length == 0 && Modifier.isPublic(method.getModifiers()) &&
                    !Modifier.isStatic(method.getModifiers())) {
                try {
                    return method.invoke(target, new Object[0]);
                } catch (Exception e) {
                    log.error("Unable to get property "+name+" on "+target.getClass().getName());
                }
                break;
            }
        }
        throw new IllegalArgumentException("No such method found ("+getter+" on "+target.getClass().getName()+")");
    }

    public final static Object callOperation(Object target, String operation, Object[] args) {
        Class cls = target.getClass();
        Method[] list = cls.getMethods();
        for (int i = 0; i < list.length; i++) {
            Method method = list[i];
            if(method.getName().equals(operation) && ((args == null && method.getParameterTypes().length == 0) || (args != null && args.length == method.getParameterTypes().length))
                    && Modifier.isPublic(method.getModifiers()) && !Modifier.isStatic(method.getModifiers())) {
                try {
                    return method.invoke(target, args);
                } catch (Exception e) {
                    log.error("Unable to invoke "+operation+" on "+target.getClass().getName());
                }
                break;
            }
        }
        throw new IllegalArgumentException("No such method found ("+operation+" on "+target.getClass().getName()+")");
    }
}
