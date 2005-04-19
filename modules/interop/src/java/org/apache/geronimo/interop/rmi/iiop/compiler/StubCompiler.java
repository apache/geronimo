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
import java.io.File;
import java.util.*;

import org.apache.geronimo.interop.generator.*;
import org.apache.geronimo.interop.util.JavaClass;
import org.apache.geronimo.interop.util.ProcessUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StubCompiler extends Compiler {
    private final Log log = LogFactory.getLog(StubCompiler.class);

    private ValueTypeContext        vtc = new ValueTypeContext();

    private String                  inStreamName = "getInputStream";
    private String                  outStreamName = "getOutputStream";

    private HashMap                 packages = new HashMap();

    public StubCompiler(GenOptions go, ClassLoader cl) {
        super(go, cl);
    }

    protected void addMethod(JClass jc, String iiopMethodName, JReturnType jrc,
                             String name, JParameter[] jparms, Class[] excepts) {
        //
        // Method Template:
        //
        // java.lang.Object $key_1 = $getRequestKey();
        // for (int $retry = 0; ; $retry++)
        // {
        //     try
        //     {
        //         org.apache.geronimo.interop.rmi.iiop.client.Connection $connection_2 = this.$connect();
        //         org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream $output_3 = $connection_2.getSimpleOutputStream();  // simple idl
        //         org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream $output_3 = $connection_2.getOutputStream();        // rmi-iiop
        //         $output_3.writeObject(type$1, p1);
        //         $connection_2.invoke(this, "_is_a", $key_1, $retry);
        //         org.apache.geronimo.interop.rmi.iiop.ObjectInputStream $input_4 = $connection_2.getSimpleInputStream();     // simple idl
        //         org.apache.geronimo.interop.rmi.iiop.ObjectInputStream $input_4 = $connection_2.getInputStream();           // rmi-iiop
        //         $connection_2.forget($key_1);
        //         $connection_2.close();
        //         java.lang.String $et_5 = $connection_2.getExceptionType();
        //         if ($et_5 != null)
        //         {
        //             throw org.apache.geronimo.interop.rmi.iiop.SystemExceptionFactory.getException($connection_2.getException());
        //         }
        //         boolean $djc_result;
        //         $djc_result = $input_4.readBoolean();
        //         return $djc_result;
        //     }
        //     catch (org.apache.geronimo.interop.rmi.iiop.client.RetryInvokeException $ex_6)
        //     {
        //         if ($retry == 3)
        //         {
        //             throw $ex_6.getRuntimeException();
        //         }
        //     }
        // }

        //JParameter jpID = new JParameter( java.lang.String.class, "id" );
        JMethod jm = jc.newMethod(jrc, name, jparms, excepts);

        JLocalVariable jlvKey = jm.newLocalVariable(Object.class, "$key", new JExpression(new JCodeStatement("$getRequestKey()")));
        JLocalVariable jlvRetry = jm.newLocalVariable(int.class, "$retry");

        JForStatement jfs = new JForStatement(new JCodeStatement(jlvRetry.getName() + " = 0"),
                                              new JExpression(new JCodeStatement(" ; ")),
                                              new JCodeStatement(jlvRetry.getName() + "++"));

        jm.addStatement(jfs);

        JTryCatchFinallyStatement tcfs = new JTryCatchFinallyStatement();
        JTryStatement ts = tcfs.getTryStatement();

        JBlockStatement jbs = (JBlockStatement) ts;

        JLocalVariable jlvConn = jbs.newLocalVariable(org.apache.geronimo.interop.rmi.iiop.client.Connection.class, "$conn");
        JLocalVariable jlvOutput = jbs.newLocalVariable(org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream.class, "$out");
        JLocalVariable jlvEt = jbs.newLocalVariable(java.lang.String.class, "$et");
        JLocalVariable jlvInput = null;

        JLocalVariable jlvRc = null;
        if (jrc != null && jrc.getType() != void.class) {
            jlvRc = jbs.newLocalVariable(jrc.getType(), "$rc");
            jlvInput = jbs.newLocalVariable(org.apache.geronimo.interop.rmi.iiop.ObjectInputStream.class, "$in");
        }

        jbs.addStatement(new JCodeStatement(jlvConn.getName() + " = this.$connect();"));
        jbs.addStatement(new JCodeStatement(jlvOutput.getName() + " = " + jlvConn.getName() + "." + outStreamName + "();"));

        String writeMethod = null;
        String writeCall = "";
        for (int i = 0; i < jparms.length; i++) {
            writeMethod = getWriteMethod(jparms[i]);

            if (writeMethod != null) {
                writeCall = writeMethod + "( " + jparms[i].getName() + " )";
            } else {
                writeCall = "writeObject( " + vtc.getValueTypeVarName(jc, jparms[i]) + ", " + jparms[i].getName() + ")";
            }

            jbs.addStatement(new JCodeStatement(jlvOutput.getName() + "." + writeCall + ";"));
        }

        jbs.addStatement(new JCodeStatement(jlvConn.getName() + ".invoke(this, \"" + iiopMethodName + "\", " + jlvKey.getName() + ", $retry);"));
        if (jlvRc != null) {
            jbs.addStatement(new JCodeStatement(jlvInput.getName() + " = " + jlvConn.getName() + "." + inStreamName + "();"));
        }
        jbs.addStatement(new JCodeStatement(jlvConn.getName() + ".forget(" + jlvKey.getName() + ");"));
        jbs.addStatement(new JCodeStatement(jlvConn.getName() + ".close();"));
        jbs.addStatement(new JCodeStatement(jlvEt.getName() + " = " + jlvConn.getName() + ".getExceptionType();"));

        JIfElseIfElseStatement jiefs = new JIfElseIfElseStatement(new JExpression(new JCodeStatement(jlvEt.getName() + " != null")));
        JIfStatement jis = jiefs.getIfStatement();
        jis.addStatement(new JCodeStatement("throw org.apache.geronimo.interop.rmi.iiop.SystemExceptionFactory.getException(" + jlvConn.getName() + ".getException());"));
        jbs.addStatement(jiefs);

        if (jlvRc != null) {
            String readMethod = getReadMethod(jlvRc);
            String readCall = "";

            if (readMethod != null) {
                readCall = jlvInput.getName() + "." + readMethod + "()";
            } else {
                readCall = "(" + jlvRc.getTypeDecl() + ")" + jlvInput.getName() + "." + "readObject( " + vtc.getValueTypeVarName(jc, jlvRc) + ")";
            }

            jbs.addStatement(new JCodeStatement(jlvRc.getName() + " = " + readCall + ";"));
            jbs.addStatement(new JCodeStatement("return " + jlvRc.getName() + ";"));
        }

        ts.addStatement(jbs);

        JVariable jv = new JVariable(org.apache.geronimo.interop.rmi.iiop.client.RetryInvokeException.class, "$ex");
        JCatchStatement cs = tcfs.newCatch(jv);

        jiefs = new JIfElseIfElseStatement(new JExpression(new JCodeStatement(jlvRetry.getName() + " == 3")));
        jis = jiefs.getIfStatement();
        jis.addStatement(new JCodeStatement("throw " + jv.getName() + ".getRuntimeException();"));
        cs.addStatement(jiefs);

        jfs.addStatement(tcfs);
    }

    protected void addMethod_is_a(JClass jc) {
        JParameter jpID = new JParameter(java.lang.String.class, "id");
        addMethod(jc, "_is_a", new JReturnType(boolean.class),
                  "_is_a",
                  new JParameter[]{jpID},
                  (Class[]) null);

    }

    protected void addMethod(MethodOverload mo, JClass jc) {
        Method m = mo.method;

        String name = m.getName();
        JParameter[] sparms = getMethodParms(m);

        addMethod(jc, mo.iiop_name, new JReturnType(m.getReturnType()),
                  name,
                  sparms,
                  m.getExceptionTypes());

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
        String stubClassName = "";
        JPackage pkg = null;

        while (intf != null && intf.hasNext() ) {
            // Clear the value type cache.
            vtc.clear();

            riClassName = (String)intf.next();
            stubClassName = JavaClass.addPackageSuffix(riClassName, "iiop_stubs") + "_Stub";

            try {
                riClass = getClassLoader().loadClass( riClassName );
            } catch (Exception ex) {
                throw new GenException( "Generate Stubs Failed:", ex );
            }

            String pkgName = JavaClass.getNamePrefix(stubClassName);
            pkg = (JPackage) packages.get( pkgName );
            if (pkg == null)
            {
                pkg = new JPackage( pkgName );
                packages.put( pkgName, pkg );
            }

            String className = JavaClass.getNameSuffix(stubClassName);
            JClass jc = pkg.newClass(className);
            jc.addImport("org.apache.geronimo.interop.rmi.iiop", "ObjectRef");
            jc.setExtends("ObjectRef");
            jc.addImplements(riClass.getName());

            JConstructor jcCon = jc.newConstructor((JParameter[]) null, (Class[]) null);
            jcCon.addStatement(new JCodeStatement("super();"));

            addMethod_is_a(jc);

            Method m[] = getMethods( riClass, go.isSimpleIdl());
            MethodOverload mo[] = null;
            mo = getMethodOverloads( m );
            for (int i = 0; mo != null && i < mo.length; i++) {
                addMethod( mo[i], jc );
            }
        }

        Set pkgSet = packages.keySet();
        Iterator pkgIt = pkgSet.iterator();
        String stubPkg = "";

        while (pkgIt.hasNext())
        {
            stubPkg = (String) pkgIt.next();
            pkg = (JPackage)packages.get(stubPkg);
            System.out.println("Generating Package: " + stubPkg);
            jg.generate(pkg);
        }
    }

    public void compile()
            throws Exception {

        Set pkg = packages.keySet();
        Iterator pkgIt = pkg.iterator();
        String stubPkg = "";

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
            stubPkg = (String) pkgIt.next();
            stubPkg = stubPkg.replace( '.', File.separatorChar );
            filesToCompile = adjustPath(go.getGenSrcDir() + File.separator + stubPkg + File.separator + "*.java");

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

    public static void main(String args[]) throws Exception {
        GenOptions go = null;

        try
        {
            go = new GenOptions( "./stubs", args );
        }
        catch( GenWarning gw )
        {
            gw.printStackTrace();
        }

        ClassLoader cl = ClassLoader.getSystemClassLoader();
        StubCompiler sg = new StubCompiler( go, cl );

        if (go.isGenerate()) {
            sg.generate();
        }

        if (go.isCompile()) {
            sg.compile();
        }
    }
}
