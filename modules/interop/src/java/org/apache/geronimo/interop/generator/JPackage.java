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

import java.util.Vector;


public class JPackage extends JEntity {
    protected Vector _classes = new Vector();

    public JPackage(String name) {
        super(name);
    }

    public JClass newClass(String name) {
        JClass c = new JClass(name, this);
        _classes.add(c);
        return c;
    }

    public void deleteClass(JClass c) {
        _classes.removeElement(c);
    }

    public Vector getClasses() {
        return _classes;
    }
}
