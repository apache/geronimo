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
package org.apache.geronimo.interop.rmi.iiop.portable;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.geronimo.interop.generator.GenException;
import org.apache.geronimo.interop.generator.GenOptions;
import org.apache.geronimo.interop.generator.JCatchStatement;
import org.apache.geronimo.interop.generator.JClass;
import org.apache.geronimo.interop.generator.JCodeStatement;
import org.apache.geronimo.interop.generator.JExpression;
import org.apache.geronimo.interop.generator.JField;
import org.apache.geronimo.interop.generator.JIfStatement;
import org.apache.geronimo.interop.generator.JLocalVariable;
import org.apache.geronimo.interop.generator.JMethod;
import org.apache.geronimo.interop.generator.JPackage;
import org.apache.geronimo.interop.generator.JParameter;
import org.apache.geronimo.interop.generator.JReturnType;
import org.apache.geronimo.interop.generator.JTryCatchFinallyStatement;
import org.apache.geronimo.interop.generator.JTryStatement;
import org.apache.geronimo.interop.generator.JType;
import org.apache.geronimo.interop.generator.JVariable;
import org.apache.geronimo.interop.generator.JavaGenerator;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.portable.ApplicationException;
import org.omg.CORBA.portable.IDLEntity;
import org.omg.CORBA.portable.RemarshalException;

public class PortableStubCompiler {
    private HashMap packages = new HashMap();
    private final GenOptions genOptions;
    private final ClassLoader classLoader;

    public PortableStubCompiler(GenOptions genOptions, ClassLoader classLoader) {
        this.genOptions = genOptions;
        this.classLoader = classLoader;
    }

    public void generate() throws GenException {
        JavaGenerator javaGenerator = new JavaGenerator(genOptions);

        List interfaces = genOptions.getInterfaces();
        for (Iterator iterator = interfaces.iterator(); iterator.hasNext();) {
            String interfaceName = (String) iterator.next();

            // load the interface class
            Class interfaceClass = null;
            try {
                interfaceClass = classLoader.loadClass(interfaceName);
            } catch (Exception ex) {
                throw new GenException("Generate Stubs Failed:", ex);
            }


            // get the package object
            String packageName = getPackageName(interfaceName);
            JPackage jpackage = (JPackage) packages.get(packageName);
            if (jpackage == null) {
                jpackage = new JPackage(packageName);
                packages.put(packageName, jpackage);
            }


            // build the basic class object
            String className = "_" + getClassName(interfaceName) + "_Stub_DAIN";
            JClass jclass = jpackage.newClass(className);
            jclass.addImport("javax.rmi.CORBA", "Stub");
            jclass.setExtends("Stub");
            jclass.addImplements(interfaceClass.getName());

            addMethod_ids(jclass, interfaceName);

            IiopOperation[] iiopOperations = createIiopOperations(interfaceClass);
            for (int i = 0; iiopOperations != null && i < iiopOperations.length; i++) {
                addMethod(iiopOperations[i], jclass);
            }
        }

        for (Iterator iterator = packages.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String packageName = (String) entry.getKey();
            JPackage jpackage = (JPackage) entry.getValue();

            System.out.println("Generating Package: " + packageName);
            javaGenerator.generate(jpackage);
        }
    }

    private static String getClassName(String interfaceName) {
        int endIndex = interfaceName.lastIndexOf('.');
        if (endIndex < 0) {
            return interfaceName;
        }
        return interfaceName.substring(endIndex + 1);
    }

    private static String getPackageName(String interfaceName) {
        int endIndex = interfaceName.lastIndexOf('.');
        if (endIndex < 0) {
            return "";
        }
        return interfaceName.substring(0, endIndex);
    }

