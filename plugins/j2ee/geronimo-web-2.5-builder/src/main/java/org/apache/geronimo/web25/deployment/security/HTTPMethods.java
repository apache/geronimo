/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.web25.deployment.security;

import java.util.Set;
import java.util.HashSet;
import java.util.regex.Pattern;

/**
 * Tracks sets of HTTP actions for use while computing permissions during web deployment.
 *
 * @version $Rev$ $Date$
 */
public class HTTPMethods {
    private static final Pattern TOKEN_PATTERN = Pattern.compile("[!-~&&[^\\(\\)\\<\\>@,;:\\\\\"/\\[\\]\\?=\\{\\}]]*");

    private final Set<String> methods = new HashSet<String>();
    private boolean isExcluded = false;


    public HTTPMethods() {
    }

    public HTTPMethods(HTTPMethods httpMethods, boolean complemented) {
        isExcluded = httpMethods.isExcluded ^ complemented;
        methods.addAll(httpMethods.methods);
    }

    public void add(String httpMethod) {
        if (isExcluded) {
            checkToken(httpMethod);
            methods.remove(httpMethod);
        } else if (httpMethod == null || httpMethod.length() == 0) {
            isExcluded = true;
            methods.clear();
        } else {
            checkToken(httpMethod);
            methods.add(httpMethod);
        }
    }

    public HTTPMethods add(HTTPMethods httpMethods) {
        if (isExcluded) {
            if (httpMethods.isExcluded) {
                methods.retainAll(httpMethods.methods);
            } else {
                methods.removeAll(httpMethods.methods);
            }
        } else {
            if (httpMethods.isExcluded) {
                isExcluded = true;
                Set<String> toRemove = new HashSet<String>(methods);
                methods.clear();
                methods.addAll(httpMethods.methods);
                methods.removeAll(toRemove);
            } else {
                methods.addAll(httpMethods.methods);
            }
        }
        return this;
    }

    public HTTPMethods remove(HTTPMethods httpMethods) {
        if (isExcluded) {
            if (httpMethods.isExcluded) {
                //TODO questionable
                isExcluded = false;
                Set<String> toRemove = new HashSet<String>(methods);
                methods.clear();
                methods.addAll(httpMethods.methods);
                methods.removeAll(toRemove);
            } else {
                methods.addAll(httpMethods.methods);
            }
        } else {
            if (httpMethods.isExcluded) {
                methods.retainAll(httpMethods.methods);
            } else {
                methods.removeAll(httpMethods.methods);
            }
        }
        if (!isExcluded && methods.isEmpty()) {
            return null;
        }
        return this;
    }

    public String getHttpMethods() {
        return getHttpMethodsBuffer(isExcluded).toString();
    }

    public StringBuilder getHttpMethodsBuffer() {
        return getHttpMethodsBuffer(isExcluded);
    }

    public String getComplementedHttpMethods() {
        return getHttpMethodsBuffer(!isExcluded).toString();
    }

    private StringBuilder getHttpMethodsBuffer( boolean excluded) {
        StringBuilder buffer = new StringBuilder();
        if (excluded) {
            buffer.append("!");
        }
        boolean afterFirst = false;
        for (String method : methods) {
            if (afterFirst) {
                buffer.append(",");
            } else {
                afterFirst = true;
            }
            buffer.append(method);
        }
        return buffer;
    }

    private void checkToken(String method) {
        if (!TOKEN_PATTERN.matcher(method).matches()) {
            throw new IllegalArgumentException("Invalid HTTPMethodSpec");
        }
    }


    public boolean isNone() {
        return !isExcluded && methods.isEmpty();
    }
}
