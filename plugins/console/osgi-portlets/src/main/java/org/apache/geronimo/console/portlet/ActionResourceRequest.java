/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.console.portlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.PortalContext;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletSession;
import javax.portlet.ResourceRequest;
import javax.portlet.WindowState;
import javax.servlet.http.Cookie;

/**
 * Commons-fileupload does not support portlet2.0's
 * serveResource(ResourceRequest,ResourceResponse) now.
 * PortletFileUpload.parse(ActionRequest) need a ActionRequest parameter. So
 * create a Wapper Class implements ActionRequest and delegate all operations to
 * an instance of type ResourceRequest.
 */
public class ActionResourceRequest implements ActionRequest {
	private ResourceRequest request;

	public ActionResourceRequest(ResourceRequest request) {
		this.request = request;
	}

	@Override
	public String getCharacterEncoding() {
		return request.getCharacterEncoding();
	}

	@Override
	public int getContentLength() {
		return request.getContentLength();
	}

	@Override
	public String getContentType() {
		return request.getContentType();
	}

	@Override
	public String getMethod() {
		return request.getMethod();
	}

	@Override
	public InputStream getPortletInputStream() throws IOException {
		return request.getPortletInputStream();
	}

	@Override
	public BufferedReader getReader() throws UnsupportedEncodingException,
			IOException {
		return request.getReader();
	}

	@Override
	public void setCharacterEncoding(String arg0)
			throws UnsupportedEncodingException {
		request.setCharacterEncoding(arg0);
	}

	@Override
	public Object getAttribute(String arg0) {
		return request.getAttribute(arg0);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return request.getAttributeNames();
	}

	@Override
	public String getAuthType() {
		return request.getAuthType();
	}

	@Override
	public String getContextPath() {
		return request.getContextPath();
	}

	@Override
	public Cookie[] getCookies() {
		return request.getCookies();
	}

	@Override
	public Locale getLocale() {
		return request.getLocale();
	}

	@Override
	public Enumeration<Locale> getLocales() {
		return request.getLocales();
	}

	@Override
	public String getParameter(String arg0) {
		return request.getParameter(arg0);
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		return request.getParameterMap();
	}

	@Override
	public Enumeration<String> getParameterNames() {
		return request.getParameterNames();
	}

	@Override
	public String[] getParameterValues(String arg0) {
		return request.getParameterValues(arg0);
	}

	@Override
	public PortalContext getPortalContext() {
		return request.getPortalContext();
	}

	@Override
	public PortletMode getPortletMode() {
		return request.getPortletMode();
	}

	@Override
	public PortletSession getPortletSession() {
		return request.getPortletSession();
	}

	@Override
	public PortletSession getPortletSession(boolean arg0) {
		return request.getPortletSession(arg0);
	}

	@Override
	public PortletPreferences getPreferences() {
		return request.getPreferences();
	}

	@Override
	public Map<String, String[]> getPrivateParameterMap() {
		return request.getPrivateParameterMap();
	}

	@Override
	public Enumeration<String> getProperties(String arg0) {
		return request.getProperties(arg0);
	}

	@Override
	public String getProperty(String arg0) {
		return request.getProperty(arg0);
	}

	@Override
	public Enumeration<String> getPropertyNames() {
		return request.getPropertyNames();
	}

	@Override
	public Map<String, String[]> getPublicParameterMap() {
		return request.getPublicParameterMap();
	}

	@Override
	public String getRemoteUser() {
		return request.getRemoteUser();
	}

	@Override
	public String getRequestedSessionId() {
		return request.getRequestedSessionId();
	}

	@Override
	public String getResponseContentType() {
		return request.getResponseContentType();
	}

	@Override
	public Enumeration<String> getResponseContentTypes() {
		return request.getResponseContentTypes();
	}

	@Override
	public String getScheme() {
		return request.getScheme();
	}

	@Override
	public String getServerName() {
		return request.getServerName();
	}

	@Override
	public int getServerPort() {
		return request.getServerPort();
	}

	@Override
	public Principal getUserPrincipal() {
		return request.getUserPrincipal();
	}

	@Override
	public String getWindowID() {
		return request.getWindowID();
	}

	@Override
	public WindowState getWindowState() {
		return request.getWindowState();
	}

	@Override
	public boolean isPortletModeAllowed(PortletMode arg0) {
		return request.isPortletModeAllowed(arg0);
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		return request.isRequestedSessionIdValid();
	}

	@Override
	public boolean isSecure() {
		return request.isSecure();
	}

	@Override
	public boolean isUserInRole(String arg0) {
		return request.isUserInRole(arg0);
	}

	@Override
	public boolean isWindowStateAllowed(WindowState arg0) {
		return request.isWindowStateAllowed(arg0);
	}

	@Override
	public void removeAttribute(String arg0) {
		request.removeAttribute(arg0);
	}

	@Override
	public void setAttribute(String arg0, Object arg1) {
		request.setAttribute(arg0, arg1);
	}

}
