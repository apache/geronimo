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

public class JVariable extends JType {
    private String            name;
    private JExpression       initExpr;

    public JVariable(Class type, String name) {
        super(type);
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setInitExpression(JExpression initExpr) {
        this.initExpr = initExpr;
    }

    public JExpression getInitExpression() {
        return initExpr;
    }

    public int hashCode() {
        return super.hashCode() + name.hashCode();
    }

    public boolean equals(Object other) {
        boolean rc = false;

        if (other == this) {
            rc = true;
        } else if (other instanceof JVariable) {
            JVariable v = (JVariable) other;

            rc = super.equals(other);
            if (rc)
            {
                v.getName().equals(name);
            }
        }

        return rc;
    }

    protected void showTypeInfo() {
        System.out.println("getName() = " + name);
        super.showTypeInfo();
    }

}
