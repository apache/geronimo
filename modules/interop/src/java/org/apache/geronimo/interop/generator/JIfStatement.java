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

public class JIfStatement extends JBlockStatement {
    private JExpression   expr;

    public JIfStatement(JExpression e) {
        super();
        expr = e;
    }

    public JExpression getExpression() {
        return expr;
    }

    public int hashCode() {
        return expr.hashCode();
    }

    public boolean equals(Object other) {
        boolean rc = false;

        if (this == other) {
            rc = true;
        } else if (other instanceof JIfStatement) {
            JIfStatement is = (JIfStatement) other;

            if (is.expr == expr) {
                // Todo: Need equals()
                rc = true;
            }
        }

        return rc;
    }
}
