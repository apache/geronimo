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

import java.util.HashMap;
import java.util.Map;

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
import org.apache.geronimo.datastore.impl.remote.messaging.MsgBody;
import org.apache.geronimo.datastore.impl.remote.messaging.MsgHeader;
import org.apache.geronimo.datastore.impl.remote.messaging.MsgHeaderConstants;
import org.apache.geronimo.datastore.impl.remote.messaging.MsgOutInterceptor;
import org.apache.geronimo.datastore.impl.remote.messaging.Node;
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;

/**
 * It is a wrapper/proxy for a GFileManager, whose services need to be exposed
 * via a ServerNode.
 *
 * @version $Revision: 1.4 $ $Date: 2004/03/24 11:42:57 $
 */
public class GFileManagerProxy
    extends AbstractConnector
    implements GBean, GFileManager, Connector
{

    private static final Log log = LogFactory.getLog(GFileManagerProxy.class);

    /**
     * Used to allocate GFileStub identifiers.
     */
    private volatile int gFileSeq; 
    
    /**
     * Proxied GFileManager.
     */
    private final GFileManager fileManager;

    /**
     * It is a mapping of GFileStub identifiers to actual GFiles.
     */
    private Map gFiles;
    
    /**
     * Builds a proxy for the provided GFileManager.
     * 
     * @param aNode Node containing this instance.
     * @param aFileManager GFileManager to be proxied by this instance.
     */
    public GFileManagerProxy(Node aNode,
        GFileManager aFileManager) {
        super(aNode);
        if ( null == aFileManager ) {
            throw new IllegalArgumentException("GFileManager is required.");
        }
        fileManager = aFileManager;
        gFiles = new HashMap();
        gFileSeq = 0;
    }
    
    public String getName() {
        return fileManager.getName();
    }

    public GFile factoryGFile(Object anOpaque, String aPath)
        throws GFileManagerException {
        GFile gFile = fileManager.factoryGFile(anOpaque, aPath);
        Integer id = registerGFile(gFile);
        return new GFileStub(aPath, id);
    }
    
    public void persistNew(Object anOpaque, GFile aFile) {
        GFileStub stub = (GFileStub) aFile;
        GFile actualGFile = retrieveGFile(stub.getID());
        fileManager.persistNew(anOpaque, actualGFile);
    }

    public void persistUpdate(Object anOpaque, GFile aFile) {
        GFileStub stub = (GFileStub) aFile;
        GFile actualGFile = retrieveGFile(stub.getID());
        fileManager.persistUpdate(anOpaque, actualGFile);
    }

    public void persistDelete(Object anOpaque, GFile aFile) {
        GFileStub stub = (GFileStub) aFile;
        GFile actualGFile = retrieveGFile(stub.getID());
        fileManager.persistDelete(anOpaque, actualGFile);
    }

    public Object startInteraction() {
        return fileManager.startInteraction();
    }

    public void endInteraction(Object anOpaque) throws GFileManagerException {
        fileManager.endInteraction(anOpaque);
    }
    
    /**
     * Execute a request on the GFile identified by anID.
     * 
     * @param anID GFile identifier.
     * @param aRequest Request to be executed against the GFile identified by
     * anID.
     * @return Request result. 
     */
    public CommandResult executeOnGFile(Integer anID, CommandRequest aRequest) {
        GFile gFile = retrieveGFile(anID);
        aRequest.setTarget(gFile);
        return aRequest.execute();
    }
    
    protected void handleRequest(Msg aMsg) {
        MsgHeader header = aMsg.getHeader();
        MsgBody body = aMsg.getBody();
        
        Object id = header.getHeader(MsgHeaderConstants.CORRELATION_ID);
        Object srcNode = header.getHeader(MsgHeaderConstants.SRC_NODE);

        CommandRequest command = (CommandRequest) body.getContent();

        command.setTarget(this);
        
        CommandResult result = command.execute();
        Msg msg = new Msg();
        body = msg.getBody();
        body.setContent(result);
        MsgOutInterceptor reqOut =
            new HeaderOutInterceptor(
                MsgHeaderConstants.CORRELATION_ID,
                id,
                new HeaderOutInterceptor(
                    MsgHeaderConstants.DEST_CONNECTOR,
                    getName(),
                    new HeaderOutInterceptor(
                        MsgHeaderConstants.DEST_NODES,
                        srcNode,
                        out)));
        reqOut.push(msg);
    }
    
    
    /**
     * Registers a GFile.
     * 
     * @param aFile GFile to be registered.
     * @return Identifier of this GFile. This identifier is used to build a
     * GFileStub mirroring aFile on the ServantNode side.
     */
    private Integer registerGFile(GFile aFile) {
        Integer id = new Integer(++gFileSeq);
        synchronized(gFiles) {
            gFiles.put(id, aFile);
        }
        return id;
    }

    /**
     * Retrieves the GFile having the specified identifier.
     *  
     * @param anID Id of the GFile to be returned.
     * @return GFile.
     */
    protected GFile retrieveGFile(Integer anID) {
        GFile gFile;
        synchronized(gFiles) {
            gFile = (GFile) gFiles.get(anID);
        }
        if ( null == gFile ) {
            throw new IllegalArgumentException("GFileStub {" + anID + 
                "} is not registered.");
        }
        return gFile;
    }
    
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory factory = new GBeanInfoFactory(GFileManagerProxy.class, AbstractConnector.GBEAN_INFO);
        factory.setConstructor(
            new String[] {"Node", "Delegate"},
            new Class[] {Node.class, GFileManager.class});
        factory.addReference("Delegate", GFileManager.class);
        GBEAN_INFO = factory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
    
}
