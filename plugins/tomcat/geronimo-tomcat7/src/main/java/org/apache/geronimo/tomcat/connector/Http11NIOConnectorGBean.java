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

import java.util.Map;

import org.apache.catalina.connector.Connector;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.Persistent;
import org.apache.geronimo.management.geronimo.WebManager;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.tomcat.TomcatContainer;

@GBean(name="Tomcat Connector HTTP NIO")
public class Http11NIOConnectorGBean extends AbstractHttp11ConnectorGBean implements Http11NIOProtocol{

    public Http11NIOConnectorGBean(@ParamAttribute(manageable=false, name = "name") String name,
                                   @ParamAttribute(manageable=false, name = "initParams") Map<String, String> initParams,
                                   @ParamAttribute(manageable=false, name = "host") String host,
                                   @ParamAttribute(manageable=false, name = "port") int port,
                                   @ParamReference(name = "TomcatContainer") TomcatContainer container,
                                   @ParamReference(name = "ServerInfo") ServerInfo serverInfo,
                                   @ParamAttribute(manageable=false, name = "connector") Connector conn)  throws Exception {

        super(name, initParams, "org.apache.coyote.http11.Http11NioProtocol", host, port, container, serverInfo, conn);
    }

    public int getDefaultPort() {
        return 80;
    }

    public String getGeronimoProtocol(){
        return WebManager.PROTOCOL_HTTP;
    }

    public int getAcceptorThreadCount() {
        Object value = connector.getAttribute("acceptorThreadCount");
        return value == null ? 1 : Integer.parseInt(value.toString());
    }

    public int getAcceptorThreadPriority() {
        Object value = connector.getAttribute("acceptorThreadCount");
        return value == null ? Thread.NORM_PRIORITY : Integer.parseInt(value.toString());
    }

    public boolean getCommand_line_options() {
        Object value = connector.getAttribute("command-line-options");
        return value == null ? true : Boolean.valueOf(value.toString());
    }

    public int getOomParachute() {
        Object value = connector.getAttribute("oomParachute");
        return value == null ? 1048576 : Integer.parseInt(value.toString());
    }

    public int getPollerThreadCount() {
        Object value = connector.getAttribute("pollerThreadCount");
        return value == null ? 1 : Integer.parseInt(value.toString());
    }

    public int getPollerThreadPriority() {
        Object value = connector.getAttribute("pollerThreadCount");
        return value == null ? Thread.NORM_PRIORITY : Integer.parseInt(value.toString());
    }

    public int getProcessCache() {
        Object value = connector.getAttribute("processCache");
        return value == null ? 200 : Integer.parseInt(value.toString());
    }

    public int getSelectorPool_maxSelectors() {
        Object value = connector.getAttribute("selectorPool.maxSelectors");
        return value == null ? 200 : Integer.parseInt(value.toString());
    }

    public int getSelectorPool_maxSpareSelectors() {
        Object value = connector.getAttribute("selectorPool.maxSpareSelectors");
        return value == null ? -1 : Integer.parseInt(value.toString());
    }

    public int getSelectorTimeout() {
        Object value = connector.getAttribute("selectorTimeout");
        return value == null ? 1000 : Integer.parseInt(value.toString());
    }

    public int getSocket_appReadBufSize() {
        Object value = connector.getAttribute("socket.appReadBufSize");
        return value == null ? 8192 : Integer.parseInt(value.toString());
    }

    public int getSocket_appWriteBufSize() {
        Object value = connector.getAttribute("socket.appWriteBufSize");
        return value == null ? 8192 : Integer.parseInt(value.toString());
    }

    public int getSocket_bufferPool() {
        Object value = connector.getAttribute("socket.bufferPool");
        return value == null ? 500 : Integer.parseInt(value.toString());
    }

    public int getSocket_bufferPoolSize() {
        Object value = connector.getAttribute("socket.bufferPoolSize");
        return value == null ? 104857600 : Integer.parseInt(value.toString());
    }

    public boolean getSocket_directBuffer() {
        Object value = connector.getAttribute("socket.directBuffer");
        return value == null ? false : Boolean.valueOf(value.toString());
    }

    public int getSocket_eventCache() {
        Object value = connector.getAttribute("socket.eventCache");
        return value == null ? 500 : Integer.parseInt(value.toString());
    }

    public int getSocket_keyCache() {
        Object value = connector.getAttribute("socket.keyCache");
        return value == null ? 500 : Integer.parseInt(value.toString());
    }

    public boolean getSocket_ooBInline() {
        Object value = connector.getAttribute("socket.ooBInline");
        return value == null ? true : Boolean.valueOf(value.toString());
    }

    public int getSocket_performanceBandwidth() {
        Object value = connector.getAttribute("socket.performanceBandwidth");
        return value == null ? 1 : Integer.parseInt(value.toString());
    }

