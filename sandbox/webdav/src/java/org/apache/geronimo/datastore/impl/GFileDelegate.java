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

package org.apache.geronimo.datastore.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

/**
 * This is a sub-set of the GFile interface. GFile does not extend this
 * interface as this interface is implementation specific.
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/25 13:36:16 $
 */
public interface GFileDelegate
{

    public boolean exists();
    public boolean isDirectory();
    public boolean isFile();
    public String[] listFiles();
    public void lock();
    public void unlock();
    public Map getProperties();
    public InputStream getInputStream() throws IOException;
    public Map getPropertiesByName(Collection aCollOfNames);
    public void addProperty(String aName, String aValue);
    public void removeProperty(String aName);

}