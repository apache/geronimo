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
import org.apache.geronimo.datastore.impl.remote.messaging.CommandRequest;
import org.apache.geronimo.datastore.impl.remote.messaging.CommandResult;
import org.apache.geronimo.datastore.impl.remote.messaging.Connector;
import org.apache.geronimo.datastore.impl.remote.messaging.HeaderOutInterceptor;
import org.apache.geronimo.datastore.impl.remote.messaging.Msg;
import org.apache.geronimo.datastore.impl.remote.messaging.MsgBody;
import org.apache.geronimo.datastore.impl.remote.messaging.MsgHeader;
import org.apache.geronimo.datastore.impl.remote.messaging.MsgHeaderConstants;
import org.apache.geronimo.datastore.impl.remote.messaging.MsgOutInterceptor;
import org.apache.geronimo.datastore.impl.remote.messaging.NodeInfo;
import org.apache.geronimo.datastore.impl.remote.messaging.RequestSender;
import org.apache.geronimo.datastore.impl.remote.messaging.ServerNodeContext;
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanContext;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.WaitingException;

/**
 * GFileManager mirroring a GFileManagerProxy mounted by a remote node.
 * <BR>
 * Operations peformed against this instance are actually executed against the
 * remote GFileManagerProxy having the same name than this instance. 
 *
 * @version $Revision: 1.4 $ $Date: 2004/03/11 15:36:13 $
 */
public class GFileManagerClient
    implements GBean, GFileManager, Connector
{

    private static final Log log = LogFactory.getLog(GFileManagerClient.class);

    /**
     * Name of the proxy to be mirrored. 
     */
    private final String name;
    
    /**
     * Node hosting the proxy to be mirrored.
     */
    private final NodeInfo node; 

    /**
     * Context of the ServerNode which has mounted this instance.
     */
    protected ServerNodeContext serverNodeContext;
    
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
     * @param aNodeName Name of the node hosting the proxy.
     */
    public GFileManagerClient(String aName, NodeInfo aNode) {
        if ( null == aName ) {
            throw new IllegalArgumentException("Name is required.");
        } else if ( null == aNode ) {
            throw new IllegalArgumentException("Node is required.");
        }
        name = aName;
        node = aNode;
    }
    
    public String getName() {
        return name;
    }
    
    /**
     * Gets the NodeInfo of the node hosting the GFileManagerProxy mirrored by
     * this instance.
     * 
     * @return Hosting node info.
     */
    public NodeInfo getHostingNode() {
        return node;
    }
    
    public void start() {
        sender.sendSyncRequest(new CommandRequest("start", null), out, node);
    }

    public GFile factoryGFile(String aPath) {
        GFileStub result = (GFileStub)
            sender.sendSyncRequest(
                new CommandRequest("factoryGFile", new Object[] {aPath}), out,
                    node);
        result.setGFileManagerClient(this);
        return result;
    }
    
    public void persistNew(GFile aFile) {
        sender.sendSyncRequest(
            new CommandRequest("persistNew", new Object[] {aFile}), out, node);
    }

    public void persistUpdate(GFile aFile) {
        sender.sendSyncRequest(
            new CommandRequest("persistUpdate", new Object[] {aFile}), out,
                node);
    }

    public void persistDelete(GFile aFile) {
        sender.sendSyncRequest(
            new CommandRequest("persistDelete", new Object[] {aFile}), out,
                node);
    }

    public void end() throws GFileManagerException {
        sender.sendSyncRequest(new CommandRequest("end", null), out, node);
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

    public void setContext(ServerNodeContext aContext) {
        serverNodeContext = aContext;
        sender = aContext.getRequestSender();
        out = aContext.getOutput();
        if ( null != out ) {
            out =
                new HeaderOutInterceptor(
                    MsgHeaderConstants.DEST_CONNECTOR,
                    name, out);
        }
    }
    
    public void deliver(Msg aMsg) {
        MsgHeader header = aMsg.getHeader();
        MsgBody body = aMsg.getBody();
        
        CommandResult result = (CommandResult) body.getContent();
        sender.setResponse(
            header.getHeader(MsgHeaderConstants.CORRELATION_ID), result);
    }
    
    /**
     * Used by GFileStubs to send request to their corresponding GFile on the
     * proxy side.
     * 
     * @param aStub GFileStub sending the request.
     * @param aRequest Request.
     * @return Request result.
     */
    protected Object sendGFileRequest(GFileStub aStub, CommandRequest aRequest) {
        CommandResult result = (CommandResult) 
            sender.sendSyncRequest(new CommandRequest("executeOnGFile",
                new Object[] {aStub.getID(), aRequest}), out, node);
        if ( result.isSuccess() ) {
            return result.getResult();
        }
        throw new RuntimeException(result.getException());
    }
    
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory factory = new GBeanInfoFactory(GFileManagerClient.class);
        factory.setConstructor(
            new String[] {"Name", "NodeName"},
            new Class[] {String.class, String.class});
        factory.addAttribute("Name", true);
        factory.addAttribute("NodeName", true);
        factory.addOperation("start");
        factory.addOperation("factoryGFile", new Class[]{String.class});
        factory.addOperation("persistNew", new Class[]{GFile.class});
        factory.addOperation("persistUpdate", new Class[]{GFile.class});
        factory.addOperation("persistDelete", new Class[]{GFile.class});
        factory.addOperation("end");
        GBEAN_INFO = factory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
    
}
