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

package org.apache.geronimo.messaging;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;

import org.apache.geronimo.common.propertyeditor.PropertyEditorException;

import junit.framework.TestCase;

/**
 *
 * @version $Revision: 1.1 $ $Date: 2004/07/17 03:51:15 $
 */
public class NodeInfoEditorTest
    extends TestCase
{

    public void testOK() throws Exception {
        String name = "Node1";
        String address = "127.0.0.1";
        int port = 1234;
        String text = name +  "," + address + "," + port;
        PropertyEditor ed = PropertyEditorManager.findEditor(NodeInfo.class);
        ed.setAsText(text);
        NodeInfo info = (NodeInfo) ed.getValue();
        assertEquals(name, info.getName());
        assertEquals(address, info.getAddress().getHostAddress());
        assertEquals(port, info.getPort());
    }

    public void testNOK1() throws Exception {
        String name = "Node1";
        String text = name;
        PropertyEditor ed = PropertyEditorManager.findEditor(NodeInfo.class);
        try {
            ed.setAsText(text);
            fail("No address");
        } catch (PropertyEditorException e) {
        }
    }
    
    public void testNOK2() throws Exception {
        String name = "Node1";
        String address = "500.0.0.1";
        String text = name + "," + address;
        PropertyEditor ed = PropertyEditorManager.findEditor(NodeInfo.class);
        try {
            ed.setAsText(text);
            fail("wrong address");
        } catch (PropertyEditorException e) {
        }
    }
    
    public void testNOK3() throws Exception {
        String name = "Node1";
        String address = "127.0.0.1";
        String text = name + "," + address;
        PropertyEditor ed = PropertyEditorManager.findEditor(NodeInfo.class);
        try {
            ed.setAsText(text);
            fail("no port");
        } catch (PropertyEditorException e) {
        }
    }
    
}
