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

package org.apache.geronimo.datastore.impl.remote.datastore;

import java.io.File;
import java.net.InetAddress;
import java.util.Collections;

import org.apache.geronimo.datastore.GFileManager;
import org.apache.geronimo.datastore.Util;
import org.apache.geronimo.datastore.impl.LockManager;
import org.apache.geronimo.datastore.impl.local.AbstractUseCaseTest;
import org.apache.geronimo.datastore.impl.local.LocalGFileManager;
import org.apache.geronimo.datastore.impl.remote.datastore.GFileManagerClient;
import org.apache.geronimo.datastore.impl.remote.datastore.GFileManagerProxy;
import org.apache.geronimo.datastore.impl.remote.messaging.NodeInfo;
import org.apache.geronimo.datastore.impl.remote.messaging.ServerNode;
import org.apache.geronimo.kernel.Kernel;

/**
 * This is a remote use-case.
 *
 * @version $Revision: 1.1 $ $Date: 2004/03/03 15:19:01 $
 */
public class RemoteUseCaseTest extends AbstractUseCaseTest {

    private Kernel kernel;
    
    /**
     * In this set-up one initializes two nodes, namely Node1 and Node2. A
     * local GFileManager is mounted by Node1. A client GFileManager is mounted
     * by Node2. Node2 joins Node1.
     */
    protected void setUp() throws Exception {
        File root = new File(System.getProperty("java.io.tmpdir"),
                "GFileManager");
        Util.recursiveDelete(root);
        root.mkdir();
        
        LockManager lockManager = new LockManager();

        GFileManager delegate;
        delegate = new LocalGFileManager("test", root, lockManager);
        InetAddress address = InetAddress.getLocalHost();
        GFileManagerProxy proxy = new GFileManagerProxy(delegate);
        NodeInfo nodeInfo1 = new NodeInfo("Node1", address, 8080);
        ServerNode server1 = new ServerNode(nodeInfo1,
            Collections.singleton(proxy));
        server1.doStart();
        proxy.doStart();

        fileManager = new GFileManagerClient("test", "Node1");
        NodeInfo nodeInfo2 = new NodeInfo("Node2", address, 8082);
        ServerNode server2 = new ServerNode(nodeInfo2,
            Collections.singleton(fileManager));
        server2.doStart();
        ((GFileManagerClient) fileManager).doStart();
        
        server2.join(nodeInfo1);
    }
    
}
