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
package org.apache.geronimo.interop.rmi.iiop.client;

public class ValueInfo {
    public String _nameToBeResolved = null;
    public Object _objectToBeBound = null;

    public ValueInfo(String nameToBeResolved) {
        _nameToBeResolved = nameToBeResolved;
    }

    public ValueInfo(Object objectToBeBound) {
        _objectToBeBound = objectToBeBound;
    }

    public void setNameToBeResolved(String name) {
        _nameToBeResolved = name;
    }

    public String getNameToBeResolved() {
        return _nameToBeResolved;
    }

    public void setObjectToBeBound(Object object) {
        _objectToBeBound = object;
    }

    public Object getObjectToBeBound() {
        return _objectToBeBound;
    }
}
