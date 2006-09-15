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
package org.apache.geronimo.clustering.wadi;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.geronimo.clustering.Session;
import org.codehaus.wadi.web.WebSession;

/**
 *
 * @version $Rev$ $Date$
 */
public class WADISessionAdaptor implements Session {
    private final WebSession session;
    private final Map state;
    
    public WADISessionAdaptor(WebSession session) {
        this.session = session;
        
        state = new StateMap();
    }

    public String getSessionId() {
        return session.getId();
    }

    public void release() {
        try {
            session.destroy();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot release session " + session);
        }
    }

    public Object addState(String key, Object value) {
        return session.setAttribute(key, value);
    }

    public Object getState(String key) {
        return session.getAttribute(key);
     }

    public Object removeState(String key) {
        return session.removeAttribute(key);
    }

    public Map getState() {
        return state;
    }
    
    private class StateMap implements Map {

        public Object put(Object key, Object value) {
            String wadiKey = ensureTypeAndCast(key);
            return addState(wadiKey, value);
        }

        public Object remove(Object key) {
            String wadiKey = ensureTypeAndCast(key);
            return removeState(wadiKey);
        }

        public void clear() {
            throw new UnsupportedOperationException();
        }

        public boolean containsKey(Object key) {
            throw new UnsupportedOperationException();
        }

        public boolean containsValue(Object value) {
            throw new UnsupportedOperationException();
        }

        public Set entrySet() {
            throw new UnsupportedOperationException();
        }

        public Object get(Object key) {
            String wadiKey = ensureTypeAndCast(key);
            return getState(wadiKey);
        }

        public boolean isEmpty() {
            throw new UnsupportedOperationException();
        }

        public Set keySet() {
            return session.getAttributeNameSet();
        }

        public void putAll(Map t) {
            throw new UnsupportedOperationException();
        }

        public int size() {
            return session.getAttributeNameSet().size();
        }

        public Collection values() {
            throw new UnsupportedOperationException();
        }

        private String ensureTypeAndCast(Object key) {
            if (!(key instanceof String)) {
                throw new ClassCastException(String.class + " is expected.");
            }
            return (String) key;
        }
    }
}
