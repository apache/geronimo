/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.datastore.impl.remote.datastore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.datastore.GFile;
import org.apache.geronimo.datastore.GFileManager;
import org.apache.geronimo.datastore.GFileManagerException;
import org.apache.geronimo.datastore.impl.remote.messaging.Connector;
import org.apache.geronimo.datastore.impl.remote.messaging.HeaderOutInterceptor;
import org.apache.geronimo.datastore.impl.remote.messaging.Msg;
import org.apache.geronimo.datastore.impl.remote.messaging.MsgBody;
import org.apache.geronimo.datastore.impl.remote.messaging.MsgHeader;
import org.apache.geronimo.datastore.impl.remote.messaging.MsgHeaderConstants;
import org.apache.geronimo.datastore.impl.remote.messaging.MsgOutInterceptor;
import org.apache.geronimo.datastore.impl.remote.messaging.RequestSender;
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanContext;
import org.apache.geronimo.gbean.WaitingException;

/**
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/25 13:36:15 $
 */
public class GFileManagerClient
    implements GBean, GFileManager, Connector
{

    private static final Log log = LogFactory.getLog(GFileManagerClient.class);

    /**
     * Name of this client. 
     */
    private final String name;
    
    /**
     * Msg output to be used by this client to communicate with its proxy.
     */
    private MsgOutInterceptor out;
    
    /**
     * Requests sender.
     */
    private RequestSender sender;

    private GBeanContext context;
    
    /**
     * Creates a client having the specified name. The name MUST be the name
     * of the proxy to be mirrored.
     * 
     * @param aName Name of the proxy to be mirrored.
     */
    public GFileManagerClient(String aName) {
        if ( null == aName ) {
            throw new IllegalArgumentException("Name is required.");
        }
        name = aName;
        sender = new RequestSender();
    }
    
    public String getName() {
        return name;
    }
    
    public void start() {
        sender.sendSyncRequest(new ProxyCommand("start", null), out);
    }

    public GFile factoryGFile(String aPath) {
        GFileStub result = (GFileStub)
            sender.sendSyncRequest(
                new ProxyCommand("factoryGFile", new Object[] {aPath}), out);
        result.setGFileManagerClient(this);
        return result;
    }
    
    public void persistNew(GFile aFile) {
        sender.sendSyncRequest(
            new ProxyCommand("persistNew", new Object[] {aFile}), out);
    }

    public void persistUpdate(GFile aFile) {
        sender.sendSyncRequest(
            new ProxyCommand("persistUpdate", new Object[] {aFile}), out);
    }

    public void persistDelete(GFile aFile) {
        sender.sendSyncRequest(
            new ProxyCommand("persistDelete", new Object[] {aFile}), out);
    }

    public void end() throws GFileManagerException {
        sender.sendSyncRequest(new ProxyCommand("end", null), out);
    }
    
    public void setGBeanContext(GBeanContext aContext) {
        context = aContext;
    }

    public void doStart() throws WaitingException, Exception {
    }

    public void doStop() throws WaitingException, Exception {
    }

    public void doFail() {
    }

    public void setOutput(MsgOutInterceptor anOut) {
        if ( null != anOut ) {
            out = new HeaderOutInterceptor(
                MsgHeaderConstants.DEST_CONNECTOR,
                name,
                anOut);
        } else {
            out = null;
        }
    }
    
    public void deliver(Msg aMsg) {
        MsgHeader header = aMsg.getHeader();
        MsgBody body = aMsg.getBody();
        
        CommandResult result = (CommandResult) body.getContent();
        sender.setResponse(
            header.getHeader(MsgHeaderConstants.CORRELATION_ID), result);
    }
    
    protected Object sendSyncRequest(Object anOpaque) {
        return sender.sendSyncRequest(anOpaque, out);
    }
    
}
