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

public interface Ajp13Protocol {
    //AJP
    public String getAddress();
    public void setAddress(String address);
    public int getBacklog();
    public void setBacklog(int backlog);
    public int getBufferSize();
    public void setBufferSize(int bufferSize);
    public int getConnectionTimeout();
    public void setConnectionTimeout(int connectionTimeout);
    public String getExecutor();
    public void setExecutor(String executor);
    public String getHost();
    public void setHost(String address);
    public int getKeepAliveTimeout();
    public void setKeepAliveTimeout(int keepAliveTimeout);
    public int getMaxThreads();
    public void setMaxThreads(int maxThreads);
    public int getMaxSpareThreads();
    public void setMaxSpareThreads(int maxSpareThreads);
    public int getMinSpareThreads();
    public void setMinSpareThreads(int minSpareThreads);
    public int getPort();
    public void setPort(int port);
    public boolean getTcpNoDelay();
    public void setTcpNoDelay(boolean tcpNoDelay);
    public boolean getTomcatAuthentication();
    public void setTomcatAuthentication(boolean tomcatAuthentication);
}
