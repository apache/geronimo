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

package org.apache.geronimo.datastore.impl.remote;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.MethodInterceptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.datastore.GFile;
import org.apache.geronimo.datastore.GFileManager;
import org.apache.geronimo.datastore.GFileManagerException;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.messaging.AbstractEndPoint;
import org.apache.geronimo.messaging.Node;
import org.apache.geronimo.messaging.NodeInfo;
import org.apache.geronimo.messaging.interceptors.MsgOutInterceptor;
import org.apache.geronimo.messaging.util.EndPointCallback;
import org.apache.geronimo.messaging.util.ProxyFactory;

/**
 * GFileManager mirroring a GFileManagerProxy mounted by a remote node.
 * <BR>
 * Operations peformed against this instance are actually executed against the
 * remote GFileManagerProxy having the same name than this instance. 
 *
 * @version $Revision: 1.1 $ $Date: 2004/05/11 12:24:59 $
 */
public class GFileManagerClient
    extends AbstractEndPoint
    implements GFileManager
{

    private static final Log log = LogFactory.getLog(GFileManagerClient.class);

    /**
     * Name of the proxy to be mirrored. 
     */
    private final String name;
    
    /**
     * Node hosting the proxy to be mirrored.
     */
    private final NodeInfo node; 

    private GFileManager remoteThis;
    
    /**
     * EndPointCallback used under the cover by otherMembers.
     */
    private final EndPointCallback endPointCallback;
    
    /**
     * Creates a client having the specified name. The name MUST be the name
     * of the proxy to be mirrored.
     * 
     * @param aNode Node hosting the proxy.
     * @param aName Name of the proxy to be mirrored.
     * @param aNodeInfo Node hosting the proxy to be mirrored. 
     */
    public GFileManagerClient(Node aNode,
        String aName, NodeInfo aNodeInfo) {
        super(aNode, aName);
        if ( null == aName ) {
            throw new IllegalArgumentException("Name is required.");
        } else if ( null == aNode ) {
            throw new IllegalArgumentException("Node is required.");
        }
        name = aName;
        node = aNodeInfo;
        
        endPointCallback = new EndPointCallback(sender);
        endPointCallback.setEndPointId(aName);
        endPointCallback.setTargets(new NodeInfo[] {aNodeInfo});
        ProxyFactory factory =
            new ProxyFactory(new Class[] {GFileManager.class},
            new Callback[] {endPointCallback},
            new Class[] {MethodInterceptor.class}, null);
        remoteThis = (GFileManager) factory.getProxy();
    }
    
    public String getName() {
        return name;
    }
    
    /**
     * Gets the NodeInfo of the node hosting the GFileManagerProxy mirrored by
     * this instance.
     * 
     * @return Hosting node info.
     */
    public NodeInfo getHostingNode() {
        return node;
    }
    
    public void setMsgProducerOut(MsgOutInterceptor aMsgOut) {
        super.setMsgProducerOut(aMsgOut);
        endPointCallback.setOut(out);
    }
    
    public Object startInteraction() {
        return remoteThis.startInteraction();
    }

    public GFile factoryGFile(Object anOpaque, String aPath)
        throws GFileManagerException {
        return remoteThis.factoryGFile(anOpaque, aPath);
    }
    
    public void persistNew(Object anOpaque, GFile aFile) {
        remoteThis.persistNew(anOpaque, aFile);
    }

    public void persistUpdate(Object anOpaque, GFile aFile) {
        remoteThis.persistUpdate(anOpaque, aFile);
    }

    public void persistDelete(Object anOpaque, GFile aFile) {
        remoteThis.persistDelete(anOpaque, aFile);
    }

    public void endInteraction(Object anOpaque) throws GFileManagerException {
        remoteThis.endInteraction(anOpaque);
    }
    
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory factory = new GBeanInfoFactory(GFileManagerClient.class, AbstractEndPoint.GBEAN_INFO);
        factory.setConstructor(
            new String[] {"Node", "ID", "HostingNode"},
            new Class[] {Node.class, String.class, NodeInfo.class});
        factory.addAttribute("HostingNode", true);
        GBEAN_INFO = factory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
    
}
