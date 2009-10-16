/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.kernel.repository;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;

/**
 * @version $Rev$ $Date$
 */
public class ImportType implements Serializable {
    private static final long serialVersionUID = 9084371394522950958L;

    private static final Map typesByName = new HashMap();

    // todo this class imples that there are only classes and services... is that true?
    public static final ImportType ALL = new ImportType("all");
    public static final ImportType CLASSES = new ImportType("classes");
    public static final ImportType SERVICES = new ImportType("services");

    public static ImportType getByName(String name) {
        ImportType type = (ImportType) typesByName.get(name.toLowerCase());
        if (type == null) throw new IllegalStateException("Unknown import type: " + name);
        return type;
    }

    private final String name;

    private ImportType(String name) {
        this.name = name;
        typesByName.put(name, this);
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return name;
    }

    protected Object readResolve() {
        String name = this.name;
        return getByName(name);
    }
}
