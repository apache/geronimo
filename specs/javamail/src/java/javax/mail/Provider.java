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
    /**
     * A enumeration inner class that defines Provider types.
     */
    public static class Type {
        /**
         * A message store provider such as POP3 or IMAP4.
         */
        public static final Type STORE = new Type();

        /**
         * A message transport provider such as SMTP.
         */
        public static final Type TRANSPORT = new Type();

        private Type() {
        }
    }

    private final String className;
    private final String protocol;
    private final Type type;
    private final String vendor;
    private final String version;

    Provider(String protocol, String className, Type type, String vendor, String version) {
        this.protocol = protocol;
        this.className = className;
        this.type = type;
        this.vendor = vendor;
        this.version = version;
    }

    public String getClassName() {
        return className;
    }

    public String getProtocol() {
        return protocol;
    }

    public Type getType() {
        return type;
    }

    public String getVendor() {
        return vendor;
    }

    public String getVersion() {
        return version;
    }

    public String toString() {
        return "protocol="
                + protocol
                + "; type="
                + type
                + "; class="
                + className
                + (vendor == null ? "" : "; vendor=" + vendor)
                + (version == null ? "" : ";version=" + version);
    }
}
