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
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHttpRequest;
import org.mortbay.jetty.servlet.ServletHttpResponse;

/**
 * @version $Rev:  $ $Date:  $
 */
public class RequestWrapperBeforeAfter implements BeforeAfter {
    private final BeforeAfter next;
    private final ServletHandler handler;

    public RequestWrapperBeforeAfter(BeforeAfter next, ServletHandler handler) {
        this.next = next;
        this.handler = handler;
    }

    public void before(Object[] context, HttpRequest httpRequest, HttpResponse httpResponse) {
        if (httpRequest != null) {
            ServletHttpRequest request = (ServletHttpRequest) httpRequest.getWrapper();
            if (request == null) {
                // Build the request and response.

                String requestURI = httpRequest.getPath();
                String contextPath = handler.getHttpContext().getContextPath();
                String relativeURI = requestURI.substring(contextPath.length());
                request = new ServletHttpRequest(handler, relativeURI, httpRequest);
                ServletHttpResponse response = new ServletHttpResponse(request, httpResponse);
                httpRequest.setWrapper(request);
                httpResponse.setWrapper(response);
            }
        }

        if (next != null) {
            next.before(context, httpRequest, httpResponse);
        }
        
    }

    public void after(Object[] context, HttpRequest httpRequest, HttpResponse httpResponse) {

    }
}
