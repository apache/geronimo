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

package org.apache.geronimo.datastore.impl.local;

import java.io.File;

import org.apache.geronimo.datastore.Util;
import org.apache.geronimo.datastore.impl.LockManager;
import org.apache.geronimo.datastore.impl.local.LocalGFileManager;

/**
 * This is a simple use-case.
 *
 * @version $Rev$ $Date$
 */
public class LocalUseCaseTest extends AbstractUseCaseTest {

    protected void setUp() throws Exception {
        LockManager lockManager = new LockManager();
        File root = new File(System.getProperty("java.io.tmpdir"),
                "GFileManager");
        Util.recursiveDelete(root);
        root.mkdir();
        
        fileManager = new LocalGFileManager("test", root, lockManager);
    }
    
}
