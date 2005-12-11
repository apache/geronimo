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
package org.apache.geronimo.interop.rmi.iiop.compiler;

import java.lang.reflect.Modifier;
import java.util.HashMap;

import org.apache.geronimo.interop.generator.JClass;
import org.apache.geronimo.interop.generator.JCodeStatement;
import org.apache.geronimo.interop.generator.JExpression;
import org.apache.geronimo.interop.generator.JField;
import org.apache.geronimo.interop.generator.JVariable;


public class ValueTypeContext {
    protected int _vTypeId = 0;
    protected HashMap _vTypeMap = new HashMap(20);

    public ValueTypeContext() {
        clear();
    }

    public void clear() {
        _vTypeId = 0;
        _vTypeMap.clear();
    }

    protected String getValueTypeVarName(JClass jc, JVariable jv) {
        String rc = null;

        rc = (String) _vTypeMap.get(jv.getTypeDecl());

        if (rc == null) {
            rc = "vt$" + _vTypeId++;
            _vTypeMap.put(jv.getTypeDecl(), rc);

            JField vtField = jc.newField(org.apache.geronimo.interop.rmi.iiop.ValueType.class, rc, new JExpression(new JCodeStatement("org.apache.geronimo.interop.rmi.iiop.ValueType.getInstance( " + jv.getTypeDecl() + ".class )")));
            vtField.setModifiers(Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL);
        }

        return rc;
    }
}
