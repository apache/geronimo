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

import java.beans.PropertyEditorSupport;
import java.net.InetAddress;
import java.util.StringTokenizer;

import org.apache.geronimo.common.propertyeditor.InetAddressEditor;
import org.apache.geronimo.common.propertyeditor.PropertyEditorException;

/**
 * ClusterInfo editor.
 *
 * @version $Rev$ $Date$
 */
public class ClusterInfoEditor
    extends PropertyEditorSupport
{

    private ClusterInfo info;
    
    public void setAsText(String text) throws IllegalArgumentException {
        StringTokenizer tokenizer = new StringTokenizer(text, ",");

        if ( !tokenizer.hasMoreElements() ) {
            throw new PropertyEditorException("<InetAddress>,<port>");
        }
        InetAddressEditor addressEditor = new InetAddressEditor();
        addressEditor.setAsText((String) tokenizer.nextElement());
        InetAddress address = (InetAddress) addressEditor.getValue();
        
        if ( !tokenizer.hasMoreElements() ) {
            throw new PropertyEditorException("<InetAddress>,<port>");
        }
        int port = Integer.parseInt((String) tokenizer.nextElement());
        info = new ClusterInfo(address, port);
    }

    public Object getValue() {
        return info;
    }
    
}
