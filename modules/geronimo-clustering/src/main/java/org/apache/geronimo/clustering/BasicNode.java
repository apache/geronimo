/**
 *
 * Copyright 2006 The Apache Software Foundation
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
package org.apache.geronimo.clustering;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;

/**
 *
 * @version $Rev$ $Date$
 */
public class BasicNode implements Node {
    private final String name;
    
    public BasicNode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    public static final GBeanInfo GBEAN_INFO;
    
    public static final String GBEAN_ATTR_NODE_NAME = "nodeName";
    
    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(BasicNode.class, NameFactory.GERONIMO_SERVICE);
        
        infoBuilder.addAttribute(GBEAN_ATTR_NODE_NAME, String.class, true);
        
        infoBuilder.addInterface(Node.class);
        
        infoBuilder.setConstructor(new String[] {GBEAN_ATTR_NODE_NAME});
        
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
