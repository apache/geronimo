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
import java.util.IdentityHashMap;
import java.util.Map;

import org.apache.geronimo.messaging.BaseEndPoint;
import org.apache.geronimo.messaging.Msg;
import org.apache.geronimo.messaging.Node;
import org.apache.geronimo.messaging.NodeInfo;
import org.apache.geronimo.messaging.Request;
import org.apache.geronimo.messaging.Result;
import org.apache.geronimo.messaging.interceptors.MsgOutInterceptor;
import org.apache.geronimo.messaging.io.ReplacerResolver;

/**
 * ReferenceableManager implementation.
 *
 * @version $Rev$ $Date$
 */
public class ReferenceableManagerImpl
    extends BaseEndPoint
    implements ReferenceableManager
{

    /**
     * Used to generate reference identifiers.
     */
    private int seqID = 0;

    private final Object mapsLock = new Object();
    
    /**
     * identifier to Referenceable map.
     */
    private final Map idToReferenceable;
    
    /**
     * Referenceable to ReferenceableInfo map.
     */
    private final IdentityHashMap referenceableToID;
    
    private final ReplacerResolver replacerResolver;
    
    /**
     * Creates a manager mounted by the specified node and having the specified
     * identifier.
     * 
     * @param aNode Hosting Node.
     * @param anID EndPoint identifier.
     */
    public ReferenceableManagerImpl(Node aNode, Object anID) {
        super(aNode, anID);
        
        idToReferenceable = new HashMap();
        referenceableToID = new IdentityHashMap();
        
        replacerResolver = new ReferenceReplacerResolver(this);
    }

    public void start() {
        replacerResolver.online();
        node.getReplacerResolver().append(replacerResolver);
    }
    
    public void stop() {
        replacerResolver.offline();
    }
    
    public Object factoryProxy(ReferenceableInfo aReferenceInfo) {
        NodeInfo hostingNode = aReferenceInfo.getHostingNode();
        Object endPointID = aReferenceInfo.getID();
        if ( hostingNode.equals(node.getNodeInfo()) && endPointID.equals(id) ) {
            // The referenceble is contained by this EndPoint. Returns it.
            Integer refID = new Integer(aReferenceInfo.getRefID());
            Object opaque;
            synchronized(mapsLock) {
                opaque = idToReferenceable.get(refID);
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
        ReferenceableInfo info;
        synchronized(mapsLock) {
            // Checks if the Referenceable has already been registered.
            info = (ReferenceableInfo) referenceableToID.get(aReference);
            if ( null == info ) {
                Integer refID = new Integer(++seqID);
                info = new ReferenceableInfo(node.getNodeInfo(), id,
                    aReference.getClass().getInterfaces(), refID.intValue());
                idToReferenceable.put(refID, aReference);
                referenceableToID.put(aReference, info);
            }
        }
        return info;
    }

    public void unregister(ReferenceableInfo aReferenceInfo) {
        synchronized(mapsLock) {
            idToReferenceable.remove(new Integer(aReferenceInfo.getRefID()));
            referenceableToID.remove(aReferenceInfo);
        }
    }
    
    public Object invoke(int anId, Request aRequest) throws Throwable {
        Referenceable reference;
        synchronized(mapsLock) {
            reference = (Referenceable) idToReferenceable.get(new Integer(anId));
        }
        if ( null == reference ) {
            throw new IllegalArgumentException("Unknown Reference.");
        }
        aRequest.setTarget(reference);
        Result result = aRequest.execute();
        if ( result.isSuccess() ) {
            return result.getResult();
        } else {
            throw result.getThrowable();
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
            if ( null == out ) {
                throw new IllegalStateException("No Msg out is defined");
            }
            Request request = 
                new Request("invoke", new Class[] {int.class, Request.class},
                    new Object[] {refID, aMsg.getBody().getContent()});
            aMsg.getBody().setContent(request);
            out.push(aMsg);
        }
    }

}
