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
package org.apache.geronimo.jetty.interceptor;

import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.HttpContext;
import org.apache.geronimo.jetty.JettyWebAppContext;

/**
 * @version $Rev:  $ $Date:  $
 */
public class WebApplicationContextBeforeAfter implements BeforeAfter {

    private final BeforeAfter next;
    private final int index;
    private final JettyWebAppContext webAppContext;

    public WebApplicationContextBeforeAfter(BeforeAfter next, int index, JettyWebAppContext webAppContext) {
        this.next = next;
        this.index = index;
        this.webAppContext = webAppContext;
    }

    public void before(Object[] context, HttpRequest httpRequest, HttpResponse httpResponse) {
        if (httpResponse != null) {
            context[index] = httpResponse.getHttpContext();
            httpResponse.setHttpContext(webAppContext);
        }
        if (next != null) {
            next.before(context, httpRequest, httpResponse);
        }
    }

    public void after(Object[] context, HttpRequest httpRequest, HttpResponse httpResponse) {
        if (next != null) {
            next.after(context, httpRequest, httpResponse);
        }
        if (httpResponse != null) {
            httpResponse.setHttpContext((HttpContext) context[index]);
        }
    }
}
