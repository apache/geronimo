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

public class JEntity {
    protected String _name;
    protected int _modifiers;
    protected JEntity _parent;

    public JEntity(String name) {
        this(name, 0);
    }

    public JEntity(String name, int modifiers) {
        _name = name;
        _modifiers = modifiers;
    }

    public JEntity getParent() {
        return _parent;
    }

    public void setParent(JEntity parent) {
        _parent = parent;
    }

    public String getName() {
        return _name;
    }

    public void setName(String val) {
        _name = val;
    }

    /*
     * if value is true, then the modifier will be set,
     * if value is false, then the modifier will be unset.
     */
    public void setModifier(int modifier, boolean value) {
        if (value) {
            _modifiers = (_modifiers | modifier);
        } else {
            if ((_modifiers & modifier) == modifier) {
                _modifiers = (_modifiers ^ modifier);
            }
        }
    }

    public void setModifiers(int modifiers) {
        _modifiers = modifiers;
    }

    public int getModifiers() {
        return _modifiers;
    }
}
