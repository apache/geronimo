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

import java.lang.reflect.Method;
import java.util.HashMap;

import org.apache.geronimo.interop.generator.GenOptions;
import org.apache.geronimo.interop.generator.JParameter;
import org.apache.geronimo.interop.generator.JVariable;


public class Compiler {
    protected Class _riClass;
    protected GenOptions _genOptions;
    protected ClassLoader _cl;
    protected boolean _simpleIDL = false;

    protected static HashMap _readMethods;
    protected static HashMap _writeMethods;
    protected static HashMap _overloadTypes;

    static {
        _readMethods = new HashMap();
        _readMethods.put("boolean", "readBoolean");
        _readMethods.put("char", "readChar");
        _readMethods.put("byte", "readByte");
        _readMethods.put("short", "readShort");
        _readMethods.put("int", "readInt");
        _readMethods.put("long", "readLong");
        _readMethods.put("float", "readFloat");
        _readMethods.put("double", "readDouble");

        _writeMethods = new HashMap();
        _writeMethods.put("boolean", "writeBoolean");
        _writeMethods.put("char", "writeChar");
        _writeMethods.put("byte", "writeByte");
        _writeMethods.put("short", "writeShort");
        _writeMethods.put("int", "writeInt");
        _writeMethods.put("long", "writeLong");
        _writeMethods.put("float", "writeFloat");
        _writeMethods.put("double", "writeDouble");

        _overloadTypes = new HashMap();
        _overloadTypes.put("boolean", "boolean");
        _overloadTypes.put("byte", "octet");
        _overloadTypes.put("char", "wchar");
        _overloadTypes.put("double", "double");
        _overloadTypes.put("float", "float");
        _overloadTypes.put("int", "long");
        _overloadTypes.put("long", "long_long");
        _overloadTypes.put("short", "short");
        _overloadTypes.put("java.lang.Class", "javax_rmi_CORBA.ClassDesc");
        _overloadTypes.put("java.lang.String", "CORBA.WStringValue");
        _overloadTypes.put("org.omg.CORBA.Object", "Object");
        _overloadTypes.put("org.omg.CORBA.Any", "org_omg_boxedIDL_CORBA.Any");
        _overloadTypes.put("org.omg.CORBA.TypeCode", "org_omg_boxedIDL_CORBA.TypeCode");
    }

    public Compiler(Class remoteInterface) {
        this(remoteInterface, null);
    }

    public Compiler(Class riClass, GenOptions go) {
        _riClass = riClass;

        _cl = _riClass.getClassLoader();
        if (_cl == null) {
            _cl = ClassLoader.getSystemClassLoader();
        }

        if (go == null) {
            go = new GenOptions();
        }

        _genOptions = go;
    }

    //
    // Properties
    //

    public boolean isSimpleIDL() {
        return _simpleIDL;
    }

    public void setSimpleIDL(boolean simpleIDL) {
        _simpleIDL = simpleIDL;
    }

    public GenOptions getGenOptions() {
        return _genOptions;
    }

    public void setGenOptions(GenOptions genOptions) {
        _genOptions = genOptions;
    }

    public JParameter[] getMethodParms(Method m) {
        Class p[] = m.getParameterTypes();
        JParameter parms[] = null;

        if (p != null) {
            parms = new JParameter[p.length];

            int i;
            for (i = 0; i < p.length; i++) {
                parms[i] = new JParameter(p[i], "p" + i);
            }
        }

        return parms;
    }

    protected String getReadMethod(JVariable v) {
        String rc = null;

        if (v != null) {
            rc = (String) _readMethods.get(v.getTypeDecl());
        }

        return rc;
    }

    protected String getWriteMethod(JVariable v) {
        String rc = null;

        if (v != null) {
            rc = (String) _writeMethods.get(v.getTypeDecl());
        }

        return rc;
    }


}
