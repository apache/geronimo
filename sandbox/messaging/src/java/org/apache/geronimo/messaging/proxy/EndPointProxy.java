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


/**
 * EndPointProxyFactory creates EndPoint proxies, which automatically implement
 * this interface.
 * <BR>
 * Clients should not use the contracts of this interface.
 *
 * @version $Revision: 1.1 $ $Date: 2004/05/20 13:37:11 $
 */
interface EndPointProxy
{
    
    /**
     * Releases the EndPoint proxy resources.
     */
    public void release();
    
}