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

package org.apache.geronimo.messaging.proxy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.LazyLoader;
import net.sf.cglib.proxy.MethodInterceptor;

import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.messaging.AbstractEndPoint;
import org.apache.geronimo.messaging.Node;
import org.apache.geronimo.messaging.interceptors.MsgOutInterceptor;

/**
 * EndPointProxyFactory implementation.
 *
 * @version $Revision: 1.1 $ $Date: 2004/05/20 13:37:11 $
 */
public class EndPointProxyFactoryImpl
    extends AbstractEndPoint
    implements EndPointProxyFactory
{

    /**
     * EndPoint call-backs of the proxies created by this factory. They are
     * tracked in order to update their Msg output when the factory is started
     * or stopped.
     */
    private final Collection endPointCallbacks;
    
    /**
     * Creates a factory mounted by the specified node and having the specified
     * identifier.
     * 
     * @param aNode Hosting Node.
     * @param anID EndPoint identifier.
     */
    public EndPointProxyFactoryImpl(Node aNode, Object anID) {
        super(aNode, anID);
        endPointCallbacks = new ArrayList();
    }

    /**
     * Creates a proxy for the EndPoint defined by anInfo.
     * 
     * @param anInfo EndPoint meta-data.
     * @return A proxy for the EndPoint defined by anInfo. This proxy implements
     * all the EndPoint interfaces plus the EndPointProxy interface.
     */
    public Object factory(EndPointProxyInfo anInfo) {
        if ( null == anInfo ) {
            throw new IllegalArgumentException("Info is required");
        }
        
        final EndPointCallback endPointCB = new EndPointCallback(sender);
        endPointCB.setEndPointId(anInfo.getEndPointID());
        endPointCB.setTargets(anInfo.getTargets());
        endPointCB.setOut(out);
        
        // Injects the EndPointProxy interface.
        Class[] endPointItf = anInfo.getInterfaces();
        Class[] interfaces = new Class[endPointItf.length + 1];
        for (int i = 0; i < endPointItf.length; i++) {
            interfaces[i] = endPointItf[i];
        }
        interfaces[interfaces.length - 1] = EndPointProxy.class;

        Enhancer enhancer = new Enhancer();
        enhancer.setInterfaces(interfaces);
        enhancer.setCallbackTypes(
            new Class[] {MethodInterceptor.class, LazyLoader.class});
        enhancer.setUseFactory(false);
        enhancer.setCallbacks(new Callback[] {endPointCB,
            new LazyLoader() {
                public Object loadObject() throws Exception {
                    return new HalfObjectLocal(endPointCB);
                }
            }});
        enhancer.setCallbackFilter(new HOPPFilter(anInfo.getInterfaces()));
        Object opaque = enhancer.create();
        
        synchronized(endPointCallbacks) {
            endPointCallbacks.add(endPointCB);
        }
        
        return opaque;
    }

    /**
     * Releases the resources of the specified EndPoint proxy.
     * <BR>
     * From this point, the proxy can no more be used.
     * 
     * @param aProxy EndPoint proxy.
     * @exception IllegalArgumentException Indicates that the provided instance
     * is not a proxy.
     */
    public void releaseProxy(Object aProxy) {
        if ( false == aProxy instanceof EndPointProxy ) {
            throw new IllegalArgumentException("Not an EndPointProxy");
        }
        ((EndPointProxy) aProxy).release();
    }
    
    public void setMsgProducerOut(MsgOutInterceptor aMsgOut) {
        super.setMsgProducerOut(aMsgOut);
        // When the factory is started or stopped, one also updates the
        // Msg output of the endpoint call-backs.
        synchronized(endPointCallbacks) {
            for (Iterator iter = endPointCallbacks.iterator(); iter.hasNext();) {
                EndPointCallback callback = (EndPointCallback) iter.next();
                callback.setOut(out);
            }
        }
    }
    
    public void doStop() throws WaitingException, Exception {
        super.doStop();
        synchronized(endPointCallbacks) {
            // Does not need to reset the Msg output of the call-backs as this
            // is already done via setMsgProducerOut.
            endPointCallbacks.clear();
        }
    }

    public void doFail() {
        super.doFail();
        synchronized(endPointCallbacks) {
            endPointCallbacks.clear();
        }
    }
    
    /**
     * Implements the local half of an EndPoint proxy. 
     */
    private class HalfObjectLocal implements EndPointProxy {
        private final EndPointCallback endPointCallback;
        private HalfObjectLocal(EndPointCallback anEndPointCallback) {
            endPointCallback = anEndPointCallback;
        }
        public void release() {
            endPointCallback.setOut(null);
            synchronized(endPointCallbacks) {
                endPointCallbacks.remove(endPointCallback);
            }
        }
    }
    
}
