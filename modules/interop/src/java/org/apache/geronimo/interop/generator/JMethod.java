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

public class JMethod extends JEntity {
    private JReturnType       rt;
    private JParameter        parms[];
    private Class             thrown[];
    private Vector            statements;
    private JBlockStatement   bodyBlockStatement;
    private String            body;  // Yuck

    protected JMethod(String name) {
        super(name, Modifier.PUBLIC);

        statements = new Vector();
        bodyBlockStatement = new JBlockStatement();
    }

    protected JMethod(JReturnType rt, String name, JParameter parms[], Class thrown[]) {
        this(name);

        setRT(rt);
        setParms(parms);
        setThrown(thrown);
    }

    public void setRT(JReturnType jt) {
        rt = jt;
    }

    public JReturnType getRT() {
        return rt;
    }

    public void setParms(JParameter parms[]) {
        this.parms = parms;
    }

    public JParameter[] getParms() {
        return parms;
    }

    public void setThrown(Class thrown[]) {
        this.thrown = thrown;
    }

    public Class[] getThrown() {
        return thrown;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getBody() {
        return body;
    }

    public JLocalVariable newLocalVariable(Class type, String name) {
        return bodyBlockStatement.newLocalVariable(type, name);
    }

    public JLocalVariable newLocalVariable(Class type, String name, JExpression initExpr) {
        return bodyBlockStatement.newLocalVariable(type, name, initExpr);
    }

    public void deleteLocalVariable(JLocalVariable f) {
        bodyBlockStatement.deleteLocalVariable(f);
    }

    public Vector getLocalVariables() {
        return bodyBlockStatement.getLocalVariables();
    }

    public void addStatement(JStatement s) {
        statements.add(s);
    }

    public Vector getStatements() {
        return statements;
    }
}
