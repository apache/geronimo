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


public class JSwitchStatement extends JStatement {
    protected JExpression _switchExpr;
    protected Vector _caseStatements;

    public JSwitchStatement(JExpression e) {
        _switchExpr = e;
        _caseStatements = new Vector();
    }

    public void setVariable(JExpression e) {
        _switchExpr = e;
    }

    public JExpression getExpression() {
        return _switchExpr;
    }

    public JCaseStatement getCase(JExpression e) {
        JCaseStatement rc = null;
        int index = _caseStatements.indexOf(e);

        if (index >= 0) {
            rc = (JCaseStatement) _caseStatements.get(index);
        }

        return rc;
    }

    public JCaseStatement newCase(JExpression e) {
        JCaseStatement rc = getCase(e);

        if (rc == null) {
            rc = new JCaseStatement(e);
            _caseStatements.add(rc);
        }

        return rc;
    }

    public void addCaseStatement(JExpression e, JStatement s) {
        JCaseStatement cs = getCase(e);

        if (cs == null) {
            cs = newCase(e);
        }

        cs.addStatement(s);
    }

    public Vector getCases() {
        return _caseStatements;
    }
}
