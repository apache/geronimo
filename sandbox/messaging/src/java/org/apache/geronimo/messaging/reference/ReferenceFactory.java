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

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.LazyLoader;
import net.sf.cglib.proxy.MethodInterceptor;

import org.apache.geronimo.messaging.NodeInfo;
import org.apache.geronimo.messaging.RequestSender;
import org.apache.geronimo.messaging.interceptors.MsgOutInterceptor;
import org.apache.geronimo.messaging.proxy.EndPointCallback;
import org.apache.geronimo.messaging.proxy.HOPPFilter;

/**
 * Factory of Referenceable proxies.
 *
 * @version $Revision: 1.2 $ $Date: 2004/05/20 13:37:11 $
 */
public class ReferenceFactory
{

    /**
     * Request service.
     */
    private final RequestSender sender;
    
    /**
     * Transport bus.
     */
    private final MsgOutInterceptor out;
    
    /**
     * @param aSender Request sender service.
     * @param anOut Transport bus.
     */
    public ReferenceFactory(RequestSender aSender, MsgOutInterceptor anOut) {
        if ( null == aSender ) {
            throw new IllegalArgumentException("Sender is required.");
        } else if ( null == anOut ) {
            throw new IllegalArgumentException("MsgOut is required.");
        }
        sender = aSender;
        out = anOut;
    }
    
    /**
     * Builds a proxy for the Referenceable aReferenceInfo.
     * <BR> 
     * This proxy implements all the interfaces of the Referenceable
     * and uses under the cover the request sender and transport bus provided
     * to the constructor.
     * <BR>
     * It also implements the Reference interface, though the contracts of this
     * interface are not intended to be invoked by clients.
     * 
     * @param aReferenceInfo Referenceable meta-data.
     * @return A Referenceable proxy.
     */
    public Object factory(final ReferenceableInfo aReferenceInfo) {
        EndPointCallback endPointCB = new EndPointCallback(sender);
        endPointCB.setEndPointId(aReferenceInfo.getID());
        endPointCB.setOut(out);
        endPointCB.setTargets(new NodeInfo[] {aReferenceInfo.getHostingNode()});

        Class[] refIntfs = aReferenceInfo.getRefClass();
        // Automatically adds the Reference interface to this array of
        // interfaces to be implemented by the proxy.
        Class[] interfaces = new Class[refIntfs.length + 1];
        for (int i = 0; i < refIntfs.length; i++) {
            interfaces[i] = refIntfs[i];
        }
        interfaces[interfaces.length - 1] = Reference.class;
        
        Enhancer enhancer = new Enhancer();
        enhancer.setInterfaces(interfaces);
        enhancer.setCallbackTypes(
            new Class[] {MethodInterceptor.class, LazyLoader.class});
        enhancer.setUseFactory(false);
        enhancer.setCallbacks(new Callback[] {endPointCB,
            new LazyLoader() {
                public Object loadObject() throws Exception {
                    return new HalfObjectLocal(aReferenceInfo);
                }
            }});
        enhancer.setCallbackFilter(new HOPPFilter(refIntfs));
        return enhancer.create();
    }
    
    /**
     * Implements the local half of a Referenceable proxy. 
     */
    private static class HalfObjectLocal implements Reference {
        private ReferenceableInfo referenceInfo;
        private HalfObjectLocal(ReferenceableInfo aReferenceInfo) {
            referenceInfo = aReferenceInfo;
        }
        public boolean equals(Object obj) {
            if ( false == obj instanceof Reference ) {
                return false;
            }
            Reference reference = (Reference) obj;
            return referenceInfo.equals(reference.getReferenceableInfo());
        }
        public ReferenceableInfo getReferenceableInfo() {
            return referenceInfo;
        }
    }
    
}