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
package org.apache.geronimo.webservices;

import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Map;

/**
 * @version $Rev:  $ $Date:  $
 */
public interface WebServiceContainer {

    void invoke(Request request, Response response) throws Exception;

    void getWsdl(OutputStream out) throws Exception;

    public interface Request {
        /** the HTTP OPTIONS type */
        int OPTIONS = 0; // Section 9.2
        /** the HTTP GET type */
        int GET     = 1; // Section 9.3
        /** the HTTP HEAD type */
        int HEAD    = 2; // Section 9.4
        /** the HTTP POST type */
        int POST    = 3; // Section 9.5
        /** the HTTP PUT type */
        int PUT     = 4; // Section 9.6
        /** the HTTP DELETE type */
        int DELETE  = 5; // Section 9.7
        /** the HTTP TRACE type */
        int TRACE   = 6; // Section 9.8
        /** the HTTP CONNECT type */
        int CONNECT = 7; // Section 9.9
        /** the HTTP UNSUPPORTED type */
        int UNSUPPORTED = 8;
        /** the Accept header */
        String HEADER_ACCEPT = "Accept";
        /** the Accept-Encoding header */
        String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
        /** the Accept-Language header */
        String HEADER_ACCEPT_LANGUAGE = "Accept-Language";
        /** the Content-Type header */
        String HEADER_CONTENT_TYPE = "Content-Type";
        /** the Content-Length header */
        String HEADER_CONTENT_LENGTH = "Content-Length";
        /** the Connection header */
        String HEADER_CONNECTION = "Connection";
        /** the Cache-Control header */
        String HEADER_CACHE_CONTROL = "Cache-Control";
        /** the Host header */
        String HEADER_HOST = "Host";
        /** the User-Agent header */
        String HEADER_USER_AGENT = "User-Agent";
        /** the Set-Cookie header */
        String HEADER_SET_COOKIE = "Set-Cookie";
        /** the Cookie header */
        String HEADER_COOKIE = "Cookie";

        String getHeader(String name);

        URI getURI();

        String getHost();

        String getPath();

        int getPort();

        int getContentLength();

        String getContentType();

        InputStream getInputStream() throws IOException;

        int getMethod();

        String getParameter(String name);

        Map getParameters();
    }

    public interface Response {
        void setHeader(String name, String value);

        String getHeader(String name);

        OutputStream getOutputStream();

        void setStatusCode(int code);

        int getStatusCode();

        void setContentType(String type);

        String getContentType();

        void setStatusMessage(String responseString);
    }

}
