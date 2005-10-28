/**
 *
 * Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
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

package org.apache.geronimo.corba.dii;

public class NamedValueImpl
        extends org.omg.CORBA.NamedValue
{

    String name;
    int flags;
    org.omg.CORBA.Any value;

    public NamedValueImpl(String name, org.omg.CORBA.Any value, int flags) {
        this.name = name;
        this.value = value;
        this.flags = flags;
    }

    public String name() {
        return name;
    }

    public org.omg.CORBA.Any value() {
        return value;
    }

    public int flags() {
        return flags;
    }
}

