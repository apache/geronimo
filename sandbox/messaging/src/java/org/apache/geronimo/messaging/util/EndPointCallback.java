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

package org.apache.geronimo.messaging.util;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.apache.geronimo.messaging.NodeInfo;
import org.apache.geronimo.messaging.Request;
import org.apache.geronimo.messaging.RequestSender;
import org.apache.geronimo.messaging.interceptors.MsgOutInterceptor;

/**
 *
 * @version $Revision: 1.1 $ $Date: 2004/05/11 12:06:43 $
 */
public class EndPointCallback  implements MethodInterceptor
{

    /**
     * To send request Msgs.
     */
    private final RequestSender sender;

    /**
     * Transport bus. 
     */
    private MsgOutInterceptor out;
    
    /**
     * Nodes to which the Msgs are to be sent.
     */
    private NodeInfo[] targets;

    /**
     * EndPoint identifier.
     */
    private Object id;

    public EndPointCallback(RequestSender aSender) {
        if ( null == aSender ) {
            throw new IllegalArgumentException("Sender is required.");
        }
        sender = aSender;
    }

    /**
     * @return Returns the id.
     */
    public Object getEndPointId() {
        return id;
    }

    /**
     * @param anID The id to set.
     */
    public void setEndPointId(Object anID) {
        id = anID;
    }

    /**
     * @return Returns the out.
     */
    public MsgOutInterceptor getOut() {
        return out;
    }

    /**
     * @param anOut The out to set.
     */
    public void setOut(MsgOutInterceptor anOut) {
        out = anOut;
    }

    /**
     * @return Returns the targets.
     */
    public NodeInfo[] getTargets() {
        return targets;
    }

    /**
     * @param aTargets The targets to set.
     */
    public void setTargets(NodeInfo[] aTargets) {
        targets = aTargets;
    }
    
    public Object intercept(Object arg0, Method arg1,
                            Object[] arg2, MethodProxy arg3) throws Throwable {
        try {
            Object opaque = sender.sendSyncRequest(
                    new Request(arg1.getName(), arg2), out, id, targets);
            return opaque;
        } catch (RuntimeException e) {
            Throwable nested = e.getCause();
            if ( null == nested ) {
                throw e;
            }
            // unwrap the exceptions raised by the actual method.
            Class[] exceptions = arg1.getExceptionTypes();
            for (int i = 0; i < exceptions.length; i++) {
                if ( exceptions[i].isInstance(nested) ) {
                    throw nested;
                }
            }
            throw e;
        }
    }

}
