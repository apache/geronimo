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
import org.apache.geronimo.datastore.impl.remote.messaging.ServantNode;
import org.apache.geronimo.datastore.impl.remote.messaging.ServerNode;

/**
 * This is a remote use-case.
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/29 13:14:11 $
 */
public class RemoteUseCaseTest extends AbstractUseCaseTest {

    protected void setUp() throws Exception {
        LockManager lockManager = new LockManager();
        File root = new File(System.getProperty("java.io.tmpdir"),
                "GFileManager");
        Util.recursiveDelete(root);
        root.mkdir();
        
        GFileManager delegate;
        delegate = new LocalGFileManager("test", root, lockManager);
        InetAddress address = InetAddress.getLocalHost();
        int port = 8080;
        GFileManagerProxy proxy = new GFileManagerProxy(delegate);
        ServerNode server = new ServerNode("MasterNode",
                Collections.singleton(proxy), address, port, 2);
        server.doStart();
        proxy.doStart();

        fileManager = new GFileManagerClient("test");
        ServantNode servant = new ServantNode(
                "ChildNode", Collections.singleton(fileManager), address, port, 10);
        servant.doStart();
        ((GFileManagerClient) fileManager).doStart();
    }
    
}
