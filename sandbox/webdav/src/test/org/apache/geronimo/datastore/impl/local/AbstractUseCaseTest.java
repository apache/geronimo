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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import org.apache.geronimo.datastore.GFile;
import org.apache.geronimo.datastore.GFileManager;

import junit.framework.TestCase;

/**
 *
 * @version $Revision: 1.2 $ $Date: 2004/03/24 11:42:57 $
 */
public class AbstractUseCaseTest
    extends TestCase
{
    
    protected GFileManager fileManager;
    
    public void testUseCase() throws Exception {

        byte[] content = "Dummy content".getBytes();
        
        Object interactId = fileManager.startInteraction();
        GFile file = fileManager.factoryGFile(interactId, "test");
        fileManager.persistNew(interactId, file);
        file.addProperty("name1", "value1");
        file.addProperty("name2", "value2");
        file.addProperty("name3", "value3");
        file.setContent(new ByteArrayInputStream(content));
        fileManager.endInteraction(interactId);
        
        interactId = fileManager.startInteraction();
        InputStream in = file.getInputStream();
        int read;
        int nbRead = 0;
        while ( -1 < (read = in.read()) ) {
            assertEquals("Wrong content", content[nbRead], read);
            nbRead++;
        }
        Map properties = file.getProperties();
        assertEquals("Properties issue", 3, properties.size());
        assertEquals("Properties issue", "value1", properties.get("name1"));
        assertEquals("Properties issue", "value2", properties.get("name2"));
        assertEquals("Properties issue", "value3", properties.get("name3"));
        file.addProperty("name4", "value4");
        file.removeProperty("name3");
        fileManager.persistUpdate(interactId, file);
        fileManager.endInteraction(interactId);
        
        interactId = fileManager.startInteraction();
        properties = file.getProperties();
        assertEquals("Properties issue", 3, properties.size());
        assertEquals("Properties issue", "value1", properties.get("name1"));
        assertEquals("Properties issue", "value2", properties.get("name2"));
        assertEquals("Properties issue", "value4", properties.get("name4"));
        fileManager.persistDelete(interactId, file);
        fileManager.endInteraction(interactId);
    }
    
}
