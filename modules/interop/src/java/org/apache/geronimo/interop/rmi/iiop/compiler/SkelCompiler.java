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
import java.lang.reflect.Modifier;

import org.apache.geronimo.interop.generator.GenOptions;
import org.apache.geronimo.interop.generator.JCaseStatement;
import org.apache.geronimo.interop.generator.JCatchStatement;
import org.apache.geronimo.interop.generator.JClass;
import org.apache.geronimo.interop.generator.JCodeStatement;
import org.apache.geronimo.interop.generator.JConstructor;
import org.apache.geronimo.interop.generator.JDeclareStatement;
import org.apache.geronimo.interop.generator.JExpression;
import org.apache.geronimo.interop.generator.JField;
import org.apache.geronimo.interop.generator.JLocalVariable;
import org.apache.geronimo.interop.generator.JMethod;
import org.apache.geronimo.interop.generator.JPackage;
import org.apache.geronimo.interop.generator.JParameter;
import org.apache.geronimo.interop.generator.JReturnType;
import org.apache.geronimo.interop.generator.JSwitchStatement;
import org.apache.geronimo.interop.generator.JTryCatchFinallyStatement;
import org.apache.geronimo.interop.generator.JTryStatement;
import org.apache.geronimo.interop.generator.JVariable;
import org.apache.geronimo.interop.generator.JavaGenerator;


