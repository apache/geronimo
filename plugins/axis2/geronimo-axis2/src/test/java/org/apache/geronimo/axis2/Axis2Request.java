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
package org.apache.geronimo.axis2;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.geronimo.webservices.WebServiceContainer;

class Axis2Request implements WebServiceContainer.Request {
    private int contentLength;

    private String contentType;

    private InputStream in;

    private int method;

    private Map parameters;

    private URI uri;

    private Map headers;

    private Map attributes;

    private String remoteAddress;

    /**
     *
     */
    public Axis2Request(int contentLength, String contentType, InputStream in,
            int method, Map parameters, URI uri, Map headers,
            String remoteAddress) {
        this.contentType = contentType;
        this.in = in;
        this.method = method;
        this.parameters = parameters;
        this.uri = uri;
        this.headers = headers;
        this.attributes = new HashMap();
        this.remoteAddress = remoteAddress;
    }

    public int getContentLength() {
        return contentLength;
    }

    public String getContentType() {
        return contentType;
    }

    public String getHeader(String name) {
        return (String) headers.get(name);
    }

    public InputStream getInputStream() throws IOException {
        return in;
    }

    public int getMethod() {
        return method;
    }

    public String getParameter(String name) {
        return (String) parameters.get(name);
    }

    public Map getParameters() {
        return parameters;
    }

    public URI getURI() {
        return uri;
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    public String getRemoteAddr() {
        return remoteAddress;
    }

    public String getContextPath() {
        return "/axis2";
    }

}
