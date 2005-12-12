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
import java.util.*;
import java.io.File;

import org.apache.geronimo.interop.generator.GenOptions;
import org.apache.geronimo.interop.generator.JParameter;
import org.apache.geronimo.interop.generator.JVariable;

public class Compiler {
    protected GenOptions      genOptions;

    private ClassLoader       classLoader;

    private static HashMap    readMethods;
    private static HashMap    writeMethods;
    private static HashMap    overloadTypes;

    static {
        readMethods = new HashMap();
        readMethods.put("boolean", "readBoolean");
        readMethods.put("char", "readChar");
        readMethods.put("byte", "readByte");
        readMethods.put("short", "readShort");
        readMethods.put("int", "readInt");
        readMethods.put("long", "readLong");
        readMethods.put("float", "readFloat");
        readMethods.put("double", "readDouble");

        writeMethods = new HashMap();
        writeMethods.put("boolean", "writeBoolean");
        writeMethods.put("char", "writeChar");
        writeMethods.put("byte", "writeByte");
        writeMethods.put("short", "writeShort");
        writeMethods.put("int", "writeInt");
        writeMethods.put("long", "writeLong");
        writeMethods.put("float", "writeFloat");
        writeMethods.put("double", "writeDouble");

        overloadTypes = new HashMap();
        overloadTypes.put("boolean", "boolean");
        overloadTypes.put("byte", "octet");
        overloadTypes.put("char", "wchar");
        overloadTypes.put("double", "double");
        overloadTypes.put("float", "float");
        overloadTypes.put("int", "long");
        overloadTypes.put("long", "long_long");
        overloadTypes.put("short", "short");
        overloadTypes.put("java.lang.Class", "javax_rmi_CORBA.ClassDesc");
        overloadTypes.put("java.lang.String", "CORBA.WStringValue");
        overloadTypes.put("org.omg.CORBA.Object", "Object");
        overloadTypes.put("org.omg.CORBA.Any", "org_omg_boxedIDL_CORBA.Any");
        overloadTypes.put("org.omg.CORBA.TypeCode", "org_omg_boxedIDL_CORBA.TypeCode");
    }

    public Compiler(GenOptions go, ClassLoader cl) {
        classLoader = cl;
        if (classLoader == null) {
            classLoader = ClassLoader.getSystemClassLoader();
        }

        genOptions = go;
    }

    public GenOptions getGenOptions() {
        return genOptions;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
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
            rc = (String) readMethods.get(v.getTypeDecl());
        }

