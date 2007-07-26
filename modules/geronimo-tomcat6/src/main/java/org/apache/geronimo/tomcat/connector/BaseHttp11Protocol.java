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
package org.apache.geronimo.tomcat.connector;

public interface BaseHttp11Protocol {
    //Http
    public int getAcceptCount();
    public void setAcceptCount(int acceptCount);
    public String getAddress();
    public void setAddress(String address);
    public int getBufferSize();
    public void setBufferSize(int bufferSize);
    public String getCompressableMimeType();
    public void setCompressableMimeType(String compressableMimeType);
    public String getCompression();
    public void setCompression(String compression);
    public int getConnectionLinger();
    public void setConnectionLinger(int connectionLinger);
    public int getConnectionTimeout();
    public void setConnectionTimeout(int connectionTimeout);
    public String getExecutor();
    public void setExecutor(String executor);
    public String getHost();
    public void setHost(String host);
    public int getKeepAliveTimeout();
    public void setKeepAliveTimeout(int keepAliveTimeout);
    public boolean getDisableUploadTimeout();
    public void setDisableUploadTimeout(boolean disableUploadTimeout);
    public int getMaxHttpHeaderSize();
    public void setMaxHttpHeaderSize(int maxHttpHeaderSize);
    public int getMaxKeepAliveRequests();
    public void setMaxKeepAliveRequests(int maxKeepAliveRequests);
    public int getMaxThreads();
    public void setMaxThreads(int maxThreads);
    public int getMaxSpareThreads();
    public void setMaxSpareThreads(int maxSpareThreads);
    public int getMinSpareThreads();
    public void setMinSpareThreads(int minSpareThreads);
    public String getNoCompressionUserAgents();
    public void setNoCompressionUserAgents(String noCompressionUserAgents);
    public int getPort();
    public void setPort(int port);
    public String getRestrictedUserAgents();
    public void setRestrictedUserAgents(String restrictedUserAgents);
    public String getServer();
    public void setServer(String server);
    public int getSocketBuffer();
    public void setSocketBuffer(int socketBuffer);
    public boolean getTcpNoDelay();
    public void setTcpNoDelay(boolean tcpNoDelay);
    public int getThreadPriority();
    public void setThreadPriority(int threadPriority);

}
