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
    protected JReturnType _rt;
    protected JParameter _parms[];
    protected Class _thrown[];

    protected Vector _statements;
    protected JBlockStatement _bodyBlockStatement;

    protected String _body;  // Yuck

    protected JMethod(String name) {
        super(name, Modifier.PUBLIC);

        _statements = new Vector();
        _bodyBlockStatement = new JBlockStatement();
    }

    protected JMethod(JReturnType rt, String name, JParameter parms[], Class thrown[]) {
        this(name);

        setRT(rt);
        setParms(parms);
        setThrown(thrown);
    }

    public void setRT(JReturnType jt) {
        _rt = jt;
    }

    public JReturnType getRT() {
        return _rt;
    }

    public void setParms(JParameter parms[]) {
        _parms = parms;
    }

    public JParameter[] getParms() {
        return _parms;
    }

    public void setThrown(Class thrown[]) {
        _thrown = thrown;

        /*
        if (_thrown != null)
        {
            _thrownType = new String[_thrown.length];
            int i;
            for( i=0; i<_thrown.length; i++ )
            {
                _thrownType[i] = _thrown[i].getName();
            }
        }
        else
        {
            _thrownType = null;
        }
        */
    }

    public Class[] getThrown() {
        return _thrown;
    }

    /*
    public void setThrownType( String thrownType[] )
    {
        _thrownType = thrownType;
        _thrown = null;
    }

    public String[] getThrownType()
    {
        return _thrownType;
    }
    */

    public void setBody(String body) {
        _body = body;
    }

    public String getBody() {
        return _body;
    }

    public JLocalVariable newLocalVariable(Class type, String name) {
        return _bodyBlockStatement.newLocalVariable(type, name);
    }

    public JLocalVariable newLocalVariable(Class type, String name, JExpression initExpr) {
        return _bodyBlockStatement.newLocalVariable(type, name, initExpr);
    }

    public void deleteLocalVariable(JLocalVariable f) {
        _bodyBlockStatement.deleteLocalVariable(f);
    }

    public Vector getLocalVariables() {
        return _bodyBlockStatement.getLocalVariables();
    }

    public void addStatement(JStatement s) {
        _statements.add(s);
    }

    public Vector getStatements() {
        return _statements;
    }
}
