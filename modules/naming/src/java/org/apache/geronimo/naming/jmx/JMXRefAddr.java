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

package org.apache.geronimo.naming.jmx;

import java.net.URI;
import java.net.URISyntaxException;
import javax.naming.RefAddr;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public class JMXRefAddr extends RefAddr {

    private final static String TYPE = "org.apache.geronimo.naming.jmx.RefType";

    private final String kernelName;
    private final String containerId;
    private final static String SCHEME = "geronimo";
    private final Class iface;

    public JMXRefAddr(String kernelName, String containerId, Class iface) {
        super(TYPE);
        this.kernelName = kernelName;
        this.containerId = containerId;
        this.iface = iface;
    }


    public String getKernelName() {
        return kernelName;
    }

    public String getContainerId() {
        return containerId;
    }

    public Class getInterface() {
        return iface;
    }

    public Object getContent() {
        try {
            return new URI(SCHEME, kernelName, containerId, null);
        } catch (URISyntaxException e) {
            throw (IllegalStateException)new IllegalStateException("invalid jmx ref addr").initCause(e);
        }
    }
}
