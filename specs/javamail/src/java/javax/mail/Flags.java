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

package javax.mail;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class Flags implements Cloneable, Serializable {
    public static final class Flag {
        public static final Flag ANSWERED = new Flag("Answered", true);
        public static final Flag DELETED = new Flag("Deleted", true);
        public static final Flag DRAFT = new Flag("Draft", true);
        public static final Flag FLAGGED = new Flag("Flagged", true);
        public static final Flag RECENT = new Flag("Recent", true);
        public static final Flag SEEN = new Flag("Seen", true);
        public static final Flag USER = new Flag("", false);
        private String _name;
        private boolean _system;

        private Flag(String name) {
            this(name, false);
        }

        private Flag(String name, boolean system) {
            if (name == null) {
                throw new IllegalArgumentException("Flag name cannot be null");
            }
            _name = name;
            _system = system;
        }

        private String getName() {
            return _name;
        }

        private boolean isSystemFlag() {
            return _system;
        }
    }

    private static final Flag[] FLAG_ARRAY = new Flag[0];
    private static final String[] STRING_ARRAY = new String[0];
    private Map _map = new HashMap(4);

    public Flags() {
    }

    public Flags(Flag flag) {
        add(flag);
    }

    public Flags(Flags flags) {
        add(flags);
    }

    public Flags(String name) {
        add(name);
    }

    public void add(Flag flag) {
        _map.put(flag.getName(), flag);
    }

    public void add(Flags flags) {
        _map.putAll(flags._map);
    }

    public void add(String name) {
        add(new Flag(name));
    }

    public Object clone() {
        try {
            Flags clone = (Flags) super.clone();
            // do a deep clone of user_flags
            clone._map = new HashMap(_map);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public boolean contains(Flag flag) {
        return _map.containsKey(flag.getName());
    }

    public boolean contains(Flags flags) {
        Iterator it = flags._map.keySet().iterator();
        boolean result = true;
        while (result && it.hasNext()) {
            result = _map.containsKey(it.next());
        }
        return result;
    }

    public boolean contains(String name) {
        return _map.containsKey(name);
    }

    public boolean equals(Object other) {
        if (other == null || other.getClass() != this.getClass()) {
            return false;
        }
        Flags flags = (Flags) other;
        return flags._map.equals(this._map);
    }

    public Flag[] getSystemFlags() {
        List result = new LinkedList();
        Iterator it = _map.values().iterator();
        while (it.hasNext()) {
            Flag flag = (Flag) it.next();
            if (flag.isSystemFlag()) {
                result.add(flag);
            }
        }
        return (Flag[]) result.toArray(FLAG_ARRAY);
    }

    public String[] getUserFlags() {
        List result = new LinkedList();
        Iterator it = _map.values().iterator();
        while (it.hasNext()) {
            Flag flag = (Flag) it.next();
            if (!flag.isSystemFlag()) {
                result.add(flag.getName());
            }
        }
        return (String[]) result.toArray(STRING_ARRAY);
    }

    public int hashCode() {
        return _map.keySet().hashCode();
    }

    public void remove(Flag flag) {
        _map.remove(flag.getName());
    }

    public void remove(Flags flags) {
        Iterator it = flags._map.keySet().iterator();
        while (it.hasNext()) {
            _map.remove(it.next());
        }
    }

    public void remove(String name) {
        _map.remove(name);
    }

    public String toString() {
        return _map.keySet().toString();
    }
}