public class SkelCompiler
        extends Compiler {
    protected ValueTypeContext _vtc = new ValueTypeContext();
    protected static JParameter _objInputVar = new JParameter(org.apache.geronimo.interop.rmi.iiop.ObjectInputStream.class, "input");
    protected static JParameter _objOutputVar = new JParameter(org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream.class, "output");

    public SkelCompiler(Class remoteInterface) {
        super(remoteInterface);
    }

    public SkelCompiler(Class remoteInterface, GenOptions go) {
        super(remoteInterface, go);
    }

    //
    // Methods
    //

    public void addMethodGetIds(JClass jc) {
        /*
        public String[] getIds()
        {
            return _ids;
        }
        */

        JMethod jm = jc.newMethod(new JReturnType(String.class, true),
                                  "getIds",
                                  (JParameter[]) null,
                                  (Class[]) null);

        jm.addStatement(new JCodeStatement("return _ids;"));
    }

    public void addMethodRegisterMethod(JClass jc) {
        /*
        public void registerMethod( String name, int id )
        {
            _methodMap.put( name, new Integer(id) );
        }
        */

        JMethod jm = jc.newMethod(new JReturnType(void.class),
                                  "registerMethod",
                                  new JParameter[]{new JParameter(String.class, "name"),
                                                   new JParameter(int.class, "id")},
                                  (Class[]) null);

        jm.addStatement(new JCodeStatement("_methods.put( name, new Integer(id) );"));
    }

    public void addMethodGetObjectRef(JClass jc, Class c) {
        /*
        public ObjectRef $getObjectRef()
        {
            ObjectRef or = new ObjectRef();
            or.$setID("RMI:mark.comps.Add:0000000000000000");
            or.$setObjectKey( "mark.comps.Add" );
            return or;
        }
        */

        JMethod jm = jc.newMethod(new JReturnType("ObjectRef"),
                                  "$getObjectRef",
                                  (JParameter[]) null,
                                  (Class[]) null);

        JLocalVariable jvor = jm.newLocalVariable(org.apache.geronimo.interop.rmi.iiop.ObjectRef.class, "or", new JExpression(new JCodeStatement("new ObjectRef()")));
        jm.addStatement(new JCodeStatement(jvor.getName() + ".$setID(\"RMI:" + c.getName() + ":0000000000000000\");"));
        jm.addStatement(new JCodeStatement(jvor.getName() + ".$setObjectKey(\"" + c.getName() + "\");"));
        jm.addStatement(new JCodeStatement("return " + jvor.getName() + ";"));
    }

    public void addMethodGetSkeleton(JClass jc) {
        /*
        public RemoteInterface $getSkeleton()
        {
            return this;
        }
        */

        JMethod jm = jc.newMethod(new JReturnType("RemoteInterface"),
                                  "$getSkeleton",
                                  (JParameter[]) null,
                                  (Class[]) null);

        jm.addStatement(new JCodeStatement("return this;"));
    }

    protected boolean throwsAnRMIRemoteException(Method m) {
        boolean rc = false;

        Class c[] = m.getExceptionTypes();
        int i;

        for (i = 0; i < c.length && !rc; i++) {
            rc = java.rmi.RemoteException.class.isAssignableFrom(c[i]);
        }

        return rc;
    }

    public void addMethod(Method m, JClass jc) {
        String invokeCall;
        String name = m.getName();
        JParameter[] sparms = getMethodParms(m);
        JParameter[] iparms = new JParameter[]{_objInputVar, _objOutputVar};

        if (!isSimpleIDL() && !throwsAnRMIRemoteException(m)) {
            error("Method " + m.getName() + " does not throw java.rmi.RemoteException or subclass, unable to generate its skeleton method.");
        }

        JMethod jm = jc.newMethod(new JReturnType(void.class), name, iparms, null);

        JVariable jrc = null;
        String rc = m.getReturnType().getName();
        if (rc != null && rc.length() > 0 && (!rc.equals("void"))) {
            jrc = jm.newLocalVariable(m.getReturnType(), "rc");
        }

        JTryCatchFinallyStatement tcfs = new JTryCatchFinallyStatement();
        JTryStatement ts = tcfs.getTryStatement();

        invokeCall = "_servant." + name + "(";

        if (sparms != null && sparms.length > 0) {
            int i;
            for (i = 0; i < sparms.length; i++) {
                String readMethod = getReadMethod(sparms[i]);
                JCodeStatement jcs = null;

                if (readMethod != null) {
                    // Primitive Type
                    // Cast not needed since each method returns the primitive datatype.

                    jcs = new JCodeStatement("input." + readMethod + "()");
                } else {
                    String vtVarName = _vtc.getValueTypeVarName(jc, sparms[i]);
                    if (vtVarName != null) {
                        jcs = new JCodeStatement("(" + sparms[i].getTypeDecl() + ") input.readObject( " + vtVarName + " )");
                    } else {
                        jcs = new JCodeStatement("// Code Gen Error: Class '" + sparms[i].getTypeDecl() + " is not a valid value type.");
                    }
                }

                ts.addStatement(new JDeclareStatement(sparms[i], new JExpression(jcs)));

                invokeCall += " " + sparms[i].getName();
                if (i + 1 < sparms.length) {
                    invokeCall += ",";
                }
            }
        }

        invokeCall += " )";

        if (jrc != null) {
            invokeCall = jrc.getName() + " = " + invokeCall;
        }

        invokeCall = invokeCall + ";";

        ts.addStatement(new JCodeStatement(invokeCall));

        JVariable jv = new JVariable(java.lang.Exception.class, "ex");
        JCatchStatement cs = tcfs.newCatch(jv);
        cs.addStatement(new JCodeStatement(jv.getName() + ".printStackTrace();"));

        jv = new JVariable(java.lang.Error.class, "er");
        cs = tcfs.newCatch(jv);
        cs.addStatement(new JCodeStatement(jv.getName() + ".printStackTrace();"));

        jm.addStatement(tcfs);

        if (jrc != null) {
            String writeMethod = getWriteMethod(jrc);
            JCodeStatement jcs = null;

            if (writeMethod != null) {
                // Primitive Type
                // Cast not needed since each method returns the primitive datatype.

                jcs = new JCodeStatement("output." + writeMethod + "( " + jrc.getName() + " );");
            } else {
                String vtVarName = _vtc.getValueTypeVarName(jc, jrc);
                if (vtVarName != null) {
                    jcs = new JCodeStatement("output.writeObject( " + vtVarName + ", " + jrc.getName() + " );");
                } else {
                    jcs = new JCodeStatement("// Code Gen Error: Class '" + jrc.getTypeDecl() + " is not a valid value type.");
                }
            }

            ts.addStatement(jcs);
        }
    }

    protected boolean isVariableAValueType(JVariable jv) {
        boolean rc = false;

        if (jv != null) {
            Class c = jv.getType();

            rc = isClassAValueType(c);
        }

        return rc;
    }

    protected boolean isClassAValueType(Class c) {
        boolean rc = false;

        if (c != null) {
            if (java.io.Serializable.class.isAssignableFrom(c)) {
                if (java.io.Externalizable.class.isAssignableFrom(c)) {
                    // Ok - but use the writeExternal and readExternal methods for serialization
                }

                if (!isClassARMIRemote(c)) {
                    if (Modifier.isStatic(c.getModifiers()) &&
                        c.getName().indexOf("$") != -1) {
                        // TODO: How do we determine the inner-classes contained class?
                        // Parse the <containedclass>$<innerclass> ?

                        //rc = isClassAValueType( c.getSuperclass() );
                        rc = true;
                    }

                    rc = true;
                } else {
                    error("Class: " + c.getName() + " is not proper value type as it is an instance of java.rmi.Remote or subclass.");
                }
            }
        }

        return rc;
    }

    protected boolean isClassARMIRemote(Class c) {
        boolean rc = false;

        if (c != null) {
            rc = java.rmi.Remote.class.isAssignableFrom(c);
        }

        return rc;
    }

    protected void error(String msg) {
        System.out.println("Error: " + msg);
    }

    protected void error(String msg, Throwable t) {
        error(msg);
        t.printStackTrace();
    }

    public void generate()
            throws Exception {
        _vtc.clear();

        if (!isSimpleIDL() && !isClassARMIRemote(_riClass)) {
            error("Class '" + _riClass.getName() + "' must be an instance of either java.rmi.Remote or of a subclass.");
        }

        ClassLoader cl = _riClass.getClassLoader();
        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }

        String fullGenDir = _genOptions.getGenDir();

        JavaGenerator jg = new JavaGenerator(_genOptions);

        String className = _riClass.getName();

        JPackage p = new JPackage("");
        if (_riClass.getPackage() != null) {
            p = new JPackage(_riClass.getPackage().getName());
            className = className.substring(className.lastIndexOf(".") + 1);
        }

        JClass jc = p.newClass(className + "_Skeleton");

        /*
        jw.comment( "" );
        jw.comment( "CORBA RMI-IIOP Skeleton Generator" );
        jw.comment( "  Interface: " + c.getName() );
        jw.comment( "  Date: " + (new Date(System.currentTimeMillis())).toString() );
        jw.comment( "" );
        */

        jc.addImport("org.apache.geronimo.interop.rmi.iiop", "ObjectInputStream");
        jc.addImport("org.apache.geronimo.interop.rmi.iiop", "ObjectOutputStream");
        jc.addImport("org.apache.geronimo.interop.rmi.iiop", "RemoteInterface");
        jc.addImport("org.apache.geronimo.interop.rmi.iiop", "RemoteInterface");
        jc.addImport("org.apache.geronimo.interop.rmi.iiop", "ObjectRef");
        jc.addImport("org.apache.geronimo.interop.rmi.iiop", "RemoteObject");
        jc.addImport("org.apache.geronimo.interop.rmi.iiop.server", "Adapter");
        jc.addImport("java.util", "HashMap");

        jc.setExtends("RemoteObject");
        jc.addImplements("RemoteInterface");

        JField idsField = jc.newField(String[].class, "_ids", new JExpression(new JCodeStatement("{ \"" + _riClass.getName() + "\", \"RMI:" + _riClass.getName() + ":0000000000000000\"}")), true);
        JField methodsField = jc.newField(java.util.HashMap.class, "_methods", new JExpression(new JCodeStatement("new HashMap(10)")));
        JField servantField = jc.newField(_riClass, "_servant", new JExpression(new JCodeStatement("null")));

        JConstructor jcCon = jc.newConstructor((JParameter[]) null, (Class[]) null);
        jcCon.addStatement(new JCodeStatement("super();"));

        addMethodRegisterMethod(jc);
        addMethodGetIds(jc);
        addMethodGetSkeleton(jc);
        addMethodGetObjectRef(jc, _riClass);

        JMethod jmInvoke = jc.newMethod(new JReturnType(void.class),
                                        "$invoke",
                                        new JParameter[]{new JParameter(String.class, "methodName"),
                                                         new JParameter(byte[].class, "objectKey"),
                                                         new JParameter(Object.class, "instance"),
                                                         _objInputVar,
                                                         _objOutputVar},
                                        (Class[]) null);

        jmInvoke.setModifier(Modifier.PUBLIC, true);

        JLocalVariable jvM = jmInvoke.newLocalVariable(Integer.class, "m", new JExpression(new JCodeStatement("(Integer)_methods.get(methodName)")));

        jmInvoke.addStatement(new JCodeStatement("if (m == null)"));
        jmInvoke.addStatement(new JCodeStatement("{"));
        jmInvoke.addStatement(new JCodeStatement("    throw new org.omg.CORBA.BAD_OPERATION(methodName);"));
        jmInvoke.addStatement(new JCodeStatement("}"));
        jmInvoke.addStatement(new JCodeStatement(""));
        jmInvoke.addStatement(new JCodeStatement("_servant = (" + _riClass.getName() + ")instance;"));
        jmInvoke.addStatement(new JCodeStatement(""));
        jmInvoke.addStatement(new JCodeStatement("if (m.intValue() < 0)"));
        jmInvoke.addStatement(new JCodeStatement("{"));
        jmInvoke.addStatement(new JCodeStatement("    super.invoke( m.intValue(), objectKey, instance, input, output );"));
        jmInvoke.addStatement(new JCodeStatement("}"));
        jmInvoke.addStatement(new JCodeStatement(""));

        JSwitchStatement ss = new JSwitchStatement(new JExpression(new JCodeStatement("m.intValue()")));
        JCaseStatement cs = null;
        jmInvoke.addStatement(ss);

        Method m[] = null;

        if (isSimpleIDL()) {
            m = _riClass.getMethods();
        } else {
            m = _riClass.getDeclaredMethods();
        }

        if (m != null && m.length > 0) {
            int i;
            for (i = 0; i < m.length; i++) {
                // Enter a new method id in the _methods hashtable.
                jcCon.addStatement(new JCodeStatement("registerMethod( \"" + m[i].getName() + "\", " + i + ");"));

                // Add a new case statement to the invoke swtich
                cs = ss.newCase(new JExpression(new JCodeStatement("" + i)));
                cs.addStatement(new JCodeStatement(m[i].getName() + "(input,output);"));

                // Generate the method wrapper
                addMethod(m[i], jc);
            }
        }

        jg.generate(p);
    }

    public void compile()
            throws Exception {
    }

    public Class getSkelClass() {
        Class c = null;

        try {
            //generate();
            compile();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return c;
    }

    public static void main(String args[])
            throws Exception {
        boolean generate = false;
        boolean compile = false;
        boolean simpleIDL = false;
        String ri = "";
        GenOptions go = new GenOptions();

        go.setGenDir("./src");
        go.setOverwrite(false);
        go.setVerbose(false);

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-g")) {
                generate = true;
            } else if (args[i].equals("-c")) {
                compile = true;
            } else if (args[i].equals("-d") && ((i + 1) < args.length)) {
                go.setGenDir(args[++i]);
            } else if (args[i].equals("-v")) {
                go.setVerbose(true);
            } else if (args[i].equals("-o")) {
                go.setOverwrite(true);
            } else if (args[i].equals("-s")) {
                simpleIDL = true;
            } else if (args[i].startsWith("-")) {
                System.out.println("Warning: Ignoring unrecognized options: '" + args[i] + "'");
            } else {
                ri = args[i];
            }
        }

        Class riClass = Class.forName(ri);

        SkelCompiler sg = new SkelCompiler(riClass, go);

        sg.setSimpleIDL(simpleIDL);

        if (generate) {
            if (go.isVerbose()) {
                System.out.println("Generating: " + ri);
            }

            sg.generate();
        }

        if (compile) {
            if (go.isVerbose()) {
                System.out.println("Compiling: " + ri);
            }

            sg.compile();
        }

        // sg.setSimpleIDL( true );
        // sg.generate( "org.apache.geronimo.interop.rmi.iiop.NameServiceOperations");
    }
}

