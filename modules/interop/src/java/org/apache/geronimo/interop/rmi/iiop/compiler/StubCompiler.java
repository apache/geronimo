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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.geronimo.interop.generator.GenOptions;
import org.apache.geronimo.interop.generator.JBlockStatement;
import org.apache.geronimo.interop.generator.JCatchStatement;
import org.apache.geronimo.interop.generator.JClass;
import org.apache.geronimo.interop.generator.JCodeStatement;
import org.apache.geronimo.interop.generator.JConstructor;
import org.apache.geronimo.interop.generator.JExpression;
import org.apache.geronimo.interop.generator.JField;
import org.apache.geronimo.interop.generator.JForStatement;
import org.apache.geronimo.interop.generator.JIfElseIfElseStatement;
import org.apache.geronimo.interop.generator.JIfStatement;
import org.apache.geronimo.interop.generator.JLocalVariable;
import org.apache.geronimo.interop.generator.JMethod;
import org.apache.geronimo.interop.generator.JPackage;
import org.apache.geronimo.interop.generator.JParameter;
import org.apache.geronimo.interop.generator.JReturnType;
import org.apache.geronimo.interop.generator.JTryCatchFinallyStatement;
import org.apache.geronimo.interop.generator.JTryStatement;
import org.apache.geronimo.interop.generator.JVariable;
import org.apache.geronimo.interop.generator.JavaGenerator;
import org.apache.geronimo.interop.util.JavaClass;
import org.apache.geronimo.interop.util.ProcessUtil;


