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
package org.apache.geronimo.console;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.geronimo.console.i18n.ConsoleResourceRegistry;
import org.apache.geronimo.console.message.CommonMessage;
import org.apache.geronimo.console.message.ErrorMessage;
import org.apache.geronimo.console.message.InfoMessage;
import org.apache.geronimo.console.message.WarnMessage;
import org.apache.geronimo.console.util.PortletManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Superclass with some generic functionality for console portlets
 *
 * @version $Rev$ $Date$
 */
public class BasePortlet extends GenericPortlet {
    private static final Logger log = LoggerFactory.getLogger(BasePortlet.class);
    protected final static String WEB_SERVER_JETTY = "jetty";
    protected final static String WEB_SERVER_TOMCAT = "tomcat";
    protected final static String WEB_SERVER_GENERIC = "generic";
    private static final String COMMON_MESSAGES = "commonMessages";
    private static final String FMT_LOCALE = "javax.servlet.jsp.jstl.fmt.locale.request";
    private static final String BASENAME = "portletinfo";
    private static ConsoleResourceRegistry resourceRegistry;
    
    static {
        try {
            resourceRegistry = (ConsoleResourceRegistry) PortletManager.getKernel().getGBean(ConsoleResourceRegistry.class);
        } catch (Exception e) {
            log.error("Cannot get the console resource registery service", e);
        }
    }    

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
        String booleanGetter = "is"+Character.toUpperCase(name.charAt(0))+name.substring(1);
        Method[] list = cls.getMethods();
        for (int i = 0; i < list.length; i++) {
            Method method = list[i];
            String methodName = method.getName();
            if( (methodName.equals(getter) || methodName.equals(booleanGetter))
                && method.getParameterTypes().length == 0 && Modifier.isPublic(method.getModifiers()) &&
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

    @Override
    public void render(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        @SuppressWarnings("unchecked")
        SoftReference<List<CommonMessage>> msgRef = (SoftReference<List<CommonMessage>>) request.getPortletSession().getAttribute(COMMON_MESSAGES);
        if (null != msgRef && null != msgRef.get()) {
            request.setAttribute(COMMON_MESSAGES, msgRef.get());
        }
        request.getPortletSession().removeAttribute(COMMON_MESSAGES);

        request.setAttribute(FMT_LOCALE, request.getLocale());
        
        super.render(request, response);
    }

    public final void addErrorMessage(PortletRequest request, String... messages) {
        addCommonMessage(CommonMessage.Type.Error, request, messages);
    }

    public final void addWarningMessage(PortletRequest request, String... messages) {
        addCommonMessage(CommonMessage.Type.Warn, request, messages);
    }

    public final void addInfoMessage(PortletRequest request, String... messages) {
        addCommonMessage(CommonMessage.Type.Info, request, messages);
    }

    public final String getLocalizedString(PortletRequest request, String key, Object... vars) {
        String value = resourceRegistry.handleGetObject(BASENAME, request.getLocale(), key);
        if (null == value || 0 == value.length()) return key;     
        return MessageFormat.format(value, vars);
    }

    private void addCommonMessage(CommonMessage.Type type, PortletRequest request, String[] messages) {
        if (null != messages && 0 != messages.length) {
            if (1 == messages.length) {
                addCommonMessage(type, request, messages[0], null);
            } else {
                StringBuilder sb = new StringBuilder();
                for (String message : messages) {
                    sb.append(message + "<br>");
                }
                addCommonMessage(type, request, messages[0], sb.toString());
            }
        }
    }

    private void addCommonMessage(CommonMessage.Type type, PortletRequest request, String abbr, String detail) {
        if (request instanceof ActionRequest) {
            List<CommonMessage> messages;
            @SuppressWarnings("unchecked")
            SoftReference<List<CommonMessage>> msgRef = (SoftReference<List<CommonMessage>>) request.getPortletSession().getAttribute(COMMON_MESSAGES);
            if (null == msgRef || null == msgRef.get()) {
                messages = new ArrayList<CommonMessage>();
                msgRef = new SoftReference<List<CommonMessage>>(messages);
                request.getPortletSession().setAttribute(COMMON_MESSAGES, msgRef);
            } else {
                messages = msgRef.get();
            }
            addCommonMessage(type, messages, abbr, detail);
        } else {
            @SuppressWarnings("unchecked")
            List<CommonMessage> messages = (List<CommonMessage>) request.getAttribute(COMMON_MESSAGES);
            if (null == messages) {
                messages = new ArrayList<CommonMessage>();
                request.setAttribute(COMMON_MESSAGES, messages);
            }
            addCommonMessage(type, messages, abbr, detail);
        }
    }

    private void addCommonMessage(CommonMessage.Type type, List<CommonMessage> messages, String abbr, String detail) {
        switch (type) {
        case Error:
            messages.add(new ErrorMessage(abbr, detail));
            break;
        case Warn:
            messages.add(new WarnMessage(abbr, detail));
            break;
        case Info:
            messages.add(new InfoMessage(abbr, detail));
            break;
        }
    }

}
