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

import java.beans.PropertyEditorSupport;
import java.lang.reflect.Constructor;

import org.apache.geronimo.common.propertyeditor.PropertyEditorException;

/**
 * TopologyManagerEditor.
 * <BR>
 * Based on a text, it tries to load a TopologyManager having the name:
 * org.apache.geronimo.messaging.cluster.topology.<text>TopologyManager
 * 
 * @version $Rev$ $Date$
 */
public class TopologyManagerEditor
    extends PropertyEditorSupport
{

    private TopologyManager manager;
    
    public void setAsText(String text) throws IllegalArgumentException {
        String className = "org.apache.geronimo.messaging.cluster.topology." + 
            text + "TopologyManager";
        Class clazz;
        try {
            clazz = getClass().getClassLoader().loadClass(className);
            Constructor constructor = clazz.getConstructor(null);
            manager = (TopologyManager) constructor.newInstance(null);
        } catch (Exception e) {
            throw new PropertyEditorException(e);
        }
    }

    public Object getValue() {
        return manager;
    }
    
}
