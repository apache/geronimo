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
package org.apache.geronimo.jaxws.client;

import java.lang.reflect.Method;

import javax.xml.ws.WebEndpoint;

import net.sf.cglib.proxy.CallbackFilter;

public class PortMethodFilter implements CallbackFilter {

    public int accept(Method method) {
        if (isGenericPortMethod(method) || 
            isGeneratedPortMethod(method) || 
            isCreateDispatchMethod(method)) {
            return 1; // use second method interceptor
        } else {
            return 0; // use first method interceptor
        }
    }
    
    private boolean isGenericPortMethod(Method method) {
        return (method.getName().equals("getPort"));
    }
    
    private boolean isGeneratedPortMethod(Method method) {
        return (method.getName().startsWith("get") && method.isAnnotationPresent(WebEndpoint.class));
    }

    private boolean isCreateDispatchMethod(Method method) {
        return (method.getName().equals("createDispatch"));
    }
}
