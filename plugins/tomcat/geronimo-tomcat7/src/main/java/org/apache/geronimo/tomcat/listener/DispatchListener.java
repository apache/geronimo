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
package org.apache.geronimo.tomcat.listener;

import java.util.Stack;

import javax.security.jacc.PolicyContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.catalina.Container;
import org.apache.catalina.Globals;
import org.apache.catalina.InstanceEvent;
import org.apache.catalina.InstanceListener;
import org.apache.catalina.core.StandardWrapper;
import org.apache.geronimo.tomcat.GeronimoStandardContext;
import org.apache.geronimo.tomcat.interceptor.BeforeAfter;
import org.apache.geronimo.tomcat.interceptor.BeforeAfterContext;
import org.apache.geronimo.tomcat.security.jacc.JACCRealm;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.http.mapper.Mapper;
import org.apache.tomcat.util.http.mapper.MappingData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DispatchListener implements InstanceListener {

    private static final Logger log = LoggerFactory.getLogger(DispatchListener.class);

    private static ThreadLocal<Stack<BeforeAfterContext>> currentContext = new ThreadLocal<Stack<BeforeAfterContext>>() {
        protected Stack<BeforeAfterContext> initialValue() {
            return new Stack<BeforeAfterContext>();
        }
    };

    public void instanceEvent(InstanceEvent event) {

        if (event.getType().equals(InstanceEvent.BEFORE_SERVICE_EVENT)) {
            String oldWrapperName = JACCRealm.setRequestWrapperName(event.getWrapper().getName());

            BeforeAfterContext beforeAfterContext = new BeforeAfterContext(1);
            beforeAfterContext.contexts[0] = oldWrapperName;

            currentContext.get().push(beforeAfterContext);
        }

        if (event.getType().equals(InstanceEvent.AFTER_SERVICE_EVENT)) {
            JACCRealm.setRequestWrapperName((String) currentContext.get().pop().contexts[0]);
        }

        if (event.getType().equals(InstanceEvent.BEFORE_DISPATCH_EVENT)) {
            Container parent = event.getWrapper().getParent();
            if (parent instanceof GeronimoStandardContext) {
                beforeDispatch((GeronimoStandardContext) parent, event.getRequest(), event.getResponse());
            }
        }

        if (event.getType().equals(InstanceEvent.AFTER_DISPATCH_EVENT)) {
            Container parent = event.getWrapper().getParent();
            if (parent instanceof GeronimoStandardContext) {
                afterDispatch((GeronimoStandardContext) parent, event.getRequest(), event.getResponse());
            }
        }
    }

    private void beforeDispatch(GeronimoStandardContext webContext, ServletRequest request, ServletResponse response) {

        BeforeAfter beforeAfter = webContext.getBeforeAfter();
        if (beforeAfter != null) {
            Stack<BeforeAfterContext> stack = currentContext.get();

            BeforeAfterContext beforeAfterContext = new BeforeAfterContext(webContext.getContextCount() + 2);

            String wrapperName = getWrapperName(request, webContext);
            beforeAfterContext.contexts[webContext.getContextCount()] = JACCRealm.setRequestWrapperName(wrapperName);

            beforeAfterContext.contexts[webContext.getContextCount() + 1] = PolicyContext.getContextID();
            PolicyContext.setContextID(webContext.getPolicyContextId());

            beforeAfter.before(beforeAfterContext, request, response, BeforeAfter.DISPATCHED);

            stack.push(beforeAfterContext);
        }
    }

    private void afterDispatch(GeronimoStandardContext webContext, ServletRequest request, ServletResponse response) {

        BeforeAfter beforeAfter = webContext.getBeforeAfter();
        if (beforeAfter != null) {
            Stack<BeforeAfterContext> stack = currentContext.get();
            BeforeAfterContext beforeAfterContext = stack.pop();

            beforeAfter.after(beforeAfterContext, request, response, BeforeAfter.DISPATCHED);

            JACCRealm.setRequestWrapperName((String) beforeAfterContext.contexts[webContext.getContextCount()]);
            PolicyContext.setContextID((String) beforeAfterContext.contexts[webContext.getContextCount() + 1]);
        }
    }

    private String getWrapperName(ServletRequest request, GeronimoStandardContext webContext) {

        MappingData mappingData = new MappingData();
        Mapper mapper = webContext.getMapper();
        MessageBytes mb = MessageBytes.newInstance();

        String dispatchPath = (String) request.getAttribute(Globals.DISPATCHER_REQUEST_PATH_ATTR);
        mb.setString(webContext.getName() + dispatchPath);

        try {
            mapper.map(mb, mappingData);
            StandardWrapper wrapper = (StandardWrapper) mappingData.wrapper;
            return wrapper.getName();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }

}
