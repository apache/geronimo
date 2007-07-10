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
package org.apache.geronimo.clustering;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 *
 * @version $Rev$ $Date$
 */
public abstract class AbstractNode implements Node {
    private final String name;
    
    public AbstractNode(String name) {
        if (null == name) {
            throw new IllegalArgumentException("name is required");
        }
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    public JMXConnector getJMXConnector() throws IOException {
        Map<String, Object> environment = new HashMap<String, Object>();
        environment.put(JMXConnectorFactory.DEFAULT_CLASS_LOADER, AbstractNode.class.getClassLoader());
        JMXServiceURL address = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + getHost() + ":" + getPort() + "/JMXConnector");
        return JMXConnectorFactory.connect(address, environment);
    }
    
    protected abstract String getHost();
    
    protected abstract int getPort();

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AbstractNode)) {
            return false;
        }
        AbstractNode other = (AbstractNode) obj;
        return name.equals(other.name);
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }
    
}
