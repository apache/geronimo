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

package org.apache.geronimo.naming.geronimo;

import java.util.Hashtable;

import javax.naming.NamingException;

import org.apache.geronimo.naming.geronimo.GeronimoContext;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public class GeronimoRootContext extends GeronimoContext {

    private static final String PROTOCOL = "geronimo:";
    private static final int PROTOCOL_LENGTH = PROTOCOL.length();
    static final GeronimoRootContext rootContext = new GeronimoRootContext();

    private GeronimoRootContext() {
        super();
    }

    GeronimoRootContext(Hashtable environment) {
        super(rootContext, environment);
    }

    public Object lookup(String name) throws NamingException {
        if (name.startsWith(PROTOCOL)) {
            name = name.substring(PROTOCOL_LENGTH);
            if (name.length() == 0) {
                return this;
            }
        }
        return super.lookup(name);
    }
}
