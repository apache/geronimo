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
package org.apache.geronimo.tomcat.interceptor;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.geronimo.tomcat.GeronimoStandardContext;

public class RequestListenerBeforeAfter implements BeforeAfter{
    private final BeforeAfter next;
    private final int index;
    private final GeronimoStandardContext standardContext;

    public RequestListenerBeforeAfter(BeforeAfter next, int index, GeronimoStandardContext standardContext) {
        this.next = next;
        this.index = index;
        this.standardContext = standardContext;
    }

    public void before(BeforeAfterContext beforeAfterContext, ServletRequest httpRequest, ServletResponse httpResponse, int dispatch) {
        if (httpRequest != null && httpResponse != null) {
            standardContext.fireRequestInitEventInBeforeAfter(httpRequest);
        }
        if (next != null) {
            next.before(beforeAfterContext, httpRequest, httpResponse, dispatch);
        }
    }
    

    public void after(BeforeAfterContext beforeAfterContext, ServletRequest httpRequest, ServletResponse httpResponse, int dispatch) {       
            if (next != null) {
                next.after(beforeAfterContext, httpRequest, httpResponse, dispatch);
            }
    }

}
