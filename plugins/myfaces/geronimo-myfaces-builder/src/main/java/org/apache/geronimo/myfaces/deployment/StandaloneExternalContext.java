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

package org.apache.geronimo.myfaces.deployment;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.Principal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.faces.context.ExternalContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DummyExternalContext is only used in the deployment process.
 * @version $Rev$ $Date$
 */
public class StandaloneExternalContext extends ExternalContext {

    private static final Logger logger = LoggerFactory.getLogger(StandaloneExternalContext.class);

    private Map<String, Object> applicationMap = new HashMap<String, Object>();

    private ClassLoader classLoader;

    public StandaloneExternalContext(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void dispatch(String arg0) throws IOException {

    }

    @Override
    public String encodeActionURL(String arg0) {
        return null;
    }

    @Override
    public String encodeNamespace(String arg0) {
        return null;
    }

    @Override
    public String encodeResourceURL(String arg0) {
        return null;
    }

    @Override
    public Map<String, Object> getApplicationMap() {
        return applicationMap;
    }

    @Override
    public String getAuthType() {
        return null;
    }

    @Override
    public Object getContext() {
        return null;
    }

    @Override
    public String getInitParameter(String arg0) {
        return null;
    }

    @Override
    public Map getInitParameterMap() {
        return null;
    }

    @Override
    public String getRemoteUser() {
        return null;
    }

    @Override
    public Object getRequest() {
        return null;
    }

    @Override
    public String getRequestContextPath() {
        return null;
    }

    @Override
    public Map<String, Object> getRequestCookieMap() {
        return null;
    }

    @Override
    public Map<String, String> getRequestHeaderMap() {
        return null;
    }

    @Override
    public Map<String, String[]> getRequestHeaderValuesMap() {
        return null;
    }

    @Override
    public Locale getRequestLocale() {
        return null;
    }

    @Override
    public Iterator<Locale> getRequestLocales() {
        return null;
    }

    @Override
    public Map<String, Object> getRequestMap() {
        return null;
    }

    @Override
    public Map<String, String> getRequestParameterMap() {
        return null;
    }

    @Override
    public Iterator<String> getRequestParameterNames() {
        return null;
    }

    @Override
    public Map<String, String[]> getRequestParameterValuesMap() {
        return null;
    }

    @Override
    public String getRequestPathInfo() {
        return null;
    }

    @Override
    public String getRequestServletPath() {
        return null;
    }

    @Override
    public URL getResource(String path) throws MalformedURLException {
        return classLoader.getResource(path);
    }

    @Override
    public InputStream getResourceAsStream(String path) {
        return classLoader.getResourceAsStream(path);
    }

    @Override
    public Set<String> getResourcePaths(String path) {
        try {
            Enumeration<URL> en = classLoader.getResources(path);
            Set<String> paths = new HashSet<String>();
            while (en.hasMoreElements()) {
                paths.add(en.nextElement().toURI().toString());
            }
            return paths;
        } catch (IOException e) {
            logger.warn("Fail to getResourcePaths " + path, e);
            return null;
        } catch (URISyntaxException e) {
            logger.warn("Fail to getResourcePaths " + path, e);
            return null;
        }
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    public Object getSession(boolean arg0) {
        return null;
    }

    @Override
    public Map<String, Object> getSessionMap() {
        return null;
    }

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public boolean isUserInRole(String arg0) {
        return false;
    }

    @Override
    public void log(String arg0, Throwable arg1) {
    }

    @Override
    public void log(String arg0) {
    }

    @Override
    public void redirect(String arg0) throws IOException {
    }

}
