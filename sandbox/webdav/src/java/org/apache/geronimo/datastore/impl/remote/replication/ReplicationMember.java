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

package org.apache.geronimo.datastore.impl.remote.replication;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.geronimo.datastore.impl.remote.messaging.CommandRequest;
import org.apache.geronimo.datastore.impl.remote.messaging.CommandResult;
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
 * A replication group member.
 * <BR>
 * This is a Connector in charge of replicating the state of registered
 * ReplicantCapables across N-nodes, which constitute a replication group.
 * <BR>
 * Replication members are organized as follow:
 * <pre>
 * ReplicationMember -- MTO -- ServerNode -- MTM -- ServerNode -- OTM -- ReplicationMember
 * </pre>
 *
 * @version $Revision: 1.1 $ $Date: 2004/03/03 15:27:32 $
 */
public class ReplicationMember
    implements UpdateListener, Connector, GBean
{

    /**
     * Name of the replication group.
     */
    private final String name;
    
    /**
     * ReplicantID to ReplicantCapable Map.
     */
    private final Map idToReplicant;
    
    /**
     * Names of the nodes hosting the other members of the replication group
     * of this member.
     */
    private String[] targetNodes;
    
    /**
     * Output to be used to send requests.
     */
    private MsgOutInterceptor requestOut;
    
    /**
     * Output to be used to send results.
     */
    private MsgOutInterceptor resultOut;
    
    /**
     * Requests sender.
     */
    private final RequestSender sender;
    
    /**
     * Creates a replication group member.
     * 
     * @param aName Name of the replication group owning this member.
     * @param aTargetNodes Names of the nodes hosting the other members of the
     * replication group containing this member.
     */
    public ReplicationMember(String aName, String[] aTargetNodes) {
        if ( null == aName ) {
            throw new IllegalArgumentException("Name is required");
        } else if ( null == aTargetNodes ) {
            throw new IllegalArgumentException("Node names is required");
        }
        name = aName;
        targetNodes = aTargetNodes;
        idToReplicant = new HashMap();
        sender = new RequestSender();
    }
    
    public String getName() {
        return name;
    }

    public void fireUpdateEvent(UpdateEvent anEvent) {
        // One does not send the actual ReplicantCapable in the case of an
        // update. Instead, one sends only its identifier.
        ReplicationCapable target = (ReplicationCapable) anEvent.getTarget();
        anEvent.setTarget(target.getID());
        sender.sendSyncRequest(
            new CommandRequest("mergeWithUpdate", new Object[] {anEvent}),
            requestOut);
    }

    /**
     * Merges an UpdateEvent with a registered ReplicationCapable.
     * 
     * @param anEvent Update event to be merged.
     * @throws ReplicationException Indicates that the merge can not be
     * performed.
     */
    public void mergeWithUpdate(UpdateEvent anEvent)
        throws ReplicationException {
        ReplicantID id = (ReplicantID) anEvent.getTarget();
        ReplicationCapable replicationCapable;
        synchronized(idToReplicant) {
            replicationCapable = (ReplicationCapable) idToReplicant.get(id);
        }
        if ( null == replicationCapable ) {
            throw new ReplicationException(
                "No ReplicantCapable with the id {" + id + "}");
        }
        replicationCapable.mergeWithUpdate(anEvent);
    }
    
    /**
     * Registers a ReplicantCapable. From now, UpdateEvents multicasted
     * by the provided ReplicantCapable are also pushed to the replication
     * group.
     * 
     * @param aReplicant ReplicantCapable to be controlled by this group.
     */
    public void registerReplicantCapable(ReplicationCapable aReplicant) {
        ReplicantID id = new ReplicantID();
        aReplicant.setID(id);
        sender.sendSyncRequest(
            new CommandRequest("registerLocalReplicantCapable",
                new Object[] {aReplicant}),
            requestOut);
        synchronized(idToReplicant) {
            idToReplicant.put(id, aReplicant);
            aReplicant.addUpdateListener(this);
        }
    }
    
    /**
     * This method is for internal use only.
     * <BR>
     * It registers with this member a ReplicationCapable, which has been
     * registered by a remote member.  
     * 
     * @param aReplicant ReplicantCapable to be locally registered.
     */
    public void registerLocalReplicantCapable(ReplicationCapable aReplicant) {
        synchronized(idToReplicant) {
            aReplicant.addUpdateListener(this);
            idToReplicant.put(aReplicant.getID(), aReplicant);
        }
    }
    
    /**
     * Retrieves the ReplicationCapable having the specified id.
     * 
     * @param anID Replicant identifier.
     * @return ReplicantCapable having the specified id or null if such an
     * identifier is not known.
     */
    public ReplicationCapable retrieveReplicantCapable(Object anID) {
        synchronized(idToReplicant) {
            return (ReplicationCapable) idToReplicant.get(anID);
        }
    }
    
    public void setOutput(MsgOutInterceptor anOut) {
        if ( null != anOut ) {
            MsgOutInterceptor out =
                new HeaderOutInterceptor(
                    MsgHeaderConstants.DEST_CONNECTOR,
                    name,
                    new HeaderOutInterceptor(
                        MsgHeaderConstants.DEST_NODE,
                        targetNodes,
                        anOut));
            requestOut =
                new HeaderOutInterceptor(
                    MsgHeaderConstants.BODY_TYPE,
                    MsgBody.Type.REQUEST,
                    out);
            resultOut = 
                new HeaderOutInterceptor(
                    MsgHeaderConstants.BODY_TYPE,
                    MsgBody.Type.RESPONSE,
                    out);
        } else {
            requestOut = null;
            resultOut = null;
        }
    }

    public void deliver(Msg aMsg) {
        MsgHeader header = aMsg.getHeader();
        MsgBody.Type bodyType =
        (MsgBody.Type) header.getHeader(MsgHeaderConstants.BODY_TYPE);
        if ( bodyType.equals(MsgBody.Type.REQUEST) ) {
            handleRequest(aMsg);
        } else if ( bodyType.equals(MsgBody.Type.RESPONSE) ) {
            handleResponse(aMsg);
        }
    }
    
    /**
     * Handles a request Msg.
     * 
     * @param aMsg Request Msg to be handled.
     */
    protected void handleRequest(Msg aMsg) {
        MsgBody body = aMsg.getBody();
        MsgHeader header = aMsg.getHeader();
        Object sourceNode = header.getHeader(MsgHeaderConstants.SRC_NODE);
        Object id = header.getHeader(MsgHeaderConstants.CORRELATION_ID);
        CommandRequest command;
        String gateway;
        command = (CommandRequest) body.getContent();
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
                    MsgHeaderConstants.DEST_NODE,
                    targetNodes,
                    new HeaderOutInterceptor(
                        MsgHeaderConstants.DEST_CONNECTOR,
                        name,
                        resultOut)));
        reqOut.push(msg);
    }

    /**
     * Handles a response Msg.
     * 
     * @param aMsg Response to be handled.
     */
    protected void handleResponse(Msg aMsg) {
        MsgBody body = aMsg.getBody();
        MsgHeader header = aMsg.getHeader();
        CommandResult result;
        result = (CommandResult) body.getContent();
        sender.setResponse(
            (Integer) header.getHeader(MsgHeaderConstants.CORRELATION_ID),
            result);
    }
    
    public void setGBeanContext(GBeanContext context) {
    }

    public void doStart() throws WaitingException, Exception {
    }

    public void doStop() throws WaitingException, Exception {
    }

    public void doFail() {
    }

    /**
     * ReplicantCapable identifier. 
     */
    private static class ReplicantID implements Serializable {
        private static volatile int seqId = 0;
        private final int id;
        private ReplicantID() {
            id = seqId++;
        }
        public int hashCode() {
            // TODO improve me.
            return id;
        }
        public boolean equals(Object obj) {
            if ( false == obj instanceof ReplicantID ) {
               return false;
            }
            ReplicantID replicantID = (ReplicantID) obj;
            return id == replicantID.id;
        }
    }

}
