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

package org.apache.geronimo.messaging;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.messaging.interceptors.MsgOutInterceptor;

/**
 * Based implementation for EndPoints to be exposed as GBeans.
 *
 * @version $Revision: 1.1 $ $Date: 2004/06/10 23:12:24 $
 */
public abstract class GBeanBaseEndPoint
    extends BaseEndPoint
    implements EndPoint, GBeanLifecycle
{

    /**
     * Creates an EndPoint, which is mounted by the specified Node and having
     * the specified identifier.
     * 
     * @param aNode Node owning this connector.
     * @param anID EndPoint identifier.
     */
    public GBeanBaseEndPoint(Node aNode, Object anID) {
        super(aNode, anID);
    }
    
    public void doStart() throws WaitingException, Exception {
        node.addEndPoint(this);
    }

    public void doStop() throws WaitingException, Exception {
        node.removeEndPoint(this);
    }

    public void doFail() {
        node.removeEndPoint(this);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory("Abstract EndPoint", GBeanBaseEndPoint.class.getName());
        infoFactory.addReference("Node", Node.class);
        infoFactory.addAttribute("ID", Object.class, true);
        infoFactory.addAttribute("MsgConsumerOut", MsgOutInterceptor.class, false);
        infoFactory.addAttribute("MsgProducerOut", MsgOutInterceptor.class, false);
        infoFactory.setConstructor(new String[]{"Node", "ID"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
    
}
