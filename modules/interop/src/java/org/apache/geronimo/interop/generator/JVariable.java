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


public class JVariable extends JEntity {
    protected static HashMap _typeMap = new HashMap(60);

    protected Class _type;
    protected String _typeDecl;
    protected JExpression _initExpr;
    protected boolean _isArray;

    public JVariable(Class type, String name) {
        super(name);
        setType(type);
    }

    public void setType(Class type) {
        _type = type;
        calculateTypeDecl();
    }

    public Class getType() {
        return _type;
    }

    public String getTypeDecl() {
        return _typeDecl;
    }

    public void setInitExpression(JExpression initExpr) {
        _initExpr = initExpr;
    }

    public JExpression getInitExpression() {
        return _initExpr;
    }

    public int hashCode() {
        return _type.hashCode() + _name.hashCode();
    }

    public boolean equals(Object other) {
        boolean rc = false;

        if (other == this) {
            rc = true;
        } else if (other instanceof JVariable) {
            JVariable v = (JVariable) other;

            rc = v._type.equals(_type);
        }

        return rc;
    }

    protected void calculateTypeDecl() {
        if (_type == null) {
            return;
        }

        _typeDecl = (String) _typeMap.get(_type);

        if (_typeDecl == null) {
            synchronized (_typeMap) {
                _typeDecl = _type.getName();

                if (_type.isArray()) {
                    _typeDecl = convertToTypeDecl(_typeDecl);
                }

                _typeMap.put(_type, _typeDecl);
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
        System.out.println("getName() = " + _type.getName());
        System.out.println("\tisArray()     = " + _type.isArray());
        System.out.println("\tisPrimitive() = " + _type.isPrimitive());
        System.out.println("\ttoString()    = " + _type.toString());
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
        (new JVariable(java.lang.String.class, "v")).showTypeInfo();
        (new JVariable(java.lang.String[].class, "v")).showTypeInfo();
        (new JVariable(java.lang.String[][].class, "v")).showTypeInfo();

        (new JVariable(int.class, "v")).showTypeInfo();
        (new JVariable(int[].class, "v")).showTypeInfo();
        (new JVariable(int[][].class, "v")).showTypeInfo();

        (new JVariable(java.lang.String.class, "v")).validateDeclType("java.lang.String");
        (new JVariable(java.lang.String[].class, "v")).validateDeclType("java.lang.String[]");
        (new JVariable(java.lang.String[][].class, "v")).validateDeclType("java.lang.String[][]");

        (new JVariable(int.class, "v")).validateDeclType("int");
        (new JVariable(int[].class, "v")).validateDeclType("int[]");
        (new JVariable(int[][].class, "v")).validateDeclType("int[][]");
    }
}
