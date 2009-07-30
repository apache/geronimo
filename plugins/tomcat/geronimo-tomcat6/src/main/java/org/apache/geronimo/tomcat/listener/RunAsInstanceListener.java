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

import javax.security.auth.Subject;
import org.apache.catalina.Container;
import org.apache.catalina.InstanceEvent;
import org.apache.catalina.InstanceListener;
import org.apache.catalina.Wrapper;
import org.apache.geronimo.security.Callers;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.tomcat.GeronimoStandardContext;

public class RunAsInstanceListener implements InstanceListener {

    private static final ThreadLocal<Stack<Callers>> threadLocal = new ThreadLocal<Stack<Callers>>() {
        protected Stack<Callers> initialValue() {
            return new Stack<Callers>();
        }
    };

    public void instanceEvent(InstanceEvent event) {

        if (event.getType().equals(InstanceEvent.BEFORE_SERVICE_EVENT)) {
            Container parent = event.getWrapper().getParent();
            if (parent instanceof GeronimoStandardContext) {
                Stack<Callers> callersStack = threadLocal.get();
                GeronimoStandardContext context = (GeronimoStandardContext) parent;
                Wrapper wrapper = event.getWrapper();
                String runAsRole = wrapper.getRunAs();
                Subject runAsSubject = context.getSubjectForRole(runAsRole);
                Callers oldCallers = ContextManager.pushNextCaller(runAsSubject);
                callersStack.push(oldCallers);
            }
        } else if (event.getType().equals(InstanceEvent.AFTER_SERVICE_EVENT)) {
            Container parent = event.getWrapper().getParent();
            if (parent instanceof GeronimoStandardContext) {
                Stack<Callers> callersStack = threadLocal.get();
                Callers oldCallers = callersStack.pop();
                ContextManager.popCallers(oldCallers);
            }
        }
    }
}
