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

import java.beans.PropertyEditorSupport;
import java.net.InetAddress;
import java.util.StringTokenizer;

import org.apache.geronimo.common.propertyeditor.InetAddressEditor;
import org.apache.geronimo.common.propertyeditor.PropertyEditorException;

/**
 * NodeInfo editor.
 *
 * @version $Revision: 1.1 $ $Date: 2004/07/17 03:51:15 $
 */
public class NodeInfoEditor
    extends PropertyEditorSupport
{

    private NodeInfo nodeInfo;
    
    public void setAsText(String text) throws IllegalArgumentException {
        StringTokenizer tokenizer = new StringTokenizer(text, ",");
        
        if ( !tokenizer.hasMoreElements() ) {
            throw new PropertyEditorException("<Name>,<InetAddress>,<Port>");
        }
        String name = (String) tokenizer.nextElement();
        
        if ( !tokenizer.hasMoreElements() ) {
            throw new PropertyEditorException("<Name>,<InetAddress>,<Port>");
        }
        String addressAsString = (String) tokenizer.nextElement();
        InetAddressEditor addressEditor = new InetAddressEditor();
        addressEditor.setAsText(addressAsString);
        InetAddress address = (InetAddress) addressEditor.getValue();

        if ( !tokenizer.hasMoreElements() ) {
            throw new PropertyEditorException("<Name>,<InetAddress>,<Port>");
        }
        String portAsText = (String) tokenizer.nextElement();
        int port = Integer.parseInt(portAsText);
        
        nodeInfo = new NodeInfo(name, address, port);
    }
    
    public Object getValue() {
        return nodeInfo; 
    }
    
}
