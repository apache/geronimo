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

import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.geronimo.webservices.WebServiceContainer;

class Axis2Response implements WebServiceContainer.Response {
    private int contentLength;

    private String contentType;

    private String host;

    private OutputStream out;

    private int method;

    private Map parameters;

    private String path;

    private URL uri;

    private int port;

    private Map headers;

    private int statusCode;

    private String statusMessage;

    /**
     * 
     */
    public Axis2Response(String contentType, String host, String path, URL uri,
            int port, OutputStream out) {
        this.contentType = contentType;
        this.host = host;
        this.parameters = new HashMap();
        this.path = path;
        this.uri = uri;
        this.port = port;
        this.headers = new HashMap();
        this.out = out;
    }

    public int getContentLength() {
        return contentLength;
    }

    public String getHeader(String name) {
        return (String) headers.get(name);
    }

    public String getHost() {
        return host;
    }

    public OutputStream getOutputStream() {
        return out;
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

    public String getPath() {
        return path;
    }

    public int getPort() {
        return port;
    }

    public URL getURI() {
        return uri;
    }

    /**
     * @return
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * @return
     */
    public URL getUri() {
        return uri;
    }

    /**
     * @param i
     */
    public void setContentLength(int i) {
        contentLength = i;
    }

    /**
     * @param string
     */
    public void setContentType(String string) {
        contentType = string;
    }

    /**
     * @param string
     */
    public void setHost(String string) {
        host = string;
    }

    /**
     * @param i
     */
    public void setMethod(int i) {
        method = i;
    }

    /**
     * @param map
     */
    public void setParameters(Map map) {
        parameters = map;
    }

    /**
     * @param string
     */
    public void setPath(String string) {
        path = string;
    }

    /**
     * @param i
     */
    public void setPort(int i) {
        port = i;
    }

    /**
     * @param url
     */
    public void setUri(URL url) {
        uri = url;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int code) {
        statusCode = code;
    }

    public void setStatusMessage(String responseString) {
        statusMessage = responseString;
    }

    public void flushBuffer() throws java.io.IOException {
    }

    public void setHeader(String name, String value) {
        headers.put(name, value);
    }

}

