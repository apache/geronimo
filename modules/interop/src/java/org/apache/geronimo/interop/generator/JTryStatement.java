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

public class JTryStatement extends JBlockStatement {
    public JTryStatement() {
        super();
    }
}

/*
public class JTryStatement extends JStatement
{
    protected JBlockStatement    _tryStatements;

    public JTryStatement( )
    {
        _tryStatements = new JBlockStatement();
    }

    public void addStatement( JStatement s )
    {
        if (s == _tryStatements ||
            s.equals( _tryStatements ))
        {
            // Don't add it.
            // Todo: Throw an exception?
            return;
        }

        _tryStatements.addStatement( s );
    }

    public JBlockStatement getStatement()
    {
        return _tryStatements;
    }
}
*/