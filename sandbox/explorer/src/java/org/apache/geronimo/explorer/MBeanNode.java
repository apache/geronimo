/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.geronimo.explorer;

import javax.management.ObjectName;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Tree model for MBeans
 *
 * @version <code>$Rev$ $Date$</code>
 */
public class MBeanNode extends DefaultMutableTreeNode {

    public MBeanNode(ObjectName name) {
        super(name);
    }

    public ObjectName getObjectName() {
        return (ObjectName) getUserObject();
    }    

    public String toString() {
        return getObjectName().getKeyPropertyListString();
    }

}
