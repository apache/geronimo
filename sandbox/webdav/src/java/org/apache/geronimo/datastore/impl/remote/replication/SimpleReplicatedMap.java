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

package org.apache.geronimo.datastore.impl.remote.replication;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A simple Map, which is ReplicationCapable aware.
 *
 * @version $Revision: 1.2 $ $Date: 2004/03/11 15:36:14 $
 */
public class SimpleReplicatedMap
    implements Map, ReplicationCapable
{

    private Map delegate;
    private Set listeners;
    private Object objectID;
    
    public SimpleReplicatedMap() {
        delegate = new HashMap();
        listeners = new HashSet();
    }

    public void setID(Object anID) {
        objectID = anID;
    }
    
    public Object getID() {
        return objectID; 
    }
    
    public void addUpdateListener(UpdateListener aListener) {
        synchronized(listeners) {
            listeners.add(aListener);
        }
    }
    
    public void removeUpdateListener(UpdateListener aListener) {
        synchronized(listeners) {
            listeners.remove(aListener);
        }
    }
    
    private void multicastEvent(MapUpdateEvent anEvent) {
        synchronized(listeners) {
            for (Iterator iter = listeners.iterator(); iter.hasNext();) {
                UpdateListener listener = (UpdateListener) iter.next();
                listener.fireUpdateEvent(anEvent);
            }
        }
    }
    
    public void mergeWithUpdate(UpdateEvent anEvent)
        throws ReplicationException {
        MapUpdateEvent event = (MapUpdateEvent) anEvent;
        int id = anEvent.getId();
        switch (id) {
            case MapUpdateEvent.CLEAR:
                delegate.clear();
                break;
            case MapUpdateEvent.PUT:
                delegate.put(event.key, event.value);
                break;
            case MapUpdateEvent.PUTALL:
                delegate.putAll(event.map);
                break;
            case MapUpdateEvent.REMOVE:
                delegate.remove(event.key);
                break;
            default:
                throw new ReplicationException("Undefined event id.");
        }
    }

    public void clear() {
        multicastEvent(new MapUpdateEvent(MapUpdateEvent.CLEAR, this));
        delegate.clear();
    }

    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    public Set entrySet() {
        return delegate.entrySet();
    }

    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    public Object get(Object key) {
        return delegate.get(key);
    }

    public int hashCode() {
        return delegate.hashCode();
    }

    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    public Set keySet() {
        return delegate.keySet();
    }

    public Object put(Object key, Object value) {
        multicastEvent(
            new MapUpdateEvent(MapUpdateEvent.PUT, this, key, value));
        return delegate.put(key, value);
    }

    public void putAll(Map t) {
        multicastEvent(new MapUpdateEvent(t, this));
        delegate.putAll(t);
    }

    public Object remove(Object key) {
        multicastEvent(
            new MapUpdateEvent(MapUpdateEvent.REMOVE, this, key, null));
        return delegate.remove(key);
    }

    public int size() {
        return delegate.size();
    }

    public Collection values() {
        return delegate.values();
    }

    public static class MapUpdateEvent implements UpdateEvent {
        private static final int BASE = 1;
        public static final int CLEAR = 1 + BASE;
        public static final int PUT = 2 + BASE;
        public static final int PUTALL = 3 + BASE;
        public static final int REMOVE = 4 + BASE;
        
        private int id;
        private Object target;
        private Object key;
        private Object value;
        private Map map;
        /**
         * Required for Externalization. 
         */
        public MapUpdateEvent() {}
        public MapUpdateEvent(int anId, Object aTarget) {
            this(anId, aTarget, null, null);
        }
        public MapUpdateEvent(int anId, Object aTarget,
            Object aKey,
            Object aValue) {
            id = anId;
            target = aTarget;
            key = aKey;
            value = aValue;
        }
        public MapUpdateEvent(Map aMap, Object aTarget) {
            this(PUTALL, aTarget, null, null);
            map = aMap;
        }
        public int getId() {
            return id;
        }
        public Object getTarget() {
            return target;
        }
        public void setTarget(Object aTarget) {
            target = aTarget;
        }
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeInt(id);
            out.writeObject(target);
            out.writeObject(key);
            out.writeObject(value);
            out.writeObject(map);
        }
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            id = in.readInt();
            target = in.readObject();
            key = in.readObject();
            value = in.readObject();
            map = (Map) in.readObject();
        }
    }
    
}
