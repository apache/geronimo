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

import org.apache.geronimo.messaging.interceptors.MsgTransformer;


/**
 * EndPointProxyFactory creates EndPoint proxies, which automatically implement
 * this interface.
 *
 * @version $Rev$ $Date$
 */
public interface EndPointProxy
{
    
    /**
     * Releases the EndPoint proxy resources.
     */
    public void release();
    
    /**
     * Allows to transform Msgs being sent by this proxy.
     * 
     * @param aTransformer Msg transformer.
     */
    public void setTransformer(MsgTransformer aTransformer);
    
}