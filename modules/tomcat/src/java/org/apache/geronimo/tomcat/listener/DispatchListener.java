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
package org.apache.geronimo.tomcat.listener;

import java.util.Stack;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.catalina.Container;
import org.apache.catalina.Globals;
import org.apache.catalina.InstanceEvent;
import org.apache.catalina.InstanceListener;
import org.apache.catalina.core.StandardWrapper;
import org.apache.geronimo.tomcat.GeronimoStandardContext;
import org.apache.geronimo.tomcat.interceptor.BeforeAfter;
import org.apache.geronimo.tomcat.realm.TomcatGeronimoRealm;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.http.mapper.Mapper;
import org.apache.tomcat.util.http.mapper.MappingData;

public class DispatchListener implements InstanceListener {

    private static ThreadLocal currentContext = new ThreadLocal() {
        protected Object initialValue() {
            return new Stack();
        }
    };

    public void instanceEvent(InstanceEvent event) {

        if (event.getType().equals(InstanceEvent.BEFORE_DISPATCH_EVENT)) {

            Container parent = event.getWrapper().getParent();
            if (parent instanceof GeronimoStandardContext) {
                beforeDispatch((GeronimoStandardContext) parent, event
                        .getRequest(), event.getResponse());
            }
        }

        if (event.getType().equals(InstanceEvent.AFTER_DISPATCH_EVENT)) {
            Container parent = event.getWrapper().getParent();
            if (parent instanceof GeronimoStandardContext) {
                afterDispatch((GeronimoStandardContext) parent, event
                        .getRequest(), event.getResponse());
            }
        }
    }

    private void beforeDispatch(GeronimoStandardContext webContext,
            ServletRequest request, ServletResponse response) {

        BeforeAfter beforeAfter = webContext.getBeforeAfter();
        if (beforeAfter != null) {
            Stack stack = (Stack) currentContext.get();
            Object context[] = new Object[webContext.getContextCount() + 1];
            String wrapperName = getWrapperName(request, webContext);
            context[webContext.getContextCount()] = TomcatGeronimoRealm
                    .setRequestWrapperName(wrapperName);
            beforeAfter.before(context, request, response);
            stack.push(context);
        }
    }

    private void afterDispatch(GeronimoStandardContext webContext,
            ServletRequest request, ServletResponse response) {
        BeforeAfter beforeAfter = webContext.getBeforeAfter();
        if (beforeAfter != null) {
            Stack stack = (Stack) currentContext.get();
            Object context[] = (Object[]) stack.pop();
            beforeAfter.after(context, request, response);
            TomcatGeronimoRealm
                    .setRequestWrapperName((String) context[webContext
                            .getContextCount()]);
        }
    }

    private String getWrapperName(ServletRequest request,
            GeronimoStandardContext webContext) {
        
        MappingData mappingData = new MappingData();
        Mapper mapper = webContext.getMapper();
        MessageBytes mb = MessageBytes.newInstance();
        
        String dispatchPath =
            (String) request.getAttribute(Globals.DISPATCHER_REQUEST_PATH_ATTR);
        mb.setString(webContext.getName() + dispatchPath);
        
        try {
            mapper.map(mb, mappingData);
            StandardWrapper wrapper = (StandardWrapper) mappingData.wrapper;
            return wrapper.getName();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }
}