public class StubCompiler
        extends Compiler {
    protected ValueTypeContext _vtc = new ValueTypeContext();
    protected static JParameter _objInputVar = new JParameter(org.apache.geronimo.interop.rmi.iiop.ObjectInputStream.class, "input");
    protected static JParameter _objOutputVar = new JParameter(org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream.class, "output");

    protected String _stubClassName = "";
    protected Class _stubClass = null;

    protected String _inStreamName = "getInputStream";
    protected String _outStreamName = "getOutputStream";

    public StubCompiler(Class riClass) {
        super(riClass);
        init();
    }

    public StubCompiler(Class riClass, GenOptions go) {
        super(riClass, go);
        init();
    }

    protected void init() {
        String className = _riClass.getName();
        _stubClassName = JavaClass.addPackageSuffix(className, "iiop_stubs") + "_Stub";
    }

    protected void addMethod(JClass jc, JReturnType jrc, String name, JParameter[] jparms, Class[] excepts) {
//        java.lang.Object $key_1 = $getRequestKey();
//        for (int $retry = 0; ; $retry++)
//        {
//            try
//            {
//                org.apache.geronimo.interop.rmi.iiop.client.Connection $connection_2 = this.$connect();
//                org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream $output_3 = $connection_2.getSimpleOutputStream();  // simple idl
//                org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream $output_3 = $connection_2.getOutputStream();        // rmi-iiop
//                $output_3.writeObject(type$1, p1);
//                $connection_2.invoke(this, "_is_a", $key_1, $retry);
//                org.apache.geronimo.interop.rmi.iiop.ObjectInputStream $input_4 = $connection_2.getSimpleInputStream();     // simple idl
//                org.apache.geronimo.interop.rmi.iiop.ObjectInputStream $input_4 = $connection_2.getInputStream();           // rmi-iiop
//                $connection_2.forget($key_1);
//                $connection_2.close();
//                java.lang.String $et_5 = $connection_2.getExceptionType();
//                if ($et_5 != null)
//                {
//                    throw org.apache.geronimo.interop.rmi.iiop.SystemExceptionFactory.getException($connection_2.getException());
//                }
//                boolean $djc_result;
//                $djc_result = $input_4.readBoolean();
//                return $djc_result;
//            }
//            catch (org.apache.geronimo.interop.rmi.iiop.client.RetryInvokeException $ex_6)
//            {
//                if ($retry == 3)
//                {
//                    throw $ex_6.getRuntimeException();
//                }
//            }
//        }

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
        jbs.addStatement(new JCodeStatement(jlvOutput.getName() + " = " + jlvConn.getName() + "." + _outStreamName + "();"));

        String writeMethod = null;
        String writeCall = "";
        for (int i = 0; i < jparms.length; i++) {
            writeMethod = getWriteMethod(jparms[i]);

            if (writeMethod != null) {
                writeCall = writeMethod + "( " + jparms[i].getName() + " )";
            } else {
                writeCall = "writeObject( " + _vtc.getValueTypeVarName(jc, jparms[i]) + ", " + jparms[i].getName() + ")";
            }

            jbs.addStatement(new JCodeStatement(jlvOutput.getName() + "." + writeCall + ";"));
        }

        jbs.addStatement(new JCodeStatement(jlvConn.getName() + ".invoke(this, \"" + name + "\", " + jlvKey.getName() + ", $retry);"));
        if (jlvRc != null) {
            jbs.addStatement(new JCodeStatement(jlvInput.getName() + " = " + jlvConn.getName() + "." + _inStreamName + "();"));
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
                readCall = "(" + jlvRc.getTypeDecl() + ")" + jlvInput.getName() + "." + "readObject( " + _vtc.getValueTypeVarName(jc, jlvRc) + ")";
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

    // public methods

    public void addMethod_is_a(JClass jc) {
        JParameter jpID = new JParameter(java.lang.String.class, "id");
        addMethod(jc, new JReturnType(boolean.class),
                  "_is_a",
                  new JParameter[]{jpID},
                  (Class[]) null);

    }

    public void addMethod(Method m, JClass jc) {
        String name = m.getName();
        JParameter[] sparms = getMethodParms(m);

        addMethod(jc, new JReturnType(m.getReturnType()),
                  name,
                  sparms,
                  m.getExceptionTypes());

    }

    protected Method[] getMethods() {
        Method myMethods[] = _riClass.getDeclaredMethods();
        Method myOpsMethods[] = null;
        Class myInterfaces[] = _riClass.getInterfaces();

        if (myInterfaces != null && myInterfaces.length > 0) {
            String opsName = _riClass.getName() + "Operations";

            for (int i = 0; i < myInterfaces.length; i++) {
                if (myInterfaces[i].getName().equals(opsName)) {
                    myOpsMethods = myInterfaces[i].getDeclaredMethods();
                    break;
                }
            }
        }

        Method m[] = null;

        if (myOpsMethods == null) {
            m = myMethods;
        } else {
            m = new Method[myMethods.length + myOpsMethods.length];
            System.arraycopy(myMethods, 0, m, 0, myMethods.length);
            System.arraycopy(myOpsMethods, 0, m, myMethods.length, myOpsMethods.length);
        }

        return m;
    }

    public void generate()
            throws Exception {
        _vtc.clear();

        if (_simpleIDL) {
            _inStreamName = "getSimpleInputStream";
            _outStreamName = "getSimpleOutputStream";
        }

        JavaGenerator jg = new JavaGenerator(_genOptions);

        String className;
        JPackage p = new JPackage(JavaClass.getNamePrefix(_stubClassName));
        className = JavaClass.getNameSuffix(_stubClassName);

        JClass jc = p.newClass(className);
        jc.addImport("org.apache.geronimo.interop.rmi.iiop", "ObjectRef");
        jc.setExtends("ObjectRef");
        jc.addImplements(_riClass.getName());

        JField idsField = jc.newField(String[].class, "_ids", new JExpression(new JCodeStatement("{ \"" + _riClass.getName() + "\", \"RMI:" + _riClass.getName() + ":0000000000000000\"}")), true);

        JConstructor jcCon = jc.newConstructor((JParameter[]) null, (Class[]) null);
        jcCon.addStatement(new JCodeStatement("super();"));

        addMethod_is_a(jc);

        Method m[] = null;
        m = getMethods();
        for (int i = 0; m != null && i < m.length; i++) {
            addMethod(m[i], jc);
        }

        jg.generate(p);
    }

    public void compile()
            throws Exception {
        String className = _riClass.getName();
        String stubClassName = JavaClass.addPackageSuffix(className, "iiop_stubs");
        String stubPackage = JavaClass.getNamePrefix(stubClassName);

        System.out.println("Compiling Package: " + stubPackage);
        System.out.println("Compiling Stub: " + stubClassName);

        String javac = "javac -d ../classes -classpath ../classes;D:/Dev/3rdparty.jag/ejb21/ejb-2_1-api.jar " + stubPackage.replace('.', '/') + "/*.java";

        ProcessUtil pu = ProcessUtil.getInstance();
        pu.setEcho(System.out);
        pu.run(javac, (String[]) null, "./src");
    }

    public Class getStubClass() {
        System.out.println("StubCompiler.getStubClass(): riClass: " + _riClass);

        if (_stubClass == null) {
            try {
                _stubClass = Class.forName(_stubClassName);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (_stubClass == null) {
                    generate();
                    compile();
                    _stubClass = Class.forName(_stubClassName);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return _stubClass;
    }

    public static void main(String args[])
            throws Exception {
        boolean generate = false;
        boolean compile = false;
        boolean loadclass = false;
        boolean simpleidl = false;
        List interfaces = new LinkedList();
        GenOptions go = new GenOptions();

        go.setGenDir("./");
        go.setOverwrite(false);
        go.setVerbose(false);

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-g")) {
                generate = true;
            } else if (args[i].equals("-c")) {
                compile = true;
            } else if (args[i].equals("-l")) {
                loadclass = true;
            } else if (args[i].equals("-s")) {
                simpleidl = true;
            } else if (args[i].equals("-d") && ((i + 1) < args.length)) {
                go.setGenDir(args[++i]);
            } else if (args[i].equals("-v")) {
                go.setVerbose(true);
            } else if (args[i].equals("-o")) {
                go.setOverwrite(true);
            } else if (args[i].startsWith("-")) {
                System.out.println("Warning: Ignoring unrecognized options: '" + args[i] + "'");
            } else {
                interfaces.add(args[i]);
            }
        }

        Iterator i = interfaces.iterator();
        while (i != null && i.hasNext()) {
            String intfName = (String) i.next();

            if (intfName.startsWith("RMI:")) {
                simpleidl = false;
                intfName = intfName.substring(4);
            } else if (intfName.startsWith("IDL:")) {
                simpleidl = true;
                intfName = intfName.substring(4);
            }

            Class riClass = Class.forName(intfName);
            StubCompiler sg = new StubCompiler(riClass, go);
            sg.setSimpleIDL(simpleidl);

            if (generate) {
                sg.generate();
            }

            if (compile) {
                sg.compile();
            }

            if (loadclass) {
                Class c = sg.getStubClass();
                System.out.println("StubClass: " + c);
            }
        }

        // sg.setSimpleIDL( true );
        // sg.generate( "org.apache.geronimo.interop.rmi.iiop.NameServiceOperations");
    }

}