    public static Method[] getAllMethods(Class intfClass) {
        LinkedList methods = new LinkedList();

        LinkedList stack = new LinkedList();
        stack.addFirst(intfClass);

        Set visited = new HashSet();
        while (!stack.isEmpty()) {
            Class intf = (Class) stack.removeFirst();
            methods.addAll(Arrays.asList(intf.getDeclaredMethods()));
            visited.add(intf);

            Class myInterfaces[] = intfClass.getInterfaces();
            for (int i = 0; i < myInterfaces.length; i++) {
                Class myInterface = myInterfaces[i];
                if (!visited.contains(myInterface)) {
                    stack.addFirst(myInterface);
                }
            }
        }

        return (Method[]) methods.toArray(new Method[methods.size()]);
    }

    public static IiopOperation[] createIiopOperations(Class intfClass) {
        Method[] methods = getAllMethods(intfClass);

        // index the methods by name
        HashMap methodsByName = new HashMap(methods.length);
        for (int i = 0; i < methods.length; i++) {
            List methodList = (ArrayList) methodsByName.get(methods[i].getName());
            if (methodList == null) {
                methodList = new ArrayList(methods.length);
                methodsByName.put(methods[i].getName(), methodList);
            }
            methodList.add(methods[i]);
        }

        List overloadList = new ArrayList(methodsByName.size());
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            String iiopName = method.getName();
            if (((List) methodsByName.get(method.getName())).size() > 1) {
                iiopName = buildOverloadMethodName(method);
            }
            overloadList.add(new IiopOperation(iiopName, method));
        }

