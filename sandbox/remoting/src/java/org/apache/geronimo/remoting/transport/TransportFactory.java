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

package org.apache.geronimo.remoting.transport;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @version $Rev$ $Date$
 */
public abstract class TransportFactory {

    private static ArrayList factories = new ArrayList();
    static {
        // Try our best to add a few known TransportFactory objects.
        // We may not be able to load due to ClassNotFound problems.
        try {
            addFactory(loadFactory("async"));
        } catch (Throwable ignore) {
        }
    }
    
    static private TransportFactory loadFactory(String type) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        String className = "org.apache.geronimo.remoting.transport."+type+".TransportFactory";
        return (TransportFactory) Thread.currentThread().getContextClassLoader().loadClass(className).newInstance();
    }

    public static TransportFactory getTransportFactory(URI uri) {
        Iterator iterator = factories.iterator();
        while (iterator.hasNext()) {
            TransportFactory i = (TransportFactory) iterator.next();
            if (i.handles(uri))
                return i;
        }

        // Not found.. see if we can dynamicly load a new Factory for it based on scheme.        
        try {
            TransportFactory factory = loadFactory(uri.getScheme());
            if (factory.handles(uri)) {
                addFactory(factory);
                return factory;                
            }
        } catch (Throwable ignore) {
        }
        return null;

    }

    public static boolean unexport(Object object) {
        boolean wasExported = false;
        Iterator iterator = factories.iterator();
        while (iterator.hasNext()) {
            TransportFactory i = (TransportFactory) iterator.next();
            if( i.doUnexport(object) )
                wasExported = true;
        }
        return wasExported;
    }

    static public void addFactory(TransportFactory tf) {
        factories.add(tf);
    }

    static public void removeFactory(TransportFactory tf) {
        factories.remove(tf);
    }

    abstract protected boolean handles(URI uri);
    abstract public TransportClient createClient();
    abstract public TransportServer createSever();
    abstract public boolean doUnexport(Object object);

}
