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

public class JBlockStatement extends JStatement {
    private Vector    localVars;
    private Vector    statements;

    public JBlockStatement() {
        localVars = new Vector();
        statements = new Vector();
    }

    public boolean hasVariables() {
        return localVars.size() > 0;
    }

    public boolean hasStatements() {
        return statements.size() > 0;
    }

    public JLocalVariable newLocalVariable(Class type, String name) {
        return newLocalVariable(type, name, null);
    }

    public JLocalVariable newLocalVariable(Class type, String name, JExpression initExpr) {
        JLocalVariable v = new JLocalVariable(type, name);

        v.setInitExpression(initExpr);

        localVars.add(v);

        return v;
    }

    public void deleteLocalVariable(JLocalVariable f) {
        localVars.remove(f);
    }

    public Vector getLocalVariables() {
        return localVars;
    }

    public void addStatement(JStatement s) {
        if (s != this) {
            statements.add(s);
        }
    }

    public Vector getStatements() {
        return statements;
    }
}