        return (IiopOperation[]) overloadList.toArray(new IiopOperation[overloadList.size()]);
    }

    public static String buildOverloadMethodName(Method method) {
        Class parameterTypes[] = method.getParameterTypes();
        String name = method.getName() + "_";
        if (parameterTypes.length ==0) {
            name += "_";
        } else {
            for (int i = 0; i < parameterTypes.length; i++) {
                Class parameterType = parameterTypes[i];
                name += buildOverloadParameterName(parameterType);
            }
        }
        return name.replace('.', '_');
    }

    public static String buildOverloadParameterName(Class parameterType) {
        String name = "";

        int arrayDimensions = 0;
        while (parameterType.isArray()) {
            arrayDimensions++;
            parameterType = parameterType.getComponentType();
        }

        // arrays start with org_omg_boxedRMI_
        if (arrayDimensions > 0) {
            name += "_org_omg_boxedRMI";
        }

        // IDLEntity types must be prefixed with org_omg_boxedIDL_
        if (IDLEntity.class.isAssignableFrom(parameterType)) {
            name += "_org_omg_boxedIDL";
        }

        // determine the parameterType name... this is overriden for special corba types
        String parameterTypeName = (String) overloadTypes.get(parameterType.getName());
        if (parameterTypeName == null) {
            parameterTypeName = parameterType.getName();
        }

        // add package
        String packageName = getPackageName(parameterTypeName);
        if (packageName.length() > 0) {
            name += "_" + packageName;
        }

        // arrays now contain a dimension indicator
        if (arrayDimensions > 0) {
            name += "_" + "seq" + arrayDimensions;
        }

        // add the class name
        name += "_" + getClassName(parameterTypeName);

        return name;
    }

    private void addMethod(JClass jclass, String iiopMethodName, JReturnType jreturnType, String name, JParameter[] jparameters, Class[] exceptions) {
        //
        // Method Template:
        //
        // if (!Util.isLocal(this)) {
        //     try {
        //         org.omg.CORBA_2_3.portable.InputStream in = null;
        //         try {
        //             org.omg.CORBA_2_3.portable.OutputStream out =
        //                 (org.omg.CORBA_2_3.portable.OutputStream)
        //                 _request("passAndReturnCheese__org_apache_geronimo_interop_rmi_iiop_compiler_Cheese", true);
        //             out.write_value((Serializable)arg0,Cheese.class);
        //             in = (org.omg.CORBA_2_3.portable.InputStream)_invoke(out);
        //             return (Cheese) in.read_value(Cheese.class);
        //         } catch (ApplicationException ex) {
        //             in = (org.omg.CORBA_2_3.portable.InputStream) ex.getInputStream();
        //             String id = in.read_string();
        //             throw new UnexpectedException(id);
        //         } catch (RemarshalException ex) {
        //             return passAndReturnCheese(arg0);
        //         } finally {
        //             _releaseReply(in);
        //         }
        //     } catch (SystemException ex) {
        //         throw Util.mapSystemException(ex);
        //     }
        // } else {
        //     ServantObject so = _servant_preinvoke("passAndReturnCheese__org_apache_geronimo_interop_rmi_iiop_compiler_Cheese",Foo.class);
        //     if (so == null) {
        //         return passAndReturnCheese(arg0);
        //     }
        //     try {
        //         Cheese arg0Copy = (Cheese) Util.copyObject(arg0,_orb());
        //         Cheese result = ((Foo)so.servant).passAndReturnCheese(arg0Copy);
        //         return (Cheese)Util.copyObject(result,_orb());
        //     } catch (Throwable ex) {
        //         Throwable exCopy = (Throwable)Util.copyObject(ex,_orb());
        //         throw Util.wrapException(exCopy);
        //     } finally {
        //         _servant_postinvoke(so);
        //     }
        // }

        JMethod jmethod = jclass.newMethod(jreturnType, name, jparameters, exceptions);

        JTryCatchFinallyStatement outerTryCatchFinally = new JTryCatchFinallyStatement();
        jmethod.addStatement(outerTryCatchFinally);
        JTryStatement outerTry = outerTryCatchFinally.getTryStatement();

        JLocalVariable inVar = outerTry.newLocalVariable(org.omg.CORBA_2_3.portable.InputStream.class,
                "in",
                new JExpression(new JCodeStatement("null")));

        JTryCatchFinallyStatement innterTryCatchFinally = new JTryCatchFinallyStatement();
        outerTry.addStatement(innterTryCatchFinally);
        JTryStatement innerTry = innterTryCatchFinally.getTryStatement();

        JLocalVariable outVar = innerTry.newLocalVariable(org.omg.CORBA_2_3.portable.OutputStream.class,
                "out",
                new JExpression(new JCodeStatement("(" + org.omg.CORBA_2_3.portable.OutputStream.class.getName() + ") _request(\"" + iiopMethodName + "\", true)")));

        // Write the variables
        for (int i = 0; i < jparameters.length; i++) {
            JParameter jparameter = jparameters[i];

            String writeMethod = getWriteMethod(jparameter);
            String writeCall;
            if (writeMethod != null) {
                writeCall = writeMethod + "( " + jparameter.getName() + " )";
            } else {
                String cast = "";
                if (!Serializable.class.isAssignableFrom(jparameter.getType())) {
                    cast = "(java.io.Serializable)";
                }
                writeCall = "write_value(" + cast + jparameter.getName() + ", " + jparameter.getTypeDecl() + ".class)";
            }

            innerTry.addStatement(new JCodeStatement(outVar.getName() + "." + writeCall + ";"));
        }

        // invoke the method
        String invoke = "_invoke(" + outVar.getName() + ");";
        if (jreturnType.getType() != Void.TYPE) {
            invoke = inVar.getName() + " = (" + inVar.getTypeDecl() + ")" + invoke;
        }
        innerTry.addStatement(new JCodeStatement(invoke));

        // read the return value
        if (jreturnType.getType() != Void.TYPE) {
            String readMethod = getReadMethod(jreturnType);
            String readCall = "";

            if (readMethod != null) {
                readCall = inVar.getName() + "." + readMethod + "()";
            } else {
                readCall = "(" + jreturnType.getTypeDecl() + ")" + inVar.getName() + ".read_value( " + jreturnType.getTypeDecl() + ".class)";
            }
            innerTry.addStatement(new JCodeStatement("return " + readCall + ";"));
        }

        JVariable exVar = new JVariable(ApplicationException.class, "ex");
        JCatchStatement jcatchStatement = innterTryCatchFinally.newCatch(exVar);

        jcatchStatement.addStatement(new JCodeStatement(inVar.getName() + " = (" + inVar.getTypeDecl() + ") " + exVar.getName() + ".getInputStream();"));
        JLocalVariable idVar = jcatchStatement.newLocalVariable(String.class,
                "id",
                new JExpression(new JCodeStatement(inVar.getName() + ".read_string()")));
//        if (id.equals("IDL:org/apache/geronimo/interop/rmi/iiop/compiler/other/BlahEx:1.0")) {
//            throw (BlahException) in.read_value(BlahException.class);
//        }
//        if (id.equals("IDL:org/apache/geronimo/interop/rmi/iiop/compiler/BooEx:1.0")) {
//            throw (BooException) in.read_value(BooException.class);
//        }
        for (int i = 0; i < exceptions.length; i++) {
            Class exception = exceptions[i];
            if (exception.equals(RemoteException.class)) {
                continue;
            }
            String exceptionName = exception.getName().replace('.', '/');
            if (exceptionName.endsWith("Exception")) {
                exceptionName = exceptionName.substring(0, exceptionName.length() - "Exception".length());
            }
            exceptionName += "Ex";
            JIfStatement jif = new JIfStatement(new JExpression(new JCodeStatement(idVar.getName() + ".equals(\"IDL:" + exceptionName + ":1.0\")")));
            jif.addStatement(new JCodeStatement("throw (" + exception.getName() + ") in.read_value(" + exception.getName() + ".class);"));
            jcatchStatement.addStatement(jif);

        }
        jcatchStatement.addStatement(new JCodeStatement("throw new java.rmi.UnexpectedException(" + idVar.getName() + ");"));

        //         } catch (RemarshalException ex) {
        //             return passAndReturnCheese(arg0);
        exVar = new JVariable(RemarshalException.class, "ex");
        jcatchStatement = innterTryCatchFinally.newCatch(exVar);

        String remarshal = name + "(";
        for (int i = 0; i < jparameters.length; i++) {
            JParameter jparameter = jparameters[i];
            if (i > 0) {
                remarshal += ", ";
            }
            remarshal += jparameter.getName();
        }
        remarshal += ");";
        if (jreturnType.getType() != Void.TYPE) {
            remarshal = "return " + remarshal;
        }
        jcatchStatement.addStatement(new JCodeStatement(remarshal));

        //         } finally {
        //             _releaseReply(in);
//        JBlockStatement jfinally = new JBlockStatement();
        innterTryCatchFinally.addFinallyStatement(new JCodeStatement("_releaseReply(" + inVar.getName() + ");"));
//        jfinally.addStatement(new JCodeStatement("_releaseReply(" + inVar.getName() + ");"));

        //     } catch (SystemException ex) {
        //         throw Util.mapSystemException(ex);
        exVar = new JVariable(SystemException.class, "ex");
        jcatchStatement = outerTryCatchFinally.newCatch(exVar);
        jcatchStatement.addStatement(new JCodeStatement("throw javax.rmi.CORBA.Util.mapSystemException(" + exVar.getName() + ");"));
    }

    private void addMethod_ids(JClass jclass, String interfaceName) {
        //
        // Method Template:
        //
        // private static final String[] _type_ids = {
        //     "RMI:org.apache.geronimo.interop.rmi.iiop.portable.Foo:0000000000000000"
        // };
        //
        // public String[] getIds()
        // {
        //     return _type_ids;
        // }
        //

        JField typesField = jclass.newField(String[].class, "_type_ids", new JExpression(new JCodeStatement("{ \"RMI:" + interfaceName + ":0000000000000000\" }")));
        typesField.setModifiers(Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL);

        JMethod jmethod = jclass.newMethod(new JReturnType(String[].class), "_ids", null, null);
        jmethod.addStatement(new JCodeStatement("return _type_ids;"));
    }


    private void addMethod(IiopOperation iiopOperation, JClass jclass) {
        Method method = iiopOperation.getMethod();

        JReturnType jreturnType = new JReturnType(method.getReturnType());

        Class[] parameterTypes = method.getParameterTypes();
        JParameter[] jparameters = new JParameter[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            jparameters[i] = new JParameter(parameterTypes[i], "arg" + i);
        }

        addMethod(jclass,
                iiopOperation.getName(),
                jreturnType,
                method.getName(),
                jparameters,
                method.getExceptionTypes());
    }

