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
import org.apache.geronimo.datastore.impl.remote.messaging.AbstractConnector;
import org.apache.geronimo.datastore.impl.remote.messaging.CommandRequest;
import org.apache.geronimo.datastore.impl.remote.messaging.CommandResult;
import org.apache.geronimo.datastore.impl.remote.messaging.Connector;
import org.apache.geronimo.datastore.impl.remote.messaging.HeaderOutInterceptor;
import org.apache.geronimo.datastore.impl.remote.messaging.Msg;
import org.apache.geronimo.datastore.impl.remote.messaging.MsgHeaderConstants;
import org.apache.geronimo.datastore.impl.remote.messaging.Node;
import org.apache.geronimo.datastore.impl.remote.messaging.NodeInfo;
import org.apache.geronimo.datastore.impl.remote.messaging.NodeContext;
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;

/**
 * GFileManager mirroring a GFileManagerProxy mounted by a remote node.
 * <BR>
 * Operations peformed against this instance are actually executed against the
 * remote GFileManagerProxy having the same name than this instance. 
 *
 * @version $Revision: 1.5 $ $Date: 2004/03/24 11:42:57 $
 */
public class GFileManagerClient
    extends AbstractConnector
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
     * Creates a client having the specified name. The name MUST be the name
     * of the proxy to be mirrored.
     * 
     * @param aName Name of the proxy to be mirrored.
     * @param aNode Node hosting the proxy.
     */
    public GFileManagerClient(Node aNode,
        String aName, NodeInfo aNodeInfo) {
        super(aNode);
        if ( null == aName ) {
            throw new IllegalArgumentException("Name is required.");
        } else if ( null == aNode ) {
            throw new IllegalArgumentException("Node is required.");
        }
        name = aName;
        node = aNodeInfo;
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
    
    public Object startInteraction() {
        return sender.sendSyncRequest(
            new CommandRequest("startInteraction", null), out, node);
    }

    public GFile factoryGFile(Object anOpaque, String aPath) {
        GFileStub result = (GFileStub)
            sender.sendSyncRequest(
                new CommandRequest("factoryGFile",
                    new Object[] {anOpaque, aPath}), out,
                    node);
        result.setGFileManagerClient(this);
        return result;
    }
    
    public void persistNew(Object anOpaque, GFile aFile) {
        sender.sendSyncRequest(
            new CommandRequest("persistNew",
                new Object[] {anOpaque, aFile}), out, node);
    }

    public void persistUpdate(Object anOpaque, GFile aFile) {
        sender.sendSyncRequest(
            new CommandRequest("persistUpdate",
                new Object[] {anOpaque, aFile}), out,
                node);
    }

    public void persistDelete(Object anOpaque, GFile aFile) {
        sender.sendSyncRequest(
            new CommandRequest("persistDelete",
                new Object[] {anOpaque, aFile}), out,
                node);
    }

    public void endInteraction(Object anOpaque) throws GFileManagerException {
        sender.sendSyncRequest(new CommandRequest("endInteraction",
            new Object[] {anOpaque}), out, node);
    }
    
    public void setContext(NodeContext aContext) {
        super.setContext(aContext);
        if ( null != out ) {
            out =
                new HeaderOutInterceptor(
                    MsgHeaderConstants.DEST_CONNECTOR,
                    name, out);
        }
    }
    
    protected void handleRequest(Msg aMsg) {
        throw new IllegalArgumentException("Client-side does not handle requests.");
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
        GBeanInfoFactory factory = new GBeanInfoFactory(GFileManagerClient.class, AbstractConnector.GBEAN_INFO);
        factory.setConstructor(
            new String[] {"Node", "Name", "HostingNode"},
            new Class[] {Node.class, String.class, NodeInfo.class});
        factory.addAttribute("HostingNode", true);
        GBEAN_INFO = factory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
    
}
