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

    private static HashMap    typeMap = new HashMap(60);
    private Class             type;
    private String            typeDecl;

    public JType(Class type) {
        super(null);
        setType(type);
    }

    public void setType(Class type) {
        this.type = type;
        calculateTypeDecl();
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

    protected void calculateTypeDecl() {
        if (type == null) {
            return;
        }

        typeDecl = (String) typeMap.get(type);

        if (typeDecl == null) {
            synchronized (typeMap) {
                typeDecl = type.getName();

                if (type.isArray()) {
                    typeDecl = convertToTypeDecl(typeDecl);
                }

                typeMap.put(type, typeDecl);
            }
        }
    }

    protected String convertToTypeDecl(String typeName) {
        String rc = "";
        char charAt = 0;
        int i;

        if (typeName != null && typeName.length() > 0) {
            for (i = 0; i < typeName.length(); i++) {
                charAt = typeName.charAt(i);

                if (charAt == '[') {
                    rc = rc + "[]";
                } else if (charAt == 'Z') {
                    rc = "boolean" + rc;
                } else if (charAt == 'B') {
                    rc = "byte" + rc;
                } else if (charAt == 'C') {
                    rc = "char" + rc;
                } else if (charAt == 'L') {
                    int semiIndex = typeName.indexOf(";");
                    rc = typeName.substring(i + 1, semiIndex) + rc;
                    i = semiIndex;
                } else if (charAt == 'D') {
                    rc = "double" + rc;
                } else if (charAt == 'F') {
                    rc = "float" + rc;
                } else if (charAt == 'I') {
                    rc = "int" + rc;
                } else if (charAt == 'J') {
                    rc = "long" + rc;
                } else if (charAt == 'S') {
                    rc = "short" + rc;
                } else {
                    System.out.println("Error: Invalid signature. typeName = " + typeName + ", charAt = " + charAt + ", i = " + i);
                }
            }
        }

        return rc;
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
