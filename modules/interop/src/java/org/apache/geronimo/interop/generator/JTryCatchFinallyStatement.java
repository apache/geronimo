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


public class JTryCatchFinallyStatement extends JStatement {
    protected JTryStatement _TryStatement;
    protected Vector _catchStatements;
    protected JFinallyStatement _FinallyStatement;

    public JTryCatchFinallyStatement() {
        _TryStatement = new JTryStatement();
        _catchStatements = new Vector();
        _FinallyStatement = new JFinallyStatement();
    }

    public void addTryStatement(JStatement s) {
        _TryStatement.addStatement(s);
    }

    public JTryStatement getTryStatement() {
        return _TryStatement;
    }

    public JCatchStatement getCatch(JVariable v) {
        JCatchStatement rc = null;
        int index = _catchStatements.indexOf(v);

        if (index >= 0) {
            rc = (JCatchStatement) _catchStatements.get(index);
        }

        return rc;
    }

    public JCatchStatement newCatch(JVariable v) {
        JCatchStatement rc = getCatch(v);

        if (rc == null) {
            rc = new JCatchStatement(v);
            _catchStatements.add(rc);
        }

        return rc;
    }

    public void addCatchStatement(JVariable v, JStatement s) {
        JCatchStatement cs = getCatch(v);

        if (cs == null) {
            cs = newCatch(v);
        }

        cs.addStatement(s);
    }

    public Vector getCatches() {
        return _catchStatements;
    }

    public void addFinallyStatement(JStatement s) {
        _FinallyStatement.addStatement(s);
    }

    public JFinallyStatement getFinallyStatement() {
        return _FinallyStatement;
    }
}
