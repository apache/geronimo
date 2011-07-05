/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.system.bundle;

import java.io.File;
import java.io.IOException;

public interface BundleRecorder {
    
    /**
     * Record the bundle in startup.properties
     * @param bundleFile
     * @param groupId
     * @param startLevel
     * @return the installed bundle id, return -1 if installed failed
     * @throws IOException
     */
    public long recordInstall(File bundleFile,  String groupId, int startLevel) throws IOException;
    
    
    /**
     * Erase the bundle if it appears in startup.properties
     * @param bundleId
     * @throws IOException
     */
    public void eraseUninstall(long bundleId) throws IOException;
    
    
    /**
     * Get the bundle id according to its symbolic name and version.
     * It is pretty slow to call listBundles of OSGi JMX API.
     * @param symbolicName
     * @param version
     * @return the installed bundle id, return -1 if can not find.
     */
    public long getBundleId(String symbolicName, String version);
}


