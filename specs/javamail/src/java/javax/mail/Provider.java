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

/**
 * @version $Rev$ $Date$
 */
public class Provider {
    Provider(String protocol,
             String className,
             Type type,
             String vendor,
             String version) {
        _protocol = protocol;
        _className = className;
        _type = type;
        _vendor = vendor;
        _version = version;
    }

    public static class Type {
        private String _name;
        public static final Type STORE = new Type("store");
        public static final Type TRANSPORT = new Type("transport");

        private Type(String name) {
            _name = name;
        }

        static Type getType(String name) {
            if (name.equals("store")) {
                return STORE;
            } else if (name.equals("transport")) {
                return TRANSPORT;
            } else {
                return null;
            }
        }
    }

    private String _className;
    private String _protocol;
    private Type _type;
    private String _vendor;
    private String _version;

    public String getClassName() {
        return _className;
    }

    public String getProtocol() {
        return _protocol;
    }

    public Type getType() {
        return _type;
    }

    public String getVendor() {
        return _vendor;
    }

    public String getVersion() {
        return _version;
    }

    public String toString() {
        return "protocol="
                + _protocol
                + "; type="
                + _type
                + "; class="
                + _className
                + (_vendor == null ? "" : "; vendor=" + _vendor)
                + (_version == null ? "" : ";version=" + _version);
    }
}
