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

package org.apache.geronimo.messaging.replication;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.LazyLoader;
import net.sf.cglib.proxy.MethodInterceptor;

import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.messaging.AbstractEndPoint;
import org.apache.geronimo.messaging.Node;
import org.apache.geronimo.messaging.NodeInfo;
import org.apache.geronimo.messaging.interceptors.MsgOutInterceptor;
import org.apache.geronimo.messaging.proxy.EndPointCallback;
import org.apache.geronimo.messaging.proxy.HOPPFilter;

/**
 * ReplicationMember implementation.
 *
 * @version $Revision: 1.3 $ $Date: 2004/06/02 11:29:25 $
 */
public class ReplicationMemberImpl
    extends AbstractEndPoint
    implements ReplicationMember, GBean 
{

    /**
     * ReplicantID to ReplicantCapable Map.
     */
    private final Map idToReplicant;
    
    /**
     * A proxy for the RemoteMembers of the replication group. 
     */
    private ReplicationMember otherMembers;
    
    /**
     * EndPointCallback used under the cover by otherMembers.
     */
    private final EndPointCallback endPointCB;
    
    /**
     * Creates a replication group member.
     * 
     * @param aNode Node containing this instance.
     * @param anID Replication group identifier.
     * @param aTargetNodes Nodes hosting the other members of the
     * replication group containing this member.
     */
    public ReplicationMemberImpl(Node aNode, Object anID,
        NodeInfo[] aTargetNodes) {
        super(aNode, anID);
        if ( null == aTargetNodes ) {
            throw new IllegalArgumentException("Node names is required");
        }
        
        idToReplicant = new HashMap();
        
        endPointCB = new EndPointCallback(sender);
        endPointCB.setEndPointId(anID);
        endPointCB.setTargets(aTargetNodes);
        
        Class[] interfaces = new Class[] {ReplicationMember.class};
        Enhancer enhancer = new Enhancer();
        enhancer.setInterfaces(interfaces);
        enhancer.setCallbackTypes(
            new Class[] {MethodInterceptor.class, LazyLoader.class});
        enhancer.setUseFactory(false);
        enhancer.setCallbacks(new Callback[] {endPointCB,
            new LazyLoader() {
                public Object loadObject() throws Exception {
                    throw new UnsupportedOperationException("Empty local half.");
                }
            }});
        enhancer.setCallbackFilter(new HOPPFilter(interfaces));
        otherMembers = (ReplicationMember) enhancer.create();
    }
    
    public Object getReplicationGroupID() {
        return id;
    }
    
    public void setMsgProducerOut(MsgOutInterceptor aMsgOut) {
        super.setMsgProducerOut(aMsgOut);
        endPointCB.setOut(out);
    }
    
    public void fireUpdateEvent(UpdateEvent anEvent)
        throws ReplicationException {
        Object target = anEvent.getTarget();
        if ( target instanceof ReplicationCapable ) {
            // One does not send the actual ReplicantCapable in the case of an
            // update. Instead, one sends only its identifier.
            anEvent.setTarget(((ReplicationCapable)target).getID());
        } else if ( false == target instanceof ReplicantID ) {
            throw new ReplicationException("Target is unknown.");
        }
        otherMembers.mergeWithUpdate(anEvent);
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
        ReplicantID repID = (ReplicantID) anEvent.getTarget();
        ReplicationCapable replicationCapable;
        synchronized(idToReplicant) {
            replicationCapable = (ReplicationCapable) idToReplicant.get(repID);
        }
        if ( null == replicationCapable ) {
            throw new ReplicationException(
                "No ReplicantCapable with the id {" + repID + "}");
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
        ReplicantID repID = new ReplicantID();
        aReplicant.setID(repID);
        otherMembers.registerLocalReplicantCapable(aReplicant);
        synchronized(idToReplicant) {
            idToReplicant.put(repID, aReplicant);
        }
        aReplicant.addUpdateListener(this);
    }

    public void unregisterReplicantCapable(ReplicationCapable aReplicant) {
        Object repID = aReplicant.getID();
        synchronized(idToReplicant) {
            idToReplicant.remove(repID);
        }
        aReplicant.removeUpdateListener(this);
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
    
    /**
     * ReplicantCapable identifier. 
     */
    private static class ReplicantID implements Externalizable {
        private static volatile int seqId = 0;
        private int repID;
        public ReplicantID() {
            repID = seqId++;
        }
        public int hashCode() {
            return repID;
        }
        public boolean equals(Object obj) {
            if ( false == obj instanceof ReplicantID ) {
               return false;
            }
            ReplicantID replicantID = (ReplicantID) obj;
            return repID == replicantID.repID;
        }
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeInt(repID);
        }
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            repID = in.readInt();
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory("Replication Member", ReplicationMemberImpl.class.getName(), AbstractEndPoint.GBEAN_INFO);
        infoFactory.addAttribute("TargetNodes",  NodeInfo[].class, true);
        infoFactory.addOperation("registerReplicantCapable", new Class[] {ReplicationCapable.class});
        infoFactory.addOperation("retrieveReplicantCapable", new Class[] {Object.class});
        infoFactory.setConstructor(new String[]{"Node", "ID", "TargetNodes"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
    
}
