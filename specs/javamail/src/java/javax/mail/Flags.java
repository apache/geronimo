/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//
package javax.mail;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
        public boolean equals(Object other) {
            if (other == null || other.getClass() != this.getClass()) {
                return false;
            }
            Flag flag = (Flag) other;
            return (
                flag.getName() == null
                    && this.getName() == null
                    || flag.getName() != null
                    && flag.getName().equals(this.getName()));
        }
        private String getName() {
            return _name;
        }
        public int hashCode() {
            return getName().hashCode();
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
    public Object clone() throws CloneNotSupportedException {
        Flags clone = (Flags) super.clone();
        // do a deep clone of user_flags
        clone._map = new HashMap(_map);
        return clone;
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
