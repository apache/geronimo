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
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.geronimo.datastore.Util;
import org.apache.geronimo.datastore.impl.GFileDAO;
import org.apache.geronimo.datastore.impl.GFileTO;
import org.apache.geronimo.datastore.impl.LockManager;
import org.apache.geronimo.datastore.impl.local.LocalGFileDAO;

import junit.framework.TestCase;

/**
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/29 13:14:11 $
 */
public class LocalGFileDAOTest extends TestCase
{

    private File root;
    
    protected void setUp() throws Exception {
        root = new File(System.getProperty("java.io.tmpdir"), "GFileManager");
        Util.recursiveDelete(root);
        root.mkdir();
        LocalGFileManager fileManager =
            new LocalGFileManager("test", root, new LockManager());
    }
    
    public void testCRUD() throws Exception {
        String path = "TestFile";
        Map props = new HashMap();
        props.put("name1", "value1");
        props.put("name2", "value2");
        byte[] content = "Dummy content".getBytes();
        ByteArrayInputStream in = new ByteArrayInputStream(content);
        GFileTO fileTO = new GFileTO(path, props, in);
        GFileDAO fileDAO = new LocalGFileDAO(root);

        // Create.
        fileDAO.create(fileTO);
        
        // Read.
        fileTO = fileDAO.read(path);
        assertEquals("Wrong path", path, fileTO.getPath());
        props = fileTO.getProperties();
        assertEquals("Wrong # of properties", 2, props.size());
        for (Iterator iter = props.entrySet().iterator(); iter.hasNext();) {
            Map.Entry property = (Map.Entry) iter.next();
            String name = (String) property.getKey();
            if ( name.equals("name1") ) {
                assertEquals("Wrong value", "value1", property.getValue());
            } else if ( name.equals("name2") ) {
                assertEquals("Wrong value", "value2", property.getValue());
            } else {
                assertTrue("Wrong property name {" + name + "}", false);
            }
        }
        
        // Update
        props.put("name3", "value3");
        fileDAO.update(fileTO);
        fileTO = fileDAO.read(path);
        props = fileTO.getProperties();
        assertEquals("Wrong # of properties", 3, props.size());
        
        // Delete
        fileDAO.delete(path);
        fileTO = fileDAO.read(path);
        assertFalse("Should not exist.", fileTO.exists());
    }
    
}