        return rc;
    }

    protected String getWriteMethod(JVariable v) {
        String rc = null;

        if (v != null) {
            rc = (String) writeMethods.get(v.getTypeDecl());
        }

        return rc;
    }

    protected static void error( String errMsg ) {
        System.err.println( "Error: " + errMsg );
        System.exit(1);
    }

    protected void error(String msg, Throwable t) {
        error(msg);
        t.printStackTrace();
    }

    protected static void warn( String warnMsg ) {
        System.out.println( "Warning: " + warnMsg );
    }

    protected String adjustPath( String path )
    {
        // Maybe it would be easier if GenOptions just made sure that platform path
        // separators and file separators were as required on the platform?

        if (File.separatorChar == '/') {
            // We're under Unix, change '\\' to '/'
            return path.replace( '\\', '/' );
        } else {
            // We're under Windows, change '/' to '\\'
            return path.replace( '/', '\\' );
        }
    }

    protected void addMethodsToList( ArrayList list, Method[] methods )
    {
        for(int i=0; list != null && methods != null && i < methods.length; i++ )
        {
            list.add( methods[i] );
        }
    }

    protected void collectInterfaceMethods( ArrayList list, Class intfClass, boolean simpleIdl )
    {
        Method myMethods[] = intfClass.getDeclaredMethods();

        if (!simpleIdl)
        {
            addMethodsToList( list, myMethods );
        }

        Class myInterfaces[] = intfClass.getInterfaces();
        if (myInterfaces != null && myInterfaces.length > 0)
        {
            String opsName = intfClass.getName() + "Operations";

            for (int i = 0; i < myInterfaces.length; i++)
            {
                if (simpleIdl)
                {
                    // add interface and its Operations, only if there is a coresponding Operations
                    if (myInterfaces[i].getName().equals(opsName))
                    {
                        addMethodsToList( list, myMethods );
                        addMethodsToList( list, myInterfaces[i].getDeclaredMethods() );
                        continue;
                    }
                    else
                    {
                        collectInterfaceMethods( list, myInterfaces[i], simpleIdl );
                    }
                }
                else
                {
                    // Collect the interface methods for all interfaces ..
                    collectInterfaceMethods( list, myInterfaces[i], simpleIdl );
                }
            }
        }
    }

    protected Method[] getMethods(Class intfClass, boolean isSimpleIdl)
    {
        Method myMethods[] = intfClass.getDeclaredMethods();
        ArrayList list = new ArrayList( myMethods.length * 2 );

        collectInterfaceMethods( list, intfClass, isSimpleIdl );

        Object[] objs = list.toArray();
        Method[] methods = new Method[objs.length];
        System.arraycopy( objs, 0, methods, 0, objs.length );

        return methods;
    }

    public MethodOverload[] getMethodOverloads(Class intfCalss, boolean isSimpleIdl) {
        Method[] methods = getMethods(intfCalss, isSimpleIdl);
        MethodOverload[] methodOverloads = getMethodOverloads(methods);
        return methodOverloads;
    }

    public MethodOverload[] getMethodOverloads( Method methods[] )
    {
        HashMap hm = new HashMap( methods.length );

        // Put all the methods into the hashmap
        for( int i=0; methods != null && i < methods.length; i++ )
        {
            ArrayList al = (ArrayList)hm.get( methods[i].getName() );
            if (al == null)
            {
                al = new ArrayList( methods.length );
                al.add( methods[i] );
                hm.put( methods[i].getName(), al );
            }
            else
            {
                al.add( methods[i] );
            }
        }

        Set keySet = hm.keySet();
        ArrayList overloadList = new ArrayList( methods.length );
        for (Iterator keyIt = keySet.iterator(); keyIt != null && keyIt.hasNext(); )
        {
            ArrayList al = (ArrayList)hm.get( keyIt.next() );
            if (al.size() == 1)
            {
                Method m = (Method)al.remove(0);
                overloadList.add( new MethodOverload( m.getName(), m ) );
            }
            else
            {
                for( int i=0; i<=al.size(); i++ )
                {
                    Method m = (Method)al.remove(0);
                    overloadList.add( new MethodOverload( overloadMethodName(m), m ) );
                }
            }
        }

        Object obj[] = overloadList.toArray();
        MethodOverload m[] = new MethodOverload[ obj.length ];
        System.arraycopy( obj, 0, m, 0, obj.length );

        return m;
    }

    protected String overloadMethodName( Method m )
    {
        Class parms[] = m.getParameterTypes();
        String name = m.getName() + "_";
        for( int i=0; i<parms.length; i++ )
        {
            name += "_" + parms[i].getName();
        }
        return name.replace( '.', '_' );
    }

    public class MethodOverload
    {
        public Method method;
        public String iiop_name;

        public MethodOverload( String iiop_name, Method method )
        {
            this.method = method;
            this.iiop_name = iiop_name;
        }

        public int hashCode()
        {
            return iiop_name.hashCode();
        }

        public boolean equals( Object other )
        {
            if (other instanceof MethodOverload)
            {
                MethodOverload mother = (MethodOverload)other;
                if (iiop_name != null)
                {
                    return iiop_name.equals( mother.iiop_name );
                }
                else
                {
                    return iiop_name == mother.iiop_name;
                }
            }

            return false;
        }
    }
}
