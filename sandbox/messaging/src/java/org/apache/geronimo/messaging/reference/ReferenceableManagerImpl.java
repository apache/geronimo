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

package org.apache.geronimo.messaging.reference;

import java.util.HashMap;
import java.util.Map;

import org.apache.geronimo.messaging.AbstractEndPoint;
import org.apache.geronimo.messaging.Msg;
import org.apache.geronimo.messaging.Node;
import org.apache.geronimo.messaging.NodeInfo;
import org.apache.geronimo.messaging.Request;
import org.apache.geronimo.messaging.Result;
import org.apache.geronimo.messaging.interceptors.MsgOutInterceptor;

/**
 * ReferenceableManager implementation.
 *
 * @version $Revision: 1.1 $ $Date: 2004/05/11 12:06:42 $
 */
public class ReferenceableManagerImpl
    extends AbstractEndPoint
    implements ReferenceableManager
{

    /**
     * Used to generate reference identifiers.
     */
    private static int seqID = 0;

    /**
     * identifier to Referenceable map.
     */
    private final Map registered;

    /**
     * Creates a manager mounted by the specified node and having the specified
     * identifier.
     * 
     * @param aNode Hosting Node.
     * @param anID EndPoint identifier.
     */
    public ReferenceableManagerImpl(Node aNode, Object anID) {
        super(aNode, anID);
        
        registered = new HashMap();
        // Adds the ReferenceReplacerResolver to the current chain.
        node.getReplacerResolver().append(new ReferenceReplacerResolver(this));
    }

    public Object factoryProxy(ReferenceableInfo aReferenceInfo) {
        NodeInfo hostingNode = aReferenceInfo.getHostingNode();
        Object endPointID = aReferenceInfo.getID();
        if ( hostingNode.equals(node.getNodeInfo()) && endPointID.equals(id) ) {
            // The referenceble is contained by this EndPoint. Returns it.
            Integer refID = new Integer(aReferenceInfo.getRefID());
            Object opaque;
            synchronized(registered) {
                opaque = registered.get(refID);
            }
            if ( null == opaque ) {
                throw new IllegalArgumentException("Referenceable {" +
                    aReferenceInfo + "} does not exist.");
            }
            return opaque;
        }
        ReferenceFactory factory =
            new ReferenceFactory(sender,
                new ProxyMsgProvider(aReferenceInfo.getRefID()));
        return factory.factory(aReferenceInfo);
    }

    public ReferenceableInfo register(Referenceable aReference) {
        Integer refID;
        synchronized(registered) {
            refID = new Integer(++seqID);
            registered.put(refID, aReference);
        }
        return new ReferenceableInfo(node.getNodeInfo(), id,
            aReference.getClass().getInterfaces(),
            refID.intValue());
    }

    public void unregister(ReferenceableInfo aReferenceInfo) {
        synchronized(registered) {
            registered.remove(new Integer(aReferenceInfo.getRefID()));
        }
    }
    
    public Object invoke(int anId, Request aRequest) throws Exception {
        Referenceable reference;
        synchronized(registered) {
            reference = (Referenceable) registered.get(new Integer(anId));
        }
        if ( null == reference ) {
            throw new IllegalArgumentException("Unknown Reference.");
        }
        aRequest.setTarget(reference);
        Result result = aRequest.execute();
        if ( result.isSuccess() ) {
            return result.getResult();
        } else {
            throw result.getException();
        }
    }
    
    /**
     * A Msg output wrapping an invokation on a Reference into an
     * invokation on the ReferenceManager. This latter invokes then the wrapped
     * invokation on the Reference.
     */
    private class ProxyMsgProvider implements MsgOutInterceptor {
        private final Integer refID;
        private ProxyMsgProvider(int aRefID) {
            refID = new Integer(aRefID);
        }
        public void push(Msg aMsg) {
            Request request = 
                new Request("invoke",
                    new Object[] {refID, aMsg.getBody().getContent()});
            aMsg.getBody().setContent(request);
            out.push(aMsg);
        }
    }

}
