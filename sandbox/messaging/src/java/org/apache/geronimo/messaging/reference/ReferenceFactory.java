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

import java.lang.reflect.Method;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.apache.geronimo.messaging.NodeInfo;
import org.apache.geronimo.messaging.RequestSender;
import org.apache.geronimo.messaging.interceptors.MsgOutInterceptor;
import org.apache.geronimo.messaging.util.EndPointCallback;
import org.apache.geronimo.messaging.util.ProxyFactory;

/**
 * Factory of Referenceable proxies.
 *
 * @version $Revision: 1.1 $ $Date: 2004/05/11 12:06:42 $
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
    public Object factory(ReferenceableInfo aReferenceInfo) {
        EndPointCallback endPointCallback = new EndPointCallback(sender);
        endPointCallback.setEndPointId(aReferenceInfo.getID());
        endPointCallback.setOut(out);
        endPointCallback.setTargets(
            new NodeInfo[] {aReferenceInfo.getHostingNode()});

        Class[] refIntfs = aReferenceInfo.getRefClass();
        // Automatically adds the Reference interface to this array of
        // interfaces to be implemented by the proxy.
        Class[] interfaces = new Class[refIntfs.length + 1];
        for (int i = 0; i < refIntfs.length; i++) {
            interfaces[i] = refIntfs[i];
        }
        interfaces[interfaces.length - 1] = Reference.class;
        
        HalfObjectLocal halfObjLocal = new HalfObjectLocal(aReferenceInfo);
        ProxyFactory factory =
            new ProxyFactory(interfaces,
                new Callback[] {endPointCallback, halfObjLocal},
                new Class[] {MethodInterceptor.class, MethodInterceptor.class},
                new HOPPFilter(refIntfs));
        return factory.getProxy();
    }
    
    /**
     * Implements the local half of a Referenceable proxy. 
     */
    private static class HalfObjectLocal
        implements Reference, MethodInterceptor {
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
        public Object intercept(Object arg0, Method arg1, Object[] arg2,
            MethodProxy arg3) throws Throwable {
            return arg3.invoke(this, arg2);
        }
    }
    
    /**
     * Maps the methods defined by the Referenceable interfaces to the first
     * Callback of a ProxyFactory.
     * <BR>
     * The other methods are mapped to the second one.
     */
    private class HOPPFilter implements CallbackFilter {
        private final Class[] interfaces;
        private HOPPFilter(Class[] anInterfaces) {
            interfaces = anInterfaces;
        }
        public int accept(Method arg0) {
            Class declaringClass = arg0.getDeclaringClass(); 
            for (int i = 0; i < interfaces.length; i++) {
                if ( interfaces[i].equals(declaringClass) ) {
                    return 0;
                }
            }
            return 1;  
        }
    }
    
}