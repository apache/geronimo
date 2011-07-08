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
package org.apache.geronimo.converter;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

/**
 * Some helper methods for reading DOM trees.  Have I written this like 8 times or what?
 *
 * @version $Rev$ $Date$
 */
public class DOMUtils {
    public static String getChildText(Element parent, String childName) {
        NodeList list = parent.getElementsByTagName(childName);
        if(list.getLength() > 1) {
            throw new IllegalStateException("Multiple child elements with name " + childName);
        } else if(list.getLength() == 0) {
            return null;
        }
        Element child = (Element) list.item(0);
        return getText(child);
    }

    public static String getText(Element element) {
        StringBuilder buf = new StringBuilder();
        NodeList list = element.getChildNodes();
        boolean found = false;
        for(int i=0; i<list.getLength(); i++) {
            Node node = list.item(i);
            if(node.getNodeType() == Node.TEXT_NODE) {
                buf.append(node.getNodeValue());
                found = true;
            }
        }
        return found ? buf.toString() : null;
    }
}
