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

public interface Http11NIOProtocol {

    public boolean getUseSendfile();
    public void setUseSendfile(boolean useSendfile);
    public boolean getUseExecutor();
    public void setUseExecutor(boolean useExecutor);
    public int getAcceptorThreadCount();
    public void setAcceptorThreadCount(int acceptorThreadCount);
    public int getAcceptorThreadPriority();
    public void setAcceptorThreadPriority(int acceptorThreadPriority);
    public int getPollerThreadCount();
    public void setPollerThreadCount(int pollerThreadCount);
    public int getPollerThreadPriority();
    public void setPollerThreadPriority(int pollerThreadPriority);
    public int getSelectorTimeout();
    public void setSelectorTimeout(int selectorTimeout);
    public boolean getUseComet();
    public void setUseComet(boolean useComet);
    public int getProcessCache();
    public void setProcessCache(int processCache);
    public boolean getSocket_directBuffer();
    public void setSocket_directBuffer(boolean socket_directBuffer);
    public int getSocket_rxBufSize();
    public void setSocket_rxBufSize(int socket_rxBufSize);
    public int getSocket_txBufSize();
    public void setSocket_txBufSize(int socket_txBufSize);
    public int getSocket_appReadBufSize();
    public void setSocket_appReadBufSize(int socket_appReadBufSize);
    public int getSocket_appWriteBufSize();
    public void setSocket_appWriteBufSize(int socket_appWriteBufSize);
    public int getSocket_bufferPool();
    public void setSocket_bufferPool(int socket_bufferPool);
    public int getSocket_bufferPoolSize();
    public void setSocket_bufferPoolSize(int socket_bufferPoolSize);
    public int getSocket_processorCache();
    public void setSocket_processorCache(int socket_processorCache);
    public int getSocket_keyCache();
    public void setSocket_keyCache(int socket_keyCache);
    public int getSocket_eventCache();
    public void setSocket_eventCache(int socket_eventCache);
    public boolean getSocket_tcpNoDelay();
    public void setSocket_tcpNoDelay(boolean socket_tcpNoDelay);
    public boolean getSocket_soKeepAlive();
    public void setSocket_soKeepAlive(boolean socket_soKeepAlive);
    public boolean getSocket_ooBInline();
    public void setSocket_ooBInline(boolean socket_ooBInline);
    public boolean getSocket_soReuseAddress();
    public void setSocket_soReuseAddress(boolean socket_soReuseAddress);
    public boolean getSocket_soLingerOn();
    public void setSocket_soLingerOn(boolean socket_soLingerOn);
    public int getSocket_soLingerTime();
    public void setSocket_soLingerTime(int socket_soLingerTime);
    public int getSocket_soTimeout();
    public void setSocket_soTimeout(int socket_soTimeout);
    public int getSocket_soTrafficClass();
    public void setSocket_soTrafficClass(int socket_soTrafficClass);
    public int getSocket_performanceConnectionTime();
    public void setSocket_performanceConnectionTime(int socket_performanceConnectionTime);
    public int getSocket_performanceLatency();
    public void setSocket_performanceLatency(int socket_performanceLatency);
    public int getSocket_performanceBandwidth();
    public void setSocket_performanceBandwidth(int socket_performanceBandwidth);
    public int getSelectorPool_maxSelectors();
    public void setSelectorPool_maxSelectors(int selectorPool_maxSelectors);
    public int getSelectorPool_maxSpareSelectors();
    public void setSelectorPool_maxSpareSelectors(int selectorPool_maxSpareSelectors);
    public boolean getCommand_line_options();
    public void setCommand_line_options(boolean command_line_options);
    public int getOomParachute();
    public void setOomParachute(int oomParachute);
 
}
