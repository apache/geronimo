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

import groovy.lang.GroovyObject;

import java.net.URISyntaxException;

import javax.management.MBeanServer;

import org.apache.geronimo.remoting.jmx.RemoteMBeanServerFactory;
import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * A temporary bootstap mechanism for the Groovy script until
 * Groovy reaches beta-1 and can support static main(String[]) methods
 * 
 * @version <code>$Rev$ $Date$</code>
 */
public class ExplorerMain {
    public static void main(String[] args) {
        
        String host="localhost";
        if( args.length > 0 )
            host = args[0];
        
        try {
            GroovyObject explorer = (GroovyObject) ExplorerMain.class.getClassLoader().loadClass("org.apache.geronimo.explorer.Explorer").newInstance();
            InvokerHelper.setProperty(explorer, "treeModel", getMBeanTreeModel(host));
            explorer.invokeMethod("run", null);
        }
        catch (Exception e) {
            System.out.println("Caught: " + e);
            e.printStackTrace();
        }
    }

    public static MBeanTreeModel getMBeanTreeModel(String host)
        throws Exception {
        return new MBeanTreeModel(getMBeanServer(host));
    }
    
    public static MBeanServer getMBeanServer(String host) throws URISyntaxException {
        return RemoteMBeanServerFactory.create(host);
        //return MBeanServerFactory.createMBeanServer();
    }
}
