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

package org.apache.geronimo.messaging.jmx;

import javax.management.MBeanServer;

import org.apache.geronimo.messaging.EndPoint;

/**
 * EndPoint exposing a Referenceable MBeanServer to other EndPoints.
 *
 * @version $Revision: 1.1 $ $Date: 2004/05/24 12:12:47 $
 */
public interface MBeanServerEndPoint
    extends EndPoint
{
    
    /**
     * Gets a Referenceable MBeanServer.
     * <BR>
     * The returned MBeanServer is a Referenceable. This way it can be
     * passed around to EndPoints registered by remote Nodes.
     * 
     * @return A Referenceable MBeanServer.
     */
    public MBeanServer getMBeanServer();
    
}