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

public class JIfElseIfElseStatement extends JStatement {
    private JIfStatement      ifStatement;
    private Vector            elseifStatements;
    private JElseStatement    elseStatement;

    public JIfElseIfElseStatement(JExpression if_expr) {
        ifStatement = new JIfStatement(if_expr);
        elseifStatements = new Vector();
        elseStatement = new JElseStatement();
    }

    public void addIfStatement(JStatement s) {
        ifStatement.addStatement(s);
    }

    public JIfStatement getIfStatement() {
        return ifStatement;
    }

    public Vector getIfStatements() {
        return ifStatement.getStatements();
    }

    public void addElseStatement(JStatement s) {
        elseStatement.addStatement(s);
    }

    public JElseStatement getElseStatement() {
        return elseStatement;
    }

    public Vector getElseStatements() {
        return elseStatement.getStatements();
    }

    public JElseIfStatement getElseIf(JExpression e) {
        JElseIfStatement rc = null;
        int index = elseifStatements.indexOf(e);

        if (index >= 0) {
            rc = (JElseIfStatement) elseifStatements.get(index);
        }

        return rc;
    }

    public JElseIfStatement newElseIf(JExpression e) {
        JElseIfStatement rc = getElseIf(e);

        if (rc == null) {
            rc = new JElseIfStatement(e);
            elseifStatements.add(rc);
        }

        return rc;
    }

    public void addCatchStatement(JExpression e, JStatement s) {
        JElseIfStatement eis = getElseIf(e);

        if (eis == null) {
            eis = newElseIf(e);
        }

        eis.addStatement(s);
    }

    public Vector getElseIfs() {
        return elseifStatements;
    }
}
