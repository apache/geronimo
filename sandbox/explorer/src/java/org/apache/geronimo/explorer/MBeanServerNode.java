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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Tree model for MBeans
 *
 * @version <code>$Revision: 1.3 $ $Date: 2004/03/10 10:00:24 $</code>
 */
public class MBeanServerNode extends DefaultMutableTreeNode {

    private MBeanServer server;
    private Map domains = new HashMap();

    public MBeanServerNode() {
        this(MBeanServerFactory.createMBeanServer());
    }

    public MBeanServerNode(MBeanServer server) {
        super("Server");
        this.server = server;
        createDomains();
    }
    
    public MBeanServer getMBeanServer() {
        return server;
    }
    
    protected void createDomains() {
        Set names = server.queryNames(null, null);
        for (Iterator iter = names.iterator(); iter.hasNext();) {
            ObjectName name = (ObjectName) iter.next();
            String domain = name.getDomain();
            DefaultMutableTreeNode domainNode = (DefaultMutableTreeNode) domains.get(domain);
            if (domainNode == null) {
                domainNode = new DefaultMutableTreeNode(domain);
                domains.put(domain, domainNode);
                add(domainNode);
            }
            domainNode.add(new MBeanNode(name));
        }
    }
}
