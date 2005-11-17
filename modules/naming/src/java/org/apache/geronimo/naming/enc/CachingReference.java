/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.naming.enc;

import org.apache.geronimo.naming.reference.SimpleReference;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.spi.NamingManager;
import java.util.Hashtable;

/**
 * @version $Rev$ $Date$
 */
public class CachingReference {
    private final Object lock = new Object();
    private final String fullName;
    private final Reference reference;
    private final String className;
    private Object value;

    public CachingReference(String fullName, Reference reference) {
        this.fullName = fullName;
        this.reference = reference;
        className = reference.getClassName();
    }

    public Object get() throws NamingException {
        synchronized(lock) {
            if (value == null) {
                value = resolveReference();
            }
            return value;
        }
    }

    private Object resolveReference() throws NamingException {
        // for SimpleReference we can just call the getContext method
        if (reference instanceof SimpleReference) {
            try {
                return ((SimpleReference) reference).getContent();
            } catch (NamingException e) {
                throw e;
            } catch (Exception e) {
                throw (NamingException) new NamingException("Could not look up : " + fullName).initCause(e);
            }
        }

        // for normal References we have to do it the slow way
        try {
            return NamingManager.getObjectInstance(reference, null, null, new Hashtable());
        } catch (NamingException e) {
            throw e;
        } catch (Exception e) {
            throw (NamingException) new NamingException("Could not look up : " + fullName).initCause(e);
        }
    }

    public String getClassName() {
        return className;
    }
}
