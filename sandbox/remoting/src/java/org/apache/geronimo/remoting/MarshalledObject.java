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

package org.apache.geronimo.remoting;

import java.io.IOException;

/**
 * Interface to a marshalled form of an object.  This is an interface because
 * there might be multiple marshalled forms we want to support based on transport.
 * Example: 
 *    - XML or SOAP based marshalled format
 *    - byte[] based marshalled format
 * 
 * @version $Rev$ $Date$
 */
public interface MarshalledObject {

    public void set(Object object) throws IOException;
    public Object get() throws IOException, ClassNotFoundException;
    public Object get(ClassLoader classloader) throws IOException, ClassNotFoundException;

}
