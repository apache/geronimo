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

import org.apache.geronimo.messaging.EndPoint;

/**
 * Factory of EndPoint proxies.
 *
 * @version $Revision: 1.3 $ $Date: 2004/06/10 23:12:24 $
 */
public interface EndPointProxyFactory extends EndPoint
{
    
    /**
     * Starts.
     */
    public void start();

    /**
     * Stops.
     */
    public void stop();
    
    /**
     * Creates a proxy for the EndPoint defined by anInfo.
     * 
     * @param anInfo EndPoint meta-data.
     * @return A proxy for the EndPoint defined by anInfo. This proxy implements
     * all the EndPoint interfaces plus the EndPointProxy interface.
     */
    public Object factory(EndPointProxyInfo anInfo);
    
    /**
     * Releases the resources of the specified EndPoint proxy.
     * <BR>
     * From this point, the proxy can no more be used.
     * <BR>An IllegalStateException should be thrown when a method is invoked
     * on a EndPoint proxy.
     * 
     * @param aProxy EndPoint proxy.
     * @exception IllegalArgumentException Indicates that the provided instance
     * is not a proxy.
     */
    public void releaseProxy(Object aProxy);
    
}