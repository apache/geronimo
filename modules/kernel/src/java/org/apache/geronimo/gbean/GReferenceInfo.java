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

package org.apache.geronimo.gbean;

import java.io.Serializable;


/**
 * @version $Revision: 1.5 $ $Date: 2004/05/27 01:05:58 $
 */
public class GReferenceInfo implements Serializable {
    /**
     * Name of this reference.
     */
    private final String name;

    /**
     * Type of this reference.
     */
    private final String type;

    /**
     * Name of the setter method.
     * The default is "set" + name.  In the case of a default value we do a caseless search for the name.
     */
    private final String setterName;

    public GReferenceInfo() {
        this(null, null, null);
    }

    public GReferenceInfo(String name, String type) {
        this(name, type, null);
    }

    public GReferenceInfo(String name, Class type) {
        this(name, type.getName(), null);
    }

    public GReferenceInfo(String name, String type, String setterName) {
        this.name = name;
        this.type = type;
        this.setterName = setterName;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getSetterName() {
        return setterName;
    }

    public String toString() {
        return "[GReferenceInfo: name=" + name +
                " type=" + type +
                " setterName=" + setterName +
                "]";
    }
}
