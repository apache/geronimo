/**
 *
 *  Copyright 2004-2005 The Apache Software Foundation
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
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.interop.generator;

public class JReturnType {
    protected Class _type;
    protected String _typeName;
    protected boolean _isArray;

    public JReturnType(Class type) {
        this(type, false);
    }

    public JReturnType(Class type, boolean isArray) {
        _type = type;
        _typeName = type.getName();
        _isArray = isArray;
    }

    public JReturnType(String typeName) {
        this(typeName, false);
    }

    public JReturnType(String typeName, boolean isArray) {
        _typeName = typeName;
        _isArray = isArray;

        try {
            _type = Class.forName(_typeName);
        } catch (Exception e) {
            // Ignore;
        }
    }

    public void setType(Class type) {
        _type = type;
        _typeName = type.getName();
    }

    public Class getType() {
        return _type;
    }

    public void setTypeName(String typeName) {
        _type = null;
        _typeName = typeName;
    }

    public String getTypeName() {
        return _typeName;
    }

    public boolean isArray() {
        return _isArray;
    }

    public int hashCode() {
        return _type.hashCode();
    }

    public boolean equals(Object other) {
        boolean rc = false;

        if (other == this) {
            rc = true;
        } else if (other instanceof org.apache.geronimo.interop.generator.JReturnType) {
            org.apache.geronimo.interop.generator.JReturnType jr = (org.apache.geronimo.interop.generator.JReturnType) other;

            rc = jr._typeName.equals(_typeName);
        }

        return rc;
    }
}
