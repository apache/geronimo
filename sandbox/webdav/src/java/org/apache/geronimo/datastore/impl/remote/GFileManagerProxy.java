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

import org.apache.geronimo.datastore.GFile;
import org.apache.geronimo.datastore.GFileManager;
import org.apache.geronimo.datastore.GFileManagerException;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.messaging.AbstractEndPoint;
import org.apache.geronimo.messaging.Node;

/**
 * It is a wrapper/proxy for a GFileManager, whose services need to be exposed
 * via a Node.
 *
 * @version $Revision: 1.2 $ $Date: 2004/06/02 11:29:24 $
 */
public class GFileManagerProxy
    extends AbstractEndPoint
    implements GFileManager
{

    /**
     * Proxied GFileManager.
     */
    private final GFileManager fileManager;

    /**
     * Builds a proxy for the provided GFileManager.
     * 
     * @param aNode Node containing this instance.
     * @param aFileManager GFileManager to be proxied by this instance.
     */
    public GFileManagerProxy(Node aNode,
        GFileManager aFileManager) {
        super(aNode, aFileManager.getName());
        if ( null == aFileManager ) {
            throw new IllegalArgumentException("GFileManager is required.");
        }
        fileManager = aFileManager;
    }
    
    public String getName() {
        return fileManager.getName();
    }

    public GFile factoryGFile(Object anOpaque, String aPath)
        throws GFileManagerException {
        return fileManager.factoryGFile(anOpaque, aPath);
    }
    
    public void persistNew(Object anOpaque, GFile aFile) {
        fileManager.persistNew(anOpaque, aFile);
    }

    public void persistUpdate(Object anOpaque, GFile aFile) {
        fileManager.persistUpdate(anOpaque, aFile);
    }

    public void persistDelete(Object anOpaque, GFile aFile) {
        fileManager.persistDelete(anOpaque, aFile);
    }

    public Object startInteraction() {
        return fileManager.startInteraction();
    }

    public void endInteraction(Object anOpaque) throws GFileManagerException {
        fileManager.endInteraction(anOpaque);
    }
    
    public static final GBeanInfo GBEAN_INFO;
    
    static {
        GBeanInfoFactory factory = new GBeanInfoFactory(GFileManagerProxy.class, AbstractEndPoint.GBEAN_INFO);
        factory.setConstructor(new String[] {"Node", "Delegate"});
        factory.addReference("Delegate", GFileManager.class);
        GBEAN_INFO = factory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
    
}
