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

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.LazyLoader;
import net.sf.cglib.proxy.MethodInterceptor;

import org.apache.geronimo.messaging.BaseEndPoint;
import org.apache.geronimo.messaging.Node;
import org.apache.geronimo.messaging.interceptors.MsgOutInterceptor;
import org.apache.geronimo.messaging.interceptors.MsgTransformer;

/**
 * EndPointProxyFactory implementation.
 *
 * @version $Revision: 1.3 $ $Date: 2004/07/20 00:06:13 $
 */
public class EndPointProxyFactoryImpl
    extends BaseEndPoint
    implements EndPointProxyFactory
{

    /**
     * Collection<SoftReference>.
     * <BR>
     * EndPoint call-backs of the proxies created by this factory. They are
     * tracked in order to update their Msg output when the factory is started
     * or stopped.
     */
    private final Collection callbacks;
    
    /**
     * Creates a factory mounted by the specified node and having the specified
     * identifier.
     * 
     * @param aNode Hosting Node.
     * @param anID EndPoint identifier.
     */
    public EndPointProxyFactoryImpl(Node aNode, Object anID) {
        super(aNode, anID);
        callbacks = new ArrayList();
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
        
        synchronized(callbacks) {
            callbacks.add(new SoftReference(endPointCB));
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
        synchronized(callbacks) {
            for (Iterator iter = callbacks.iterator(); iter.hasNext();) {
                EndPointCallback callback =
                    (EndPointCallback) ((SoftReference) iter.next()).get();
                if ( null == callback ) {
                    iter.remove();
                } else {
                    callback.setOut(out);
                }
            }
        }
    }
    
    public void start() {
    }

    public void stop() {
        synchronized(callbacks) {
            // Does not need to reset the Msg output of the call-backs as this
            // is already done via setMsgProducerOut.
            callbacks.clear();
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
            synchronized(callbacks) {
                for (Iterator iter = callbacks.iterator(); iter.hasNext();) {
                    EndPointCallback callback =
                        (EndPointCallback) ((SoftReference) iter.next()).get();
                    if ( null == callback || callback == endPointCallback ) {
                        iter.remove();
                    }
                }
                callbacks.remove(endPointCallback);
            }
        }
        public void setTransformer(MsgTransformer aTransformer) {
            endPointCallback.setTransformer(aTransformer);
        }
    }
    
}
