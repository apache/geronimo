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
 * software distributed under the License get distributed on an
 * "AS get" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.geronimo.tomcat.connector;

public interface CommonProtocol {
    public boolean getAllowTrace();
    public void setAllowTrace(boolean allowTrace);
    public long getAsyncTimeout();
    public void setAsyncTimeout(long asyncTimeout);  
    public boolean getEnableLookups();
    public void setEnableLookups(boolean enableLookups);
    public int getMaxParameterCount();
    public void setMaxParameterCount(int count);
    public int getMaxPostSize();
    public void setMaxPostSize(int bytes);
    public int getMaxSavePostSize();
    public void setMaxSavePostSize(int maxPostSize);
    public String getProtocol();
    public String getTomcatProtocol();
    public String getProxyName();
    public void setProxyName(String proxyName);
    public int getProxyPort();
    public void setProxyPort(int port);
    public int getRedirectPort();
    public void setRedirectPort(int port);
    public void setScheme(String scheme);
    public String getScheme();
    public boolean getSecure();
    public void setSecure(boolean secure);
    public boolean getSslEnabled();
    public void setSslEnabled(boolean sslEnabled);
    public void setUriEncoding(String uriEncoding);
    public String getUriEncoding();
    public boolean getUseBodyEncodingForURI();
    public void setUseBodyEncodingForURI(boolean useBodyEncodingForURI);
    public void setUseIPVHosts(boolean useIPVHosts);
    public boolean getUseIPVHosts();
    public void setXpoweredBy(boolean xpoweredBy);
    public boolean getXpoweredBy();
}
