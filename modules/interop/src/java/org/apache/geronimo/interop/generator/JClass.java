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
import java.util.Vector;

public class JClass extends JEntity {
    private JPackage      pkg;
    private Vector        imports;
    private Vector        impls;
    private String        baseClassName;
    private Vector        constructors;
    private Vector        methods;
    private Vector        fields;
    private Vector        classes = new Vector();
    private JClass        parent;

    protected JClass(String name) {
        super(name, Modifier.PUBLIC);

        imports = new Vector();
        impls = new Vector();
        baseClassName = "";

        constructors = new Vector();
        methods = new Vector();
        fields = new Vector();
    }

    protected JClass(String name, JPackage pkg) {
        this(name);

        if (pkg == null) {
            pkg = new JPackage("");
        }

        this.pkg = pkg;
    }

    protected JClass(String name, JClass parent) {
        this(name);

        this.parent = parent;
    }

    public JConstructor newConstructor(JParameter parms[], Class thrown[]) {
        JConstructor c = new JConstructor(parms, thrown);
        c.setParent(this);
        constructors.add(c);
        return c;
    }

    public void deleteConstructor(JConstructor m) {
        constructors.removeElement(m);
    }

    public Vector getConstructors() {
        return constructors;
    }

    public JMethod newMethod(JReturnType rt, String name, JParameter parms[], Class thrown[]) {
        JMethod m = new JMethod(rt, name, parms, thrown);
        m.setParent(this);
        methods.add(m);
        return m;
    }

    public void deleteMethod(JMethod m) {
        methods.removeElement(m);
    }

    public Vector getMethods() {
        return methods;
    }

    protected void setFieldParentAndModifier(JField f) {
        f.setParent(this);

        if (Modifier.isPublic(this.getModifiers())) {
            f.setModifiers(f.getModifiers() | Modifier.PUBLIC);
        }

        if (Modifier.isProtected(this.getModifiers())) {
            f.setModifiers(f.getModifiers() | Modifier.PROTECTED);
        }

        if (Modifier.isPrivate(this.getModifiers())) {
            f.setModifiers(f.getModifiers() | Modifier.PRIVATE);
        }
    }

    public JField newField(Class type, String name) {
        return newField(type, name, null);
    }

    public JField newField(Class type, String name, JExpression initExpr) {
        return newField(type, name, initExpr, false);
    }

    public JField newField(Class type, String name, JExpression initExpr, boolean isArray) {
        JField f = new JField(type, name);

        setFieldParentAndModifier(f);
        f.setInitExpression(initExpr);

        fields.add(f);

        return f;
    }

    public void deleteField(JField f) {
        fields.remove(f);
    }

    public Vector getFields() {
        return fields;
    }

    public JClass newClass(String name) {
        JClass c = new JClass(name, this);
        classes.add(c);
        return c;
    }

    public JPackage getPackage() {
        if (parent != null) {
            return parent.getPackage();
        } else {
            return pkg;
        }
    }

    public String getName() {
        if (parent != null) {
            return parent.getName() + "$" + super.getName();
        } else {
            return super.getName();
        }
    }

    public void setExtends(String bcl) {
        setBaseClassName(bcl);
    }

    public String getExtends() {
        return getBaseClassName();
    }

    public void setBaseClassName(String bcl) {
        baseClassName = bcl;
    }

    public String getBaseClassName() {
        return baseClassName;
    }

    public void addImplements(String className) {
        impls.add(className);
    }

    public void removeImplements(String className) {
        impls.remove(className);
    }

    public Vector getImplements() {
        return impls;
    }

    /*
     * Adding Imports
     */
    public void addImport(Package pkg, String itemName) {
        if (pkg != null) {
            addImport(pkg.getName(), itemName);
        }
    }

    public void addImport(Package pkg) {
        if (pkg != null) {
            addImport(pkg.getName(), "*");
        }
    }

    public void addImport(String name, String itemName) {
        addImport(name + "." + itemName);
    }

    public void addImport(String fqName) {
        imports.add(fqName);
    }

    public void removeImport(Package pkg, String itemName) {
        if (pkg != null) {
            removeImport(pkg.getName(), itemName);
        }
    }

    public void removeImport(Package pkg) {
        if (pkg != null) {
            removeImport(pkg.getName());
        }
    }

    public void removeImport(String name, String itemName) {
        removeImport(name + "." + itemName);
    }

    public void removeImport(String name) {
        imports.remove(name);
    }

    public Vector getImports() {
        return imports;
    }
}
