/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.tomcat.valve;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.security.auth.Subject;

import org.apache.catalina.valves.ValveBase;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.geronimo.security.ContextManager;

/**
 * @version $Rev$ $Date$
 */
public class DefaultSubjectValve extends ValveBase {

    private final Subject defaultSubject;

    public DefaultSubjectValve(Subject defaultSubject) {
        this.defaultSubject = defaultSubject;
    }

    public void invoke(Request request, Response response) throws IOException, ServletException {
        boolean setSubject = ContextManager.getCurrentCaller() == null;
        if (setSubject) {
            ContextManager.setCurrentCaller(defaultSubject);
            ContextManager.setNextCaller(defaultSubject);
            try {
                getNext().invoke(request, response);
            } finally {
                ContextManager.setCurrentCaller(null);
                ContextManager.setNextCaller(null);
            }
        } else {
            getNext().invoke(request, response);
        }

    }
}