//    public void compile()
//            throws Exception {
//
//        Set pkg = packages.keySet();
//        Iterator pkgIt = pkg.iterator();
//        String stubPkg = "";
//
//        /*
//         * Each of the packages were generated under go.getGenSrcDir().
//         *
//         * Go through all the packages and run the compiler on *.java
//         */
//        String classpath = new File(genOptions.getClasspath()).getAbsolutePath();
//        String srcpath = new File(genOptions.getGenSrcDir()).getAbsolutePath();
//
//        String filesToCompile = "";
//        String javacCmd = "";
//
//        while (pkgIt.hasNext()) {
//            stubPkg = (String) pkgIt.next();
//            stubPkg = stubPkg.replace('.', File.separatorChar);
//            filesToCompile = new File(new File(genOptions.getGenSrcDir(), stubPkg), "*.java").getAbsolutePath();
//
//            System.out.println("Compiling Package: " + filesToCompile);
//
//            javacCmd = "javac -d " + genOptions.getGenClassDir() +
//                    (genOptions.isCompileDebug() ? " -g" : "") +
//                    " -classpath " + classpath + " " +
//                    " -sourcepath " + srcpath + " " + filesToCompile;
//
//            System.out.println("Lauching: " + javacCmd);
//
//            ProcessUtil pu = ProcessUtil.getInstance();
//            pu.setEcho(System.out);
//            pu.run(javacCmd, null, "./");
//        }
//    }

    private static HashMap readMethods;
    private static HashMap writeMethods;
    private static HashMap overloadTypes;

    static {
        readMethods = new HashMap();
        readMethods.put("boolean", "read_boolean");
        readMethods.put("char", "read_wchar");
        readMethods.put("byte", "read_octet");
        readMethods.put("short", "read_short");
        readMethods.put("int", "read_long");
        readMethods.put("long", "read_longlong");
        readMethods.put("float", "read_float");
        readMethods.put("double", "read_double");
        readMethods.put("org.omg.CORBA.Object", "read_Object");

        writeMethods = new HashMap();
        writeMethods.put("boolean", "write_boolean");
        writeMethods.put("char", "write_wchar");
        writeMethods.put("byte", "write_octet");
        writeMethods.put("short", "write_short");
        writeMethods.put("int", "write_long");
        writeMethods.put("long", "write_longlong");
        writeMethods.put("float", "write_float");
        writeMethods.put("double", "write_double");
        writeMethods.put("org.omg.CORBA.Object", "write_Object");

        overloadTypes = new HashMap();
        overloadTypes.put("boolean", "boolean");
        overloadTypes.put("char", "wchar");
        overloadTypes.put("byte", "octet");
        overloadTypes.put("short", "short");
        overloadTypes.put("int", "long");
        overloadTypes.put("long", "long_long");
        overloadTypes.put("float", "float");
        overloadTypes.put("double", "double");
        overloadTypes.put("java.lang.Class", "javax.rmi.CORBA.ClassDesc");
        overloadTypes.put("java.lang.String", "CORBA.WStringValue");
        overloadTypes.put("org.omg.CORBA.Object", "Object");
    }

    protected String getWriteMethod(JVariable jvariable) {
        if (jvariable != null) {
            return (String) writeMethods.get(jvariable.getTypeDecl());
        }
        return null;
    }

    protected String getReadMethod(JType jtype) {
        if (jtype != null) {
            return (String) readMethods.get(jtype.getTypeDecl());
        }
        return null;
    }

}
