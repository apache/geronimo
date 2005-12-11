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
import java.io.File;
import java.util.*;

import org.apache.geronimo.interop.generator.*;
import org.apache.geronimo.interop.util.JavaClass;
import org.apache.geronimo.interop.util.ProcessUtil;
import org.apache.geronimo.interop.adapter.Adapter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SkelCompiler extends Compiler {
    private final Log log = LogFactory.getLog(SkelCompiler.class);

    private ValueTypeContext      vtc = new ValueTypeContext();

    private static JParameter     objInputVar = new JParameter(org.apache.geronimo.interop.rmi.iiop.ObjectInputStream.class, "input");
    private static JParameter     objOutputVar = new JParameter(org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream.class, "output");

    private String                  inStreamName = "getInputStream";
    private String                  outStreamName = "getOutputStream";

    private HashMap                 packages = new HashMap();

    public SkelCompiler(GenOptions go, ClassLoader cl) {
        super(go, cl);
    }

    public void addMethodGetIds(JClass jc) {
        //
        // Method Template:
        //
        // public String[] getIds()
        // {
        //     return _ids;
        // }
        //

        JMethod jm = jc.newMethod(new JReturnType(String[].class),
                                  "getIds",
                                  (JParameter[]) null,
                                  (Class[]) null);

        jm.addStatement(new JCodeStatement("return _ids;"));
    }

    public void addMethodRegisterMethod(JClass jc) {
        //
        // Method Template:
        //
        // public void registerMethod( String name, int id )
        // {
        //     _methodMap.put( name, new Integer(id) );
        // }
        //

        JMethod jm = jc.newMethod(new JReturnType(void.class),
                                  "registerMethod",
                                  new JParameter[]{new JParameter(String.class, "name"),
                                                   new JParameter(int.class, "id")},
                                  (Class[]) null);

        jm.addStatement(new JCodeStatement("_methods.put( name, new Integer(id) );"));
    }

    public void addMethodGetObjectRef(JClass jc, Class c) {
        //
        // Method Template:
        //
        //  public ObjectRef getObjectRef()
        // {
        //     ObjectRef or = new ObjectRef();
        //     or.$setID("RMI:mark.comps.Add:0000000000000000");
        //     or.$setObjectKey( "mark.comps.Add" );
        //     return or;
        // }
        //

        JMethod jm = jc.newMethod(new JReturnType(org.apache.geronimo.interop.rmi.iiop.ObjectRef.class),
                                  "getObjectRef",
                                  (JParameter[]) null,
                                  (Class[]) null);

        JLocalVariable jvor = jm.newLocalVariable(org.apache.geronimo.interop.rmi.iiop.ObjectRef.class, "or", new JExpression(new JCodeStatement("new ObjectRef()")));
        jm.addStatement(new JCodeStatement(jvor.getName() + ".$setID(\"RMI:" + c.getName() + ":0000000000000000\");"));
        jm.addStatement(new JCodeStatement(jvor.getName() + ".$setObjectKey(\"" + c.getName() + "\");"));
        jm.addStatement(new JCodeStatement("return " + jvor.getName() + ";"));
    }

    public void addMethodGetSkeleton(JClass jc) {
        //
        // Method Template
        //
        // public RemoteInterface $getSkeleton()
        // {
        //     return this;
        // }
        //

        JMethod jm = jc.newMethod(new JReturnType(org.apache.geronimo.interop.rmi.iiop.RemoteInterface.class),
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

    public void addMethod(MethodOverload mo, JClass jc, GenOptions go) {
        String invokeCall;
        Method m = mo.method;
        String name = m.getName();
        JParameter[] sparms = getMethodParms(m);
        JParameter[] iparms = new JParameter[]{objInputVar, objOutputVar};
        String vtVarName = null;
        JCodeStatement codeStmt = null;

        if (!go.isSimpleIdl() && !throwsAnRMIRemoteException(m)) {
            error("Method " + m.getName() + " does not throw java.rmi.RemoteException or subclass, unable to generate its skeleton method.");
        }

        JMethod jm = jc.newMethod(new JReturnType(void.class), mo.iiop_name, iparms, null);

        JVariable jrc = null;
        String rc = m.getReturnType().getName();
        if (rc != null && rc.length() > 0 && (!rc.equals("void"))) {
            jrc = jm.newLocalVariable(m.getReturnType(), "rc");
        }

        ArrayList   declareStatementList = new ArrayList( 20 );
        JStatement  invokeStatement = null;

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
                    vtVarName = vtc.getValueTypeVarName(jc, sparms[i]);
                    if (vtVarName != null) {
                        jcs = new JCodeStatement("(" + sparms[i].getTypeDecl() + ") input.readObject( " + vtVarName + " )");
                    } else {
                        jcs = new JCodeStatement("// Code Gen Error: Class '" + sparms[i].getTypeDecl() + " is not a valid value type.");
                    }
                }

                declareStatementList.add(new JDeclareStatement(sparms[i], new JExpression(jcs)));

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

        invokeStatement = new JCodeStatement(invokeCall);

        JStatement writeResultStatement = null;
        if (jrc != null) {
            String writeMethod = getWriteMethod(jrc);
            codeStmt = null;

            if (writeMethod != null) {
                // Primitive Type
                // Cast not needed since each method returns the primitive datatype.

                codeStmt = new JCodeStatement("output." + writeMethod + "( " + jrc.getName() + " );");
            } else {
                vtVarName = vtc.getValueTypeVarName(jc, jrc);
                if (vtVarName != null) {
                    codeStmt = new JCodeStatement("output.writeObject( " + vtVarName + ", " + jrc.getName() + " );");
                } else {
                    codeStmt = new JCodeStatement("// Code Gen Error: Class '" + jrc.getTypeDecl() + " is not a valid value type.");
                }
            }

            writeResultStatement = codeStmt;
        }

        //
        // The exception handling block:
        //
        //            try
        //            {
        //                invoke method()
        //            }
        //            catch (java.lang.Exception $ex_1)
        //            {
        //                Listed here are the individual catches that the method can throw
        //                if ($ex_1 instanceof org.apache.geronimo.interop.CosNaming.NamingContextPackage.NotFound)
        //                {
        //                    $output.writeException(type$4, $ex_1);
        //                    return;
        //                }
        //                if ($ex_1 instanceof org.apache.geronimo.interop.CosNaming.NamingContextPackage.CannotProceed)
        //                {
        //                    $output.writeException(type$5, $ex_1);
        //                    return;
        //                }
        //                throw $ex_1;
        //            }

        Class[] excepts = m.getExceptionTypes();
        JVariable jvExcept = null;
        JVariable jvTmp = null;

        JCatchStatement catchStmt = null;

        if (excepts != null && excepts.length > 0)
        {
            JTryCatchFinallyStatement tcfs = new JTryCatchFinallyStatement();
            JTryStatement ts = tcfs.getTryStatement();

            if (declareStatementList.size() > 0)
            {
                for( int i=0; i<declareStatementList.size(); i++ )
                {
                    ts.addStatement( (JStatement)declareStatementList.get(i) );
                }
            }

            ts.addStatement( invokeStatement );

            jvExcept = new JVariable(java.lang.Exception.class, "ex");
            catchStmt = tcfs.newCatch(jvExcept);

            for( int i=0; excepts != null && i < excepts.length; i++ )
            {
                jvTmp = new JVariable( excepts[i], "exvar" );
                vtVarName = vtc.getValueTypeVarName(jc, jvTmp);
                codeStmt = null;
                if (vtVarName != null) {
                    codeStmt = new JCodeStatement("output.writeException( " + vtVarName + ", " + jvExcept.getName() + ");" );
                } else {
                    codeStmt = new JCodeStatement("// Code Gen Error: Class '" + sparms[i].getTypeDecl() + " is not a valid value type.");
                }

                JIfStatement ifs = new JIfStatement( new JExpression(
                        new JCodeStatement( jvExcept.getName() + " instanceof " + excepts[i].getName() ) ));
                ifs.addStatement( codeStmt );
                ifs.addStatement( new JCodeStatement( "return;" ));
                catchStmt.addStatement( ifs );
            }

            if (writeResultStatement != null)
            {
                ts.addStatement( writeResultStatement );
            }

            jm.addStatement(tcfs);
        }
        else
        {
            if (declareStatementList.size() > 0)
            {
                for( int i=0; i<declareStatementList.size(); i++ )
                {
                    jm.addStatement( (JStatement)declareStatementList.get(i) );
                }
            }

            jm.addStatement( invokeStatement );

            if (writeResultStatement != null)
            {
                jm.addStatement( writeResultStatement );
            }
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

    public void generate() throws GenException {

        GenOptions go = getGenOptions();
        List interfaces = go.getInterfaces();
        Iterator intf = null;

        if (interfaces != null) {
            intf = interfaces.iterator();
        }

        JavaGenerator jg = new JavaGenerator(genOptions);

        if (go.isSimpleIdl()) {
            inStreamName = "getSimpleInputStream";
            outStreamName = "getSimpleOutputStream";
        } else {
            inStreamName = "getInputStream";
            outStreamName = "getOutputStream";
        }

        String riClassName = "";
        Class  riClass = null;
        String skelClassName = "";
        String pkgName = "";
        JPackage pkg = null;

        while (intf != null && intf.hasNext() ) {
            // Clear the value type cache.
            vtc.clear();

            riClassName = (String)intf.next();


            try {
                riClass = getClassLoader().loadClass( riClassName );
            } catch (Exception ex) {
                throw new GenException( "Generate Skels Failed:", ex );
            }

            if (!go.isSimpleIdl() && !isClassARMIRemote(riClass)) {
                error("Class '" + riClass.getName() + "' must be an instance of either java.rmi.Remote or of a subclass.");
            }

            pkgName = JavaClass.getNamePrefix(riClassName);
            skelClassName = JavaClass.getNameSuffix(riClassName);
            pkg = (JPackage) packages.get( pkgName );
            if (pkg == null)
            {
                pkg = new JPackage( pkgName );
                packages.put( pkgName, pkg );
            }

            JClass jc = pkg.newClass(skelClassName + "_Skeleton");

            jc.addImport("org.apache.geronimo.interop.rmi.iiop", "RemoteInterface");
            jc.addImport("org.apache.geronimo.interop.rmi.iiop", "ObjectRef");
            jc.addImport("org.apache.geronimo.interop.rmi.iiop", "RemoteObject");

            jc.setExtends("RemoteObject");
            jc.addImplements("RemoteInterface");

            JField idsField = jc.newField(String[].class, "_ids", new JExpression(new JCodeStatement("{ \"" + riClass.getName() + "\", \"RMI:" + riClass.getName() + ":0000000000000000\"}")), true);
            idsField.setModifiers(Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL);

            JField servantField = jc.newField(riClass, "_servant", new JExpression(new JCodeStatement("null")));
            servantField.setModifiers(Modifier.PRIVATE);

            JConstructor jcCon = jc.newConstructor((JParameter[]) null, (Class[]) null);
            jcCon.addStatement(new JCodeStatement("super();"));

            // Replaced with the method in the RemoteObject parent class
            //addMethodRegisterMethod(jc);

            addMethodGetIds(jc);

            // Not used anymore
            //addMethodGetSkeleton(jc);

            addMethodGetObjectRef(jc, riClass);

            JMethod jmInvoke = jc.newMethod(new JReturnType(void.class),
                                            "invoke",
                                            new JParameter[]{new JParameter(String.class, "methodName"),
                                                             new JParameter(byte[].class, "objectKey"),
                                                             new JParameter(Adapter.class, "adapter"),
                                                             objInputVar,
                                                             objOutputVar},
                                            (Class[]) null);

            jmInvoke.setModifier(Modifier.PUBLIC);

            JLocalVariable jvM = jmInvoke.newLocalVariable(Integer.class, "m", new JExpression(new JCodeStatement("getMethodId(methodName); // (Integer)_methods.get(methodName)")));

            JIfStatement jis = new JIfStatement(new JExpression(new JCodeStatement(jvM.getName() + " == null")));
            jis.addStatement(new JCodeStatement("throw new org.omg.CORBA.BAD_OPERATION(methodName);"));
            jmInvoke.addStatement(jis);

            jmInvoke.addStatement(new JCodeStatement("_servant = (" + riClass.getName() + ")adapter.getServant(); //instance;"));

            JIfStatement jis2 = new JIfStatement(new JExpression(new JCodeStatement(jvM.getName() + ".intValue() < 0")));
            jis2.addStatement(new JCodeStatement("super.invoke( " + jvM.getName() + ".intValue(), objectKey, adapter, input, output );"));
            jmInvoke.addStatement(jis2);

            JTryCatchFinallyStatement tcfs = new JTryCatchFinallyStatement();
            JTryStatement ts = tcfs.getTryStatement();

            JSwitchStatement switchStmt = new JSwitchStatement(new JExpression(new JCodeStatement("m.intValue()")));
            JCaseStatement caseStmt = null;
            ts.addStatement(switchStmt);

            Method m[] = getMethods( riClass, go.isSimpleIdl());
            MethodOverload mo[] = null;
            mo = getMethodOverloads( m );

            for (int i = 0; mo != null && i < mo.length; i++)
            {
                // Enter a new method id in the _methods hashtable.
                jcCon.addStatement(new JCodeStatement("registerMethod( \"" + mo[i].iiop_name + "\", " + i + ");"));

                // Add a new case statement to the invoke swtich
                caseStmt = switchStmt.newCase(new JExpression(new JCodeStatement("" + i)));
                caseStmt.addStatement(new JCodeStatement(mo[i].iiop_name + "(input,output);"));

                // Generate the method wrapper
                addMethod(mo[i], jc, go);
            }

            JCatchStatement catchStmt = null;
            JVariable jvExcept = null;

            jvExcept = new JVariable(java.lang.Error.class, "erEx");
            catchStmt = tcfs.newCatch(jvExcept);
            catchStmt.addStatement(new JCodeStatement( "throw new org.apache.geronimo.interop.SystemException( " + jvExcept.getName() + " );" ) );

            jvExcept = new JVariable(java.lang.RuntimeException.class, "rtEx");
            catchStmt = tcfs.newCatch(jvExcept);
            catchStmt.addStatement(new JCodeStatement( "throw " + jvExcept.getName() + ";" ) );

            jvExcept = new JVariable(java.lang.Exception.class, "exEx");
            catchStmt = tcfs.newCatch(jvExcept);
            catchStmt.addStatement(new JCodeStatement( "throw new org.apache.geronimo.interop.SystemException( " + jvExcept.getName() + " );" ) );

            jmInvoke.addStatement( tcfs );
        }

        Set pkgSet = packages.keySet();
        Iterator pkgIt = pkgSet.iterator();
        String skelPkg = "";

        while (pkgIt.hasNext())
        {
            skelPkg = (String) pkgIt.next();
            pkg = (JPackage)packages.get(skelPkg);
            System.out.println("Generating Package: " + skelPkg);
            jg.generate(pkg);
        }
    }

    public void compile()
            throws Exception {

        Set pkg = packages.keySet();
        Iterator pkgIt = pkg.iterator();
        String skelPkg = "";

        /*
         * Each of the packages were generated under go.getGenSrcDir().
         *
         * Go through all the packages and run the compiler on *.java
         */

        GenOptions  go = getGenOptions();
        String classpath = adjustPath(go.getClasspath());
        String srcpath = adjustPath(go.getGenSrcDir());

        String filesToCompile = "";
        String javacCmd = "";

        while (pkgIt.hasNext())
        {
            skelPkg = (String) pkgIt.next();
            skelPkg = skelPkg.replace( '.', File.separatorChar );
            filesToCompile = adjustPath(go.getGenSrcDir() + File.separator + skelPkg + File.separator + "*.java");

            System.out.println("Compiling Package: " + filesToCompile);

            javacCmd = "javac -d " + go.getGenClassDir() +
                            ( go.isCompileDebug() ? " -g" : "" ) +
                            " -classpath " + classpath + " " +
                            " -sourcepath " + srcpath + " " + filesToCompile;

            System.out.println( "Lauching: " + javacCmd );

            ProcessUtil pu = ProcessUtil.getInstance();
            pu.setEcho(System.out);
            pu.run(javacCmd, (String[]) null, "./" );
        }
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

    public static void main(String args[]) throws Exception {
        GenOptions go = null;

        try
        {
            go = new GenOptions( "./skels", args );
        }
        catch( GenWarning gw )
        {
            gw.printStackTrace();
        }

        ClassLoader cl = ClassLoader.getSystemClassLoader();
        SkelCompiler sg = new SkelCompiler( go, cl );

        if (go.isGenerate()) {
            sg.generate();
        }

        if (go.isCompile()) {
            sg.compile();
        }
    }
}
