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

import java.lang.reflect.Modifier;

public class JEntity {
    private String    name;
    private int       modifiers;
    private JEntity   parent;

    public JEntity(String name) {
        this(name, Modifier.PUBLIC);
    }

    public JEntity(String name, int modifiers) {
        this.name = name;
        this.modifiers = modifiers;
    }

    public JEntity getParent() {
        return parent;
    }

    public void setParent(JEntity parent) {
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String val) {
        name = val;
    }

    /*
     * if value is true, then the modifier will be set,
     * if value is false, then the modifier will be unset.
     */
    protected void adjustModifier(int modifier, boolean value) {
        if (value) {
            modifiers = (modifiers | modifier);
        } else {
            if ((modifiers & modifier) == modifier) {
                modifiers = (modifiers ^ modifier);
            }
        }
    }

    public void setModifier( int modifier )
    {
        adjustModifier( modifier, true );
    }

    public void unsetModifier( int modifier )
    {
        adjustModifier( modifier, false );
    }

    /*
     * Sets all the modifiers in one set
     *
     * Example: setModifiers(Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL);
     */
    public void setModifiers(int modifiers) {
        this.modifiers = modifiers;
    }

    public int getModifiers() {
        return modifiers;
    }
}