    public int getSocket_performanceConnectionTime() {
        Object value = connector.getAttribute("socket.performanceConnectionTime");
        return value == null ? 1 : Integer.parseInt(value.toString());
    }

    public int getSocket_performanceLatency() {
        Object value = connector.getAttribute("socket.performanceLatency");
        return value == null ? 0 : Integer.parseInt(value.toString());
    }

    public int getSocket_processorCache() {
        Object value = connector.getAttribute("socket.processorCache");
        return value == null ? 500 : Integer.parseInt(value.toString());
    }

    public int getSocket_rxBufSize() {
        Object value = connector.getAttribute("socket.rxBufSize");
        return value == null ? 25188 : Integer.parseInt(value.toString());
    }

    public boolean getSocket_soKeepAlive() {
        Object value = connector.getAttribute("socket.soKeepAlive");
        return value == null ? false : Boolean.valueOf(value.toString());
    }

    public boolean getSocket_soLingerOn() {
        Object value = connector.getAttribute("socket.soLingerOn");
        return value == null ? true : Boolean.valueOf(value.toString());
    }

    public int getSocket_soLingerTime() {
        Object value = connector.getAttribute("socket.soLingerTime");
        return value == null ? 25 : Integer.parseInt(value.toString());
    }

    public boolean getSocket_soReuseAddress() {
        Object value = connector.getAttribute("socket.soReuseAddress");
        return value == null ? true : Boolean.valueOf(value.toString());
    }

    public int getSocket_soTimeout() {
        Object value = connector.getAttribute("socket.soTimeout");
        return value == null ? 5000 : Integer.parseInt(value.toString());
    }

    public int getSocket_soTrafficClass() {
        Object value = connector.getAttribute("socket.soTrafficClass");
        return value == null ? (0x04 | 0x08 | 0x010) : new Integer(value.toString()).intValue();
    }

    public boolean getSocket_tcpNoDelay() {
        Object value = connector.getAttribute("socket.tcpNoDelay");
        return value == null ? false : Boolean.valueOf(value.toString());
    }

    public int getSocket_txBufSize() {
        Object value = connector.getAttribute("socket.txBufSize");
        return value == null ? 43800 : Integer.parseInt(value.toString());
    }

    public boolean getUseComet() {
        Object value = connector.getAttribute("useComet");
        return value == null ? true : Boolean.valueOf(value.toString());
    }

    public boolean getUseExecutor() {
        Object value = connector.getAttribute("useExecutor");
        return value == null ? true : Boolean.valueOf(value.toString());
    }

    public boolean getUseSendfile() {
        Object value = connector.getAttribute("useSendfile");
        return value == null ? true : Boolean.valueOf(value.toString());
    }

    @Persistent(manageable=false)
    public void setAcceptorThreadCount(int acceptorThreadCount) {
        connector.setAttribute("acceptorThreadCount", Integer.valueOf(acceptorThreadCount));
    }

    @Persistent(manageable=false)
    public void setAcceptorThreadPriority(int acceptorThreadPriority) {
        connector.setAttribute("acceptorThreadPriority", Integer.valueOf(acceptorThreadPriority));
    }

    @Persistent(manageable=false)
    public void setCommand_line_options(boolean command_line_options) {
        connector.setAttribute("command-line-options", Boolean.valueOf(command_line_options));
    }

    @Persistent(manageable=false)
    public void setOomParachute(int oomParachute) {
        connector.setAttribute("oomParachute", Integer.valueOf(oomParachute));
    }

    @Persistent(manageable=false)
    public void setPollerThreadCount(int pollerThreadCount) {
        connector.setAttribute("pollerThreadCount", Integer.valueOf(pollerThreadCount));
    }

    @Persistent(manageable=false)
    public void setPollerThreadPriority(int pollerThreadPriority) {
        connector.setAttribute("pollerThreadPriority", Integer.valueOf(pollerThreadPriority));
    }

    @Persistent(manageable=false)
    public void setProcessCache(int processCache) {
        connector.setAttribute("processCache", Integer.valueOf(processCache));
    }

    @Persistent(manageable=false)
    public void setSelectorPool_maxSelectors(int selectorPool_maxSelectors) {
        connector.setAttribute("selectorPool.maxSelectors", Integer.valueOf(selectorPool_maxSelectors));
    }

    @Persistent(manageable=false)
    public void setSelectorPool_maxSpareSelectors(int selectorPool_maxSpareSelectors) {
        connector.setAttribute("selectorPool.maxSpareSelectors", Integer.valueOf(selectorPool_maxSpareSelectors));
    }

    @Persistent(manageable=false)
    public void setSelectorTimeout(int selectorTimeout) {
        connector.setAttribute("selectorTimeout", Integer.valueOf(selectorTimeout));
    }

    @Persistent(manageable=false)
    public void setSocket_appReadBufSize(int socket_appReadBufSize) {
        connector.setAttribute("socket.appReadBufSize", Integer.valueOf(socket_appReadBufSize));
    }

