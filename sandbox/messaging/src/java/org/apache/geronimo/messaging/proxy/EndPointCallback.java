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

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.apache.geronimo.messaging.NodeInfo;
import org.apache.geronimo.messaging.Request;
import org.apache.geronimo.messaging.RequestSender;
import org.apache.geronimo.messaging.interceptors.MsgOutInterceptor;

/**
 * This Callback sends Request to an EndPoint hosted by a set of Nodes. 
 *
 * @version $Revision: 1.2 $ $Date: 2004/06/01 13:37:14 $
 */
public class EndPointCallback
    implements MethodInterceptor
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

    /**
     * @param aSender RequestSender to be used to send Request to the
     * associated EndPoint.
     */
    public EndPointCallback(RequestSender aSender) {
        if ( null == aSender ) {
            throw new IllegalArgumentException("Sender is required.");
        }
        sender = aSender;
    }

    /**
     * Gets the target EndPoint identifier.
     * 
     * @return Returns the id.
     */
    public Object getEndPointId() {
        return id;
    }

    /**
     * Sets the identifier of the target EndPoint.
     * 
     * @param anID The id to set.
     */
    public void setEndPointId(Object anID) {
        id = anID;
    }

    /**
     * Gets the Msg transport used to sent Requests.
     * 
     * @return Returns the out.
     */
    public MsgOutInterceptor getOut() {
        return out;
    }

    /**
     * Sets the Msg output to be used to sent Requests. 
     * 
     * @param anOut The out to set.
     */
    public void setOut(MsgOutInterceptor anOut) {
        out = anOut;
    }

    /**
     * Gets the Nodes hosting the target EndPoint.
     * 
     * @return Returns the targets.
     */
    public NodeInfo[] getTargets() {
        return targets;
    }

    /**
     * Sets the Nodes hosting the target EndPoints.
     * 
     * @param aTargets The targets to set.
     */
    public void setTargets(NodeInfo[] aTargets) {
        targets = aTargets;
    }
    
    public Object intercept(Object arg0, Method arg1,
                            Object[] arg2, MethodProxy arg3) throws Throwable {
        if ( null == out ) {
            throw new IllegalStateException("No Msg out is defined");
        } else if ( null == id ) {
            throw new IllegalStateException("No EndPoint id is defined");
        } else if ( null == targets ) {
            throw new IllegalStateException("No target nodes is defined");
        }
        try {
            Object opaque = sender.sendSyncRequest(
                new Request(arg1.getName(), arg1.getParameterTypes(), arg2),
                    out, id, targets);
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
