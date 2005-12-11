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

import java.util.HashMap;

public class JType extends JEntity {

    private static HashMap    typeCache = new HashMap(60);
    private Class             type;
    private String            typeDecl;

    public JType(Class type) {
        super(null);
        setType(type);
    }

    public void setType(Class type) {
        this.type = type;
        this.typeDecl = calculateTypeDecl(type);
    }

    public Class getType() {
        return type;
    }

    public String getTypeDecl() {
        return typeDecl;
    }

    public int hashCode() {
        return type.hashCode();
    }

    public boolean equals(Object other) {
        boolean rc = false;

        if (other == this) {
            rc = true;
        } else if (other instanceof JType) {
            JType t = (JType) other;

            rc = t.type.equals(type);
        }

        return rc;
    }

    private static String calculateTypeDecl(Class type) {
        String typeName = type.getName();
        synchronized (typeCache) {
            String typeDecl= (String) typeCache.get(typeName);
            if (typeDecl != null) {
                return typeDecl;
            }

            StringBuffer typeString = new StringBuffer();

            while (type.isArray()) {
                typeString.append("[]");
                type = type.getComponentType();
            }

            typeString.insert(0, type.getName());
            if (type.getDeclaringClass() != null) {
                String declaringClassName = calculateTypeDecl(type.getDeclaringClass());
                assert type.getName().startsWith(declaringClassName + "$");
                typeString.setCharAt(declaringClassName.length(), '.');
            }

            typeDecl = typeString.toString();
            typeCache.put(typeName, typeDecl);
            return typeDecl;
        }
    }

    protected void showTypeInfo() {
        System.out.println("\tisArray()     = " + type.isArray());
        System.out.println("\tisPrimitive() = " + type.isPrimitive());
        System.out.println("\ttoString()    = " + type.toString());
        System.out.println("\ttypeDecl      = " + getTypeDecl());
        System.out.println("");
    }

    protected void validateDeclType(String t) {
        String ct = getTypeDecl();
        if (!t.equals(ct)) {
            System.out.println("Class Decl Type: '" + ct + "' does not match expected type: '" + t + "'");
        }
    }

    public static void main(String args[])
            throws Exception {
        (new JType(java.lang.String.class)).showTypeInfo();
        (new JType(java.lang.String[].class)).showTypeInfo();
        (new JType(java.lang.String[][].class)).showTypeInfo();

        (new JType(int.class)).showTypeInfo();
        (new JType(int[].class)).showTypeInfo();
        (new JType(int[][].class)).showTypeInfo();

        (new JType(java.lang.String.class)).validateDeclType("java.lang.String");
        (new JType(java.lang.String[].class)).validateDeclType("java.lang.String[]");
        (new JType(java.lang.String[][].class)).validateDeclType("java.lang.String[][]");

        (new JType(int.class)).validateDeclType("int");
        (new JType(int[].class)).validateDeclType("int[]");
        (new JType(int[][].class)).validateDeclType("int[][]");
    }
}