    @Persistent(manageable=false)
    public void setSocket_appWriteBufSize(int socket_appWriteBufSize) {
        connector.setAttribute("socket.appWriteBufSize", Integer.valueOf(socket_appWriteBufSize));
    }

    @Persistent(manageable=false)
    public void setSocket_bufferPool(int socket_bufferPool) {
        connector.setAttribute("socket.bufferPool", Integer.valueOf(socket_bufferPool));
    }

    @Persistent(manageable=false)
    public void setSocket_bufferPoolSize(int socket_bufferPoolSize) {
        connector.setAttribute("socket.bufferPoolSize", Integer.valueOf(socket_bufferPoolSize));
    }

    @Persistent(manageable=false)
    public void setSocket_directBuffer(boolean socket_directBuffer) {
        connector.setAttribute("socket.directBuffer", Boolean.valueOf(socket_directBuffer));
    }

    @Persistent(manageable=false)
    public void setSocket_eventCache(int socket_eventCache) {
        connector.setAttribute("socket.eventCache", Integer.valueOf(socket_eventCache));
    }

    @Persistent(manageable=false)
    public void setSocket_keyCache(int socket_keyCache) {
        connector.setAttribute("socket.keyCache", Integer.valueOf(socket_keyCache));
    }

    @Persistent(manageable=false)
    public void setSocket_ooBInline(boolean socket_ooBInline) {
        connector.setAttribute("socket.ooBInline", Boolean.valueOf(socket_ooBInline));
    }

    @Persistent(manageable=false)
    public void setSocket_performanceBandwidth(int socket_performanceBandwidth) {
        connector.setAttribute("socket.performanceBandwidth", Integer.valueOf(socket_performanceBandwidth));
    }

    @Persistent(manageable=false)
    public void setSocket_performanceConnectionTime(int socket_performanceConnectionTime) {
        connector.setAttribute("socket.performanceConnectionTime", Integer.valueOf(socket_performanceConnectionTime));
    }

    @Persistent(manageable=false)
    public void setSocket_performanceLatency(int socket_performanceLatency) {
        connector.setAttribute("socket.performanceLatency", Integer.valueOf(socket_performanceLatency));
    }

    @Persistent(manageable=false)
    public void setSocket_processorCache(int socket_processorCache) {
        connector.setAttribute("socket.processorCache", Integer.valueOf(socket_processorCache));
    }

    @Persistent(manageable=false)
    public void setSocket_rxBufSize(int socket_rxBufSize) {
        connector.setAttribute("socket.rxBufSize", Integer.valueOf(socket_rxBufSize));
    }

    @Persistent(manageable=false)
    public void setSocket_soKeepAlive(boolean socket_soKeepAlive) {
        connector.setAttribute("socket.soKeepAlive", Boolean.valueOf(socket_soKeepAlive));
    }

    @Persistent(manageable=false)
    public void setSocket_soLingerOn(boolean socket_soLingerOn) {
        connector.setAttribute("socket.soLingerOn", Boolean.valueOf(socket_soLingerOn));
    }

    @Persistent(manageable=false)
    public void setSocket_soLingerTime(int socket_soLingerTime) {
        connector.setAttribute("socket.soLingerTime", Integer.valueOf(socket_soLingerTime));
    }

    @Persistent(manageable=false)
    public void setSocket_soReuseAddress(boolean socket_soReuseAddress) {
        connector.setAttribute("socket.soReuseAddress", Boolean.valueOf(socket_soReuseAddress));
    }

    @Persistent(manageable=false)
    public void setSocket_soTimeout(int socket_soTimeout) {
        connector.setAttribute("socket.soTimeout", Integer.valueOf(socket_soTimeout));
    }

    @Persistent(manageable=false)
    public void setSocket_soTrafficClass(int socket_soTrafficClass) {
        connector.setAttribute("socket.soTrafficClass", Integer.valueOf(socket_soTrafficClass));
    }

    @Persistent(manageable=false)
    public void setSocket_tcpNoDelay(boolean socket_tcpNoDelay) {
        connector.setAttribute("socket.tcpNoDelay", Boolean.valueOf(socket_tcpNoDelay));
    }

    @Persistent(manageable=false)
    public void setSocket_txBufSize(int socket_txBufSize) {
        connector.setAttribute("socket.txBufSize", Integer.valueOf(socket_txBufSize));
    }

    @Persistent(manageable=false)
    public void setUseComet(boolean useComet) {
        connector.setAttribute("useExecutor", Boolean.valueOf(useComet));
    }

    @Persistent(manageable=false)
    public void setUseExecutor(boolean useExecutor) {
        connector.setAttribute("useExecutor", Boolean.valueOf(useExecutor));
    }

    @Persistent(manageable=false)
    public void setUseSendfile(boolean useSendfile) {
        connector.setAttribute("useSendfile", Boolean.valueOf(useSendfile));
    }


}
