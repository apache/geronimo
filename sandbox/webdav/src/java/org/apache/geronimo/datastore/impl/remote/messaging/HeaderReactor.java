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

package org.apache.geronimo.datastore.impl.remote.messaging;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Reactor in charge of dispatching Msgs to Connectors based on their headers.
 *
 * @version $Revision: 1.3 $ $Date: 2004/03/18 12:14:05 $
 */
public class HeaderReactor
    implements Processor
{

    private static final Log log = LogFactory.getLog(HeaderReactor.class);
    
    /**
     * Header to Connector map.
     */
    private final Map connectors;
    
    /**
     * inbound interceptor from which Msgs are consumed.
     */
    private final HeaderInInterceptor actualIn;

    /**
     * Used to dispatch Threads to invoke the deliver method of Connectors.
     */
    private final Processors processors;
    
    /**
     * Dispatch the provided Header blocks.
     * 
     * @param anIn Header blocks to be dispatched.
     * @param aProcessors Processor pool used to invoke the deliver method
     * of Connectors in a separate Thread.
     */
    public HeaderReactor(HeaderInInterceptor anIn, Processors aProcessors) {
        if ( null == anIn ) {
            throw new IllegalArgumentException("Label input is required.");
        } else if ( null == aProcessors ) {
            throw new IllegalArgumentException("Processors is required.");
        }
        actualIn = anIn;
        connectors = new HashMap();
        processors = aProcessors;
    }

    /**
     * Registers a new header.
     * 
     * @param aHeader Header.
     * @param aConnector Connector to which Msgs having the specified header 
     * should be dispatched. 
     */
    public void register(Object aHeader, Connector aConnector) {
        synchronized(connectors) {
            connectors.put(aHeader, aConnector);
        }
    }

    /**
     * Unregisters a header.
     * 
     * @param aHeader Header to be deregistered.
     */
    public void unregister(Object aHeader) {
        synchronized(connectors) {
            connectors.remove(aHeader);
        }
    }
    
    public void run() {
        try {
            while ( true ) {
                dispatch();
            }
        } catch (MsgInterceptorStoppedException e) {
            log.info("Stopping HeaderReactor", e);
            return;
        }
    }

    /**
     * Dispatches Msgs to the relevant Connector.
     */
    private void dispatch() {
        final Msg msg;
        msg = actualIn.pop();
        Object opaque = actualIn.getHeader();
        final Connector connector;
        synchronized (connectors) {
            connector = (Connector) connectors.get(opaque);
        }
        if ( null == connector ) {
            log.error("No Connector for header {" + opaque + "}");
            return;
        }
        Processor processor = new Processor() {
            public void run() {
                connector.deliver(msg);
            }
            public void release() {}
        };
        processors.execute(processor);
    }

}
