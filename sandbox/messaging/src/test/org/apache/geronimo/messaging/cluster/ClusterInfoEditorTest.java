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

package org.apache.geronimo.messaging.cluster;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;

import org.apache.geronimo.common.propertyeditor.PropertyEditorException;

import junit.framework.TestCase;

/**
 *
 * @version $Rev$ $Date$
 */
public class ClusterInfoEditorTest
    extends TestCase
{

    public void testOK() throws Exception {
        String address = "127.0.0.1";
        int port = 1234;
        String property = address + "," + port;
        PropertyEditor ed = PropertyEditorManager.findEditor(ClusterInfo.class);
        ed.setAsText(property);
        ClusterInfo info = (ClusterInfo) ed.getValue();
        assertEquals(address, info.getAddress().getHostAddress());
        assertEquals(port, info.getPort());
    }
     
    public void testNOK() throws Exception {
        String address = "234.0.0.0";
        String property = address;
        PropertyEditor ed = PropertyEditorManager.findEditor(ClusterInfo.class);
        try {
            ed.setAsText(property);
            fail("No port.");
        } catch (PropertyEditorException e) {
        }
    }
    
}
