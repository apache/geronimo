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

package org.apache.geronimo.messaging.cluster.topology;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;

import org.apache.geronimo.common.propertyeditor.PropertyEditorException;

import junit.framework.TestCase;

/**
 *
 * @version $Rev$ $Date$
 */
public class TopologyManagerEditorTest
    extends TestCase
{

    public void testOK() throws Exception {
        String type = "Ring";
        PropertyEditor ed =
            PropertyEditorManager.findEditor(TopologyManager.class);
        ed.setAsText(type);
        Object opaque = ed.getValue();
        assertTrue(opaque instanceof RingTopologyManager);
    }
    
    public void testNOK() throws Exception {
        String type = "Ring2";
        PropertyEditor ed =
            PropertyEditorManager.findEditor(TopologyManager.class);
        try {
            ed.setAsText(type);
            fail("Undefined TopologyManager");
        } catch (PropertyEditorException e) {
        }
    }
    
}
